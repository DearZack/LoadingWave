package io.github.dearzack.loading;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Zack on 2017/5/2.
 */

public class LoadingView extends View {

    private static final String TAG = "LoadingView";

    private static final float DEFAULT_WATER_LEVEL_RATIO = 0.5f;
    private static final float DEFAULT_AMPLITUDE_RATIO = 0.1f;

    private int mWidth, mHeight;
    private Context mContext;
    private Paint waterPaint;
    private Path waterPath;
    private Matrix matrix;
    private BitmapShader waveShader;

    private float waterLevel;//水位高度

    private int waterPresent = 40;

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
        TypedArray typedArray = mContext.obtainStyledAttributes(attrs, R.styleable.LoadingView);
        waterPresent = typedArray.getInteger(R.styleable.LoadingView_waterPresent, waterPresent);
        typedArray.recycle();

        waterPaint = new Paint();
        waterPaint.setColor(Color.BLUE);
        waterPaint.setAntiAlias(true);
        waterPaint.setStyle(Paint.Style.FILL);

        waterPath = new Path();
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
            matrix.setScale(1, 0.5f, 1, waterLevel);
            matrix.postTranslate(0, 0);
            waveShader.setLocalMatrix(matrix);
            float radius = mWidth / 2f;
            canvas.drawCircle(mWidth / 2f, mHeight / 2f, radius, waterPaint);
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
//        waterPath.moveTo(0, (mHeight * (100 - waterPresent)) / 100);
//        waterPath.arcTo(new RectF(0, 0, mWidth, mHeight), 0, 180, true);
//        waterPath.close();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        setWaveShader();
    }

    private void setWaveShader() {
        double defaultAngularFrequency = 2.0f * Math.PI  / mWidth;
        float defaultAmplitude = mHeight * DEFAULT_AMPLITUDE_RATIO;
        waterLevel = mHeight * DEFAULT_WATER_LEVEL_RATIO;
        float waveLength = mWidth;

        Bitmap bitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint wavePaint = new Paint();
        wavePaint.setStrokeWidth(2);
        wavePaint.setAntiAlias(true);

        final int endX = mWidth + 1;
        final int endY = mHeight + 1;

        float[] waveY = new float[endX];

        //绘制背后颜色较浅部分，有3D效果
        wavePaint.setColor(addAlpha(Color.BLACK, 0.3f));
        for (int beginX = 0; beginX < endX; beginX++) {
            double wx = beginX * defaultAngularFrequency;
            float beginY = (float) (waterLevel + defaultAmplitude * Math.sin(wx));
            canvas.drawLine(beginX, beginY, beginX, endY, wavePaint);
            waveY[beginX] = beginY;
        }

        //绘制前面颜色较深部分
        wavePaint.setColor(Color.BLACK);
        final int wave2Shift = (int) (waveLength / 4);
        for (int beginX = 0; beginX < endX; beginX++) {
            //相差四分之一个周期
            canvas.drawLine(beginX, waveY[(beginX + wave2Shift) % endX], beginX, endY, wavePaint);
        }

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
}
