# LoadingWave

**High imitation Baidu post bar loading animation**

<img src="./screenShot/loading.gif">

## Usage
### Step 1. Add the JitPack repository to your build file
Add it in your root build.gradle at the end of repositories:

```groovy
allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```

### Step 2. Add the dependency
```groovy
dependencies {
	        compile 'com.github.DearZack:LoadingWave:v1.0'
	}
```

### Step 3
Include the LoadingView widget in your layout. And you can customize it like this.
```xml
<io.github.dearzack.loading.LoadingView
        android:layout_width="100dp"
        android:layout_height="100dp"
        app:textColor="#ffffff"
        app:waterColor="#3bacfc"
        app:waterPercent="50"
        app:waterText="Z" />
```

## Customization

name | format | description
---|---|---
textColor | color | default #ffffff
waterColor | color | default #3bacfc
waterPercent | float | default 50
waterText | string | default "贴"

## Change Log


## License

    Copyright 2016 Zack

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.

