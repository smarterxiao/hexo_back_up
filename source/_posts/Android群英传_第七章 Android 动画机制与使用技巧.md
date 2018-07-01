---
title: Android群英传 第七章 Android 动画机制与使用技巧
date: 2017-11-01 22:17:07
categories: android
top : 107
tags:
- 基础
- Android群英传
---

# Android View 动画框架
> Android框架定义了透明度，旋转，缩放，位移等常见的几种动画，原理是每次绘制视图的时候哦获取View所在的ViewGroup中的drawChild函数，获取该View的Animation的Transformation值，然后调用canvas。concat（transformToApply。getMatrix()）,通过矩阵完成动画帧。如果动画没有完成，就调用invalidate（），直到完成绘制。
* 属性动画
* 视图动画

# 视图动画

## 透明度动画

```
        View view = findViewById(R.id.view);
        AlphaAnimation aa=new AlphaAnimation(0,1);
        aa.setDuration(1000);
        view.startAnimation(aa);

```

## 旋转动画

```
      View view = findViewById(R.id.view);
      RotateAnimation aa=new RotateAnimation(0,1);
      aa.setDuration(1000);
      view.startAnimation(aa);
```

## 位移动画

```
     View view = findViewById(R.id.view);
     TranslateAnimation aa=new TranslateAnimation(0,200,0,300);
     aa.setDuration(1000);
     view.startAnimation(aa);
```

## 缩放动画

```
       View view = findViewById(R.id.view);
       ScaleAnimation aa=new ScaleAnimation(0,2,0,2);
       aa.setDuration(1000);
       view.startAnimation(aa);

```

## 动画集合

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


# 属性动画的分析
## ObjecAnimator  这个是属性动画的基础
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

## PropertyValuesHolder :属性动画集合

属性动画集合的使用,类似视图动画的AnimationSet

```
      PropertyValuesHolder pvh1 = PropertyValuesHolder.ofFloat("translationX", 300f);
      PropertyValuesHolder pvh2 = PropertyValuesHolder.ofFloat("scaleX", 1f, 0, 1f);
      PropertyValuesHolder pvh3 = PropertyValuesHolder.ofFloat("scaleY", 3000);
      ObjectAnimator.ofPropertyValuesHolder(view, pvh1, pvh2, pvh3).setDuration(3000).start();

```

## ValueAnimator
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

## 动画事件的监听

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

## AnimatorSet
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
## 在XML中使用属性动画
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


## 属性动画的简写

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
# Android 布局动画
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

# 插值器

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

# 自定义动画
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


# Android 5.x SVG矢量动画

通过xml绘制动画

## 标签

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

## 常用指令
* L：L 200 400 从(0,0)到(200,400)的直线
* M：移动坐标点
* A：弧线 RX，RY是椭圆半轴的大小；XROTATION是椭圆的X轴与水平方向顺时针方向的夹角；FLAG1只有两个值 0表示小角弧线，1表示大角弧线；FLAF2只有两个值，确定起点和终点的方向，1为顺时针 2为逆时针；X，Y是终点坐标

## 一个SVG编辑器

http://editor.method.ac/

Inkscape

## Android使用SVG
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

# Android 动画特效

## 展开动画
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


## 计时器动画

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

## 下拉展开动画
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
