---
title: Android开发艺术探索 第十三章 综合技术
date: 2018-01-27 17:08:33
top : 213
tags:
- 进阶
- Android开发艺术探索
categories: android
---
# 引子
这一章是一些使用频率比较低，但是比较重要的东西。很多东西由于google推出了兼容库解决。所以有一部分内容一笔带过

# 使用CrashHandler来获取应用的crash信息
这个是系统为我们提供的，当发生奔溃时可以获取奔溃信息

```
public class CrashHandler   implements Thread.UncaughtExceptionHandler{
    @Override
    public void uncaughtException(Thread t, Throwable e) {
        
    }
}

```
在这里可以获取异常信息，并可以保存日志，现在都用的是腾讯bugly或者友盟奔溃日志系统。或者公司要求自己定制

# 方法数超出
由于单个dex文件的限制，单个dex文件中方法不能超过64k。当初google认为一个app方法数不可能超过64k，现在打脸了。
现在google提供了兼容库，可以解决这个问题，解决问题的方法是dex分包
当前只需要在gradle中配置
```
android {
    defaultConfig {
        multiDexEnabled true
    }
}

dependencies {
    implementation  'com.android.support:multidex:1.0.2'
}


```

然后自定义application继承MultiDexApplication
```
public class MyApplication extends MultiDexApplication {
    @Override
        protected void attachBaseContext(Context base) {
            super.attachBaseContext(base);
            MultiDex.install(this); // 初始化
        }
}
```

这样就解决了

# android 动态加载技术
这个后面会专门写一片博客

# 反编译初步
这个后面会专门写一片博客