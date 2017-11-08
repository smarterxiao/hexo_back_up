---
title: Android群英传
date: 2017-11-01 22:17:07
tags:
---

# 简介
这本书是之前一直想整理的书籍 ，只要是Android基础的书籍，主要还是吧自己常用的整理一下方便查找。这本书是2015的版本。

# 第一章 Android 体系与系统架构

## 小结
一个网站：http://androidxref.com/  这个是一个Android的源码查看网站

# 第二章 Android 开发工具新接触

##  环境配置

一个常用的Android镜像网站 http://www.androiddevtools.cn/

##  ADB 的使用
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

##  Android 控件架构

   抛开平常偷懒使用的butterknife。平常使用最多的的就是`setContentView` 然后`findViewById`。里面具体发生了那些事情，在这里做一个简单的介绍。

 ![Alt text](图像1509633530.png "AndroidUI架构")

 ![Alt text](图像1509634201.png "简陋的视图树")

 ** findViewById是深度遍历的** 在`setContentView`之后 ActivityManagerService 会回调OnResume()方法 这个时候系统才会将DecorView添加到PhoneWindow中让他显示出来，完成界面的绘制

##  View的测量和绘制

### View的测量
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

    ```
      //  如果不知道控件的大小，可以使用这个方法就可以知道控件的大小了
      int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
      int h = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
      view.measure(w, h);
      view.layout();
      addView(view);  //就可以将控件添加到副布局中了
    ```

### View布局

就是`onLayout()`的过程

###  View绘制

 绘制主要需要在`onDraw()`方法中进行，有一个画布，所有的绘制操作都和画布有关，后面会详细解释的，最好理解一下ps的图层，图像是一层一层叠加起来的

![Alt text](tuceng.jpg "图层的理解")
###  ViewGroup的测量

> ViewGroup在测量的时候回遍历所有子View的measure方法，然后会调用layout方法进行布局，最后在绘制

###  红包布局实战

> 最近抢红包这个东西比较火，就用它来实现一次简单的ViewGroup实战

![Alt text](图像1509980950.png "自定义ViewGroup效果图")

  * MainActivity

```
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}

```

   RedPacketLayout

```
public class RedPacketLayout extends ViewGroup {
    public RedPacketLayout(Context context) {
        super(context);
    }

    public RedPacketLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RedPacketLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    RedPacketView big=new BigRedPacketView(getContext());
    RedPacketView small=new SmallRedPacketView(getContext());

    //ViewGroup要重写的方法 这个是一个抽象方法
    @Override
    protected void onLayout(boolean b, int i, int i1, int i2, int i3) {
        //测量模式
        int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
        //测量
        big.measure(w, h);
        small.measure(w, h);
         //layout 布局 位置先写死
        big.layout(100,100,100+big.getMeasuredWidth(),big.getMeasuredHeight()+100);
        small.layout(200,200,200+small.getMeasuredWidth(),small.getMeasuredHeight()+200);
        addView(big);
        addView(small);
    }
}


```

   RedPacketView

```
public abstract class RedPacketView extends android.support.v7.widget.AppCompatTextView {
    public RedPacketView(Context context) {
        super(context);
        init();
    }

    public RedPacketView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RedPacketView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

   public abstract void init();
}

```

   BigRedPacketView

```

public class BigRedPacketView extends RedPacketView {
    public BigRedPacketView(Context context) {
        super(context);
    }

    public BigRedPacketView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BigRedPacketView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void init() {
        setText("大红包");
        setTextColor(Color.GREEN);
    }
}

```

  SmallRedPacketView

```

public class SmallRedPacketView extends RedPacketView {
    public SmallRedPacketView(Context context) {
        super(context);
    }

    public SmallRedPacketView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SmallRedPacketView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void init() {
        setText("小红包");
        setTextColor(Color.RED);
    }
}

```

  activity_main

```
<?xml version="1.0" encoding="utf-8"?>
<android.support.constraint.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context="com.smart.qun.MainActivity">

  <com.smart.qun.RedPacketLayout
      android:layout_width="match_parent"
      android:layout_height="match_parent"></com.smart.qun.RedPacketLayout>

</android.support.constraint.ConstraintLayout>
```
## 自定义View

###  一些重要的回调方法

  * `onFinishInfalte` 从XML加载完组件回调
  * `onSizeChanged` 组件大小改变时回调
  * `onMeasure` 回调该方法来进行测量
  * `onLayout` 回掉该方法确定显示位置
  * `onTouchEvent` 监听到触摸事件是回调

### 对textView的自定义

#### 有边框的TextView
```
· @Override
    protected void onDraw(Canvas canvas) {

        //在回调父类方法前，实现自己的逻辑，放在textView文字下方
        //三明治上层的面包


        Paint mPaint = new Paint();
        mPaint.setColor(getResources().getColor(android.R.color.holo_blue_bright));
        mPaint.setStyle(Paint.Style.FILL);

        Paint mPaint2 = new Paint();
        mPaint2.setColor((Color.YELLOW));
        mPaint2.setStyle(Paint.Style.FILL);


        canvas.drawRect(0,0,getMeasuredWidth(),getMeasuredHeight(),mPaint);
        canvas.drawRect(10,10,getMeasuredWidth()+10,getMeasuredHeight()+10,mPaint2);
        canvas.save();
        canvas.translate(10,0);
        //中间的肉
        super.onDraw(canvas);

        canvas.restore();
        //下层的面包
        //在回调父类方法后，实现自己的逻辑，放在textView文字上方
    }
```

![Alt text](图像1510154184.png "自定义TextView效果图,有一个边框")

#### 心得
之前看第一遍的时候对`canvas.save()`和`canvas.restore()`这两个方法有一点不理解，第二遍的时候明白了很多，给大家演示一下就明白了。

先来两张图

    ![Alt text](test1.jpg "头像小王 300*300")
    ![Alt text](test2.jpg "头像小李 300*300")

  ```
  @Override
  protected void onDraw(Canvas canvas) {

      mPaint = new Paint();
      bitmap1 = BitmapFactory.decodeResource(getResources(), R.mipmap.test1);
      bitmap2 = BitmapFactory.decodeResource(getResources(), R.mipmap.test2);

      canvas.drawBitmap(bitmap1, 0, 0, mPaint);
      canvas.scale(5f, 5f);//对画布进行放大
      canvas.drawBitmap(bitmap2, 30, 30, mPaint);
      super.onDraw(canvas);

  }
  ```

![Alt text](图像1510154576.png "先放大 300*300")


  ```
  @Override
  protected void onDraw(Canvas canvas) {


      mPaint = new Paint();
      bitmap1 = BitmapFactory.decodeResource(getResources(), R.mipmap.test1);
      bitmap2 = BitmapFactory.decodeResource(getResources(), R.mipmap.test2);
      canvas.drawBitmap(bitmap1, 0, 0, mPaint);
      canvas.save();//保存
      canvas.scale(5f, 5f);//对画布进行放大
      canvas.restore();//恢复
      canvas.drawBitmap(bitmap2, 30, 30, mPaint);
      super.onDraw(canvas);
    }
    ```

  ![Alt text](图像1510154812.png "执行save和restore")

`canvas.save()`和`canvas.restore()`的意义是在某一个时间节点保存当前画布的状态，在对画布进行操作之后还原画布的状态


#### 亮闪闪的TextView
> 主要使用Shade来渲染





##  自定义ViewGroup

# 第四章
