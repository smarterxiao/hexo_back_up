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

![Alt text](1510156396552.png "头像小王300*300")

![Alt text](1510156433278.png "头像小李300*300")

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

![Alt text](图像1510154576.png "先放大300*300")


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


### 闪动的TextView
> 主要使用Shade来渲染

```
Paint paint;
  int mViewWidth = 0;
  private LinearGradient lin;
  int mTranslate = 0;
  private Matrix matrix;
  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
      super.onSizeChanged(w, h, oldw, oldh);
      Log.i("test","onSizeChanged");
      if (mViewWidth == 0) {
          mViewWidth = getMeasuredWidth();
          if (mViewWidth > 0) {
              paint = getPaint();
              lin = new LinearGradient(0, 0, mViewWidth, 0, new int[]{Color.BLUE, 0xffffffff, Color.BLUE}, null, Shader.TileMode.CLAMP);
              paint.setShader(lin);
              matrix = new Matrix();
          }
      }
  }
  @Override
  protected void onDraw(Canvas canvas) {
      super.onDraw(canvas);
      Log.i("test","onDraw");
      if (matrix != null) {

          mTranslate += mViewWidth / 5;
          if (mTranslate > 2 * mViewWidth) {
              mTranslate = -mViewWidth;
          }
          matrix.setTranslate(mTranslate, 0);
          lin.setLocalMatrix(matrix);
          postInvalidate();
      }


  }
```

###复合控件
这个是通过自定义属性来自定义控件：比如`android:layout_height="match_parent"` 这个是Android系统给我们提供的属性，我们可以自己定义这种属性。
1. 自定义属性资源
在`res/value`目录创建`attrs.xml`
```

<?xml version="1.0" encoding="utf-8"?>
<resources>

    <declare-styleable name="test">
        <attr name="title" format="string" />
        <attr name="titleTextSize" format="dimension" />
        <attr name="titleTextColor" format="color" />
        <attr name="leftTextColor" format="color" />
        //不同的属性使用|隔开
        <attr name="leftBackGround" format="reference|color" />
        <attr name="leftText" format="string" />
        <attr name="rightTextColor" format="color" />
        <attr name="rightBackGround" format="reference|color" />
        <attr name="rightText" format="string" />
    </declare-styleable>

</resources>


```


```
public class SmarterLin extends LinearLayout {
    private String string;
    public SmarterLin(Context context) {
        this(context,null);
        init();
    }
    //第二种构造方法的作用就是用于自定义属性
    public SmarterLin(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
        init();
    }
    public SmarterLin(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if(attrs!=null){
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.test);
            typedArray.getColor(R.styleable.test_leftTextColor,0);
            typedArray.getDrawable(R.styleable.test_leftBackGround);
            string = typedArray.getString(R.styleable.test_leftText);
            typedArray.getDimension(R.styleable.test_titleTextSize,0);
            //避免再次创建错误
            typedArray.recycle();
        }
    }
    private void init() {
      //在这里设置自定属性，并添加控件
        Button bt=new Button(getContext());
        bt.setText(string);
    }
}

```

```
<com.smart.myapplication.SmarterLin
      android:text="Hello World!"
      xmlns:custom="http://schemas.android.com/apk/res-auto"   //定义自己的命名空间  xmlns:custom="http://schemas.android.com/apk/res-auto"   custom是自定义空间的名字
      custom:rightText="test"  //使用自定义空间
      android:layout_width="match_parent"
      android:layout_height="match_parent" />
```
###重写View来实现全新的自定义控件
就是上面闪动的textview

##  自定义ViewGroup
### 一个弹性scrollView
不是那么流畅，只是大概理解他的原理
```
<com.smart.myapplication.SpringScrollLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    tools:context="com.smart.myapplication.MainActivity">

        <TextView
            android:background="#00ffff"
            android:layout_width="match_parent"
            android:layout_height="match_parent" />

        <TextView
            android:background="#00ff00"
            android:layout_width="match_parent"
            android:layout_height="match_parent" />
        <TextView
            android:background="#ff0000"
            android:layout_width="match_parent"
            android:layout_height="match_parent" />
</com.smart.myapplication.SpringScrollLayout>
```

```

public class SpringScrollLayout extends ViewGroup {

    private float mLasty;
    private int mStart;
    private DisplayMetrics dm;
    private float mEnd;

    public SpringScrollLayout(Context context) {
        super(context);
    }

    public SpringScrollLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SpringScrollLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onLayout(boolean change, int l, int t, int r, int b) {
        WindowManager wm = (WindowManager) getContext()
                .getSystemService(Context.WINDOW_SERVICE);
        dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        LayoutParams mlp = getLayoutParams();
        mlp.height = dm.heightPixels * getChildCount();
        setLayoutParams(mlp);
        for (int i4 = 0; i4 < getChildCount(); i4++) {
            View child = getChildAt(i4);
            child.layout(l, i4 * dm.heightPixels, r, (i4 + 1) * dm.heightPixels);
        }
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int count = getChildCount();
        for (int i = 0; i < count; ++i) {
            View child = getChildAt(i);
            measureChild(child, widthMeasureSpec, heightMeasureSpec);
        }
    }
    Scroller scroller = new Scroller(getContext());
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLasty = y;
                mStart = getScrollY();
                break;
            case MotionEvent.ACTION_MOVE:
                if (!scroller.isFinished()) {
                    scroller.abortAnimation();
                }
                float dy = mLasty - y;
                if (getScaleY() < 0) {
                    dy = 0;
                }
                if (getScaleY() > getHeight() - dm.heightPixels) {

                    dy = 0;
                }
                scrollBy(0, (int) dy);
                break;

            case MotionEvent.ACTION_UP:

                mEnd = getScaleY();


                float dScrollY = mEnd - mStart;

                if (dScrollY > 0) {

                    if (dScrollY < dm.heightPixels / 3) {
                        scroller.startScroll(0, getScrollY(), 0, (int)- dScrollY);
                    } else {
                        scroller.startScroll(0, getScrollY(), 0, (int) (dm.heightPixels - dScrollY));
                    }
                } else {

                    if (-dScrollY < dm.heightPixels / 3) {
                        scroller.startScroll(0, getScrollY(), 0, (int) -dScrollY);
                    } else {
                        scroller.startScroll(0, getScrollY(), 0, (int) (-dm.heightPixels - dScrollY));
                    }
                }
                break;
        }
        postInvalidate();
        return true;
    }
    @Override
    public void computeScroll() {
        super.computeScroll();


        if (scroller.computeScrollOffset()) {
            scrollTo(0, scroller.getCurrY());
            postInvalidate();
        }
    }
}

```

## Android 事件拦截机制分析

ViewGroup主要是这三个方法
```
@Override
 public boolean dispatchTouchEvent(MotionEvent ev) {
     return super.dispatchTouchEvent(ev);
 }

 @Override
   public boolean onInterceptTouchEvent(MotionEvent ev) {
       return super.onInterceptTouchEvent(ev);
   }
 @Override
 public boolean onTouchEvent(MotionEvent event) {
     return super.onTouchEvent(event);
 }

```
View主要是这个方法

```
@Override
public boolean onTouchEvent(MotionEvent event) {
    return super.onTouchEvent(event);
}
```

  ![Alt text](图像1510416628.png "触摸事件完整流程")

一般不会操作`dispatchTouchEvent`这个方法，所以这里就不讨论这个方法 ，如果在ViewGroup中截断事件`onInterceptTouchEvent` 那么View是收不到事件的，如果在子View中处理了事件`onTouchEvent`，就是`onTouchEvent`返回值是`true`,ViewGroup是收不到事件的
  ![Alt text](图像1510417038.png "触摸事件完整流程2")


# 第四章 List使用
现在这个很少用了，就不讲了，后面会有RecyclerView和ListView的源码分析

# 第五章 Android  Scroll分析
## 坐标系
这个是滚动的基础，所有的移动都是在这个坐标系内进行滚动
有两种坐标系，一个是以屏幕左上角位原点，一个是以父控件左上角为原点,获取的(X，Y)的值是不一向的
![Alt text](图像1510671624.png   "Android坐标系")

MotionEvent
* getX()
* getY()
* getRawX()
* getRawY()

View
* getLeft()
* getRight()
* getBottom()
* getTop()

## 滑动的七种方法

```
xml:
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"

    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    tools:context="com.smart.myapplication.MainActivity">

    <com.smart.myapplication.TestView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_margin="50dp"
        android:text="dddd" />

</LinearLayout>

TestView: 重写onTouchEvent
@Override
public boolean onTouchEvent(MotionEvent event) {
   switch (event.getAction()) {
      case MotionEvent.ACTION_DOWN:
            Log.i("event x: ",event.getX()+"");
            Log.i("event y: ",event.getY()+"");
            Log.i("view x: ",event.getRawX()+"");
            Log.i("view y: ",event.getRawY()+"");
            Log.i("event getRawX: ", getX()+"");
            Log.i("event getRawY: ", getY()+"");
            Log.i("left: ",getLeft()+"");
            Log.i("right: ",getRight()+"");
            Log.i("bottom: ",getBottom()+"");
            Log.i("top: ",getTop()+"");
            break;
      case MotionEvent.ACTION_UP:
          break;
      case MotionEvent.ACTION_CANCEL:
          break;
      case MotionEvent.ACTION_HOVER_MOVE:
          break;
          }
        return true;
    }
点击结果:
        I/event x:: 54.0
        I/event y:: 23.0
        I/view x:: 175.0
        I/view y:: 175.0
        I/event getRawX:: 229.0
        I/event getRawY:: 478.0
        I/left:: 175
        I/right:: 287
        I/bottom:: 241
        I/top:: 175
```
###  Layout方法

* 第一种写法

```
public class TestView extends android.support.v7.widget.AppCompatTextView {
    private float x;
    private float y;
    private float lastX;
    private float lastY;
    public TestView(Context context) {
        super(context);
    }
    public TestView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public TestView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        x = event.getX();
        y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastX = x;
                Log.i("test","test");
                lastY = y;
                break;
            case MotionEvent.ACTION_UP:
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
            case MotionEvent.ACTION_MOVE:
                int dx = (int) (x - lastX);
                int dy = (int) (y - lastY);
                layout(getLeft() + dx, getTop() + dy, getRight() + dx, getBottom() + dy);
                break;
        }


        return true;
    }
}

```
* 第二种写法
```
public class TestView extends android.support.v7.widget.AppCompatTextView {
    private float x;
    private float y;
    private float lastX;
    private float lastY;
    public TestView(Context context) {
        super(context);
    }
    public TestView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public TestView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        x = event.getRawX();
        y = event.getRawY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastX = x;
                Log.i("test","test");
                lastY = y;
                break;
            case MotionEvent.ACTION_UP:
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
            case MotionEvent.ACTION_MOVE:
                int dx = (int) (x - lastX);
                int dy = (int) (y - lastY);
                layout(getLeft() + dx, getTop() + dy, getRight() + dx, getBottom() + dy);
                lastX=x;//加这个东西，不然会越跑越快
                lastY=y;
                break;
        }
        return true;
    }
}

```

###  offsetLeftAndRight()与offsetTopAndBottom()


* 第一种写法

```
public class TestView extends android.support.v7.widget.AppCompatTextView {
    private float x;
    private float y;
    private float lastX;
    private float lastY;
    public TestView(Context context) {
        super(context);
    }
    public TestView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public TestView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        x = event.getX();
        y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastX = x;
                Log.i("test","test");
                lastY = y;
                break;
            case MotionEvent.ACTION_UP:
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
            case MotionEvent.ACTION_MOVE:
                int dx = (int) (x - lastX);
                int dy = (int) (y - lastY);
                offsetLeftAndRight(dx);
                offsetTopAndBottom(dy); //  这两个方法和这个是一个意思    layout(getLeft() + dx, getTop() + dy, getRight() + dx, getBottom() + dy);
                break;
        }


        return true;
    }
}

```
* 第二种写法
```

public class TestView extends android.support.v7.widget.AppCompatTextView {

    private float x;
    private float y;
    private float lastX;
    private float lastY;

    public TestView(Context context) {
        super(context);
    }

    public TestView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TestView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        x = event.getRawX();
        y = event.getRawY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastX = x;
                Log.i("test","test");
                lastY = y;
                break;
            case MotionEvent.ACTION_UP:
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
            case MotionEvent.ACTION_MOVE:
                int dx = (int) (x - lastX);
                int dy = (int) (y - lastY);

             offsetLeftAndRight(dx);
             offsetTopAndBottom(dy); //  这两个方法和这个是一个意思    layout(getLeft() + dx, getTop() + dy, getRight() + dx, getBottom() + dy);
                lastX=x;//加这个东西，不然会越跑越快
                lastY=y;
                break;
        }

        return true;
    }
}

```

###  LayoutParams
`LayoutParams`保存了View的布局参数,所以可以获取`LayoutParams`之后改变布局参数来改变View的位置。获取`dx`和`dy`两种方式下面就写一种
使用这种方式要注意父布局
```
public class TestView extends android.support.v7.widget.AppCompatTextView {
    private float x;
    private float y;
    private float lastX;
    private float lastY;
    public TestView(Context context) {
        super(context);
    }
    public TestView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public TestView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        x = event.getRawX();
        y = event.getRawY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastX = x;
                Log.i("test","test");
                lastY = y;
                break;
            case MotionEvent.ACTION_UP:
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
            case MotionEvent.ACTION_MOVE:
                int dx = (int) (x - lastX);
                int dy = (int) (y - lastY);
                LinearLayout.LayoutParams layoutParams= (LinearLayout.LayoutParams) getLayoutParams(); //父布局是LinearLayout
                layoutParams.leftMargin=getLeft()+dx;
                layoutParams.topMargin=getTop()+dy;
                setLayoutParams(layoutParams);
                lastX=x;//加这个东西，不然会越跑越快
                lastY=y;
                break;
        }
        return true;
    }
}
```
也可以使用`ViewGroup.MarginLayoutParams layoutParams`，这样就不用区分父布局的类型，`ViewGroup`是所有包含控件的基类，一般改变位置是改变这个控件的`Margin`属性
```
public class TestView extends android.support.v7.widget.AppCompatTextView {
    private float x;
    private float y;
    private float lastX;
    private float lastY;
    public TestView(Context context) {
        super(context);
    }
    public TestView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public TestView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        x = event.getRawX();
        y = event.getRawY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastX = x;
                Log.i("test","test");
                lastY = y;
                break;
            case MotionEvent.ACTION_UP:
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
            case MotionEvent.ACTION_MOVE:
                int dx = (int) (x - lastX);
                int dy = (int) (y - lastY);
                ViewGroup.MarginLayoutParams layoutParams= (ViewGroup.MarginLayoutParams) getLayoutParams();
                layoutParams.leftMargin=getLeft()+dx;
                layoutParams.topMargin=getTop()+dy;
                setLayoutParams(layoutParams);
                lastX=x;//加这个东西，不然会越跑越快
                lastY=y;
                break;
        }
        return true;
    }
}
```

###  scrollTo和scrollBy
使用`scrollBy(dx, dy)`替换移动布局的操作，发现有问题，是布局的内容移动，就是如果是`TextView`,那么移动的是`TextView`的文字，不是`TextView`,而且<font color=RED>内容的移动方向是和手移动的方向相反</font>
```
public class TestView extends android.support.v7.widget.AppCompatTextView {

    private float x;
    private float y;
    private float lastX;
    private float lastY;
    public TestView(Context context) {
        super(context);
    }
    public TestView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public TestView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        x = event.getRawX();
        y = event.getRawY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastX = x;
                Log.i("test", "test");
                lastY = y;
                break;
            case MotionEvent.ACTION_UP:
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
            case MotionEvent.ACTION_MOVE:
                int dx = (int) (x - lastX);
                int dy = (int) (y - lastY);
                scrollBy(dx, dy);  //就是这个东西，移动的是内容
                lastX = x;//加这个东西，不然会越跑越快
                lastY = y;
                break;
        }
        return true;
    }
}
```

这里要补充一下视图移动的知识，使用`scrollTo`和`scrollBy`的时候移动的是盖板。从一个小孔看外面的世界，你把孔向左移动，但是孔里面的画面相对孔是向左移动。
![Alt text](图像1510759310.png  "图解")
最上面的是原始状态，屏幕和圆圈的关系，当使用`scrollTo`和`scrollBy`移动的时候，手指向右移动，这个时候移动的屏幕，导致图二的显示，但是实际上屏幕是不会移动的，所以就可以理解为图三的效果，内容向左移动了，所以`scrollTo`和`scrollBy`使用`dx`和`dy`的时候要取反，这个时候手指移动的方向就是内容移动的方向，然后屏幕是不动的 ，就是`textView`是不动的，动的是内容,就是`textView`是不动的，动的是内容，就是`textView`是不动的，动的是内容 。重要的话说三遍

```
public class TestView extends android.support.v7.widget.AppCompatTextView {

    private float x;
    private float y;
    private float lastX;
    private float lastY;
    public TestView(Context context) {
        super(context);
    }
    public TestView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public TestView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        x = event.getRawX();
        y = event.getRawY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastX = x;
                Log.i("test", "test");
                lastY = y;
                break;
            case MotionEvent.ACTION_UP:
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
            case MotionEvent.ACTION_MOVE:
                int dx = (int) (x - lastX);
                int dy = (int) (y - lastY);
                scrollBy(-dx, -dy);  //取反
                lastX = x;//加这个东西，不然会越跑越快
                lastY = y;
                break;
        }
        return true;
    }
}
```

###  Scroller
> 感觉群英传对这一部分讲的不太清楚，就自己写一下自己的理解，先上代码。实现一个功能，不管怎么拖动中间的控件，就是那个大王叫我来巡山的TextView，最后在当前位置在滚动到(100,100)的距离

```
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    tools:context="com.smart.myapplication.MainActivity">
    <com.smart.myapplication.TestView
        android:layout_width="match_parent"
        android:layout_height="match_parent"
         >
        <TextView
            android:background="#ff0000"
            android:text="大王叫我来巡山"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content" />
    </com.smart.myapplication.TestView>

</LinearLayout>

```

```

public class TestView extends LinearLayout {
    private float x;
    private float y;
    private float lastX;
    private float lastY;
    public TestView(Context context) {
        super(context);
    }
    public TestView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public TestView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    Scroller scroller=new Scroller(getContext());
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        x = event.getRawX();
        y = event.getRawY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_UP:
                scroller.startScroll((int)- event.getX(), (int) -event.getY(),-100,-100,10000);
                invalidate();
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
            case MotionEvent.ACTION_MOVE:
                int dx = (int) (x - lastX);
                int dy = (int) (y - lastY);
                scrollBy(-dx, -dy);  //取反
                lastX = x;//加这个东西，不然会越跑越快
                lastY = y;
                break;
        }
        return true;
    }
    @Override
    public void computeScroll() {
        super.computeScroll();
        if(scroller.computeScrollOffset()){//判断是否执行完毕
            scrollTo(scroller.getCurrX(),scroller.getCurrY());
            invalidate();
        }
    }
}

```

`scroller.startScroll((int)- event.getX(), (int) -event.getY(),-100,-100,10000);` 第一个参数是开始的x、第二个是开始的y、第三个参数是移动的变化量dx、第四个是移动的变化量dy，第五个是时间可以弄长一点
记住这个坐标和我们的坐标系是相反的，因为scroller底层使用的是scrollTo，移动的是里面的内容，所以从屏幕的方向看过去是相反的。`startScroll`开始之后会调用`computeScroll()`。如果想要移动到指定的位置，dx和dy这个要用当前位置和指定位置的差值


### 属性动画 这个后面讲
### ViewDragHelper
> drawerLayout和SlidingPaneLayout背后的男人
drawerLayout:侧边栏拖出
SlidingPaneLayout：可以实现IOS侧滑关闭的功能

* 最简单的版本，实现LinearLayout内部控件的移动

```
public class TestView extends LinearLayout {
    private ViewDragHelper viewDragHelper;
    public TestView(Context context) {
        super(context);
        init();
    }
    public TestView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    public TestView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    private void init() {
        viewDragHelper = ViewDragHelper.create(this, new ViewDragHelper.Callback() {
            //判断View是不是你想要移动的 是就是true，不是就是false  
            @Override
            public boolean tryCaptureView(View child, int pointerId) {
                return true;
            }
            @Override
            public void onViewDragStateChanged(int state) {
                super.onViewDragStateChanged(state);
            }

            //水平滑动
            @Override
            public int clampViewPositionHorizontal(View child, int left, int dx) {
                return left;
            }
            //垂直滑动
            @Override
            public int clampViewPositionVertical(View child, int top, int dy) {
                return top;
            }
        });
    }
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return viewDragHelper.shouldInterceptTouchEvent(ev);

    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        viewDragHelper.processTouchEvent(event);
        return true;
    }
    @Override
    public void computeScroll() {
        super.computeScroll();
        if (viewDragHelper.continueSettling(true)) {//判断是否执行完毕
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }
}

```
* 最简单版本的布局
```
<com.smart.myapplication.TestView
    android:layout_width="match_parent"
    android:layout_height="match_parent">
    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:background="#ff0000"
        android:text="dddd" />

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:background="#ffff00"
        android:text="dddd" />
</com.smart.myapplication.TestView>

```

* 第二个版本，可以回归原位
```
public class TestView extends LinearLayout {
    private ViewDragHelper viewDragHelper;
    public TestView(Context context) {
        super(context);
        init();
    }
    public TestView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    public TestView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    private void init() {
        viewDragHelper = ViewDragHelper.create(this, new ViewDragHelper.Callback() {
            //判断View是不是你想要移动的 是就是true，不是就是false  
            @Override
            public boolean tryCaptureView(View child, int pointerId) {
                return true;
            }
            @Override
            public void onViewDragStateChanged(int state) {
                super.onViewDragStateChanged(state);
            }
            //释放之后的回调，就是手指抬起的回调
           @Override
           public void onViewReleased(View releasedChild, float xvel, float yvel) {
               //如果有第一个孩子，这里偷懒没做判断 如果移动超过500 就到300的位置，如果小于500 就回归原位
               if (getChildAt(0).getLeft() < 500) {
                   viewDragHelper.smoothSlideViewTo(getChildAt(0), 0, 0);
                   ViewCompat.postInvalidateOnAnimation(TestView.this);
               } else {
                   viewDragHelper.smoothSlideViewTo(getChildAt(0), 300, 0);
                   ViewCompat.postInvalidateOnAnimation(TestView.this);

               }
               super.onViewReleased(releasedChild, xvel, yvel);
           }

            //水平滑动
            @Override
            public int clampViewPositionHorizontal(View child, int left, int dx) {
                return left;
            }
            //垂直滑动
            @Override
            public int clampViewPositionVertical(View child, int top, int dy) {
                return top;
            }
        });
    }
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return viewDragHelper.shouldInterceptTouchEvent(ev);

    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        viewDragHelper.processTouchEvent(event);
        return true;
    }
    @Override
    public void computeScroll() {
        super.computeScroll();
        if (viewDragHelper.continueSettling(true)) {//判断是否执行完毕
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }
}
```

* 终极效果


```

public class TestView extends FrameLayout {

    private ViewDragHelper viewDragHelper;
    private View menu;
    private View mainView;
    private int measuredWidth;

    public TestView(Context context) {
        super(context);
        init();
    }

    public TestView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TestView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        viewDragHelper = ViewDragHelper.create(this, new ViewDragHelper.Callback() {
            @Override
            public boolean tryCaptureView(View child, int pointerId) {
                return mainView==child;
            }

            @Override
            public void onViewDragStateChanged(int state) {
                super.onViewDragStateChanged(state);
            }

            //view位置改变调用
            @Override
            public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
                super.onViewPositionChanged(changedView, left, top, dx, dy);
            }

            //view被用户触摸回调
            @Override
            public void onViewCaptured(View capturedChild, int activePointerId) {
                super.onViewCaptured(capturedChild, activePointerId);
            }



            //view拖动状态改变回调
            @Override
            public void onEdgeDragStarted(int edgeFlags, int pointerId) {
                super.onEdgeDragStarted(edgeFlags, pointerId);
            }

            //释放之后的回调，就是手指抬起的回调
            @Override
            public void onViewReleased(View releasedChild, float xvel, float yvel) {

                //如果有第一个孩子，这里偷懒没做判断 如果移动超过500 就到300的位置，如果小于500 就回归原位
                if (mainView.getLeft() < 500) {
                    viewDragHelper.smoothSlideViewTo(mainView, 0, 0);
                    ViewCompat.postInvalidateOnAnimation(TestView.this);
                } else {
                    viewDragHelper.smoothSlideViewTo(mainView, measuredWidth, 0);
                    ViewCompat.postInvalidateOnAnimation(TestView.this);

                }


                super.onViewReleased(releasedChild, xvel, yvel);
            }

            //水平滑动
            @Override
            public int clampViewPositionHorizontal(View child, int left, int dx) {
                return left;
            }

            //垂直滑动的变化量
            @Override
            public int clampViewPositionVertical(View child, int top, int dy) {
                return 0;
            }
        });
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return viewDragHelper.shouldInterceptTouchEvent(ev);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        viewDragHelper.processTouchEvent(event);
        return true;
    }


    @Override
    public void computeScroll() {
        super.computeScroll();
        if (viewDragHelper.continueSettling(true)) {//判断是否执行完毕
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        measuredWidth = menu.getMeasuredWidth();

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        menu = getChildAt(0);
        mainView = getChildAt(1);
    }
}

```

![Alt text](qq.gif  "模拟qq的侧滑效果")

# 第六章 Android图形机制与处理技
## 屏幕的信息
###  屏幕大小
单位是英寸，是手机对角线的长度。我们说的苹果是4.5英寸的屏幕是指的是对角线长度
### 分辨率
是手机像素的个数，eg：720*1280 这个说明宽有720个像素，高有1280个像素
### PPI
屏幕对角线的密度，屏幕对角线像素个数/对角线长度
计算方法
  w：屏幕宽分辨率  h：屏幕高分辨率  s：屏幕对角线长度(单位是英寸)  

![Alt text](图像1511094759.png  "结果就是ppi")

### 系统屏幕密度

| 密度       | ldpi      |         mdpi |hdpi|xdpi|xxhdpi|
| ------------- |:-------------:| -----:|-----:|-----:|-----:|
| 密度值     |        120 | 160 |240|320|480|
| 分辨率     | 240*320     |   320*160|480*800|720*1280|1080*1920|

现在开发如果要求不高一般使用xdpi的就可以了，其他的可以放下

### 单位转换

sp和dp的区别是sp是针对字体的，会随着系统字体的调整而变化，dp是不会的
```
public class DisplayUtils {


    /**
     * 将px值转换为dpi或者dp
     *
     * @param context 建议Application
     * @param pxValue px值
     * @return 结果  px---->dp
     */
    public static int px2dpi(@NonNull Context context, float pxValue) {

        final float scale = context.getResources().getDisplayMetrics().density;

        return (int) (pxValue / scale + 0.5);

    }


    /**
     * 将dp值转换为px
     *
     * @param context  建议Application
     * @param dipValue dp值
     * @return 结果 dp---->px
     */
    public static int dpi2px(@NonNull Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);

    }


    /**
     * 将px值转换为sp
     *
     * @param context 建议Application
     * @param pxValue px值
     * @return 结果  px---->sp
     */
    public static int px2sp(@NonNull Context context, float pxValue) {

        final float scale = context.getResources().getDisplayMetrics().scaledDensity;

        return (int) (pxValue / scale + 0.5);

    }

    /**
     * 将sp值转换为px
     *
     * @param context 建议Application
     * @param spValue sp值
     * @return 结果  sp---->px
     */
    public static int sp2px(@NonNull Context context, float spValue) {
        final float scale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * scale + 0.5f);

    }

    /**
     *  使用系统api将dp转换为px
     * @param context  建议Application
     * @param dp  dp值
     * @return  dp---->px
     */
    public static int  dp2px(@NonNull Context context ,int dp){
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,dp,context.getResources().getDisplayMetrics());
    }

    /**
     * 使用系统api将sp转换为px
     * @param context  建议Application
     * @param sp  dp值
     * @return  sp---->px
     */
    public static int  sp2px(@NonNull Context context ,int sp){
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,sp,context.getResources().getDisplayMetrics());
    }

}

```


## 2D绘图的基础
 画笔和画布的一些用法，还有很多这里就不列举了，可以查看api
```
public class TesView extends android.support.v7.widget.AppCompatTextView {
    public TesView(Context context) {
        super(context);
    }
    public TesView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public TesView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    Paint mPaint=new Paint();


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPaint.setAntiAlias(true);//设置时候抗锯齿
        mPaint.setColor(456);//设置颜色
        mPaint.setARGB(1,1,1,1);//设置argb颜色
        mPaint.setAlpha(456);//设置透明度
        mPaint.setTextSize(456);//设置字体大小
        mPaint.setStyle(Paint.Style.STROKE);//设置是空心还是实心
        mPaint.setStrokeWidth(456);//设置空心边框的长度

        //画点
        canvas.drawPoint(1,1,mPaint);
        //画线
        canvas.drawLine(1,1,40,40,mPaint);
        float[] pts={1,1,2,2,56,67,77,909};
        canvas.drawLines(pts,mPaint);
        //画矩形
        canvas.drawRect(0,0,1,1,mPaint);
        //画圆角矩形
        canvas.drawRoundRect(1,1,1,1,,1,1,mPaint);
        //画圆
        canvas.drawCircle(1,1,10,mPaint);

        //画圆弧： Paint.Style.STROKE+userCenter（true） 空心扇形   Paint.Style.STROKE+userCenter（false） 空心弧形
        // Paint.Style.FILL+userCenter（true） 实心扇形   Paint.Style.FILL+userCenter（false） 实心弧形
        canvas.drawArc(1,1,1,1,1,1,true,mPaint);
        //画椭圆
        canvas.drawOval(1,1,1,1,mPaint);

        //画文字
        canvas.drawText("王八坨子",1,1,mPaint);

        float[] ptss={1,1,2,2,56,67,77,909};
        //在指定位置画文字  已经过时
        canvas.drawPosText("王八坨子",ptss,mPaint);


        //画路径
        Path path=new Path();
        path.moveTo(200,200);//将起点移动到200,200
        path.lineTo(500,500);//连接500,500
        path.lineTo(777,500);//连接777,500
        canvas.drawPath(path,mPaint);
    }
}

```

## XML绘图

### bitmap
可以将图片转换为bitmap
```
<?xml version="1.0" encoding="utf-8"?>
<bitmap xmlns:android="http://schemas.android.com/apk/res/android" android:src="@drawable/ic_launcher_background">
</bitmap>
```
### shape
这个比较简单，这个可以使用渐变，如果有渐变一般是美工给一张图片解决... 一般实现不了UI的效果   
### layer
多个shape的集合 类似图层叠加效果
### selector
多个shape的集合，可以实现不同状态的效果显示

## Android 绘图技巧

```
    canvas.save();  保存画布当前状态
     canvas.restore();还原之前保存的画布状态，比如sava之后调用translate，之后还原到之前的位置
     canvas.translate(100,100);
     canvas.rotate(100);
```

#### 画一个时钟

![Alt text](图像1511103351.png "效果图")

```
public class TesView extends android.support.v7.widget.AppCompatTextView {
    public TesView(Context context) {
        super(context);
    }

    public TesView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TesView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {

        Paint mPaint=new Paint();

        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(5);

    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint mPaint=new Paint();

        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(5);
        canvas.drawCircle(getWidth()/2,getHeight()/2,getWidth()/2,mPaint);

        Paint painDegree=new Paint();

        painDegree.setStrokeWidth(3);
        for (int i = 0; i < 24; i++) {
            if(i==0||i==6||i==12||i==18){
                painDegree.setStrokeWidth(6);
                painDegree.setTextSize(30);
                这个是画的  |  这种线 就是第一个画的是12点的位置，旋转之后再画12点的位置
                canvas.drawLine(getWidth()/2,getHeight()/2-getWidth()/2,getWidth()/2,getHeight()/2-getWidth()/2+60,painDegree);
                String degree=String.valueOf(i);
                canvas.drawText(degree,getWidth()/2-painDegree.measureText(degree)/2,getHeight()/2-getWidth()/2+90,painDegree);
            }else{
                painDegree.setStrokeWidth(3);
                painDegree.setTextSize(15);
                canvas.drawLine(getWidth()/2,getHeight()/2-getWidth()/2,getWidth()/2,getHeight()/2-getWidth()/2+30,painDegree);
                String degree=String.valueOf(i);
                canvas.drawText(degree,getWidth()/2-painDegree.measureText(degree)/2,getHeight()/2-getWidth()/2+60,painDegree);
            }
            //画完一笔旋转，刚好旋转360度不用保存
            canvas.rotate(15,getWidth()/2,getHeight()/2);
        }


        Paint paintHour=new Paint();
        paintHour.setStrokeWidth(20);
        Paint paintMinute=new Paint();
        paintMinute.setStrokeWidth(10);

        canvas.save();
        canvas.translate(getWidth()/2,getHeight()/2);
        canvas.drawLine(0,0,100,100,paintHour);
        canvas.drawLine(0,0,200,200,paintMinute);

        canvas.restore();


    }
}
```

#### Layer图层


![Alt text](图像1511104169.png "关闭图层的效果")

```
public class TesView extends android.support.v7.widget.AppCompatTextView {
    public TesView(Context context) {
        super(context);
    }
    public TesView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public TesView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint mPaint=new Paint();
        canvas.drawColor(Color.WHITE);
        mPaint.setColor(Color.BLUE);
        canvas.drawCircle(150,150,100,mPaint);
//        canvas.saveLayerAlpha(0,0,400,400,127);
        mPaint.setColor(Color.RED);
        canvas.drawCircle(200,200,100,mPaint);
//        canvas.restore();
    }
}

```

![Alt text](图像1511104031.png "开启图层效果")

```
public class TesView extends android.support.v7.widget.AppCompatTextView {
    public TesView(Context context) {
        super(context);
    }
    public TesView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public TesView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint mPaint=new Paint();
        canvas.drawColor(Color.WHITE);
        mPaint.setColor(Color.BLUE);
        canvas.drawCircle(150,150,100,mPaint);
        //将下一个图层的透明度变化为127 就是50%
        canvas.saveLayerAlpha(0,0,400,400,127);
        mPaint.setColor(Color.RED);
        canvas.drawCircle(200,200,100,mPaint);
        canvas.restore();//这个是还原save的操作
    }
}

```


## Android图像处理值色彩特效处理
#### 矩阵
看这个之前先来看一下矩阵，第一个矩阵第一行*第二个矩阵第一列 就是结果的第一个元素

![Alt text](bg2015090104.png "矩阵乘法")

（2,1）* (1,1) =3  

![Alt text](bg2015090105.png "矩阵乘法")

![Alt text](图像1511276003.png.png "矩阵乘法")
如果想要变红色  一般会修改a1或者e1的值  b1,c1,d1 就为0  修改a1就是给原来色值乘上a1，修改e1就是给原来色值加上一个偏移

想看的可以看一下这个视频
http://open.163.com/special/opencourse/daishu.html
或者不想看可以看一下这个阮一峰的博客
http://www.ruanyifeng.com/blog/2015/09/matrix-multiplication.html

#### 概述
图像从三个维度来描述
* 色调
* 饱和度
* 亮度
Android使用`ColorMatrix`帮助我们封装了矩阵，来实现对矩阵的操作
```

    //设置色调
    ColorMatrix colorMatrix=new ColorMatrix();
    colorMatrix.setRotate(0,1);
    colorMatrix.setRotate(1,1);
    colorMatrix.setRotate(2,1);

    //设置饱和度 如果值为0  就是灰色的图像，老照片的效果
    ColorMatrix colorMatrix1=new ColorMatrix();
    colorMatrix1.setSaturation(0);

    //设置亮度 如果都是0 就变成黑色了
    ColorMatrix colorMatrix2=new ColorMatrix();
    colorMatrix2.setScale(0,0,0,0);
    //混合上面三种矩阵
    ColorMatrix colorMatrix3=new ColorMatrix();
    colorMatrix.postConcat(colorMatrix);
    colorMatrix.postConcat(colorMatrix1);
    colorMatrix.postConcat(colorMatrix2);
```
#### 一个例子
这个是原图
![Alt text](pg_11111111111.jpg "两个美女")
看一下效果

改变`ColorMatrix`的`setRotate`，从-1到1

![Alt text](gif_0.gif "改变0")

![Alt text](gif_1.gif "改变1")

![Alt text](gif_2.gif "改变2")

![Alt text](gif_3.gif "3个都改变")

`setSaturation`  范围 0-2
![Alt text](djfkdskfl2.gif "改变饱和度")
`setScale`   范围 0-2
![Alt text](sldkfldkajflks.gif "改变亮度")

代码

进度条
```
<?xml version="1.0" encoding="utf-8"?>
<ScrollView xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <LinearLayout xmlns:tools="http://schemas.android.com/tools"

        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:orientation="vertical"
        tools:context="com.smart.myapplication.MainActivity">

        <LinearLayout
            android:orientation="horizontal"
            android:layout_width="match_parent"
            android:layout_height="106dp">
        <ImageView
            android:id="@+id/iv_test"
            android:layout_width="160dp"
            android:layout_height="106dp"
            android:src="@mipmap/pg_11111111111" />
            <ImageView
                android:id="@+id/iv_test1"
                android:layout_width="160dp"
                android:layout_height="106dp"
                 />
        </LinearLayout>
        <SeekBar
            android:id="@+id/bar_red"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:max="100"
            android:progress="0" />

        <TextView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="色调" />

        <SeekBar
            android:id="@+id/bar_green"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:max="100"
            android:progress="50" />

        <TextView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="饱和度" />

        <SeekBar
            android:id="@+id/bar_blue"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:max="100"
            android:progress="0" />

        <TextView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="TextView"
            tools:text="亮度" />

    </LinearLayout>
</ScrollView>
```

```


public class MainActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener {

    private ImageView iv_test;
    private SeekBar bar_red;
    private SeekBar bar_green;
    private SeekBar bar_blue;

    int MID_VALUE = 50;
    private float mHue;
    private float mStauration;
    private float mLum;
    private ImageView iv_test1;
    private Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        iv_test = findViewById(R.id.iv_test);
        iv_test1 = findViewById(R.id.iv_test1);
        bar_red = findViewById(R.id.bar_red);
        bar_green = findViewById(R.id.bar_green);
        bar_blue = findViewById(R.id.bar_blue);
        bar_red.setOnSeekBarChangeListener(this);
        bar_green.setOnSeekBarChangeListener(this);
        bar_blue.setOnSeekBarChangeListener(this);
        bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.pg_11111111111);

    }
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        switch (seekBar.getId()) {
            case R.id.bar_red:
                mHue = (progress-MID_VALUE*1.0f)/MID_VALUE*180;
                break;
            case R.id.bar_green:
                mStauration = progress * 1.0f / MID_VALUE;
                break;
            case R.id.bar_blue:
                mLum = progress * 1.0f / MID_VALUE;
                break;
        }
        iv_test1.setImageBitmap(handleImageEffect(bitmap,mHue,mStauration,mLum));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        switch (seekBar.getId()) {
        }
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        switch (seekBar.getId()) {
        }
    }
    public  static Bitmap handleImageEffect(Bitmap bm,float hue,float saturation,float lum){
        Bitmap bmp=Bitmap.createBitmap(bm.getWidth(),bm.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas=new Canvas(bmp);
        Paint paint=new Paint();
        //色调
        ColorMatrix hueMarix=new ColorMatrix();
        hueMarix.setRotate(0,  hue);
        hueMarix.setRotate(1,  hue);
        hueMarix.setRotate(2,hue);
        //饱和度
        ColorMatrix saturationMarix=new ColorMatrix();
        saturationMarix.setSaturation(saturation);
        //亮度
        ColorMatrix lumMarix=new ColorMatrix();
        lumMarix.setScale(lum,lum,lum,1);

        ColorMatrix imageMatrix=new ColorMatrix();
//下面这三个要分开调用 每次解开一个 这个是书里面的一个坑 不然会一直显示黑色的
//        imageMatrix.postConcat(hueMarix);
//        imageMatrix.postConcat(saturationMarix);
//        imageMatrix.postConcat(lumMarix);

        //设置画笔颜色过滤器
        paint.setColorFilter(new ColorMatrixColorFilter(imageMatrix));
        canvas.drawBitmap(bm,0,0,paint);
        return bmp;
    }
}

```
下面来看一下`ColorMatrix` 这个东西到底是什么，点击进入源码 `ctrl+b`，这里就分析一下比较正要的代码

```
ColorMatrix {
 private final float[] mArray = new float[20];//这是一个四行五列的矩阵，在这里面没有矩阵这种数据结构，用数组替代
 public void setRotate(int axis, float degrees) {
     reset();
     double radians = degrees * Math.PI / 180d;
     float cosine = (float) Math.cos(radians);
     float sine = (float) Math.sin(radians);
     switch (axis) {
     // Rotation around the red color
     case 0:
        //
        //   double radians = degrees * Math.PI / 180d;
        //   float cosine = (float) Math.cos(radians);
        //  float sine = (float) Math.sin(radians);
        //x 代表原来的值，然后改变色调就是改变红绿蓝的比例，可以看一下上面的矩阵的乘法
        //     x      x        x     x    x
        //     x      cosine  sine   x    x
        //     x    -sine   cosine   x    x
        //     x      x        x     x    x
        //
         mArray[6] = mArray[12] = cosine;
         mArray[7] = sine;
         mArray[11] = -sine;
         break;
     // Rotation around the green color
     case 1:
         mArray[0] = mArray[12] = cosine;
         mArray[2] = -sine;
         mArray[10] = sine;
         break;
     // Rotation around the blue color
     case 2:
         mArray[0] = mArray[6] = cosine;
         mArray[1] = sine;
         mArray[5] = -sine;
         break;
     default:
         throw new RuntimeException();
     }
 }
//改变饱和度也是改变这个色彩矩阵
 public void setSaturation(float sat) {
    reset();
    float[] m = mArray;//矩阵

    final float invSat = 1 - sat;
    final float R = 0.213f * invSat;
    final float G = 0.715f * invSat;
    final float B = 0.072f * invSat;

    m[0] = R + sat; m[1] = G;       m[2] = B;
    m[5] = R;       m[6] = G + sat; m[7] = B;
    m[10] = R;      m[11] = G;      m[12] = B + sat;
}
//改变亮度也是改变这个色彩矩阵
public void setScale(float rScale, float gScale, float bScale,
                       float aScale) {
      final float[] a = mArray;//矩阵

      for (int i = 19; i > 0; --i) {
          a[i] = 0;
      }
      a[0] = rScale;
      a[6] = gScale;
      a[12] = bScale;
      a[18] = aScale;
  }
}
```

> 可以发现改变饱和度，色调，亮度就是改变红绿蓝的数值

#### 一个更加明显的例子，之前书上是黑白的，不是很明显

```
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    tools:context="com.smart.myapplication.MainActivity">
    <ImageView
        android:id="@+id/iv_test"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:src="@mipmap/pg_11111111111" />
    <GridLayout
        android:id="@+id/gl"
        android:columnCount="5"
        android:rowCount="4"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1" />
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal">
        <Button
            android:id="@+id/bt1"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="改变" />
        <Button
            android:id="@+id/bt2"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="重置" />
    </LinearLayout>
</LinearLayout>

```


```
public class MainActivity extends AppCompatActivity {
    private ImageView iv_test;
    private Bitmap bitmap;
    private int mHeight;
    private int mWidth;
    private GridLayout gridLayout;
    float[] mColorMatrix = new float[20];
    private Button bt1;
    private Button bt2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        iv_test = findViewById(R.id.iv_test);
        bt1 = findViewById(R.id.bt1);
        bt2 = findViewById(R.id.bt2);
        iv_test = findViewById(R.id.iv_test);
        bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.pg_11111111111);
        gridLayout = findViewById(R.id.gl);
        gridLayout.post(new Runnable() {
            @Override
            public void run() {
                mWidth = gridLayout.getWidth() / 5;
                mHeight = gridLayout.getHeight() / 4;
                addEts();
                initMatrix();

            }
        });
        bt1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnChange(v);
            }
        });
        bt2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnReset(v);
            }
        });
    }
    private void initMatrix() {
        for (int i = 0; i < 20; i++) {
            if (i % 6 == 0) {
                ets[i].setText(String.valueOf(1));
            } else {
                ets[i].setText(String.valueOf(0));
            }
        }
    }
    EditText[] ets = new EditText[20];
    private void addEts() {
        for (int i = 0; i < 20; i++) {
            EditText et = new EditText(MainActivity.this);
            ets[i] = et;
            gridLayout.addView(et, mWidth, mHeight);
        }
    }
    private void getMatrix() {
        for (int i = 0; i < 20; i++) {
            mColorMatrix[i] = Float.valueOf(ets[i].getText().toString());
        }
    }
    private void setBitmapMatrix() {
        Bitmap bmp = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        ColorMatrix colorMatrix = new ColorMatrix();

        colorMatrix.set(mColorMatrix);
        Canvas canvas = new Canvas(bmp);
        Paint paint = new Paint();
        //将变换的效果设置到画笔
        paint.setColorFilter(new ColorMatrixColorFilter(mColorMatrix));
        //画一个bitMap
        canvas.drawBitmap(bitmap, 0, 0, paint);
        iv_test.setImageBitmap(bmp);
    }
    public void btnChange(View view) {
        getMatrix();
        setBitmapMatrix();
    }
    public void btnReset(View view) {
        initMatrix();
        getMatrix();
        setBitmapMatrix();
    }
}

```

这个是原图
![Alt text](pg_11111111111.jpg "两个美女")

* 灰度效果

|      ----   |      ----        |   ----  | ---- | ---- |
| ------------- |:-------------:| -----:|-----:|-----:|
| 0.33f    | 0.59f | 0.11f |0|0|
| 0.33f     | 0.59f      |  0.11f |0|0|
| 0.33f | 0.59f   | 0.11f|0|0|
| 0 |0 | 0|1|0|

![Alt text](huiduxiaoguo.png "灰度效果")

* 反转效果

|----| ----        |   ----  | ---- | ---- |
| ------------- |:-------------:| -----:|-----:|-----:|
| -1   | 0 | 0 |1|1|
| 0    | -1     |  0 |1|1|
|0 | 0  | -1|1|1|
| 0 |0 | 0|1|0|

![Alt text](device-2017-12-04-231328.png "反转效果")


* 怀旧效果

|----| ----        |   ----  | ---- | ---- |
| ------------- |:-------------:| -----:|-----:|-----:|
| 0.393f   | 0.769f | 0.189f |0|0|
| 0.349f    | 0.686f     |  0.168f |0|0|
|0.272f| 0.534f | 0.131f|0|0|
| 0 |0 | 0|1|0|

![Alt text](device-2017-12-04-231739.png "怀旧效果")



* 去色效果

|----| ----        |   ----  | ---- | ---- |
| ------------- |:-------------:| -----:|-----:|-----:|
| 1.5f   | 1.5f |1.5f |0|-1|
| 1.5f    | 1.5f     | 1.5f |0|-1|
|1.5f| 1.5f |1.5f|0|-1|
| 0 |0 | 0|1|0|

![Alt text](device-2017-12-04-232141.png "去色效果")



* 高饱和度

|----| ----        |   ----  | ---- | ---- |
| ------------- |:-------------:| -----:|-----:|-----:|
| 1.438f   | -0.122f |-0.016f |0|0.03f|
| -0.062f    | 1.378f     | -0.016ff |0|0.05f|
|-0.062f|  -0.122f  |1.438f  |1|0|
| 0 |0 | 0|1|0|

![Alt text](device-2017-12-04-232508.png "高饱和度")

#### 像素点分析
我们可以更加详细的处理每一个像素点，通过API`BitMap.getPiexs`获取每一个像素点，之后通过计算每一个颜色值，来达到我们想要的效果。

一个简单的流程，不能直接修改原来图片的`Piexs` 只能复制出来创建一个新的图片
```
private ImageView iv_test;
private Bitmap bitmap;
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    iv_test = findViewById(R.id.iv_test);

    bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.pg_11111111111);
    int width = bitmap.getWidth();
    int height = bitmap.getHeight();
    Bitmap bit = Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_8888);
    int[] old = new int[width * height];//旧的颜色集合
    int[] newColors = new int[width * height];//旧的新的集合
    /**
     * @param pixels   获取的矩阵
     * @param offset   偏移
     * @param stride.  宽度，就是每一行有多少个像素
     * @param x        起始位置
     * @param y        结束位置
     * @param width    宽度
     * @param height   高度
     */
    bitmap.getPixels(old, 0, width, 0, 0, width, height);
    int color = old[10]; //获取第十一个像素点的颜色  一个具体的像素点
    //解析这个颜色值，分离出 ARGB
    int red = Color.red(color);
    int green = Color.green(color);
    int blue = Color.blue(color);
    int alpha = Color.alpha(color);

    //对颜色值进行处理
    red=2*red;
    green=2*green;
    blue=2*blue;
    alpha=2*alpha;
    //制作颜色
    newColors[10]=Color.argb(alpha,red,green,blue);
    //设置颜色矩阵
    bit.setPixels(newColors, 0, width, 0, 0, width, height);
}

```

#### 处理像素点的例子
代码大部分都是相同的，可以重点关注一下`对颜色值进行处理,这里就是ov大厂处理图片的地方`注释之间的代码

* 反色效果
```

public class MainActivity extends AppCompatActivity {

    private ImageView iv_test;
    private Bitmap bitmap;
    private ImageView iv_test1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //两个imageview
        iv_test = findViewById(R.id.iv_test);
        iv_test1 = findViewById(R.id.iv_test1);

        bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.pg_11111111111);

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap bit = Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_8888);
        int[] old = new int[width * height];//旧的颜色集合
        int[] newColors = new int[width * height];//旧的新的集合
        /**
         * @param pixels   获取的矩阵
         * @param offset   偏移
         * @param stride.  宽度，就是每一行有多少个像素
         * @param x        起始位置
         * @param y        结束位置
         * @param width    宽度
         * @param height   高度
         */
        bitmap.getPixels(old, 0, width, 0, 0, width, height);



        for (int i = 0; i < width * height; i++) {
            int color = old[i]; //获取第i个像素点的颜色
            //解析这个颜色值，分离出 ARGB
            int red = Color.red(color);
            int green = Color.green(color);
            int blue = Color.blue(color);
            int alpha = Color.alpha(color);

            int red1;
            int green1;
            int blue1;
            int alpha1;

            //对颜色值进行处理,这里就是ov大厂处理图片的地方
            red1=255-red;//处理红色
            green1=255-green;//处理绿色
            blue1=255-blue;//处理蓝色
            alpha1=alpha;//处理透明度


            //边界判断
            if(red1>255){
                red1=255;
            }else if(red1<0){
                red1=0;
            }

            if(green1>255){
                green1=255;
            }else if(green1<0){
                green1=0;
            }


            if(blue1>255){
                blue1=255;
            }else if(blue1<0){
                blue1=0;
            }
            if(alpha1>255){
                alpha1=255;
            }else if(alpha1<0){
                alpha1=0;
            }

            //制作颜色
            newColors[i]=Color.argb(alpha1,red1,green1,blue1);
        }
        //设置颜色矩阵
        bit.setPixels(newColors, 0, width, 0, 0, width, height);
        iv_test1.setImageBitmap(bit);
    }
}

```
![Alt text](device-2017-12-05-232020.png "反色效果")

* 老照片

```


public class MainActivity extends AppCompatActivity {

    private ImageView iv_test;
    private Bitmap bitmap;
    private ImageView iv_test1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //两个imageview
        iv_test = findViewById(R.id.iv_test);
        iv_test1 = findViewById(R.id.iv_test1);

        bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.pg_11111111111);

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap bit = Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_8888);
        int[] old = new int[width * height];//旧的颜色集合
        int[] newColors = new int[width * height];//旧的新的集合
        /**
         * @param pixels   获取的矩阵
         * @param offset   偏移
         * @param stride.  宽度，就是每一行有多少个像素
         * @param x        起始位置
         * @param y        结束位置
         * @param width    宽度
         * @param height   高度
         */
        bitmap.getPixels(old, 0, width, 0, 0, width, height);



        for (int i = 0; i < width * height; i++) {
            int color = old[i]; //获取第i个像素点的颜色
            //解析这个颜色值，分离出 ARGB
            int red = Color.red(color);
            int green = Color.green(color);
            int blue = Color.blue(color);
            int alpha = Color.alpha(color);

            int red1;
            int green1;
            int blue1;
            int alpha1;

            //对颜色值进行处理,这里就是ov大厂处理图片的地方
            red1= (int) (0.393*red+0.769*green+0.189*blue);//处理红色
            green1=(int) (0.349*red+0.686*green+0.186*blue);//处理绿色
            blue1=(int) (0.272*red+0.534*green+0.131*blue);//处理蓝色
            alpha1=alpha;//处理透明度

            //边界判断
            if(red1>255){
                red1=255;
            }else if(red1<0){
                red1=0;
            }

            if(green1>255){
                green1=255;
            }else if(green1<0){
                green1=0;
            }


            if(blue1>255){
                blue1=255;
            }else if(blue1<0){
                blue1=0;
            }
            if(alpha1>255){
                alpha1=255;
            }else if(alpha1<0){
                alpha1=0;
            }

            //制作颜色
            newColors[i]=Color.argb(alpha1,red1,green1,blue1);
        }
        //设置颜色矩阵
        bit.setPixels(newColors, 0, width, 0, 0, width, height);
        iv_test1.setImageBitmap(bit);
    }
}

```

![Alt text](device-2017-12-05-232351.png "老照片")

* 浮雕效果：这个感觉有点不对
> 算法 有ABC三个点，对B进行浮雕算法
B.red=C.red-B.red+127;
B.green=C.green-B.green+127;
B.blue=C.blue-B.blue+127;

```


public class MainActivity extends AppCompatActivity {

    private ImageView iv_test;
    private Bitmap bitmap;
    private ImageView iv_test1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//两个imageview
        iv_test = findViewById(R.id.iv_test);
        iv_test1 = findViewById(R.id.iv_test1);

        bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.pg_11111111111);

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap bit = Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_8888);
        int[] old = new int[width * height];//旧的颜色集合
        int[] newColors = new int[width * height];//旧的新的集合
        /**
         * @param pixels   获取的矩阵
         * @param offset   偏移
         * @param stride.  宽度，就是每一行有多少个像素
         * @param x        起始位置
         * @param y        结束位置
         * @param width    宽度
         * @param height   高度
         */
        bitmap.getPixels(old, 0, width, 0, 0, width, height);



        for (int i = 0; i < width * height; i++) {
            int color = old[i]; //获取第i个像素点的颜色

            int color2 = old[i+1>255?255:i+1];
            //解析这个颜色值，分离出 ARGB
            int red = Color.red(color);
            int green = Color.green(color);
            int blue = Color.blue(color);
            int alpha = Color.alpha(color);


            int red2 = Color.red(color2);
            int green2 = Color.green(color2);
            int blue2 = Color.blue(color2);
            int alpha2 = Color.alpha(color2);

            int red1;
            int green1;
            int blue1;
            int alpha1;

            //对颜色值进行处理,这里就是ov大厂处理图片的地方
            red1= red2-red+127;//处理红色
            green1= green2-green+127;//处理绿色
            blue1= blue2-blue+127;//处理蓝色
            alpha1=alpha;//处理透明度

            //边界判断
            if(red1>255){
                red1=255;
            }else if(red1<0){
                red1=0;
            }

            if(green1>255){
                green1=255;
            }else if(green1<0){
                green1=0;
            }


            if(blue1>255){
                blue1=255;
            }else if(blue1<0){
                blue1=0;
            }
            if(alpha1>255){
                alpha1=255;
            }else if(alpha1<0){
                alpha1=0;
            }

            //制作颜色
            newColors[i]=Color.argb(alpha1,red1,green1,blue1);
        }
        //设置颜色矩阵
        bit.setPixels(newColors, 0, width, 0, 0, width, height);
        iv_test1.setImageBitmap(bit);
    }
}
```

![Alt text](device-2017-12-05-233233.png "浮雕效果")

## Android图像处理之图形特效处理
> 上面看完了`ColorMatrix`对颜色的处理，现在看一下`Matrix`对图形变换的处理，本质是一致的，都是通过矩阵计算
只不过从`argb`变成了`xy`

![Alt text](图像1512488459.png "简单的一个矩阵算法")

正常情况会让 g=h=0 i=1 这样1=g*X+h*Y+i 始终成立

#### 原始变换矩阵

|----| ----        |   ----  |
| ------------- |:-------------:| -----:|
| 1   | 0 |0 |
| 0  |1| 0 |
|0|  0 |1  |


####平移变换

|----| ----        |   ----  |
| ------------- |:-------------:| -----:|
| 1   | 0 |dx |
| 0  |1| dy|
|0|  0 |1  |

![Alt text](图像1515509868.png "平移")

![Alt text](图像1515510116.png "平移 ")

#### 旋转变换

a 顺时针旋转的角度

|----| ----        |   ----  |
| ------------- |:-------------:| -----:|
| cos(a)   |  -sin(a)  |0 |
|  sin(a)   | cos(a) | 0|
|0|  0 |1  |
![Alt text](图像1515510352.png "旋转 ")

#### 缩放

|----| ----        |   ----  |
| ------------- |:-------------:| -----:|
| k1 | 0  |0 |
| 0  | k2| 0|
|0|  0 |1  |
就是乘以不同的系数

#### 错切变换

|----| ----        |   ----  |
| ------------- |:-------------:| -----:|
| 1 | k1  |0 |
| k2  | 1| 0|
|0|  0 |1  |
![Alt text](错切.png "旋转 ")


#### 小结
把前面4种变化

|----| ----        |   ----  |
| ------------- |:-------------:| -----:|
| A| B  |C |
| D  | E| F|
|0|  0 |1  |

* A和E控制：缩放变换
* B和D控制：错切变换
* C和F控制：平移变换
* A、B、D、E控制：旋转变换

#### 效果展示和代码

##### 平移变换
效果
![Alt text](device-2018-01-11-215812.png "平移，看下面那张图片")

![Alt text](device-2018-01-11-220301.png "缩放，看下面那张图片")

![Alt text](device-2018-01-11-220845.png "旋转45，看下面那张图片")

![Alt text](device-2018-01-11-221212.png "错切x，看下面那张图片")

![Alt text](device-2018-01-11-221335.png "错切y，看下面那张图片")

布局  
```
<com.smart.myapplication.TesView
android:layout_width="1000dp"
android:layout_height="1000dp"
/>
```
代码

```
public class TesView extends android.support.v7.widget.AppCompatTextView {
    public TesView(Context context) {
        super(context);
    }
    public TesView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public TesView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.pg_11111111111);
        canvas.drawBitmap(bitmap,0,0,new Paint());
        //绘制的时候进行平移100像素
        float[] mImageMatrix=new float[]{1,0,100,0,1,100,0,0,1};
        //绘制的时候进行放大1.2倍
        float[] mImageMatrix=new float[]{1.2f,0,0,0,1.2f,0,0,0,1};
        //绘制的时候进行旋转45度
        float[] mImageMatrix=new float[]{0.5f,-0.5f,0,0.5f,0.5f,0,0,0,1};
        //绘制的时候进行x轴错切
        float[] mImageMatrix=new float[]{1,0.5f,0,0,1,0,0,0,1};
        //绘制的时候进行y轴错切
        float[] mImageMatrix=new float[]{1,0,0,0.5f,1,0,0,0,1};
        Matrix matrix=new Matrix();
        matrix.setValues(mImageMatrix);
        canvas.drawBitmap(bitmap,matrix,null);
    }
}

```

#### api提供的方法一样可以
```
@Override
protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.pg_11111111111);
        canvas.drawBitmap(bitmap,0,0,new Paint());
        Matrix matrix=new Matrix();
        matrix.setRotate();//旋转
        matrix.setTranslate();//平移
        matrix.setScale();//缩放
        matrix.setSkew();//错切
        matrix.postxxx();//后乘运算
        matrix.prexxx();//前乘运算
        matrix.setValues(mImageMatrix);
        canvas.drawBitmap(bitmap,matrix,null);
```


#### 像素块分析
就是把一个图片分为一个一个不同的像素组，对这个像素组进行计算,类似一个拼图，把一张图片给拆开，不过没有凹凸


```
public class TesView extends android.support.v7.widget.AppCompatTextView {

    private Bitmap bitmap;
    private int height;
    private int width;
    private int HEIGHT;
    private int WIDTH;
    private float[] orig;
    private float[] verts;

    public TesView(Context context) {
        super(context);
        init();
    }

    private void init() {


        bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.pg_11111111111);
        width = bitmap.getWidth();
        height = bitmap.getHeight();
        orig = new float[width*height/2];
        verts = new float[width*height/2];
        int index = 0;
        HEIGHT = 100;
        WIDTH = 100;
        for (int y = 0; y <= HEIGHT; y++) {
            float fy = height * y / HEIGHT;
            for (int i = 0; i <= WIDTH; i++) {
                float fx = width * i / WIDTH;
                orig[index * 2 ] = verts[index * 2] = fx;
                orig[index * 2 + 1] = verts[index * 2 + 1] = fy ;
                index++;
            }
        }
    }

    public TesView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TesView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    private void flagWave() {
        for (int j = 0; j <= HEIGHT; j++) {
            for (int i1 = 0; i1 <= WIDTH; i1++) {
                verts[(j * (WIDTH + 1) + i1) * 2 + 0] += 0;
                float offsetY = (float) Math.sin((float) i1 / WIDTH * 2 * Math.PI + Math.PI * k);
                //这个5是系数 ，就是滚动的距离
                verts[(j * (WIDTH + 1) + i1) * 2 + 1] = orig[(j * WIDTH + i1) * 2 + 1] + offsetY*5 ;
            }
        }

    }

    float k = 0f;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        flagWave();
        k += 0.1;// 这样可以扭动起来
        canvas.drawBitmapMesh(bitmap, WIDTH, HEIGHT, verts, 0, null, 0, null);
        invalidate();
    }
}

```

![Alt text](dongdong.gif "效果")

## Android 图像处理之画笔特效处理

##### 圆角图片

![Alt text](2041548-d964105abf4be5d9.jpg "Google的官方图片")

```
public class guaguakaView extends android.support.v7.widget.AppCompatTextView {
    public guaguakaView(@NonNull Context context) {
        super(context);
    }
    public guaguakaView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }
    public guaguakaView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.pg_11111111111);

        Bitmap mOut=Bitmap.createBitmap(bitmap.getWidth(),bitmap.getHeight(),Bitmap.Config.ARGB_8888);
        Canvas mCanvas=new Canvas(mOut);
        Paint paint=new Paint();
        paint.setAntiAlias(true);
        mCanvas.drawRoundRect(0,0,bitmap.getWidth(),bitmap.getHeight(),80,80,paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        mCanvas.drawBitmap(bitmap,0,0,paint);
        canvas.drawBitmap(mOut,0,0,new Paint());
    }
}

```

![Alt text](device-2018-01-14-212001.png "圆角图片")


##### 刮刮卡

![Alt text](guaguaka.png "刮刮卡效果图")

```
public class TesView extends android.support.v7.widget.AppCompatTextView {

    private Bitmap bitmap;
    private int height;
    private int width;
    private int HEIGHT;
    private int WIDTH;
    private float[] orig;
    private float[] verts;

    public TesView(Context context) {
        super(context);
        init();
    }
    private void init() {
        bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.pg_11111111111);
        width = bitmap.getWidth();
        height = bitmap.getHeight();
        orig = new float[width*height/2];
        verts = new float[width*height/2];
        int index = 0;
        HEIGHT = 100;
        WIDTH = 100;
        for (int y = 0; y <= HEIGHT; y++) {
            float fy = height * y / HEIGHT;
            for (int i = 0; i <= WIDTH; i++) {
                float fx = width * i / WIDTH;
                orig[index * 2 ] = verts[index * 2] = fx;
                orig[index * 2 + 1] = verts[index * 2 + 1] = fy ;
                index++;
            }
        }
    }
    public TesView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    public TesView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    private void flagWave() {
        for (int j = 0; j <= HEIGHT; j++) {
            for (int i1 = 0; i1 <= WIDTH; i1++) {
                verts[(j * (WIDTH + 1) + i1) * 2 + 0] += 0;
                float offsetY = (float) Math.sin((float) i1 / WIDTH * 2 * Math.PI + Math.PI * k);
                verts[(j * (WIDTH + 1) + i1) * 2 + 1] = orig[(j * WIDTH + i1) * 2 + 1] + offsetY*5 ;
            }
        }
    }
    float k = 0f;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        flagWave();
        k += 0.1;
        canvas.drawBitmapMesh(bitmap, WIDTH, HEIGHT, verts, 0, null, 0, null);
        invalidate();
    }
}

```

#### Shader  着色器

分类
* BitmapShader  //图片Shader
* LinearGradient  //线性Shader
* RadialGradient    //光束SHader
* SweepGradient     //梯度  Shader
* ComposeShader    //混合  Shader

模式

* CLAMP  拉伸，最后那个像素不断重复
* REPEAT 重复
* MIRROR 镜像


![Alt text](device-2018-01-14-220130.png "圆形头像效果图")

```
public class RoundView extends View {
    public RoundView(Context context) {
        super(context);
    }
    public RoundView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }
    public RoundView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
      //头像要比较大 不然显示有问题，这个是一个demo，下面会有原因
        Bitmap mBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.pg_11111111111);
        BitmapShader bitmapShader = new BitmapShader(mBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        Paint mPaint = new Paint();
        mPaint.setShader(bitmapShader);
        canvas.drawCircle(500, 250, 200, mPaint);
    }
}

```


不同的显示模式的效果

![Alt text](1510156433278.png "原图")
```
@Override
 protected void onDraw(Canvas canvas) {
     super.onDraw(canvas);


     Bitmap mBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.test1);

     BitmapShader bitmapShader = new BitmapShader(mBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
     Paint mPaint = new Paint();
     mPaint.setShader(bitmapShader);
     canvas.drawCircle(500, 500, 500, mPaint);
 }
```

![Alt text](device-2018-01-14-220943.png "效果图")

```
      Bitmap mBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.test1);
      LinearGradient bitmapShader = new LinearGradient(0, 0, 400,400, Color.BLUE,Color.YELLOW,Shader.TileMode.REPEAT);
      Paint mPaint = new Paint();mPaint.setShader(bitmapShader);
      canvas.drawCircle(500, 500, 500, mPaint);
```

![Alt text](device-2018-01-14-222421.png "效果图")




```
      Bitmap mBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.test1);
      RadialGradient radialGradient=new RadialGradient(10,10,100,Color.BLUE,Color.BLACK,Shader.TileMode.REPEAT);
      Paint mPaint = new Paint();
      mPaint.setShader(radialGradient);
      canvas.drawCircle(500, 500, 500, mPaint);
```

![Alt text](device-2018-01-14-223034.png "效果图")

```
        Bitmap mBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.test1);
        SweepGradient radialGradient=new SweepGradient(100,100,Color.BLUE,Color.BLACK);
        Paint mPaint = new Paint();
        mPaint.setShader(radialGradient);
        canvas.drawCircle(500, 500, 500, mPaint);
```

![Alt text](device-2018-01-14-223502.png "效果图")




```
public class ReflectView extends View {

    private Paint mPaint;
    private PorterDuffXfermode mCfermode;
    private PorterDuffXfermode porterDuffXfermode;
    private Bitmap bitmap;
    private Bitmap otherBitmap;
    private Paint paint;

    public ReflectView(Context context) {
        super(context);
        initRes(context);
    }

    public ReflectView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initRes(context);
    }


    public ReflectView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initRes(context);
    }

    private void initRes(Context context) {
        bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.pg_11111111111);
        Matrix matrix=new Matrix();
        //缩放 如果是负值，就会倒立
        matrix.setScale(1f,-1f);
        otherBitmap = bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        paint = new Paint();
        paint.setShader(new LinearGradient(0, bitmap.getHeight(),0, bitmap.getWidth()+ bitmap.getHeight()/4,0xDD000000,0x10000000, Shader.TileMode.CLAMP));
        porterDuffXfermode = new PorterDuffXfermode(PorterDuff.Mode.DST_IN);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.BLACK);
        canvas.drawBitmap(bitmap,0,0,null);
        canvas.drawBitmap(otherBitmap,0,bitmap.getHeight(),null);
        paint.setXfermode(porterDuffXfermode);
        //设置渐变
        canvas.drawRect(0,bitmap.getHeight(),bitmap.getWidth(),bitmap.getHeight()*2,paint);
        paint.setXfermode(null);

    }
}

```

![Alt text](device-2018-01-14-231137.png "效果图")

##### PathEffect 不同的笔绘制的特效


```
public class EffectView extends View {

    public EffectView(Context context) {
        super(context);
    }
    public EffectView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }
    public EffectView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Path path=new Path();
        path.moveTo(0,0);
        for (int i = 0; i < 30; i++) {
            path.lineTo(i*35,(float)(Math.random()*100));
        }
        canvas.drawPath(path,new Paint());
    }
}

```

![Alt text](device-2018-01-14-232423.png "效果图")

##### PathEffect 不同的笔绘制的特效 系统api

* CornerPathEffect 拐角处变得圆滑
* DiscretePathEffect 线段上面产生咋点
* DashPathEffect 绘制虚线
* PathDashPathEffect 画方形或者圆形的虚线
* ComposePathEffect 组合效果

```
public class EffectView extends View {

    public EffectView(Context context) {
        super(context);
    }


    public EffectView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public EffectView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
//        Path path=new Path();
//        canvas.drawPath(path,new Paint());
        PathEffect[] pathEffects=new PathEffect[6];
        pathEffects[0]=null;
        pathEffects[1]=new CornerPathEffect(30);
        pathEffects[2]=new DiscretePathEffect(3.0f,3.0f);
        pathEffects[3]=new DashPathEffect(new float[]{20,10,5,10},0);
        Path path=new Path();
        path.moveTo(0,0);
        for (int i = 0; i < 30; i++) {
            path.lineTo(i*35,(float)(Math.random()*100));
        }
        path.addRect(0,0,8,8,Path.Direction.CCW);
        pathEffects[4]=new PathDashPathEffect(path,12,0,PathDashPathEffect.Style.ROTATE);
        pathEffects[5]=new ComposePathEffect(pathEffects[3],pathEffects[1]);
        Paint paint=new Paint();
        for (PathEffect pathEffect : pathEffects) {
            paint.setPathEffect(pathEffect);
            canvas.drawPath(path,paint);
            canvas.translate(0,200);
        }

    }
}

```

![Alt text](device-2018-01-14-233455.png "效果图")


## View的兄弟 SurfaceView
Android是可以在子线程更新UI的，SurfaceView就是要在子线程更新UI。他有双缓冲机制，这个后面讲，用的很少

# 第七章 Android 动画机制与使用技巧

## Android View 动画框架
> Android框架定义了透明度，旋转，缩放，位移等常见的几种动画，原理是每次绘制视图的时候哦获取View所在的ViewGroup中的drawChild函数，获取该View的Animation的Transformation值，然后调用canvas。concat（transformToApply。getMatrix()）,通过矩阵完成动画帧。如果动画没有完成，就调用invalidate（），直到完成绘制。
* 属性动画
* 视图动画

## 视图动画

### 透明度动画

```
        View view = findViewById(R.id.view);
        AlphaAnimation aa=new AlphaAnimation(0,1);
        aa.setDuration(1000);
        view.startAnimation(aa);

```

### 旋转动画

```
      View view = findViewById(R.id.view);
      RotateAnimation aa=new RotateAnimation(0,1);
      aa.setDuration(1000);
      view.startAnimation(aa);
```

### 位移动画

```
     View view = findViewById(R.id.view);
     TranslateAnimation aa=new TranslateAnimation(0,200,0,300);
     aa.setDuration(1000);
     view.startAnimation(aa);
```

### 缩放动画

```
       View view = findViewById(R.id.view);
       ScaleAnimation aa=new ScaleAnimation(0,2,0,2);
       aa.setDuration(1000);
       view.startAnimation(aa);

```

### 动画集合

```
    View view = findViewById(R.id.view);
    ScaleAnimation aa = new ScaleAnimation(0, 2, 0, 2);
    aa.setDuration(1000);
    TranslateAnimation ab = new TranslateAnimation(0, 2, 0, 2);
    ab.setDuration(1000);
    AnimationSet as = new AnimationSet(true);
    as.setDuration(1000);
    as.addAnimation(aa);
    as.addAnimation(ab);
    //回调
    as.setAnimationListener(new Animation.AnimationListener() {
         @Override
         public void onAnimationStart(Animation animation) {

         }

         @Override
         public void onAnimationEnd(Animation animation) {

         }

         @Override
         public void onAnimationRepeat(Animation animation) {

         }
     });
    view.startAnimation(as);

```


## 属性动画的分析
### ObjecAnimator  这个是属性动画的基础
本质是通过反射获取属性
```
       View view = findViewById(R.id.view);
       ObjectAnimator animator=ObjectAnimator.ofFloat(view,"translationX",300);
       animator.setDuration(1300);
       animator.start();
 ```
![Alt text](shuxing.gif "效果图")

属性动画常用的属性

* translationX和translationY   控制View对象和他容器左上角的距离
* scaleX和scaleY  围绕他的支点进行缩放，缩放的倍数
* pivotX和pivotY 这两个属性控制着View对象的支点位置，围绕这个支点进行旋转和缩放变换处理，默认情况是这个对象的中心点   
* x和y 这两个对象是描述了VIew对象在他容器的最终位置，是translationX和translationY的对象中点
* alpha 表示透明度，默认值是1 不透明 0 完全透明

如果一个属性没有get和set方法，那可以自己添加一个get和set方法来实现自定义的属性效果


```

public class TeView {

    private View mTarget;
    public View getmTarget() {
        return mTarget;
    }
    public int getWidth() {
        return mTarget.getLayoutParams().width;
    }

    public void setWidth(int width) {
        mTarget.getLayoutParams().width = width;
        mTarget.requestLayout();
    }


    public TeView(View mTarget) {
        this.mTarget = mTarget;
    }

    public void setmTarget(View mTarget) {
        this.mTarget = mTarget;
    }
}





```

使用
```

View view = findViewById(R.id.view);
TeView teView=new TeView(view);
ObjectAnimator animator=ObjectAnimator.ofInt(teView,"width",500);
animator.setDuration(6300);
animator.start();

```

![Alt text](dongdonghua.gif "效果图")

### PropertyValuesHolder :属性动画集合

属性动画集合的使用,类似视图动画的AnimationSet

```
      PropertyValuesHolder pvh1 = PropertyValuesHolder.ofFloat("translationX", 300f);
      PropertyValuesHolder pvh2 = PropertyValuesHolder.ofFloat("scaleX", 1f, 0, 1f);
      PropertyValuesHolder pvh3 = PropertyValuesHolder.ofFloat("scaleY", 3000);
      ObjectAnimator.ofPropertyValuesHolder(view, pvh1, pvh2, pvh3).setDuration(3000).start();

```

### ValueAnimator
ValueAnimator是继承自ObjectAnimator，ValueAnimator本身不提供动画效果，他更像是一个数值发生器，我们定义一个区间，然后通过回调我们自己处理，知道区间行走完毕

```

    ValueAnimator animator = ValueAnimator.ofFloat(0, 100);
     animator.setTarget(view);
     animator.setDuration(1000).start();
     animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
         @Override
         public void onAnimationUpdate(ValueAnimator animation) {
             float animatedValue = (float) animation.getAnimatedValue();
             Log.i("区间进度：", animatedValue + "");
         }
     });


部分结果：
12-06 10:02:29.438 19945-19945/? I/区间进度：: 11.175674
12-06 10:02:29.454 19945-19945/? I/区间进度：: 12.912909
12-06 10:02:29.471 19945-19945/? I/区间进度：: 14.644662
12-06 10:02:29.487 19945-19945/? I/区间进度：: 16.582394
12-06 10:02:29.504 19945-19945/? I/区间进度：: 18.615437
12-06 10:02:29.521 19945-19945/? I/区间进度：: 20.610731
12-06 10:02:29.538 19945-19945/? I/区间进度：: 22.81198
12-06 10:02:29.555 19945-19945/? I/区间进度：: 25.090742

```

### 动画事件的监听

```
animator.addListener(new Animator.AnimatorListener() {
       @Override
       public void onAnimationStart(Animator animation) {

       }

       @Override
       public void onAnimationEnd(Animator animation) {

       }

       @Override
       public void onAnimationCancel(Animator animation) {

       }

       @Override
       public void onAnimationRepeat(Animator animation) {

       }
   });
```

或者 :只是监听部分事件


```
        animator.addListener(new AnimatorListenerAdapter() {
           /**
            * {@inheritDoc}
            *
            * @param animation
            */
           @Override
           public void onAnimationEnd(Animator animation) {
               super.onAnimationEnd(animation);
           }
       });

```

### AnimatorSet
可以实现和PropertyValuesHolder 类似的效果，并且能控制顺序

```
  ObjectAnimator animator1 = ObjectAnimator.ofFloat(view, "translationX", 300);
   ObjectAnimator animator2 = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0f, 1f);
   ObjectAnimator animator3 = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0f, 1f);

   AnimatorSet set = new AnimatorSet();
   set.setDuration(1000);
   set.playSequentially();//循环播放
   set.play().with().before().after()//播放顺序
   set.playTogether(animator1, animator2, animator3);
   set.start();

```
### 在XML中使用属性动画
在res文件夹创建文件夹res/animator   然后将属性动画的xml文件放在这个文件夹里面

```
<?xml version="1.0" encoding="utf-8"?>
<objectAnimator
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:duration="1000"
    android:propertyName="scaleX"
    android:valueTo="1.0"
    android:valueFrom="2.0"
    android:valueType="floatType">
</objectAnimator>

```
使用
```
      Animator anim = AnimatorInflater.loadAnimator(this, R.animator.xxxx);
      anim.setTarget(view);
      anim.start();
```


### 属性动画的简写

```

        view.animate().alpha(0).y(300).setDuration(300).withStartAction(new Runnable() {
            @Override
            public void run() {

            }
        }).withEndAction(new Runnable() {
            @Override
            public void run() {

            }
        }).start();
```
## Android 布局动画
就是在指定的ViewGroup上给ViewGroup增加View的时候添加一个过渡的动画效果，最简单的是在ViewGroup的XML中，是增加的时候


```

        final TextView textView = new TextView(this);

        final ViewGroup test = findViewById(R.id.test);

        View view = findViewById(R.id.view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                test.addView(textView);
                textView.setText("dfjkdssjfdksdsj");
            }
        });

```

![Alt text](dongxiao.gif "效果图")

可以通过LayoutAnimationController来自定义一个View的过渡效果
```
public class MainActivity extends AppCompatActivity {

    private ImageView iv_test;
    private Bitmap bitmap;
    private ImageView iv_test1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//两个imageview
//        iv_test = findViewById(R.id.iv_test);
//        iv_test1 = findViewById(R.id.iv_test1);
         LinearLayout test = findViewById(R.id.test);
        ScaleAnimation sa=new ScaleAnimation(0,1,0,1);
        sa.setDuration(2000);
        LayoutAnimationController lac=new LayoutAnimationController(sa,0.5f);
        lac.setOrder(LayoutAnimationController.ORDER_NORMAL);//顺序
        test.setLayoutAnimation(lac);
    }
}

```

## 插值器

就是动画进行的速率，例如先快后慢，加速运动，匀减速，...
```

public class MainActivity extends AppCompatActivity {

    private ImageView iv_test;
    private Bitmap bitmap;
    private ImageView iv_test1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//两个imageview
//        iv_test = findViewById(R.id.iv_test);
//        iv_test1 = findViewById(R.id.iv_test1);



         LinearLayout test = findViewById(R.id.test);

        ScaleAnimation sa=new ScaleAnimation(0,1,0,1);
        sa.setDuration(2000);
        sa.setInterpolator();// 设置差值器
        LayoutAnimationController lac=new LayoutAnimationController(sa,0.5f);
        lac.setOrder(LayoutAnimationController.ORDER_NORMAL);
        test.setLayoutAnimation(lac);
    }


}

```

## 自定义动画
原理：集成不同的动画比如：ScaleAnimation 然后重写applyTransformation的方法
ScaleAnimation的具体实现
```
    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
      float sx = 1.0f;
     float sy = 1.0f;
     float scale = getScaleFactor();

     if (mFromX != 1.0f || mToX != 1.0f) {
         sx = mFromX + ((mToX - mFromX) * interpolatedTime);
     }
     if (mFromY != 1.0f || mToY != 1.0f) {
         sy = mFromY + ((mToY - mFromY) * interpolatedTime);
     }

     if (mPivotX == 0 && mPivotY == 0) {
         t.getMatrix().setScale(sx, sy);//获取矩阵，对矩阵处理 这个是处理缩放矩阵，所以是一个缩放动画
     } else {
         t.getMatrix().setScale(sx, sy, scale * mPivotX, scale * mPivotY);
     }
    }

```


例子
可以通过Camera来处理矩阵，这个是Android封装的一些信息

```
//自定义动画
public class XAnimation extends Animation {
  //使用Camera处理矩阵
    private Camera mCamera;
    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
//        super.applyTransformation(interpolatedTime, t);
        final Matrix matrix = t.getMatrix();
        mCamera.save();
        mCamera.rotateY(10 * interpolatedTime);
        mCamera.getMatrix(matrix);
        mCamera.restore();
        matrix.preTranslate(x1, y1);
        matrix.postTranslate(-x1, -y1);
    }


    int x1;
    int y1;

    @Override
    public void initialize(int width, int height, int parentWidth, int parentHeight) {
        super.initialize(width, height, parentWidth, parentHeight);
        mCamera = new Camera();
        setDuration(2000);
        setFillAfter(true);
        setInterpolator(new BounceInterpolator());
        x1 = width / 2;
        y1 = height / 2;
    }
}




```
调用
```

public class MainActivity extends AppCompatActivity {
    private ImageView iv_test;
    private Bitmap bitmap;
    private ImageView iv_test1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//两个imageview
//        iv_test = findViewById(R.id.iv_test);
//        iv_test1 = findViewById(R.id.iv_test1);
         LinearLayout test = findViewById(R.id.test);
        XAnimation sa=new XAnimation();
        sa.setDuration(2000);
        LayoutAnimationController lac=new LayoutAnimationController(sa,0.5f);
        lac.setOrder(LayoutAnimationController.ORDER_NORMAL);
        test.setLayoutAnimation(lac);
    }
}
```

![Alt text](device-2018-01-20-235707.png "效果图")


## Android 5.x SVG矢量动画

通过xml绘制动画

### 标签

* M =moveto(M X,Y) ：将画笔移动到指定的坐标位置，但未发生绘制
* L=lineto(L X,Y)  ：将直线移动到指定的位置
* H=horizontal lineto(H X): 画水平线到x坐标的位置
* V=vertical lineto(V,Y):画垂直线到指定Y坐标位置
* C=curveto(C X1,Y1,X2,Y2,ENDX,ENDY):三次贝塞尔曲线
* S=smooth curveto(S X2,Y2,ENDX,ENDY):三次贝塞尔曲线
* Q=quadratic Belzier curve(Q X,Y,ENDX,ENDY):二次贝塞尔曲线
* T=smooth quadratic Belzier curve(T ENDX,ENDY)：映射前面路径后的终点
* A=elliptical Arc(A RX,RY,XROTATION,FALG1,FALG2,X,Y): 弧线
* Z=closePath():关闭路径

> 注意点
* 坐标轴以(0,0)位中心，x向下，y像右
* 所有指令大小写均可，大写是绝对定位，相对全局坐标系，小写是相对定位，参照父布局坐标系
* 指令和数据间的空格可以省略
* 同一个指令出现多次可以只用一个

### 常用指令
* L：L 200 400 从(0,0)到(200,400)的直线
* M：移动坐标点
* A：弧线 RX，RY是椭圆半轴的大小；XROTATION是椭圆的X轴与水平方向顺时针方向的夹角；FLAG1只有两个值 0表示小角弧线，1表示大角弧线；FLAF2只有两个值，确定起点和终点的方向，1为顺时针 2为逆时针；X，Y是终点坐标

### 一个SVG编辑器

http://editor.method.ac/

Inkscape

### Android使用SVG
Google 在Android 5.x中提供了下面两个新的API来支持SVG
* VectorDrawable
* AnimatedVectorDrawable

#### VectorDrawable
```
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:height="200dp"
    android:width="200dp"
    android:viewportHeight="100"   //将200dp分为100份
    android:viewportWidth="100">
</vector>
```

```

<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="200dp"
    android:height="200dp"
    android:viewportHeight="100"
    android:viewportWidth="100">
    <group
        android:name="test"
        android:rotation="0">
        <path
            android:fillColor="#ff0000"
            android:pathData="M 25 50 a 25,25 ,0 1,0 ,50,0"></path>  //这个path就是SVG的路径
    </group>
</vector>
```

![Alt text](图像1516505303.png "效果图")

```
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="200dp"
    android:height="200dp"
    android:viewportHeight="100"
    android:viewportWidth="100">
    <group
        android:name="test"
        android:rotation="0">
        <path
            android:strokeColor="#ff0000"
            android:strokeWidth="2"   //非填充
            android:pathData="M 25 50 a 25,25 ,0 1,0 ,50,0"></path>
    </group>
</vector>
```

![Alt text](图像1516505521.png "效果图")

#### AnimatedVectorDrawable ：这个是有动画的SVG
使用
```
public class MainActivity extends AppCompatActivity {

    private ImageView iv_test;
    private Bitmap bitmap;
    private ImageView iv_test1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LinearLayout test = findViewById(R.id.test);
        ImageView view = findViewById(R.id.view);
        ((Animatable) view.getDrawable()).start();
    }
}
```
布局
```
<?xml version="1.0" encoding="utf-8"?>


<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:id="@+id/test"
    android:animateLayoutChanges="true"
    tools:context="com.smart.myapplication.MainActivity">


    <ImageView
        android:id="@+id/view"
        android:src="@drawable/hahaha"
        android:layout_width="100dp"
        android:layout_height="100dp" />

</LinearLayout>

```

动画:在res/animator文件夹下没有新建 xxxx.xml
```

<?xml version="1.0" encoding="utf-8"?>
<objectAnimator
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:duration="1000"
    android:propertyName="scaleX"
    android:valueTo="1.0"
    android:valueFrom="2.0"
    android:valueType="floatType">
</objectAnimator>
```
hahaha.xml  关联SVG和动画
```
<?xml version="1.0" encoding="utf-8"?>
<animated-vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:drawable="@drawable/wahaha">
<target
    android:animation="@animator/xxxx"
    android:name="test">

</target>
</animated-vector>
```
背景
```
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="200dp"
    android:height="200dp"
    android:viewportHeight="100"
    android:viewportWidth="100">
    <group
        android:name="test"
        android:rotation="0">
        <path
            android:strokeColor="#ff0000"
            android:strokeWidth="2"
            android:pathData="M 25 50 a 25,25 ,0 1,0 ,50,0"></path>
    </group>
</vector>
```

SVG动画实例

java代码
```
public class MainActivity extends AppCompatActivity {
    private ImageView iv_test;
    private Bitmap bitmap;
    private ImageView iv_test1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LinearLayout test = findViewById(R.id.test);
        final ImageView view = findViewById(R.id.view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((Animatable) view.getDrawable()).start();
            }
        });

    }
}

```
布局
```
<?xml version="1.0" encoding="utf-8"?>


<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:id="@+id/test"
    android:animateLayoutChanges="true"
    tools:context="com.smart.myapplication.MainActivity">


    <ImageView
        android:id="@+id/view"
        android:src="@drawable/hahaha"
        android:layout_width="100dp"
        android:layout_height="100dp" />

</LinearLayout>

```

xxxx.xml 定义属性动画
```

<?xml version="1.0" encoding="utf-8"?>
<objectAnimator
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:duration="1000"
    android:propertyName="pathData"
    android:valueTo="M 20,80 L 50,50,80,80"
    android:valueFrom="M 20,80 L 50,80,80,80"   //属性动画
    android:interpolator="@android:anim/bounce_interpolator"
    android:valueType="pathType">
</objectAnimator>
```
hahaha.xml :定义动画
```
<?xml version="1.0" encoding="utf-8"?>
<animated-vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:drawable="@drawable/wahaha">
    <target
        android:name="path1"
        android:animation="@animator/xxxx" />
    <target
        android:name="path2"
        android:animation="@animator/xxxx">

    </target>
</animated-vector>

```

定义路径
```
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="200dp"
    android:height="200dp"
    android:viewportHeight="100"
    android:viewportWidth="100">
    <group
       >
        <path
            android:name="path1"
            android:strokeColor="#ff0000"
            android:strokeWidth="5"
            android:strokeLineCap="round"
            android:pathData="M 20 80 L 50,80,80,20"></path>

        <path
            android:name="path2"
            android:strokeColor="#ffff00"
            android:strokeWidth="5"
            android:strokeLineCap="round"
            android:pathData="M 20 20 L 50,20,80,20"></path>
    </group>
</vector>
```

![Alt text](enenen.gif "效果图")

#### 例子

这个一般是UI给的图，所以就不做非常详细的介绍了

## Android 动画特效

### 展开动画
```

public class MainActivity extends AppCompatActivity {
    private ImageView iv_test;
    private Bitmap bitmap;
    private ImageView iv_test1;
    private View view;
    private View view1;
    private View view3;
    private View view2;
    private View view4;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        view = findViewById(R.id.view);
        view1 = findViewById(R.id.view1);
        view2 = findViewById(R.id.view2);
        view3 = findViewById(R.id.view3);
        view4 = findViewById(R.id.view4);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAnim();
            }
        });
    }
    boolean mFlag = false;
    private void startAnim() {
        ObjectAnimator animator0 = ObjectAnimator.ofFloat(view, "alpha", 1f, 0.5f);
        ObjectAnimator animator1 = ObjectAnimator.ofFloat(view1, "translationY", 200f);
        ObjectAnimator animator2 = ObjectAnimator.ofFloat(view2, "translationX", 200f);
        ObjectAnimator animator3 = ObjectAnimator.ofFloat(view3, "translationY", -200);
        ObjectAnimator animator4 = ObjectAnimator.ofFloat(view4, "translationX", -200);
        AnimatorSet set = new AnimatorSet();
        set.setDuration(500);
        set.setInterpolator(new BounceInterpolator());
        set.playTogether(animator0, animator1, animator2, animator3, animator4);
        set.start();
        mFlag = false;
    }
}

```
布局
```

<?xml version="1.0" encoding="utf-8"?>


<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:id="@+id/test"
    android:gravity="center"
    android:layout_gravity="center"
    android:animateLayoutChanges="true"
    tools:context="com.smart.myapplication.MainActivity">
    <ImageView
        android:id="@+id/view1"
        android:background="#0000ff"
        android:layout_width="50dp"
        android:layout_height="50dp" />
    <ImageView
        android:id="@+id/view2"
        android:background="#ffff00"
        android:layout_width="50dp"
        android:layout_height="50dp" />
    <ImageView
        android:id="@+id/view3"
        android:background="#00ffff"
        android:layout_width="50dp"
        android:layout_height="50dp" />
    <ImageView
        android:id="@+id/view4"
        android:background="#00ff00"
        android:layout_width="50dp"
        android:layout_height="50dp" />
    <ImageView
        android:id="@+id/view"
        android:background="#ff0000"
        android:layout_width="50dp"
        android:layout_height="50dp" />
</RelativeLayout>

```


![Alt text](shuxingdonghua.gif "效果图")


### 计时器动画

```
public class MainActivity extends AppCompatActivity {

    private TextView view;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        view = findViewById(R.id.view);
        ValueAnimator valueAnimator = ValueAnimator.ofInt(1, 100);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                view.setText("$" + (Integer) animation.getAnimatedValue());
            }
        });
        valueAnimator.setDuration(3000);
        valueAnimator.start();
    }

}
```

![Alt text](jishi.gif "效果图")

### 下拉展开动画
```
public class MainActivity extends AppCompatActivity {
    private View hide;
    private float density;
    private float mHidden;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        hide = findViewById(R.id.hide);
        density = getResources().getDisplayMetrics().density;
        mHidden = (float) (density*40+0.5);
    }
    public void llClick(View view) {
        animateOpen(hide);
    }
    private void animateOpen(View hide) {
        hide.setVisibility(View.VISIBLE);
        ValueAnimator animator=CreateDropAnimator(hide,0, (int) mHidden);
        animator.start();
    }
    private ValueAnimator CreateDropAnimator(final View hide, int start, int end) {
        ValueAnimator animator=ValueAnimator.ofInt(start,end);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Integer animatedValue = (Integer) animation.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams=hide.getLayoutParams();
                layoutParams.height=animatedValue;
                hide.setLayoutParams(layoutParams);
            }
        });
        return animator;
    }
}
```
布局
```
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    tools:context="com.smart.myapplication.MainActivity">


    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:background="#ff0000"
        android:gravity="center_vertical"
        android:onClick="llClick"
        android:orientation="horizontal">


        <ImageView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:src="@mipmap/ic_launcher" />

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:src="@mipmap/ic_launcher"
            android:text="ClickMe"
            android:textSize="30dp" />
    </LinearLayout>

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="400dp"
        android:gravity="center_vertical"
        android:id="@+id/hide"
        android:visibility="gone">


        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="I am hidden"
            android:textSize="20dp" />

    </LinearLayout>
</LinearLayout>

```

![Alt text](clickshow.gif "效果图")

# 第八章Activity与Activity调用展分析
## Activity

![Alt text](060009291302389.png "生命周期图")

*  create :创建UI
*  resume ：初始化Pause中释放的资源
*  pause  ： 释放资源，比如camera，sensor，receivers

## Activity任务栈
可以通过AndroidMainfest文件alunchMode来设置也可以通过Intent的flag来设置
* standard： 启动的Activity都在一个任务栈中，先进先出，创建就放入任务栈
* singleTop : 启动的Activity都在一个任务栈中，判断启动的Activity是不是在最上面，如果在栈顶，就不创建，不在栈顶就创建
* singleTask：和singleTop类似，检查整个Activity栈是否存在需要启动的Activity，如果存在，就把他上面的全部销毁，不存在就创建。如果在其他程序中通过SingleTask启动这个Activity，就会创建一个新的任务栈，不是放在启动的任务栈，如果这个Activity已经在任务栈中存在，就不会创建任务栈，而是把它放到前台，放到栈顶   
* singleInstance：和浏览器工作的内容有点相似，如果多个程序启动，如果当前没有在浏览器中打开，就会打开浏览器，否则就会在当前打开的浏览器中访问，并且这个会在singleInstance的Activity会出现在一个新的任务栈中，这个任务栈只存在一个Activity

## Intent FLAG 启动模式

* onInterceptTouchEvent.FLAG_ACTIVITY_NEW_TSK ：这个启动的会放在一个新的task中
* onInterceptTouchEvent.FLAG_ACTIVITY_SINGLE_TOP ：singleTop
* onInterceptTouchEvent.SINGLE_ACTIVITY_CLERA_TOP ：singleTask
* onInterceptTouchEvent.FLAG_ACTIVITY_NO_HISTORY  :A启动B ，B用这个模式启动C 那么任务栈中就会剩下 AC B会关闭

## 清空任务栈

* clearTaskOnLaunch 启动的时候回将他上方所有的Activity清楚
* finishOnTaskLaunch：离开这个activity任务栈的时候吧自己关闭
* alwaysRetainTaskState： 这个任务栈不接受任何清理命令，一直存在

# 第九章 Android 系统信息与安全机制简介
## Android系统信息获取
可以通过android.os.BUILD和SystemProPerty来获取，参数太多了没救不列出来了
## AndroidApk应用信息获取之PacketManager
获取包的信息，
## AndroidApk应用信息获取之ActivityManager
获取应用运行时的信息
## 解析Packets.xml获取系统信息
在/data/system/packets.xml
## ANdroid安全机制
### 混淆代码
### 权限机制
### 数字证书
### linux内核层安钻机制--UID和王文权限控制
### Android虚拟机沙箱机制--沙箱隔离
## APK反编译简介
* apktool
* dex2jar，jd-gui

# 第10章 Android 性能优化入门

## 布局优化
### Android UI渲染机制
人眼感觉流畅的画面大概是40帧每秒到60帧每秒，而且 他是16ms会绘制一次，如果一次绘制在20ms，就会在16ms的时候发出VSYNC信号，就无法绘制，该帧就会被丢弃，导致丢帧
在开发者选项中，可以 选择 PROFILE GPU Rendering(GPU 呈现模式分析) 并选中 OnScreen as bars 就会在屏幕上出现一些条线图，尽量让所有的条形图都在这个绿线一下



### 避免 OverDraw

在开发者选项中，选择 Enable GPU Overdraw(调试 GPU 过度绘制) 可以查看绘制的次数，蓝色区域是绘制比较少的，红色是绘制多的，尽量减少红色区域

### 优化布局层级
1. Google建议嵌套的布局层数不要超过10层，如果超过10层就会出现绘制性能下降明显的情况，Android是通过对树的遍历遍历整个xml的，所以布局最好控制在5层以内
2. 避免嵌套过多无用布局
3. 使用<include> 复用Layout，如果这个布局已经创建过了，在第二次创建的时候速度会快很多的，但是要适度，产品更改的比较频繁就尽量少用，不然会导致这个布局过于复杂
4. 使用ViewStub延时加载，就是第一次加载的时候不出现，在调用setVisibility（View.VISIBLE）的时候放到布局树上面，如果使用setVisibility（View.GONE） 只是不显示，但是还是会放在布局树上面
### Hierarchy Viewer 这个应该在优化布局里面的，但是比较复杂和重要就拿出来了
建议使用模拟器，不然可能会失败的，使用Android Studio 步骤：
Tool   -->    Android --> Android Device Monitor  -->

![Alt text](图像1516719324.png "生命周期图")
这样就可以很方便的找到冗余的布局了


## 内存优化

### 什么是内存
* 寄存器 ：这个无法控制
* 栈(Stack):存放基本数据类型和对象的引用，但是对象本身不在栈中，而是放在堆中
* 堆(Heap) ： 存放new出来的对象和数组，有GC来管理
* 静态存储区域(Static Field): 程序运行的过程中一直存在的数据，比如static修饰的变量
* 常量池(Constant Pool) 存放常量

在进行分配的时候 new 一个对象 这个对象在栈中有一个引用，在堆中存储，当该变量的作用域结束后，栈中的内存会回收，但是堆中的内存不会立即回收，等待GC来回收

### 获取Android 系统内存信息

#### process Stats
```
adb shell dumpsys procstats

```
#### Meminfo
使用系统上的内存监视器，在设置-APP-Running里面打开，或者 `adb shell dumpsys meminfo`


### 内存回收
#### bitmap优化
开发过Android都知道Bitmap是引起OOM最主要的原因之一
如何优化
* 使用合适分辨率和大小的图片
* 及时回收内存，使用bitmap。recycle()方法释放资源，在Android3.0以后，bitmap放到了堆中，由GC管理，就不用手动释放了

#### 代码优化
* 减少枚举和迭代器的使用
* 避免使用IOC框架，反射会降低效率
* Cursor，Sensor，File，Receiver等及时释放和回收
* 使用SurfaceView替代View进行大量绘图操作
* 使用OpenGl和RenderScript进行绘图操作
* 尽量缓存视图，而不是每次都执行infalte()

### Lint工具
这个先不讲


## 使用Android Studio的Memory Monitor 工具
## 使用TraceView工具优化App性能
### 生成TraceView日志的两种方法
* 使用Debug类辅助：Debug.startMethodTracing() 方法
在OnCreate中使用Debug.startMethodTracing() 在OnDestory中使用Debug.stopMethodTracing()，可以精确监听，然后在/sdcard/dmtrace.trace 保存文件，需要读写sd卡权限，当然可以自定义输出路径 `adb pull /sdcard/trace_log.trace     /local/LOG/`

* 使用Android Device Monitor工具

![Alt text](rrrrrrr.png "位置")

### 分析Trace View日志
主要显示信息






| 英文       | 中文和含义    |    
| ------------- |:-------------:|
|Incl CPU Time    |   某个方法占用CPU的时间      |
| Excl CPU Time  |   某个方法本身（不包括字方法）占用CPU的时间  |  
| Incl  Time  |  某个方法真正执行的时间   |  
| Excl Real Time  |  某个方法本身（不包括字方法）真正执行的时间  |  
|Calls +resurCalls| 调用次数+递归回调的次数|

每个时间都包含两列。一个是实际时间，一个是百分比，分析的时候通常从Incl CPU Time 和Calls +resurCalls开始分析，对占用时间长的进行重点分析，如果占用时间长并且Calls +resurCalls少，就可以列为重点怀疑对象了

## 使用Mat工具进行分析
打开 Android Device Monitor工具 如图

![Alt text](图像1516802863.png "操作")


![Alt text](图像1516802989.png "操作")


这里有一个小技巧，就是不停的点击Cause Gc按钮 如果data Object一栏中的Total Size 有明显变化，就代表有可能存在内存泄漏

![Alt text](图像1516803153.png "操作")

在这里到处一个.hprof文件。这个就是我们要分析的文件，不过这个不能直接使用mat打开，要使用命令行转换


```

D:\sdk\platform-tools\hprof-conv input.hprof out.hprof

```

将转换后的文件使用mat打开。打开mat工具，选择open a heap dump 选项
![Alt text](图像1516803752.png "操作")


* Histogram 直方图 用于显示内存中每一个对象的数量，大小和名称。可以在最上面一样过滤关键字，在选择的对象上右击，选择List objects-with incoming references可以查看具体的对象

![Alt text](图像1516804054.png "操作")

* Dominator Tree 会对内存中的对象按照大小进行排序，并显示对象之间的引用结构，通过分析大对象找到内存消耗的原因

![Alt text](图像1516804298.png "操作")

## Dumpsys 命令分析系统状态
在使用Dumpsys时，可以通过输入`adb shell dumpsys+` 获取提示


| 参数       | 中文和含义    |    
| ------------- |:-------------:|
|activity   |   显示所有activity栈的信息      |
|meminfo |   内存信息  |  
|battery|  电池信息   |  
| package  |  包信息 |  
|wifi| 显示WIFI信息|
|alarm| 显示alarm信息|
|procstats|显示内存状态|

性能优化这个是一个具有挑战的工作，这些只是入门的方式

# 第十一章 搭建云端服务器
## 几个备胎，需要的时候可以查看官方文档
* 原子云
* avos cloud
* powerApp

# 第十二章 Android 5.x新特性
## Android 5.x UI设计
https://developer.android.com/design/material/index.html?hl=zh-cn 建议看一下官方文档，这一章节就简单说一下，这一张主要针对不常用的部分
## Meterial design 主题
三种默认主题
```
@android:style/Theme.Material(dark version)
@android:style/Theme.Material.Light(light version)
@android:style/Theme.Material.Light.DarkActionBar

```
## Palette
是一个取色器，可以根据不同的图片进行取色，然后设置相关的主题

* Vibrant(充满活力的)
* Vibrant dark(充满活力的 黑色)
* Vibrant light(充满活力的 亮色)
* Muted(柔和的)
* Muted Drak(柔和的黑)
* Muted Light(柔和的亮)

## 视图与阴影
### 阴影
Android 5.0以后新添加了一个属性z， `android：elevation=xxdp` 就会有一个高度，这样就会有一个投影，类似一个灯泡在上方 下方有一个桌子，地上就会有阴影。

## Tinting和Clipping
### Tinting 着色
修改Alpha遮罩修改图像的颜色
```

<ImageView
    android:id="@+id/iv"
    android:src="@mipmap/pg_11111111111"
    android:layout_width="match_parent"
    android:elevation="5dp"
    android:tint="#ff0000"
    android:tintMode="add"
    android:layout_height="match_parent" />
```

### Clipping 裁切
ViewOutlineProvide

代码
```


public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
      final TextView tvCircle= findViewById(R.id.tv_circle);
        final TextView tvRect=   findViewById(R.id.tv_rect);


        ViewOutlineProvider viewOutlineProvider1=new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setOval(0,0,tvCircle.getWidth(),tvCircle.getHeight());
            }
        };

        ViewOutlineProvider viewOutlineProvider2=new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0,0,tvRect.getWidth(),tvRect.getHeight(),30);
            }
        };
        tvCircle.setOutlineProvider(viewOutlineProvider1);
        tvRect.setOutlineProvider(viewOutlineProvider2);

    }
}

```

布局

```
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    tools:context="com.smart.myapplication.MainActivity">

<ImageView
    android:id="@+id/iv"
    android:src="@mipmap/pg_11111111111"
    android:layout_width="wrap_content"
    android:elevation="5dp"
    android:tint="#ff0000"
    android:tintMode="add"
    android:layout_height="wrap_content" />

    <TextView
        android:id="@+id/tv_rect"
        android:elevation="1dp"
        android:layout_width="100dp"
        android:layout_height="100dp" />

    <TextView
        android:id="@+id/tv_circle"
        android:elevation="1dp"
        android:layout_width="100dp"
        android:layout_height="100dp" />
</LinearLayout>

```

![Alt text](device-2018-01-25-230916.png "效果")

## 列表与卡片
* RecyclerView
* CardView
这两个非常的常用这里就不讲了

## 过度动画
提供了三种过度动画

* 进入
  * explode(分解) 从屏幕中间进出,移动视图
  * slide(滑动) 从屏幕边缘进出,移动视图
  * fade(淡出) 通过改变透明度达到添加或者移除视图
* 退出
  * explode(分解) 从屏幕中间进出,移动视图
  * slide(滑动) 从屏幕边缘进出,移动视图
  * fade(淡出) 通过改变透明度达到添加或者移除视图
* 共享元素
  * changeBounds 改变目标视图的布局边界
  * changeClipBounds 建材目标是土边界
  * changeTransform 改变目标视图的缩放比例和旋转角度
  * changeImageTransform 改变目标图片的大小和缩放比例


A进入B  进入和退出
A activity`startActivity(intent，ActivityOptions.makeSceneTransitionAnimation(this),toBundle())`
B activity `getwindows.requestFeature(window.feature_content_transitions)`
B activity `getwindowssetEnterTransition(new Explode())`
B activity `getwindowssetExitTransition(new Explode())`

A进入B 转场动画
对需要共享的元素添加属性
`androidxref:transitionName="xxx"`
如果只有一个需要共享
`startActivity(intent，ActivityOptions.makeSceneTransitionAnimation(this),toBundle())`
如果多个需要共享
`startActivity(intent，ActivityOptions.makeSceneTransitionAnimation(this，Pair.create(view,"share"),，Pair.create(fab,"fab")),toBundle())`

## 动画效果
### 水波纹
### Circular Reveal

```
/**
*
* centerX 动画开始中心点
* centerY 动画开始中心点
* startRadius 开始半径
* endRadius 结束半径
*/
public static animator createCircularReveal(View view,int centerX,int centerY,float startRadius,float endRadius){
  return new RevealAnimator(view,centerX,centerY,startRadius,endRadius);
}

```


### Vuew state changes animation
* stateListAnimator
* animated-selector

## ToolBar
## Notification 每个版本都有 5.x 6.x 7.x 8.x 这个要去看文档

# 第十三章 Android 实例与提高

这里会花时间写一个记账软件
