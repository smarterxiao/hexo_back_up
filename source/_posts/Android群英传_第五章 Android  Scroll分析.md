---
title: Android群英传 第五章 Android  Scroll分析
date: 2017-11-01 22:17:05
categories: android
top : 105
tags:
- 基础
- Android群英传
---

# 坐标系
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
* getLocationOnScreen()  获取View到屏幕的距离
# 滑动的七种方法

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
        I/event x:: 54.0
        I/event y:: 23.0
        I/view x:: 175.0
        I/view y:: 175.0
        I/event getRawX:: 229.0
        I/event getRawY:: 478.0
        I/left:: 175
        I/right:: 287
        I/bottom:: 241
        I/top:: 175
```
##  Layout方法

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

##  offsetLeftAndRight()与offsetTopAndBottom()


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

##  LayoutParams
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

##  scrollTo和scrollBy
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

##  Scroller
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


## 属性动画 这个后面讲
## ViewDragHelper
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
