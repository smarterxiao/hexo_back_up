---
title: Android群英传 第六章 Android图形机制与处理技
date: 2017-11-01 22:17:06
categories: android
top : 106
tags:
- 基础
- Android群英传
---
# 屏幕的信息
##  屏幕大小
单位是英寸，是手机对角线的长度。我们说的苹果是4.5英寸的屏幕是指的是对角线长度
## 分辨率
是手机像素的个数，eg：720*1280 这个说明宽有720个像素，高有1280个像素
## PPI
屏幕对角线的密度，屏幕对角线像素个数/对角线长度
计算方法
  w：屏幕宽分辨率  h：屏幕高分辨率  s：屏幕对角线长度(单位是英寸)  

![Alt text](图像1511094759.png  "结果就是ppi")

## 系统屏幕密度

| 密度       | ldpi      |         mdpi |hdpi|xdpi|xxhdpi|
| ------------- |:-------------:| -----:|-----:|-----:|-----:|
| 密度值     |        120 | 160 |240|320|480|
| 分辨率     | 240*320     |   320*160|480*800|720*1280|1080*1920|

现在开发如果要求不高一般使用xdpi的就可以了，其他的可以放下

## 单位转换

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


# 2D绘图的基础
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

# XML绘图

## bitmap
可以将图片转换为bitmap
```
<?xml version="1.0" encoding="utf-8"?>
<bitmap xmlns:android="http://schemas.android.com/apk/res/android" android:src="@drawable/ic_launcher_background">
</bitmap>
```
## shape
这个比较简单，这个可以使用渐变，如果有渐变一般是美工给一张图片解决... 一般实现不了UI的效果   
## layer
多个shape的集合 类似图层叠加效果
## selector
多个shape的集合，可以实现不同状态的效果显示

# Android 绘图技巧

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


# Android图像处理值色彩特效处理
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

# Android图像处理之图形特效处理
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

# Android 图像处理之画笔特效处理

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


# View的兄弟 SurfaceView
Android是可以在子线程更新UI的，SurfaceView就是要在子线程更新UI。他有双缓冲机制，这个后面讲，用的很少
