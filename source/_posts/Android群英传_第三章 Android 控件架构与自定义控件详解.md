---
title: Android群英传 第三章 Android 控件架构与自定义控件详解
date: 2017-11-01 22:17:03
categories: android
top : 103
tags:
- 基础
- Android群英传
---


#  Android 控件架构

   抛开平常偷懒使用的butterknife。平常使用最多的的就是`setContentView` 然后`findViewById`。里面具体发生了那些事情，在这里做一个简单的介绍。

 ![Alt text](图像1509633530.png "AndroidUI架构")

 ![Alt text](图像1509634201.png "简陋的视图树")

 ** findViewById是深度遍历的** 在`setContentView`之后 ActivityManagerService 会回调OnResume()方法 这个时候系统才会将DecorView添加到PhoneWindow中让他显示出来，完成界面的绘制

#  View的测量和绘制

## View的测量
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

## View布局

就是`onLayout()`的过程

##  View绘制

 绘制主要需要在`onDraw()`方法中进行，有一个画布，所有的绘制操作都和画布有关，后面会详细解释的，最好理解一下ps的图层，图像是一层一层叠加起来的

![Alt text](tuceng.jpg "图层的理解")
##  ViewGroup的测量

> ViewGroup在测量的时候回遍历所有子View的measure方法，然后会调用layout方法进行布局，最后在绘制

##  红包布局实战

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
# 自定义View

##  一些重要的回调方法

  * `onFinishInfalte` 从XML加载完组件回调
  * `onSizeChanged` 组件大小改变时回调
  * `onMeasure` 回调该方法来进行测量
  * `onLayout` 回掉该方法确定显示位置
  * `onTouchEvent` 监听到触摸事件是回调

## 对textView的自定义

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


## 闪动的TextView
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

#  自定义ViewGroup
## 一个弹性scrollView
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

# Android 事件拦截机制分析

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
