---
title: Android开发艺术探索 第十二章 Bitmap的加载和Cache
date: 2018-01-27 17:08:20
top : 212
tags:
- 进阶
- Android开发艺术探索
categories: android
---
本章主题是Bitmap的加载和Cache，主要包括三个方面的内容。
首先讲述如何有效地加载一个Bitmap，这是一个很有意义的话题，由于Bitmap的特殊性以及Android对单个应用所施加的内存限制，比如16MB，这导致加载Bitmap的时候很容易出现OOM的情况。因此如何高效加载Bitmap是一个很重要但是很容易被忽视的问题。虽然现在有各种成熟的框架，但是还是要知道这方面的知识。
接着介绍Android中常用的缓存策略，缓存策略是一个通用的思想，可以用在很多场景中，但是实际开发中经常要用到Bitmap做缓存。通过缓存策略，我们不需要每次都从网络上请求图片或者从存储设备中加载图片，这样极大提高了图片的加载效率和用户的使用体验。目前比较常用的图片缓存策略是LruCache和DiskLruCache，其中LruCache常被用作内存缓存，而DiskLruCache常被用作存储缓存。Lru是Least Recently Used 的缩写，即最近最少使用算法。这个算法的核心思想是：当缓存快满时，会淘汰近期使用最少的缓存目标，很显然Lru算法的思想很容易被接受。
最后本章会介绍如何优化列表的卡顿现象，ListView和GradView由于要加载大量的子视图，当用户快速滑动时很容易出现卡顿的现象，因此本章最后针对这个问题给出一些优化建议。
为了更好的讲述上面三个主题，本章提供了一个实例程序，改程序会尝试从网络加载大量的图片并显示在GridView中，可以发现这个程序有很强的实用性，并完善本节的三个主题：图片加载，缓存策略，列表华东的流畅性，通过这个示例程序读者可以很好的理解本章的全部内容并能在实际中灵活应用。

# Bitmap的高效加载
在介绍Bitmap的高效加载之前，先说一下如何加载一个Bitmap，Bitmap在Android中指的是一张图片，可以是png，也可以是jpg等其他常见的图片格式。那么如何加载一个图片呢。

```
     BitmapFactory.decodeFile()
     BitmapFactory.decodeResource()
     BitmapFactory.decodeStream()
     BitmapFactory.decodeByteArray()
```
比较常用的是上面这几个方法，都是通过BitmapFactory来构建，分别对应于从文件、资源、流、字节数组加载图片。其中`decodeFile`和`decodeResource`又间接调用了`decodeStream`的方法，这四类方法最终是在Android底层实现的，对应着BitmapFactory的几个native方法。
如何高效加载Bitmap呢？其实核心思想很简单，就是采用BitmapFactory.options来加载所需要尺寸的图片。这里假设通过ImageView来显示图片，很多时候ImageView并没有图片原始尺寸那么大，这个时候把整个图片加载进来后再设定给ImageView，这显然没有必要的，因为ImageView并没有办法显示原始的图片。通过BitmapFactory.options就可以按一定的采样率来加载缩小后的图片，将缩小后的图片在ImageView中显示，这样就会降低内存占用从而在一定程度上避免出现OOM问题，提高了Bitmap加载时的性能。BitmapFactory提供的加载图片的四类方法都支持BitmapFactory.options参数，通过他们就可以很方便的对一个图片进行采样缩放。
通过BitmapFactory.options来缩放图片，主要是用到了他的inSampleSize参数，即采样率。当inSampleSize为1时，采用后的图片大小为图片原始大小，当inSampleSize大于1时，比如为2，那么采样后的图片的宽和高都是原来图片的1/2，而像素数为原图的1/4，其战友的内存大小也为原图的1/4。那一张1024*1024的图片来说，假定采用ARGB8888格式存储，那么他战友的内存为1024*1024*4 即4MB，如果inSampleSize为2，那么采样后的图片其占用的内存为 512*512*4 即1M。可以发现采用率inSampleSize必须是大于1的证书图片才会有缩小的效果，而且采样率同时作用于宽和高，这将导致缩放后的图片大小以采用率的2次方形式递减，即缩放比例为1/(inSampleSize的2次方)，比如inSampleSize为4，那么缩放比例就是1/16。有一种特殊的情况就是inSampleSize小于1时，其作用相当于1，即五缩放效果。另外最新的官方文档中之处，inSampleSize的取值应总为2的指数，比如 1、2、4、8、16...如果外界传递给系统的inSampleSize不是2的指数，那么系统会向下取整并选择一个最接近2的指数来代替，比如3，系统会选择2来替代。但是经过验证发现这个结论并不是在所有的Android版本上面都成立，因此把他当成一个建议就可以。
考虑到实际情况，比如ImageView的大小是100*100像素，而图片的原始大小为200*200，那么采样率inSampleSize设置为2就可以。但是如果图片大小是100*150呢？如果这个时候采样率还应该选择2，这样缩放后的图片为100*150，任然适合ImageView的，如果采样率为3，那么缩放后的图片会小于ImageView的大小，所以这样图片会被拉伸从而导致图片模糊。
通过采样率可以高效的加载图片，那么到底如何获取采样率呢？获取采样率也很简单，遵循如下流程：

1. 将BitmapFactory.Options的inJustDecodeBounds参数设置为true并加载图片
2. 从BitmapFactory.Options获取图片原始宽高信息，他们对应于outWidth和outHeight参数。
3. 根据采样率的规则并结合目标View的所需大小计算出采样率inSampleSize
4. 将BitmapFactory.Options的inJustDecodeBounds参数设置为false并重新加载图片

经过上面四个步骤，加载出的图片就是最终缩放后的图片，当然也有可能不需要缩放。这里说明一下inJustDecodeBounds参数，当此参数设置true时，BitmapFactory只会解析图片的原始宽高信息，并不会真正加载图片，所以这个操作是轻量级的。另外需要注意的是，这个时候BitmapFactory加载的图片宽高和图片的围着以及程序运行的设备有关，比如同一张图片放在不同的drawable目录下或者不同的屏幕密度的设备上，BitmapFactory会得到不同的结果，之所以会出现这个现象，这和Android的资源加载机制有关，这里不会深入的探讨这个问题，但是大家开发的时候已经用到了这个东西，就是不同的drawable。
将上面的四个流程用程序来实现，就产生了下面的代码：
这里使用的是mipmap下面的图片：

这里的图片是1920*1080的
首先我们不做任何处理，在不同的mipmap文件夹都放置这张图片

![Alt text](meizi.jpg "原始图片")


布局

```
<?xml version="1.0" encoding="utf-8"?>
<android.support.constraint.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity">

<ImageView
    android:id="@+id/iv"
    android:layout_width="100dp"
    android:layout_height="100dp" />

</android.support.constraint.ConstraintLayout>
```

代码
```
public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImageView iv = findViewById(R.id.iv);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.meizi);
        iv.setImageBitmap(bitmap);
    }
}

```


将
```
Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.meizi);
iv.setImageBitmap(bitmap);
```
注释
查看内存信息

![Alt text](原始图片.png "没有加载图片时的内存信息")

可以看到在没有加载bitmap时是20.9m
然后放开注释
查看内存可以得到如下信息
![Alt text](原始内存信息.png "原始内存信息")
是21.2m
可以看到一张图片占用了4.6M的内存
接着先删除xxxhdpi下面的文件，运行看一下内存，是21.2m，大概是之前的4倍左右

![Alt text](xxx.png "删除xxxhdpi的图片之后的内存信息")
这里说明一下，我用的是nexus 6p

这说明如果在默认加载xxxhdpi图片的手机上，如果没有将图片放置在xxxhdpi而是放置在xxhdpi，系统会自动填充这些空余的像素，导致bitmap占用内存扩大，而且是以4的指数级增长的。但是如果是在低分辨率的手机上面加载高清图片，系统会自动的剪裁。

这里讲上面的4个流程使用程序来实现，就产生了如下代码：
```

public class BitmapUtils {
    public  static Bitmap decodeSampleBitmapFromResorce(Resources res,int resId,int reqWidth,int reqHeight){
        BitmapFactory.Options options=new BitmapFactory.Options();
        options.inJustDecodeBounds=true;
        BitmapFactory.decodeResource(res,resId,options);
        options.inSampleSize=calculateInSampleSize(options,reqWidth,reqHeight);
        options.inJustDecodeBounds=false;
        return  BitmapFactory.decodeResource(res,resId,options);
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final  int height=options.outHeight;
        final  int width=options.outWidth;
        int inSampleSize=1;
        if(height>reqHeight||width>reqWidth){
            final int halfHeight=height/2;
            final  int halfWidth=width/2;
            while((halfHeight/inSampleSize)>=reqHeight&&(halfWidth/inSampleSize)>=reqWidth){
                inSampleSize*=2;
            }
        }
        return  inSampleSize;

    }
}

```

有了上面的两个方法，实际使用的时候就非常简单了，比如ImageView所期望的图片大小为100*100像素，这个时候就可以通过如下方式高效加载并显示图片：

```
public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImageView iv = findViewById(R.id.iv);

        Bitmap bitmap = BitmapUtils.decodeSampleBitmapFromResorce(getResources(), R.mipmap.meizi, iv.getMeasuredWidth(), iv.getMeasuredHeight());
        iv.setImageBitmap(bitmap);
    }
}


```

我们来看一下内存的结果
