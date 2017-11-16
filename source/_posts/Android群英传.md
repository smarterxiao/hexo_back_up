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
