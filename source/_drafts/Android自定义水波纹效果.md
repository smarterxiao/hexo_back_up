---
title: Android自定义水波纹效果
tags:
- 进阶
- Android自定义控件
categories: android
---

# 引子
最近公司要做一个项目要求的效果是一个水波纹效果。这里是UI给的一个效果图，当然不是要完全一样，没有那个水泡，颜色也不一样。

![Alt text](wave.gif "水波纹效果图")


![Alt text](效果图.png "UI图")



# 分析实现效果的思路

这里把动态水波纹效果拆解一下，分析一下重点：
* 第一点：看到的水波是一个渐变色的，如果是自己实现一个渐变色的填充，应该会和UI图有很大的效果差异。这里采用的方案是UI给一个圆形的渐变填充效果图。

* 第二点：水波的形状，这里发现水波是一个曲线，就想到了贝塞尔曲线，用贝塞尔曲线实现一个水波的path效果，然后通过PorterDuffXfermode与原图合并。剪裁出水波纹的效果

* 第三点：水波会动，这里用属性动画来处理水的波动效果

使用自定义控件来实现这个水波纹效果，控件名称是WaveView继承自View

# 第一步 :水波分层的UI切图，从底层到最上层，并绘制底层


![Alt text](layer_first.png "第一层")


![Alt text](layer_second.png "第二层")


![Alt text](layer_third.png "第三层")


![Alt text](layer_fourth.png "第四层")


![Alt text](layer_fifth.png "第五层")

第一层是底层，用于背景显示
第二层是一个半径小一号的原图
第三层是水波一号
第四层是水波二号
第五层是一个渐变

## 底层背景绘制


```

public class WaveView extends View {

    // 底层
    private Bitmap firstBmp;
    // 第二层
    private Bitmap secondBmp;
    // 第三层
    private Bitmap thirdBmp;
    // 第四层
    private Bitmap fourthBmp;
    // 第五层
    private Bitmap fifthBmp;
    //全部尺寸
    private RectF fullDesRect;


    // 圆bitMap的大小，用于计算比例
    int circleBitMapWidth;
    int circleBitMapHeight;

    // 底层Bitmap的大小，用于计算比例
    int bgBitMapWidth;
    int bgBitMapHeight;

    //控件圆与控件背景的差值的1/2
    int offsetViewHeight;
    int offsetViewWidth;

    // 控件 圆大小，通过 circleViewHeight=bgViewHeight*circleBitMapWidth/bgBitMapWidth得到
    private int circleViewHeight;
    private int circleViewWidth;

    // 控件背景大小
    private int bgViewHeight;
    private int bgViewWidth;

    // 进度百分比
    float precent = 60;

    // 水波高度
    float waveHeight = 0;
    // 水波波峰
    float waveTopHeight = 60;
    //混合模式
    private PorterDuff.Mode mPorterDuffMode = PorterDuff.Mode.DST_IN;

    // 水波纹画笔
    private Paint wavePaint;
    // 混合画笔，用于将两张图片混合
    private Paint mixPaint;
    //水波纹路径
    private Path wavePath;

    private RectF circleDesRect;
    private RectF waveDesRect;
    private Rect thirdSrcRect;
    private Rect fourthSrcRect;
    private PorterDuffXfermode mXfermode;

    // 默认偏移
    private int offsetDefault = -40;
    //第四张图片的默认偏移
    private int offsetFourth = offsetDefault;
    //第三张图片的默认偏移
    private int offsetThird = 0;
    public WaveView(Context context) {
        super(context);
        init();
    }



    public WaveView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WaveView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {

        wavePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        wavePath = new Path();

        wavePaint.setAntiAlias(true);
        wavePaint.setStyle(Paint.Style.FILL);

        mixPaint = new Paint();
        circleDesRect = new RectF();
        fullDesRect = new RectF();
        thirdSrcRect = new Rect();
        fourthSrcRect = new Rect();
        //设置混合模式
        mXfermode = new PorterDuffXfermode(mPorterDuffMode);
        //获取图片
        firstBmp = BitmapFactory.decodeResource(getContext().getResources(), R.mipmap.layer_first);
        //图片大小
        bgBitMapWidth = firstBmp.getWidth();
        bgBitMapHeight = firstBmp.getHeight();
         //获取图片
        secondBmp = BitmapFactory.decodeResource(getContext().getResources(), R.mipmap.layer_second);
               //图片大小
        circleBitMapWidth = secondBmp.getWidth();
        circleBitMapHeight = secondBmp.getHeight();
        thirdBmp = BitmapFactory.decodeResource(getContext().getResources(), R.mipmap.layer_third);
        fourthBmp = BitmapFactory.decodeResource(getContext().getResources(), R.mipmap.layer_fourth);
        fifthBmp = BitmapFactory.decodeResource(getContext().getResources(), R.mipmap.layer_fifth);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //得到控件大小
        bgViewHeight = MeasureSpec.getSize(heightMeasureSpec);
        bgViewWidth = MeasureSpec.getSize(widthMeasureSpec);

        //获取图片要绘制的大小区别于图片大小
        circleViewHeight = bgViewHeight * circleBitMapHeight / bgBitMapHeight;
        circleViewWidth = bgViewWidth * circleBitMapWidth / bgBitMapWidth;
        // 获取图片绘制大小与控件大小差值
        offsetViewHeight = (bgViewHeight - circleViewHeight) / 2;
        offsetViewWidth = (bgViewWidth - circleViewWidth) / 2;
        // 要绘制的图片坐标
        circleDesRect.bottom = bgViewHeight - offsetViewHeight;
        circleDesRect.top = offsetViewHeight;
        circleDesRect.left = offsetViewWidth;
        circleDesRect.right = bgViewWidth - offsetViewWidth;
        // 要绘制的底图位置坐标
        fullDesRect.bottom = bgViewHeight;
        fullDesRect.top = 0;
        fullDesRect.left = 0;
        fullDesRect.right = bgViewWidth;

        // 要绘制的波浪的位置
        waveDesRect.bottom = bgViewHeight - offsetViewHeight;
        waveDesRect.top = offsetViewHeight;
        waveDesRect.left = offsetViewWidth;
        waveDesRect.right = bgViewWidth - offsetViewWidth;
        // 波浪一号绘制的坐标
        thirdSrcRect.bottom = bgViewHeight - 2 * offsetViewHeight;
        thirdSrcRect.top = 0;
        thirdSrcRect.left = offsetThird;
        thirdSrcRect.right = bgViewWidth - 2 * offsetViewWidth + offsetThird;
        // 波浪二号绘制的坐标
        fourthSrcRect.bottom = bgViewHeight - 2 * offsetViewHeight;
        fourthSrcRect.top = 0;
        fourthSrcRect.left = offsetFourth;
        fourthSrcRect.right = bgViewWidth - 2 * offsetViewWidth + offsetFourth;

        // 水波高度
        waveHeight = (100 - precent) * circleViewHeight / 100;

    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 绘制底层
        canvas.drawBitmap(firstBmp, null, fullDesRect, mixPaint);
        //第二层
        canvas.drawBitmap(secondBmp, null, circleDesRect, mixPaint);
    }
}


```

在构造方法中得到五层背景图，然后在`onMeasure`方法中测量要绘制的大小，在`onDraw`中绘制两层底层图片。下面的重点就是在`onDraw`方法中

# 第二步 : 静态水波的实现
## path的绘制
### 贝塞尔曲线
贝塞尔曲线网上的介绍很多了，这里就不重点介绍他，这里分享两个网址，可以直接看到贝塞尔曲线的效果
http://cubic-bezier.com/#.45,0,.21,1
http://myst729.github.io/bezier-curve/

然后是不同阶贝塞尔曲线的效果图

![Alt text](一阶贝塞尔.gif "有起点和终点，无控制点")

![Alt text](二阶贝塞尔.gif "有起点和终点，一个控制点")

![Alt text](三阶贝塞尔.gif "有起点和终点，两个控制点")

![Alt text](四阶贝塞尔.gif "有起点和终点，三个控制点")
贝塞尔曲线有两个非常重要的东西，一个是两个端点，然后是控制点(可以有多个)

## 水波纹剪裁

在确定使用贝塞尔曲线来绘制波浪后，我们来绘制一个path图形。
Android原生提供了二阶贝塞尔曲线和三阶贝塞尔曲线，由于水波是有波峰和波谷的，所以这里采用三阶贝塞尔曲线
这里建议可以使用picPick的曲线功能来查看效果
来看一下水波的效果


![Alt text](水波.png "贝塞尔曲线")

确定了控制点、起点和终点，就可以来撸代码了。
第一步：
创建一个空的图片，大小是控件大小的两倍，这个是为了实现后面水波纹的波动效果
第二步：
使用path绘制一个封闭图形。可以看一下下面的图，封闭的不规则图形。
![Alt text](图像1.png "绘制的形状")
为了实现水波纹的波动，这里创建的不规则图形中包含两个周期的贝塞尔曲线
![Alt text](图像2.png "绘制的形状")

下面上代码
```
 /**
     * 第三层的水波
     *
     * @return 合成的图片
     */
    private Bitmap createWaveBitmap() {

        // 创建画笔
        wavePaint.setColor(Color.parseColor("#ff0000"));
        Paint paint = new Paint();
        paint.setAntiAlias(true);
         // 创建bitmap
        Bitmap bitmap = Bitmap.createBitmap(circleViewWidth * (1 + waveNum), circleViewHeight, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        wavePath.reset();
        waveWidth = circleViewWidth / waveNum / 2;
        // 创建封闭曲线
        wavePath.moveTo(0, waveHeight);

        int factor = 0;
        for (int i = 0; i < waveNum + 1; i++) {
            factor = 2 * waveWidth * i;
            
            wavePath.cubicTo(factor + waveWidth, waveHeight - waveTopHeight, factor + waveWidth, waveHeight + waveTopHeight, circleViewWidth + factor, waveHeight);

        }
        wavePath.lineTo(circleViewWidth * (1 + waveNum), circleViewHeight);

        wavePath.lineTo(0, circleViewHeight);
        // 封闭曲线
        wavePath.close();
        // 绘制这个不规则图形
        canvas.drawPath(wavePath, wavePaint);
        return bitmap;
    }

```

这里要说一下`path.cubicTo`方法，第一个参数是起点，第二个和第三个参数是控制点，第四个参数是终点，这样一个不规则图形就成功绘制了。
下面修改我们的`onDraw`方法
```
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 绘制底层
        canvas.drawBitmap(firstBmp, null, fullDesRect, mixPaint);
        //第二层
        canvas.drawBitmap(secondBmp, null, circleDesRect, mixPaint);

        int count = canvas.saveLayer(circleDesRect, mixPaint);

        //绘制目标图
        canvas.drawBitmap(thirdBmp, null, circleDesRect, mixPaint);
        mixPaint.setXfermode(mXfermode);
        //设置混合模式
        //绘制源图
        canvas.drawBitmap(createWaveBitmap(), thirdSrcRect, waveDesRect, mixPaint);

        //清除混合模式
        mixPaint.setXfermode(null);
        //还原画布
        canvas.restoreToCount(count);

        count = canvas.saveLayer(fullDesRect, mixPaint);

    }
```
这里还用到一个图层的概念，canvas的一些操作这里不会讲，感兴趣可以自己百度一下，或者看一下android群英传关于自定义控件的章节。
这里在说一下混合模式。

![Alt text](Xfermode.jpg "Xfermode")
这个是用来混合图片的，我们的要求是取出两张图片共有的部分，然后用背景图片来填充，所以采用DST_IN模式，这样就可以将两张图片混合在一起。
同理，第四层也这样处理,在将第五层阴影层绘制上去

```
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 绘制底层
        canvas.drawBitmap(firstBmp, null, fullDesRect, mixPaint);
        //第二层
        canvas.drawBitmap(secondBmp, null, circleDesRect, mixPaint);

        int count = canvas.saveLayer(circleDesRect, mixPaint);

        //绘制目标图
        canvas.drawBitmap(thirdBmp, null, circleDesRect, mixPaint);
        mixPaint.setXfermode(mXfermode);
        //设置混合模式
        //绘制源图
        canvas.drawBitmap(createWaveBitmap(), thirdSrcRect, waveDesRect, mixPaint);

        //清除混合模式
        mixPaint.setXfermode(null);
        //还原画布
        canvas.restoreToCount(count);

        count = canvas.saveLayer(fullDesRect, mixPaint);


        //绘制目标图
        canvas.drawBitmap(fourthBmp, null, circleDesRect, mixPaint);


        //设置混合模式
        mixPaint.setXfermode(mXfermode);

        //绘制源图
        canvas.drawBitmap(createWaveBitmap(), fourthSrcRect, waveDesRect, mixPaint);

        //清除混合模式
        mixPaint.setXfermode(null);
        //还原画布
        canvas.restoreToCount(count);


        //第五层
        canvas.drawBitmap(fifthBmp, null, circleDesRect, mixPaint);


    }

```
现在已经实现了水波纹的效果，如下图所示

![Alt text](效果图.png "UI图")

# 第三步 : 水波纹动起来
这里的处理是在`drawBitmap`的时候改变绘制的位置。可以看一下四个参数。第一个参数是要绘制的bitmap，第二个参数是要绘制的地方，就是画布的某个地方，第三个参数是绘制bitmap的哪个部分，第四个参数是画笔、

在创建水波纹不规则图形的时候，已经创建了两个周期的不规则图形，通过改变第三个参数绘制bitmap的坐标，实现水波纹的动态效果

```
 post(new Runnable() {
            @Override
            public void run() {
                valueAnimator = ValueAnimator.ofInt(circleViewWidth * 99);
                valueAnimator.setDuration(1000L * 40);
                valueAnimator.setRepeatCount(-1);
                valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        Integer animatedValue = (Integer) animation.getAnimatedValue();
                        setThirdOffset(animatedValue);
                        Log.i("test","动画执行中");
                    }
                });
                valueAnimator.setInterpolator(new LinearInterpolator());

            }
            valueAnimator.start()
        });
```

```
    private void setThirdOffset(int offset) {

        offsetFourth = offset + offsetDefault;

        fourthSrcRect.left = offsetFourth % circleViewWidth;
        fourthSrcRect.right = fourthSrcRect.left + circleViewWidth;

        offsetThird = offset;
        thirdSrcRect.left = offset % circleViewWidth;
        thirdSrcRect.right = thirdSrcRect.left + circleViewWidth;
        invalidate();
    }
```

下面是完整的代码：

```
/**
 * 水波纹控件
 */
public class DietWaveView extends View {
    //混合模式
    private PorterDuff.Mode mPorterDuffMode = PorterDuff.Mode.DST_IN;

    // 进度百分比
    float precent = 60;

    // 水波高度
    float waveHeight = 0;
    // 水波波峰
    float waveTopHeight = 60;


    // 水波纹画笔
    private Paint wavePaint;
    // 混合画笔，用于将两张图片混合
    private Paint mixPaint;
    //水波纹路径
    private Path wavePath;

    // 控制点
//    private Point wavePoint;

    // 水波数量
    private int waveNum = 1;

    // 圆bitMap的大小，用于计算比例
    int circleBitMapWidth;
    int circleBitMapHeight;

    // 底层Bitmap的大小，用于计算比例
    int bgBitMapWidth;
    int bgBitMapHeight;

    //控件圆与控件背景的差值的1/2
    int offsetViewHeight;
    int offsetViewWidth;

    // 控件 圆大小，通过 circleViewHeight=bgViewHeight*circleBitMapWidth/bgBitMapWidth得到
    private int circleViewHeight;
    private int circleViewWidth;

    // 控件背景大小
    private int bgViewHeight;
    private int bgViewWidth;

    // 每个水波的宽度
    private int waveWidth;

    private RectF circleDesRect;
    private RectF waveDesRect;
    private Rect thirdSrcRect;
    private Rect fourthSrcRect;
    private PorterDuffXfermode mXfermode;
    //    private Bitmap dstBmp;
    // 底层
    private Bitmap firstBmp;
    // 第二层
    private Bitmap secondBmp;
    // 第三层
    private Bitmap thirdBmp;
    // 第四层
    private Bitmap fourthBmp;
    // 第五层
    private Bitmap fifthBmp;
    //全部尺寸
    private RectF fullDesRect;

    // 默认偏移
    private int offsetDefault = -40;

    //第四张图片的默认偏移
    private int offsetFourth = offsetDefault;
    //第三张图片的默认偏移
    private int offsetThird = 0;

    private ValueAnimator valueAnimator;

    public DietWaveView(Context context) {
        super(context);
        init();
    }

    public DietWaveView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DietWaveView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        wavePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        wavePath = new Path();

        wavePaint.setAntiAlias(true);
        wavePaint.setStyle(Paint.Style.FILL);

        mixPaint = new Paint();
//        wavePoint = new Point(0, 0);
        circleDesRect = new RectF();
        fullDesRect = new RectF();
        waveDesRect = new RectF();
        thirdSrcRect = new Rect();
        fourthSrcRect = new Rect();
        mXfermode = new PorterDuffXfermode(mPorterDuffMode);
//        dstBmp = BitmapFactory.decodeResource(getContext().getResources(), R.mipmap.test);

        firstBmp = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.layer_first);
        bgBitMapWidth = firstBmp.getWidth();
        bgBitMapHeight = firstBmp.getHeight();
        secondBmp = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.layer_second);
        circleBitMapWidth = secondBmp.getWidth();
        circleBitMapHeight = secondBmp.getHeight();
        thirdBmp = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.layer_third);
        fourthBmp = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.layer_fourth);
        fifthBmp = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.layer_fifth);



        post(new Runnable() {
            @Override
            public void run() {
                valueAnimator = ValueAnimator.ofInt(circleViewWidth * 99);
//        valueAnimator.setStartDelay(100);
                valueAnimator.setDuration(1000L * 40);
                valueAnimator.setRepeatCount(-1);
//                valueAnimator.setRepeatMode(ValueAnimator.RESTART);
                valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        Integer animatedValue = (Integer) animation.getAnimatedValue();
                        setThirdOffset(animatedValue);
                        Log.i("test","动画执行中");
                    }
                });
                valueAnimator.setInterpolator(new LinearInterpolator());
                valueAnimator.start();
            }
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        bgViewHeight = MeasureSpec.getSize(heightMeasureSpec);
        bgViewWidth = MeasureSpec.getSize(widthMeasureSpec);


        circleViewHeight = bgViewHeight * circleBitMapHeight / bgBitMapHeight;
        circleViewWidth = bgViewWidth * circleBitMapWidth / bgBitMapWidth;
        offsetViewHeight = (bgViewHeight - circleViewHeight) / 2;
        offsetViewWidth = (bgViewWidth - circleViewWidth) / 2;

        circleDesRect.bottom = bgViewHeight - offsetViewHeight;
        circleDesRect.top = offsetViewHeight;
        circleDesRect.left = offsetViewWidth;
        circleDesRect.right = bgViewWidth - offsetViewWidth;

        fullDesRect.bottom = bgViewHeight;
        fullDesRect.top = 0;
        fullDesRect.left = 0;
        fullDesRect.right = bgViewWidth;


        waveDesRect.bottom = bgViewHeight - offsetViewHeight;
        waveDesRect.top = offsetViewHeight;
        waveDesRect.left = offsetViewWidth;
        waveDesRect.right = bgViewWidth - offsetViewWidth;

        thirdSrcRect.bottom = bgViewHeight - 2 * offsetViewHeight;
        thirdSrcRect.top = 0;
        thirdSrcRect.left = offsetThird;
        thirdSrcRect.right = bgViewWidth - 2 * offsetViewWidth + offsetThird;

        fourthSrcRect.bottom = bgViewHeight - 2 * offsetViewHeight;
        fourthSrcRect.top = 0;
        fourthSrcRect.left = offsetFourth;
        fourthSrcRect.right = bgViewWidth - 2 * offsetViewWidth + offsetFourth;


        waveHeight = (100 - precent) * circleViewHeight / 100;

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 绘制底层
        canvas.drawBitmap(firstBmp, null, fullDesRect, mixPaint);
        //第二层
        canvas.drawBitmap(secondBmp, null, circleDesRect, mixPaint);

        int count = canvas.saveLayer(circleDesRect, mixPaint);

        //绘制目标图
        canvas.drawBitmap(thirdBmp, null, circleDesRect, mixPaint);
        mixPaint.setXfermode(mXfermode);
        //设置混合模式
        //绘制源图
        canvas.drawBitmap(createWaveBitmap(), thirdSrcRect, waveDesRect, mixPaint);

        //清除混合模式
        mixPaint.setXfermode(null);
        //还原画布
        canvas.restoreToCount(count);

        count = canvas.saveLayer(fullDesRect, mixPaint);


        //绘制目标图
        canvas.drawBitmap(fourthBmp, null, circleDesRect, mixPaint);


        //设置混合模式
        mixPaint.setXfermode(mXfermode);

        //绘制源图
        canvas.drawBitmap(createWaveBitmap(), fourthSrcRect, waveDesRect, mixPaint);

        //清除混合模式
        mixPaint.setXfermode(null);
        //还原画布
        canvas.restoreToCount(count);


        //第五层
        canvas.drawBitmap(fifthBmp, null, circleDesRect, mixPaint);


    }


    private void setThirdOffset(int offset) {

        offsetFourth = offset + offsetDefault;

        fourthSrcRect.left = offsetFourth % circleViewWidth;
        fourthSrcRect.right = fourthSrcRect.left + circleViewWidth;

        offsetThird = offset;
        thirdSrcRect.left = offset % circleViewWidth;
        thirdSrcRect.right = thirdSrcRect.left + circleViewWidth;
        invalidate();
    }

    // private void setFourthOffset(int offset) {


    //     invalidate();
    // }


//
//
//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//
//        switch (event.getAction()) {
//            case MotionEvent.ACTION_MOVE:
//                wavePoint.x = (int) event.getX();
//                wavePoint.y = (int) event.getY();
//                invalidate();
//                break;
//
//            default:
//        }
//        return true;
//    }


    /**
     * 第三层的水波
     *
     * @return 合成的图片
     */
    private Bitmap createWaveBitmap() {

        wavePaint.setColor(Color.parseColor("#ff0000"));
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        Bitmap bitmap = Bitmap.createBitmap(circleViewWidth * (1 + waveNum), circleViewHeight, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        wavePath.reset();
        waveWidth = circleViewWidth / waveNum / 2;
        wavePath.moveTo(0, waveHeight);

        int factor = 0;
        for (int i = 0; i < waveNum + 1; i++) {
            factor = 2 * waveWidth * i;
//            wavePath.quadTo(wavePoint.x + factor + waveWidth+start, waveTopHeight + waveHeight + wavePoint.y, wavePoint.x + factor + 2 * waveWidth+start, waveHeight);
//            wavePath.quadTo(wavePoint.x + factor + 3 * waveWidth+start, -waveTopHeight + waveHeight + wavePoint.y, wavePoint.x + factor + 4 * waveWidth+start, waveHeight);

            wavePath.cubicTo(factor + waveWidth, waveHeight - waveTopHeight, factor + waveWidth, waveHeight + waveTopHeight, circleViewWidth + factor, waveHeight);

        }
        wavePath.lineTo(circleViewWidth * (1 + waveNum), circleViewHeight);

        wavePath.lineTo(0, circleViewHeight);
        wavePath.close();
        canvas.drawPath(wavePath, wavePaint);
        return bitmap;
    }

}




}

```

这样动态水波纹效果就实现了