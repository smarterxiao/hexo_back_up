---
title: Android自定义内阴影效果
tags:
- 进阶
- Android自定义控件
categories: android
---
# 引子
最近公司要做一个项目要求的效果是一个圆环百分比效果。
![Alt text](circle_view.png "圆环效果")


# 分析实现效果的思路

第一步是通过path路径来绘制三个一个弧形，然后将这三个弧形拼接在一起
第二步是绘制内阴影

# 第一步：弧形的绘制
这个比较简单，通过path路径就可以绘制出想要的形状
```
/**
 * @author xiao
 * 热量控件
 */
public class DietHeatProportionView extends View {

    private int axungeColor;
    private int axungeWeight;
    private int axungeCoverColor;

    private int carbohydrateColor;
    private int carbohydrateCoverColor;
    private int carbohydrateWeight;

    private int proteinColor;
    private int proteinCoverColor;
    private int proteinWeight;

    private float innerRadius;
    private float outPadding;
    private Paint paint;


    //偏移角度
    private int offsetAngle;
    private int allWeight;
    private float tatalWidth;
    private float tatalHeight;
    private float outerWidth;
    private float outerHeight;

    private float innerWidth;
    private float innerHeight;
    private Path path;
    private float axungeAngle;
    private float carbohydrateAngle;
    private float proteinAngle;
    private Paint proteinPaint;



    public DietHeatProportionView(Context context) {
        super(context);
        init();
    }

    public DietHeatProportionView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs, 0);
        init();
    }

    public DietHeatProportionView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.DietHeatProportionView, defStyleAttr, 0);
        axungeColor = typedArray.getColor(R.styleable.DietHeatProportionView_axunge_color, 0);
        axungeWeight = typedArray.getInteger(R.styleable.DietHeatProportionView_axunge_weight, 3);
        axungeCoverColor = typedArray.getColor(R.styleable.DietHeatProportionView_axunge_cover_color, 0);

        carbohydrateColor = typedArray.getColor(R.styleable.DietHeatProportionView_carbohydrate_color, 0);
        carbohydrateCoverColor = typedArray.getInteger(R.styleable.DietHeatProportionView_carbohydrate_cover_color, 0);
        carbohydrateWeight = typedArray.getColor(R.styleable.DietHeatProportionView_carbohydrate_weight, 3);

        proteinColor = typedArray.getColor(R.styleable.DietHeatProportionView_protein_color, 0);
        proteinCoverColor = typedArray.getColor(R.styleable.DietHeatProportionView_protein_cover_color, 0);
        proteinWeight = typedArray.getInteger(R.styleable.DietHeatProportionView_protein_weight, 3);

        innerRadius = typedArray.getDimension(R.styleable.DietHeatProportionView_inner_radius, 0);
        offsetAngle = typedArray.getInteger(R.styleable.DietHeatProportionView_start_offset, 0);

        outPadding = typedArray.getDimension(R.styleable.DietHeatProportionView_out_padding, 0);
        typedArray.recycle();


        init();
    }


    private void init() {
        axungeWeight=3;
        carbohydrateWeight=3;
        proteinWeight=3;
        proteinPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);

        proteinPaint.setColor(Color.parseColor("#ff0000"));
        path = new Path();
        paint = new Paint();

        //第一个控件的位置
        allWeight = axungeWeight + carbohydrateWeight + proteinWeight;
        axungeAngle = ((1f * axungeWeight / allWeight) * 360);
        carbohydrateAngle = ((1f * carbohydrateWeight / allWeight) *360);
        proteinAngle = 360 - axungeAngle - carbohydrateAngle;

        Log.i("test allWeight  :",allWeight+"");
    }

    private void calculatePoint(float angleOffset, float angle) {


    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        tatalWidth = MeasureSpec.getSize(widthMeasureSpec);
        tatalHeight = MeasureSpec.getSize(heightMeasureSpec);

        outerWidth = tatalWidth - outPadding;
        outerHeight = tatalHeight - outPadding;

        innerWidth = outerWidth - innerRadius;
        innerHeight = outerHeight - innerRadius;
    }


     @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        RectF rectF = new RectF(0, 0, tatalWidth, tatalHeight);
        RectF rectF1 = new RectF(60, 60, tatalWidth-60, tatalHeight-60);


        proteinPaint.setAntiAlias(true);
        proteinPaint.setStrokeWidth(1);
        proteinPaint.setStyle(Paint.Style.FILL);
        proteinPaint.setColor(Color.parseColor("#49e8cd"));
        path.addArc(rectF, offsetAngle, axungeAngle + offsetAngle);

        Log.i("test",offsetAngle+"");
        Log.i("test",axungeAngle + offsetAngle+"");
        path.arcTo(rectF1, offsetAngle+axungeAngle , -axungeAngle);
        path.close();

        canvas.drawPath(path,proteinPaint);



        proteinPaint.reset();
        proteinPaint.setColor(Color.parseColor("#c7fff5"));
        path.reset();
        path.addArc(rectF, offsetAngle+axungeAngle, carbohydrateAngle);

        Log.i("test",offsetAngle+"");
        Log.i("test",axungeAngle + offsetAngle+"");
        path.arcTo(rectF1, offsetAngle+axungeAngle+carbohydrateAngle , -carbohydrateAngle);

        path.close();
        canvas.drawPath(path,proteinPaint);


        proteinPaint.setColor(Color.parseColor("#6ddced"));
        path.reset();
        path.addArc(rectF, offsetAngle+axungeAngle+carbohydrateAngle, proteinAngle);

        Log.i("test",offsetAngle+"");
        Log.i("test",axungeAngle + offsetAngle+"");
        path.arcTo(rectF1, axungeAngle + offsetAngle+carbohydrateAngle+proteinAngle , -proteinAngle);

        path.close();
        canvas.drawPath(path,proteinPaint);


    }

}
```
通过path可以绘制一个环形

# 第二步：内阴影的绘制
这里内阴影使用的是BlurMaskFilter这个面具，他可以生成一个内阴影。但是不能控制内阴影的颜色。所以这里采取的方案是绘制出内阴影之后，取出Bitmap的透明度，然后重新绘制一个Bitmap，这样就可以得到想要的阴影了。

```
/**
 * @author xiao
 * 热量控件
 */
public class DietHeatProportionView extends View {

    private int axungeColor;
    private int axungeWeight;
    private int axungeCoverColor;

    private int carbohydrateColor;
    private int carbohydrateCoverColor;
    private int carbohydrateWeight;

    private int proteinColor;
    private int proteinCoverColor;
    private int proteinWeight;

    private float innerRadius;
    private float outPadding;
    private Paint paint;


    //偏移角度
    private int offsetAngle;
    private int allWeight;
    private float tatalWidth;
    private float tatalHeight;
    private float outerWidth;
    private float outerHeight;

    private float innerWidth;
    private float innerHeight;
    private Path path;
    private float axungeAngle;
    private float carbohydrateAngle;
    private float proteinAngle;
    private Paint proteinPaint;



    public DietHeatProportionView(Context context) {
        super(context);
        init();
    }

    public DietHeatProportionView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs, 0);
        init();
    }

    public DietHeatProportionView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.DietHeatProportionView, defStyleAttr, 0);
        axungeColor = typedArray.getColor(R.styleable.DietHeatProportionView_axunge_color, 0);
        axungeWeight = typedArray.getInteger(R.styleable.DietHeatProportionView_axunge_weight, 3);
        axungeCoverColor = typedArray.getColor(R.styleable.DietHeatProportionView_axunge_cover_color, 0);

        carbohydrateColor = typedArray.getColor(R.styleable.DietHeatProportionView_carbohydrate_color, 0);
        carbohydrateCoverColor = typedArray.getInteger(R.styleable.DietHeatProportionView_carbohydrate_cover_color, 0);
        carbohydrateWeight = typedArray.getColor(R.styleable.DietHeatProportionView_carbohydrate_weight, 3);

        proteinColor = typedArray.getColor(R.styleable.DietHeatProportionView_protein_color, 0);
        proteinCoverColor = typedArray.getColor(R.styleable.DietHeatProportionView_protein_cover_color, 0);
        proteinWeight = typedArray.getInteger(R.styleable.DietHeatProportionView_protein_weight, 3);

        innerRadius = typedArray.getDimension(R.styleable.DietHeatProportionView_inner_radius, 0);
        offsetAngle = typedArray.getInteger(R.styleable.DietHeatProportionView_start_offset, 0);

        outPadding = typedArray.getDimension(R.styleable.DietHeatProportionView_out_padding, 0);
        typedArray.recycle();


        init();
    }


    private void init() {
        axungeWeight=3;
        carbohydrateWeight=3;
        proteinWeight=3;
        proteinPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);

        proteinPaint.setColor(Color.parseColor("#ff0000"));
        path = new Path();
        paint = new Paint();

        //第一个控件的位置
        allWeight = axungeWeight + carbohydrateWeight + proteinWeight;
        axungeAngle = ((1f * axungeWeight / allWeight) * 360);
        carbohydrateAngle = ((1f * carbohydrateWeight / allWeight) *360);
        proteinAngle = 360 - axungeAngle - carbohydrateAngle;

        Log.i("test allWeight  :",allWeight+"");
    }

    private void calculatePoint(float angleOffset, float angle) {


    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        tatalWidth = MeasureSpec.getSize(widthMeasureSpec);
        tatalHeight = MeasureSpec.getSize(heightMeasureSpec);

        outerWidth = tatalWidth - outPadding;
        outerHeight = tatalHeight - outPadding;

        innerWidth = outerWidth - innerRadius;
        innerHeight = outerHeight - innerRadius;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

//        canvas.save();

//        canvas.translate(tatalWidth / 2, tatalHeight / 2);

        RectF rectF = new RectF(0, 0, tatalWidth, tatalHeight);
        RectF rectF1 = new RectF(60, 60, tatalWidth-60, tatalHeight-60);


        proteinPaint.setAntiAlias(true);
        proteinPaint.setStrokeWidth(1);
        proteinPaint.setStyle(Paint.Style.FILL);
        proteinPaint.setColor(Color.parseColor("#c7fff5"));
        path.addArc(rectF, offsetAngle, axungeAngle + offsetAngle);

        Log.i("test",offsetAngle+"");
        Log.i("test",axungeAngle + offsetAngle+"");
        path.arcTo(rectF1, offsetAngle+axungeAngle , -axungeAngle);
        path.close();

//        proteinPaint.setMaskFilter(new EmbossMaskFilter(40, BlurMaskFilter.Blur.INNER));
        canvas.drawPath(path,proteinPaint);


//        paint.setShader(new RadialGradient(0,0,1000,Color.parseColor("#ff0000"),Color.parseColor("#00ffff"), Shader.TileMode.CLAMP));
//
//        paint.setShader(new LinearGradient(0,0,1000,1000,Color.RED,Color.BLUE, Shader.TileMode.CLAMP));

        setLayerType(LAYER_TYPE_SOFTWARE, null);
        paint.setColor(Color.parseColor("#49e8cd"));
        paint.setAntiAlias(true);
        paint.setStrokeWidth(20);
        paint.setStyle(Paint.Style.FILL);

//        paint.setShadowLayer(12, -5, -5,Color.parseColor("#c7fff5"));
        BlurMaskFilter blurMaskFilter = new BlurMaskFilter(20, BlurMaskFilter.Blur.INNER);
        paint.setMaskFilter(blurMaskFilter);
        Bitmap bitmap=Bitmap.createBitmap((int)tatalWidth,(int)tatalHeight, Bitmap.Config.ARGB_8888);
        Canvas mCanvas=new Canvas(bitmap);
        mCanvas.drawPath(path,paint);

        paint.reset();
        paint.setColor(Color.parseColor("#49e8cd"));
        paint.setAntiAlias(true);
        paint.setStrokeWidth(20);
        paint.setStyle(Paint.Style.FILL);
        Bitmap bitmap1 = bitmap.extractAlpha();
//        paint.setColorFilter(new ColorMatrixColorFilter(new ColorMatrix()));
//        ColorMatrixColorFilter colorFilter = new ColorMatrixColorFilter(new float[]{
//                1, 0, 0, 0, 0,
//                0, 1, 0, 0, 0,
//                0, 0, 1, 0, 0,
//                0, 0, 0, 1, 0
//        });
//        paint.setColorFilter(colorFilter);

        canvas.drawBitmap(bitmap1, 0, 0, paint);

//-------------------------------------------------------------------------------------

        proteinPaint.reset();
        proteinPaint.setColor(Color.parseColor("#e7ffe1"));
        path.reset();
        path.addArc(rectF, offsetAngle+axungeAngle, carbohydrateAngle);

        Log.i("test",offsetAngle+"");
        Log.i("test",axungeAngle + offsetAngle+"");
        path.arcTo(rectF1, offsetAngle+axungeAngle+carbohydrateAngle , -carbohydrateAngle);

        path.close();
        canvas.drawPath(path,proteinPaint);



        setLayerType(LAYER_TYPE_SOFTWARE, null);
        paint.setColor(Color.parseColor("#97ff81"));
        paint.setAntiAlias(true);
        paint.setStrokeWidth(20);
        paint.setStyle(Paint.Style.FILL);

//        paint.setShadowLayer(12, -5, -5,Color.parseColor("#c7fff5"));
         blurMaskFilter = new BlurMaskFilter(20, BlurMaskFilter.Blur.INNER);
        paint.setMaskFilter(blurMaskFilter);
         bitmap=Bitmap.createBitmap((int)tatalWidth,(int)tatalHeight, Bitmap.Config.ARGB_8888);
         mCanvas=new Canvas(bitmap);
        mCanvas.drawPath(path,paint);

        paint.reset();
        paint.setColor(Color.parseColor("#97ff81"));
        paint.setAntiAlias(true);
        paint.setStrokeWidth(20);
        paint.setStyle(Paint.Style.FILL);
         bitmap1 = bitmap.extractAlpha();
//        paint.setColorFilter(new ColorMatrixColorFilter(new ColorMatrix()));
//        ColorMatrixColorFilter colorFilter = new ColorMatrixColorFilter(new float[]{
//                1, 0, 0, 0, 0,
//                0, 1, 0, 0, 0,
//                0, 0, 1, 0, 0,
//                0, 0, 0, 1, 0
//        });
//        paint.setColorFilter(colorFilter);

        canvas.drawBitmap(bitmap1, 0, 0, paint);


//-------------------------------------------------------------------------------------


        proteinPaint.setColor(Color.parseColor("#cef8ff"));
        path.reset();
        path.addArc(rectF, offsetAngle+axungeAngle+carbohydrateAngle, proteinAngle);

        Log.i("test",offsetAngle+"");
        Log.i("test",axungeAngle + offsetAngle+"");
        path.arcTo(rectF1, axungeAngle + offsetAngle+carbohydrateAngle+proteinAngle , -proteinAngle);

        path.close();
        canvas.drawPath(path,proteinPaint);




        setLayerType(LAYER_TYPE_SOFTWARE, null);
        paint.setColor(Color.parseColor("#6ddced"));
        paint.setAntiAlias(true);
        paint.setStrokeWidth(20);
        paint.setStyle(Paint.Style.FILL);

//        paint.setShadowLayer(12, -5, -5,Color.parseColor("#c7fff5"));
        blurMaskFilter = new BlurMaskFilter(20, BlurMaskFilter.Blur.INNER);
        paint.setMaskFilter(blurMaskFilter);
        bitmap=Bitmap.createBitmap((int)tatalWidth,(int)tatalHeight, Bitmap.Config.ARGB_8888);
        mCanvas=new Canvas(bitmap);
        mCanvas.drawPath(path,paint);

        paint.reset();
        paint.setColor(Color.parseColor("#6ddced"));
        paint.setAntiAlias(true);
        paint.setStrokeWidth(20);
        paint.setStyle(Paint.Style.FILL);
        bitmap1 = bitmap.extractAlpha();
//        paint.setColorFilter(new ColorMatrixColorFilter(new ColorMatrix()));
//        ColorMatrixColorFilter colorFilter = new ColorMatrixColorFilter(new float[]{
//                1, 0, 0, 0, 0,
//                0, 1, 0, 0, 0,
//                0, 0, 1, 0, 0,
//                0, 0, 0, 1, 0
//        });
//        paint.setColorFilter(colorFilter);

        canvas.drawBitmap(bitmap1, 0, 0, paint);

//        canvas.restore();
    }


}

```

这里有一点需要注意，由于是第二次绘制上去的，因此两个颜色要交换位置。正常情况下，内阴影应该是在整个图片的最上方，但是我获取了内阴影的透明度，所以绘制之后的图bitmap是带有透明度的，这样让圆环的底色是阴影的颜色。在上面绘制一层有透明度的背景，这样底层的内阴影会因为透明度的原因显示出来。

![Alt text](效果图.jpg "效果图")