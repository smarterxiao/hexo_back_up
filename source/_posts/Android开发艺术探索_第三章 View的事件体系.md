---
title: Android开发艺术探索 第三章 View的事件体系
date: 2018-01-27 17:08:22
top : 203
tags:
- 进阶
- Android开发艺术探索
categories: android
---
> 这个是非常重要的一个内容，一般面试的时候都会问一下的

# View基础知识
## 什么是View？
View是android中所有控件的基类，用户的交互其实是可View直接交互的，可以把它理解为界面层的抽象，所有的View可以理解为屏幕中间的一块矩形区域
ViewGroup是一个控件组。

## View的位置？
系统是通过坐标系来确认View的位置的，View主要有四个属性：top，left，right，bottom。
* top View最上面距离父控件顶的距离
* left View最左面距离父控件左面的距离
* right View最右面距离父控件左面的距离
* bottom View最下面距离父控件顶的距离

需要注意一下这里的坐标系，他是相对于父控件来讲的。
这里用图来说明一下


![Alt text](图像1521983638.png "一个例子")


如何获取这四个值呢
* left=view.getLeft();
* right=view.getRight();
* Top=view.getTop();
* Bottom=view.getBottom();

控件的 width=right-left
控件的 height=bottom-top

这里要注意下：android 3.0 之后为view新加了4个属性，分别为x,y,translationX和translationY
其中x和y是View的左上角坐标，而translationX和translationY是View左上角相对于父容器的偏移量这几个值也是相对于父容器的坐标值。并且translationX和translationY的默认值是0。和View的四个基本位置参数一样，view也为他们提供了get和set方法，换算关系如下
x=left+translationX
y=top+translationY
需要注意的是，在View的平移过程中，top和left表示原始右上角的位置信息。其值不会发生改变，此时发生改变的是x，y，translationX和translationY

在5.0以后新加了x这个属性，这个是z轴的距离，用来显示阴影，想象一下桌子上面悬挂一个灯泡 然后会在桌子边缘有一个阴影。而x是桌子的高度 这样可以控制影子的大小。

这里补充一点，怎么样获取控件到屏幕的距离呢，而不是到父控件的距离,这里有两个方法
* getLocationOnScreen():用来获取一个View在屏幕中的位置
* getLocationInWindow():用来获取一个View在其所在窗口中的位置

```
int[] screenLocation = new int [2];
int[] windowLocation = new int [2];
getLocationInWindow();
getLocationOnScreen(windowLocation)
```

在activity中他们获取的值是一样的，如果不是在activity中，就不一样了，因为activity是一个全屏的windows，而dialog不是一个全屏的dialog,在toast中和dialog是一致的,这个后面会介绍的

下面写一个demo来验证一下x=left+translationX和y=top+translationY的换算关系

布局
```
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="#00ffff"
    tools:context="com.smart.kaifa.MainActivity">
    <FrameLayout
        android:layout_width="200dp"
        android:layout_height="400dp"
        android:layout_marginLeft="100dp"
        android:background="#ffff00">

        <TextView
            android:background="#ff0000"
            android:id="@+id/test"
            android:layout_width="20dp"
            android:layout_marginLeft="20dp"
            android:layout_marginTop="20dp"
            android:layout_height="20dp" />
    </FrameLayout>
</FrameLayout>

```

代码
```
public class MainActivity extends AppCompatActivity {

    private int left;
    private int right;
    private int bottom;
    private int top;
    private int width;
    private int height;
    private float x;
    private float y;
    private float translationX;
    private float scrollX;
    private float translationY;
    private float scrollY;
    private View viewById;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewById = findViewById(R.id.test);


    }
    //activity 真正可见的时间点
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        // TODO Auto-generated method stub
        if(hasFocus){
            left = viewById.getLeft();
            right = viewById.getRight();
            bottom = viewById.getBottom();
            top = viewById.getTop();
            width = viewById.getWidth();
            height = viewById.getHeight();
            x = viewById.getX();
            y = viewById.getY();
            translationX = viewById.getTranslationX();
            translationY = viewById.getTranslationY();
            scrollX = viewById.getScrollX();
            scrollY = viewById.getScrollY();

            Log.i("第一次： left：", left + "");
            Log.i("第一次： right：", right + "");
            Log.i("第一次： bottom：", bottom + "");
            Log.i("第一次： top：", top + "");
            Log.i("第一次： width：", width + "");
            Log.i("第一次： height：", height + "");
            Log.i("第一次： x：", x + "");
            Log.i("第一次： y：", y + "");
            Log.i("第一次： translationX：", translationX + "");
            Log.i("第一次： translationY：", translationY + "");
            Log.i("第一次： scrollX：", scrollX + "");
            Log.i("第一次： scrollY：", scrollY + "");


            ObjectAnimator animator = ObjectAnimator.ofFloat(viewById, "translationX", 200);
            animator.setDuration(1000);
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    left = viewById.getLeft();
                    right = viewById.getRight();
                    bottom = viewById.getBottom();
                    top = viewById.getTop();
                    width = viewById.getWidth();
                    height = viewById.getHeight();
                    x = viewById.getX();
                    y = viewById.getY();
                    translationX = viewById.getTranslationX();
                    translationY = viewById.getTranslationY();
                    scrollX = viewById.getScrollX();
                    scrollY = viewById.getScrollY();
                    Log.i("第二次： ********", "***************************************************************************");
                    Log.i("第二次： left：", left + "");
                    Log.i("第二次： right：", right + "");
                    Log.i("第二次： bottom：", bottom + "");
                    Log.i("第二次： top：", top + "");
                    Log.i("第二次： width：", width + "");
                    Log.i("第二次： height：", height + "");
                    Log.i("第二次： x：", x + "");
                    Log.i("第二次： y：", y + "");
                    Log.i("第二次： translationX：", translationX + "");
                    Log.i("第二次： translationY：", translationY + "");
                    Log.i("第二次： scrollX：", scrollX + "");
                    Log.i("第二次： scrollY：", scrollY + "");
                }
            });
            animator.start();

        }
        super.onWindowFocusChanged(hasFocus);
    }


}

```


```
03-25 22:13:16.307 18794-18794/com.smart.kaifa I/第一次： left：: 70
03-25 22:13:16.307 18794-18794/com.smart.kaifa I/第一次： right：: 140
03-25 22:13:16.307 18794-18794/com.smart.kaifa I/第一次： bottom：: 140
03-25 22:13:16.307 18794-18794/com.smart.kaifa I/第一次： top：: 70
03-25 22:13:16.307 18794-18794/com.smart.kaifa I/第一次： width：: 70
03-25 22:13:16.307 18794-18794/com.smart.kaifa I/第一次： height：: 70
03-25 22:13:16.307 18794-18794/com.smart.kaifa I/第一次： x：: 70.0
03-25 22:13:16.307 18794-18794/com.smart.kaifa I/第一次： y：: 70.0
03-25 22:13:16.307 18794-18794/com.smart.kaifa I/第一次： translationX：: 0.0
03-25 22:13:16.307 18794-18794/com.smart.kaifa I/第一次： translationY：: 0.0
03-25 22:13:16.307 18794-18794/com.smart.kaifa I/第一次： scrollX：: 0.0
03-25 22:13:16.307 18794-18794/com.smart.kaifa I/第一次： scrollY：: 0.0
03-25 22:13:17.314 18794-18794/com.smart.kaifa I/第二次： ********: ***************************************************************************
03-25 22:13:17.314 18794-18794/com.smart.kaifa I/第二次： left：: 70
03-25 22:13:17.314 18794-18794/com.smart.kaifa I/第二次： right：: 140
03-25 22:13:17.315 18794-18794/com.smart.kaifa I/第二次： bottom：: 140
03-25 22:13:17.315 18794-18794/com.smart.kaifa I/第二次： top：: 70
03-25 22:13:17.315 18794-18794/com.smart.kaifa I/第二次： width：: 70
03-25 22:13:17.315 18794-18794/com.smart.kaifa I/第二次： height：: 70
03-25 22:13:17.315 18794-18794/com.smart.kaifa I/第二次： x：: 270.0
03-25 22:13:17.316 18794-18794/com.smart.kaifa I/第二次： y：: 70.0
03-25 22:13:17.316 18794-18794/com.smart.kaifa I/第二次： translationX：: 200.0
03-25 22:13:17.316 18794-18794/com.smart.kaifa I/第二次： translationY：: 0.0
03-25 22:13:17.316 18794-18794/com.smart.kaifa I/第二次： scrollX：: 0.0
03-25 22:13:17.317 18794-18794/com.smart.kaifa I/第二次： scrollY：: 0.0
```

可以看到:需要注意的是，在View的平移过程中，top和left表示原始右上角的位置信息。其值不会发生改变，此时发生改变的是x，y，translationX和translationY

x=left+translationX
y=top+translationY
这个是是不变的，交换一下顺序
left=x-translationX   
top=y-translationY

可以这么理解 x是当前相对控件的位置，而translationX是平移的距离。所以他们的差值就是开始的位置

熟悉了控件的摆放位置之后，下面就了解一下用户手指的操作时间

## Motion和TouchSlop
1. MotionEvent
这个是手指触摸屏幕后产生的事件：
* ACTION_DOWN： 按下
* ACTION_MOVE：移动
* ACTION_UP：离开
MotionEvent也为我们提供了两种坐标

MotionEvent.getX();MotionEvent.getY();  这个返回的是点击位置相对于当前View左上角的x和y坐标
MotionEvent.getRawX();MotionEvent.getRawY();这个返回的是点击位置相对于屏幕左上角的坐标

布局
```
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="#00ffff"
    tools:context="com.smart.kaifa.MainActivity">
    <FrameLayout
        android:layout_width="200dp"
        android:layout_height="400dp"
        android:layout_marginLeft="100dp"
        android:background="#ffff00">
        <com.smart.kaifa.TestTouchView
            android:background="#ff0000"
            android:id="@+id/test"
            android:layout_width="20dp"
            android:layout_marginLeft="20dp"
            android:layout_marginTop="20dp"
            android:layout_height="20dp" />
    </FrameLayout>
</FrameLayout>

```
代码
```
public class TestTouchView extends android.support.v7.widget.AppCompatTextView {
    public TestTouchView(Context context) {
        super(context);
    }
    public TestTouchView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public TestTouchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.i("x ：",event.getX()+"");
        Log.i("y ：",event.getY()+"");
        Log.i("rawX ：",event.getRawX()+"");
        Log.i("rawY ：",event.getRawY()+"");
        return super.onTouchEvent(event);
    }
}

```
结果
```
第一次点击
03-25 22:39:10.299 24542-24542/com.smart.kaifa I/x ：: 33.0
03-25 22:39:10.300 24542-24542/com.smart.kaifa I/y ：: 8.0
03-25 22:39:10.300 24542-24542/com.smart.kaifa I/rawX ：: 453.0
03-25 22:39:10.301 24542-24542/com.smart.kaifa I/rawY ：: 358.0

第二次点击
03-25 22:39:20.016 24542-24542/com.smart.kaifa I/x ：: 39.0
03-25 22:39:20.017 24542-24542/com.smart.kaifa I/y ：: 20.0
03-25 22:39:20.017 24542-24542/com.smart.kaifa I/rawX ：: 459.0
03-25 22:39:20.017 24542-24542/com.smart.kaifa I/rawY ：: 370.0
```


2. TouchSlop
这个是系统提供的被认为滑动的最小距离，换句话说,如果一次滑动距离小于这个值，就表示没有滑动，防止手抖，这个是和设备相关的一个变量，每一个设备值可能是不同的，
`ViewConfiguration.get(getContext).getScaledTouchSlop`来获取，过滤一些临界值

## VelocityTracker GrstureDetrctor和Scroller
1. VelocityTracker 用来速度追踪
速度追踪，用于追踪手指在滑动过程中的速度，包括水平和竖直方向的速度
使用 在view的`onTouchEvent`中
```
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //设置追踪
        VelocityTracker velocityTracker = VelocityTracker.obtain();
        velocityTracker.addMovement(event);
        // 收获结果
        velocityTracker.computeCurrentVelocity(1000);
        float xVelocity = velocityTracker.getXVelocity();
        float yVelocity= velocityTracker.getYVelocity();
        return super.onTouchEvent(event);
    }
```
在一步中有两点要注意：第一点：获取速度之前必须先计算速度`velocityTracker.computeCurrentVelocity`
第二点是：这里的速度是指一段时间内手指所滑动过的像素数，比如讲时间间隔设置为1000ms，在1s内手指水平方向滑动100个像素，速度就是100，速度可以是负数，当手指从右往左滑动的时候就是负值：
速度=(终点位置-起始位置)/时间
不需要的时候要回收
```
      velocityTracker.clear();
     velocityTracker.recycle();
```

2. GrstureDetrctor 用来速度追踪
手势检测，用于辅助检测用户的单击，滑动，长按，双击等行为
使用
```
public class TestTouchView extends android.support.v7.widget.AppCompatTextView implements GestureDetector.OnDoubleTapListener {

    private GestureDetector mGestureDetector;

    public TestTouchView(Context context) {
        super(context);
        init();
    }

    public TestTouchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {

        mGestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {

            /**
             * 手指按下屏幕会触发
             *
             * @param e
             * @return
             */
            @Override
            public boolean onDown(MotionEvent e) {
                Log.i("----------------", "onDown");
                return false;
            }

            /**
             * 手指按下屏幕会触发,还没松开或拖动，由一个ACTION_DOWN触发
             * 注意和onDown的区别，它是一种状态
             *
             * @param e
             * @return
             */
            @Override
            public void onShowPress(MotionEvent e) {
                Log.i("----------------", "onShowPress");
            }

            /**
             * 手指（轻轻触摸屏幕后）松开，伴随着1个MotionEvent ACTION_UP 而触发，这个是单击行为
             *
             * @param e
             * @return
             */
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                Log.i("----------------", "onSingleTapUp");
                return false;
            }

            /**
             * 手指按下屏幕并且拖动，有一个ACTION_DOWN和多个ACTION_MOVE触发，这是触动行为
             *
             * @param e1
             * @param e2
             * @param distanceX
             * @param distanceY
             * @return
             */
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                Log.i("----------------", "onScroll");
                return false;
            }

            /**
             * 长久按住屏幕不放，就是长按
             *
             * @param e
             */
            @Override
            public void onLongPress(MotionEvent e) {
                Log.i("----------------", "onLongPress");
            }

            /**
             * 用户按下屏幕，快速滑动后松开，由1个ACTION_DOWN、多个ACTION_MOVE，和一个ACTION_UP组成
             *
             * @param e1
             * @param e2
             * @param velocityX
             * @param velocityY
             * @return
             */
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                Log.i("----------------", "onFling");
                return false;
            }

        });
        mGestureDetector.setOnDoubleTapListener(this);
        mGestureDetector.setIsLongpressEnabled(false);
        // 这里注意： 要返回true 不然GestureDetector的一些回调不会执行，比如onscroll
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mGestureDetector.onTouchEvent(event);
                Log.i("----------------", "onTouch");
                return true;
            }


        });
    }

    public TestTouchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.i("----------------", "onTouchEvent");
        return mGestureDetector.onTouchEvent(event);
    }

    /**
     * 严格的单击行为，注意他和onSingleTabUp的区别，如果触发了onSingleTapConfirmed，那么后面不可能在精耕者另一次单击行为，即这只可能是一次单击事件。，而不是双击中的一次单击
     *
     * @param e
     * @return
     */
    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        Log.i("----------------", "onSingleTapConfirmed");
        return false;
    }

    /**
     * 双击，由两次连续的单机组成，他不可能和onSingleTapConfirmed共存
     *
     * @param e
     * @return
     */
    @Override
    public boolean onDoubleTap(MotionEvent e) {
        Log.i("----------------", "onDoubleTap");
        return false;
    }

    /**
     * 表示发生了双击行为，在双击期间，ACTION_DOWN、ACTION_MOVE和ACTION_UP会触发此回调
     *
     * @param e
     * @return
     */
    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        Log.i("----------------", "onDoubleTapEvent");
        return false;
    }
    // 这里也可以拦截
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (mGestureDetector.onTouchEvent(ev)) {
            return mGestureDetector.onTouchEvent(ev);
        }
        return super.dispatchTouchEvent(ev);
    }
}

```


|   方法名  | 描述     |    所属接口|
| ------------- |:-------------:| :-------------:|
| onDown        |     手指按下屏幕会触发|OnGestureListener |
| onShowPress     |   手指按下屏幕会触发,还没松开或拖动，由一个ACTION_DOWN触发,注意和onDown的区别，它是一种状态   |  OnGestureListener |
| onSingleTapUp      |手指（轻轻触摸屏幕后）松开，伴随着1个MotionEvent ACTION_UP 而触发，这个是单击行为   |OnGestureListener |
| onScroll       |手指按下屏幕并且拖动，有一个ACTION_DOWN和多个ACTION_MOVE触发，这是触动行为  |OnGestureListener  |
| onLongPress       |长久按住屏幕不放，就是长按   |OnGestureListener  |
| onFling     |用户按下屏幕，快速滑动后松开，由1个ACTION_DOWN、多个ACTION_MOVE，和一个ACTION_UP组成  |OnGestureListener  |
| onSingleTapConfirmed|严格的单击行为，注意他和onSingleTabUp的区别，如果触发了onSingleTapConfirmed，那么后面不可能在精耕者另一次单击行为，即这只可能是一次单击事件。，而不是双击中的一次单击|OnDoubleTapListener|
| onDoubleTap     |双击，由两次连续的单机组成，他不可能和onSingleTapConfirmed共存  |OnDoubleTapListener  |
| onDoubleTapEvent   |表示发生了双击行为，在双击期间，ACTION_DOWN、ACTION_MOVE和ACTION_UP会触发此回调   |OnDoubleTapListener  |


一次单击
```
03-26 22:30:34.055 28182-28182/com.smart.kaifa I/----------------: onDown
03-26 22:30:34.056 28182-28182/com.smart.kaifa I/----------------: onDown
03-26 22:30:34.057 28182-28182/com.smart.kaifa I/----------------: onTouch
03-26 22:30:34.101 28182-28182/com.smart.kaifa I/----------------: onTouch
03-26 22:30:34.101 28182-28182/com.smart.kaifa I/----------------: onSingleTapUp
03-26 22:30:34.102 28182-28182/com.smart.kaifa I/----------------: onSingleTapUp
03-26 22:30:34.102 28182-28182/com.smart.kaifa I/----------------: onTouch
03-26 22:30:34.357 28182-28182/com.smart.kaifa I/----------------: onSingleTapConfirmed
```

一次双击
可以看到`onSingleTapConfirmed`是在最后的调用的，这样就可以在双击中获取单击，防止用户连续点击了
```
03-26 22:30:52.937 28182-28182/com.smart.kaifa I/----------------: onDown
03-26 22:30:52.939 28182-28182/com.smart.kaifa I/----------------: onDown
03-26 22:30:52.939 28182-28182/com.smart.kaifa I/----------------: onTouch
03-26 22:30:52.957 28182-28182/com.smart.kaifa I/----------------: onTouch
03-26 22:30:52.958 28182-28182/com.smart.kaifa I/----------------: onSingleTapUp
03-26 22:30:52.958 28182-28182/com.smart.kaifa I/----------------: onSingleTapUp
03-26 22:30:52.958 28182-28182/com.smart.kaifa I/----------------: onTouch
03-26 22:30:53.063 28182-28182/com.smart.kaifa I/----------------: onDoubleTap
03-26 22:30:53.063 28182-28182/com.smart.kaifa I/----------------: onDoubleTapEvent
03-26 22:30:53.063 28182-28182/com.smart.kaifa I/----------------: onDown
03-26 22:30:53.063 28182-28182/com.smart.kaifa I/----------------: onDown
03-26 22:30:53.063 28182-28182/com.smart.kaifa I/----------------: onTouch
03-26 22:30:53.091 28182-28182/com.smart.kaifa I/----------------: onDoubleTapEvent
03-26 22:30:53.091 28182-28182/com.smart.kaifa I/----------------: onDoubleTapEvent
03-26 22:30:53.091 28182-28182/com.smart.kaifa I/----------------: onTouch
03-26 22:30:53.101 28182-28182/com.smart.kaifa I/----------------: onDoubleTapEvent
03-26 22:30:53.101 28182-28182/com.smart.kaifa I/----------------: onDoubleTapEvent
03-26 22:30:53.101 28182-28182/com.smart.kaifa I/----------------: onTouch
03-26 22:30:53.102 28182-28182/com.smart.kaifa I/----------------: onDoubleTapEvent
03-26 22:30:53.102 28182-28182/com.smart.kaifa I/----------------: onSingleTapUp
03-26 22:30:53.102 28182-28182/com.smart.kaifa I/----------------: onTouch
03-26 22:30:53.363 28182-28182/com.smart.kaifa I/----------------: onSingleTapConfirmed


```

一次移动
```
03-26 22:31:26.057 28182-28182/com.smart.kaifa I/----------------: onDown
03-26 22:31:26.057 28182-28182/com.smart.kaifa I/----------------: onDown
03-26 22:31:26.058 28182-28182/com.smart.kaifa I/----------------: onTouch
03-26 22:31:26.147 28182-28182/com.smart.kaifa I/----------------: onTouch
03-26 22:31:26.151 28182-28182/com.smart.kaifa I/----------------: onShowPress
03-26 22:31:26.151 28182-28182/com.smart.kaifa I/----------------: onShowPress
03-26 22:31:26.163 28182-28182/com.smart.kaifa I/----------------: onTouch
03-26 22:31:26.180 28182-28182/com.smart.kaifa I/----------------: onScroll
03-26 22:31:26.180 28182-28182/com.smart.kaifa I/----------------: onTouch
03-26 22:31:26.196 28182-28182/com.smart.kaifa I/----------------: onScroll
03-26 22:31:26.196 28182-28182/com.smart.kaifa I/----------------: onTouch
03-26 22:31:26.213 28182-28182/com.smart.kaifa I/----------------: onScroll
03-26 22:31:26.213 28182-28182/com.smart.kaifa I/----------------: onTouch
03-26 22:31:26.230 28182-28182/com.smart.kaifa I/----------------: onScroll
03-26 22:31:26.230 28182-28182/com.smart.kaifa I/----------------: onTouch
03-26 22:31:26.246 28182-28182/com.smart.kaifa I/----------------: onScroll
03-26 22:31:26.246 28182-28182/com.smart.kaifa I/----------------: onTouch
03-26 22:31:26.263 28182-28182/com.smart.kaifa I/----------------: onScroll
03-26 22:31:26.263 28182-28182/com.smart.kaifa I/----------------: onTouch
03-26 22:31:26.280 28182-28182/com.smart.kaifa I/----------------: onScroll
03-26 22:31:26.280 28182-28182/com.smart.kaifa I/----------------: onTouch
03-26 22:31:26.296 28182-28182/com.smart.kaifa I/----------------: onScroll
03-26 22:31:26.296 28182-28182/com.smart.kaifa I/----------------: onTouch
03-26 22:31:26.313 28182-28182/com.smart.kaifa I/----------------: onScroll
03-26 22:31:26.313 28182-28182/com.smart.kaifa I/----------------: onTouch
03-26 22:31:26.330 28182-28182/com.smart.kaifa I/----------------: onScroll
03-26 22:31:26.330 28182-28182/com.smart.kaifa I/----------------: onTouch
03-26 22:31:26.346 28182-28182/com.smart.kaifa I/----------------: onScroll
03-26 22:31:26.346 28182-28182/com.smart.kaifa I/----------------: onTouch
03-26 22:31:26.363 28182-28182/com.smart.kaifa I/----------------: onScroll
03-26 22:31:26.363 28182-28182/com.smart.kaifa I/----------------: onTouch
03-26 22:31:26.379 28182-28182/com.smart.kaifa I/----------------: onScroll
03-26 22:31:26.380 28182-28182/com.smart.kaifa I/----------------: onTouch
03-26 22:31:26.396 28182-28182/com.smart.kaifa I/----------------: onScroll
03-26 22:31:26.397 28182-28182/com.smart.kaifa I/----------------: onTouch
03-26 22:31:26.413 28182-28182/com.smart.kaifa I/----------------: onScroll
03-26 22:31:26.413 28182-28182/com.smart.kaifa I/----------------: onTouch
03-26 22:31:26.430 28182-28182/com.smart.kaifa I/----------------: onScroll
03-26 22:31:26.430 28182-28182/com.smart.kaifa I/----------------: onTouch
03-26 22:31:26.447 28182-28182/com.smart.kaifa I/----------------: onScroll
03-26 22:31:26.447 28182-28182/com.smart.kaifa I/----------------: onTouch
03-26 22:31:26.463 28182-28182/com.smart.kaifa I/----------------: onScroll
03-26 22:31:26.463 28182-28182/com.smart.kaifa I/----------------: onTouch
03-26 22:31:26.480 28182-28182/com.smart.kaifa I/----------------: onScroll
03-26 22:31:26.480 28182-28182/com.smart.kaifa I/----------------: onTouch
03-26 22:31:26.497 28182-28182/com.smart.kaifa I/----------------: onScroll
03-26 22:31:26.497 28182-28182/com.smart.kaifa I/----------------: onTouch
03-26 22:31:26.513 28182-28182/com.smart.kaifa I/----------------: onScroll
03-26 22:31:26.513 28182-28182/com.smart.kaifa I/----------------: onTouch
03-26 22:31:26.530 28182-28182/com.smart.kaifa I/----------------: onScroll
03-26 22:31:26.530 28182-28182/com.smart.kaifa I/----------------: onTouch
03-26 22:31:26.547 28182-28182/com.smart.kaifa I/----------------: onScroll
03-26 22:31:26.547 28182-28182/com.smart.kaifa I/----------------: onTouch
03-26 22:31:26.563 28182-28182/com.smart.kaifa I/----------------: onScroll
03-26 22:31:26.564 28182-28182/com.smart.kaifa I/----------------: onTouch
03-26 22:31:26.580 28182-28182/com.smart.kaifa I/----------------: onScroll
03-26 22:31:26.580 28182-28182/com.smart.kaifa I/----------------: onTouch
03-26 22:31:26.597 28182-28182/com.smart.kaifa I/----------------: onScroll
```

一次飞动
```
03-26 22:30:17.226 28182-28182/com.smart.kaifa I/----------------: onDown
03-26 22:30:17.227 28182-28182/com.smart.kaifa I/----------------: onDown
03-26 22:30:17.227 28182-28182/com.smart.kaifa I/----------------: onTouch
03-26 22:30:17.240 28182-28182/com.smart.kaifa I/----------------: onTouch
03-26 22:30:17.266 28182-28182/com.smart.kaifa I/----------------: onScroll
03-26 22:30:17.266 28182-28182/com.smart.kaifa I/----------------: onTouch
03-26 22:30:17.283 28182-28182/com.smart.kaifa I/----------------: onScroll
03-26 22:30:17.283 28182-28182/com.smart.kaifa I/----------------: onTouch
03-26 22:30:17.300 28182-28182/com.smart.kaifa I/----------------: onScroll
03-26 22:30:17.300 28182-28182/com.smart.kaifa I/----------------: onTouch
03-26 22:30:17.316 28182-28182/com.smart.kaifa I/----------------: onScroll
03-26 22:30:17.316 28182-28182/com.smart.kaifa I/----------------: onTouch
03-26 22:30:17.334 28182-28182/com.smart.kaifa I/----------------: onScroll
03-26 22:30:17.334 28182-28182/com.smart.kaifa I/----------------: onTouch
03-26 22:30:17.344 28182-28182/com.smart.kaifa I/----------------: onScroll
03-26 22:30:17.344 28182-28182/com.smart.kaifa I/----------------: onTouch
03-26 22:30:17.348 28182-28182/com.smart.kaifa I/----------------: onFling
03-26 22:30:17.348 28182-28182/com.smart.kaifa I/----------------: onTouch
```

## Scroller
弹性滑动对象：用于实现view的弹性滑动，当我们使用scrollTo进行滑动的时候，是瞬间完成的，没有过度。体验不好。这个需要View和`computerScroll`一起

```
public class TestTouchView extends FrameLayout {


    private Scroller scroller;

    public TestTouchView(Context context) {
        super(context);
        init();
    }

    public TestTouchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {

        scroller = new Scroller(getContext());

        smoothScrollTo(-1000, 100);
    }

    private void smoothScrollTo(int x, int y) {
        int x1 = getScrollX();
        int dx = x - x1;
        Log.i("--------", "smoothScrollTo");
        scroller.startScroll(0, 0, dx, 0, 10000);
        invalidate();
    }

    @Override
    public void computeScroll() {
        if (scroller.computeScrollOffset()) {
            Log.i("computeScroll CurrX:", "" + scroller.getCurrX());
            scrollTo(scroller.getCurrX(), scroller.getCurrY());
            postInvalidate();
        }

    }
}
```

记住 scorller针对的对象是TestTouchView内部的东西，和padding类似 ，如果TestTouchView内部由两个控件，那么两个控件都会移动的，还有一点，方向是反方向的，传入`scroller.startScroll(0, 0, -1000, 0, 10000)`是往右侧移动，可以理解为手机屏幕移动了-1000，但是内容没有移动。

##View的滑动
上面简单介绍了View的一些基础知识和概念,现在开始介绍一个很重要的内容：View的滑动，在Android设备上，滑动几乎是应用的标配，不管是下拉刷新还是侧滑
这里View的滑动可以归纳为三种方式：
* 第一种是通过View本身提供的scrollTo和scrollBy方法来实现滑动。
* 第二种是通过动画给View施加平移效果来实现滑动。
* 第三种是通过改变View的LayoutParams是的View重新布局从而实现滑动

## 第一种：使用View本身提供的scrollTo和scrollBy实现滑动

这里可以看一下他的源码：他是通过改变scrollx和scrolly的值来改变的，而scrollx和scrolly是可以view的属性，可以通过get和set方法获取和设置的
```
// 到某一个具体的位置去，然后将上一次滑动的数值保存
public void scrollTo(int x, int y) {
    if (mScrollX != x || mScrollY != y) {
        int oldX = mScrollX;
        int oldY = mScrollY;
        mScrollX = x;
        mScrollY = y;
        invalidateParentCaches();
        onScrollChanged(mScrollX, mScrollY, oldX, oldY);
        if (!awakenScrollBars()) {
            postInvalidateOnAnimation();
        }
    }
}
// 一次偏移多少的偏移量，scrollBy其实是调用scrollTo的
public void scrollBy(int x, int y) {
     scrollTo(mScrollX + x, mScrollY + y);
 }
```
这里要说一下，在滑动过程中，mScrollx始终等于左View边缘和view内容左边缘在水平方向上面的距离的,而mScrollY始终等于View上边缘与内容边缘的竖直方向的距离，mscrollx和mscrolly只能改变 **View内容的位置** 而不能改变View在布局中的位置。并且mScrolly和mScrollX的单位为像素，并且当View左边缘在View内容左边缘的右边的时候，mScrollX为正值，反之为负值。当View上边缘在View内容上边缘下方的时候，mScrollX为正值，反之为负值。换句话说，如果从左向右移动，那么mScrollx为负值，反之为正值。如果从上往下滑动，那么mScrollY为负值，反之为正值。
来一张图示意一下。


![Alt text](图像1522504775.png "示意图")





实心的是view内容，空心的是View的边框，开始的时候实心内容是在View边框中，有一部分是露在外边的，这个时候使用scrollTo移动100px，移动的是View中的内容，边框位置不变，可以看到View的内容就完全显示在View中间了。但是从人的角度来看，View的位置是不动的，所以是View像左移动了100px，就是-100px。和人的感觉是相反的。

* 没有滚动之前的效果


![Alt text](device-2018-03-31-224723.png "效果")

*  一次scrollTo操作，改变的是内容
```

public class MainActivity extends AppCompatActivity {

    private int left;
    private int right;
    private int bottom;
    private int top;
    private int width;
    private int height;
    private float x;
    private float y;
    private float translationX;
    private float scrollX;
    private float translationY;
    private float scrollY;
    private View viewById;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewById = findViewById(R.id.test);
    }
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        // TODO Auto-generated method stub
        if (hasFocus) {
            left = viewById.getLeft();
            right = viewById.getRight();
            bottom = viewById.getBottom();
            top = viewById.getTop();
            width = viewById.getWidth();
            height = viewById.getHeight();
            x = viewById.getX();
            y = viewById.getY();
            translationX = viewById.getTranslationX();
            translationY = viewById.getTranslationY();
            scrollX = viewById.getScrollX();
            scrollY = viewById.getScrollY();
            Log.i("第一次： left：", left + "");
            Log.i("第一次： right：", right + "");
            Log.i("第一次： bottom：", bottom + "");
            Log.i("第一次： top：", top + "");
            Log.i("第一次： width：", width + "");
            Log.i("第一次： height：", height + "");
            Log.i("第一次： x：", x + "");
            Log.i("第一次： y：", y + "");
            Log.i("第一次： translationX：", translationX + "");
            Log.i("第一次： translationY：", translationY + "");
            Log.i("第一次： scrollX：", scrollX + "");
            Log.i("第一次： scrollY：", scrollY + "");
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            viewById.scrollTo(-100, -100);
                            left = viewById.getLeft();
                            right = viewById.getRight();
                            bottom = viewById.getBottom();
                            top = viewById.getTop();
                            width = viewById.getWidth();
                            height = viewById.getHeight();
                            x = viewById.getX();
                            y = viewById.getY();
                            translationX = viewById.getTranslationX();
                            translationY = viewById.getTranslationY();
                            scrollX = viewById.getScrollX();
                            scrollY = viewById.getScrollY();
                            Log.i("第二次： ********", "***************************************************************************");
                            Log.i("第二次： left：", left + "");
                            Log.i("第二次： right：", right + "");
                            Log.i("第二次： bottom：", bottom + "");
                            Log.i("第二次： top：", top + "");
                            Log.i("第二次： width：", width + "");
                            Log.i("第二次： height：", height + "");
                            Log.i("第二次： x：", x + "");
                            Log.i("第二次： y：", y + "");
                            Log.i("第二次： translationX：", translationX + "");
                            Log.i("第二次： translationY：", translationY + "");
                            Log.i("第二次： scrollX：", scrollX + "");
                            Log.i("第二次： scrollY：", scrollY + "");
                        }
                    });
                }
            }, 1000);
        }
        super.onWindowFocusChanged(hasFocus);
    }
}

```
改变的值
```
03-31 22:33:47.107 30190-30190/com.smart.kaifa I/第一次： left：: 0
03-31 22:33:47.107 30190-30190/com.smart.kaifa I/第一次： right：: 700
03-31 22:33:47.107 30190-30190/com.smart.kaifa I/第一次： bottom：: 700
03-31 22:33:47.107 30190-30190/com.smart.kaifa I/第一次： top：: 0
03-31 22:33:47.107 30190-30190/com.smart.kaifa I/第一次： width：: 700
03-31 22:33:47.107 30190-30190/com.smart.kaifa I/第一次： height：: 700
03-31 22:33:47.107 30190-30190/com.smart.kaifa I/第一次： x：: 0.0
03-31 22:33:47.107 30190-30190/com.smart.kaifa I/第一次： y：: 0.0
03-31 22:33:47.107 30190-30190/com.smart.kaifa I/第一次： translationX：: 0.0
03-31 22:33:47.107 30190-30190/com.smart.kaifa I/第一次： translationY：: 0.0
03-31 22:33:47.107 30190-30190/com.smart.kaifa I/第一次： scrollX：: 0.0
03-31 22:33:47.107 30190-30190/com.smart.kaifa I/第一次： scrollY：: 0.0
03-31 22:33:48.110 30190-30190/com.smart.kaifa I/第二次： ********: ***************************************************************************
03-31 22:33:48.110 30190-30190/com.smart.kaifa I/第二次： left：: 0
03-31 22:33:48.110 30190-30190/com.smart.kaifa I/第二次： right：: 700
03-31 22:33:48.110 30190-30190/com.smart.kaifa I/第二次： bottom：: 700
03-31 22:33:48.110 30190-30190/com.smart.kaifa I/第二次： top：: 0
03-31 22:33:48.111 30190-30190/com.smart.kaifa I/第二次： width：: 700
03-31 22:33:48.111 30190-30190/com.smart.kaifa I/第二次： height：: 700
03-31 22:33:48.111 30190-30190/com.smart.kaifa I/第二次： x：: 0.0
03-31 22:33:48.111 30190-30190/com.smart.kaifa I/第二次： y：: 0.0
03-31 22:33:48.111 30190-30190/com.smart.kaifa I/第二次： translationX：: 0.0
03-31 22:33:48.111 30190-30190/com.smart.kaifa I/第二次： translationY：: 0.0
03-31 22:33:48.111 30190-30190/com.smart.kaifa I/第二次： scrollX：: -100.0
03-31 22:33:48.112 30190-30190/com.smart.kaifa I/第二次： scrollY：: -100.0
```


![Alt text](device-2018-03-31-224548.png "效果")

* 如果直接修改scorllx和scrolly的属性,这个效果和scrollto的效果是一样的
```
public class MainActivity extends AppCompatActivity {

    private int left;
    private int right;
    private int bottom;
    private int top;
    private int width;
    private int height;
    private float x;
    private float y;
    private float translationX;
    private float scrollX;
    private float translationY;
    private float scrollY;
    private View viewById;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewById = findViewById(R.id.test);


    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        // TODO Auto-generated method stub
        if (hasFocus) {
            left = viewById.getLeft();
            right = viewById.getRight();
            bottom = viewById.getBottom();
            top = viewById.getTop();
            width = viewById.getWidth();
            height = viewById.getHeight();
            x = viewById.getX();
            y = viewById.getY();
            translationX = viewById.getTranslationX();
            translationY = viewById.getTranslationY();
            scrollX = viewById.getScrollX();
            scrollY = viewById.getScrollY();

            Log.i("第一次： left：", left + "");
            Log.i("第一次： right：", right + "");
            Log.i("第一次： bottom：", bottom + "");
            Log.i("第一次： top：", top + "");
            Log.i("第一次： width：", width + "");
            Log.i("第一次： height：", height + "");
            Log.i("第一次： x：", x + "");
            Log.i("第一次： y：", y + "");
            Log.i("第一次： translationX：", translationX + "");
            Log.i("第一次： translationY：", translationY + "");
            Log.i("第一次： scrollX：", scrollX + "");
            Log.i("第一次： scrollY：", scrollY + "");

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            viewById.setScrollY(-100);
                            viewById.setScrollX(-100);
                            left = viewById.getLeft();
                            right = viewById.getRight();
                            bottom = viewById.getBottom();
                            top = viewById.getTop();
                            width = viewById.getWidth();
                            height = viewById.getHeight();
                            x = viewById.getX();
                            y = viewById.getY();
                            translationX = viewById.getTranslationX();
                            translationY = viewById.getTranslationY();
                            scrollX = viewById.getScrollX();
                            scrollY = viewById.getScrollY();
                            Log.i("第二次： ********", "***************************************************************************");
                            Log.i("第二次： left：", left + "");
                            Log.i("第二次： right：", right + "");
                            Log.i("第二次： bottom：", bottom + "");
                            Log.i("第二次： top：", top + "");
                            Log.i("第二次： width：", width + "");
                            Log.i("第二次： height：", height + "");
                            Log.i("第二次： x：", x + "");
                            Log.i("第二次： y：", y + "");
                            Log.i("第二次： translationX：", translationX + "");
                            Log.i("第二次： translationY：", translationY + "");
                            Log.i("第二次： scrollX：", scrollX + "");
                            Log.i("第二次： scrollY：", scrollY + "");
                        }
                    });
                }
            }, 1000);
        }
        super.onWindowFocusChanged(hasFocus);
    }
}

```
```
03-31 22:37:05.765 31212-31212/com.smart.kaifa I/第一次： left：: 0
03-31 22:37:05.765 31212-31212/com.smart.kaifa I/第一次： right：: 700
03-31 22:37:05.765 31212-31212/com.smart.kaifa I/第一次： bottom：: 700
03-31 22:37:05.765 31212-31212/com.smart.kaifa I/第一次： top：: 0
03-31 22:37:05.765 31212-31212/com.smart.kaifa I/第一次： width：: 700
03-31 22:37:05.765 31212-31212/com.smart.kaifa I/第一次： height：: 700
03-31 22:37:05.766 31212-31212/com.smart.kaifa I/第一次： x：: 0.0
03-31 22:37:05.766 31212-31212/com.smart.kaifa I/第一次： y：: 0.0
03-31 22:37:05.766 31212-31212/com.smart.kaifa I/第一次： translationX：: 0.0
03-31 22:37:05.766 31212-31212/com.smart.kaifa I/第一次： translationY：: 0.0
03-31 22:37:05.766 31212-31212/com.smart.kaifa I/第一次： scrollX：: 0.0
03-31 22:37:05.766 31212-31212/com.smart.kaifa I/第一次： scrollY：: 0.0
03-31 22:37:06.768 31212-31212/com.smart.kaifa I/第二次： ********: ***************************************************************************
03-31 22:37:06.768 31212-31212/com.smart.kaifa I/第二次： left：: 0
03-31 22:37:06.768 31212-31212/com.smart.kaifa I/第二次： right：: 700
03-31 22:37:06.768 31212-31212/com.smart.kaifa I/第二次： bottom：: 700
03-31 22:37:06.768 31212-31212/com.smart.kaifa I/第二次： top：: 0
03-31 22:37:06.768 31212-31212/com.smart.kaifa I/第二次： width：: 700
03-31 22:37:06.768 31212-31212/com.smart.kaifa I/第二次： height：: 700
03-31 22:37:06.769 31212-31212/com.smart.kaifa I/第二次： x：: 0.0
03-31 22:37:06.769 31212-31212/com.smart.kaifa I/第二次： y：: 0.0
03-31 22:37:06.769 31212-31212/com.smart.kaifa I/第二次： translationX：: 0.0
03-31 22:37:06.769 31212-31212/com.smart.kaifa I/第二次： translationY：: 0.0
03-31 22:37:06.769 31212-31212/com.smart.kaifa I/第二次： scrollX：: -100.0
03-31 22:37:06.769 31212-31212/com.smart.kaifa I/第二次： scrollY：: -100.0
```

![Alt text](device-2018-03-31-224548.png "效果")


* 如果使用TranslationX平移100 是view的边框平移100
```
public class MainActivity extends AppCompatActivity {

    private int left;
    private int right;
    private int bottom;
    private int top;
    private int width;
    private int height;
    private float x;
    private float y;
    private float translationX;
    private float scrollX;
    private float translationY;
    private float scrollY;
    private View viewById;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewById = findViewById(R.id.test);


    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        // TODO Auto-generated method stub
        if (hasFocus) {
            left = viewById.getLeft();
            right = viewById.getRight();
            bottom = viewById.getBottom();
            top = viewById.getTop();
            width = viewById.getWidth();
            height = viewById.getHeight();
            x = viewById.getX();
            y = viewById.getY();
            translationX = viewById.getTranslationX();
            translationY = viewById.getTranslationY();
            scrollX = viewById.getScrollX();
            scrollY = viewById.getScrollY();

            Log.i("第一次： left：", left + "");
            Log.i("第一次： right：", right + "");
            Log.i("第一次： bottom：", bottom + "");
            Log.i("第一次： top：", top + "");
            Log.i("第一次： width：", width + "");
            Log.i("第一次： height：", height + "");
            Log.i("第一次： x：", x + "");
            Log.i("第一次： y：", y + "");
            Log.i("第一次： translationX：", translationX + "");
            Log.i("第一次： translationY：", translationY + "");
            Log.i("第一次： scrollX：", scrollX + "");
            Log.i("第一次： scrollY：", scrollY + "");


            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            viewById.setTranslationX(100);//重点
                            viewById.setTranslationY(100);//重点
                            left = viewById.getLeft();
                            right = viewById.getRight();
                            bottom = viewById.getBottom();
                            top = viewById.getTop();
                            width = viewById.getWidth();
                            height = viewById.getHeight();
                            x = viewById.getX();
                            y = viewById.getY();
                            translationX = viewById.getTranslationX();
                            translationY = viewById.getTranslationY();
                            scrollX = viewById.getScrollX();
                            scrollY = viewById.getScrollY();
                            Log.i("第二次： ********", "***************************************************************************");
                            Log.i("第二次： left：", left + "");
                            Log.i("第二次： right：", right + "");
                            Log.i("第二次： bottom：", bottom + "");
                            Log.i("第二次： top：", top + "");
                            Log.i("第二次： width：", width + "");
                            Log.i("第二次： height：", height + "");
                            Log.i("第二次： x：", x + "");
                            Log.i("第二次： y：", y + "");
                            Log.i("第二次： translationX：", translationX + "");
                            Log.i("第二次： translationY：", translationY + "");
                            Log.i("第二次： scrollX：", scrollX + "");
                            Log.i("第二次： scrollY：", scrollY + "");
                        }
                    });
                }
            }, 1000);
        }
        super.onWindowFocusChanged(hasFocus);
    }
}

```

```
03-31 22:39:21.987 31988-31988/com.smart.kaifa I/第一次： left：: 0
03-31 22:39:21.987 31988-31988/com.smart.kaifa I/第一次： right：: 700
03-31 22:39:21.987 31988-31988/com.smart.kaifa I/第一次： bottom：: 700
03-31 22:39:21.987 31988-31988/com.smart.kaifa I/第一次： top：: 0
03-31 22:39:21.987 31988-31988/com.smart.kaifa I/第一次： width：: 700
03-31 22:39:21.987 31988-31988/com.smart.kaifa I/第一次： height：: 700
03-31 22:39:21.987 31988-31988/com.smart.kaifa I/第一次： x：: 0.0
03-31 22:39:21.987 31988-31988/com.smart.kaifa I/第一次： y：: 0.0
03-31 22:39:21.987 31988-31988/com.smart.kaifa I/第一次： translationX：: 0.0
03-31 22:39:21.987 31988-31988/com.smart.kaifa I/第一次： translationY：: 0.0
03-31 22:39:21.987 31988-31988/com.smart.kaifa I/第一次： scrollX：: 0.0
03-31 22:39:21.987 31988-31988/com.smart.kaifa I/第一次： scrollY：: 0.0
03-31 22:39:22.990 31988-31988/com.smart.kaifa I/第二次： ********: ***************************************************************************
03-31 22:39:22.990 31988-31988/com.smart.kaifa I/第二次： left：: 0
03-31 22:39:22.990 31988-31988/com.smart.kaifa I/第二次： right：: 700
03-31 22:39:22.991 31988-31988/com.smart.kaifa I/第二次： bottom：: 700
03-31 22:39:22.991 31988-31988/com.smart.kaifa I/第二次： top：: 0
03-31 22:39:22.991 31988-31988/com.smart.kaifa I/第二次： width：: 700
03-31 22:39:22.991 31988-31988/com.smart.kaifa I/第二次： height：: 700
03-31 22:39:22.991 31988-31988/com.smart.kaifa I/第二次： x：: 100.0
03-31 22:39:22.992 31988-31988/com.smart.kaifa I/第二次： y：: 100.0
03-31 22:39:22.992 31988-31988/com.smart.kaifa I/第二次： translationX：: 100.0
03-31 22:39:22.992 31988-31988/com.smart.kaifa I/第二次： translationY：: 100.0
03-31 22:39:22.992 31988-31988/com.smart.kaifa I/第二次： scrollX：: 0.0
03-31 22:39:22.992 31988-31988/com.smart.kaifa I/第二次： scrollY：: 0.0
```

![Alt text](device-2018-03-31-224323.png "效果")




*  修改x和y的属性
```
public class MainActivity extends AppCompatActivity {

    private int left;
    private int right;
    private int bottom;
    private int top;
    private int width;
    private int height;
    private float x;
    private float y;
    private float translationX;
    private float scrollX;
    private float translationY;
    private float scrollY;
    private View viewById;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewById = findViewById(R.id.test);


    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        // TODO Auto-generated method stub
        if (hasFocus) {
            left = viewById.getLeft();
            right = viewById.getRight();
            bottom = viewById.getBottom();
            top = viewById.getTop();
            width = viewById.getWidth();
            height = viewById.getHeight();
            x = viewById.getX();
            y = viewById.getY();
            translationX = viewById.getTranslationX();
            translationY = viewById.getTranslationY();
            scrollX = viewById.getScrollX();
            scrollY = viewById.getScrollY();

            Log.i("第一次： left：", left + "");
            Log.i("第一次： right：", right + "");
            Log.i("第一次： bottom：", bottom + "");
            Log.i("第一次： top：", top + "");
            Log.i("第一次： width：", width + "");
            Log.i("第一次： height：", height + "");
            Log.i("第一次： x：", x + "");
            Log.i("第一次： y：", y + "");
            Log.i("第一次： translationX：", translationX + "");
            Log.i("第一次： translationY：", translationY + "");
            Log.i("第一次： scrollX：", scrollX + "");
            Log.i("第一次： scrollY：", scrollY + "");


            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            viewById.setX(100);
                            viewById.setY(100);
                            left = viewById.getLeft();
                            right = viewById.getRight();
                            bottom = viewById.getBottom();
                            top = viewById.getTop();
                            width = viewById.getWidth();
                            height = viewById.getHeight();
                            x = viewById.getX();
                            y = viewById.getY();
                            translationX = viewById.getTranslationX();
                            translationY = viewById.getTranslationY();
                            scrollX = viewById.getScrollX();
                            scrollY = viewById.getScrollY();
                            Log.i("第二次： ********", "***************************************************************************");
                            Log.i("第二次： left：", left + "");
                            Log.i("第二次： right：", right + "");
                            Log.i("第二次： bottom：", bottom + "");
                            Log.i("第二次： top：", top + "");
                            Log.i("第二次： width：", width + "");
                            Log.i("第二次： height：", height + "");
                            Log.i("第二次： x：", x + "");
                            Log.i("第二次： y：", y + "");
                            Log.i("第二次： translationX：", translationX + "");
                            Log.i("第二次： translationY：", translationY + "");
                            Log.i("第二次： scrollX：", scrollX + "");
                            Log.i("第二次： scrollY：", scrollY + "");
                        }
                    });
                }
            }, 1000);


        }
        super.onWindowFocusChanged(hasFocus);
    }


}

```

如果修改x和y的值，会导致translationX和translationY的值相应改变，
x=left+translationX
y=top+translationY
这个关系式是不变的，而left和top是控件的固有属性，在平移的过程之中不会发生变化，所以x和translationX会相对线性的改变
```
03-31 22:46:29.420 1993-1993/com.smart.kaifa I/第一次： left：: 0
03-31 22:46:29.420 1993-1993/com.smart.kaifa I/第一次： right：: 700
03-31 22:46:29.420 1993-1993/com.smart.kaifa I/第一次： bottom：: 700
03-31 22:46:29.420 1993-1993/com.smart.kaifa I/第一次： top：: 0
03-31 22:46:29.420 1993-1993/com.smart.kaifa I/第一次： width：: 700
03-31 22:46:29.420 1993-1993/com.smart.kaifa I/第一次： height：: 700
03-31 22:46:29.421 1993-1993/com.smart.kaifa I/第一次： x：: 0.0
03-31 22:46:29.421 1993-1993/com.smart.kaifa I/第一次： y：: 0.0
03-31 22:46:29.421 1993-1993/com.smart.kaifa I/第一次： translationX：: 0.0
03-31 22:46:29.421 1993-1993/com.smart.kaifa I/第一次： translationY：: 0.0
03-31 22:46:29.421 1993-1993/com.smart.kaifa I/第一次： scrollX：: 0.0
03-31 22:46:29.421 1993-1993/com.smart.kaifa I/第一次： scrollY：: 0.0
03-31 22:46:30.424 1993-1993/com.smart.kaifa I/第二次： ********: ***************************************************************************
03-31 22:46:30.424 1993-1993/com.smart.kaifa I/第二次： left：: 0
03-31 22:46:30.425 1993-1993/com.smart.kaifa I/第二次： right：: 700
03-31 22:46:30.425 1993-1993/com.smart.kaifa I/第二次： bottom：: 700
03-31 22:46:30.425 1993-1993/com.smart.kaifa I/第二次： top：: 0
03-31 22:46:30.425 1993-1993/com.smart.kaifa I/第二次： width：: 700
03-31 22:46:30.425 1993-1993/com.smart.kaifa I/第二次： height：: 700
03-31 22:46:30.425 1993-1993/com.smart.kaifa I/第二次： x：: 100.0
03-31 22:46:30.426 1993-1993/com.smart.kaifa I/第二次： y：: 100.0
03-31 22:46:30.426 1993-1993/com.smart.kaifa I/第二次： translationX：: 100.0
03-31 22:46:30.426 1993-1993/com.smart.kaifa I/第二次： translationY：: 100.0
03-31 22:46:30.426 1993-1993/com.smart.kaifa I/第二次： scrollX：: 0.0
03-31 22:46:30.426 1993-1993/com.smart.kaifa I/第二次： scrollY：: 0.0
```

![Alt text](device-2018-03-31-224323.png "效果")

## 第二种：属性动画，属性动画是改变View的属性值

如果是android3.0以下的版本可以使用nineoldandroids ，但是现在已经基本上不用兼容android2.3的版本了
上面已经有一个demo了

```

public class MainActivity extends AppCompatActivity {

    private int left;
    private int right;
    private int bottom;
    private int top;
    private int width;
    private int height;
    private float x;
    private float y;
    private float translationX;
    private float scrollX;
    private float translationY;
    private float scrollY;
    private View viewById;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewById = findViewById(R.id.test);


    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        // TODO Auto-generated method stub
        if(hasFocus){
            left = viewById.getLeft();
            right = viewById.getRight();
            bottom = viewById.getBottom();
            top = viewById.getTop();
            width = viewById.getWidth();
            height = viewById.getHeight();
            x = viewById.getX();
            y = viewById.getY();
            translationX = viewById.getTranslationX();
            translationY = viewById.getTranslationY();
            scrollX = viewById.getScrollX();
            scrollY = viewById.getScrollY();

            Log.i("第一次： left：", left + "");
            Log.i("第一次： right：", right + "");
            Log.i("第一次： bottom：", bottom + "");
            Log.i("第一次： top：", top + "");
            Log.i("第一次： width：", width + "");
            Log.i("第一次： height：", height + "");
            Log.i("第一次： x：", x + "");
            Log.i("第一次： y：", y + "");
            Log.i("第一次： translationX：", translationX + "");
            Log.i("第一次： translationY：", translationY + "");
            Log.i("第一次： scrollX：", scrollX + "");
            Log.i("第一次： scrollY：", scrollY + "");


            ObjectAnimator animator = ObjectAnimator.ofFloat(viewById, "translationX", 200);
            animator.setDuration(1000);
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    left = viewById.getLeft();
                    right = viewById.getRight();
                    bottom = viewById.getBottom();
                    top = viewById.getTop();
                    width = viewById.getWidth();
                    height = viewById.getHeight();
                    x = viewById.getX();
                    y = viewById.getY();
                    translationX = viewById.getTranslationX();
                    translationY = viewById.getTranslationY();
                    scrollX = viewById.getScrollX();
                    scrollY = viewById.getScrollY();
                    Log.i("第二次： ********", "***************************************************************************");
                    Log.i("第二次： left：", left + "");
                    Log.i("第二次： right：", right + "");
                    Log.i("第二次： bottom：", bottom + "");
                    Log.i("第二次： top：", top + "");
                    Log.i("第二次： width：", width + "");
                    Log.i("第二次： height：", height + "");
                    Log.i("第二次： x：", x + "");
                    Log.i("第二次： y：", y + "");
                    Log.i("第二次： translationX：", translationX + "");
                    Log.i("第二次： translationY：", translationY + "");
                    Log.i("第二次： scrollX：", scrollX + "");
                    Log.i("第二次： scrollY：", scrollY + "");
                }
            });
            animator.start();
        }
        super.onWindowFocusChanged(hasFocus);
    }
}

```

结果
```
03-25 22:13:16.307 18794-18794/com.smart.kaifa I/第一次： left：: 70
03-25 22:13:16.307 18794-18794/com.smart.kaifa I/第一次： right：: 140
03-25 22:13:16.307 18794-18794/com.smart.kaifa I/第一次： bottom：: 140
03-25 22:13:16.307 18794-18794/com.smart.kaifa I/第一次： top：: 70
03-25 22:13:16.307 18794-18794/com.smart.kaifa I/第一次： width：: 70
03-25 22:13:16.307 18794-18794/com.smart.kaifa I/第一次： height：: 70
03-25 22:13:16.307 18794-18794/com.smart.kaifa I/第一次： x：: 70.0
03-25 22:13:16.307 18794-18794/com.smart.kaifa I/第一次： y：: 70.0
03-25 22:13:16.307 18794-18794/com.smart.kaifa I/第一次： translationX：: 0.0
03-25 22:13:16.307 18794-18794/com.smart.kaifa I/第一次： translationY：: 0.0
03-25 22:13:16.307 18794-18794/com.smart.kaifa I/第一次： scrollX：: 0.0
03-25 22:13:16.307 18794-18794/com.smart.kaifa I/第一次： scrollY：: 0.0
03-25 22:13:17.314 18794-18794/com.smart.kaifa I/第二次： ********: ***************************************************************************
03-25 22:13:17.314 18794-18794/com.smart.kaifa I/第二次： left：: 70
03-25 22:13:17.314 18794-18794/com.smart.kaifa I/第二次： right：: 140
03-25 22:13:17.315 18794-18794/com.smart.kaifa I/第二次： bottom：: 140
03-25 22:13:17.315 18794-18794/com.smart.kaifa I/第二次： top：: 70
03-25 22:13:17.315 18794-18794/com.smart.kaifa I/第二次： width：: 70
03-25 22:13:17.315 18794-18794/com.smart.kaifa I/第二次： height：: 70
03-25 22:13:17.315 18794-18794/com.smart.kaifa I/第二次： x：: 270.0
03-25 22:13:17.316 18794-18794/com.smart.kaifa I/第二次： y：: 70.0
03-25 22:13:17.316 18794-18794/com.smart.kaifa I/第二次： translationX：: 200.0
03-25 22:13:17.316 18794-18794/com.smart.kaifa I/第二次： translationY：: 0.0
03-25 22:13:17.316 18794-18794/com.smart.kaifa I/第二次： scrollX：: 0.0
03-25 22:13:17.317 18794-18794/com.smart.kaifa I/第二次： scrollY：: 0.0
```

属性动画和普通动画的区别在于普通动画移动之后由于没有改变属性值，只是视觉效果变化，这个时候点击事件任然保留在原地，而属性动画点击事件和视觉效果一致
## 使用布局参数，就是修改LayoutParams
```

public class MainActivity extends AppCompatActivity {

    private int left;
    private int right;
    private int bottom;
    private int top;
    private int width;
    private int height;
    private float x;
    private float y;
    private float translationX;
    private float scrollX;
    private float translationY;
    private float scrollY;
    private View viewById;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewById = findViewById(R.id.test);


    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        // TODO Auto-generated method stub
        if (hasFocus) {
            left = viewById.getLeft();
            right = viewById.getRight();
            bottom = viewById.getBottom();
            top = viewById.getTop();
            width = viewById.getWidth();
            height = viewById.getHeight();
            x = viewById.getX();
            y = viewById.getY();
            translationX = viewById.getTranslationX();
            translationY = viewById.getTranslationY();
            scrollX = viewById.getScrollX();
            scrollY = viewById.getScrollY();

            Log.i("第一次： left：", left + "");
            Log.i("第一次： right：", right + "");
            Log.i("第一次： bottom：", bottom + "");
            Log.i("第一次： top：", top + "");
            Log.i("第一次： width：", width + "");
            Log.i("第一次： height：", height + "");
            Log.i("第一次： x：", x + "");
            Log.i("第一次： y：", y + "");
            Log.i("第一次： translationX：", translationX + "");
            Log.i("第一次： translationY：", translationY + "");
            Log.i("第一次： scrollX：", scrollX + "");
            Log.i("第一次： scrollY：", scrollY + "");

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) viewById.getLayoutParams();
                            layoutParams.width += 500;
                            layoutParams.leftMargin += 100;
                            viewById.setLayoutParams(layoutParams);
                            left = viewById.getLeft();
                            right = viewById.getRight();
                            bottom = viewById.getBottom();
                            top = viewById.getTop();
                            width = viewById.getWidth();
                            height = viewById.getHeight();
                            x = viewById.getX();
                            y = viewById.getY();
                            translationX = viewById.getTranslationX();
                            translationY = viewById.getTranslationY();
                            scrollX = viewById.getScrollX();
                            scrollY = viewById.getScrollY();
                            Log.i("第二次： ********", "***************************************************************************");
                            Log.i("第二次： left：", left + "");
                            Log.i("第二次： right：", right + "");
                            Log.i("第二次： bottom：", bottom + "");
                            Log.i("第二次： top：", top + "");
                            Log.i("第二次： width：", width + "");
                            Log.i("第二次： height：", height + "");
                            Log.i("第二次： x：", x + "");
                            Log.i("第二次： y：", y + "");
                            Log.i("第二次： translationX：", translationX + "");
                            Log.i("第二次： translationY：", translationY + "");
                            Log.i("第二次： scrollX：", scrollX + "");
                            Log.i("第二次： scrollY：", scrollY + "");
                        }
                    });
                }
            }, 1000);
        }
        super.onWindowFocusChanged(hasFocus);
    }


}

```

```
03-31 23:13:47.225 10457-10457/com.smart.kaifa I/第一次： left：: 0
03-31 23:13:47.225 10457-10457/com.smart.kaifa I/第一次： right：: 700
03-31 23:13:47.225 10457-10457/com.smart.kaifa I/第一次： bottom：: 700
03-31 23:13:47.225 10457-10457/com.smart.kaifa I/第一次： top：: 0
03-31 23:13:47.225 10457-10457/com.smart.kaifa I/第一次： width：: 700
03-31 23:13:47.225 10457-10457/com.smart.kaifa I/第一次： height：: 700
03-31 23:13:47.225 10457-10457/com.smart.kaifa I/第一次： x：: 0.0
03-31 23:13:47.225 10457-10457/com.smart.kaifa I/第一次： y：: 0.0
03-31 23:13:47.225 10457-10457/com.smart.kaifa I/第一次： translationX：: 0.0
03-31 23:13:47.225 10457-10457/com.smart.kaifa I/第一次： translationY：: 0.0
03-31 23:13:47.226 10457-10457/com.smart.kaifa I/第一次： scrollX：: 0.0
03-31 23:13:47.226 10457-10457/com.smart.kaifa I/第一次： scrollY：: 0.0
03-31 23:13:48.228 10457-10457/com.smart.kaifa I/第二次： ********: ***************************************************************************
03-31 23:13:48.228 10457-10457/com.smart.kaifa I/第二次： left：: 0
03-31 23:13:48.228 10457-10457/com.smart.kaifa I/第二次： right：: 700
03-31 23:13:48.228 10457-10457/com.smart.kaifa I/第二次： bottom：: 700
03-31 23:13:48.228 10457-10457/com.smart.kaifa I/第二次： top：: 0
03-31 23:13:48.228 10457-10457/com.smart.kaifa I/第二次： width：: 700
03-31 23:13:48.229 10457-10457/com.smart.kaifa I/第二次： height：: 700
03-31 23:13:48.229 10457-10457/com.smart.kaifa I/第二次： x：: 0.0
03-31 23:13:48.229 10457-10457/com.smart.kaifa I/第二次： y：: 0.0
03-31 23:13:48.229 10457-10457/com.smart.kaifa I/第二次： translationX：: 0.0
03-31 23:13:48.229 10457-10457/com.smart.kaifa I/第二次： translationY：: 0.0
03-31 23:13:48.229 10457-10457/com.smart.kaifa I/第二次： scrollX：: 0.0
03-31 23:13:48.229 10457-10457/com.smart.kaifa I/第二次： scrollY：: 0.0
```
![Alt text](device-2018-03-31-232202.png "效果")
可以看到改变了宽和margin，layoutParams后面会探究的

## 不同滑动方式的对比

* scrollTo和scrollBy ：主要适合对View内容的滑动
* 动画：操作简单，主要适合没有交互的View和实现复杂动画效果
* 改变布局参数：操作稍微复杂，使用与有交互的View

# 弹性滑动
知道了View的滑动，我们还要知道如何实现View的弹性滑动，实现弹性滑动的方式有很多，比如通过handler#postDelayed...
## Scroller ：之前说过

```
public class TestTouchView extends FrameLayout {


    private Scroller scroller;

    public TestTouchView(Context context) {
        super(context);
        init();
    }

    public TestTouchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {

        scroller = new Scroller(getContext());

        smoothScrollTo(-1000, 100);
    }

    private void smoothScrollTo(int x, int y) {
        int x1 = getScrollX();
        int dx = x - x1;
        Log.i("--------", "smoothScrollTo");
        scroller.startScroll(0, 0, dx, 0, 10000);
        invalidate();
    }

    @Override
    public void computeScroll() {
        if (scroller.computeScrollOffset()) {
            Log.i("computeScroll CurrX:", "" + scroller.getCurrX());
            scrollTo(scroller.getCurrX(), scroller.getCurrY());
            postInvalidate();
        }

    }
}
```

注意一下这里滑动是View的内容的滑动，不包括View本身，其实也是通过修改scrollX和scrollY来实现的
注意一下 这里说一下原理：
首先创建了一个Scroller，然后使用`  scroller.startScroll(0, 0, dx, 0, 10000)`之后`  invalidate()`这个时候会调用View的`onDraw`方法。然后`onDraw`方法有调用了View的`computeScroll`,就是我们重写的这个方法，这样就不断重新绘制，直到完成

## 通过动画

```
   ObjectAnimator.ofFloat(viewById,"translationX",0,100).setDuration(1000).start();
```
这里也可以通过值动画（属性动画的一种）来玩成
```

public class MainActivity extends AppCompatActivity {

    private View viewById;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewById = findViewById(R.id.test);
        final int startx=0;
        final int endx=100;
        ValueAnimator animator = ValueAnimator.ofInt(0, 1).setDuration(1000);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float animatedFraction = animation.getAnimatedFraction();
                viewById.scrollTo((int) (startx+endx*animatedFraction),0);
            }
        });
        animator.start();
    }
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
    }


}
```
这个和Scroller一样
## 也可以使用延时策略
如果使用handler，这个不一定流畅，因为调度handler是会有延时的

# View的事件分发机制
上面介绍了View的基础知识和View的滑动，这里介绍一下View的事件分发机制
## 点击事件的传递规则
这里分析的对象是MotionEvent。事件传递的过程其实就是MotionEvent的传递

三个传递的方法


* dispatchTouchEvent(MotionEvent event)
用来进行事件分发，如果事件能够传递到当前View，则这个方法一定会被调用，返回值结果手当前View的onTouchEvent和下级的dispatchTouchEvent方法影响，表示是否消耗当前事件
* onInterceptTouchEvent(MotionEvent event)
在这个方法中是内部调用的方法，用来判断是否拦截某个事件，如果当前View拦截了某个事件，那么在同一个事件序列当中，此方法不会再被调用，返回结果表示是否拦截了当前的事件
* onTouchEvent(MotionEvent event)

在dispatchTouchEvent中调用，用来处理点击事件，返回结果表示是否消耗了当前事件，如果不消耗，则同一事件序列中，当前View无法再次接收到事件


这里用伪代码来描述一下

```
public boolean dispatchTouchEvent(MotionEvent ev){
  boolean consume=false;
  if(onInterceptTouchEvent(ev)){
    consume=onTouchEvent(ev);

  }else{
    consume=child.dispatchTouchTvent(ev);
  }
  return consume;
}
```
上面的伪代码已经将三者的关系表现的很清楚了。通过上面的伪代码，我们可以大致了解点击事件传递规则：对于一个根ViewGroup来说，点击事件产生后，首先会传递给它，这个时候会调用他的`dispatchTouchEvent`方法，如果他的`onInterceptTouchEvent`返回true，表示要拦截这个事件，这个时候事件就会交给这个控件`onTouchEvent`，如果这个ViewGroup的`onInterceptTouchEvent`方法返回false，表示不拦截，这个时候当前的事件就会传递给子控件，接着子控件的`dispatchTouchEvent`就会被调用。如此反复
当一个View需要处理事件的时候，如果他设置了`OnTouchListener`，那么`OnTouchListener`中的`OnTouch`方法就会被调用。这个时候事件的处理要看`OnTouch`的返回值，如果返回false,则当前的`OnTouch`方法就会被调用。如果返回true,则当前的`OnTouch`方法就不会被调用。因此，给View设置OnTouchListener，其优先级比OnTouchRvent要搞。在Ontouch方法中如果设置有OnClickListener,那么他的OnClick方法就会被调用，OnClickListener是优先级最低的。

当一个点击事件产生之后，他的传递过程遵循如下顺序：Activity->Window->View，即事件总是先传递给Activity，Activity传递给Window，最后Window在传递给顶级的View，然后在按照事件分发的及机制分发事件。考虑到一种情况，如果一个View的OnTouchEvent返回false，那么他的父容器的OnTouchEvent就会被调用，以此类推，如果所有的元素都不处理这个事件，那么这个事件将会最终传递给Activity处理。即Activity的OnTouchEvent 会被调用

一个事件传递的demo
布局
```
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context="com.smart.kaifa.MainActivity">
    <com.smart.kaifa.TestTouchView
        android:id="@+id/test"
        android:layout_width="400dp"
        android:layout_height="400dp"
        android:background="#ff00ff">
        <com.smart.kaifa.TestTouchView1
            android:layout_width="300dp"
            android:layout_height="300dp"
            android:background="#00ffff">
            <com.smart.kaifa.TestView
                android:layout_width="50dp"
                android:layout_height="50dp"
                android:background="#00ff00"
                android:text="dkdk" />
            <com.smart.kaifa.Test1View
                android:layout_width="50dp"
                android:layout_height="50dp"
                android:layout_marginLeft="60dp"
                android:background="#0000ff"
                android:text="dkdk" />
        </com.smart.kaifa.TestTouchView1>
    </com.smart.kaifa.TestTouchView>
</FrameLayout>

```
Activity
```
public class MainActivity extends AppCompatActivity {

    private View viewById;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewById = findViewById(R.id.test);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {


    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        Log.i("哇哈哈   MainActivity", "dispatchTouchEvent========");
        return super.dispatchTouchEvent(ev);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.i("哇哈哈   MainActivity", "onTouchEvent========");
        return super.onTouchEvent(event);
    }
}

```


TestTouchView
```
public class TestTouchView extends FrameLayout {

    public TestTouchView(Context context) {
        super(context);

    }

    public TestTouchView(Context context, AttributeSet attrs) {
        super(context, attrs);

    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        Log.i("哇哈哈   TestTouchView", "dispatchTouchEvent========");
        return super.dispatchTouchEvent(ev);
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        Log.i("哇哈哈   TestTouchView", "onInterceptTouchEvent========");
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.i("哇哈哈   TestTouchView", "onTouchEvent========");
        return super.onTouchEvent(event);
    }
}

```

TestView
```
public class TestView extends View {

    public TestView(Context context) {
        super(context);
    }


    public TestView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public TestView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        Log.i("哇哈哈   TestView","dispatchTouchEvent========");
        return super.dispatchTouchEvent(ev);
    }



    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.i("哇哈哈   TestView","onTouchEvent========");
        return super.onTouchEvent(event);
    }
}
```

activity和View 是没有`onInterceptTouchEvent`


这里有一些注意点：
1. 同一个事件是从手指按下到离开，包含down 和若干个move最后是up
2. 正常情况下，一个事件序列只能被一个View拦截且消耗，这一条原因可以参考下一条，因为一旦一个元素拦截了这个事件，那么同一个事件序列内的所有事件都会直接交给他处理。因此同一个事件序列的事件不能分别有两个View处理，但是通过特殊的手段可以做到，比如讲一个本该自己处理的事件传递给其他控件，在自己的`OnTouchEvent`方法中调用其他控件的`OnTouchEvent`方法
3. 某个View一旦决定拦截，那么这一个事件序列都只能由他来处理（如果事件序列能够传递的话），并且它的`onInterceptTouchEvent`不会再被调用，这个也很好理解。就是说如果当一个View决定拦截一个事件之后，那么系统会把同一个事件徐磊内的其他方法都直接交给他处理。因此就不用再调用这个View的`OnInterceptTouchEvent`方法。
这里：在TestTouchView中拦截`onInterceptTouchEvent`返回true,但是没有在onTouchEvent中处理他,这个时候TestTouchView的子控件就收不到事件，并且由于事件没有处理，就会传递给activity，之后就不会再调用拦截的方法，直接交给处理的人，
```
com.smart.kaifa I/哇哈哈   MainActivity: dispatchTouchEvent========
com.smart.kaifa I/哇哈哈   TestTouchView: dispatchTouchEvent========
    onInterceptTouchEvent========
    onTouchEvent========
com.smart.kaifa I/哇哈哈   MainActivity: onTouchEvent========
com.smart.kaifa I/哇哈哈   MainActivity: dispatchTouchEvent========
    onTouchEvent========
com.smart.kaifa I/哇哈哈   MainActivity: dispatchTouchEvent========
    onTouchEvent========
com.smart.kaifa I/哇哈哈   MainActivity: dispatchTouchEvent========
    onTouchEvent========
com.smart.kaifa I/哇哈哈   MainActivity: dispatchTouchEvent========
    onTouchEvent========
com.smart.kaifa I/哇哈哈   MainActivity: dispatchTouchEvent========
    onTouchEvent========
com.smart.kaifa I/哇哈哈   MainActivity: dispatchTouchEvent========
    onTouchEvent========
com.smart.kaifa I/哇哈哈   MainActivity: dispatchTouchEvent========
    onTouchEvent========
com.smart.kaifa I/哇哈哈   MainActivity: dispatchTouchEvent========
    onTouchEvent========
com.smart.kaifa I/哇哈哈   MainActivity: dispatchTouchEvent========
    onTouchEvent========
com.smart.kaifa I/哇哈哈   MainActivity: dispatchTouchEvent========
    onTouchEvent========
com.smart.kaifa I/哇哈哈   MainActivity: dispatchTouchEvent========
    onTouchEvent========
```

这里：在TestTouchView1中拦截`onInterceptTouchEvent`返回true，并且在TestTouchView中处理事件，TestTouchView的`onTouchEvent`方法返回true,这个时候事件会分发，虽然是在TestTouchView1中拦截的，但是由于不是在TestTouchView1中处理，系统在第一次down的时候知道了事件是TestTouchView处理的，就不会再分发给TestTouchView1了
```
com.smart.kaifa I/哇哈哈   MainActivity: dispatchTouchEvent========
com.smart.kaifa I/哇哈哈   TestTouchView: dispatchTouchEvent========
    onInterceptTouchEvent========
com.smart.kaifa I/哇哈哈   TestTouchView1: dispatchTouchEvent========
    onInterceptTouchEvent========
    onTouchEvent========
com.smart.kaifa I/哇哈哈   TestTouchView: onTouchEvent========
com.smart.kaifa I/哇哈哈   MainActivity: dispatchTouchEvent========
com.smart.kaifa I/哇哈哈   TestTouchView: dispatchTouchEvent========
    onTouchEvent========
com.smart.kaifa I/哇哈哈   MainActivity: dispatchTouchEvent========
com.smart.kaifa I/哇哈哈   TestTouchView: dispatchTouchEvent========
com.smart.kaifa I/哇哈哈   TestTouchView: onTouchEvent========
com.smart.kaifa I/哇哈哈   MainActivity: dispatchTouchEvent========
com.smart.kaifa I/哇哈哈   TestTouchView: dispatchTouchEvent========
    onTouchEvent========
```
4. 如果View不消耗除Action_Down以外的其他事件，那么这个点击事件就会消失。此时父元素的OnTouchEvent并不会被调用，并且当前View可以持续受到后续的事件，最终这些消失的点击事件会传递给Activity处理
默认都没有拦截事件，这个时候如果在View上面移动，可以看到只有down事件是全部传递了，move和up都没有经过子View，这个直接传递给了activity的onTouchEvent来处理
```
com.smart.kaifa I/哇哈哈   MainActivity: dispatchTouchEvent========
com.smart.kaifa I/哇哈哈   TestTouchView: dispatchTouchEvent========
    onInterceptTouchEvent========
com.smart.kaifa I/哇哈哈   TestTouchView1: dispatchTouchEvent========
    onInterceptTouchEvent========
com.smart.kaifa I/哇哈哈   Test1View: dispatchTouchEvent========
    onTouchEvent========
com.smart.kaifa I/哇哈哈   TestTouchView1: onTouchEvent========
com.smart.kaifa I/哇哈哈   TestTouchView: onTouchEvent========
com.smart.kaifa I/哇哈哈   MainActivity: onTouchEvent========
com.smart.kaifa I/哇哈哈   MainActivity: dispatchTouchEvent========
com.smart.kaifa I/哇哈哈   MainActivity: onTouchEvent========
com.smart.kaifa I/哇哈哈   MainActivity: dispatchTouchEvent========
    onTouchEvent========
com.smart.kaifa I/哇哈哈   MainActivity: dispatchTouchEvent========
    onTouchEvent========
```
5. 某个View一旦开始处理事件，如果它不消耗Action_Down事件，（`ontouchEvent`返回了false），那么，同一个事件序列中的其他事件都不会再交给他来处理，并且事件将重新交给他的父元素去处理，即父元素的`OnTouchEvent`方法会被调用。意思就是事件一旦交给一个View处理，那么他就必须消耗掉，否则同一事件序列中剩下的事件就不会再交给他来处理了。
6. ViewGroup默认不拦截任何事件。Android源码中ViewGroup的`onInterceptTouch-Event`方法默认返回false
7. View没有`onInterceptTouch-Event`方法，一旦有点击事件传递给他，那么他的OnTouchEvent方法就会被调用
8. View的`onTouchEvent`默认都会消耗事件，返回true，除非他是不可点击的（clickable和longClickable同时为false）。View的longClickable属性默认为false，clickable属性要分情况，比如Botton的clickable属性默认为true，而textview的clickable属性默认为false

Test1View 是一个button
```
com.smart.kaifa I/哇哈哈   MainActivity: dispatchTouchEvent========
com.smart.kaifa I/哇哈哈   TestTouchView: dispatchTouchEvent========
    onInterceptTouchEvent========
com.smart.kaifa I/哇哈哈   TestTouchView1: dispatchTouchEvent========
    onInterceptTouchEvent========
com.smart.kaifa I/哇哈哈   Test1View: dispatchTouchEvent========
    onTouchEvent========
com.smart.kaifa I/哇哈哈   MainActivity: dispatchTouchEvent========
com.smart.kaifa I/哇哈哈   TestTouchView: dispatchTouchEvent========
    onInterceptTouchEvent========
com.smart.kaifa I/哇哈哈   TestTouchView1: dispatchTouchEvent========
    onInterceptTouchEvent========
com.smart.kaifa I/哇哈哈   Test1View: dispatchTouchEvent========
    onTouchEvent========
com.smart.kaifa I/哇哈哈   MainActivity: dispatchTouchEvent========
com.smart.kaifa I/哇哈哈   TestTouchView: dispatchTouchEvent========
    onInterceptTouchEvent========
com.smart.kaifa I/哇哈哈   TestTouchView1: dispatchTouchEvent========
    onInterceptTouchEvent========
com.smart.kaifa I/哇哈哈   Test1View: dispatchTouchEvent========
    onTouchEvent========
com.smart.kaifa I/哇哈哈   MainActivity: dispatchTouchEvent========
com.smart.kaifa I/哇哈哈   TestTouchView: dispatchTouchEvent========
    onInterceptTouchEvent========
com.smart.kaifa I/哇哈哈   TestTouchView1: dispatchTouchEvent========
    onInterceptTouchEvent========
com.smart.kaifa I/哇哈哈   Test1View: dispatchTouchEvent========
    onTouchEvent========
```
TestView 是一个textview
```
com.smart.kaifa I/哇哈哈   MainActivity: dispatchTouchEvent========
com.smart.kaifa I/哇哈哈   TestTouchView: dispatchTouchEvent========
com.smart.kaifa I/哇哈哈   TestTouchView: onInterceptTouchEvent========
com.smart.kaifa I/哇哈哈   TestTouchView1: dispatchTouchEvent========
    onInterceptTouchEvent========
com.smart.kaifa I/哇哈哈   TestView: dispatchTouchEvent========
    onTouchEvent========
com.smart.kaifa I/哇哈哈   TestTouchView1: onTouchEvent========
com.smart.kaifa I/哇哈哈   TestTouchView: onTouchEvent========
com.smart.kaifa I/哇哈哈   MainActivity: onTouchEvent========
com.smart.kaifa I/哇哈哈   MainActivity: dispatchTouchEvent========
    onTouchEvent========
com.smart.kaifa I/哇哈哈   MainActivity: dispatchTouchEvent========
    onTouchEvent========
com.smart.kaifa I/哇哈哈   MainActivity: dispatchTouchEvent========
    onTouchEvent========
com.smart.kaifa I/哇哈哈   MainActivity: dispatchTouchEvent========
    onTouchEvent========
com.smart.kaifa I/哇哈哈   MainActivity: dispatchTouchEvent========
    onTouchEvent========
com.smart.kaifa I/哇哈哈   MainActivity: dispatchTouchEvent========
    onTouchEvent========

```

可以看到 bottom是有clickable的，所以默认onTouchEvent返回true，之后所有的事件都交给Test1View处理，而TestView是textview，不是clickable，，所以默认onTouchEvent返回false，之后所有的事件都交给activitty处理处理
9. View的enable属性不影响onTouchEvent的返回值。哪怕一个View是disable状态，只要他的clickable或者longClickable有一个为true，那么他的onTouchEvent就返回true
10. onclick会发生的前提是当前View是可以点击的，并且他收到了down和up事件
11. 事件传递过程是由外向内的，即从父控件传递给子控件。通过`requestDisallowInterceptTouchEvent`方法可以在子元素中干预父元素的时间分发过程，但是ActionDown除外

```
public class TestTouchView1 extends FrameLayout {
    public TestTouchView1(@NonNull Context context) {
        super(context);
    }
    public TestTouchView1(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }
    public TestTouchView1(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        Log.i("哇哈哈   TestTouchView1", "dispatchTouchEvent========");
        return true;
    }
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        Log.i("哇哈哈   TestTouchView1", "onInterceptTouchEvent========");
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:  //这里不能拦截了，不然事件就不会传递给子控件。但是在子控件中
                return false;
            case MotionEvent.ACTION_MOVE:   //表示父类需要
                return true;
            case MotionEvent.ACTION_UP:
                return true;
            default:
                break;
        }
        return false;    //如果设置拦截，除了down,其他都是父类处理
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.i("哇哈哈   TestTouchView1", "onTouchEvent========");
        return super.onTouchEvent(event);
    }
}

```

```
public class Test1View extends Button {

    public Test1View(Context context) {
        super(context);

    }


    public Test1View(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public Test1View(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
//        getParent().requestDisallowInterceptTouchEvent(false);

        Log.i("哇哈哈   Test1View","dispatchTouchEvent========");
        return super.dispatchTouchEvent(ev);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.i("哇哈哈   Test1View","onTouchEvent========");
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                getParent().requestDisallowInterceptTouchEvent(true);
                Log.d("TAG", "You down button");
                break;
            case MotionEvent.ACTION_UP:
                Log.d("TAG", "You up button");
                break;
            case MotionEvent.ACTION_MOVE:
                Log.d("TAG", "You move button");
        }
        return true;
//        return super.onTouchEvent(event);
    }
}

```

可以看到虽然父控件拦截了up和move事件，但是子控件使用了`  getParent().requestDisallowInterceptTouchEvent`告诉父控件不要拦截，父控件就不会拦截
```
com.smart.kaifa I/哇哈哈   MainActivity: dispatchTouchEvent========
com.smart.kaifa I/哇哈哈   TestTouchView: dispatchTouchEvent========
com.smart.kaifa I/哇哈哈   TestTouchView: onInterceptTouchEvent========
com.smart.kaifa I/哇哈哈   TestTouchView1: dispatchTouchEvent========
com.smart.kaifa I/哇哈哈   MainActivity: dispatchTouchEvent========
com.smart.kaifa I/哇哈哈   TestTouchView: dispatchTouchEvent========
    onInterceptTouchEvent========
com.smart.kaifa I/哇哈哈   TestTouchView1: dispatchTouchEvent========
com.smart.kaifa I/哇哈哈   MainActivity: dispatchTouchEvent========
com.smart.kaifa I/哇哈哈   TestTouchView: dispatchTouchEvent========
    onInterceptTouchEvent========
com.smart.kaifa I/哇哈哈   TestTouchView1: dispatchTouchEvent========
com.smart.kaifa I/哇哈哈   MainActivity: dispatchTouchEvent========
com.smart.kaifa I/哇哈哈   TestTouchView: dispatchTouchEvent========
com.smart.kaifa I/哇哈哈   TestTouchView: onInterceptTouchEvent========
com.smart.kaifa I/哇哈哈   TestTouchView1: dispatchTouchEvent========
```



## 事件分发源码分析：有四个组件

* activity

dispatchTouchEvent

```
public boolean dispatchTouchEvent(MotionEvent ev) {

    //如果是down 调用空方法onUserInteraction()
    // 然后将event传递给window
    // 1
     if (ev.getAction() == MotionEvent.ACTION_DOWN) {
         onUserInteraction();
     }
     //getWindow() 得到phoneWindow
     //2
     if (getWindow().superDispatchTouchEvent(ev)) {
         return true;
     }
     return onTouchEvent(ev);
 }

public void onUserInteraction() {
 }

```

onTouchEvent
* window


```
//3
@Override
  public boolean superDispatchTouchEvent(MotionEvent event) {
      //这里将事件传递给 最顶层的ViewGroup
      return mDecor.superDispatchTouchEvent(event);
  }

```

* DecorView

```
//4  这个是最顶层的ViewGroup
public boolean superDispatchTouchEvent(MotionEvent event) {
    return super.dispatchTouchEvent(event);
}

```

* viewgroup
//5
```
      @Override
         public boolean dispatchTouchEvent(MotionEvent ev) {
                 //InputFilter 的工作也分为两个步骤，首先由InputEventConsistencyVerifier 对象（InputEventConsistencyVerifier.java）对输入事件的完整性做一个检查，检查事件的ACTION_DOWN 和 ACTION_UP 是否一一配对
             if (mInputEventConsistencyVerifier != null) {
                 mInputEventConsistencyVerifier.onTouchEvent(ev, 1);
             }
              // 判断是否要分发
             // If the event targets the accessibility focused view and this is it, start
             // normal event dispatch. Maybe a descendant is what will handle the click.
             if (ev.isTargetAccessibilityFocus() && isAccessibilityFocusedViewOrHost()) {
                 ev.setTargetAccessibilityFocus(false);
             }

             boolean handled = false;
             //如果事件是一个安全的事件，就进入
             if (onFilterTouchEventForSecurity(ev)) {
                 final int action = ev.getAction();
                 final int actionMasked = action & MotionEvent.ACTION_MASK;

                 // Handle an initial down.
                 // 如果是down事件
                 if (actionMasked == MotionEvent.ACTION_DOWN) {
                     // Throw away all previous state when starting a new touch gesture.
                     // The framework may have dropped the up or cancel event for the previous gesture
                     // due to an app switch, ANR, or some other state change.

                     // Cancels and clears all touch targets.  清空之前对控件的操作
                     cancelAndClearTouchTargets(ev);
                     // 重置触摸状态
                     resetTouchState();
                 }

                 // Check for interception.
                 final boolean intercepted;
                 //如果是down事件  mFirstTouchTarget:如果事件由ViewGroup的子控件处理成功， mFirstTouchTarget 会被赋值，并指向子元素
                 //如果是down事件，就一定会走这个if判断
                 //如果不是down事件，并且不交给子控件处理，intercepted = true
                 if (actionMasked == MotionEvent.ACTION_DOWN
                         || mFirstTouchTarget != null) {
                           //mGroupFlags :这个标记为是子控件调用requestDisallowInterceptTouchEvent的时候设定，用于子控件给父控件提示要不要拦截事件
                     final boolean disallowIntercept = (mGroupFlags & FLAG_DISALLOW_INTERCEPT) != 0;

                     if (!disallowIntercept) {
                       //如果没有子控件的提示，调用父控件的拦截事件的方法
                       //onInterceptTouchEvent 不是每次都调用的
                         intercepted = onInterceptTouchEvent(ev);
                         ev.setAction(action); // restore action in case it was changed
                     } else {
                         intercepted = false;
                     }
                 } else {
                     // There are no touch targets and this action is not an initial down
                     // so this view group continues to intercept touches.
                     intercepted = true;
                 }

                 // If intercepted, start normal event dispatch. Also if there is already
                 // a view that is handling the gesture, do normal event dispatch.
                 if (intercepted || mFirstTouchTarget != null) {
                     ev.setTargetAccessibilityFocus(false);
                 }

                 // Check for cancelation.
                 // 判断是否取消此次事件
                 final boolean canceled = resetCancelNextUpFlag(this)
                         || actionMasked == MotionEvent.ACTION_CANCEL;

                 // Update list of touch targets for pointer down, if needed.
                 final boolean split = (mGroupFlags & FLAG_SPLIT_MOTION_EVENTS) != 0;
                 TouchTarget newTouchTarget = null;
                 boolean alreadyDispatchedToNewTouchTarget = false;
                 // 如果事件不取消也不拦截
                 if (!canceled && !intercepted) {

                     // If the event is targeting accessiiblity focus we give it to the
                     // view that has accessibility focus and if it does not handle it
                     // we clear the flag and dispatch the event to all children as usual.
                     // We are looking up the accessibility focused host to avoid keeping
                     // state since these events are very rare.
                     View childWithAccessibilityFocus = ev.isTargetAccessibilityFocus()
                             ? findChildWithAccessibilityFocus() : null;

                     if (actionMasked == MotionEvent.ACTION_DOWN
                             || (split && actionMasked == MotionEvent.ACTION_POINTER_DOWN)
                             || actionMasked == MotionEvent.ACTION_HOVER_MOVE) {
                         final int actionIndex = ev.getActionIndex(); // always 0 for down
                         final int idBitsToAssign = split ? 1 << ev.getPointerId(actionIndex)
                                 : TouchTarget.ALL_POINTER_IDS;

                         // Clean up earlier touch targets for this pointer id in case they
                         // have become out of sync.
                         removePointersFromTouchTargets(idBitsToAssign);
                        // 获取子控件的数量
                         final int childrenCount = mChildrenCount;
                         if (newTouchTarget == null && childrenCount != 0) {
                           // 获取event的x和y
                             final float x = ev.getX(actionIndex);
                             final float y = ev.getY(actionIndex);
                             // Find a child that can receive the event.
                             // Scan children from front to back.
                             final ArrayList<View> preorderedList = buildTouchDispatchChildList();
                             final boolean customOrder = preorderedList == null
                                     && isChildrenDrawingOrderEnabled();
                             final View[] children = mChildren;
                             for (int i = childrenCount - 1; i >= 0; i--) {
                               // 遍历子控件，
                                 final int childIndex = getAndVerifyPreorderedIndex(
                                         childrenCount, i, customOrder);
                                 final View child = getAndVerifyPreorderedView(
                                         preorderedList, children, childIndex);

                                 // If there is a view that has accessibility focus we want it
                                 // to get the event first and if not handled we will perform a
                                 // normal dispatch. We may do a double iteration but this is
                                 // safer given the timeframe.
                                 if (childWithAccessibilityFocus != null) {
                                     if (childWithAccessibilityFocus != child) {
                                         continue;
                                     }
                                     childWithAccessibilityFocus = null;
                                     i = childrenCount - 1;
                                 }
                                //判断子控件是否在进行动画播放，判断点击事件是否在一个子控件的范围里面isTransformedTouchPointInView
                                 if (!canViewReceivePointerEvents(child)
                                         || !isTransformedTouchPointInView(x, y, child, null)) {
                                     ev.setTargetAccessibilityFocus(false);
                                     continue;
                                 }
                                // 满足不在播放动画并且在控件内
                                 newTouchTarget = getTouchTarget(child);
                                 if (newTouchTarget != null) {
                                     // Child is already receiving touch within its bounds.
                                     // Give it the new pointer in addition to the ones it is handling.
                                     newTouchTarget.pointerIdBits |= idBitsToAssign;
                                     break;
                                 }

                                 resetCancelNextUpFlag(child);
                                 // 如果子控件不为空并且可以接受：
                                 //如果不能接受就交个父控件
                                 if (dispatchTransformedTouchEvent(ev, false, child, idBitsToAssign)) {
                                     // Child wants to receive touch within its bounds.
                                     mLastTouchDownTime = ev.getDownTime();
                                     if (preorderedList != null) {
                                         // childIndex points into presorted list, find original index
                                         for (int j = 0; j < childrenCount; j++) {
                                             if (children[childIndex] == mChildren[j]) {
                                                 mLastTouchDownIndex = j;
                                                 break;
                                             }
                                         }
                                     } else {
                                         mLastTouchDownIndex = childIndex;
                                     }
                                     mLastTouchDownX = ev.getX();
                                     mLastTouchDownY = ev.getY();
                                     // 处理TouchTarget。这个可以理解为一个链式结构，用于处理事件的拦截
                                     //这里 mFirstTouchTarget就赋值，不为空
                                     newTouchTarget = addTouchTarget(child, idBitsToAssign);
                                     alreadyDispatchedToNewTouchTarget = true;
                                     break;
                                 }

                                 // The accessibility focus didn't handle the event, so clear
                                 // the flag and do a normal dispatch to all children.
                                 ev.setTargetAccessibilityFocus(false);
                             }
                             if (preorderedList != null) preorderedList.clear();
                         }

                         if (newTouchTarget == null && mFirstTouchTarget != null) {
                             // Did not find a child to receive the event.
                             // Assign the pointer to the least recently added target.
                             newTouchTarget = mFirstTouchTarget;
                             while (newTouchTarget.next != null) {
                                 newTouchTarget = newTouchTarget.next;
                             }
                             newTouchTarget.pointerIdBits |= idBitsToAssign;
                         }
                     }
                 }

                 // Dispatch to touch targets.
                 if (mFirstTouchTarget == null) {
                     // No touch targets so treat this as an ordinary view.
                     handled = dispatchTransformedTouchEvent(ev, canceled, null,
                             TouchTarget.ALL_POINTER_IDS);
                 } else {
                     // Dispatch to touch targets, excluding the new touch target if we already
                     // dispatched to it.  Cancel touch targets if necessary.
                     TouchTarget predecessor = null;
                     TouchTarget target = mFirstTouchTarget;
                     while (target != null) {
                         final TouchTarget next = target.next;
                         if (alreadyDispatchedToNewTouchTarget && target == newTouchTarget) {
                             handled = true;
                         } else {
                             final boolean cancelChild = resetCancelNextUpFlag(target.child)
                                     || intercepted;
                             if (dispatchTransformedTouchEvent(ev, cancelChild,
                                     target.child, target.pointerIdBits)) {
                                 handled = true;
                             }
                             if (cancelChild) {
                                 if (predecessor == null) {
                                     mFirstTouchTarget = next;
                                 } else {
                                     predecessor.next = next;
                                 }
                                 target.recycle();
                                 target = next;
                                 continue;
                             }
                         }
                         predecessor = target;
                         target = next;
                     }
                 }

                 // Update list of touch targets for pointer up or cancel, if needed.
                 if (canceled
                         || actionMasked == MotionEvent.ACTION_UP
                         || actionMasked == MotionEvent.ACTION_HOVER_MOVE) {
                     resetTouchState();
                 } else if (split && actionMasked == MotionEvent.ACTION_POINTER_UP) {
                     final int actionIndex = ev.getActionIndex();
                     final int idBitsToRemove = 1 << ev.getPointerId(actionIndex);
                     removePointersFromTouchTargets(idBitsToRemove);
                 }
             }

             if (!handled && mInputEventConsistencyVerifier != null) {
                 mInputEventConsistencyVerifier.onUnhandledEvent(ev, 1);
             }
             return handled;
         }



         @Override
             public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {

                 if (disallowIntercept == ((mGroupFlags & FLAG_DISALLOW_INTERCEPT) != 0)) {
                     // We're already in this state, assume our ancestors are too
                     return;
                 }

                 if (disallowIntercept) {
                     mGroupFlags |= FLAG_DISALLOW_INTERCEPT;
                 } else {
                     mGroupFlags &= ~FLAG_DISALLOW_INTERCEPT;
                 }

                 // Pass it up to our parent
                 if (mParent != null) {
                     mParent.requestDisallowInterceptTouchEvent(disallowIntercept);
                 }
             }


            // 判断事件分发给谁
            6
             private boolean dispatchTransformedTouchEvent(MotionEvent event, boolean cancel,
                     View child, int desiredPointerIdBits) {
                 final boolean handled;

                 // Canceling motions is a special case.  We don't need to perform any transformations
                 // or filtering.  The important part is the action, not the contents.
                 final int oldAction = event.getAction();
                 if (cancel || oldAction == MotionEvent.ACTION_CANCEL) {
                     event.setAction(MotionEvent.ACTION_CANCEL);
                     if (child == null) {
                         handled = super.dispatchTouchEvent(event);
                     } else {
                         handled = child.dispatchTouchEvent(event);
                     }
                     event.setAction(oldAction);
                     return handled;
                 }

                 // Calculate the number of pointers to deliver.
                 final int oldPointerIdBits = event.getPointerIdBits();
                 final int newPointerIdBits = oldPointerIdBits & desiredPointerIdBits;

                 // If for some reason we ended up in an inconsistent state where it looks like we
                 // might produce a motion event with no pointers in it, then drop the event.
                 if (newPointerIdBits == 0) {
                     return false;
                 }

                 // If the number of pointers is the same and we don't need to perform any fancy
                 // irreversible transformations, then we can reuse the motion event for this
                 // dispatch as long as we are careful to revert any changes we make.
                 // Otherwise we need to make a copy.
                 final MotionEvent transformedEvent;
                 if (newPointerIdBits == oldPointerIdBits) {
                     if (child == null || child.hasIdentityMatrix()) {
                         if (child == null) {
                             handled = super.dispatchTouchEvent(event);
                         } else {
                             final float offsetX = mScrollX - child.mLeft;
                             final float offsetY = mScrollY - child.mTop;
                             event.offsetLocation(offsetX, offsetY);

                             handled = child.dispatchTouchEvent(event);

                             event.offsetLocation(-offsetX, -offsetY);
                         }
                         return handled;
                     }
                     transformedEvent = MotionEvent.obtain(event);
                 } else {
                     transformedEvent = event.split(newPointerIdBits);
                 }

                 // Perform any necessary transformations and dispatch.
                 if (child == null) {
                     handled = super.dispatchTouchEvent(transformedEvent);
                 } else {
                     final float offsetX = mScrollX - child.mLeft;
                     final float offsetY = mScrollY - child.mTop;
                     transformedEvent.offsetLocation(offsetX, offsetY);
                     if (! child.hasIdentityMatrix()) {
                         transformedEvent.transform(child.getInverseMatrix());
                     }

                     handled = child.dispatchTouchEvent(transformedEvent);
                 }

                 // Done.
                 transformedEvent.recycle();
                 return handled;
             }

```

* view

```
// 7
public boolean dispatchTouchEvent(MotionEvent event) {
       // If the event should be handled by accessibility focus first.
       if (event.isTargetAccessibilityFocus()) {
           // We don't have focus or no virtual descendant has it, do not handle the event.
           if (!isAccessibilityFocusedViewOrHost()) {
               return false;
           }
           // We have focus and got the event, then use normal event dispatch.
           event.setTargetAccessibilityFocus(false);
       }

       boolean result = false;

       if (mInputEventConsistencyVerifier != null) {
           mInputEventConsistencyVerifier.onTouchEvent(event, 0);
       }

       final int actionMasked = event.getActionMasked();
       if (actionMasked == MotionEvent.ACTION_DOWN) {
           // Defensive cleanup for new gesture
           stopNestedScroll();
       }
      // 对事件进行过滤
       if (onFilterTouchEventForSecurity(event)) {
           if ((mViewFlags & ENABLED_MASK) == ENABLED && handleScrollBarDragging(event)) {
               result = true;
           }
           //noinspection SimplifiableIfStatement
           ListenerInfo li = mListenerInfo;
           //判断有没有TouchListener，这个在onTouchEvent之前处理
           if (li != null && li.mOnTouchListener != null
                   && (mViewFlags & ENABLED_MASK) == ENABLED
                   && li.mOnTouchListener.onTouch(this, event)) {
               result = true;
           }
           // 如果没有拦截，调用View 的onTouchEvent
           if (!result && onTouchEvent(event)) {
               result = true;
           }
       }

       if (!result && mInputEventConsistencyVerifier != null) {
           mInputEventConsistencyVerifier.onUnhandledEvent(event, 0);
       }

       // Clean up after nested scrolls if this is the end of a gesture;
       // also cancel it if we tried an ACTION_DOWN but we didn't want the rest
       // of the gesture.
       if (actionMasked == MotionEvent.ACTION_UP ||
               actionMasked == MotionEvent.ACTION_CANCEL ||
               (actionMasked == MotionEvent.ACTION_DOWN && !result)) {
           stopNestedScroll();
       }

       return result;
   }


   //8  如果没有onTouchListener 就调用onTouchEvent
   public boolean onTouchEvent(MotionEvent event) {
       final float x = event.getX();
       final float y = event.getY();
       final int viewFlags = mViewFlags;
       final int action = event.getAction();

       final boolean clickable = ((viewFlags & CLICKABLE) == CLICKABLE
               || (viewFlags & LONG_CLICKABLE) == LONG_CLICKABLE)
               || (viewFlags & CONTEXT_CLICKABLE) == CONTEXT_CLICKABLE;

       if ((viewFlags & ENABLED_MASK) == DISABLED) {
           if (action == MotionEvent.ACTION_UP && (mPrivateFlags & PFLAG_PRESSED) != 0) {
               setPressed(false);
           }
           mPrivateFlags3 &= ~PFLAG3_FINGER_DOWN;
           // 在不可以用状态下的View也会消耗事件
           // A disabled view that is clickable still consumes the touch
           // events, it just doesn't respond to them.
           return clickable;
       }
       // 如果View设置代理，就会调用这个方法
       if (mTouchDelegate != null) {
           if (mTouchDelegate.onTouchEvent(event)) {
               return true;
           }
       }
      //对点击事件的处理
      // 知道可以点击或者长按就会消耗事件
      // 那么就会返回true，表示处理了这个事件
       if (clickable || (viewFlags & TOOLTIP) == TOOLTIP) {
           switch (action) {
               case MotionEvent.ACTION_UP:
                   mPrivateFlags3 &= ~PFLAG3_FINGER_DOWN;
                   if ((viewFlags & TOOLTIP) == TOOLTIP) {
                       handleTooltipUp();
                   }
                   if (!clickable) {
                       removeTapCallback();
                       removeLongPressCallback();
                       mInContextButtonPress = false;
                       mHasPerformedLongPress = false;
                       mIgnoreNextUpEvent = false;
                       break;
                   }
                   boolean prepressed = (mPrivateFlags & PFLAG_PREPRESSED) != 0;
                   if ((mPrivateFlags & PFLAG_PRESSED) != 0 || prepressed) {
                       // take focus if we don't have it already and we should in
                       // touch mode.
                       boolean focusTaken = false;
                       if (isFocusable() && isFocusableInTouchMode() && !isFocused()) {
                           focusTaken = requestFocus();
                       }

                       if (prepressed) {
                           // The button is being released before we actually
                           // showed it as pressed.  Make it show the pressed
                           // state now (before scheduling the click) to ensure
                           // the user sees it.
                           setPressed(true, x, y);
                       }

                       if (!mHasPerformedLongPress && !mIgnoreNextUpEvent) {
                           // This is a tap, so remove the longpress check
                           removeLongPressCallback();

                           // Only perform take click actions if we were in the pressed state
                           if (!focusTaken) {
                               // Use a Runnable and post this rather than calling
                               // performClick directly. This lets other visual state
                               // of the view update before click actions start.
                               // 如果设置了onClickListener，就会在这里处理  performClick()处理点击事件，就是说在up的时候处理的
                               if (mPerformClick == null) {
                                   mPerformClick = new PerformClick();
                               }
                               if (!post(mPerformClick)) {
                                   performClick();
                               }
                           }
                       }

                       if (mUnsetPressedState == null) {
                           mUnsetPressedState = new UnsetPressedState();
                       }

                       if (prepressed) {
                           postDelayed(mUnsetPressedState,
                                   ViewConfiguration.getPressedStateDuration());
                       } else if (!post(mUnsetPressedState)) {
                           // If the post failed, unpress right now
                           mUnsetPressedState.run();
                       }

                       removeTapCallback();
                   }
                   mIgnoreNextUpEvent = false;
                   break;

               case MotionEvent.ACTION_DOWN:
                   if (event.getSource() == InputDevice.SOURCE_TOUCHSCREEN) {
                       mPrivateFlags3 |= PFLAG3_FINGER_DOWN;
                   }
                   mHasPerformedLongPress = false;

                   if (!clickable) {
                       checkForLongClick(0, x, y);
                       break;
                   }

                   if (performButtonActionOnTouchDown(event)) {
                       break;
                   }

                   // Walk up the hierarchy to determine if we're inside a scrolling container.
                   boolean isInScrollingContainer = isInScrollingContainer();

                   // For views inside a scrolling container, delay the pressed feedback for
                   // a short period in case this is a scroll.
                   if (isInScrollingContainer) {
                       mPrivateFlags |= PFLAG_PREPRESSED;
                       if (mPendingCheckForTap == null) {
                           mPendingCheckForTap = new CheckForTap();
                       }
                       mPendingCheckForTap.x = event.getX();
                       mPendingCheckForTap.y = event.getY();
                       postDelayed(mPendingCheckForTap, ViewConfiguration.getTapTimeout());
                   } else {
                       // Not inside a scrolling container, so show the feedback right away
                       setPressed(true, x, y);
                       checkForLongClick(0, x, y);
                   }
                   break;

               case MotionEvent.ACTION_CANCEL:
                   if (clickable) {
                       setPressed(false);
                   }
                   removeTapCallback();
                   removeLongPressCallback();
                   mInContextButtonPress = false;
                   mHasPerformedLongPress = false;
                   mIgnoreNextUpEvent = false;
                   mPrivateFlags3 &= ~PFLAG3_FINGER_DOWN;
                   break;

               case MotionEvent.ACTION_MOVE:
                   if (clickable) {
                       drawableHotspotChanged(x, y);
                   }

                   // Be lenient about moving outside of buttons
                   if (!pointInView(x, y, mTouchSlop)) {
                       // Outside button
                       // Remove any future long press/tap checks
                       removeTapCallback();
                       removeLongPressCallback();
                       if ((mPrivateFlags & PFLAG_PRESSED) != 0) {
                           setPressed(false);
                       }
                       mPrivateFlags3 &= ~PFLAG3_FINGER_DOWN;
                   }
                   break;
           }

           return true;
       }

       return false;
   }

// 9 这个方法如果设置了点击事件，可以看到会设置标记
   public void setOnClickListener(@Nullable OnClickListener l) {
       if (!isClickable()) {
           setClickable(true);
       }
       getListenerInfo().mOnClickListener = l;
   }

```


```flow

st=>start: start
op1=>operation: activity dispatchTouchEvent
op2=>operation: window.superDispatchTouchEvent
op3=>operation: mDecor.superDispatchTouchEvent
op4=>operation: viewgroup.dispatchTouchEvent
op5=>operation: viewgroup.requestDisallowInterceptTouchEvent
op6=>operation: viewgroup.onInterceptTouchEvent
op7=>operation: viewgroup.dispatchTransformedTouchEvent
op8=>operation: view.dispatchTouchEvent
op10=>operation: view.onTouchEvent
op12=>operation: viewgroup.onTouchEvent
op13=>operation: activity.onTouchEvent
cond1=>condition: 子控件是否设置了requestDisallowInterceptTouchEvent
cond2=>condition: viewgroup是否拦截事件onInterceptTouchEvent
cond3=>condition: 处理事件：是自己处理还是交给子控件处理
cond4=>condition: 子控件是否设置了TouchListener
cond5=>condition: 子控件是否设置了ClickListener
e=>end
st->op1->op2->op3->op4->op5->cond1
cond1(yes)->op6->cond2
cond1(no)->cond2
cond2(yes)->op12->op13
cond2(no)->op7->op
cond3(yes)->op12->cond5
cond3(no)->op8->cond4
cond4(yes)->e
cond4(no)->op10->cond5
cond5(yes)->e
cond5(no)->op12->op13->e

```

# View的滑动冲突
这个是开发的时候经常遇到的问题
## 常见的滑动冲突场景
* 场景1 : 外部滑动方向和内部滑动方向不一致
* 场景2 ：外部华东方向和内部滑动方向一致
* 场景3 ：上面两种嵌套的情况

这里先说场景1 ： 这个主要是Viewpager和Fragment配合的时候所组成的页面滑动，主流应用几乎都会使用这个效果。在fragment中有listview。Viewpager内部帮助我们处理了这种冲突，但是如果是scrollView就会遇到冲突。
再说一下场景2 ： 这种情况就稍微复杂一点，因为是内部和外部同时都会监听同一个方向的滚动，所以系统不知道要将事件分发给谁。一般是内部滑动，外部跟着滑动
最后说一下场景3 ：场景1和场景2两种情况嵌套，因此这个比较复杂。

## 事件冲突的处理规则
不管冲突多么复杂，总归是有规则的。
第一种情况可以根据三角形来计算，如果是水平方向的dx大于垂直方向的dy，就用dx，反之一样
第二种情况要依据具体的情况来解答
第三种情况更加复杂，后面举个例子

## 滑动冲突的解决方式

1. 外部拦截法
所谓的外部拦截法法，就是点击事件都会先经过父控件处理，然后进行分发，如果不需要，就拦截。需要重写`onInterceptTouchEvent`

```
@Override
   public boolean onInterceptTouchEvent(MotionEvent ev) {
       boolean intercepted = false;
       boolean need = false;// 父控件是否需要点击事件
       int x = (int) ev.getX();
       int y = (int) ev.getY();
       switch (ev.getAction()) {
           case MotionEvent.ACTION_DOWN:
               intercepted=false;
         break;
           case MotionEvent.ACTION_MOVE:
               if(need){
                   intercepted=true;
               }else{
                   intercepted=false;
               }
                   break;
           case MotionEvent.ACTION_UP:
               intercepted=false;
               break;
           default:
               break;
       }
       return intercepted;
   }

```
这个就是一个典型的外部拦截法的处理，针对不同的冲突，只需要适当修改拦截的条件就可以。首先down必须返回false，不然所有事件就会被父控件拦截。move事件更具具体情况进行拦截。up一定要返回false，因为单纯up事件是没有意义的。
考虑到一种情况：如果父控件在up时返回true，那么导致子控件无法接受到up事件，就无法触发子控件的onclick事件了。但是父控件这个比较特殊，一旦他开始拦截任何一个事件，那么后续的事件都会交给他来处理。而up作为最后一个事件也必定可以传递给父控件，即便父容器onInterceptTouchEvent在up是返回false

2. 内部拦截发
这种方式是父控件不拦截事件，所有事件全部传递给子控件，子控件来处理。这种复方石和android事件分发机制不一致。需要配合requestDisallowInterceptTouchEvent来实现

```
@Override
public boolean dispatchTouchEvent(MotionEvent ev) {

    int x = (int) ev.getX();
    int y = (int) ev.getY();

    switch (ev.getAction()) {
        case MotionEvent.ACTION_DOWN:
         getParent().requestDisallowInterceptTouchEvent(true);
            break;
        case MotionEvent.ACTION_MOVE:
          if(父控件需要){
              getParent().requestDisallowInterceptTouchEvent(false);
          }
            break;
        case MotionEvent.ACTION_UP:

            break;
        default:
            break;
    }
    return super.dispatchTouchEvent(ev);
}

```

3. 实战
实战：父控件拦截法
下面通过一个demo来运用这两种方法
如果一个控件实现ViewGroup，一定要实现onLayout方法

这个是activity
```
public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }
    private void initView() {
        LayoutInflater inflater = getLayoutInflater();
        HorizontalScrollViewEx horizontalScrollView = findViewById(R.id.hsv);
        int screenWidth = 1440;
        int screenHeight = 1800;
        // 添加View
        for (int i = 0; i < 3; i++) {
            ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.test, horizontalScrollView, false);
            layout.getLayoutParams().width = screenWidth;
            TextView textView = layout.findViewById(R.id.title);
            textView.setText("page i" + 1);
            layout.setBackgroundColor(Color.rgb(255 / (i + 1), 255 / (i + 1), 0));
            createList(layout);
            horizontalScrollView.addView(layout);
        }
    }
    private void createList(ViewGroup layout) {
        ListView listView = layout.findViewById(R.id.list);
        ArrayList<String> datas = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            datas.add("name" + i);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.content_list_item, R.id.name, datas);
        listView.setAdapter(adapter);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
    }
}

```

```
public class HorizontalScrollViewEx extends FrameLayout {
    private static final String TAG = "HorizontalScrollViewEx";
    private int mChildrenSize=3;
    private int mCHildWidth=1440;
    private int mChildIndex;
    private int mLastX = 0;
    private int mLastY = 0;
    private int mLastXIntercept = 0;
    private int mLastYIntercept = 0;

    private Scroller mScroller;
    private VelocityTracker mVelpcityTracker;


    private void init() {
        mScroller = new Scroller(getContext());
        mVelpcityTracker = VelocityTracker.obtain();
    }

    public HorizontalScrollViewEx(Context context) {
        super(context);
        init();
    }

    public HorizontalScrollViewEx(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HorizontalScrollViewEx(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    // 判断是否拦截
    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        boolean intercepted = false;
        int x = (int) event.getX();
        int y = (int) event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                intercepted = false;
                if (!mScroller.isFinished()) {
                    //动画没有结束强制结束动画
                    mScroller.abortAnimation();
                    intercepted = true;
                }
                break;
            case MotionEvent.ACTION_MOVE:

                int deltaX = x - mLastXIntercept;
                int deltaY = x - mLastYIntercept;
                if (Math.abs(deltaX) > Math.abs(deltaY)) {
                    intercepted = true;
                } else {
                    intercepted = false;
                }
                break;
            case MotionEvent.ACTION_UP:
                intercepted = false;
                break;
        }

        Log.i("哇哈哈", "intercepted :" + intercepted);
        mLastX = x;
        mLastY = y;
        mLastXIntercept = x;
        mLastYIntercept = y;
        return intercepted;
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childLeft = 0;
        final int childCount = getChildCount();
        mChildrenSize = childCount;

        for (int i = 0; i < childCount; i++) {
            final View childView = getChildAt(i);
            if (childView.getVisibility() != View.GONE) {
                final int childWidth = childView.getMeasuredWidth();
                childView.layout(childLeft, 0, childLeft + childWidth,
                        childView.getMeasuredHeight());
                childLeft += childWidth;
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mVelpcityTracker.addMovement(event);
        int x = (int) event.getX();
        int y = (int) event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!mScroller.isFinished()) {
                    //动画没有结束强制结束动画
                    mScroller.abortAnimation();
                }
                break;
            case MotionEvent.ACTION_MOVE:

                int deltaX = x - mLastX;
                int deltaY = x - mLastY;
                scrollBy(-deltaX, 0);

                break;
            case MotionEvent.ACTION_UP:
                int scrollX = getScrollX();
                int scrollToChildIndex = scrollX / mCHildWidth;
                mVelpcityTracker.computeCurrentVelocity(1000);
                float xVelocity = mVelpcityTracker.getXVelocity();
                if (Math.abs(xVelocity) >= 50) {
                    mChildIndex = xVelocity > 0 ? mChildIndex - 1 : mChildIndex + 1;

                } else {
                    mChildIndex = (scrollX + mCHildWidth / 2) / mCHildWidth;
                }
                mChildIndex = Math.max(0, Math.min(mChildIndex, mChildrenSize - 1));
                int dx = mChildIndex * mCHildWidth - scrollX;
                smoothScrollBy(dx, 0);
                mVelpcityTracker.clear();
                break;
        }


        mLastY = y;
        mLastX = x;
        return super.onTouchEvent(event);
    }

    private void smoothScrollBy(int dx, int i) {
        mScroller.startScroll(getScrollX(), 0, dx, 0, 500);
        invalidate();
    }


    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();
        }


        super.computeScroll();
    }
}


```


xml
```
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context="com.smart.kaifa.MainActivity">

<com.smart.kaifa.HorizontalScrollViewEx
    android:id="@+id/hsv"
    android:layout_width="match_parent"
    android:layout_height="match_parent">
</com.smart.kaifa.HorizontalScrollViewEx>
</FrameLayout>

```

实战：子控件拦截法
```

public class ListViewEx extends ListView {
    private static final String TAG = "ListEx";
    private HorizontalScrollViewEx2 horizontalScrollViewEx2;
    private int mLastX = 0;
    private int mLastY = 0;

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        horizontalScrollViewEx2= (HorizontalScrollViewEx2)( getParent());
        int x = (int) event.getX();
        int y = (int) event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 父控件不要拦截
                horizontalScrollViewEx2.requestDisallowInterceptTouchEvent(true);
                break;
            case MotionEvent.ACTION_MOVE:

                int deltaX = x - mLastX;
                int deltaY = x - mLastY;
                if (Math.abs(deltaX) > Math.abs(deltaY)) {
                    horizontalScrollViewEx2.requestDisallowInterceptTouchEvent(false);
                }
                break;
            case MotionEvent.ACTION_UP:

                break;
        }
        mLastX = x;
        mLastY = y;
        return super.dispatchTouchEvent(event);
    }


    public ListViewEx(Context context) {
        super(context);
    }

    public ListViewEx(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ListViewEx(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}

```

```
public class HorizontalScrollViewEx2 extends ViewGroup {
    private int mChildrenSize = 3;

    public HorizontalScrollViewEx2(Context context) {
        super(context);
        init();
    }

    public HorizontalScrollViewEx2(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mScroller = new Scroller(getContext());
        mVelpcityTracker = VelocityTracker.obtain();
    }

    public HorizontalScrollViewEx2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private Scroller mScroller;
    private int mLastX = 0;
    private int mLastY = 0;
    private VelocityTracker mVelpcityTracker;
    private int mCHildWidth = 1440;
    private int mChildIndex;
    private int mLastXIntercept = 0;
    private int mLastYIntercept = 0;

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childLeft = 0;
        final int childCount = getChildCount();
        mChildrenSize = childCount;
        for (int i = 0; i < childCount; i++) {
            final View childView = getChildAt(i);
//            if (childView.getVisibility() != View.GONE) {
                final int childWidth = childView.getMeasuredWidth();
                childView.layout(childLeft, 0, childLeft + childWidth,
                       1000);
                childLeft += childWidth;
//            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int measuredWidth = 0;
        int measuredHeight = 0;
        final int childCount = getChildCount();
        measureChildren(widthMeasureSpec, heightMeasureSpec);

        int widthSpaceSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSpaceSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        if (childCount == 0) {
            setMeasuredDimension(0, 0);
        } else if (heightSpecMode == MeasureSpec.AT_MOST) {
            final View childView = getChildAt(0);
            measuredHeight = childView.getMeasuredHeight();
            setMeasuredDimension(widthSpaceSize, childView.getMeasuredHeight());
        } else if (widthSpecMode == MeasureSpec.AT_MOST) {
            final View childView = getChildAt(0);
            measuredWidth = childView.getMeasuredWidth() * childCount;
            setMeasuredDimension(measuredWidth, heightSpaceSize);
        } else {
            final View childView = getChildAt(0);
            measuredWidth = childView.getMeasuredWidth() * childCount;
            measuredHeight = childView.getMeasuredHeight();
            setMeasuredDimension(measuredWidth, measuredHeight);
        }
    }
    private void smoothScrollBy(int dx, int i) {
        mScroller.startScroll(getScrollX(), 0, dx, 0, 500);
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mVelpcityTracker.addMovement(event);
        int x = (int) event.getX();
        int y = (int) event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!mScroller.isFinished()) {
                    //动画没有结束强制结束动画
                    mScroller.abortAnimation();
                }
                break;
            case MotionEvent.ACTION_MOVE:

                int deltaX = x - mLastX;
                int deltaY = x - mLastY;
                scrollBy(-deltaX, 0);

                break;
            case MotionEvent.ACTION_UP:
                int scrollX = getScrollX();
                int scrollToChildIndex = scrollX / mCHildWidth;
                mVelpcityTracker.computeCurrentVelocity(1000);
                float xVelocity = mVelpcityTracker.getXVelocity();
                if (Math.abs(xVelocity) >= 50) {
                    mChildIndex = xVelocity > 0 ? mChildIndex - 1 : mChildIndex + 1;

                } else {
                    mChildIndex = (scrollX + mCHildWidth / 2) / mCHildWidth;
                }
                mChildIndex = Math.max(0, Math.min(mChildIndex, mChildrenSize - 1));
                int dx = mChildIndex * mCHildWidth - scrollX;
                smoothScrollBy(dx, 0);
                mVelpcityTracker.clear();
                break;
        }


        mLastY = y;
        mLastX = x;
        return super.onTouchEvent(event);
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();
        }


        super.computeScroll();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastX = x;
                mLastY = y;

                if (!mScroller.isFinished()) {
                    //动画没有结束强制结束动画
                    mScroller.abortAnimation();
                    return true;
                }
                return false;
        }
        return true;
    }


}

```

```
public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }

    private void initView() {

        LayoutInflater inflater = getLayoutInflater();
        HorizontalScrollViewEx2 horizontalScrollView = findViewById(R.id.hsv);
        int screenWidth = 1440;
        int screenHeight = 1800;
        for (int i = 0; i < 3; i++) {
            ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.test, horizontalScrollView, false);
            layout.getLayoutParams().width = screenWidth;
            layout.getLayoutParams().height = screenHeight;
            layout.setBackgroundColor(Color.rgb(255 / (i + 1), 255 / (i + 1), 0));
            createList(layout);
            horizontalScrollView.addView(layout);
            Log.i("test11221", horizontalScrollView.getMeasuredWidth() + "---");
            layout.getWidth();
            Log.i("test", layout.getMeasuredWidth() + "");

        }
    }

    private void createList(ViewGroup layout) {
        ListViewEx listView = layout.findViewById(R.id.list);
        ArrayList<String> datas = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            datas.add("name" + i);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.content_list_item, R.id.name, datas);
        listView.setAdapter(adapter);
    }
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {

    }


}

```

```
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context="com.smart.kaifa.MainActivity">

<com.smart.kaifa.HorizontalScrollViewEx2
    android:id="@+id/hsv"
    android:layout_width="match_parent"
    android:layout_height="match_parent">



</com.smart.kaifa.HorizontalScrollViewEx2>


</FrameLayout>

```
text.xml
```
<?xml version="1.0" encoding="utf-8"?>
    <com.smart.kaifa.ListViewEx
        android:id="@+id/list"
        xmlns:android="http://schemas.android.com/apk/res/android"
        android:layout_width="match_parent"
        android:layout_height="match_parent"></com.smart.kaifa.ListViewEx>

```

注意一下，viewgroup很多时候要自己measure

这里提供一个场景来分析一下场景2

这里提供一个可以上下滑动父容器：StickyLayout，然后在他的内部分别放置一个Header和ListView这样内外两层就可以同时上下滑动。于是形成了场景2的冲突。当然这里的滑动规则是：当Header显示时或者ListView滑动到顶部的时候，由StickyLayout拦截事件，当header隐藏的时候，这里要分情况，如果是向下滑动，还是由StickyLayout拦截事件，如果向上滑动，就交给子控件。

这个在下一章View的工作原理里面介绍
