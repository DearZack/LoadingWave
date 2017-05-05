package io.github.dearzack.loading;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

/**
 * Created by Zack on 2017/5/2.
 */

public class LoadingView extends View {

    private static final String TAG = "LoadingView";

    private static final float DEFAULT_WATER_LEVEL_RATIO = 0.5f;
    private static final float DEFAULT_AMPLITUDE_RATIO = 0.1f;

    private int mWidth, mHeight;
    private Context mContext;
    private Paint waterPaint, waterTextPaint, normaTextPaint;
    private Matrix matrix;
    private BitmapShader waveShader, waveTextShader;
    private float waveShiftRatio;

    private float waterLevel;//水位高度
    private int textDrawX, textDrawY;
    private ObjectAnimator waveShiftAnimator;
    private AnimatorSet animatorSet;

    private String waterText;
    private int waterColor, textColor;
    private float waterPercent;

    public LoadingView(Context context) {
        this(context, null);
    }

    public LoadingView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        matrix = new Matrix();
        initAnimation();
        TypedArray typedArray = mContext.obtainStyledAttributes(attrs, R.styleable.LoadingView);
        waterText = typedArray.getString(R.styleable.LoadingView_waterText);
        waterColor = typedArray.getColor(R.styleable.LoadingView_waterColor, Color.parseColor("#3bacfc"));
        textColor = typedArray.getColor(R.styleable.LoadingView_textColor, Color.WHITE);
        waterPercent = typedArray.getFloat(R.styleable.LoadingView_waterPercent, 50.0f);
        typedArray.recycle();

        if (TextUtils.isEmpty(waterText)) {
            waterText = "贴";
        }

        if (waterText.length() > 1) {
            waterText = waterText.substring(0, 1);
        }

        if (waterPercent > 100) {
            waterPercent = 100;
        } else if (waterPercent < 0) {
            waterPercent = 0;
        }

        waterPaint = new Paint();
        waterPaint.setAntiAlias(true);
        waterPaint.setStyle(Paint.Style.FILL);

        waterTextPaint = new Paint();
        waterTextPaint.setAntiAlias(true);
        waterTextPaint.setStyle(Paint.Style.FILL);

        normaTextPaint = new Paint();
        normaTextPaint.setAntiAlias(true);
        normaTextPaint.setStyle(Paint.Style.FILL);
    }

    private void initAnimation() {
        waveShiftAnimator = ObjectAnimator.ofFloat(this, "waveShiftRatio", 0f, 1f);
        waveShiftAnimator.setRepeatCount(ValueAnimator.INFINITE);
        waveShiftAnimator.setDuration(1000);
        waveShiftAnimator.setInterpolator(new LinearInterpolator());
        animatorSet = new AnimatorSet();
        animatorSet.play(waveShiftAnimator);
        animatorSet.start();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (width < height) {
            width = height;
        } else {
            height = width;
        }
        mWidth = measureDimension(width, widthMeasureSpec);
        mHeight = measureDimension(height, heightMeasureSpec);
        setMeasuredDimension(mWidth, mHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (waveShader != null) {
            if (waterPaint.getShader() == null) {
                waterPaint.setShader(waveShader);
            }
            matrix.setScale(1, 0.5f, 0, waterLevel);
            //set***、post***、pre***的区别在于
            //set是直接设置Matrix的值，每次set一次，整个Matrix的数组都会变掉。
            //post是后乘，当前的矩阵乘以参数给出的矩阵。可以连续多次使用post，来完成所需的整个变换。
            //pre是前乘，参数给出的矩阵乘以当前的矩阵。所以操作是在当前矩阵的最前面发生的。
//            matrix.setTranslate(waveShiftRatio * mWidth, 0);
            matrix.postTranslate(waveShiftRatio * mWidth, 0);
            waveShader.setLocalMatrix(matrix);
            waveTextShader.setLocalMatrix(matrix);
            float radius = mWidth / 2f;
            canvas.drawText(waterText, textDrawX, textDrawY, normaTextPaint);
            canvas.drawCircle(mWidth / 2f, mHeight / 2f, radius, waterPaint);
            canvas.drawText(waterText, textDrawX, textDrawY, waterTextPaint);
        }
    }

    public int measureDimension(int defaultSize, int measureSpec) {
        int result;

        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = defaultSize;   //UNSPECIFIED
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        //这里才可以拿到width和height的值
        waterTextPaint.setTextSize(mWidth / 2);
        normaTextPaint.setTextSize(mWidth / 2);
        Rect rect = new Rect();
        normaTextPaint.getTextBounds(waterText, 0, waterText.length(), rect);
        int width = rect.width();//文字宽
        int height = rect.height();//文字高
        textDrawX = (mWidth - width) / 2;
        textDrawY = (mHeight + height) / 2;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        setWaveShader();
    }

    @Override
    protected void onDetachedFromWindow() {
        if (animatorSet != null) {
            animatorSet.cancel();
        }
        super.onDetachedFromWindow();
    }

    private void setWaveShader() {
        double defaultAngularFrequency = 2.0f * Math.PI / mWidth;
        float defaultAmplitude = mHeight * DEFAULT_AMPLITUDE_RATIO;
        waterLevel = mHeight * (1 - waterPercent / 100);
        float waveLength = mWidth;

        if (mWidth <= 0 || mHeight <= 0) {
            return;
        }

        Bitmap bitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Bitmap bitmapText = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
        Canvas canvasText = new Canvas(bitmapText);

        Paint wavePaint = new Paint();
        wavePaint.setStrokeWidth(2);
        wavePaint.setAntiAlias(true);
        Paint wavePaintText = new Paint();
        wavePaintText.setStrokeWidth(2);
        wavePaintText.setAntiAlias(true);
        wavePaintText.setColor(textColor);
        normaTextPaint.setColor(waterColor);

        final int endX = mWidth + 1;
        final int endY = mHeight + 1;

        float[] waveY = new float[endX];

        //绘制背后颜色较浅部分，有3D效果
        wavePaint.setColor(addAlpha(waterColor, 0.3f));
        for (int beginX = 0; beginX < endX; beginX++) {
            double wx = beginX * defaultAngularFrequency;
            float beginY = (float) (waterLevel + defaultAmplitude * Math.sin(wx));
            canvas.drawLine(beginX, beginY, beginX, endY, wavePaint);
            canvasText.drawLine(beginX, beginY, beginX, endY, wavePaintText);
            waveY[beginX] = beginY;
        }

        //绘制前面颜色较深部分
        wavePaint.setColor(waterColor);
        final int wave2Shift = (int) (waveLength / 4);
        for (int beginX = 0; beginX < endX; beginX++) {
            //相差四分之一个周期
            canvas.drawLine(beginX, waveY[(beginX + wave2Shift) % endX], beginX, endY, wavePaint);
            canvasText.drawLine(beginX, waveY[(beginX + wave2Shift) % endX], beginX, endY, wavePaintText);
        }

        waveTextShader = new BitmapShader(bitmapText, Shader.TileMode.REPEAT, Shader.TileMode.CLAMP);
        waterTextPaint.setShader(waveTextShader);

        //x轴复制，y轴拉伸，这里拉伸是指将最后一个像素一直复制下去，和常见的图片拉伸的意思不太一样
        waveShader = new BitmapShader(bitmap, Shader.TileMode.REPEAT, Shader.TileMode.CLAMP);
        waterPaint.setShader(waveShader);
    }

    private int addAlpha(int color, float factor) {
        int alpha = Math.round(Color.alpha(color) * factor);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }

    public void setWaveShiftRatio(float waveShiftRatio) {
        if (this.waveShiftRatio != waveShiftRatio) {
            this.waveShiftRatio = waveShiftRatio;
            invalidate();
        }
    }

    public int getWaterColor() {
        return waterColor;
    }

    public void setWaterColor(int waterColor) {
        this.waterColor = waterColor;
        setWaveShader();
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
        setWaveShader();
    }

    public float getWaterPercent() {
        return waterPercent;
    }

    public void setWaterPercent(float waterPercent) {
        this.waterPercent = waterPercent;
        setWaveShader();
    }
}
