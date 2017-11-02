---
title: Android群英传
date: 2017-11-01 22:17:07
tags:
---

# 简介
这本书是之前一直想整理的书籍 ，只要是Android基础的书籍，主要还是吧自己常用的整理一下方便查找。这本书是2015的版本。

# 第一章 Android 体系与系统架构

## 小结
1. 一个网站：http://androidxref.com/  这个是一个Android的源码查看网站

# 第二章 Android 开发工具新接触

## 1. 环境配置

1. 一个常用的Android镜像网站 http://www.androiddevtools.cn/

## 2. ADB 的使用
1. 将ADB加入环境变量方便使用
    如果加入成功，那么运行`adb version`可以得到下面的结果

    ```
    C:\Users\smart>adb version
    Android Debug Bridge version 1.0.39
    Revision 3db08f2c6889-android
    Installed as D:\sdk\platform-tools\adb.exe
    ```

2. ADB 的时候连不上手机

  windows 检查一下驱动，可以使用应用宝这些安装驱动

3. ADB 常用命令
  * adb shell  
> 这个就可以使用shell命令了，linux是基于Linux开发的，所以可以使用Linux的命令

  *  adb install -r 应用程序名称   安装应用程序
    ```
      adb install -r F:\Test.apk
    ```
  * adb push local remote  将文件写入一个目录 ，作用比install大很多
    ```
    adb push d:\test.apk /system/app/

    adb push /system/app/ d:\file.txt
    将手机文件写到d盘目录下  
    ```
  * 查看系统盘符
  ```
  adb shell df
  ```
  * 输出所有已安装的应用
  ```
  adb shell pm list packages -f
  ```
  * 模拟按键输入
  ```
  adb shell input keyevent 3
  ```
  * 模拟keyEvent
  ```
  adb shell input keyevent 82 menu
            input keyevent 3 home
            input keyevent 19 up
            input keyevent 20 down
            input keyevent 21 left
            input keyevent 22 right
            input keyevent 66 enter
            input keyevent 4 back
  ```
  * 滑动输入
  ```
  adb shell input touchscreen x1 x2 x3 x4
  adb shell input touchscreen swipe 18 665 18 350
  ```
  * 查看activity的运行状态，同时过滤“帅哥”关键字
    ```
    1. adb shell dumpsys
    2. dumpsys activity activitys|grep "帅哥"

    ```
  * package 管理信息
    ```
    pm list packages -f
    ```
  * 启动一个activity
    ```
    adb shell am start -n 包名/包名+类名
    ```
  * 屏幕录制
    ```
    adb shell screenrecord /sdcard/demo.mp4
    ```
  * 重新启动
    ```
    adb reboot
    ```
4. adb 命令的来源,android很多操作都可以通过adb来执行

  看http://androidxref.com/ `\system\core\toolbox `  和 `frameworks\base\cmds`
5. 模拟器现在可以用网易木木或者系统的，建议使用真机调试

# 第三章 Android 控件架构与自定义控件详解

## 1. Android 控件架构
   抛开平常偷懒使用的butterknife。平常使用最多的的就是`setContentView` 然后`findViewById`。里面具体发生了那些事情，在这里做一个简单的介绍。

 ![Alt text](图像1509633530.png "AndroidUI架构")

 ![Alt text](图像1509634201.png "简陋的视图树")

 ** findViewById是深度遍历的** 在`setContentView`之后 ActivityManagerService 会回调OnResume()方法 这个时候系统才会将DecorView添加到PhoneWindow中让他显示出来，完成界面的绘制

## 2. View的测量和绘制

#### 2.1 View的测量
测量主要在`onMeasure`方法中进行，主要是有一个类来测量：MeasureSpec,这是一个32位的int值，高2位是测量模式，低30位用来测量大小，内部使用的都是位运算，提高效率，但是我从来没有过位运算。
* 测量模式
  * EXACTLY
    这个是精确值模式，当我们将控件设置为`layout_width`或者`layout_height`属性指定为具体数值的时候使用。e：`android：layout_width=100dp` 这个时候系统使用的就是EXACTLY模式
  * AT_MOST
    即最大值模式，当控件的属性`layout_width`为`wrap_content`的时候就是使用这种模式，要求控件的大小不超过父控件大小就可以
  * UNSPEXIFIED
    这个是不测量模式，用于自定义控件
* 使用
  * 一
    ```
    int specMode=MeasureSpec.getMode(measureSpec)
    int specSize=MeasureSpec.getSize(measureSpec)
    
    ```
  * 二
