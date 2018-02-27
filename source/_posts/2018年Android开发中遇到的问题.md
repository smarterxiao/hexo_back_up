---
title: 2018年Android开发中遇到的问题
date: 2018-02-26 21:56:17
tags:
- Android
- 问题
- 日记
- 2018年
---

## 2018.2.26
今天遇到了一个问题是在ScrollView中加载长图片，在某些手机上会显示白屏的问题
报错信息是这个
```
16:31:09.49222636OpenGLRendererBitmap too large to be uploaded into a texture (1125x4359, max=4096x4096
```
这个是由于开启了硬件加速之后，android openGL对图片的绘制大小有要求，最大是`max=4096x4096` 所以最简单的解决办法就是关闭硬件加速
这里有四个级别的硬件加速，在小米4手机上面展示1000*2096分辨率的长图片会出现这个问题，或者使用短一点的图片尺寸小于 2750px
* Application
```
<application
    android:hardwareAccelerated="false"
...>
</application>
```

* Activity
```
<application
    android:hardwareAccelerated="true">
    <activity ... />
    <activity android:hardwareAccelerated="false" />
</application>
```
* Window
```
getWindow().setFlags(
   WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
   WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
```
* View
```
myView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
```
