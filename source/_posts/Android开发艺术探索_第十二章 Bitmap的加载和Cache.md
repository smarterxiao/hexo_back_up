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

这里讲上面的四个流程使用程序来实现，就产生了如下代码：
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





# Android中缓存策略
缓存策略在Android中有着广泛的使用场景，尤其在图片加载这个场景下，缓存策略就变得更加重要了。考虑一种使用场景：有一批网络图片，需要下载后再用户界面上予以显示，这个场景再PC环境下是很简单的，直接把所有的图片下载到本地再显示即可，但是放到移动设备上面就不一样。不管是Android还是Ios设备，流量对于用户来说都是一种宝贵的资源，由于流量是收费的，所以在应用开发中不能消耗用户的流量，否则这个应用肯定不能被用户所接受。因此必须提供一种解决方案来解决流量问题。
如何避免过多的使用流量呢？那就是本节索要讨论的主题：缓存啦。当程序第一次从网络上加载图片后，就将其缓存到设备上，这样下次使用这张图片就不用再从网络上获取，这样就节约了用户的流量，并且提高了图片的加载速度。这里说一下主流的缓存策略。
当加载一张图片时，先判断时候内存中时候存在这张图片，如果存在，就直接使用这张图片。如果内存不存在这张图片，就通过硬盘缓存来判断是否存在这张图片，如果存在就加载到内存中显示，如果不存在再从网络加载。这样就可以高效节约用户的流量了。
说道缓存策略，其实没有统一的标准，一般来说，缓存策略主要包含缓存的添加、获取和删除这三类操作。如何添加和获取缓存这个比较好理解，那么为什么还要删除缓存呢？这是因为不管是内存缓存还是存储缓存都是有容量大小限制的，因为内存和诸如SD卡之类的存储容量限制，因此在使用的时候就要制定缓存的最大容量。如果缓存满了，但是程序要继续添加缓存，这个时候就要按照一定的策略删除本地或者内存中的缓存。如何定义这种策略就对应不同的算法。例如根据时间来定义缓存的新旧，或者根据缓存访问的次数来定义新旧...每一种算法都有它的优点和缺点，要正确的使用。
目前常用的算法是LRU算法，就是近期最少使用算法，它的核心思想是当缓存满时，会优先淘汰那些最近最少使用的缓存对象。采用LRU算法的缓存有两种：LruCache和DiskLruCache，LruCache用于实现内存缓存，而DiskLruCache充当硬盘缓存，通过这两者完美的结合，就可以很方便的实现一个具有很高实用价值的ImageLoader。这里先介绍一下这两个的使用。



## LruCache
LruCache是Android3.1所提供的一个缓存类，通过support-v4兼容包可以兼容到早起的Android版本。
LruCache是一个泛型类，他内部采用一个LinkedHashMap以强引用的防治存储外界的缓存对象，其提供了get和put方法来完成缓存的获取和添加操作，当缓存满时，LruCache会移除较早使用的缓存对象，然后再讲新的缓存对象添加。这里要明白一个东西：强引用、弱引用和软引用的区别
* 强引用：直接的对象引用
* 软引用：当一个对象只有软引用时，系统内存不足时此对象会被GC回收
* 弱引用：当一个对象只有弱引用时，此对象随时会被GC回收

另外LruCache是线程安全的，下面是LruCache的定义。

LruCache的实现比较简单，我们可以看一下他的源码。
先来看一下LruCache的使用方法，之后在分析一下他的源码
```

        int maxMemory= (int) (Runtime.getRuntime().maxMemory()/1024);
        int cacheSize=maxMemory/8;
        LruCache<String,Bitmap> mMemoryCache=new LruCache<String,Bitmap>(cacheSize){
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return super.sizeOf(key, value);
            }
        };
```
在上面的代码中，只需要提供缓存的总容量大小并重写sizeOf方法即可。sizeOf方法的作用是计算缓存对象的大小，这里大小的单位需要和总容量的单位一致。对于上面的示例代码来说，总容量的大小为当前进程的1/8，单位为kb，而sizeOf方法则完成了Bitmap对象的大小计算。很明显，之所以除以1024也是为了将其单位转换为kb。一些特殊情况下，还需要重写LruCache的entryRemoved方法，Lruchche移除旧缓存时会调用entryRemoved方法，因此可以在entryRemoved中完成一些资源回收的工作(如果需要的话)
除了LruCache的创建以外，还有缓存的获取和添加，这也很简单，从LruCache中获取一个缓存对象

```
     mMemoryCache.get(key)
   
```
向LruCache中添加一个缓存对象
```
     mMemoryCache.put(key,bitmap)
```

LruCache还支持删除操作，通过remove方法即可删除一个指定的缓存对象。可以看到LruCache的实现以及使用都非常简单，虽然很简单，但是任然不影响它具有强大的功能，从Android3.1开始，LruCache就是Android的一部分了。
既然LruCache这么强大，我来分析一下他的源码，吸收一下他的思想。

这里先从他的构造方法开始
```
        LruCache<String,Bitmap> mMemoryCache=new LruCache<String,Bitmap>(cacheSize){
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return super.sizeOf(key, value);
            }

              @Override
            public void resize(int maxSize) {
                super.resize(maxSize);
            }

            @Override
            public void trimToSize(int maxSize) {
                super.trimToSize(maxSize);
            }

            @Override
            protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue) {
                super.entryRemoved(evicted, key, oldValue, newValue);
            }

            @Override
            protected Bitmap create(String key) {
                return super.create(key);
            }
        };

```

我们来看一下他做了什么操作
```
    public LruCache(int maxSize) {
        if (maxSize <= 0) {
            throw new IllegalArgumentException("maxSize <= 0");
        }
        this.maxSize = maxSize;
        this.map = new LinkedHashMap<K, V>(0, 0.75f, true);
    }
```

可以发现LruCache内部使用的是LinkedhashMap
然后要看一下他的put方法，看做了什么操作
```
    public final V put(K key, V value) {
        if (key == null || value == null) {
            throw new NullPointerException("key == null || value == null");
        }

        V previous;
        synchronized (this) {
            putCount++;
            size += safeSizeOf(key, value);
            previous = map.put(key, value);
            if (previous != null) {
                size -= safeSizeOf(key, previous);
            }
        }

        if (previous != null) {
            entryRemoved(false, key, previous, value);
        }

        trimToSize(maxSize);
        return previous;
    }
```



可以发现控制LruCache占用内存大小的方法是`safeSizeOf`和`trimToSize`方法
在添加的时候计算大小
```
    private int safeSizeOf(K key, V value) {
        int result = sizeOf(key, value);
        if (result < 0) {
            throw new IllegalStateException("Negative size: " + key + "=" + value);
        }
        return result;
    }
```
这个是LruCache默认的测量大小的方法，但是可以看到我们在创建LruCache对这个方法进行了重写，由于返回Bitmap的大小
```
    protected int sizeOf(K key, V value) {
        return 1;
    }
```
这样就测量出来当前BitMap占用总内存大小
之后是调用`trimToSize`方法检测内存是否超出

```
    public void trimToSize(int maxSize) {
        while (true) {
            K key;
            V value;
            synchronized (this) {
                if (size < 0 || (map.isEmpty() && size != 0)) {
                    throw new IllegalStateException(getClass().getName()
                            + ".sizeOf() is reporting inconsistent results!");
                }

                if (size <= maxSize) {
                    break;
                }

                Map.Entry<K, V> toEvict = map.eldest();
                if (toEvict == null) {
                    break;
                }

                key = toEvict.getKey();
                value = toEvict.getValue();
                map.remove(key);
                size -= safeSizeOf(key, value);
                evictionCount++;
            }

            entryRemoved(true, key, value, null);
        }
    }
```
如果内存超出，就通过`eldest()`筛选要移除的key值。LinkedHashMap的eldest方法作用是移除最老的值。感兴趣的可以看一下他的源码。这里就不分析了。

这样就可以将超出内存的bitmap移除出内存。

下面看一下get方法
```
  public final V get(K key) {
        if (key == null) {
            throw new NullPointerException("key == null");
        }

        V mapValue;
        synchronized (this) {
            mapValue = map.get(key);
            if (mapValue != null) {
                hitCount++;
                return mapValue;
            }
            missCount++;
        }

        /*
         * Attempt to create a value. This may take a long time, and the map
         * may be different when create() returns. If a conflicting value was
         * added to the map while create() was working, we leave that value in
         * the map and release the created value.
         */

        V createdValue = create(key);
        if (createdValue == null) {
            return null;
        }

        synchronized (this) {
            createCount++;
            mapValue = map.put(key, createdValue);

            if (mapValue != null) {
                // There was a conflict so undo that last put
                map.put(key, mapValue);
            } else {
                size += safeSizeOf(key, createdValue);
            }
        }

        if (mapValue != null) {
            entryRemoved(false, key, createdValue, mapValue);
            return mapValue;
        } else {
            trimToSize(maxSize);
            return createdValue;
        }
    }

```
首先判断key值对应的对象是否存在，如果不存在，就尝试创建一个对象，如果创建失败，就返回null。
这里注意一点，在创建LruCache时，可以重写一个create方法，这个方法就是当key对应的Value值为空时，会调用的创建方法，这个方法默认返回null。

看到这里就可以发现LruCache其实并没有那么复杂，只是对LinkedhashMap的一次包装。

## DiskLruCache