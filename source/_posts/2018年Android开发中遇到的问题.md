---
title: 2018年Android开发中遇到的问题
date: 2018-02-26 21:56:17
tags:
- Android
- 问题
- 日记
- 2018年
---

## 2018.2.26
今天遇到了一个问题是在ScrollView中加载长图片，在某些手机上会显示白屏的问题
报错信息是这个
```
16:31:09.49222636OpenGLRendererBitmap too large to be uploaded into a texture (1125x4359, max=4096x4096
```
这个是由于开启了硬件加速之后，android openGL对图片的绘制大小有要求，最大是`max=4096x4096` 所以最简单的解决办法就是关闭硬件加速
这里有四个级别的硬件加速，在小米4手机上面展示1000*2096分辨率的长图片会出现这个问题，或者使用短一点的图片尺寸小于 2750px
* Application
```
<application
    android:hardwareAccelerated="false"
...>
</application>
```

* Activity
```
<application
    android:hardwareAccelerated="true">
    <activity ... />
    <activity android:hardwareAccelerated="false" />
</application>
```
* Window
```
getWindow().setFlags(
   WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
   WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
```
* View
```
myView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
```

## 2018.3.12
1. 今天遇到一个问题是android帧动画
这里是相关的stackoverflow
https://stackoverflow.com/questions/8692328/causing-outofmemoryerror-in-frame-by-frame-animation-in-android

```
public class AnimationsContainer {

    // single instance procedures
    private static AnimationsContainer mInstance;

    private AnimationsContainer() {
    }

    ;

    public static AnimationsContainer getInstance() {
        if (mInstance == null)
            mInstance = new AnimationsContainer();
        return mInstance;
    }

    // animation progress dialog frames
    private int[] mProgressAnimFrames = {};

    // animation splash screen frames
    private int[] mSplashAnimFrames = {
            R.drawable.money_get_00, R.drawable.money_get_01, R.drawable.money_get_02, R.drawable.money_get_03,
            R.drawable.money_get_04, R.drawable.money_get_05, R.drawable.money_get_06, R.drawable.money_get_07,
            R.drawable.money_get_08, R.drawable.money_get_09, R.drawable.money_get_10, R.drawable.money_get_11,
            R.drawable.money_get_12, R.drawable.money_get_13, R.drawable.money_get_14, R.drawable.money_get_15,
            R.drawable.money_get_16, R.drawable.money_get_17, R.drawable.money_get_18, R.drawable.money_get_19,
            R.drawable.money_get_20, R.drawable.money_get_21, R.drawable.money_get_22, R.drawable.money_get_23,
            R.drawable.money_get_24, R.drawable.money_get_25, R.drawable.money_get_26, R.drawable.money_get_27,
            R.drawable.money_get_28, R.drawable.money_get_29, R.drawable.money_get_30, R.drawable.money_get_31,
            R.drawable.money_get_32, R.drawable.money_get_33, R.drawable.money_get_34, R.drawable.money_get_35,
            R.drawable.money_get_36, R.drawable.money_get_37, R.drawable.money_get_38, R.drawable.money_get_39,
            R.drawable.money_get_40, R.drawable.money_get_41, R.drawable.money_get_42, R.drawable.money_get_43,
            R.drawable.money_get_44, R.drawable.money_get_45, R.drawable.money_get_46, R.drawable.money_get_47,
            R.drawable.money_get_48, R.drawable.money_get_49, R.drawable.money_get_50};


    /**
     * @param imageView
     * @return progress dialog animation
     */
    public FramesSequenceAnimation createProgressDialogAnim(ImageView imageView, OnAnimationStoppedListener listener) {
        return new FramesSequenceAnimation(imageView, mProgressAnimFrames, 30, listener);
    }

    /**
     * @param imageView
     * @return splash screen animation
     */
    public FramesSequenceAnimation createSplashAnim(ImageView imageView, OnAnimationStoppedListener listener) {
        return new FramesSequenceAnimation(imageView, mSplashAnimFrames, 30, listener);
    }

    /**
     * AnimationPlayer. Plays animation frames sequence in loop
     */
    public class FramesSequenceAnimation {
        private int[] mFrames; // animation frames
        private int mIndex; // current frame
        private boolean mShouldRun; // true if the animation should continue running. Used to stop the animation
        private boolean mIsRunning; // true if the animation currently running. prevents starting the animation twice
        private SoftReference<ImageView> mSoftReferenceImageView; // Used to prevent holding ImageView when it should be dead.
        private Handler mHandler;
        private int mDelayMillis;
        private OnAnimationStoppedListener mOnAnimationStoppedListener;

        int times = 1;
        private Bitmap mBitmap = null;
        private BitmapFactory.Options mBitmapOptions;

        public FramesSequenceAnimation(ImageView imageView, int[] frames, int fps, OnAnimationStoppedListener listener) {
            mHandler = new Handler();
            mFrames = frames;
            mIndex = -1;
            mSoftReferenceImageView = new SoftReference<ImageView>(imageView);
            mShouldRun = false;
            mIsRunning = false;
            mDelayMillis = 1000 / fps;
            mOnAnimationStoppedListener = listener;
            imageView.setImageResource(mFrames[0]);

            // use in place bitmap to save GC work (when animation images are the same size & type)
            if (Build.VERSION.SDK_INT >= 11) {
                Bitmap bmp = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                int width = bmp.getWidth();
                int height = bmp.getHeight();
                Bitmap.Config config = bmp.getConfig();
                mBitmap = Bitmap.createBitmap(width, height, config);
                mBitmapOptions = new BitmapFactory.Options();
                // setup bitmap reuse options.
                mBitmapOptions.inBitmap = mBitmap;
                mBitmapOptions.inMutable = true;
                mBitmapOptions.inSampleSize = 1;
            }
        }

        private int getNext() {
            mIndex++;
            if (mIndex >= mFrames.length) {

                mIndex = 0;
                times--;
                if (times <= 0) {
                    mShouldRun = false;
                }
            }
            return mFrames[mIndex];
        }

        /**
         * Starts the animation
         */
        public synchronized void start() {
            mSoftReferenceImageView.get().setVisibility(View.VISIBLE);
            mShouldRun = true;
            if (mIsRunning)
                return;

            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    ImageView imageView = mSoftReferenceImageView.get();
                    if (!mShouldRun || imageView == null) {
                        mIsRunning = false;
                        if (mOnAnimationStoppedListener != null) {
                            mOnAnimationStoppedListener.AnimationStopped();
                        }
                        return;
                    }

                    mIsRunning = true;
                    mHandler.postDelayed(this, mDelayMillis);

                    if (imageView.isShown()) {
                        int imageRes = getNext();
                        if (mBitmap != null) { // so Build.VERSION.SDK_INT >= 11
                            Bitmap bitmap = null;
                            try {
                                bitmap = BitmapFactory.decodeResource(imageView.getResources(), imageRes, mBitmapOptions);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            if (bitmap != null) {
                                imageView.setImageBitmap(bitmap);
                            } else {
                                imageView.setImageResource(imageRes);
                                mBitmap.recycle();
                                mBitmap = null;
                            }
                        } else {
                            imageView.setImageResource(imageRes);
                        }
                    }

                }
            };

            mHandler.post(runnable);
        }

        /**
         * Stops the animation
         */
        public synchronized void stop() {
            mShouldRun = false;
        }

        public  boolean getStatus() {
            return mShouldRun;

        }



        public void setPlayTime(int times) {
            if (times <= 0) {
                times = 1;
            }
            this.times = times;
        }

    }
}

```

使用方法
```
    private void playMoneyAnimator() {
        if (anim == null) {
            anim = AnimationsContainer.getInstance().createSplashAnim(imageview, new OnAnimationStoppedListener() {
                @Override
                public void AnimationStopped() {

                }
            });
        }

        if (!anim.getStatus()) {
            anim.setPlayTime(1);
            anim.start();
        }
    }

```

解决方案不用帧动画，通过对imageview内部图片进行切换来处理

2. 这里记录一下经常会忘掉的方法
Gson String 转换为 list的方法不然老是忘了
```
List<aaa> finishBeanTP = gson.fromJson("xxxxxxx", new TypeToken<List<aaa>>() {}.getType());
```

glide 加载GIf的方法

private final String GIF_PATH = "file:///android_asset/licai_refresh_header.gif"; Glide.with(context).load(GIF_PATH).asGif().dontAnimate().centerCrop().diskCacheStrategy(DiskCacheStrategy.SOURCE).into(iv);
## 2018.3.21
如果使用git报错：
fatal: could not read Username for 'https://github.com': No such file or directo
不知道解决办法可以升级一下git版本看一下 ，我是这样解决的
