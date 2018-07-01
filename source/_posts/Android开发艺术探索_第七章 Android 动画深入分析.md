---
title: Android开发艺术探索 第七章 Android 动画深入分析  
date: 2018-01-27 17:08:28
top : 207
tags:
- 进阶
- Android开发艺术探索
categories: android
---

> Android的动画可以分为三种：View动画，帧动画和属性动画，其实帧动画也属于View动画的一种，只不过他和平移。旋转等常见的View动画在表现形式上略有不用而已。View动画通过场景里的对象不断做图像变换（平移，缩放，旋转，透明度）从而产生动画效果，他是一种渐进式的动画，并且View动画支持自定义。帧动画通过顺序播放一系列图像从而产生动画效果，可以简单理解为图片切换动画。属性动画是API11的新特性，在低版本无法使用水性动画，但是我们任然可以通过使用兼容库来使用它。

# View动画
View动画的作用对象是View，他支持4中动画效果，分别是平移动画，缩放动画，旋转动画，透明动画，处理这四种经典的动画，帧动画也属于View动画，但是帧动画的表现形式和上面四种动画不太一样，为了更好了区分这四种变换和帧动画。这里的View动画就表示4中变换，不包括帧动画。
## View动画的种类
View动画的四种变换效果对应着4个Animation的子类：TranslateAnimation，ScaleAnimation，RotateAnimation，AlphaAnimation，这四种动画可以通过XML来创建，也可以通过代码来动态创建

|名称|标签|子类|效果|
|:---:|:---:|:---:|:---:|
|平移动画|translate|TranslateAnimation|移动View|
|缩放动画|scale|ScaleAnimation|放大或者缩小View|
|旋转动画|rotate|RotateAnimation|旋转View|
|透明动画|alpha|alphaAnimation|改变View的透明度|

```
<?xml version="1.0" encoding="utf-8"?>
<set xmlns:android="http://schemas.android.com/apk/res/android">
    <alpha
        android:fromAlpha="1"
        android:toAlpha="0.3" />

    <rotate
        android:fromDegrees="0"
        android:pivotX="0"
        android:pivotY="0"
        android:toDegrees="2" />

    <translate
        android:fromXDelta="0"
        android:fromYDelta="0"
        android:toXDelta="100"
        android:toYDelta="100" />

    <scale
        android:fromXScale="0"
        android:fromYScale="0"
        android:pivotX="5"
        android:pivotY="5"
        android:toXScale="2"
        android:toYScale="2"/>
</set>
```
从上面语法可以看出View动画既可以是单个动画，也可以是一系列动画
set标签标示动画集合，对应animationSet这个类，他可以包含若干个动画，并且内部也是可以嵌套其他动画集合的，他的连个属性如下

* android:interpolator
  标示动画集合所采用的的差值器，差值器影响动画的速度，比如非匀速，加速度，减速等播放效果
* android:shareInterpolator
  标示集合动画是否共享一个差值器，如果集合不指定差值器，那么子动画就需要单独指定差值器或者使用默认值
* translate

|标签|含义|
|:---:|:---:|
|android:fromXDelta|表示x的起始值，比如0|
|android:fromYDelta|表示y的起始值，比如0|
|android:toXDelta|表示x的结束值，比如100|
|android:toYDelta|表示y的结束值|

* scale
这里会提及一个轴点的概念，默认的轴点是中心点，是左右两个方向同时缩放，如果设置轴点为右边界，那么View就会只向左边进行缩放，可以试一下

|标签|含义|
|:---:|:---:|
|android:fromXScale|水平方向的起始值，比如0.5|
|android:fromYScale|竖直方向的起始值，比如0.5|
|android:toXScale|水平方向的结束值，比如0.5|
|android:toYScale|竖直方向的结束值，比如0.5|
|android:pivotX|缩放的轴点的X坐标，会影响缩放效果|
|android:pivotY|缩放的轴点的Y坐标，会影响缩放效果|

* rotate
  旋转动画中也有轴点的概念，他会影响旋转的具体效果，默认情况下是围绕中心点进行旋转的

|标签|含义|
|:---:|:---:|
|android:fromDegrees|旋转开始的角度，比如0|
|android:toDegrees|旋转结束的角度，比如180|
|android:pivotX|旋转的轴点x坐标|
|android:pivotY|旋转的轴点y坐标|

* alpha

|标签|含义|
|:---:|:---:|
|android:fromAlpha|表示透明度起始值，比如0|
|android:toAlpha|表示透明度结束值，比如1|

* 公共属性

|标签|含义|
|:---:|:---:|
|android:duration|动画持续时间|
|android:fillAfter|动画结束之后是否停留在当前位置，true表示停留，false表示不停留|

上面简单介绍了一下View动画的分类和常用的属性值及其含义，现在来看一下效果
首先是单个动画效果

* translate 效果

动画xml
```[xml]
<?xml version="1.0" encoding="utf-8"?>
<set xmlns:android="http://schemas.android.com/apk/res/android" android:duration="10000">
    <translate
        android:fromXDelta="0"
        android:fromYDelta="0"
        android:toXDelta="1000"
        android:toYDelta="1000" />
</set>
```

activity
```[java]
public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImageView tv = findViewById(R.id.tx);
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.filename);
        tv.startAnimation(animation);
    }
}

```

布局XML
```[xml]
<?xml version="1.0" encoding="utf-8"?>
<android.support.constraint.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity">

    <ImageView
        android:id="@+id/tx"
        android:src="@drawable/test333"
        android:layout_width="100dp"
        android:layout_height="100dp" />

</android.support.constraint.ConstraintLayout>
```

![Alt text](move.gif "移动效果图")

*  scale 效果

```
<?xml version="1.0" encoding="utf-8"?>
<set xmlns:android="http://schemas.android.com/apk/res/android"
    android:duration="10000">
    <scale
        android:fromXScale="0"
        android:fromYScale="0"
        android:toXScale="2"
        android:toYScale="2" />
</set>
```
![Alt text](scale_1.gif "缩放效果图")

```
<?xml version="1.0" encoding="utf-8"?>
<set xmlns:android="http://schemas.android.com/apk/res/android"
    android:duration="10000">
    <scale

        android:fromXScale="0"
        android:fromYScale="0"
        android:pivotX="300"
        android:pivotY="300"
        android:toXScale="2"
        android:toYScale="2" />
</set>
```

![Alt text](scale_2.gif "设置轴点的缩放效果图")

* rotate 效果
```
<?xml version="1.0" encoding="utf-8"?>
<set xmlns:android="http://schemas.android.com/apk/res/android"
    android:duration="10000">
    <rotate
        android:fromDegrees="0"
        android:toDegrees="180" />
</set>
```
![Alt text](rotate_1.gif "旋转效果图")

```
<?xml version="1.0" encoding="utf-8"?>
<set xmlns:android="http://schemas.android.com/apk/res/android"
    android:duration="10000">
    <rotate
        android:fromDegrees="0"
        android:pivotX="300"
        android:pivotY="300"
        android:toDegrees="180" />
</set>
```
![Alt text](rotate_2.gif "设置轴点的旋转效果图")

* alpha 效果

```
<?xml version="1.0" encoding="utf-8"?>
<set xmlns:android="http://schemas.android.com/apk/res/android"
    android:duration="10000">
    <alpha
        android:fromAlpha="0.3"
        android:toAlpha="1" />
</set>
```
![Alt text](alpha.gif "透明度变化效果")

使用代码设置动画

```
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImageView tv = findViewById(R.id.tx);
        AlphaAnimation alphaAnimation=new AlphaAnimation(0,1);
        alphaAnimation.setDuration(10000);
        tv.startAnimation(alphaAnimation);
    }
}

```
组合动画就是将几个标签放在一起，这样几个动画同时播放

这里做一个验证
```
<?xml version="1.0" encoding="utf-8"?>
<set xmlns:android="http://schemas.android.com/apk/res/android"
    android:duration="3000" android:fillAfter="true">
    <translate
        android:fromXDelta="0"
        android:fromYDelta="0"
        android:toXDelta="300"
        android:toYDelta="300" />
</set>
```
设置`android:fillAfter="true"` 这样移动之后就不会回去，但是给View设置点击事件，看一下点击事件会不会随着动画移动
```
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImageView tv = findViewById(R.id.tx);
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.filename);
        tv.startAnimation(animation);
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "点击我了", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

```
发现点击事件还是停留在原来的地方，点击移动之后View显示的位置是没有点击事件的。
View动画的原理是在draw的时候操作画布

## 自定义动画

除了系统提供的四种View动画外，我们还可以自定义View动画，自定义动画是一件既简单又复杂的事情，说简单是他操作比较简单，只需要继承Animation并且实现他的抽象方法initialize和applyTransformation,在initialize中做一些简单的初始化，在applyTransformation中做相应的矩阵变化即可，很多时候需要采用camera来简化矩阵的变换过程，说他复杂，是因为自定义View动画的过程主要是矩阵变化的过程，而矩阵都是数学上面的概念，如果对这个不熟悉，就比较困难了。

这里实现一个简单的3d旋转动画


```
public class Rotate3dAnimation extends Animation {
    private final float mFromDegrees;
    private final float mToDegrees;
    private final float mCenterX;
    private final float mCenterY;
    private final float mDepthZ;
    private final boolean mReverse;
    private Camera mCamera;

    public Rotate3dAnimation(float fromDegrees, float toDegrees,
                             float centerX, float centerY, float depthZ, boolean reverse) {
        mFromDegrees = fromDegrees;
        mToDegrees = toDegrees;
        mCenterX = centerX;
        mCenterY = centerY;
        mDepthZ = depthZ;
        mReverse = reverse;
    }

    @Override
    public void initialize(int width, int height, int parentWidth, int parentHeight) {
        super.initialize(width, height, parentWidth, parentHeight);
        mCamera = new Camera();
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        final float fromDegrees = mFromDegrees;
        float degrees = fromDegrees + ((mToDegrees - fromDegrees) * interpolatedTime);

        final float centerX = mCenterX;
        final float centerY = mCenterY;
        final Camera camera = mCamera;
        final Matrix matrix = t.getMatrix();
        camera.save();
        if (mReverse) {
            camera.translate(0.0f, 0.0f, mDepthZ * interpolatedTime);
        } else {
            camera.translate(0.0f, 0.0f, mDepthZ * (1.0f - interpolatedTime));
        }
        camera.rotateY(degrees);
        camera.getMatrix(matrix);
        camera.restore();
        matrix.preTranslate(-centerX, -centerY);
        matrix.postTranslate(centerX, centerY);
    }
}
```

![Alt text](rotate_3d.gif "3d旋转效果")

## 帧动画
帧动画是播放一组预先设定好的图片，类似于电影播放，不同于View动画，系统提供了另外一个类AnimationDrawable来使用帧动画，帧动画的使用比较简单

这个是放在res/drawable中的
```
<?xml version="1.0" encoding="utf-8"?>
<animation-list xmlns:android="http://schemas.android.com/apk/res/android">
<item android:drawable="@drawable/test333" android:duration="200"/>
    <item android:drawable="@drawable/test333" android:duration="200"/>
</animation-list>
```
然后将它设置播放就可以
```
public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final ImageView tv = findViewById(R.id.tx);
        AnimationDrawable animationDrawable = (AnimationDrawable) tv.getDrawable();
        animationDrawable.start();
    }
}

```

# View动画的特殊使用场景
一些特殊的animation
## LayoutAnimation
LayoutAnimation作用于ViewGroup，为ViewGroup指定一个动画，这样当他的子元素在出厂的时候都会有这种动画效果，这种动画效果常常被用在ListView上面，我们时常看到一种特殊的ListView，他的每一个item都以一定的动画效果出现，这个不是什么高深的技术，他使用的就是LayoutAnimation。LayoutAnimation是一个动画集合，为了给ViewGroup的子元素加上出场效果
在res/anim文件夹下面创建
```
<?xml version="1.0" encoding="utf-8"?>
<layoutAnimation xmlns:android="http://schemas.android.com/apk/res/android"
    android:animation="@anim/anim_item"
    android:animationOrder="normal"
    android:delay="0.5">


</layoutAnimation>
```

* android:delay 表示动画开始时的延迟，如果元素入场动画的时间周期都为300，那么0.5表示每个元素都需要延迟150ms才能播放入场动画，总体来说第一个延迟150，第二个延迟300，以此类推
* android：animationOrder  表示子元素播放动画的顺序，有三种选项：normal，reverse和random，其中normal表示顺序，reverse表示逆向，random表示随机
* android:animation 表示元素指定的入场动画

在res/anim下面anim_item
```
<?xml version="1.0" encoding="utf-8"?>
<set xmlns:android="http://schemas.android.com/apk/res/android"
    android:duration="300"
    android:shareInterpolator="true">
    <alpha
        android:fromAlpha="0.0"
        android:toAlpha="1.0" />
    <translate
        android:fromXDelta="500"
        android:toXDelta="0" />
</set>
```
activity
```[java]
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ListView tv = findViewById(R.id.list);
        ArrayList<String> datas = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            datas.add("name" + i);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.content_list_item, R.id.name, datas);
        tv.setAdapter(adapter);
    }

}

```
activity xml
```
<?xml version="1.0" encoding="utf-8"?>
<android.support.constraint.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity">

    <ListView
        android:id="@+id/list"
        android:layoutAnimation="@anim/test111"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />

</android.support.constraint.ConstraintLayout>
```
![Alt text](list_animation.gif "listView动画效果")

也可以使用代码实现这个效果
```
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ListView tv = findViewById(R.id.list);
        Animation animation=AnimationUtils.loadAnimation(this,R.anim.test111);
        LayoutAnimationController controller=new LayoutAnimationController(animation);
        controller.setDelay(0.5f);
        controller.setOrder(LayoutAnimationController.ORDER_NORMAL);
        tv.setLayoutAnimation(controller);
        ArrayList<String> datas = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            datas.add("name" + i);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.content_list_item, R.id.name, datas);
        tv.setAdapter(adapter);
    }

}
```

## Activity的切换效果
activity有默认的切换效果，但是这个效果我们可以自定义的，主要用到overridePendingtransition()这个方法，这个方法必须在startActivity(Intent)或者finish()之后调用才能生效，他的含义如下
* enterAnim  activity被打开动画
* exitAnim activity被暂停时，所需要的动画

```
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.bt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TestActivity.class);
                startActivity(intent);
                //入场动画
                overridePendingTransition(R.anim.enter_anim,R.anim.exit_anim);
            }
        });
    }

}

```
```
public class TestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    public void finish() {
        super.finish();
        //出场动画
        overridePendingTransition(R.anim.enter_anim,R.anim.exit_anim);
    }
}

```
res/anim/exit_anim.xml
```[exit_anim]
<?xml version="1.0" encoding="utf-8"?>
<set xmlns:android="http://schemas.android.com/apk/res/android"
    android:duration="300"
  >
    <alpha
        android:fromAlpha="1"
        android:toAlpha="0" />
</set>
```
res/anim/enter_anim.xml
```
<?xml version="1.0" encoding="utf-8"?>
<set xmlns:android="http://schemas.android.com/apk/res/android"
    android:duration="300">
    <alpha
        android:fromAlpha="0"
        android:toAlpha="1" />
</set>
```

这里是一个简单的透明度变化
![Alt text](activity.gif "activity 动画效果")

fragment也可以添加切换动画，是在api11之后引入的，可以通过fragmentTransaciton的`setCustomAnimations()`方法来添加切换动画
# 属性动画
属性动画是API11新加入的特性，和View动画不同，他对作用对象进行了脱产，属性动画可以对任意对象做动画，甚至还可以没有对象...
属性动画中的ValueAnimator，ObjectAnimator和AnimatorSet等概念，通过他们可以实现不同的效果
## 使用属性动画
属性动画可以对任意对象的属性进行动画设置，而不仅仅是View，他改变的是一个对象的属性，而View动画只是在draw的时候对画布进行操作。属性动画的默认时间间隔是300ms，默认帧率是10ms/帧。 如果需要兼容android2.3之前的版本，可以使用nineoldandroids这个库。比较常用的几个动画类是ValueAnimator，ObjectAnimator和Animatorset。其中ObjectAnimator继承自ValueAnimator，AnimatorSet是动画集合，可以定义一组动画。
```
        ObjectAnimator.ofFloat(bt,"translationY",-bt.getHeight()).start();
```
一个简单的ObjectAnimator动画，这个是系统已经内置好的，在Y轴方向上面进行平移

```
ValueAnimator colorAnim=ObjectAnimator.ofInt(bt,"backgroundColor",0xffff8080,0xff8080ff);
colorAnim.setDuration(3000);
colorAnim.setEvaluator(new ArgbEvaluator());
colorAnim.setRepeatCount(ValueAnimator.INFINITE);
colorAnim.setRepeatMode(ValueAnimator.REVERSE);
colorAnim.start();
```
![Alt text](color_change.gif "颜色变化")
动画集合
```
AnimatorSet set = new AnimatorSet();

     set.playTogether(ObjectAnimator.ofFloat(bt, "rotationX", 0, 360)
             , ObjectAnimator.ofFloat(bt, "rotationY", 0, 180)
             , ObjectAnimator.ofFloat(bt, "rotation", 0, -90)
             , ObjectAnimator.ofFloat(bt, "translationX", 0, 90)
             , ObjectAnimator.ofFloat(bt, "translationY", 0, 90)
             , ObjectAnimator.ofFloat(bt, "scaleX", 1, 1.5f)
             , ObjectAnimator.ofFloat(bt, "scaleY", 1, 1.5f)
             , ObjectAnimator.ofFloat(bt, "alpha", 1, 0.25f, 1));
     set.setDuration(5000).start();
```
![Alt text](animator_set.gif "属性动画集合变换")

除了使用代码实现之外，还可以通过XML来实现，属性动画的定义需要在res/animator目录下面,和View动画res/anim做区分。语法规则如下

```
<?xml version="1.0" encoding="utf-8"?>
<set xmlns:android="http://schemas.android.com/apk/res/android"
    android:ordering="together">
    <objectAnimator
        android:duration="300"
        android:propertyName="x"
        android:valueTo="20"
        android:valueType="intType" />

    <animator android:valueType="colorType" />
    <set android:ordering="together">

    </set>
    <propertyValuesHolder />
</set>
```
属性动画的各种参数都比较好理解，在XML中可以定义ValueAnimator，ObjectAnimator，Animatorset：其中ValueAnimator对应aniamtor，OnjectAnimator对应OnjectAnimator，set表示Animatorset。在set中有一个标签android:vrdering同时又连个属性可选：together和sequentially，其中together表示动画集合中的子动画同时播放，sequentially表示动画集合中的自动化按照前后顺序播放，默认是together

|标签|含义|
|:---:|:---:|
|android:propertyName|表示属性动画的作用对象的属性名称|
|android:duration|表示动画的时长|
|android:valueFrom|表示属性的起始值|
|android:valueTo|表示属性结束值|
|android:startOffset|表示动画演示，当动画开始后，多少毫秒后会真正播放此动画|
|android:reoeatCount|表示动画的重复次数：这里说明一下默认值是你0，-1代表无线循环|
|android:repeatMode|表示动画的重复模式：有连个选项restart和reverse，一个是连续重复，一个是逆向重复|
|android:valueType|表示android：propertyName所指定的类型，有intType，floatType，colorType,pathType|

来一个例子

```
Animator animator = AnimatorInflater.loadAnimator(this, R.animator.animator_1);
 animator.setTarget(bt);
 animator.start();
```
建议使用代码，很多东西需要动态测量的
## 理解插值器和估值器
TimeInterpolator中文译为时间插值器，他的作用是根据时间流逝的百分比来计算出当前属性改变的百分比，系统预置有LinearInterpolator(线性插值器：匀速动画)，AccelerateDecelerateInterPolator(加速插值器：动画两头慢，中间快)，decelerateInterpolator（减速插值器：速度越来越慢）等。TyepEvaluator中文翻译为类型估值算法，也叫估值器，他的作用是更具当前属性改变的百分比计算改变后的值，有intEvalutor，FloatEvaluator和ArgbEvaluator。
这里看一下实例
这里有一个匀速运动的动画，所以采用线性插值器和整型估值算法，在40ms内，View的X属性从0-40的变换
由于动画的默认刷新率为10ms/帧,所以该动画分为5帧进行，我们来考虑第三帧，x=20 r=20ms 当时间t=20ms的时候，时间流逝的百分比是（20/40）0.5，意味着现在时间过了一半，那么X改变了多少呢，这个就需要线性估值算法来操作了。当时间流逝一半的时候，X的变换也应该是一半，即x改变的是0.5.看一下他的源码
```
public class LinearInterpolator extends BaseInterpolator implements NativeInterpolatorFactory {

    public LinearInterpolator() {
    }

    public LinearInterpolator(Context context, AttributeSet attrs) {
    }

    public float getInterpolation(float input) {
        return input;
    }

    /** @hide */
    @Override
    public long createNativeInterpolator() {
        return NativeInterpolatorFactoryHelper.createLinearInterpolator();
    }
}

```
可以看到线性插值器的返回和输出值一样，因此插值器返回的值是0.5，这意味着x的改变时0.5，这个时候插值器的工作就完成了
这里在看一下DecelerateInterpolator 减速插值器的`getInterpolation`可以看到对输入值进行处理了
```
public class DecelerateInterpolator extends BaseInterpolator implements NativeInterpolatorFactory {
    public DecelerateInterpolator() {
    }

    /**
     * Constructor
     *
     * @param factor Degree to which the animation should be eased. Setting factor to 1.0f produces
     *        an upside-down y=x^2 parabola. Increasing factor above 1.0f makes exaggerates the
     *        ease-out effect (i.e., it starts even faster and ends evens slower)
     */
    public DecelerateInterpolator(float factor) {
        mFactor = factor;
    }

    public DecelerateInterpolator(Context context, AttributeSet attrs) {
        this(context.getResources(), context.getTheme(), attrs);
    }

    /** @hide */
    public DecelerateInterpolator(Resources res, Theme theme, AttributeSet attrs) {
        TypedArray a;
        if (theme != null) {
            a = theme.obtainStyledAttributes(attrs, R.styleable.DecelerateInterpolator, 0, 0);
        } else {
            a = res.obtainAttributes(attrs, R.styleable.DecelerateInterpolator);
        }

        mFactor = a.getFloat(R.styleable.DecelerateInterpolator_factor, 1.0f);
        setChangingConfiguration(a.getChangingConfigurations());
        a.recycle();
    }

    public float getInterpolation(float input) {
        float result;
        if (mFactor == 1.0f) {
            result = (float)(1.0f - (1.0f - input) * (1.0f - input));
        } else {
            result = (float)(1.0f - Math.pow((1.0f - input), 2 * mFactor));
        }
        return result;
    }

    private float mFactor = 1.0f;

    /** @hide */
    @Override
    public long createNativeInterpolator() {
        return NativeInterpolatorFactoryHelper.createDecelerateInterpolator(mFactor);
    }
}

```
再来看一下整型估值算法
```
public class IntEvaluator implements TypeEvaluator<Integer> {
    public Integer evaluate(float fraction, Integer startValue, Integer endValue) {
        int startInt = startValue;
        return (int)(startInt + fraction * (endValue - startInt));
    }
}
```
三个参数表示估值小数，开始值，结束值，对应于我们的例子就分别是0.55,0,40 这样整型估值器给我吗就返回20 。

属性动画要求对象的改属性有set和get方法（可选）插值器和估值器处理系统提供的，还可以自定义，实现也很简单
插值器继承BaseInterpolator，估值器继承TypeEvaluator
http://www.jcodecraeer.com/a/anzhuokaifa/androidkaifa/2015/0110/2292.html
## 属性动画的监听器
属性动画提供了监听器用于监听动画的播放过程，主要有两个接口AnimatorUpdateListener和AnimatorListener

```
public static interface AnimatorListener {
     default void onAnimationStart(Animator animation, boolean isReverse) {
         onAnimationStart(animation);
     }
     default void onAnimationEnd(Animator animation, boolean isReverse) {
         onAnimationEnd(animation);
     }
     void onAnimationStart(Animator animation);
     void onAnimationEnd(Animator animation);
     void onAnimationCancel(Animator animation);
     void onAnimationRepeat(Animator animation);
 }
```

也可以使用AnimatorListenerAdapter这个已经实现好的，在具体实现某一个方法
ValueAnimator.AnimatorUpdateListener 这个比较特殊，他会监听整个动画的过程，动画是由许多帧组成的，每播放一帧，onAnimationUpdate就会被调用

```
public static interface AnimatorUpdateListener {
    void onAnimationUpdate(ValueAnimator animation);
}

```

## 对任意属性做动画
这里先提出一个问题：给Button加一个动画，让这个Button的宽度从当前宽度增加到500px，这个可以使用View动画，但是View动画只是对canvas的修改，他的实质并没有变化，点击事件等并没有移动到当前显示的大小，这个就可以使用属性动画
```
ObjectAnimator.ofInt(bt,"width",500).setDuration(5000).start();
```
但是在AndroidStudio中会显示报红，提示没有这个属性
下面分析一下属性动画的原理:属性动画要求动画作用的对象提供该属性的set和get方法，属性动画根据外界传递的该属性的初始值和最终值，以动画的效果多次调用set方法，每一次传递的set方法都不一样，确切的说随着时间的推移，所传递的值越来越接近最终值，总结一下，我们对object的属性abc做动画，如果想让动画生效，那么要满足两个条件
1. object必须要提供setAbc方法，如果动画没有初始化传递值，还要提供getAbc方法，因为系统要去取abc的初始值，（如果这条不满足，程序直接crash）
2. object的setAbc属性必须可以通过某正方式反映出来，不然无效，没有任何效果

以上两个条件缺一不可，那么我们队width属性做动画为什么没有效果，因为button内部虽然提供了setwidth和getwidth的方法，但是这个方法并不是改变视图的大小，他是textView新添加的方法，View是没有这个setWidth方法的，由于Button继承了TextView，所以But透明也就有了setWidth方法。下卖弄看一下这个getWidth和setWidth方法的源码

```
public void setWidth(int pixels) {
    mMaxWidth = mMinWidth = pixels;
    mMaxWidthMode = mMinWidthMode = PIXELS;

    requestLayout();
    invalidate();
}

public final int getWidth() {
    return mRight - mLeft;
}

```

可以看到getWidth确实是获取View的宽度，但是setWidth是设置TextView的最大宽度和最小宽度，并不是设置View的宽度。这个和TextView的宽度不是一个东西。具体来说textView的宽度应该对应android:layout_width属性，而TextView还有一个属性android:width这个属性就对应TextView的setWidth方法，总之，TextView和Button的setWidth、getWidth干的是不同的事情，通过setWidth无法改变控件的宽度，所以对width做属性动画是没有效果的。针对上面这个问题，官方文档给了3种解决方案
* 给你的对象加上set和get方法，如果有权限的话
* 用一个类来包装原始的对象，间接为其提供get和set属性
* 采用ValueAnimator监听动画过程，自己实现属性动画的改变

针对这三种解决办法，这里给出具体的介绍
1. 给你的对象加上get和set方法，如果你有权限的话
这个意思很好理解，如果你有权限的话，加上get和set就可以，但是很多时候不可行
2. 用一个类来包装原始的对象，间接为其提供get和set属性
这是一种很实用的方法。

```

public class MainActivity extends AppCompatActivity {

    private View bt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bt = findViewById(R.id.bt);

    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        ViewWrapper viewWrapper = new ViewWrapper(bt);
        ObjectAnimator.ofInt(viewWrapper, "width", 500).setDuration(5000).start();
    }

    private static class ViewWrapper {
        private View mTarget;

        public ViewWrapper(View target) {
            mTarget = target;
        }

        private int getWidth() {
            return mTarget.getLayoutParams().width;
        }

        private void setWidth(int width) {
            mTarget.getLayoutParams().width = width;
            mTarget.requestLayout();
        }

    }
}

```

![Alt text](animator_show1.gif "效果")

3. 采用ValueAnimator，监听动画过程，自己实现属性的改变
首先说说什么是ValueAnimator，ValueAnimator本身不作用与任何对象，也就是直接使用它没有任何动画效果，他可以对一个值做动画，然后我们可以监听其动画过程，在动画过程中修改我们的对象的属性值，就相当于我们给对象做了动画

```

@Override
  public void onWindowFocusChanged(boolean hasFocus) {
      super.onWindowFocusChanged(hasFocus);
//        ViewWrapper viewWrapper = new ViewWrapper(bt);
      performAnimate(bt,0,500);
  }

private void performAnimate(final View target, final int start, final int end) {
    ValueAnimator valueAnimator=ValueAnimator.ofInt(1,100);
    valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
        private IntEvaluator intEvaluator=new IntEvaluator();
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            //获取当前进度
            int currentValue= (int) animation.getAnimatedValue();
            //获取当前动画占整个动画的百分比
            float fraction=animation.getAnimatedFraction();
            target.getLayoutParams().width=intEvaluator.evaluate(fraction,start,end);
            target.requestLayout();
        }
    });
    valueAnimator.setDuration(5000);
    valueAnimator.start();
}

```

![Alt text](animator_show1.gif "效果")

这里还要说一下，在这个方法里面去当前值1-100和当前占的比例，我们可以计算出现在的宽度，如果时间过了一半，当前是50，比例为0.5，假设Button的初始宽度是100，那么最终的宽度是500，那么增加的宽度应该是总增加宽度的一半就是（500-100）* 0.5总宽度是（500-100）* 0.5+100

###属性动画的工作原理
属性动画要求动画作用的对象提供该属性的set方法，属性动画根据你传递的改属性的初始值和最终值。以动画的效果多次调用set方法，每次set的值都不一样，随着时间的推移不断接近最终值，如果没有传递初始值，那么还有有get方法去获取最终值
现在来看一下属性动画的源码

就用上面讲的那个例子
```
    ObjectAnimator.ofInt(viewWrapper,"width",0,500).setDuration(2000).start();
```

先看一下ofInt方法

```
public static ObjectAnimator ofInt(Object target, String propertyName, int... values) {
      ObjectAnimator anim = new ObjectAnimator(target, propertyName);
      anim.setIntValues(values);
      return anim;
  }

```
看一下他的构造方法，这里创建PropertyValuesHolder并初始化，然后进行赋值
```
public void setPropertyName(@NonNull String propertyName) {
    // mValues could be null if this is being constructed piecemeal. Just record the
    // propertyName to be used later when setValues() is called if so.
    if (mValues != null) {
        PropertyValuesHolder valuesHolder = mValues[0];
        String oldName = valuesHolder.getPropertyName();
        valuesHolder.setPropertyName(propertyName);
        mValuesMap.remove(oldName);
        mValuesMap.put(propertyName, valuesHolder);
    }
    mPropertyName = propertyName;
    // New property/values/target should cause re-initialization prior to starting
    mInitialized = false;
}

```
这里创建对象并且调用了setIntValues这个方法
```
@Override
public void setIntValues(int... values) {
    if (mValues == null || mValues.length == 0) {
        // No values yet - this animator is being constructed piecemeal. Init the values with
        // whatever the current propertyName is
        if (mProperty != null) {
            setValues(PropertyValuesHolder.ofInt(mProperty, values));
        } else {
            setValues(PropertyValuesHolder.ofInt(mPropertyName, values));
        }
    } else {
        super.setIntValues(values);
    }
}
```

在看一下start方法

```
@Override
public void start() {
    AnimationHandler.getInstance().autoCancelBasedOn(this);
    if (DBG) {
        Log.d(LOG_TAG, "Anim target, duration: " + getTarget() + ", " + getDuration());
        for (int i = 0; i < mValues.length; ++i) {
            PropertyValuesHolder pvh = mValues[i];
            Log.d(LOG_TAG, "   Values[" + i + "]: " +
                pvh.getPropertyName() + ", " + pvh.mKeyframes.getValue(0) + ", " +
                pvh.mKeyframes.getValue(1));
        }
    }
    super.start();
}

```
意思很简单，就是会判断如果当前动画，等待的动画，延迟的动画中有和当前动画相同的动画，就会把相同的动画取消掉，然后就是调用父类的start方法

```[valueAnimator]
@Override
 public void start() {
     start(false);
 }

```
也就是调用valueAnimator的这个方法
```
private void start(boolean playBackwards) {
     if (Looper.myLooper() == null) {
         throw new AndroidRuntimeException("Animators may only be run on Looper threads");
     }
     mReversing = playBackwards;
     mSelfPulse = !mSuppressSelfPulseRequested;
     // Special case: reversing from seek-to-0 should act as if not seeked at all.
     if (playBackwards && mSeekFraction != -1 && mSeekFraction != 0) {
         if (mRepeatCount == INFINITE) {
             // Calculate the fraction of the current iteration.
             float fraction = (float) (mSeekFraction - Math.floor(mSeekFraction));
             mSeekFraction = 1 - fraction;
         } else {
             mSeekFraction = 1 + mRepeatCount - mSeekFraction;
         }
     }
     mStarted = true;
     mPaused = false;
     mRunning = false;
     mAnimationEndRequested = false;
     // Resets mLastFrameTime when start() is called, so that if the animation was running,
     // calling start() would put the animation in the
     // started-but-not-yet-reached-the-first-frame phase.
     mLastFrameTime = -1;
     mFirstFrameTime = -1;
     mStartTime = -1;
     addAnimationCallback(0);

     if (mStartDelay == 0 || mSeekFraction >= 0 || mReversing) {
         // If there's no start delay, init the animation and notify start listeners right away
         // to be consistent with the previous behavior. Otherwise, postpone this until the first
         // frame after the start delay.
         startAnimation();
         if (mSeekFraction == -1) {
             // No seek, start at play time 0. Note that the reason we are not using fraction 0
             // is because for animations with 0 duration, we want to be consistent with pre-N
             // behavior: skip to the final value immediately.
             setCurrentPlayTime(0);
         } else {
             setCurrentFraction(mSeekFraction);
         }
     }
 }

```
可以看到属性动画是要运行在looper线程中的，由于属性动画会改变UI，所以这里的looper线程指的是主线程，然后会调用到jni层，之后会调用回来，调用doAnimationFrame方法
```
public final boolean doAnimationFrame(long frameTime) {
     if (mStartTime < 0) {
         // First frame. If there is start delay, start delay count down will happen *after* this
         // frame.
         mStartTime = mReversing ? frameTime : frameTime + (long) (mStartDelay * sDurationScale);
     }

     // Handle pause/resume
     if (mPaused) {
         mPauseTime = frameTime;
         removeAnimationCallback();
         return false;
     } else if (mResumed) {
         mResumed = false;
         if (mPauseTime > 0) {
             // Offset by the duration that the animation was paused
             mStartTime += (frameTime - mPauseTime);
         }
     }

     if (!mRunning) {
         // If not running, that means the animation is in the start delay phase of a forward
         // running animation. In the case of reversing, we want to run start delay in the end.
         if (mStartTime > frameTime && mSeekFraction == -1) {
             // This is when no seek fraction is set during start delay. If developers change the
             // seek fraction during the delay, animation will start from the seeked position
             // right away.
             return false;
         } else {
             // If mRunning is not set by now, that means non-zero start delay,
             // no seeking, not reversing. At this point, start delay has passed.
             mRunning = true;
             startAnimation();
         }
     }

     if (mLastFrameTime < 0) {
         if (mSeekFraction >= 0) {
             long seekTime = (long) (getScaledDuration() * mSeekFraction);
             mStartTime = frameTime - seekTime;
             mSeekFraction = -1;
         }
         mStartTimeCommitted = false; // allow start time to be compensated for jank
     }
     mLastFrameTime = frameTime;
     // The frame time might be before the start time during the first frame of
     // an animation.  The "current time" must always be on or after the start
     // time to avoid animating frames at negative time intervals.  In practice, this
     // is very rare and only happens when seeking backwards.
     final long currentTime = Math.max(frameTime, mStartTime);
     boolean finished = animateBasedOnTime(currentTime);

     if (finished) {
         endAnimation();
     }
     return finished;
 }

```
最后会调用animateBasedOnTime这个方法,他又调用了animateValue这个方法
```
void animateValue(float fraction) {
      fraction = mInterpolator.getInterpolation(fraction);
      mCurrentFraction = fraction;
      int numValues = mValues.length;
      for (int i = 0; i < numValues; ++i) {
          mValues[i].calculateValue(fraction);
      }
      if (mUpdateListeners != null) {
          int numListeners = mUpdateListeners.size();
          for (int i = 0; i < numListeners; ++i) {
              mUpdateListeners.get(i).onAnimationUpdate(this);
          }
      }
  }


```
这里calculateValue这个方法就是计算每一帧动画所对应的属性值，下面看一下在哪儿调用set和get方法的
在这个PropertyValuesHolder类里面，会调用方法
```
private void setupValue方法(Object target, Keyframe kf) {
    if (mProperty != null) {
        Object value = convertBack(mProperty.get(target));
        kf.setValue(value);
    } else {
        try {
            if (mGetter == null) {
                Class targetClass = target.getClass();
                setupGetter(targetClass);
                if (mGetter == null) {
                    // Already logged the error - just return to avoid NPE
                    return;
                }
            }
            Object value = convertBack(mGetter.invoke(target));
            kf.setValue(value);
        } catch (InvocationTargetException e) {
            Log.e("PropertyValuesHolder", e.toString());
        } catch (IllegalAccessException e) {
            Log.e("PropertyValuesHolder", e.toString());
        }
    }
}

```
可以发现是通过反射调用的。
当动画下一帧到来的时候，propertyValuesHolder中的setAnimatedValue方法会将新的属性设置给对象

```
void setAnimatedValue(Object target) {
    if (mProperty != null) {
        mProperty.set(target, getAnimatedValue());
    }
    if (mSetter != null) {
        try {
            mTmpValueArray[0] = getAnimatedValue();
            mSetter.invoke(target, mTmpValueArray);
        } catch (InvocationTargetException e) {
            Log.e("PropertyValuesHolder", e.toString());
        } catch (IllegalAccessException e) {
            Log.e("PropertyValuesHolder", e.toString());
        }
    }
}

```


# 使用动画注意事项
1. OOM问题
这个问题只要出现在帧动画中，图片数量很多就很容易出现OOM,所以建议使用gif来替代
2. 内存泄漏
在属性动画中有一类无限循环动画，这类动画需要在Activity退出的时候及时停止，否则将导致Activity无法释放从而导致内存泄露，通过验证后发现View动画不存在这个问题
3. 兼容性问题
属性动画在3.0一下有兼容性问题，但是现在默认最低版本一般是4.4。这个问题不大。
4. View动画的问题
View动画主要是对View的影像做动画，就是在draw的时候改变矩阵，但是他并没有改变View的状态，因此有时候会出现动画完成后无法隐藏的问题，就是setVisibility(View.Gone)失效的问题，要通过view。clearAnimation（）来清除。
5. 不要使用px，这个大家都知道
6. 动画元素的交互
View动画平移之后还在原地，新位置是无法触发点击事件的，属性动画可以
7. 硬件加速
使用动画过程中建议开启硬件加速，这样会提高动画流畅性
