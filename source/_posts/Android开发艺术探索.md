---
title: Android开发艺术探索
date: 2018-01-27 17:08:20
tags: android进阶第一步
---

>  这个是Android 开发艺术探索的读书笔记，感觉在把android群英传整理之后对android的知识体系有了更加深入的理解，好记性不如烂笔头，还是记录一下。https://github.com/appium/android-apidemos/blob/master/src/io/appium/android/apis/animation/Rotate3dAnimation.java 这个是这本书项目源码 作者的
>  这里看的是Androoid 8.0的代码，和作者的5.0略有不同

# 第一章 Activity的生命周期和启动模式
## Android 的生命周期全面分析
### 正常情况下的生命周期分析

* onCreate : 这个表示Activity正在被创建，是整个生命周期的第一个方法，在这个方法中，我们可以做一些初始化工作，比如调用setContentView去加载界面布局资源，初始化Activity所需要的数据。

* onRestart : 这个表示Activity正在重新启动，一般情况下，当前Activtiy从不可见重新变为可见状态的的时候Onrestart就会被重新调用，这个一般是用户行为导致的。

* onStart : 这个表示Activity正在被启动，即将开始，这个时候Activity已经可见了，但是还没有出现在前台，无法和用户交互，这个时候其实可以理解为Activity已经显示出来了，但是我们看不到


* onResume 表明Activity已经可见了，并且出现在前台并且开始活动，注意这个和onStart的对比，这两个都表示Activity已经可见，但是Onstart的时候Activity还在后台，onResume之后Activity才显示到前台

* onPause 表明Activity正在停止，正常情况下，紧接着onStop会被调用，特殊的，如果这个时候在快速货到当前Activity，那么onResume方法会被调用，这种属于极端情况，用户操作很难重现这一个场景，此时可以做到存储一些数据，停止一些动画的操作。onPause必须先执行完毕，新的Activity的onResume方法才会执行

* onStop 表示Activity即将停止，可以做一些稍微重量级的回收工作，但是不能太耗时

* onDestory 表示Activity即将被销毁，这是Activity生命周期中的最后一个回调，在这里，我们可以做一些回收工作，并最终释放资源

可以看一下下面这张图，这个是Google放出的Activity生命周期的切换过程

![Alt text](activity_lifecycle.png "Android 生命周期")

这里在做一些特殊的说明
1. 针对一个特定的Activity，第一次启动，回调如下：oncreate--> onStart --> onResume

2. 当用户打开新的Activity或者切换到桌面的时候，回调如下：`onPause`->`onStop` 还有一种特殊的情况，如果新的Activity采用透明的主题，那么当前Activity会不会调用`onStop`

3. 当用户再次回到原Activity时，回调如下：onRestart->onStart->onResume

4. 当用户按back键回退时，回调如下:onPause->onStop->onDestory

5. 当Activity被系统回收之后再次打开的时候，生命周期方法回调过程和(1)一样，注意是生命周期方法一样，不代表所有过程都一样。

6. 从整个生命周期来讲，`onCreate`和`onDestory`是配对的。分别表示这Activity的勾线和销毁，并且只有可能有一次调用。从Activity时候可见来讲，`onStart`和`onStop`是配对的，随着用户的操作或者设备屏幕的点亮与熄灭，这两个方法可能会被调用多次，从Activity时候在前台来说，`onResume`和`onPause`是配对的，随着用户的操作或者设备屏幕的点亮和熄灭被调用多次

`onStart`和`onResume`、`onPause`和`onStop`在描述上来讲是差不多的，但是他们描述的东西在维度上是有区别的，`onStart`和`onStop`是从Activity是否可见这个角度来描述的，而`onResume`和`onPause`是用Activity是否位于前台这个角度来描述的，在实际使用中并没有明显的区别。

如果A `Activity` 启动B`Activity` 那么是Bactivity的`onResume`先执行还是A的`onPause`先执行？这个要看一下源码，是先执行A`Activity`的`onPause` 在执行B`Activity`的onResume

### 异常情况下的生命周期方法周期分析

#### 资源相关的系统配置发生改变导致Activity被杀死并重新创建
比较典型的一个例子是手机的横竖屏切换，但是这个时候会问为什么会在旋转屏幕的时候会杀死当前的Activity并创建新的Activity。这个是为了资源的适配。举一个例子，就拿图片来讲，我们把一张图片放在一个drawable目录，为了兼容不同的设备，会在其他目录放一些图片，eg：drawable-mdpi，drawable-hdpi，drawable-land等这样在程序启动的时候，系统会根据不同的设备加载适合的resource资源，比如横屏和竖屏的情况会拿到不同的两张图片(设定了landscape或者portrait状态下的图片)，所以在旋转屏幕的时候会销毁Activity并重新创建，来加载不同的文件。
如果我们不作特殊的处理，就会产生如下的生命周期

![Alt text](图像1517055460.png "Android 异常状态下的生命周期")

在Activity的`onStop`调用之前会先调用`onSaveInstanceState`方法保存当前Activity的状态，但是`onSaveInstanceState`和`onPause`的调用没有既定的时序相关，可以在`onPause`之后调用，也可以在`onPause`之前调用。在Activity创建之后，系统会调用`onRestoreInstanceState`将之前在`onSaveInstanceState`保存的Bundle传递过来，这个时候可以在`onCreate`和`onRestoreInstanceState`中判断当前Activity是否是被重建，如果被重建，Bundle参数就不为空。从时序上来讲，`onRestoreInstanceState`的调用时机在`onStart`之后

同时，我们要知道，在`onSaveInstanceState`和`onRestoreInstanceState`方法中，系统自动为我们做了一些恢复工作。当Activity在异常情况下需要重新创建是，系统会为我们保存当前Activity的数据结构，并且在Activity重启后为我们恢复这些数据，例如EditText,ListView滚动位置...。

具体保存的工作流程是这样的。Activity被意外终止的时候，Activity会调用`onSaveInstanceState`去保存数据，然后Activity会委托Window去保存数据，接着Window会委托他上面的顶级容器去保存数据。顶层容器是一个ViewGroup，一般来说他很有可能是DecordView，最后顶层容器在去意义通过直他的子元素来保存数据，这个时候整个数据的保存过程就完成了。其实可以发现，这是一种典型的委托思想，上曾委托下层，这种思想在Android中有很多的运用，比如View的绘制和事件的分发。可以点击TextView,查看他的`onSaveInstanceState`这个方法就可以知道他会保存那些数据了。

```
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(savedInstanceState!=null){
        String test=    savedInstanceState.getString("extra_test");
            Log.i("Tag",""+test);
        }
    }
    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putString("extra_test","天气");
    }
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        String test=    savedInstanceState.getString("extra_test");
        Log.i("Tag",""+test);
    }
}

```

`onRestoreInstanceState`这个方法被调用，那么他的Bundle一定不为空，但是`onCreate`被调用，如果要用到Bundle，那么要判断非空状态，这两个方法选择任意的都可以恢复数据，但是官方建议我们使用`onRestoreInstanceState`去恢复数据。`onSaveInstanceState`这个方法还有一点要补充的就是如果Activity是正常销毁的，就不会调用这个方法。

#### 资源内存不足导致低优先级的Activity被杀死
这里先来讲一下Android Activity的优先级


|   状态  | 解释     |     优先级，小的优先级高 |
| ------------- |:-------------:| :-------------:|
| 前台Activity        |       正在和用户交互的Activity|1 |
| 非前台，但是可见的Activity        | 比如在Activity中弹出一个对话框，导致Activity可见，但是在后台无法和用户交互   |  2 |
| 后台Activity       |调用了onStop的   |3   |

当系统内存不足的时候，系统会先杀死目标Activity所在的进程，并陆续调用`onSaveInstanceState`和`onRestoreInstanceState`来保存和恢复数据。如果一个进程中没有四大组件在运行，那么这个进程将很容易被杀死，因此，一些后台工作不适合脱离四大组件单独运行在后天中，这样进程很容易被杀死。最好是在Service中进行，从而保持一定的优先级，避免被系统杀死。


Android进程优先级从高到底 https://developer.android.com/guide/components/processes-and-threads.html?hl=zh-cn
* 前台进程
用户当前操作所必需的进程。如果一个进程满足以下任一条件，即视为前台进程：
  * 托管用户正在交互的 Activity（已调用 Activity 的 onResume() 方法）
  * 托管某个 Service，后者绑定到用户正在交互的 Activity
  * 托管正在“前台”运行的 Service（服务已调用 startForeground()）
  * 托管正执行一个生命周期回调的 Service（onCreate()、onStart() 或 onDestroy()）
  * 托管正执行其 onReceive() 方法的 BroadcastReceiver
通常，在任意给定时间前台进程都为数不多。只有在内存不足以支持它们同时继续运行这一万不得已的情况下，系统才会终止它们。 此时，设备往往已达到内存分页状态，因此需要终止一些前台进程来确保用户界面正常响应。

* 可见进程

没有任何前台组件、但仍会影响用户在屏幕上所见内容的进程。 如果一个进程满足以下任一条件，即视为可见进程：
  * 托管不在前台、但仍对用户可见的 Activity（已调用其 onPause() 方法）。例如，如果前台 Activity 启动了一个对话框，允许在其后显示上一 Activity，则有可能会发生这种情况。
  * 托管绑定到可见（或前台）Activity 的 Service。
可见进程被视为是极其重要的进程，除非为了维持所有前台进程同时运行而必须终止，否则系统不会终止这些进程。

* 服务进程
正在运行已使用 startService() 方法启动的服务且不属于上述两个更高类别进程的进程。尽管服务进程与用户所见内容没有直接关联，但是它们通常在执行一些用户关心的操作（例如，在后台播放音乐或从网络下载数据）。因此，除非内存不足以维持所有前台进程和可见进程同时运行，否则系统会让服务进程保持运行状态。

* 后台进程
包含目前对用户不可见的 Activity 的进程（已调用 Activity 的 onStop() 方法）。这些进程对用户体验没有直接影响，系统可能随时终止它们，以回收内存供前台进程、可见进程或服务进程使用。 通常会有很多后台进程在运行，因此它们会保存在 LRU （最近最少使用）列表中，以确保包含用户最近查看的 Activity 的进程最后一个被终止。如果某个 Activity 正确实现了生命周期方法，并保存了其当前状态，则终止其进程不会对用户体验产生明显影响，因为当用户导航回该 Activity 时，Activity 会恢复其所有可见状态。 有关保存和恢复状态的信息，

* 空进程
不含任何活动应用组件的进程。保留这种进程的的唯一目的是用作缓存，以缩短下次在其中运行组件所需的启动时间。 为使总体系统资源在进程缓存和底层内核缓存之间保持平衡，系统往往会终止这些进程。

从服务进程开始，一般是不会被系统杀死的，但是这个还要看国内各种不同Rom的优化了。

如果重新创建Activity，就在清单文件中为activity添加`android：configChanges="orientaion"`

## Activity的启动模式
### Activity的LaunchMode
* standard
标准模式，这个是系统的默认模式。在这种模式下，Activity  A启动了Activity B ，那么B就会进入到A的Activity。如果是使用ApplicationContext去启动standard模式的Activitty就会报错。这个是应为standard模式的activity默认会进入启动它的activity所属的任务栈，但是由于非Activity类型的Context并没有任务栈，所以就有问题了，这个是有就需要在启动的时候之指定FLAG_ACTIVITY_NEW_TASK标记位，这样启动的时候就会为他创建一个新的任务栈。这个时候启动的Activity实际上是一个singleTask模式启动的。
* singleTop
栈顶复用模式。类似standard模式，但是区别是如果这个activity在栈顶，就不会创建。但是会回调他的`onNewItent`方法
* singleTask
栈内复用模式。这是一种单实例模式，在这种模式下，只要Activity在一个栈中存在，那么多次启动此Activity都不会重新创建实例，和singleTop一样，会回调`onNewItent`方法。具体的，当启动一个具有singleTask模式的Activity，比如Activity A，这个时候系统会寻找时候存在A想要的任务栈，如果不存在，就重新创建一个任务栈，然后创建A并把A实例放入栈中。如果存在A所需要的任务栈，那么久这是要看任务栈中有没有实例A，如果有，就把A调到栈顶，并调用`onNewIntent`方法，如果不存在，就创建并把A压入栈中。
举个例子
 比如任务栈S1目前的情况是ABC，这个时候Activity D用singleTask模式请求启动。
 第一种情况。 其所需要的任务栈是S2.由于S2和D实例均不存在，这个时候会创建任务栈S2，并创建D将D压入S2
 第二种情况，假设D所需要的任务栈是S1，那么S1已经存在，就会直接创建D并压入S1
 第三种情况，如果D所需要的任务栈是S1，并且当前任务栈S1的情况为ADBC，那么根据栈内复用原则，就不会创建D，而是把D切换到栈顶，调用其`onNewIntent`方法，同时由于singleTask默认具有clearTop效果，会导致D上面的Activity全部出栈，最终S1中的情况是AD
* singleInstance
单实例模式。这个是对singleTask的加强版，除了具有singleTask所有的属性外，还加强了一点，那就是具有这种模式的Activity只能单独的存在一个任务栈中，换句话说，如果Activity A 具有singleInstance模式，当A启动后，系统会为他创建一个新的Activity任务栈，然后这个A单独存在这个任务栈，由于栈内复用的特性，后续君不悔创建新的activity，除非这个独特的任务栈被系统销毁了。

上面介绍了几种启动模式，这里需要指出的一种情况，假设我们目前有两个任务栈，前台任务栈的情况是AB，后台任务栈的情况是CD，假设CD的启动模式均为singleTask。现在启动D，那么整个后台任务栈就会被切换到前台，这个时候后退列表就变成了ABCD，当用户按back的时候，ABCD会一一出栈。这个时候如果启动的是C，就不一样了，C是singleTask的，会把D出栈，这个时候就只剩下ABC了。

这里有一个概念就是Activity任务栈，这个是什么呢?这个就要从一个参数说起TaskAffinity可以翻译成亲和度，这个参数标记了Activity任务栈的任务栈名。默认情况下所有的Activity所需要的任务栈的名字就是应用的包名。当然，我们可以为美特Activity都单独指定TaskAffinity，这个属性一定不能喝包名相同，否则就相当于没有指定。TaskAffinity属性主要和singleTask启动模式或者allowTaskReparenting属性配对使用，在其他情况下没有意义。另外，任务栈分为浅谈任务和后台任务栈，后台任务栈中的Activity位于暂停状态，用户可以通过切换将后台任务栈再次调到前台。当TaskAffinity和singleTask启动模式配对使用的时候，他是具有改模式的Activity的目前任务栈的名字，待启动的Activity会运行在名字和TaskAffinity相同的任务栈中。当TaskAffinity和allowTaskReparenting结合的时候，这种情况比较复杂，会产生特殊的效果，当一个应用A启动了应用B中的某个Activity，如果这个Activity的allowTaskReparenting属性值为true，那么应用B被启动之后，这个Activity会直接从应用A的任务栈转移到应用B的任务栈，这个还是比较抽象，举个例子：如果现在有两个应用A和B，A启动了B的一个Activity C，然后按Home回到桌面，在点击B进入，这个时候启动的不是B的住Activity，而是会进入到被A启动的Activity，或者说C从A的任务栈转移到了B的任务栈。可以这么理解，由于A启动了C这个时候C只能运行在A的任务栈中，但是C属于B的应用，正常情况下他的TaskAffinity一定和A的任务栈不相同，因为包名不同，所以。当B被启动之后B创建自己的任务栈，这个时候系统发现C原本所想要的任务栈已经被创建了。所以就把C从A的任务栈中移动到B的任务栈中。

如何给Activity指定启动模式呢?有两种方法。
第一种是通过清单文件指定Activity启动模式
```
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.smart.kaifa">

    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/AppTheme">
        <activity android:name=".MainActivity" android:launchMode="singleTask">//在这里为Activity设置启动模式
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />

                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>

</manifest>
```

另外一种是通过在Intent中设置标志位来为Activity指定启动模式

```
   Intent intent=new Intent();
   intent.setClass(MainActivity.this,SecondActivity.class);
   intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
   startActivity(intent);
```
这两种方式都可以为Activity指定启动模式，但是两者还是有区别的，首先第二种的优先级要高于第一种，当两种同时存在的时候以第二种方式为准，其次上述两种启动方式的限定范围上有所不同，比如，第一种方式无法直接为Activity设置Flag_ACTIVITY_CLEAR_TOP标识，而第二种方式无法为Activity指定singleInstance模式。

```
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        View viewById = findViewById(R.id.test);


        viewById.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent();
                intent.setClass(MainActivity.this,MainActivity.class);
                startActivity(intent);
            }
        });
    }
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.i("哇哈哈","营养快线");
    }
}

```
点击四次
然后使用`adb shell dumpsys activity activitys`命令查看

```
ACTIVITY MANAGER ACTIVITIES (dumpsys activity activities)
Display #0 (activities from top to bottom):
  Stack #1:
  mFullscreen=true
  mBounds=null
    Task id #2243
    mFullscreen=true
    mBounds=null
    mMinWidth=-1
    mMinHeight=-1
    mLastNonFullscreenBounds=null
      TaskRecord{38f09c #2243 A=com.smart.kaifa U=0 StackId=1 sz=1}
      Intent { act=android.intent.action.MAIN cat=[android.intent.category.LAUNCHER] flg=0x10200000 cmp=com.smart.kaifa/.MainActivity bnds=[443,1427][667,1651] (has extras) }
        Hist #0: ActivityRecord{3ab3e8d u0 com.smart.kaifa/.MainActivity t2243}
          Intent { act=android.intent.action.MAIN cat=[android.intent.category.LAUNCHER] flg=0x10200000 cmp=com.smart.kaifa/.MainActivity bnds=[443,1427][667,1651] (has extras) }
          ProcessRecord{6362aa5 27067:com.smart.kaifa/u0a190}

    Running activities (most recent first):
      TaskRecord{38f09c #2243 A=com.smart.kaifa U=0 StackId=1 sz=1}//size是1
        Run #0: ActivityRecord{3ab3e8d u0 com.smart.kaifa/.MainActivity t2243}//创建了一次

    mResumedActivity: ActivityRecord{3ab3e8d u0 com.smart.kaifa/.MainActivity t2243}



```

这个时候去掉singleTask，我们点击3下，之后看一下结果

```

ACTIVITY MANAGER RECENT TASKS (dumpsys activity recents)
  Recent tasks:
  * Recent #0: TaskRecord{b808e48 #2245 A=com.smart.kaifa U=0 StackId=1 sz=3}
  * Recent #1: TaskRecord{260611 #2237 I=com.google.android.googlequicksearchbox/com.google.android.launcher.GEL U=0 StackId=0 sz=1}
  * Recent #2: TaskRecord{8d1157c #2123 A=com.android.systemui U=0 StackId=5 sz=1}
  * Recent #3: TaskRecord{82ff376 #2233 A=com.google.android.packageinstaller U=0 StackId=-1 sz=0}
  * Recent #4: TaskRecord{4a7ede4 #2230 I=com.android.settings/.Settings$AppDrawOverlaySettingsActivity U=0 StackId=-1 sz=0}
  * Recent #5: TaskRecord{853f24d #2185 A=com.android.vending U=0 StackId=-1 sz=0}
  * Recent #6: TaskRecord{41fd102 #2184 A=com.github.shadowsocks U=0 StackId=-1 sz=0}
  * Recent #7: TaskRecord{cc6ca13 #2162 A=com.clov4r.android.nil U=0 StackId=-1 sz=0}
  * Recent #8: TaskRecord{fe80450 #2180 A=com.google.android.googlequicksearchbox U=0 StackId=-1 sz=0}
  * Recent #9: TaskRecord{cfa6649 #2161 A=com.smart.myapplication U=0 StackId=-1 sz=0}
  * Recent #10: TaskRecord{73e0af7 #2159 A=com.android.chrome U=0 StackId=-1 sz=0}
  * Recent #11: TaskRecord{73dfb4e #2157 I=com.android.settings/.deviceinfo.UsbModeChooserActivity U=0 StackId=-1 sz=0}
  * Recent #12: TaskRecord{449f46f #2153 A=com.qihoo.explorer U=0 StackId=-1 sz=0}
  * Recent #13: TaskRecord{f7a1e05 #2002 A=com.google.android.deskclock U=0 StackId=-1 sz=0}
  * Recent #14: TaskRecord{9027e5a #1993 A=com.android.documentsui U=0 StackId=-1 sz=0}
  * Recent #15: TaskRecord{c2b688b #1474 A=com.google.android.GoogleCamera U=0 StackId=-1 sz=0}
  * Recent #16: TaskRecord{ed80d68 #1002 A=com.google.android.apps.docs U=0 StackId=-1 sz=0}
  * Recent #17: TaskRecord{c839581 #811 A=com.google.android.play.games U=0 StackId=-1 sz=0}
  * Recent #18: TaskRecord{e5a2626 #726 A=com.google.android.calculator U=0 StackId=-1 sz=0}
  * Recent #19: TaskRecord{b660267 #2090 A=com.xiaoji.emulator U=0 StackId=-1 sz=0}
  * Recent #20: TaskRecord{1d49814 #2086 A=com.google.android.apps.messaging U=0 StackId=-1 sz=0}
  * Recent #21: TaskRecord{15308bd #2061 A=cn.collect.lihua.workflow U=0 StackId=-1 sz=0}

ACTIVITY MANAGER ACTIVITIES (dumpsys activity activities)
Display #0 (activities from top to bottom):
  Stack #1:
  mFullscreen=true
  mBounds=null
    Task id #2245
    mFullscreen=true
    mBounds=null
    mMinWidth=-1
    mMinHeight=-1
    mLastNonFullscreenBounds=null
      TaskRecord{b808e48 #2245 A=com.smart.kaifa U=0 StackId=1 sz=3} //这里的size是3
      Intent { act=android.intent.action.MAIN cat=[android.intent.category.LAUNCHER] flg=0x10000000 cmp=com.smart.kaifa/.MainActivity }
        Hist #2: ActivityRecord{f3f1b1b u0 com.smart.kaifa/.MainActivity t2245}
          Intent { cmp=com.smart.kaifa/.MainActivity }
          ProcessRecord{4c6b4e1 27837:com.smart.kaifa/u0a190}
        Hist #1: ActivityRecord{2745f46 u0 com.smart.kaifa/.MainActivity t2245}
          Intent { cmp=com.smart.kaifa/.MainActivity }
          ProcessRecord{4c6b4e1 27837:com.smart.kaifa/u0a190}
        Hist #0: ActivityRecord{1a44344 u0 com.smart.kaifa/.MainActivity t2245}
          Intent { act=android.intent.action.MAIN cat=[android.intent.category.LAUNCHER] flg=0x10000000 cmp=com.smart.kaifa/.MainActivity }
          ProcessRecord{4c6b4e1 27837:com.smart.kaifa/u0a190}

    Running activities (most recent first):
      TaskRecord{b808e48 #2245 A=com.smart.kaifa U=0 StackId=1 sz=3}    //这里的size是3
        Run #2: ActivityRecord{f3f1b1b u0 com.smart.kaifa/.MainActivity t2245}//创建了3次
        Run #1: ActivityRecord{2745f46 u0 com.smart.kaifa/.MainActivity t2245}
        Run #0: ActivityRecord{1a44344 u0 com.smart.kaifa/.MainActivity t2245}

    mResumedActivity: ActivityRecord{f3f1b1b u0 com.smart.kaifa/.MainActivity t2245}

  Stack #0:
  mFullscreen=true
  mBounds=null
    Task id #2237
    mFullscreen=true
    mBounds=null
    mMinWidth=-1
    mMinHeight=-1
    mLastNonFullscreenBounds=null
      TaskRecord{260611 #2237 I=com.google.android.googlequicksearchbox/com.google.android.launcher.GEL U=0 StackId=0 sz=1}
      Intent { act=android.intent.action.MAIN cat=[android.intent.category.HOME] flg=0x10000100 cmp=com.google.android.googlequicksearchbox/com.google.android.launcher.GEL }
        Hist #0: ActivityRecord{29147a2 u0 com.google.android.googlequicksearchbox/com.google.android.launcher.GEL t2237}
          Intent { act=android.intent.action.MAIN cat=[android.intent.category.HOME] flg=0x10000100 cmp=com.google.android.googlequicksearchbox/com.google.android.launcher.GEL }
          ProcessRecord{18f4383 3903:com.google.android.googlequicksearchbox/u0a43}

    Running activities (most recent first):
      TaskRecord{260611 #2237 I=com.google.android.googlequicksearchbox/com.google.android.launcher.GEL U=0 StackId=0 sz=1}
        Run #0: ActivityRecord{29147a2 u0 com.google.android.googlequicksearchbox/com.google.android.launcher.GEL t2237}

  Stack #5:
  mFullscreen=true
  mBounds=null
    Task id #2123
    mFullscreen=true
    mBounds=null
    mMinWidth=-1
    mMinHeight=-1
    mLastNonFullscreenBounds=null
      TaskRecord{8d1157c #2123 A=com.android.systemui U=0 StackId=5 sz=1}
      Intent { flg=0x10804000 cmp=com.android.systemui/.recents.RecentsActivity }
        Hist #0: ActivityRecord{bd08853 u0 com.android.systemui/.recents.RecentsActivity t2123}
          Intent { flg=0x10804000 cmp=com.android.systemui/.recents.RecentsActivity }
          ProcessRecord{ac18393 4823:com.android.systemui/u0a39}

    Running activities (most recent first):
      TaskRecord{8d1157c #2123 A=com.android.systemui U=0 StackId=5 sz=1}
        Run #0: ActivityRecord{bd08853 u0 com.android.systemui/.recents.RecentsActivity t2123}
```

看一下 Running activities (most recent first): 这个后面跟着的东西
这里表明，现在有3个任务栈，一个任务栈的taskAffinity是com.smart.kaifa，一个任务栈的taskAffinity是com.android.systemui，另外一个任务栈的taskAffinity是com.google.android.googlequicksearchbox，这里可以看到com.smart.kaifa中有3个MainActivity，这里就印证了上面的内容

还有一个singleTask要说的
这里创建3个Activity：A、B、C分别对应MainActivity，SecondActivity，ThirdActivity
看一下清单文件的配置：
```
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.smart.kaifa">

    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/AppTheme">
        <activity android:name=".MainActivity">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />

                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
        <activity
            android:taskAffinity="com.xiao.x"
            android:name=".SecondActivity"
            android:label="@string/title_activity_second"
            android:launchMode="singleTask"
            android:theme="@style/AppTheme.NoActionBar" />
        <activity
            android:name=".ThirdActivity"
            android:taskAffinity="com.xiao.x"
            android:launchMode="singleTask"
            android:label="@string/title_activity_third"
            android:theme="@style/AppTheme.NoActionBar"></activity>
    </application>

</manifest>
```

这里将SecondActivity和ThirdActivity设置为singleTask，并且taskAffinity设置为com.xiao.x，假定MainActivity为A，SecondActivity为B，ThirdActivity为C，然后A启动B，B启动C，C启动B。
先分析一下。
A是Standard模式。所以A启动B的时候，由于B是singleTask并且taskAffinity是com.xiao.x，这个时候com.xiao.x任务栈不存在，就会创建这个任务栈，然后B启动C，C的taskAffinity是com.xiao.x，已经存在，只需要创建一个C放入com.xiao.x这个任务栈中
看一下 dumpsys activity的结果

```
Stack #1:
 mFullscreen=true
 mBounds=null
   Task id #2248
   mFullscreen=true
   mBounds=null
   mMinWidth=-1
   mMinHeight=-1
   mLastNonFullscreenBounds=null
     TaskRecord{520a424 #2248 A=com.xiao.x U=0 StackId=1 sz=2}
     Intent { flg=0x10000000 cmp=com.smart.kaifa/.SecondActivity }
       Hist #1: ActivityRecord{bcfad93 u0 com.smart.kaifa/.ThirdActivity t2248}
         Intent { flg=0x10000000 cmp=com.smart.kaifa/.ThirdActivity }
         ProcessRecord{7c51142 28971:com.smart.kaifa/u0a190}
       Hist #0: ActivityRecord{5fe4c2b u0 com.smart.kaifa/.SecondActivity t2248}
         Intent { flg=0x10000000 cmp=com.smart.kaifa/.SecondActivity }
         ProcessRecord{7c51142 28971:com.smart.kaifa/u0a190}
   Task id #2246
   mFullscreen=true
   mBounds=null
   mMinWidth=-1
   mMinHeight=-1
   mLastNonFullscreenBounds=null
     TaskRecord{8c10b8d #2246 A=com.smart.kaifa U=0 StackId=1 sz=1}
     Intent { act=android.intent.action.MAIN cat=[android.intent.category.LAUNCHER] flg=0x10000000 cmp=com.smart.kaifa/.MainActivity }
       Hist #0: ActivityRecord{e2d5234 u0 com.smart.kaifa/.MainActivity t2246}
         Intent { act=android.intent.action.MAIN cat=[android.intent.category.LAUNCHER] flg=0x10000000 cmp=com.smart.kaifa/.MainActivity }
         ProcessRecord{7c51142 28971:com.smart.kaifa/u0a190}

   Running activities (most recent first):
     TaskRecord{520a424 #2248 A=com.xiao.x U=0 StackId=1 sz=2}  //任务栈 com.xiao.x
       Run #2: ActivityRecord{bcfad93 u0 com.smart.kaifa/.ThirdActivity t2248}
       Run #1: ActivityRecord{5fe4c2b u0 com.smart.kaifa/.SecondActivity t2248}
     TaskRecord{8c10b8d #2246 A=com.smart.kaifa U=0 StackId=1 sz=1} //任务栈com.smart.kaifa
       Run #0: ActivityRecord{e2d5234 u0 com.smart.kaifa/.MainActivity t2246}

   mResumedActivity: ActivityRecord{bcfad93 u0 com.smart.kaifa/.ThirdActivity t2248}
```

现在对Activity的启动模式有了更加深入的了解

### Activity的Flags
这里主要分析一些比较常用的标志位
* FLAG_ACTIVITY_NEW_TASK
这个标记为的作用是为Activity指定singleTask启动模式，其效果和在XML中指定该启动模式相同
* FLAG_ACTIVITY_SINGLE_TOP
这个标记位的作用是为activity指定SingleTop启动模式，其效果和在XML中指定该启动模式相同
* FLAG_ACTIVITY_CLEAR_TOP
具有此标记位的Activity，当他在启动的时候，同一个任务栈中所有位于它上面的Activity都要出栈，这个标记位一般会和singleTask启动模式一起出现，在这种情况下，如果Activity实例已经存在，会调用OnNewIntent，如果被启动的Activity是采用standard模式，那么连同他上面的Activity都要出栈，singleTask默认自带FLAG_ACTIVITY_CLEAR_TOP效果
* FLAG_ACTIVITY_EXCLUDE_FROM_DECENTS
具有这个标记的Activity不会出现在历史Activity列表中，当某些情况下我们不希望用户通过历史列表回到我们的Activity的时候这个标记比较有用，他等同于在XML中指定Activity的属性`android:excludeFromRecents=true`

## Intent的匹配规则
Activity的启动分为两种，显示调用和隐式调用。隐式调用最主要是通过IntentFilter中设置过滤信息，只有匹配过滤信息才能启动Activity

```

<activity android:name=".MainActivity">
       <intent-filter>
           <action android:name="android.intent.action.MAIN" />

           <category android:name="android.intent.category.LAUNCHER" />
       </intent-filter>
   </activity>
   <activity
       android:name=".SecondActivity"
       android:label="@string/title_activity_second"
       android:launchMode="singleTask"
       android:taskAffinity="com.xiao.x"
       android:theme="@style/AppTheme.NoActionBar">

       <intent-filter>
           <category android:name="android.intent.category.DEFAULT"/>
           <action android:name="com.xxx" />
           <action android:name="com.xiao" />
           <category android:name="com.xxzz" />
           <data android:mimeType="text/plain" />
       </intent-filter>
```
为了匹配过滤列表，需要同时匹配过滤列表中的action,category,data信息，否则匹配失败，一个过滤列表中的action，category和data可以有多个，所有的action，category，data分别构成不同的类别，同一个类别的信息共同约束当前类别的匹配过程，只有一个Intent同时匹配action类别，category类别，data类别才算完全匹配，只有完全匹配成功才能成功启动目标Activity。另外一点，一个Activity中可以有多个intent-filter，一个intent只要能匹配任何一组intent-filter即可成功启动对应的Activity。

规则匹配详情
* action的匹配规则
action是一个字符串，系统预定义了一些action，同时我们也可以在应用中自定义action，action的匹配规则就是Intent中的aciton必须能够和过滤规则中的action匹配，这里的匹配指的是action的字符串值完全一样。一个过滤规则中可以有多个action，那么只要Intent中的action中能够与过滤规则中任何一个action相同，即可匹配成功。针对上面的过滤规则，只要我们的Intent中的action为com.xxx或者为com.xiao就可以匹配成功。需要注意的是如果Intent中没有指定action，那么匹配失败。总结一下就是action的匹配要求Intent中的action存在且必须和过滤规则中的其中一个action相同，这里需要注意他和category匹配过着的不同，另外，action区分大小写

* category匹配规则
category是一个字符串，系统已经预定义了一些category，同时我们也可以在应用中自动自category。category的匹配规则和action不同，他要求Intent中如果含有category，那么所有的category都必须和过滤规则中的其中一个category相同。换句话说，Intent中如果出现了category，不管有几个category，对于每个category来讲，它必须是过滤规则中已经定义了的category。当然，Intent中可以没有category，如果没有category的话，按照上面的描述。这个Intent任然可以匹配成功。注意他和action的区别，action是只要有一个满足就可以，而category必须是都要满足。那么为什么不设置category也可以匹配成功呢？原因是系统在startActivity的时候或者startActivityForResult的时候会默认为加上Intent加上"android.intent.category.DEFAULT"这个category，所以这个category就可以匹配前面的过滤规则中的第一个。同时为了我们隐式调用，要在intent-filter中添加一个category。

* data的匹配规则
data的匹配规则和action类似，如果过滤规则中定义了data，那么Intent中必须也要定义可匹配的data。在介绍匹配规则之前，县来讲一下data的结构，这个有点复杂

```
<activity
     android:name=".SecondActivity"
     android:label="@string/title_activity_second"
     android:launchMode="singleTask"
     android:taskAffinity="com.xiao.x"
     android:theme="@style/AppTheme.NoActionBar">

     <intent-filter>
         <data
             android:scheme="string"
             android:host="string"
             android:port="string"
             android:path="string"
             android:pathPattern="string"
             android:pathPrefix="string"

             android:mimeType="string" />
     </intent-filter>
 </activity>

```

data由两部分组成，mimeType和URI。mimeType指媒体类型，比如image/jpeg audio/mpeg4-generic和video/* 等，可以表示图片，文本，视频等不同的媒体格式，而URI中包含的数据就比较多了，下面是URI的结构

```
<scheme>://<host>:<port>/[<path>|<pathPrefix>|<pathPattern>]

```

这里在结合一个例子

```
content://com.example.project:200/folder/subfoder/etc
http://www.baidu.com:80/search/info

```

这个其实就是一个类似网址的结果


|   英文  | 解释     |
| ------------- |:-------------:|
| scheme     |     URI的模式，比如Http、file、content等,如果RUI中没有指定scheme，那么整个URI的其他参数无效，这意味着整个URI无效|
| Host       | URI的主机名，比如www.baidu.com,如果host未指定,那么整个URI中的其他参数无效，这也意味着URI是无效的   |
| Port    |URI的端口号，比如80，仅当URI中指定了scheme和host参数的时候，post参数才是有意义的   |
| path    |是描述路径信息的。表示完整路径信息  |
| pathPrefix    | 表示完整路径信息，但是他里面可以包含通配符* ，*表示0个或者多个任意字符，需要注意的是，由于正则表达式的规范，如果想表示真是的字符串，要写成\\*  |
| pathPattern    |表示路径的前缀信息 |

介绍完之后就介绍data的匹配规则了，data匹配规则和action类似，它也要求必须含有data数据，并且data数据能够完全匹配过滤规则中的某一个data，这这里的完全匹配指的是过滤规则中出现的data部分也出现了Intent中的Data中

```

<activity
    android:name=".SecondActivity"
    android:label="@string/title_activity_second"
    android:launchMode="singleTask"
    android:taskAffinity="com.xiao.x"
    android:theme="@style/AppTheme.NoActionBar">
    <intent-filter>
        <data
            android:mimeType="text/*" />//
    </intent-filter>

</activity>
```
这种匹配规则制定了媒体类型为所有类型的文字，mimeType属性必须为"text/* "才能匹配，这种情况下虽然规律规则没有指定URI，但是却有默认值，URI的默认值为content和file。也就是说，虽然没有指定URI，但是Intent中URI部分的scheme必须为content或者file才能匹配，这点是需要尤其注意的。为了匹配这个规则，我们可以写出如下示例。

```
intent.setDataAndType（URI.parse("file://abc")，“text/xxx”）
```

如果要为Intent指定完整的data,必须要调用setDataAndType方法，不能先调用setData在调用setType,因为这两个方法彼此会清除对方的值，这个看源码就很容易理解了。

```
public Intent setData(URI data){
  mData=data;
  mType=null;
  return this;
}
```

设置过滤规则：

```

<activity
    android:name=".SecondActivity"
    android:label="@string/title_activity_second"
    android:launchMode="singleTask"
    android:taskAffinity="com.xiao.x"
    android:theme="@style/AppTheme.NoActionBar">
    <intent-filter>
        <data
          android:scheme="http" ...
            android:mimeType="video/mpeg" />

            <data
              android:scheme="http" ...
                android:mimeType="audio/mpeg" />
    </intent-filter>

</activity>
```

这种规则制定了两组data规则，切每个data都制定了完整的属性值，既有URI又有mimeType。为了匹配2中的规则，我们可以写出如下示例


```
intent.setDataAndType（URI.parse("file://abc")，“audio/mpeg”）
```
或者
```
intent.setDataAndType（URI.parse("file://abc")，“video/mpeg”）
```
 通过上面的两个示例，读者应该已经明白了data的匹配规则，关于data还有一个特殊情况需要说明一下，这也是它和action不同的地方，如下两种特殊写法，它们的作用是一样的


 ```
             <data
               android:scheme="file"
                 android:host"www.baidu.com"/>
 ```


 ```
             <data
               android:scheme="file" />
                   <data    android:host"www.baidu.com"/>
 ```

到这里我们已经把IntentFilter的过滤规则都讲解一遍，还记得本节前面给出的一个intent-filter的示例吗，现在我们给出完全匹配他的Intent

```
Intent intent=new Intent("com.xiao.x")
intent.addCategory("com.xiao.x")
intent.setDataAndType(URI.parse("file://abc"),"text/plain")
startActivity(intent)
```
这个匹配规则对于Service和BroadcastReceiver是一样的
但是对于Service 系统建议使用显示的方式来启动服务


当我们使用隐式方式启动Activity的时候建议做一下判断，看能不能匹配我们的隐式Intent，如果没有就可能会报错 activity no found 。判断有两种方式，第一种是使用packetManager的resolveActivity方法或者Intent的resolveActivity方法，如果找不到Activity，就会返回null。packetManage还提供了另外一种方法，queryIntentActivities 方法，这个方法和resolveActivity不同，他返回所有匹配的activity，这个时候可以通过他过滤activity，然后直接实现应用内跳转。

```
public abstrace List<resolveInfo> queryIntentActivitys(Intent intent,int flags);
public abstrace resolveInfo  resolveActivity(Intent intent,int flags);
```

第一个参数是intent，第二个参数要注意，我们要使用MATCH_DEFAULT_ONLY这个标记位，这个标记位的含义是紧紧匹配那些在intent-filter中申明的`<category android:name="android.intent.category.DEFAULT">` 这个category的activity。使用这个标记位的意义是如果两个方法返回值不为null，那么startactivity一定可以成功，如果不使用这个标记位，那么就可以吧intent-filter中category不含DEFAULT的那些匹配出来。从而导致匹配失败，那么category为DEFSULT的那些就无法匹配出来，导致启动失败。因为不还有DEFAULT的这个category的activity无法接受隐式的intent。在action和category中有一类action和category比较重要

```
<action android:name="android.intent.action.MAIN">
<category android:name="android.intent.category.LAUNCHER">

```

这个标记了应用的一个入口，二缺一不可，不然无法启动。针对Service和BroadcastReceiver，PackageManager同样提供了类似的方法获取成功匹配的组件的信息


# 第二章 IPC机制

本章主要讲解Android中的IPC机制，首先介绍了Android中的多进程概念以及多进程开发模式中常见的注意事项，接着介绍Android中的许雷华机制和Binder。然后详细介绍进程间的通信方式。

## Android IPC简介

IPC是Inter-progress Communication的缩写，含义为进程间通信或者跨进程通信，是指两个进程之间进行数据交换的过程，说起进程间通信，我们首先要理解，进程通信是什么，什么是线程。线程和进程是截然不同的概念。按照操作系统中的描述，线程是CPU最小的调度单元，同时线程是一种有限的系统资源。二进程一般是一个执行单元，或者可以理解为一个程序或者一个应用。一个进程可以包含多个线程，一个进程可以只包含一个主线程。在Android里面主线程也叫UI线程，在UI线程里才能操作界面元素，很多时候，一个进程中需要执行大量耗时操作的情况下，会造成ANR，这个时候就需要在子线程里面执行耗时操作。

IPC不是Android独有的，任何一个操作系统都需要有相应的IPC机制，Windows上面可以通过剪贴板，管道，邮槽等进行进程间通讯。Linux可以通过命名管道，共享内存，信号量来进行进程间通讯。对于Android来讲，他是基于Linux内核的移动操作系统，他的进程间通讯方式不是完全继承自Linux，而是有他自己的进程间通讯方式。在Android中有特色的进程通讯方式就是Binder，通过Binder可以轻松的实现进程间通讯。除了Binder，Android还支持Socket，通过Socket可以实现任意两个中断之间的通讯，当然同一个设备上的两个进程通过Socket通信自然也是可以的。

说到IPC的使用场景就必须提到多进程，只有面对多进程的情况下才需要考虑进程间通讯。有两种情况：第一种是一个应用如果由于某些原因自身需要采用多进程模式来实现。第二种是和另外一个APP实现通讯，比如通过咸鱼吊起支付宝...

## Android中的多进程模式

### 开启多进程模式
通过清单文件配置来开启


```
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.smart.kaifa">

    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/AppTheme">
        <activity android:name=".MainActivity">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />

                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
        <activity
            android:name=".SecondActivity"
            android:process=":remote"//多进程模式
            android:label="@string/title_activity_second"
            android:launchMode="singleTask"
            android:theme="@style/AppTheme.NoActionBar">

        </activity>
        >
        <activity
            android:process="xxx.xxx.xxx.remote"
            android:name=".ThirdActivity"
            android:label="@string/title_activity_third"
            android:launchMode="singleTask"
            android:taskAffinity="com.xiao.x"//多进程模式
            android:theme="@style/AppTheme.NoActionBar"></activity>
    </application>

</manifest>

```


SecondActivity和ThirdActivity指定了process属性，并且他们的属性值不同，这意味着当前引用有增加了两个新的进程。假设当前应用的包名为com.xiao 那么在启动SecondActivity的时候会有一个进程com.xiao:remote 这个进程 。当ThridActivity启动时，系统也会为他单独创建一个进程xxx.xxx.xxx.remote。同时入口的MainActivity由于没有指定precess属性，那么他运行在默认进程中，默认进程名是包名。 可以通过DDMS视图查看，或者通过adb命令查看 adb shell ps

这里有一个注意点，SecondActivity和ThirdActivity的android：process属性分别为:remote和xxx.xxx.xxx.remote这两种方式有什么区别吗？其实有区别的，区别有两点，第一点是xxx.xxx.xxx.remote这种命名方式不会附加包名信息，但是:remote会附加包名信息。其次，以:开头的进程属于当前应用的私有进程，其他应用的组件不可以和他跑在同一个进程中，进程命不以:开头的进程属于全局进程，可以和其他应用通过共享ShareUID方式和他跑在同一个进程中。

我们都知道Android系统回味每一应用分配位移的UID，只有UID相同的应用才能共享数据，这里要说明的是，两个应用可以通过ShareUID跑在同一个进程中是有要求的，需要两个应用的ShareUID相同别切具有相同的签名。这种情况下才能互相访问私有数据，比如data目录，组件信息等，可以把它理解为同一个应用的两个部分

### 多进程模式的运行机制。
可以这么说开启多进程模式之后，各种奇怪的现象都出现了，首先static 不能共用了，因为Android中，每一个应用跑在一个虚拟机中，这个时候static是相对当前虚拟机的，所以在一个进程中的static修改是无效的。
举个栗子：
```


public class UserManager{

    public static int sUerId=1;

}
```

这个时候在MainActivity中设置这个值为2，在SecondActivity中获取，发现值还是1 。

一般来讲，多进程会出现下面几个常见的问题
 * 静态成员和单例模式完全失效
 * 线程同步机制完全失效
 * SharedPreferences的可靠性下降
 * Application会多次创建

 关于第四个问题，这里说一下，每次启动一个进程，就相当于创建了一个App，所以会启动一次Application。可以试一下。
 多进程模式实惠拥有不同的虚拟机、Application、内存空间，这个会给开发带来许多困扰的。

## IPC基础概念介绍

### Serializable 接口
这个是Java提供的，这个比较简单，实现这个接口，然后设置一个

```
public class HHH implements Serializable {

    private static final long seriaVersionUID=12131232322332L;
}

```

这个就可以了，seriaVersionUID一定要一样这样才能正确的反序列化。


```
序列化的过程是
User user=new User();
ObjectOutPutStream out=new ObjectOutPutStream(new FileOutputStream("xxx.xxx"));
out.writeObject(user);
out.close();


反序列化的过程是
User user=new User();
ObjectInPutStream in=new ObjectInPutStream(new FileInputStream("xxx.xxx"));
User user=(User)in.readObject(user);
in.close();
```
这个值是完全一样的，但是不是一个对象，地址变了。
seriaVersionUID的作用是为了防止版本便更导致的User结构变化导致的序列化失败，如果没有指定，两边结构不一样就会出现转换失败，如果指定了，就可以很大程度上避免序列化失败的过程，另外系统的序列化和反序列化也是可以改变的，但是不建议改变


### Parcelable接口
这个是Android提供的，相对于Serializable，内存占用更小一些

```
public class User implements Parcelable {
    public int userId;
    public String userName;
    public boolean isMale;

    //返回当前对象的内容描述，如果含有中文描述符，返回1，否则返回0，几乎所有情况都为0
    @Override
    public int describeContents() {
        return 0;
    }
    // 将当前对象写入许雷华结构中，其中flag标识有两种值 0或者1，为1时标识当前对象需要最为返回值返回，不能立即释放资源，几乎所有情况都为0
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.userId);
        dest.writeString(this.userName);
        dest.writeByte(this.isMale ? (byte) 1 : (byte) 0);
    }

    public User() {
    }

    //从与写话后的对象中创建原始对象
    protected User(Parcel in) {
        this.userId = in.readInt();
        this.userName = in.readString();
        this.isMale = in.readByte() != 0;
    }

    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
      //从序列化后的对象中创建原始对象
        @Override
        public User createFromParcel(Parcel source) {
            return new User(source);
        }
        //创建指定长度的原始对象数组
        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };
}


```

当然AndroidStudio有实现这个接口的插件：Parcelable code Generator

如果是网络情况建议使用Serializable提高兼容性，app内部使用使用Parcelable 降低内存消耗

### binder
这个是一个很深入的话题，特别复杂，这里就介绍一下Binder的使用和上层原理。

Binder是Android中的一个类，他实现了IBinder接口。从IPC角度来说，Binder是Android中的一种跨进程通讯方式，Binder还可以理解为一种虚拟的物理设备，他的设备驱动是/dev/binder。该通信方式是Linux中没有的。从Android FrameWord角度来讲，Binder是ServiceManager连接各种Manager（Activity Manager 、Window Manager 等等）和相对应的ManagerService的桥梁，从Android应用层来讲，Binder是客户端和服务端进行通信的媒介，当bindService的时候，服务端会返回UI个包含了服务端业务调用的Binder对象，通过这个Binder对象，客户端就可以获取服务器端提供的服务或者数据，这里的服务包括普通服务和基于AIDL的服务。

Android开发中，Binder主要用在Service中，包括AIDL和Messenger，其中普通Service中的Binder不涉及进程间通信，所以较为简单，无法涉及Binder的核心。而Messenger的底层其实就是AIDL，所以这里选用AIDL来分析Binder的工作机制。为了分析AIDL机制，这里要创建一个AIDL实例

Book.java

```
public class Book implements Parcelable {
    public int bookId;
    public String bookName;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.bookId);
        dest.writeString(this.bookName);
    }

    public Book() {
    }

    protected Book(Parcel in) {
        this.bookId = in.readInt();
        this.bookName = in.readString();
    }

    public static final Parcelable.Creator<Book> CREATOR = new Parcelable.Creator<Book>() {
        @Override
        public Book createFromParcel(Parcel source) {
            return new Book(source);
        }

        @Override
        public Book[] newArray(int size) {
            return new Book[size];
        }
    };
}

```


Book.aidl

```
// Book.aidl
package com.smart.kaifa;

// Declare any non-default types here with import statements
parcelable Book;

```


IBookManager.aidl
```
package com.smart.kaifa;

// Declare any non-default types here with import statements
import com.smart.kaifa.Book;
interface IBookManager {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
            double aDouble, String aString);


            List<Book> getBookList();
            void addBook(in Book book);
}

```

之后系统为我们生成的IBookManager.java 文件 可以双击shift 然后搜索


这里先介绍一下这个类：
他继承IInterface接口，同时自己还是一个接口，首先声明了`getBookList`和`addBook`和`basicTypes`方法，同时声明了三个常量来标记这三个方法，方便在transact的时候确定客户端所请求的是那个方法，接着声明了一个Stub类，这是一个Binder类。当服务端和客户端属于同一个进程的时候，方法调用不会走跨进程的transact过程，而当两者位于不同的进程的时候，方法调用走transact过程，这个逻辑由Stub内部代理类Proxy来完成。
```
/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: F:\\kaifa\\app\\src\\main\\aidl\\com\\smart\\kaifa\\IBookManager.aidl
 */
package com.smart.kaifa;
//继承IInterface接口
public interface IBookManager extends android.os.IInterface
{


  /*** Demonstrates some basic types that you can use as parameters
       * and return values in AIDL.
       */
  //定义这三个待实现的方法
  public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, java.lang.String aString) throws android.os.RemoteException;
  public java.util.List<com.smart.kaifa.Book> getBookList() throws android.os.RemoteException;
  public void addBook(com.smart.kaifa.Book book) throws android.os.RemoteException;

  /** Local-side IPC implementation stub class. */
  //这个是一个内部类Stub   继承Binder 接口并实现IBookManager这个接口，在内部类中实现
  public static abstract class Stub extends android.os.Binder implements com.smart.kaifa.IBookManager
    {
       //这个是Binder的唯一标识，一般用当前Binder的类名标识，比如本例中的 com.smart.kaifa.IBookManager
        private static final java.lang.String DESCRIPTOR = "com.smart.kaifa.IBookManager";
        //定义三个ID用于标记着三个方法
        static final int TRANSACTION_basicTypes = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
        static final int TRANSACTION_getBookList = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
        static final int TRANSACTION_addBook = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);

          /** Construct the stub at attach it to the interface. */

          //Stub的构造方法
          public Stub(){
              this.attachInterface(this, DESCRIPTOR);
            }

          /**
           * Cast an IBinder object into an com.smart.kaifa.IBookManager interface,
           * generating a proxy if needed.
           */

           //用于将服务端的Binder对象转换成客户端所需的AIDL，这种转换过程是区分进程的，如果客户端和服务端位于同一个进程，那么此方法返回的就是服务端的Stub对象本身，否则返回的是系统封装后的Stub.proxy
          public static com.smart.kaifa.IBookManager asInterface(android.os.IBinder obj){
              if ((obj==null)) {
                  return null;
                }
            android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);

            if (((iin!=null)&&(iin instanceof com.smart.kaifa.IBookManager))) {
                return ((com.smart.kaifa.IBookManager)iin);
              }
              return new com.smart.kaifa.IBookManager.Stub.Proxy(obj);
          }

          //这个方法用户返回当前Binder对象
          @Override
          public android.os.IBinder asBinder(){
                return this;
            }

          //这个方法运行在服务端中的Binder线程池中，当客户端发起跨进程请求的时候，远程请求会通过系统底层分装后交给此方法来处理，该方法的原型是 public Boolean onTransact（int code, android.os.Parcel data, android.os.Parcel reply, int flags）
          //服务端通过code可以确定客户端所请求的目标方法是什么，接着从data中取出目标方法所需要的参数（如果目标方法有参数的话），然后执行目标方法，当目标方法执行完毕后，就像reply中写入返回值（如果目标方法有返回值的话）
          //onTransacct方法执行的过程就是这样的，需要注意的是，如果此方法返回false。那么客户端的请求就会失败，因此我们可以利用这个特性来做权限验证，毕竟我们也不希望随便一个进程就可以远程调用我们的服务
          @Override
          public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException{
              switch (code)  {
                  case INTERFACE_TRANSACTION:
                      {
                          reply.writeString(DESCRIPTOR);
                          return true;
                      }
                  case TRANSACTION_basicTypes:
                      {
                          data.enforceInterface(DESCRIPTOR);
                          int _arg0;
                          _arg0 = data.readInt();
                          long _arg1;
                          _arg1 = data.readLong();
                          boolean _arg2;
                          _arg2 = (0!=data.readInt());
                          float _arg3;
                          _arg3 = data.readFloat();
                          double _arg4;
                          _arg4 = data.readDouble();
                          java.lang.String _arg5;
                          _arg5 = data.readString();
                          this.basicTypes(_arg0, _arg1, _arg2, _arg3, _arg4, _arg5);
                          reply.writeNoException();
                          return true;
                      }
                 case TRANSACTION_getBookList:
                      {
                        data.enforceInterface(DESCRIPTOR);
                        java.util.List<com.smart.kaifa.Book> _result = this.getBookList();
                        reply.writeNoException();
                        reply.writeTypedList(_result);
                        return true;
                      }
                case TRANSACTION_addBook:
                      {
                      data.enforceInterface(DESCRIPTOR);
                      com.smart.kaifa.Book _arg0;
                            if ((0!=data.readInt())) {
                                  _arg0 = com.smart.kaifa.Book.CREATOR.createFromParcel(data);
                            }else {
                                  _arg0 = null;
                            }
                      this.addBook(_arg0);
                      reply.writeNoException();
                      return true;
                      }
                  }
              return super.onTransact(code, data, reply, flags);
          }

          //这个是一个代理类mRemote这个其实是一个Stub对象，这个是Stub的一个内部代理类
          private static class Proxy implements com.smart.kaifa.IBookManager{
                private android.os.IBinder mRemote;
                Proxy(android.os.IBinder remote){
                  mRemote = remote;
                }
              @Override
              public android.os.IBinder asBinder(){
                  return mRemote;
                }
              public java.lang.String getInterfaceDescriptor(){
                    return DESCRIPTOR;
                }
              /**
               * Demonstrates some basic types that you can use as parameters
               * and return values in AIDL.
               */
          //这个是创建的aidl的时候自带的一个方法 ，注意这个是用android studio创建的时候 可以忽略，实现IBookManager方法
          @Override
          public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, java.lang.String aString) throws android.os.RemoteException{
              android.os.Parcel _data = android.os.Parcel.obtain();
              android.os.Parcel _reply = android.os.Parcel.obtain();
              try {
                _data.writeInterfaceToken(DESCRIPTOR);
                _data.writeInt(anInt);
                _data.writeLong(aLong);
                _data.writeInt(((aBoolean)?(1):(0)));
                _data.writeFloat(aFloat);
                _data.writeDouble(aDouble);
                _data.writeString(aString);
                mRemote.transact(Stub.TRANSACTION_basicTypes, _data, _reply, 0);
                _reply.readException();
              }
              finally {
                _reply.recycle();
                _data.recycle();
              }
          }

          //这里是获取List集合的方法，实现IBookManager方法
          //这个方法运行在客户端，当客户端远程调用此方法的时候，他的内部实现是这样子的：首先，创建该方法所需要输入型Parcel对象data，输出型对象parcel对象_reply和返回值对象 _result，然后把该方法的参数信息写入_data中（如果有参数的话），接着调用transact方法来发起RPC（远程过程调用）请求，同时当前线程挂起：然后服务端的onTransact方法会被调用，知道RPC过程返回后，当前线程继续执行，并从_reply中读取RPC过程返回的结果
          @Override
          public java.util.List<com.smart.kaifa.Book> getBookList() throws android.os.RemoteException{
              android.os.Parcel _data = android.os.Parcel.obtain();
              android.os.Parcel _reply = android.os.Parcel.obtain();
              java.util.List<com.smart.kaifa.Book> _result;

              try {
                _data.writeInterfaceToken(DESCRIPTOR);
                接着调用transact方法来发起RPC（远程过程调用）请求
                mRemote.transact(Stub.TRANSACTION_getBookList, _data, _reply, 0);

                _reply.readException();
                _result = _reply.createTypedArrayList(com.smart.kaifa.Book.CREATOR);
              }finally {
                _reply.recycle();
                _data.recycle();
              }
              return _result;
            }
          //这里是addBook方法，实现IBookManager方法
          //这个方法需要在客户端运行，它的执行过程和getBookList一样，但是没有返回值，所以不需要从_reply中读取返回值
          @Override
          public void addBook(com.smart.kaifa.Book book) throws android.os.RemoteException{
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();

                  try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                        if ((book!=null)) {
                        _data.writeInt(1);
                        book.writeToParcel(_data, 0);
                      }else {
                        _data.writeInt(0);
                      }
                  mRemote.transact(Stub.TRANSACTION_addBook, _data, _reply, 0);
                  _reply.readException();
                  }finally {
                  _reply.recycle();
                  _data.recycle();
                }
            }

        }

    }

}


```
通过上面的分析，可以了解了Binder的工作机制，但是还有两点需要额外说明一下：首先，当客户端发起远程请求时，由于当前线程会被挂起直到服务端进程返回数据，所以如果一个远程方法很耗时，这个时候就不能在UI线程中发起此远程请求；其次由于服务端Binder方法运行在Binder线程池中，所以Binder方法不管是否耗时都应该采取同步的方式实现，应为他已经运行在一个线程中了。为了更好的说明Binder下面给出一个Binder的工作机制图



![Alt text](图像1517752561.png "Binder的工作机制")


从上述分析过程来看，我们完全可以不提供AIDL文件即可实现Binder，之所以提供AIDL文件，是为了方便系统帮助我们生成代码，系统根据AIDL文件生成Java文件的格式是固定的，我们可以抛开AIDL文件直接写一个Binder出来。接下来就介绍如何写一个Binder出来,参考上面系统同生成的IBookManager.java这个类的代码。可以发现这个类是相当的有规律。我们可以根据这个规律实现，首先这个类分为两个部分，一个是这个类本身就继承自IInterface接口，其次它的内部由个Stub类，这个类就是个Binder，还记得我们怎么写一个Binder的服务端，


```
private final IBookManager.Stub mBinder=new IBookManager.Stub(){


@override
public List<Book> getBookList() throws RemoteException{
  synchronized(mBookList){
    return mBookList;
  }
}

@override
public void addBook(Book book) throws RemoteException{
  synchronized(mBookList){
    if(!mBookList.contains(book))
    return mBookList.add(book);
  }
}


}


```

首先我们会实现一个创建了一个Stub对象并在内部实现IBookManager的接口方法。然后在Service的onBind中返回这个Stub对象，因此，从这一点来看，我们完全可以把Stub可以吧Stub这个类提取出来直接作为一个独立的Binder类来实现，这样IBookManager中就只剩下接口本身了，通过这种分离的方式可以让他的结构变得清晰点。
根据上面的思想 收订实现一个Binder可以通过如下步骤来完成

 * 声明一个AIDL性质的接口，只需要继承IInterface接口即可，IInterface接口中只有一个asBinder方法。这个接口的实现如下：

 ```
public interface IBookManager extends IInterface{

  //这个是Binder的唯一标识，一般用当前Binder的类名标识，比如本例中的 com.smart.kaifa.IBookManager
   private static final java.lang.String DESCRIPTOR = "com.smart.kaifa.IBookManager";
   //定义三个ID用于标记着三个方法
   static final int TRANSACTION_basicTypes = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
   static final int TRANSACTION_getBookList = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
   static final int TRANSACTION_addBook = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
   //定义三个抽象方法
   public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, java.lang.String aString) throws android.os.RemoteException;
   public java.util.List<com.smart.kaifa.Book> getBookList() throws android.os.RemoteException;
   public void addBook(com.smart.kaifa.Book book) throws android.os.RemoteException;

}


 ```

可以看到，在接口中声明了一个Binder描述符和另外三个id，这三个id分别表示的是basicTypes、getBookList、addBook方法，这段代码原本也是系统生成的，我们仿照系统生成的规则去手写这部分代码，如果有更多的方法怎么做呢？很显然，我们要在声明一个id，然后按照固定模式声明这个新方法即可，这个比较好理解，不在多说。

* 实现Stub类和Stub类中的Proxy代理类，这段代码我们可以自己写，但是写出来发现和系统生成的代码是一样的，因此这个Stub类我们只需要参考系统生成的代码即可，只是结构上需要做一下调整，调整后的代码如下所示。

```
public class BookMangerImpl extends Binder implements IBookManager{

    public BookManagerImpl(){
      this.attachInterface(this,DESCRIPTOR);
    }

    //用于将服务端的Binder对象转换成客户端所需的AIDL，这种转换过程是区分进程的，如果客户端和服务端位于同一个进程，那么此方法返回的就是服务端的Stub对象本身，否则返回的是系统封装后的Stub.proxy
   public static com.smart.kaifa.IBookManager asInterface(android.os.IBinder obj){
       if ((obj==null)) {
           return null;
         }
     android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);

     if (((iin!=null)&&(iin instanceof com.smart.kaifa.IBookManager))) {
         return ((com.smart.kaifa.IBookManager)iin);
       }
       return new com.smart.kaifa.IBookManager.Stub.Proxy(obj);
   }

   //这个方法用户返回当前Binder对象
   @Override
   public android.os.IBinder asBinder(){
         return this;
     }

   //这个方法运行在服务端中的Binder线程池中，当客户端发起跨进程请求的时候，远程请求会通过系统底层分装后交给此方法来处理，该方法的原型是 public Boolean onTransact（int code, android.os.Parcel data, android.os.Parcel reply, int flags）
   //服务端通过code可以确定客户端所请求的目标方法是什么，接着从data中取出目标方法所需要的参数（如果目标方法有参数的话），然后执行目标方法，当目标方法执行完毕后，就像reply中写入返回值（如果目标方法有返回值的话）
   //onTransacct方法执行的过程就是这样的，需要注意的是，如果此方法返回false。那么客户端的请求就会失败，因此我们可以利用这个特性来做权限验证，毕竟我们也不希望随便一个进程就可以远程调用我们的服务
   @Override
   public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException{
       switch (code)  {
           case INTERFACE_TRANSACTION:
               {
                   reply.writeString(DESCRIPTOR);
                   return true;
               }
           case TRANSACTION_basicTypes:
               {
                   data.enforceInterface(DESCRIPTOR);
                   int _arg0;
                   _arg0 = data.readInt();
                   long _arg1;
                   _arg1 = data.readLong();
                   boolean _arg2;
                   _arg2 = (0!=data.readInt());
                   float _arg3;
                   _arg3 = data.readFloat();
                   double _arg4;
                   _arg4 = data.readDouble();
                   java.lang.String _arg5;
                   _arg5 = data.readString();
                   this.basicTypes(_arg0, _arg1, _arg2, _arg3, _arg4, _arg5);
                   reply.writeNoException();
                   return true;
               }
          case TRANSACTION_getBookList:
               {
                 data.enforceInterface(DESCRIPTOR);
                 java.util.List<com.smart.kaifa.Book> _result = this.getBookList();
                 reply.writeNoException();
                 reply.writeTypedList(_result);
                 return true;
               }
         case TRANSACTION_addBook:
               {
               data.enforceInterface(DESCRIPTOR);
               com.smart.kaifa.Book _arg0;
                     if ((0!=data.readInt())) {
                           _arg0 = com.smart.kaifa.Book.CREATOR.createFromParcel(data);
                     }else {
                           _arg0 = null;
                     }
               this.addBook(_arg0);
               reply.writeNoException();
               return true;
               }
           }
       return super.onTransact(code, data, reply, flags);
   }

   //这个是一个代理类mRemote这个其实是一个Stub对象，这个是Stub的一个内部代理类
   private static class Proxy implements com.smart.kaifa.IBookManager{
         private android.os.IBinder mRemote;
         Proxy(android.os.IBinder remote){
           mRemote = remote;
         }
       @Override
       public android.os.IBinder asBinder(){
           return mRemote;
         }
       public java.lang.String getInterfaceDescriptor(){
             return DESCRIPTOR;
         }
       /**
        * Demonstrates some basic types that you can use as parameters
        * and return values in AIDL.
        */
   //这个是创建的aidl的时候自带的一个方法 ，注意这个是用android studio创建的时候 可以忽略，实现IBookManager方法
   @Override
   public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, java.lang.String aString) throws android.os.RemoteException{
       android.os.Parcel _data = android.os.Parcel.obtain();
       android.os.Parcel _reply = android.os.Parcel.obtain();
       try {
         _data.writeInterfaceToken(DESCRIPTOR);
         _data.writeInt(anInt);
         _data.writeLong(aLong);
         _data.writeInt(((aBoolean)?(1):(0)));
         _data.writeFloat(aFloat);
         _data.writeDouble(aDouble);
         _data.writeString(aString);
         mRemote.transact(Stub.TRANSACTION_basicTypes, _data, _reply, 0);
         _reply.readException();
       }
       finally {
         _reply.recycle();
         _data.recycle();
       }
   }

   //这里是获取List集合的方法，实现IBookManager方法
   //这个方法运行在客户端，当客户端远程调用此方法的时候，他的内部实现是这样子的：首先，创建该方法所需要输入型Parcel对象data，输出型对象parcel对象_reply和返回值对象 _result，然后把该方法的参数信息写入_data中（如果有参数的话），接着调用transact方法来发起RPC（远程过程调用）请求，同时当前线程挂起：然后服务端的onTransact方法会被调用，知道RPC过程返回后，当前线程继续执行，并从_reply中读取RPC过程返回的结果
   @Override
   public java.util.List<com.smart.kaifa.Book> getBookList() throws android.os.RemoteException{
       android.os.Parcel _data = android.os.Parcel.obtain();
       android.os.Parcel _reply = android.os.Parcel.obtain();
       java.util.List<com.smart.kaifa.Book> _result;

       try {
         _data.writeInterfaceToken(DESCRIPTOR);
         接着调用transact方法来发起RPC（远程过程调用）请求
         mRemote.transact(Stub.TRANSACTION_getBookList, _data, _reply, 0);

         _reply.readException();
         _result = _reply.createTypedArrayList(com.smart.kaifa.Book.CREATOR);
       }finally {
         _reply.recycle();
         _data.recycle();
       }
       return _result;
     }
   //这里是addBook方法，实现IBookManager方法
   //这个方法需要在客户端运行，它的执行过程和getBookList一样，但是没有返回值，所以不需要从_reply中读取返回值
   @Override
   public void addBook(com.smart.kaifa.Book book) throws android.os.RemoteException{
         android.os.Parcel _data = android.os.Parcel.obtain();
         android.os.Parcel _reply = android.os.Parcel.obtain();

           try {
             _data.writeInterfaceToken(DESCRIPTOR);
                 if ((book!=null)) {
                 _data.writeInt(1);
                 book.writeToParcel(_data, 0);
               }else {
                 _data.writeInt(0);
               }
           mRemote.transact(Stub.TRANSACTION_addBook, _data, _reply, 0);
           _reply.readException();
           }finally {
           _reply.recycle();
           _data.recycle();
         }
     }

 }

}



}


```

其实理解了Binder的工作流程之后，很容易写出这样的东西.
现在来介绍Binder的两个非常重要的方法，linkToDeath和unlinkToDeath，我们知道，Binder运行在服务端进程，如果服务端进程由于某种原因异常终止了，这个时候客户端就会和服务端Binder连接断裂(称之为Binder死亡)，会导致我们的远程调用失败，更为关键的是，如果我们不知道Binder连接已经断裂，那么客户端的功能就会收到影响，所以Binder提供了两个方法配对，linkToDeath和unlinkToDeath，通过linkToDeath,我们可以给Binder设置一个死亡代理，当Binder死亡是，我们就会收到通知，这个时候我们就可以重新发起连接请求从而恢复连接，那么到底如何给Binder设置死亡代理呢？
很简单，先声明一个DeathRecipient对象，DeathRecipient是一个接口,其内部只有一个抽象方法binderDied，我们需要实现这个方法，当Binder死亡的时候，系统就会回调binderDied方法,我们就可以移除之前绑定的binder代理并重新绑定远程服务。
```
private IBinder.DeathRecipient mDeathRecipient=new IBinder.DeathRecipient{
@override
public void binderDied(){
  if(mBookManager==null){
    return;
  }
  mBookManager.asBinder().unlinkToDeath(mDeathRecipient,0);
  mBookManager=null;
}


}

```

其次，在客户端绑定远程服务成功后，给binder设置死亡代理
```
mService=OMessageBoxManager.Stub。asInterface（binder）;
binder.linkToDeath(mDeathRecipient,0);
```

其中，linkToDeath的第二个阐述是标记位，我们直接设置为0就可以，经过上面两个步骤。就给我们的Bionder设置死亡代理，当Binder死亡的时候我们就可以收到通知了，另外通过Binder的方法IsBinderAlive也可以判断Binder是否死亡。
到这里Ipc的基础知识就介绍完毕了，下面开始进入正题。

## Android中的IPC方式
在上面，我们介绍了IPC的几个基础知识，序列化和BInder，现在开始分析各种跨进程通信方式，具体方式有很多，比如可以通过在Intent中附加extras来传递信息，或者通过共享文件的方式共享上布局，还可以采用BInder方式来跨进程通信，ContentProvide天生就支持跨进程访问的，因此我们也可以才采用他来进行IPC通信，当然也可以通过SOcket进行
### 使用Bundle
我们知道，四大组件中的三大组件（Activity，Service，Receiver）都是支持在Intent中传递Bundle数据的，由于Bundle实现了Parcelable接口，所以他可以方便的在不同的进程间传输，基于这一点，当我们在一个进程中启动另一个进程的Activity、Service、Receiver 我们可以在Bundle中附加我们需要传输给远程进程的信息，并通过Intent传递出去
出了直接传递数据这种典型的使用场景，还有一种特殊的使用场景，比如A进程正在进行一个计算，计算完成后他要启动B进程的一个组件并把计算结果传递给进程B，可是计算结果不支持放入Bundle中，因此无法通过Intent来传输，这个时候如果我们用其他IPC方式就会略显复杂，可以考虑如下方式，我们通过Intent启动进程B的一个service组件（比如IntentService），让Service在后台进行计算，然后计算完毕在启动B进程中真正需要启动的目标组件，由于Service也运行在B进程中，所以目标苏建就可以直接获取计算结果，这样一来就轻松解决了跨进程的问题，这种方式的和兴思想在于将原本需要在A进程的计算任务转移到B进程后台Service中去执行，这样就成功的避免了进程间通信的问题，而且只用了很小的代价


### 使用文件共享

共享文件也是一种不错的进程间通信方式，两个进程通过读/写同一个文件来交换数据，比如A进程把文件写入文件，B进程通过读取这个文件来获取数据。我们知道，在windows上面，一个文件如果被加了排斥锁会导致其他线程无法对其进行访问，包括读/写，而Android是基于Linux的，是的其并发读写文件可以没有限制的进行，甚至两个线程可以同时对同一个文件进行对鞋操作都是允许的，尽管这个可能出问题，通过文件交换数据很好使用，处理可以交换一些文本信息外，我们还可以序列化一个对象到文件系统中同时从另一个进程中恢复这个对象，下面就是这种方法

还是一开始的例子，在MainActivity的onresume中序列化一个User对象到SD卡上的一个文件里，然后在SecondActivity的onResume中反序列化，我们希望在SecondActivity中能够正确的恢复User对象的值，关键代码如下：
先加一下权限，这里是测试的，所以就不加了

MainActivity
```

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        View viewById = findViewById(R.id.test);


        viewById.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();

                intent.setClass(MainActivity.this, SecondActivity.class);
                startActivity(intent);
            }
        });

    }


    @Override
    protected void onResume() {
        super.onResume();
        persistToFile();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.i("哇哈哈", "营养快线");
    }

    private void persistToFile() {
        Log.d("tag", "哇哈哈111");
        new Thread(new Runnable() {
            @Override
            public void run() {
                User user = new User(1, "Hello word", false);
                File dir = new File(Environment.getExternalStorageDirectory(), "smart");
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                Log.d("tag", "哇哈哈222");
                File file = new File(dir, "sss");

                ObjectOutputStream objectOutputStream = null;
                try {
                    objectOutputStream = new ObjectOutputStream(new FileOutputStream(file));
                    Log.d("tag", "哇哈哈333");
                    objectOutputStream.writeObject(user);
                    Log.d("tag", "哇哈哈444");
                } catch (Exception e) {
                    Log.d("tag", e.getMessage()+"||||||");
                } finally {
                    //记得关闭
                    if (objectOutputStream != null) {
                        try {
                            objectOutputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }).start();
    }


}

```

清单文件
```
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.smart.kaifa">

    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"></uses-permission>
    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/AppTheme">
        <activity android:name=".MainActivity">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />

                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
        <activity
            android:name=".SecondActivity"
            android:label="@string/title_activity_second"
            android:launchMode="singleTask"
            android:process=":remote"
            android:theme="@style/AppTheme.NoActionBar">
        </activity>
        >
    </application>

</manifest>
```

SecondActivity

```
public class SecondActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        View viewById = findViewById(R.id.test);
        viewById.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(SecondActivity.this, ThirdActivity.class);
                startActivity(intent);
            }
        });


    }


    @Override
    protected void onResume() {
        super.onResume();
        recoverFromFile();
    }


    private void recoverFromFile() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                User user = null;
                Log.d("tag", "=========");
                File cache = new File(Environment.getExternalStorageDirectory(), "smart");
                File file = new File(cache, "sss");
                Log.d("tag", "=========");
                if (file.exists()) {
                    Log.d("tag", "=========");
                    ObjectInputStream inputStream = null;
                    try {
                        Log.d("tag", "=========");
                        inputStream = new ObjectInputStream(new FileInputStream(file));
                        user = (User) inputStream.readObject();
                        Log.d("tag", user.userName+"=========");
                        final User finalUser = user;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.d("tag", finalUser.userName+"=========");
                                Toast.makeText(SecondActivity.this, finalUser.userName, Toast.LENGTH_LONG);
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.d("tag", e.getMessage()+"=========");
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                        Log.d("tag", e.getMessage()+"=========");
                    } finally {
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }).start();
    }
}

```

要注意一点 SecondActivity是运行在另外的一个进程中的，看日志的时候要切换一下进程。

可以看日志，在SecondActivity中成功的从文件恢复了之前存入对象的数据，这里只所以说内容，是因为反序列化得到对象只是在内容上和序列化之前的对象是一样的，但是他的本质是两个对象

通过文件共享这种方式来共享数据对文件格式是没有具体要求的，比如可以是文本文件，也可以是XML文件，只要读写是双方约定的数据格式即可。通过文件共享的方式也是有局限性的，比如并发读写的问题，想上面的那些例子，如果并发读写，那么我们读出的内容就有可能不是最新的，如果是并发写的话就更加严重了，因此我们要尽量避免并发写这种情况的操作或者使用线程同步来限制多个线程的写操作，通过上面的分析，我们可以知道，文件共享方式适合在对数据同步要求不高的进程之间进行通信，并用要妥善处理并发读写的问题。

当然SharedPrefences是个特例，众所周知，SharedPrefences是Android中提供的轻量级存储方案，他通过键值对的方式来存储数据，在底层实现上他采用XML文件来存储价值对，每个应用的SharedPrefences文件都可以在当前包所在的data目录下查看到。一般来说，他的目录位于/data/data/package name/shared_prefs 目录下，其中package name 表示的是当前应用的包名，从本质上来说，SharedPrefences也属于文件的一种，但是由于系统对他的读写有一定的缓存策略，记在内从重会有一份SharedPrefences文件的缓存，因此在多进程模式下，系统对他的读写就变得不可靠，当面对高并发的读写访问，sharedPrefences有很大几率会丢失数据，因此，不建议在进程通信中使用SharePrefences

### 使用Messager

Messager可以翻译为心事，顾名思义，通过他可以在不同进程中传递Messager对象，在Message中放入我们需要传递的数据，就可以轻松的实现数据的进程间传递了。Messager是一种轻量级的IPC方法，他的底层实现是AIDL，为什么这么说呢，我们大致看一下Messager这个类的构造方法就明白了。
下面Messager的两个构造方法，从构造方法的实现上我们可以明显看出AIDL的痕迹，不管是IMessager还是Stub.asInterface，这种是用方法都表明它的底层是AIDL。

```
public Messager(Handler target){
  mTarget=target.getIMessager();
}


public Messager(IBinder target){
  mTarget=IManager.Stub.asInterface(target);
}
```

Messager的使用方法很简单，他对AIDL做了封装，是的我们可以更简单的进行进程间通讯，同时由于它的一次处理一个请求，因此在服务端我们不用考虑线程同步的问题，这是因为服务端中不存在并发执行的情景，他实现了一个Messager有如下几个步骤，分为客户端和服务端。

1. 服务端进程
首先，我们需要在服务端创建一个Service来处理客户端的连接请求，同时创建一个Handler并通过他来创建一个Messager对象，然后在Service的onBind中返回这个Messager对象底层的Binder即可。

2. 客户端进程
客户端进程中，首先要绑定服务端的Service，绑定成功后用服务端返回的IBinder对象创建一个Messager，通过这个Messager就可以像服务端发送消息了，发消息类型为Message对象。如果需要服务端能够回应客户端，就和服务端一样，我们还需要创建一个Handler并创建一个新的Messager并他这个Messager对象通过Message的replyTo参数传递给服务器，服务端通过这个replyTo参数就可以回应客户端，这听起来可能还是有点抽象，下面来看两个例子

清单文件,注册
```
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.smart.kaifa">

    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"></uses-permission>
    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/AppTheme">
        <activity android:name=".MainActivity">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />

                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
        <activity
            android:name=".SecondActivity"
            android:label="@string/title_activity_second"
            android:launchMode="singleTask"
            android:process=":remote"
            android:theme="@style/AppTheme.NoActionBar">


        </activity>

        <activity android:name=".MessengerActivity"></activity>

        <service
            android:name=".MessagerService"
            android:process=".remote"></service>
        <activity
            android:name=".ThirdActivity"
            android:label="@string/title_activity_third"
            android:launchMode="singleTask"
            android:process="xxx.xxx.xxx.remote"
            android:taskAffinity="com.xiao.x"
            android:theme="@style/AppTheme.NoActionBar"></activity>
    </application>

</manifest>
```

远程服务，在其他进程中，然后在接收到消息之后打印消息
```

public class MessagerService extends Service {
    private static final String TAG = "MessagerService";
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }
    private final Messenger mMessenger = new Messenger(new MessagerHandler());
    private static class MessagerHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case 1:
                    Bundle data = msg.getData();
                    String key = data.getString("key");
                    Log.i(TAG, "receive msg from Client:" + key);
                    getString("msg");
                    break;

                default:
                    super.handleMessage(msg);
            }
        }
        private void getString(String msg) {
        }
    }

}

```

启动服务的Activity，这个是客户端的处理，发送一个消息

```
public class MessengerActivity extends Activity {

    private static final String TAG = "MessengerActivity";
    private Messenger mService;
    private ServiceConnection mConnection=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService=new Messenger(service);
            Message obtain = Message.obtain(null, 1);
            Bundle bundle=new Bundle();
            //设置key值
            bundle.putString("key","ad钙奶");
            obtain.setData(bundle);
            try {
                mService.send(obtain);
            }catch (Exception e){

            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        TextView tv = findViewById(R.id.test);
        tv.setText("点击启动服务");
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MessengerActivity.this, MessagerService.class);

                bindService(intent,mConnection, Context.BIND_AUTO_CREATE);
            }
        });


    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        //销毁的时候记得解绑，节约资源
        if(mConnection!=null){
            unbindService(mConnection);
        }

    }
}

```

打印结果

```
02-05 23:12:36.784 673-673/.remote I/MessagerService: receive msg from Client:ad钙奶
```

通过上面的例子可以看出，在Messenger中进行数据传递必须将数据放入Message中，二Messenger和Message都实现了Parcelable接口，因此，可以跨进程传输。简单来说，Message中所支持的数据类型就是Messenger所支持的传输类型，实际上通过Messenger来传递Message，Message中能够使用的载体之后what、arg1、arg2、bundle以及replyTo。Message中的另外一个字段objectt在同一个进程中很实用，但是进程间在通信的时候。在Message中的另一个字段object在同一个进程中很实用的，但是在进程间通信的时候，在Android2.2之前object字段不支持跨进程传输，及时在2.2以后，也仅仅是实现系统提供的Parcelable接口的的对象才能通过他来传输，这就意味着我们自定义的Parcelable对象是无法通过object字段来传输。当然可以使用Bundle


现在在举一个例子，在收到消息之后会回复一条消息


服务端的修改
```
public class MessagerService extends Service {


    private static final String TAG = "MessagerService";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }


    private final Messenger mMessenger = new Messenger(new MessagerHandler());


    private static class MessagerHandler extends Handler {


        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case 1:
                    Bundle data = msg.getData();
                    String key = data.getString("key");
                    Log.i(TAG, "receive msg from Client:" + key);
                    getString("msg");
                    Messenger replyTo = msg.replyTo;
                    Message message = Message.obtain(null, 2);
                    Bundle bundle=new Bundle();
                    bundle.putString("reply","消息已经获取到了");
                    message.setData(bundle);

                    try {
                      //通过Messenger发送值
                        replyTo.send(message);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;

                default:

                    super.handleMessage(msg);

            }


        }

        private void getString(String msg) {
        }
    }

}
```

客户端的修改

```
public class MessengerActivity extends Activity {

    private static final String TAG = "MessengerActivity";
    private Messenger mService;
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = new Messenger(service);
            Message obtain = Message.obtain(null, 1);
            Bundle bundle = new Bundle();
            bundle.putString("key", "ad钙奶");
            obtain.setData(bundle);
            //将客户端需要接受的东西放入,绑定handler
            obtain.replyTo=mGetReplyMessenger;
            try {

                mService.send(obtain);
            } catch (Exception e) {

            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };


    private Messenger mGetReplyMessenger = new Messenger(new MessagerHandler());

    private static class MessagerHandler extends Handler {


        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case 2:
                    Bundle data = msg.getData();
                    String key = data.getString("reply");
                    Log.i(TAG, "receive msg from Service:" + key);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }

        private void getString(String msg) {
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        TextView tv = findViewById(R.id.test);
        tv.setText("点击启动服务");
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MessengerActivity.this, MessagerService.class);
                bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
            }
        });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mConnection != null) {
            unbindService(mConnection);
        }

    }
}

```


下面是一张图解，最后发现其实是在handler进行通讯了，都持有一个Handler



![Alt text](图像1517846753.png "Messager 进程间通信图")

### 使用AIDL

通过Messager这种方式进行进程间通信的方法，可以发现，Messager是串行的方式处理客户端发来的消息的，如果大量的消息同时发送到服务端，那么使用Messager就不合适了，同时Messager的主要作用是用来传递消息，很多时候我们需要跨进程调用调用服务端的方法，所以Messenger就无法做到了。但是我们可以使用AIDL来实现跨进程的方法调用，AIDL也是Messenger的底层实现，因此Messenger本质上也是AIDL，只不过是系统做了分装而已

1. 服务端
服务端首先要创建一个Service用来监听客户端的请求，然后创建一个AIDL文件，将暴露出来给客户端的接口在这个AIDL文件中声明，最后在Service中实现这个AIDL接口即可

2. 客户端
客户端所需要做的事情就稍微简单一些，首先需要绑定服务端的Service，绑定成功了，将服务端返回的Binder对象转成AIDL接口所属的类型，接着就可以调用AIDL中的方法了。
上面描述的只是一个感性的过程，AIDL的时间过程远不止这么简单，接下来会对其中的细节和难点进行完整的介绍。

3. AIDL接口的创建
首先看AIDL接口的创建

```
// IBookManager.aidl
package com.smart.kaifa;

// Declare any non-default types here with import statements
import com.smart.kaifa.Book;
interface IBookManager {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
            double aDouble, String aString);
   List<Book> getBookList();
  void addBook(in Book book);
}

```

在AIDL文件中，并不是所有的数据类型都是可以使用的，那么到底AIDL文件支持哪些数据类型呢？如下

* 基本数据类型(int 、long、char、boolean、double)
* String和CharSequence
* List：只支持ArrayList，并且每个元素都必须能够被AIDL支持
* Map：只支持HashMap，里面的每个元素都必须被AIDL支持，包括key和value
* Parcelable:所有实现了Parcelable接口的对象
* AIDL：所有的AIDL接口本身也可以在AIDL文件中使用

以上6中数据类型就是AIDL所支持的所有类型，其中自定义的Parcelable对象和AIDL对象必须要显示import进来，不管他们是否和当前的AIDL文件位于同一个包内。比如IBookManager.aidl这个文件，里面用到了Book这个类，这个类实现了Parcelable接口和IBookManager.adil这个文件，但是遵守AIDL的规范，我们任然需要显示的import进来。AIDL中会大量使用Parcelable，至于如果使用Parcelable接口来序列化对象。

另外一个需要注意的地方是如果AIDL文件中使用了自定义的Parcelable对象，那么必须新建一个和他同名的AIDL文件，并且什么它为Parcelable类型，在上面的IBookManager.aidl，这个时候我们必须要创建Book.aidl文件在里面添加如下内容

```
// Book.aidl
package com.smart.kaifa;

// Declare any non-default types here with import statements
parcelable Book;

```

我们需要注意。AIDL中每个实现了Parcelable接口的类都需要按照上面那种方式去创建相对应的AIDL文件并什么那个类为Parcelable。初次之外，AIDL中处理基本数据列行，其他类型的参数必须标记上方向；in、out、inout in表示输入类型参数，out表示输出型参数，inout表示输入输出型参数，但是不能都使用inout。这个在底层是有开销的
为了方便AIDL开发，一般所有的AIDL相关的文件放在一个文件夹中

```
public class BookManagerService extends Service {

    private  static final String TAG="BMS";

    private CopyOnWriteArrayList<Book> mBookList=new CopyOnWriteArrayList<>();

    private Binder mBinder=new IBookManager.Stub() {
        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

        }

        @Override
        public List<Book> getBookList() throws RemoteException {
            return mBookList;
        }

        @Override
        public void addBook(Book book) throws RemoteException {
            mBookList.add(book);
        }
    };


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        mBookList.add(new Book(1,"wahaha"));
        mBookList.add(new Book(2,"xiao"));
    }
}

```
上面是一个服务端Service的典型实现，首先在onCreate中初始化添加了两本图书的信息，然后创建了一个Binder对象并且在onBind中返回他，这个对象继承自IBookManager.Stub，并实现了他内部的AIDL方法。这个过程在Binder那一节已经介绍过了，这里采用了CopyOnWriteArrayList，是为了防止多个客户端访问的情景，使用CopyOnWriteArrayList实现线程同步

```
  <service android:name=".BookManagerService" android:process=".remote"></service>
```

客户端的实现



```

public class BookManagerActivity extends Activity {

    private static final String TAG = "BookManagerActivity";

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            IBookManager bookManager = IBookManager.Stub.asInterface(service) ;

            try {
                List<Book> bookList = bookManager.getBookList();
                Log.i("test",bookList.toString());
            }catch (Exception e){

            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {


        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = new Intent(this, BookManagerService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        unbindService(mConnection);
        super.onDestroy();

    }
}

```

这样就可以了，但是注意服务端的方法可能会很久之后才被执行，所以建议在子线程中进行操作，防止ANR

输出结果

```
 [Book{bookId=1, bookName='wahaha'}, Book{bookId=2, bookName='xiao'}]
```

然后在进一步，修改服务端数据，然后在客户端在打印出来


```
public class BookManagerActivity extends Activity {

    private static final String TAG = "BookManagerActivity";

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            IBookManager bookManager = IBookManager.Stub.asInterface(service);

            try {
                List<Book> bookList = bookManager.getBookList();
                Log.i("——----------------", bookList.toString());
                //设置数据
                bookManager.addBook(new Book(100, "礼拜"));
                //在获取
                bookList = bookManager.getBookList();
                Log.i("||||||||||||", bookList.toString());
            } catch (Exception e) {

            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {


        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = new Intent(this, BookManagerService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        unbindService(mConnection);
        super.onDestroy();

    }
}

```

输出结果

```
[Book{bookId=1, bookName='wahaha'}, Book{bookId=2, bookName='xiao'}, Book{bookId=100, bookName='礼拜'}]
```

在进一步
这个时候有一个需求就是客户端厌倦了，想让服务端主动告知当前的书籍内容，就是一个观察者模式，这个时候就要在创建一个AIDL接口去实现
IOnNewBookArrivedListener 接口
```

// IOnNewBookArrivedListener.aidl
package com.smart.kaifa;

// Declare any non-default types here with import statements
import com.smart.kaifa.Book;
interface IOnNewBookArrivedListener {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
            double aDouble, String aString);

    void onNewBookArrived(in Book newBook);


}

```

新加了之后要在原先的AIDL文件中加上方法来注册Listener
```

package com.smart.kaifa;

// Declare any non-default types here with import statements
import com.smart.kaifa.Book;
interface IBookManager {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
            double aDouble, String aString);


    List<Book> getBookList();
    void addBook(in Book book);


    void registerListener(IOnNewBookArrivedListener listener);
    void unregisterListener(IOnNewBookArrivedListener listener);
}

```


服务器端代码的修改

```
public class BookManagerService extends Service {

    private static final String TAG = "BMS";
    private AtomicBoolean atomicBoolean = new AtomicBoolean(false);

    private CopyOnWriteArrayList<Book> mBookList = new CopyOnWriteArrayList<>();
    private CopyOnWriteArrayList<IOnNewBookArrivedListener> mListenerList = new CopyOnWriteArrayList<>();

    private Binder mBinder = new IBookManager.Stub() {
        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

        }

        @Override
        public List<Book> getBookList() throws RemoteException {
            return mBookList;
        }

        @Override
        public void addBook(Book book) throws RemoteException {
            mBookList.add(book);
        }

        @Override
        public void registerListener(IOnNewBookArrivedListener listener) throws RemoteException {
            //获取客户端对象
            if (!mListenerList.contains(listener)) {
                mListenerList.add(listener);
            }
        }

        @Override
        public void unregisterListener(IOnNewBookArrivedListener listener) throws RemoteException {
            if (mListenerList.contains(listener)) {
                mListenerList.remove(listener);
            }
        }
    };


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    private void onNewBookArrived(Book book) {
        mBookList.add(book);
        for (int i = 0; i < mListenerList.size(); i++) {
            IOnNewBookArrivedListener iOnNewBookArrivedListener = mListenerList.get(i);

            try {
                iOnNewBookArrivedListener.onNewBookArrived(book);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();
        mBookList.add(new Book(1, "wahaha"));
        mBookList.add(new Book(2, "xiao"));


        new Thread(new ServiceWorker()).start();
    }


    private class ServiceWorker implements Runnable {


        @Override
        public void run() {
            while (!atomicBoolean.get()) {
                try {
                    Thread.sleep(3000);

                } catch (Exception e) {

                }

                int bookId = mBookList.size() + 1;
                Book newBook = new Book(bookId, "new bool #" + bookId);
                onNewBookArrived(newBook);
            }
        }
    }
}

```


客户端代码


```

public class BookManagerActivity extends Activity {

    private static final String TAG = "BookManagerActivity";
    private static final int TAG_ID = 1;

    private Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case  1:


                    Log.i(TAG,"receove book"+msg.obj);
                    break;

                default:
                    super.handleMessage(msg);
            }



        }
    };

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            bookManager = IBookManager.Stub.asInterface(service);

            try {
                List<Book> bookList = bookManager.getBookList();
                Log.i("——----------------", bookList.toString());
                //设置数据
                bookManager.addBook(new Book(100, "礼拜"));
                //在获取
                bookList = bookManager.getBookList();
                Log.i("||||||||||||", bookList.toString());


                bookManager.registerListener(onNewBookArrivedListener);
            } catch (Exception e) {

            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {


        }
    };
    private IBookManager bookManager;
    private IOnNewBookArrivedListener onNewBookArrivedListener= new IOnNewBookArrivedListener.Stub() {
        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

        }

        @Override
        public void onNewBookArrived(Book newBook) throws RemoteException {
            mHandler.obtainMessage(1,newBook).sendToTarget();
        }
    };
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = new Intent(this, BookManagerService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        if(bookManager!=null&&bookManager.asBinder().isBinderAlive()){
            try {
                bookManager.unregisterListener(onNewBookArrivedListener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        unbindService(mConnection);
        super.onDestroy();


    }
}

```

可以这么理解 客户端连接服务端 这个时候客户端持有一个服务端的Stub，在客户端启动服务端之后，服务端也持有一个客户端的Stub，这样就实现了双方的通信，看客户端将传递过去IOnNewBookArrivedListener.Stub()，然后服务端在绑定服务的时候传递IBookManager.Stub() 这样就互相持有对象的一个stub，这样就可以通信了。

但是这个时候我们关闭的时候发现并没有移除，定时器还在每隔3s传递一次数据。这个是因为虽然在移除的时候和注册的时候传递的是同一个对象，但是在服务端，获取的是不用的对象，可以理解为地址不同但是值相同的对象，所以用这种方式无法解开注册，这个时候系统为我们提供了一个跨进程删除listener的接口，RemoteCallbackList

```
public class RemoteCallbackList<E extends IInterface>

```

所有的AIDL接口都实现了IInterface 接口，他的工作原理很简单，他在内部有一个Map结果专门用来保存所有的AIDL回调，这个Map的key是IBinder类型，value是Callback类型，如下图所示。
```
ArrayMap<IBander,Callback> mCallbacks=new arrayMap<IBander,Callback>
```
其中Callback中分装了真正远程listener，当客户端注册listener的时候，他会把这个listener的信息存入mCallbacks中，其中key和value分别通过下面的方式获得：

```
IBinder key=listener.asBinder();
Callback value=new Callback(listener,cookie)
```
在这个东西的底层，使用的是一个Map,虽然说多次跨进程传输客户端的同一个客户端的同一个对象会在服务端生成不同的对象，但是这些新生成的对象有一个共同点，那就是他们底层的Binder是同一个，利用这个特性，就可以实现上面我们无法实现的功能，当客户端注册的时候，我们只要遍历服务端所有的listener，罩住那个和解除注册listener具有相同的binder对象的服务端listener并且把它删除就可以了。这个就是remoteCallBackList为我们做的事情。同时remoteCallBackList还有一个很有用的功能，就是当客户端进程终止之后，他会自动移除客户端所注册的listener，另外，remoteCallbackList内部自动实现了线程同步的功能，所以我们使用它来注册和解注册时，不需要做额外的线程同步工作。由此可见这个类还是很有用的。

RemoteCallbackLlist使用起来很简单，我们要对BookManagerService做一些修改，首先要创建一个RemoteCallbackList对象来替代之前的CopyOnWriteArrayList。

```
private RemoteCallbackList<IOnNewBookArrivedLisstener> mListenerList=new remoteCallbackList<IOnNewBookArrivedListener>;

```

然后，修改registerListener和unregisterListener这两个接口的实现，如下

```
@Override
  public void registerListener(IOnNewBookArrivedListener listener) throws RemoteException {

   mListenerList.register(listener);

  }

  @Override
  public void unregisterListener(IOnNewBookArrivedListener listener) throws RemoteException {
      mListenerList.unregister(listener);
  }

```


接下来要修改onNewBookArrived方法，当有新书时，我们要通知所有已注册的listener，如下所示。

```

    private void onNewBookArrived(Book book) {
        mBookList.add(book);
        final int N = mListenerList.beginBroadcast();
        for (int i = 0; i < N; i++) {
            IOnNewBookArrivedListener listener = mListenerList.getBroadcastItem(i);
            if (listener != null) {

                try {
                    listener.onNewBookArrived(book);
                } catch (Exception e) {

                }

            }

        }

        mListenerList.finishBroadcast();
    }



```

BookManagerService的修改已经修改完毕了，使用RemoteCallbackList，有一点需要注意，我们无法向操作list一样去操作他，虽然他的名字中也带有一个List，但是他并不是一个List，遍历RemoteCallbackList，必须要按照下面的方式执行，其中 **beginBroadcast** 和 **finishBoradcask** 必须要配对使用，哪怕我们仅仅要获取RemoteCallbackList中的元素个数，这是必须要注意的地方。

```
final int N = mListenerList.beginBroadcast();
for (int i = 0; i < N; i++) {
    IOnNewBookArrivedListener listener = mListenerList.getBroadcastItem(i);
    if (listener != null) {
        // TODO

    }

}

mListenerList.finishBroadcast();

```

到这里AIDL的使用方式基本已经介绍完了，但是，有几点还需要再次说明一下。我们知道，客户端调用远程服务的方法，被调用的方法运行在服务端的Binder线程池中，同时客户端的线程会挂起，这个时候如果服务端的方法执行比较耗时，就会导致客户端线程长时间阻塞在这里，而如果这个客户端的线程是UI线程的话，就会导致客户端ANR，这个要求在客户端使用子线程去访问，但是客户端的OnServiceConnected和OnServiceDisconnected方法都运行在UI线程中，所以也不可以在他们里面直接调用服务端的耗时方法，这点要注意。另外由于服务端方法本省运行在服务端的Binder线程池中，所以服务单本身就可以执行大量的耗时操作，这个时候切记不要在服务端的方法中开启线程去执行异步，除非你能明确自己在干什么。否则不建议这么做。

这个时候可以模拟一下服务端的方法是耗时操作

```
@Override
      public List<Book> getBookList() throws RemoteException {
          SystemClock.sleep(10000);
          return mBookList;
      }
```
这个时候就会提示ANR。
要避免这样的问题，只要将调用客户端的调用放在子线程中

```
private ServiceConnection mConnection = new ServiceConnection() {
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {

        bookManager = IBookManager.Stub.asInterface(service);

        try {

            new Thread(new Runnable() {
                @Override
                public void run() {
                    List<Book> bookList = null;
                    try {
                        bookList = bookManager.getBookList();
                        Log.i("——----------------", bookList.toString());
                        //设置数据
                        bookManager.addBook(new Book(100, "礼拜"));
                        //在获取
                        bookList = bookManager.getBookList();
                        Log.i("||||||||||||", bookList.toString());


                        bookManager.registerListener(onNewBookArrivedListener);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                }
            }).start();

        } catch (Exception e) {

        }
    }


```
同理，当远程服务端需要调用客户端的listener中的方法时，被调用的方法运行在Binder线程池中，只不过是客户端的线程池中，所以同样不可以在服务端中调用客户端的耗时方法，比如onNewBookArrived方法,所以要放在非UI线程

```
    private void onNewBookArrived(final Book book) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                mBookList.add(book);
                final int N = mListenerList.beginBroadcast();
                for (int i = 0; i < N; i++) {
                    IOnNewBookArrivedListener listener = mListenerList.getBroadcastItem(i);
                    if (listener != null) {
                        try {
                            listener.onNewBookArrived(book);
                        } catch (Exception e) {
                        }
                    }
                }
                mListenerList.finishBroadcast();
            }
        }).start();
    }
```

另外，由于客户端的IOnNewBookArrivedListener中的onNewBookArrived方法运行在客户端的Binder线程池中
```
private IOnNewBookArrivedListener onNewBookArrivedListener= new IOnNewBookArrivedListener.Stub() {
    @Override
    public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

    }

    @Override
    public void onNewBookArrived(Book newBook) throws RemoteException {
        mHandler.obtainMessage(1,newBook).sendToTarget();
    }
};
```

因此不能在这个里面执行UI操作。

为了程序的健壮性，我们还需要你做一件事情，Binder是可能意外死亡的，通常是服务端进程意外终止的，所以当我们需要重新连接服务。有两种方法，第一种方法是给Binder设置Deathrecipient监听，当Binder死亡的时候，我们会受到binderDied方法的回调，然后我们在binderDied重新连接远程服务。另外一种方法是在onServiceDisconnected重新连接，可以随便使用一种。区别是onServiceDisconnected在客户端的UI线程中被回调，而binderDIed在客户端的Binder线程池中被回调。也就是说在BinderDied中我们不能访问UI，这个就是区别。

下面就是要进行权限验证功能，防止其他人非法连接。
这里有两种方法。
第一种方法：我们可以在onBind中进行验证，验证不通过就直接返回null，这样验证失败的客户端就无法绑定服务。至于验证方式可以有多重，比如使用permission这种方式验证。不过要在清单文件中加权限

```
<permission android:name="xxxx" android:protectionLevel="normal"></permission>
```
uses-permission和permission 的区别

* uses-permission是申请权限:这个常用，就不介绍了

* permission是自己定义权限
 它可以采用由 Android 定义（如 android.Manifest.permission 中所列）或由其他应用声明的任何权限。或者，它也可以定义自己的权限。新权限用 < permission > 元素来声明。 例如，Activity 可受到如下保护：
```
<permission android:description="string resource"
android:icon="drawable resource"
android:label="string resource"
android:name="string"
android:permissionGroup="string"
android:protectionLevel=["normal" | "dangerous" |
"signature" | "signatureOrSystem"] />

```

* android:description 对权限的描述，一般两句话，第一句描述这个权限所针对的操作，第二句话告诉用户授予APP这个权限带来的后果
* android:icon 图片资源路径
* android:label 对权限的一个简短描述
* android:name 权限的唯一标识，一般都是使用包名加权限名
* android:permissionGroup 权限所属权限组的名称
* android:protectionLevel 权限的等级
  * normal 最低的等级，声明次权限的app，系统默认授予次权限，不会提示用户
  * dangerous 权限对应的操作有安全风险，系统在安装声明此权限的app时会提示用户
  * signature 权限声明的操作只针对使用同一个证书签名的app开放
  * signatureOrSystem 于signature 类似，只是增加了rom中自带的app的声明
  * 注意：android:name属性是必须的，其他的可选，未写的系统会制定默认值

简单的使用

```
<manifest . . . >
    <permission android:name="com.example.project.DEBIT_ACCT" . . . />
    <uses-permission android:name="com.example.project.DEBIT_ACCT" />
    . . .
    <application . . .>
     <activity android:name="com.example.project.FreneticActivity"
                  android:permission="com.example.project.DEBIT_ACCT"
                 . . . >
          . . .
        </activity>
    </application>
</manifest>
```
这样就为这个activity这个组件单独加了一个权限


第二种方法是：我们可以在onTransact中进行权限验证，如果验证失败，直接返回false，这样服务端就不用终止执行AIDL中的方法达到保护服务端的目的，具体的验证方式有很多，可以采用permission验证。具体实现方式和第一种是一样的，还可以采用Uid和Pid来做验证，通过getCallingUid和getCallingPid可以拿到客户端所属的Uid和Pid，通过这两个参数我们可以做一些验证工作，比如验证包名。下面就是一个demo 既验证了Permission同时也验证了包名。如果一个应用向远程调用一个服务，要使用自定义权限BOOK_Service，其次要包含包名xiao.smart


```

public boolean onTransact(int code ,Parcel data,Parcel reply,int flags) throws RemoteException{
  int check=checkCallubgOrSelfPerssion("BOOK_Service");
  if(check=PackageManager.PERMISSION_DENIED){
    return false;
  }
  String packageName=null;
  String[] packages=getPackageManager().getPackageManagerforUid(getCalling_Uid);
  if(packages!=null&&packages.length>0){
    packageName=package[0];
  }
  if(!packageName.startWith("xiao.smart")){
    return false;
  }
  return super.onTransact(code,data,reply,flags);
}


```

### 使用ContentProvide

ContentProvide是Android中提供的专门用于不同应用间进行数据共享。从这一点看，他天生就适合进程间通信。和Messenger一样，ContentProvider的底层实现同样也是Binder，由此可见，Binder在Android系统中是多么重要。
我们无需关心底层细节即可轻松实现IPC，ContentProvide虽然使用起来简单，但是他的细节有很多，比如防止sql注入，权限控制等等。

这里只是单纯的讲解一下他的跨进程工作机制

系统预制了许多ContentProvide ，比如通讯录信息，日程表信息，要跨进程访问这些信息，只需要通过ContentResolver的query，update，insert和delete方法就可以。这里自定义一个ContentProvide，并演示如何在其他应用中使用contentProvide。下面要实现6个抽象方法，

```

public class BookProvide extends ContentProvider {

    @Override
    public boolean onCreate() {
        Log.i("test", "onCreate   :"+Thread.currentThread().getName());
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        Log.i("test", "query   :"+Thread.currentThread().getName());
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        Log.i("test", "getType   :"+Thread.currentThread().getName());
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        Log.i("test", "insert   :"+Thread.currentThread().getName());
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        Log.i("test", "delete   :"+Thread.currentThread().getName());
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        Log.i("test", "update   :"+Thread.currentThread().getName());
        return 0;
    }
}


```

除了oncreate 运行在主线程，其他都运行在Binder线程池（这个后面讲）

ContentProvide主要以表格的形式组织数据，并且可以包含多个表，和数据库有点类似。除了表格形式，ContentProvide还支持文件数据，比如图片，视频等。文件数据和表格数据的结构不同，因此处理这类数据是可以在ContentProvide中返回文件的句柄给外界，让从而让文件来访问COntentProvide中的文件信息，Android提供了MediaStore功能就是文件类型的ContentProvide，详情可以参考MediaStore。另外虽然ContentProvide底层数据看起来想一个Sqlite数据库，但是ContentProvide对底层数据没有任何要求。我们既可以使用Sqlite，也可以使用普通文件。甚至可以采用内存中的一个对象进行数据存储。这一点可以在后面的章节中进行介绍。

在实现了那6个方法之后，我们需要在清单文件中注册，

```
<provider
     android:authorities="com.xxx.xxx"// ContentProvide的唯一标识
     android:process=":provide" //运行在其他进程中
     android:permission="com.xxx" //自定义权限
     android:name=".BookProvide" //名称
     ></provider>
```

`android:authorities="com.xxx.xxx" ` 这个东西是唯一标识，通过这个标识，外部应用就可以访问我们的BookProvider。因此`android:authorities="com.xxx.xxx" `必须是唯一的，所以建议使用包名，然后为了安全起见，给他增加了权限验证。当然还可以加入 `   android:readPermission="xxx"`和`  android:writePermission="xxx"`
* readPermission:使用Content Provider的查询功能所必需的权限，即使用ContentProvider里的query()函数的权限；
* writePermission：使用ContentProvider的修改功能所必须的权限，即使用ContentProvider的insert()、update()、delete()函数的权限；
* permission：客户端读、写 Content Provider 中的数据所必需的权限名称。 本属性为一次性设置读和写权限提供了快捷途径。 不过，readPermission和writePermission属性优先于本设置。 如果同时设置了readPermission属性，则其将控制对 Content Provider 的读取。 如果设置了writePermission属性，则其也将控制对 Content Provider 数据的修改。也就是说如果只设置permission权限，那么拥有这个权限的应用就可以实现对这里的ContentProvider进行读写；如果同时设置了permission和readPermission那么具有readPermission权限的应用才可以读，拥有permission权限的才能写！也就是说只拥有permission权限是不能读的，因为readPermission的优先级要高于permission；如果同时设置了readPermission、writePermission、permission那么permission就无效了。

注册了ContentProvide之后就可以在外部应用中访问它了，为了方便，其实是偷懒，就在同一个app中进行，不过开两个进程

加入自定义权限
```
<uses-permission android:name="com.xxx"/>
 <permission
     android:name="com.xxx"
     android:label="provider pomission"
     android:protectionLevel="normal" />
 <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"></uses-permission>

```

//看一下不同的线程信息 是放在Binder线程池中的
```
02-23 23:39:34.130 8865-8865/? I/test: onCreate   :main
02-23 23:39:34.136 8865-8878/? I/test: query   :Binder:8865_2
02-23 23:39:34.138 8865-8877/? I/test: query   :Binder:8865_1
02-23 23:39:34.139 8865-8878/? I/test: query   :Binder:8865_2
```

```

public class ProvideActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provide);
        Button query = findViewById(R.id.button);
        Button add = findViewById(R.id.button4);
        Button delete = findViewById(R.id.button2);
        Button update = findViewById(R.id.button3);
        Button start = findViewById(R.id.button5);
        query.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              // 请求
                Uri url = Uri.parse("content://com.xxx.xxx");

                getContentResolver().query(url, null, null, null, null);
                getContentResolver().query(url, null, null, null, null);
                getContentResolver().query(url, null, null, null, null);
            }
        });

    }
}

```

activity 布局
```
<?xml version="1.0" encoding="utf-8"?>
<android.support.constraint.ConstraintLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <Button
        android:id="@+id/button"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="query"
        tools:layout_editor_absoluteX="0dp"
        tools:layout_editor_absoluteY="2dp" />

    <Button
        android:id="@+id/button2"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="8dp"
        android:text="delete"
        app:layout_constraintTop_toBottomOf="@+id/button"
        tools:layout_editor_absoluteX="0dp" />

    <Button
        android:id="@+id/button3"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="8dp"
        android:text="update"
        app:layout_constraintTop_toBottomOf="@+id/button2"
        tools:layout_editor_absoluteX="0dp" />

    <Button
        android:id="@+id/button4"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="8dp"
        android:text="add"
        app:layout_constraintTop_toBottomOf="@+id/button3"
        tools:layout_editor_absoluteX="0dp" />

    <Button
        android:id="@+id/button5"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="8dp"
        android:text="start"
        app:layout_constraintTop_toBottomOf="@+id/button4"
        tools:layout_editor_absoluteX="0dp" />

    <TextView
        android:id="@+id/textView"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Button"
        tools:layout_editor_absoluteX="335dp"
        tools:layout_editor_absoluteY="313dp" />
</android.support.constraint.ConstraintLayout>
```

现在这个demo已经跑通了
然后进一步完善

首先创建一个数据库的操作类,使用android 提供的SQLiteOpenHelper

```
public class DbOpenHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "book_provide.db";
    public static final String BOOK_NAME = "book";
    public static final String USER_NAME = "user";
    private static final int DB_VERSION = 1;
    //创建表的语句
    private String CREATE_BOOK_TABLE = "CREATE TABLE IF NOT EXISTS " + BOOK_NAME + " ( _id INTEGER PRIMARY KEY, " + "name TEXT)";
    private String CREATE_USER_TABLE = "CREATE TABLE IF NOT EXISTS " + USER_NAME + " ( _id INTEGER PRIMARY KEY, " + "name TEXT," + "sex INT)";
    public DbOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }
    public DbOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }
    public DbOpenHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_BOOK_TABLE);
        db.execSQL(CREATE_USER_TABLE);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}

```


然后在ContentProvide中使用sql

```
public class BookProvide extends ContentProvider {
    private static final String TAG = "BookProvide";
    private static final String AUTHORITY = "com.xxx.xxx";
    private static final Uri BOOK_URI = Uri.parse("content://" + AUTHORITY + "/book");
    private static final Uri USER_URI = Uri.parse("content://" + AUTHORITY + "/user");
    public static final int book_uri_code = 0;
    public static final int user_uri_code = 1;
    private static final UriMatcher sUriMathcer = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sUriMathcer.addURI(AUTHORITY, "book", book_uri_code);
        sUriMathcer.addURI(AUTHORITY, "user", user_uri_code);
    }
    private Context mContext;
    private SQLiteDatabase mDb;


    /**
     * 初始化并模拟数据
     */
    private void initProvideDate() {
        mDb = new DbOpenHelper(mContext).getWritableDatabase();
        mDb.execSQL("delete from " + DbOpenHelper.BOOK_NAME);
        mDb.execSQL("delete from " + DbOpenHelper.USER_NAME);
        mDb.execSQL("insert into  book values(3,'Android');");
        mDb.execSQL("insert into  book values(4,'IOS');");
        mDb.execSQL("insert into  book values(5,'HTML5');");
        mDb.execSQL("insert into  user values(1,'jake',1);");
        mDb.execSQL("insert into  user values(2,'jasmine',0);");

    }


    private String getTableName(Uri uri) {
        String tableName = null;
        switch (sUriMathcer.match(uri)) {
            case book_uri_code:
                tableName = DbOpenHelper.BOOK_NAME;
                break;
            case user_uri_code:
                tableName = DbOpenHelper.USER_NAME;
                break;
            default:
                break;
        }
        return tableName;
    }


    @Override
    public boolean onCreate() {
        //这个是放在主线程的 要注意
        Log.i("test", "onCreate   :" + Thread.currentThread().getName());
        mContext = getContext();
        initProvideDate();
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        Log.i("test", "query   :" + Thread.currentThread().getName());
        String table = getTableName(uri);
        if (table == null) {
            throw new IllegalArgumentException("Unsupported URI :" + uri);
        }
        return mDb.query(table, projection, selection, selectionArgs, null, null, sortOrder, null);
    }
    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        Log.i("test", "getType   :" + Thread.currentThread().getName());
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        Log.i("test", "insert   :" + Thread.currentThread().getName());
        String table = getTableName(uri);
        if (table == null) {
            throw new IllegalArgumentException("Unsupported URI :" + uri);
        }
        mDb.insert(table, null, values);
        return uri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        Log.i("test", "delete   :" + Thread.currentThread().getName());
        String table = getTableName(uri);
        if (table == null) {
            throw new IllegalArgumentException("Unsupported URI :" + uri);
        }
        int count = mDb.delete(table, selection, selectionArgs);
        if (count > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return count;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        Log.i("test", "update   :" + Thread.currentThread().getName());
        String table = getTableName(uri);
        if (table == null) {
            throw new IllegalArgumentException("Unsupported URI :" + uri);
        }
        int row = mDb.update(table, values, selection, selectionArgs);
        if (row > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return row;
    }
}

```



在activity中使用

```
public class ProvideActivity extends Activity {
    private Uri url;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provide);
        url = Uri.parse("content://com.xxx.xxx/book");
        ContentValues values = new ContentValues();
        values.put("_id", 6);
        values.put("name", "程序设计的艺术");
        getContentResolver().insert(url, values);
        Cursor query1 = getContentResolver().query(url, new String[]{"_id", "name"}, null, null, null);
        while (query1.moveToNext()) {
            Book book = new Book();
            book.bookId = query1.getInt(0);
            book.bookName = query1.getString(1);
            Log.i("test", "query book :" + book.toString());
        }
        query1.close();
        url = Uri.parse("content://com.xxx.xxx/user");
        Cursor query = getContentResolver().query(url, new String[]{"_id", "name", "sex"}, null, null, null);
        while (query.moveToNext()) {
            User user = new User();
            user.userId = query.getInt(0);
            user.userName = query.getString(1);
            Log.i("test", "query user :" + user.toString());
        }
        query.close();
    }
}
```

输出的日志

```

02-25 16:13:51.708 29236-29236/com.smart.kaifa I/test: query book :Book{bookId=3, bookName='Android'}
02-25 16:13:51.708 29236-29236/com.smart.kaifa I/test: query book :Book{bookId=4, bookName='IOS'}
02-25 16:13:51.708 29236-29236/com.smart.kaifa I/test: query book :Book{bookId=5, bookName='HTML5'}
02-25 16:13:51.708 29236-29236/com.smart.kaifa I/test: query book :Book{bookId=6, bookName='程序设计的艺术'}
02-25 16:13:51.728 29236-29236/com.smart.kaifa I/test: query user :User{userId=1, userName='jake', isMale=false}
02-25 16:13:51.728 29236-29236/com.smart.kaifa I/test: query user :User{userId=2, userName='jasmine', isMale=false}
```

这里有一个注意点，因为ContentProvide的增删改查方法都是放在Binder线程池中的，因此在做的时候要注意线程同步，如果是同一个SqliteDatebase，可以正确应对并发的问题，但是如果是有多个SqliteDatebase这个时候并发就会有问题。


### 使用Socket
socket分为两种，TCP和UDP
* TCP是安全的，应为他在通信的时候有一个三次握手的机制，确定目的地址存在且可以通信的情况下，他才会进行通信
* UDP是不安全的，他不管目的地址是否存在，把数据直接发送。
这里演示一个聊天室的应用，

要使用网络通信，首先要添加网络通信的权限


```
<uses-permission android:name="android.permission.INTERNET"></uses-permission>
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"></uses-permission>
```

其次不能在主线程中访问网络，会抛出如下异常，android.os.NetworkOnMainThreadException
这个程序比较简单，首先在一个远程服务创建一个TCP服务，然后在主界面中连接TCP服务，连接上之后就可以给服务端发送消息，为了更好的展示Socket工作机制，在服务端做了处理，可以多个客户端连接。

先看一下服务端的设计

```
public class SocketService extends Service {

    private boolean mIsServiceDestoryed = false;

    private String[] mDefinedMessages = new String[]{"你好", "你叫什么名字啊", "今天天气不错呦"};


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        new Thread(new TcpServer()).start();
    }


    class TcpServer implements Runnable {


        @Override
        public void run() {
            ServerSocket serverSocket = null;
            try {
                serverSocket = new ServerSocket(8688);

            } catch (Exception e) {

            }

            while (!mIsServiceDestoryed) {
                try {
                    //接受客户端请求
                    final Socket socket = serverSocket.accept();
                    Log.i("test", "accept");
                    new Thread() {
                        @Override
                        public void run() {
                            try {
                                responseClient(socket);
                            } catch (Exception e) {

                            }
                        }
                    }.start();

                } catch (Exception e) {

                }
            }

        }
    }


    private void responseClient(Socket client) throws IOException {

        BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));


        PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(client.getOutputStream())), true);

        out.print("欢迎来到聊天室");

        while (!mIsServiceDestoryed) {
            String str = in.readLine();
            Log.i("client :", "" + str);
            if (str == null) {
                break;
            }
            int i = new Random().nextInt(mDefinedMessages.length);
            String msg = mDefinedMessages[i];
            out.println(msg);
            Log.i("send :", msg);
            out.close();
            in.close();
            client.close();
        }

    }


}


```
清单文件配置
```
<service
        android:name=".SocketService"
        android:process=":xxxxx"></service>

    <activity android:name=".SocketActivity">

        <intent-filter>
            <action android:name="android.intent.action.MAIN" />

            <category android:name="android.intent.category.LAUNCHER" />
        </intent-filter>
    </activity>
```


客户端的UI
```
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:orientation="vertical"
    android:layout_height="match_parent">



    <EditText
        android:id="@+id/edit"
        android:hint="@string/app_name"
        android:layout_width="match_parent"
        android:layout_height="wrap_content" />

    <TextView
        android:id="@+id/text"
        android:background="#00ffff"
        android:text=" "
        android:layout_width="match_parent"
        android:layout_height="wrap_content" />

    <Button
        android:id="@+id/botton"
        android:text="发送"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content" />
</LinearLayout>
```

客户端的代码
```
public class SocketActivity extends Activity implements View.OnClickListener {

    private static final int MESSAGE_RECEIVE_NEW_MSG = 1;
    private static final int MESSAGE_SOCKET_CONNECTED = 2;
    private Button mSendButton;
    private TextView mMessageTextView;
    private EditText mMessageEditText;

    private PrintWriter mPrintWriter;

    private Socket mClientSocket;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_RECEIVE_NEW_MSG:
                    mMessageTextView.setText(mMessageTextView.getText() + (String) msg.obj);
                    break;

                case MESSAGE_SOCKET_CONNECTED:
                    mSendButton.setEnabled(true);
                    break;

                default:
                    break;
            }


            super.handleMessage(msg);
        }
    };



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_socket);

        mMessageTextView = findViewById(R.id.text);
        mMessageEditText = findViewById(R.id.edit);
        mSendButton = findViewById(R.id.botton);

        Intent service = new Intent(this, SocketService.class);
        startService(service);

        new Thread(new Runnable() {
            @Override
            public void run() {
              //连接远程服务器
                connectTcpService();
            }
        }).start();

        mSendButton.setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {

        if (mClientSocket != null) {
            try {
                mClientSocket.shutdownInput();
                mClientSocket.close();
            } catch (Exception e) {

            }
        }
        super.onDestroy();

    }


    private void connectTcpService() {

        Socket socket = null;


        while (socket == null) {
            try {
                socket = new Socket("localhost", 8688);
                mClientSocket = socket;
                mPrintWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                mHandler.sendEmptyMessage(MESSAGE_SOCKET_CONNECTED);
            } catch (Exception e) {

            }

            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                //开启一个死循环监听服务端的返回，有消息就通过handler发送
                while (!SocketActivity.this.isFinishing()) {
                    String message = br.readLine();
                    Log.i("test", "" + message);
                    if (message != null) {
                        String time = formateDateTime(System.currentTimeMillis());
                        final String showedMsg = "server " + time + " : " + message;
                        mHandler.obtainMessage(MESSAGE_RECEIVE_NEW_MSG, showedMsg).sendToTarget();
                    }
                }
                Log.i("test", "quit...");
                mPrintWriter.close();
                br.close();
                socket.close();
            } catch (Exception e) {
            }
        }
    }


    @Override
    public void onClick(final View v) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (v == mSendButton) {
                    final String msg = mMessageEditText.getText().toString();
                    if (!TextUtils.isEmpty(msg) && mPrintWriter != null) {
                        mPrintWriter.println(msg);
                        mMessageEditText.setText("");
                        String time = formateDateTime(System.currentTimeMillis());
                        final String showMsg = "self  " + time + ":" + msg + "\n";
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mMessageTextView.setText(mMessageTextView.getText() + showMsg);
                            }
                        });

                    }
                }
            }
        }).start();


    }


    // 格式化时间
    private String formateDateTime(long time) {

        return new SimpleDateFormat("(HH:mm:ss)").format(new Date(time));
    }
}

```


![Alt text](device-2018-02-25-214156.png "socket 聊天室截图")

## Binder 连接池
这里有一个点，就是现在有100个业务需要使用AIDL来进行通信，那么我们应该怎么处理呢，创建100个Service吗？所以这里就需要减少Service的使用，将他尽量放在一个Service中进行处理。

在这种模式下，整个工作机制是这样的：每个业务模块创建自己的AIDL接口并实现此接口。这个时候不同业务模块之间是不能有耦合的，所有实现细节我们要单独开来，然后像Service端提供自己唯一标识和其对应的Binder对象。对于服务端来讲，只需要一个Service就可以了，服务端提供一个queryBinder接口，这个接口更具业务模块特征来返回相应的Binder给她们。不同的业务模块拿到所需要的Binder对象后就可以远程调用方法了。由此可见Binder连接池的主要作用是将每个业务模块的Binder请求统一转发到远程服务中执行了。从而避免重复重复创建Service的过程。她的工作原理如下

![Alt text](图像1519566752.png "binder 连接池")

下面来写一个demo来展示一下
ISecurityCenter 是一个通用的接口提供加密功能
```
public interface ISecurityCenter {
    String encrypt(String content);
    String decrypt(String password);
}

```
ISecurityCenter 存根的实现
```
public class SecurityCenterImpl extends ISecurityCenter.Stub {
    private static final char SECOND_CODE = '^';

    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     *
     * @param anInt
     * @param aLong
     * @param aBoolean
     * @param aFloat
     * @param aDouble
     * @param aString
     */
    @Override
    public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

    }

    @Override
    public String encrypt(String content) throws RemoteException {
        char[] chars = content.toCharArray();
        Log.i("test","---");
        for (int i = 0; i < chars.length; i++) {
            chars[i] ^= SECOND_CODE;
        }
        Log.i("test","---");
        return new String(chars);
    }

    @Override
    public String decrypt(String password) throws RemoteException {
        Log.i("test","--////////*****");
        return encrypt(password);
    }
}

```

ICompute 接口提供加法功能
```
public interface ICompute {

    int add(int a,int b);
}

```
ICompute 存根的实现
```
public class ComputeImpl extends ICompute.Stub {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     *
     * @param anInt
     * @param aLong
     * @param aBoolean
     * @param aFloat
     * @param aDouble
     * @param aString
     */
    @Override
    public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

    }

    @Override
    public int add(int a, int b) throws RemoteException {

        return a+b;
    }
}

```

BinderPool 接口提供 用来实现线程池
```
interface IBinderPool {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
            double aDouble, String aString);


            IBinder queryBinder(int binderCode);
}

```




BinderPool
```
public class BinderPool {
    private static final String TAG = "BinderPool";
    public static final int BINDER_NONE = -1;
    public static final int BINDER_COMPUTE = 0;
    public static final int BINDER_SECURITY_CENTER = 1;

    private Context mContext;
    private IBinderPool mBinderPool;


    private IBinder.DeathRecipient mBinderPoolDeathRecipient = new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {

            mBinderPool.asBinder().unlinkToDeath(mBinderPoolDeathRecipient, 0);
            mBinderPool = null;
            connectBinderPoolService();

        }
    };

    public static class BinderPoolImpl extends IBinderPool.Stub {

        /**
         * Demonstrates some basic types that you can use as parameters
         * and return values in AIDL.
         *
         * @param anInt
         * @param aLong
         * @param aBoolean
         * @param aFloat
         * @param aDouble
         * @param aString
         */
        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

        }
        //不同情况返回不同的存根实现
        @Override
        public IBinder queryBinder(int binderCode) throws RemoteException {
            IBinder binder = null;
            switch (binderCode) {
                case BINDER_SECURITY_CENTER:
                    binder = new SecurityCenterImpl();
                    break;

                case BINDER_COMPUTE:
                    binder = new ComputeImpl();
                    break;

                default:
                    break;

            }
            return binder;
        }
    }


    private static volatile BinderPool sInstance;

    //这个是java 1.5之后提供的一个多线程控制工具
    private CountDownLatch mConnectBinderPoolCountDownLatch;
    ServiceConnection mBinderPoolConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBinderPool = IBinderPool.Stub.asInterface(service);
            try {
                mBinderPool.asBinder().linkToDeath(mBinderPoolDeathRecipient, 0);
            } catch (Exception e) {

            }
            mConnectBinderPoolCountDownLatch.countDown();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };


    private BinderPool(Context context) {
        mContext = context.getApplicationContext();
        connectBinderPoolService();
    }

    private synchronized void connectBinderPoolService() {


        mConnectBinderPoolCountDownLatch = new CountDownLatch(1);

        Intent service = new Intent(mContext, BinderPoolService.class);

        mContext.bindService(service, mBinderPoolConnection, Context.BIND_AUTO_CREATE);
    }


    public static BinderPool getsInstance(Context context) {
        if (sInstance == null) {
            //加入线程所 防止死锁或者重复创建
            synchronized (BinderPool.class) {
                if (sInstance == null) {
                    sInstance = new BinderPool(context);
                }
            }
        }

        return sInstance;
    }


    public IBinder queryBinder(int binderCode) {

        IBinder binder = null;
        try {
            if (mBinderPool != null) {
                binder = mBinderPool.queryBinder(binderCode);
            }
        } catch (Exception e) {
        }

        return binder;
    }


}

```
BinderPoolService
```
public class BinderPoolService extends Service {
    private static final String TAG = "BinderPoolService";
    private Binder mBinderPool = new BinderPool.BinderPoolImpl();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinderPool;
    }
}
```


activity中启动服务
```
private void doWork() {
    Log.i("msg", "点击");
    BinderPool binderPool = BinderPool.getsInstance(this);
    IBinder sequrityBinder = binderPool.queryBinder(BinderPool.BINDER_SECURITY_CENTER);
    ISecurityCenter iSecurityCenter = SecurityCenterImpl.asInterface(sequrityBinder);
    String msg = "helloword-安卓";
    Log.i("msg", msg);
    try {
        String password = iSecurityCenter.encrypt(msg);
        Log.i("password  ", password);
        Log.i("password  ", iSecurityCenter.decrypt(password));
    } catch (Exception e) {
        Log.e("test", e.getMessage());
    }


    IBinder computerBinder = binderPool.queryBinder(BinderPool.BINDER_COMPUTE);
    ICompute iCompute = ComputeImpl.asInterface(computerBinder);
    try {
        int value = iCompute.add(100, 200);
        Log.i("100+200=   ", value + "");
    } catch (Exception e) {

    }

}
```


结果
```
/msg: helloword-安卓
password: 6;221)1,:s寗匍
password: helloword-安卓
```





## 选用合适的IPC方式

|   名称  | 优点     |    缺点 |使用场景|
| ------------- |:-------------:| :-------------:|:-------------:|
|Bundle|简单易用|只能传输Bundle支持的数据类型|四大组件间进程通讯|
|文件共享|简单易用|不适合高并发场景，并且无法做到进程间的即时通讯|五并发访问情形，交换简单的数据实时性不高的场景|
|AIDL|功能强大，支持一对多并发通信，支持实时通信|使用稍微复杂，需要处理好线程同步|一对多通信并且有RPC需求|
|Messenger|功能一般，支持一对多串行通信，支持实时通信|不能很好的处理高并发情形，不能支持RPC，数据通过Message进行传输，因此只能传输Bundle支持的数据类型|低并发的一对多即时通信，五RPC需求，或者无需返回结果的RPC需求|
|ContentProvide|在数据访问方面功能强大，支持一对多并发数据共享，可通过call方法拓展其他操作|可以理解为受约束的AIDL，主要提供数据源的CRUD操作|一对多的进程间的数据共享|
|socket|功能强大，可以通过=网络传输字节流，支持一对多并发实时通信|实现细节稍微有点繁琐，不支持直接RPC|网络数据交换|

## 这里在总结一下IPC


```
package com.smart.kaifa;
public interface IBookManager extends android.os.IInterface {
    //系统帮我们生成的IBookManager 里面有一个抽象的Stub  在这个抽象的Stub里面有一个代理类
    //Stub 抽象类继承了com.smart.kaifa.IBookManager  接口 需要在下面的BookManagerService中具体的实现
    public static abstract class Stub extends android.os.Binder implements com.smart.kaifa.IBookManager {

       public static com.smart.kaifa.IBookManager asInterface(android.os.IBinder obj) {
              这个时候在bind服务的时候会调用，IBookManager.Stub.asInterface(service)  之后就返回代理，所以这是一个存根代理模式，并初始化proxy
              return new com.smart.kaifa.IBookManager.Stub.Proxy(obj);
       }

         //这个是Stub内部代理类
        private static class Proxy implements com.smart.kaifa.IBookManager {
            //这里研究一个代理内部的方法
            @Override
            public java.util.List<com.smart.kaifa.Book> getBookList() throws android.os.RemoteException {
                // 这个是系统的实现，不用管：就是讲我们调用的方法什么的进行打包,就是存储了客户端的数据信息，包括包名什么的  默认有6个 这个涉及到native层的代码了
                //在Java空间和C++都实现了Parcel，由于它在C/C++中，直接使用了内存来读取数据，因此，它更有效率 具体c的代码就不研究了
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                java.util.List<com.smart.kaifa.Book> _result;
                try {
                    // 设置标记，可以这么理解 就是告诉其他进程我要调用这个接口里面的东西  DESCRIPTOR   private static final java.lang.String DESCRIPTOR = "com.smart.kaifa.IBookManager";
                    _data.writeInterfaceToken(DESCRIPTOR);
                    // 这个时候会调用 remote 就是stub，然后通过他来区分不同的方法     
                    mRemote.transact(Stub.TRANSACTION_getBookList, _data, _reply, 0);
                    //解除标记
                    _reply.readException();
                    _result = _reply.createTypedArrayList(com.smart.kaifa.Book.CREATOR);
                return _result;
            }
        }    
    }
}

```
BookManagerService 用法
```
public class BookManagerService extends Service {
    private  static final String TAG="BMS";
    private CopyOnWriteArrayList<Book> mBookList=new CopyOnWriteArrayList<>();
    //实现抽象方法
    private Binder mBinder=new IBookManager.Stub() {
        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {
        }
        @Override
        public List<Book> getBookList() throws RemoteException {
            return mBookList;
        }
        @Override
        public void addBook(Book book) throws RemoteException {
            mBookList.add(book);
        }
    };
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // 返回binder
        return mBinder;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        mBookList.add(new Book(1,"wahaha"));
        mBookList.add(new Book(2,"xiao"));
    }
}
```

在Activity中绑定 BookManagerService 会传出ServiceConnection 连接成功之后返回Binder

```
private ServiceConnection mConnection = new ServiceConnection() {
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
      //通过这个方法拿到代理类，这个时候就可以调用服务端代码了
      IBookManager  bookManager = IBookManager.Stub.asInterface(service);

    }
    @Override
    public void onServiceDisconnected(ComponentName name) {
    }
};
```
# 第三章 View的事件体系
这个是非常重要的一个内容，一般面试的时候都会问一下的

## View基础知识
### 什么是View？
View是android中所有控件的基类，用户的交互其实是可View直接交互的，可以把它理解为界面层的抽象，所有的View可以理解为屏幕中间的一块矩形区域
ViewGroup是一个控件组。

### View的位置？
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
03-25 22:13:16.307 18794-18794/com.smart.kaifa I/第一次： left：: 70
03-25 22:13:16.307 18794-18794/com.smart.kaifa I/第一次： right：: 140
03-25 22:13:16.307 18794-18794/com.smart.kaifa I/第一次： bottom：: 140
03-25 22:13:16.307 18794-18794/com.smart.kaifa I/第一次： top：: 70
03-25 22:13:16.307 18794-18794/com.smart.kaifa I/第一次： width：: 70
03-25 22:13:16.307 18794-18794/com.smart.kaifa I/第一次： height：: 70
03-25 22:13:16.307 18794-18794/com.smart.kaifa I/第一次： x：: 70.0
03-25 22:13:16.307 18794-18794/com.smart.kaifa I/第一次： y：: 70.0
03-25 22:13:16.307 18794-18794/com.smart.kaifa I/第一次： translationX：: 0.0
03-25 22:13:16.307 18794-18794/com.smart.kaifa I/第一次： translationY：: 0.0
03-25 22:13:16.307 18794-18794/com.smart.kaifa I/第一次： scrollX：: 0.0
03-25 22:13:16.307 18794-18794/com.smart.kaifa I/第一次： scrollY：: 0.0
03-25 22:13:17.314 18794-18794/com.smart.kaifa I/第二次： ********: ***************************************************************************
03-25 22:13:17.314 18794-18794/com.smart.kaifa I/第二次： left：: 70
03-25 22:13:17.314 18794-18794/com.smart.kaifa I/第二次： right：: 140
03-25 22:13:17.315 18794-18794/com.smart.kaifa I/第二次： bottom：: 140
03-25 22:13:17.315 18794-18794/com.smart.kaifa I/第二次： top：: 70
03-25 22:13:17.315 18794-18794/com.smart.kaifa I/第二次： width：: 70
03-25 22:13:17.315 18794-18794/com.smart.kaifa I/第二次： height：: 70
03-25 22:13:17.315 18794-18794/com.smart.kaifa I/第二次： x：: 270.0
03-25 22:13:17.316 18794-18794/com.smart.kaifa I/第二次： y：: 70.0
03-25 22:13:17.316 18794-18794/com.smart.kaifa I/第二次： translationX：: 200.0
03-25 22:13:17.316 18794-18794/com.smart.kaifa I/第二次： translationY：: 0.0
03-25 22:13:17.316 18794-18794/com.smart.kaifa I/第二次： scrollX：: 0.0
03-25 22:13:17.317 18794-18794/com.smart.kaifa I/第二次： scrollY：: 0.0
```

可以看到:需要注意的是，在View的平移过程中，top和left表示原始右上角的位置信息。其值不会发生改变，此时发生改变的是x，y，translationX和translationY

x=left+translationX
y=top+translationY
这个是是不变的，交换一下顺序
left=x-translationX   
top=y-translationY

可以这么理解 x是当前相对控件的位置，而translationX是平移的距离。所以他们的差值就是开始的位置

熟悉了控件的摆放位置之后，下面就了解一下用户手指的操作时间

### Motion和TouchSlop
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
03-25 22:39:10.299 24542-24542/com.smart.kaifa I/x ：: 33.0
03-25 22:39:10.300 24542-24542/com.smart.kaifa I/y ：: 8.0
03-25 22:39:10.300 24542-24542/com.smart.kaifa I/rawX ：: 453.0
03-25 22:39:10.301 24542-24542/com.smart.kaifa I/rawY ：: 358.0

第二次点击
03-25 22:39:20.016 24542-24542/com.smart.kaifa I/x ：: 39.0
03-25 22:39:20.017 24542-24542/com.smart.kaifa I/y ：: 20.0
03-25 22:39:20.017 24542-24542/com.smart.kaifa I/rawX ：: 459.0
03-25 22:39:20.017 24542-24542/com.smart.kaifa I/rawY ：: 370.0
```


2. TouchSlop
这个是系统提供的被认为滑动的最小距离，换句话说,如果一次滑动距离小于这个值，就表示没有滑动，防止手抖，这个是和设备相关的一个变量，每一个设备值可能是不同的，
`ViewConfiguration.get(getContext).getScaledTouchSlop`来获取，过滤一些临界值

### VelocityTracker GrstureDetrctor和Scroller
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

### Scroller
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

### 第一种：使用View本身提供的scrollTo和scrollBy实现滑动

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
03-31 22:33:47.107 30190-30190/com.smart.kaifa I/第一次： left：: 0
03-31 22:33:47.107 30190-30190/com.smart.kaifa I/第一次： right：: 700
03-31 22:33:47.107 30190-30190/com.smart.kaifa I/第一次： bottom：: 700
03-31 22:33:47.107 30190-30190/com.smart.kaifa I/第一次： top：: 0
03-31 22:33:47.107 30190-30190/com.smart.kaifa I/第一次： width：: 700
03-31 22:33:47.107 30190-30190/com.smart.kaifa I/第一次： height：: 700
03-31 22:33:47.107 30190-30190/com.smart.kaifa I/第一次： x：: 0.0
03-31 22:33:47.107 30190-30190/com.smart.kaifa I/第一次： y：: 0.0
03-31 22:33:47.107 30190-30190/com.smart.kaifa I/第一次： translationX：: 0.0
03-31 22:33:47.107 30190-30190/com.smart.kaifa I/第一次： translationY：: 0.0
03-31 22:33:47.107 30190-30190/com.smart.kaifa I/第一次： scrollX：: 0.0
03-31 22:33:47.107 30190-30190/com.smart.kaifa I/第一次： scrollY：: 0.0
03-31 22:33:48.110 30190-30190/com.smart.kaifa I/第二次： ********: ***************************************************************************
03-31 22:33:48.110 30190-30190/com.smart.kaifa I/第二次： left：: 0
03-31 22:33:48.110 30190-30190/com.smart.kaifa I/第二次： right：: 700
03-31 22:33:48.110 30190-30190/com.smart.kaifa I/第二次： bottom：: 700
03-31 22:33:48.110 30190-30190/com.smart.kaifa I/第二次： top：: 0
03-31 22:33:48.111 30190-30190/com.smart.kaifa I/第二次： width：: 700
03-31 22:33:48.111 30190-30190/com.smart.kaifa I/第二次： height：: 700
03-31 22:33:48.111 30190-30190/com.smart.kaifa I/第二次： x：: 0.0
03-31 22:33:48.111 30190-30190/com.smart.kaifa I/第二次： y：: 0.0
03-31 22:33:48.111 30190-30190/com.smart.kaifa I/第二次： translationX：: 0.0
03-31 22:33:48.111 30190-30190/com.smart.kaifa I/第二次： translationY：: 0.0
03-31 22:33:48.111 30190-30190/com.smart.kaifa I/第二次： scrollX：: -100.0
03-31 22:33:48.112 30190-30190/com.smart.kaifa I/第二次： scrollY：: -100.0
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
03-31 22:37:05.765 31212-31212/com.smart.kaifa I/第一次： left：: 0
03-31 22:37:05.765 31212-31212/com.smart.kaifa I/第一次： right：: 700
03-31 22:37:05.765 31212-31212/com.smart.kaifa I/第一次： bottom：: 700
03-31 22:37:05.765 31212-31212/com.smart.kaifa I/第一次： top：: 0
03-31 22:37:05.765 31212-31212/com.smart.kaifa I/第一次： width：: 700
03-31 22:37:05.765 31212-31212/com.smart.kaifa I/第一次： height：: 700
03-31 22:37:05.766 31212-31212/com.smart.kaifa I/第一次： x：: 0.0
03-31 22:37:05.766 31212-31212/com.smart.kaifa I/第一次： y：: 0.0
03-31 22:37:05.766 31212-31212/com.smart.kaifa I/第一次： translationX：: 0.0
03-31 22:37:05.766 31212-31212/com.smart.kaifa I/第一次： translationY：: 0.0
03-31 22:37:05.766 31212-31212/com.smart.kaifa I/第一次： scrollX：: 0.0
03-31 22:37:05.766 31212-31212/com.smart.kaifa I/第一次： scrollY：: 0.0
03-31 22:37:06.768 31212-31212/com.smart.kaifa I/第二次： ********: ***************************************************************************
03-31 22:37:06.768 31212-31212/com.smart.kaifa I/第二次： left：: 0
03-31 22:37:06.768 31212-31212/com.smart.kaifa I/第二次： right：: 700
03-31 22:37:06.768 31212-31212/com.smart.kaifa I/第二次： bottom：: 700
03-31 22:37:06.768 31212-31212/com.smart.kaifa I/第二次： top：: 0
03-31 22:37:06.768 31212-31212/com.smart.kaifa I/第二次： width：: 700
03-31 22:37:06.768 31212-31212/com.smart.kaifa I/第二次： height：: 700
03-31 22:37:06.769 31212-31212/com.smart.kaifa I/第二次： x：: 0.0
03-31 22:37:06.769 31212-31212/com.smart.kaifa I/第二次： y：: 0.0
03-31 22:37:06.769 31212-31212/com.smart.kaifa I/第二次： translationX：: 0.0
03-31 22:37:06.769 31212-31212/com.smart.kaifa I/第二次： translationY：: 0.0
03-31 22:37:06.769 31212-31212/com.smart.kaifa I/第二次： scrollX：: -100.0
03-31 22:37:06.769 31212-31212/com.smart.kaifa I/第二次： scrollY：: -100.0
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
03-31 22:39:21.987 31988-31988/com.smart.kaifa I/第一次： left：: 0
03-31 22:39:21.987 31988-31988/com.smart.kaifa I/第一次： right：: 700
03-31 22:39:21.987 31988-31988/com.smart.kaifa I/第一次： bottom：: 700
03-31 22:39:21.987 31988-31988/com.smart.kaifa I/第一次： top：: 0
03-31 22:39:21.987 31988-31988/com.smart.kaifa I/第一次： width：: 700
03-31 22:39:21.987 31988-31988/com.smart.kaifa I/第一次： height：: 700
03-31 22:39:21.987 31988-31988/com.smart.kaifa I/第一次： x：: 0.0
03-31 22:39:21.987 31988-31988/com.smart.kaifa I/第一次： y：: 0.0
03-31 22:39:21.987 31988-31988/com.smart.kaifa I/第一次： translationX：: 0.0
03-31 22:39:21.987 31988-31988/com.smart.kaifa I/第一次： translationY：: 0.0
03-31 22:39:21.987 31988-31988/com.smart.kaifa I/第一次： scrollX：: 0.0
03-31 22:39:21.987 31988-31988/com.smart.kaifa I/第一次： scrollY：: 0.0
03-31 22:39:22.990 31988-31988/com.smart.kaifa I/第二次： ********: ***************************************************************************
03-31 22:39:22.990 31988-31988/com.smart.kaifa I/第二次： left：: 0
03-31 22:39:22.990 31988-31988/com.smart.kaifa I/第二次： right：: 700
03-31 22:39:22.991 31988-31988/com.smart.kaifa I/第二次： bottom：: 700
03-31 22:39:22.991 31988-31988/com.smart.kaifa I/第二次： top：: 0
03-31 22:39:22.991 31988-31988/com.smart.kaifa I/第二次： width：: 700
03-31 22:39:22.991 31988-31988/com.smart.kaifa I/第二次： height：: 700
03-31 22:39:22.991 31988-31988/com.smart.kaifa I/第二次： x：: 100.0
03-31 22:39:22.992 31988-31988/com.smart.kaifa I/第二次： y：: 100.0
03-31 22:39:22.992 31988-31988/com.smart.kaifa I/第二次： translationX：: 100.0
03-31 22:39:22.992 31988-31988/com.smart.kaifa I/第二次： translationY：: 100.0
03-31 22:39:22.992 31988-31988/com.smart.kaifa I/第二次： scrollX：: 0.0
03-31 22:39:22.992 31988-31988/com.smart.kaifa I/第二次： scrollY：: 0.0
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
03-31 22:46:29.420 1993-1993/com.smart.kaifa I/第一次： left：: 0
03-31 22:46:29.420 1993-1993/com.smart.kaifa I/第一次： right：: 700
03-31 22:46:29.420 1993-1993/com.smart.kaifa I/第一次： bottom：: 700
03-31 22:46:29.420 1993-1993/com.smart.kaifa I/第一次： top：: 0
03-31 22:46:29.420 1993-1993/com.smart.kaifa I/第一次： width：: 700
03-31 22:46:29.420 1993-1993/com.smart.kaifa I/第一次： height：: 700
03-31 22:46:29.421 1993-1993/com.smart.kaifa I/第一次： x：: 0.0
03-31 22:46:29.421 1993-1993/com.smart.kaifa I/第一次： y：: 0.0
03-31 22:46:29.421 1993-1993/com.smart.kaifa I/第一次： translationX：: 0.0
03-31 22:46:29.421 1993-1993/com.smart.kaifa I/第一次： translationY：: 0.0
03-31 22:46:29.421 1993-1993/com.smart.kaifa I/第一次： scrollX：: 0.0
03-31 22:46:29.421 1993-1993/com.smart.kaifa I/第一次： scrollY：: 0.0
03-31 22:46:30.424 1993-1993/com.smart.kaifa I/第二次： ********: ***************************************************************************
03-31 22:46:30.424 1993-1993/com.smart.kaifa I/第二次： left：: 0
03-31 22:46:30.425 1993-1993/com.smart.kaifa I/第二次： right：: 700
03-31 22:46:30.425 1993-1993/com.smart.kaifa I/第二次： bottom：: 700
03-31 22:46:30.425 1993-1993/com.smart.kaifa I/第二次： top：: 0
03-31 22:46:30.425 1993-1993/com.smart.kaifa I/第二次： width：: 700
03-31 22:46:30.425 1993-1993/com.smart.kaifa I/第二次： height：: 700
03-31 22:46:30.425 1993-1993/com.smart.kaifa I/第二次： x：: 100.0
03-31 22:46:30.426 1993-1993/com.smart.kaifa I/第二次： y：: 100.0
03-31 22:46:30.426 1993-1993/com.smart.kaifa I/第二次： translationX：: 100.0
03-31 22:46:30.426 1993-1993/com.smart.kaifa I/第二次： translationY：: 100.0
03-31 22:46:30.426 1993-1993/com.smart.kaifa I/第二次： scrollX：: 0.0
03-31 22:46:30.426 1993-1993/com.smart.kaifa I/第二次： scrollY：: 0.0
```

![Alt text](device-2018-03-31-224323.png "效果")

### 第二种：属性动画，属性动画是改变View的属性值

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
03-25 22:13:16.307 18794-18794/com.smart.kaifa I/第一次： left：: 70
03-25 22:13:16.307 18794-18794/com.smart.kaifa I/第一次： right：: 140
03-25 22:13:16.307 18794-18794/com.smart.kaifa I/第一次： bottom：: 140
03-25 22:13:16.307 18794-18794/com.smart.kaifa I/第一次： top：: 70
03-25 22:13:16.307 18794-18794/com.smart.kaifa I/第一次： width：: 70
03-25 22:13:16.307 18794-18794/com.smart.kaifa I/第一次： height：: 70
03-25 22:13:16.307 18794-18794/com.smart.kaifa I/第一次： x：: 70.0
03-25 22:13:16.307 18794-18794/com.smart.kaifa I/第一次： y：: 70.0
03-25 22:13:16.307 18794-18794/com.smart.kaifa I/第一次： translationX：: 0.0
03-25 22:13:16.307 18794-18794/com.smart.kaifa I/第一次： translationY：: 0.0
03-25 22:13:16.307 18794-18794/com.smart.kaifa I/第一次： scrollX：: 0.0
03-25 22:13:16.307 18794-18794/com.smart.kaifa I/第一次： scrollY：: 0.0
03-25 22:13:17.314 18794-18794/com.smart.kaifa I/第二次： ********: ***************************************************************************
03-25 22:13:17.314 18794-18794/com.smart.kaifa I/第二次： left：: 70
03-25 22:13:17.314 18794-18794/com.smart.kaifa I/第二次： right：: 140
03-25 22:13:17.315 18794-18794/com.smart.kaifa I/第二次： bottom：: 140
03-25 22:13:17.315 18794-18794/com.smart.kaifa I/第二次： top：: 70
03-25 22:13:17.315 18794-18794/com.smart.kaifa I/第二次： width：: 70
03-25 22:13:17.315 18794-18794/com.smart.kaifa I/第二次： height：: 70
03-25 22:13:17.315 18794-18794/com.smart.kaifa I/第二次： x：: 270.0
03-25 22:13:17.316 18794-18794/com.smart.kaifa I/第二次： y：: 70.0
03-25 22:13:17.316 18794-18794/com.smart.kaifa I/第二次： translationX：: 200.0
03-25 22:13:17.316 18794-18794/com.smart.kaifa I/第二次： translationY：: 0.0
03-25 22:13:17.316 18794-18794/com.smart.kaifa I/第二次： scrollX：: 0.0
03-25 22:13:17.317 18794-18794/com.smart.kaifa I/第二次： scrollY：: 0.0
```

属性动画和普通动画的区别在于普通动画移动之后由于没有改变属性值，只是视觉效果变化，这个时候点击事件任然保留在原地，而属性动画点击事件和视觉效果一致
### 使用布局参数，就是修改LayoutParams
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
03-31 23:13:47.225 10457-10457/com.smart.kaifa I/第一次： left：: 0
03-31 23:13:47.225 10457-10457/com.smart.kaifa I/第一次： right：: 700
03-31 23:13:47.225 10457-10457/com.smart.kaifa I/第一次： bottom：: 700
03-31 23:13:47.225 10457-10457/com.smart.kaifa I/第一次： top：: 0
03-31 23:13:47.225 10457-10457/com.smart.kaifa I/第一次： width：: 700
03-31 23:13:47.225 10457-10457/com.smart.kaifa I/第一次： height：: 700
03-31 23:13:47.225 10457-10457/com.smart.kaifa I/第一次： x：: 0.0
03-31 23:13:47.225 10457-10457/com.smart.kaifa I/第一次： y：: 0.0
03-31 23:13:47.225 10457-10457/com.smart.kaifa I/第一次： translationX：: 0.0
03-31 23:13:47.225 10457-10457/com.smart.kaifa I/第一次： translationY：: 0.0
03-31 23:13:47.226 10457-10457/com.smart.kaifa I/第一次： scrollX：: 0.0
03-31 23:13:47.226 10457-10457/com.smart.kaifa I/第一次： scrollY：: 0.0
03-31 23:13:48.228 10457-10457/com.smart.kaifa I/第二次： ********: ***************************************************************************
03-31 23:13:48.228 10457-10457/com.smart.kaifa I/第二次： left：: 0
03-31 23:13:48.228 10457-10457/com.smart.kaifa I/第二次： right：: 700
03-31 23:13:48.228 10457-10457/com.smart.kaifa I/第二次： bottom：: 700
03-31 23:13:48.228 10457-10457/com.smart.kaifa I/第二次： top：: 0
03-31 23:13:48.228 10457-10457/com.smart.kaifa I/第二次： width：: 700
03-31 23:13:48.229 10457-10457/com.smart.kaifa I/第二次： height：: 700
03-31 23:13:48.229 10457-10457/com.smart.kaifa I/第二次： x：: 0.0
03-31 23:13:48.229 10457-10457/com.smart.kaifa I/第二次： y：: 0.0
03-31 23:13:48.229 10457-10457/com.smart.kaifa I/第二次： translationX：: 0.0
03-31 23:13:48.229 10457-10457/com.smart.kaifa I/第二次： translationY：: 0.0
03-31 23:13:48.229 10457-10457/com.smart.kaifa I/第二次： scrollX：: 0.0
03-31 23:13:48.229 10457-10457/com.smart.kaifa I/第二次： scrollY：: 0.0
```
![Alt text](device-2018-03-31-232202.png "效果")
可以看到改变了宽和margin，layoutParams后面会探究的

### 不同滑动方式的对比

* scrollTo和scrollBy ：主要适合对View内容的滑动
* 动画：操作简单，主要适合没有交互的View和实现复杂动画效果
* 改变布局参数：操作稍微复杂，使用与有交互的View

## 弹性滑动
知道了View的滑动，我们还要知道如何实现View的弹性滑动，实现弹性滑动的方式有很多，比如通过handler#postDelayed...
### Scroller ：之前说过

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

### 通过动画

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
### 也可以使用延时策略
如果使用handler，这个不一定流畅，因为调度handler是会有延时的

## View的事件分发机制
上面介绍了View的基础知识和View的滑动，这里介绍一下View的事件分发机制
### 点击事件的传递规则
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
com.smart.kaifa I/哇哈哈   MainActivity: dispatchTouchEvent========
com.smart.kaifa I/哇哈哈   TestTouchView: dispatchTouchEvent========
    onInterceptTouchEvent========
    onTouchEvent========
com.smart.kaifa I/哇哈哈   MainActivity: onTouchEvent========
com.smart.kaifa I/哇哈哈   MainActivity: dispatchTouchEvent========
    onTouchEvent========
com.smart.kaifa I/哇哈哈   MainActivity: dispatchTouchEvent========
    onTouchEvent========
com.smart.kaifa I/哇哈哈   MainActivity: dispatchTouchEvent========
    onTouchEvent========
com.smart.kaifa I/哇哈哈   MainActivity: dispatchTouchEvent========
    onTouchEvent========
com.smart.kaifa I/哇哈哈   MainActivity: dispatchTouchEvent========
    onTouchEvent========
com.smart.kaifa I/哇哈哈   MainActivity: dispatchTouchEvent========
    onTouchEvent========
com.smart.kaifa I/哇哈哈   MainActivity: dispatchTouchEvent========
    onTouchEvent========
com.smart.kaifa I/哇哈哈   MainActivity: dispatchTouchEvent========
    onTouchEvent========
com.smart.kaifa I/哇哈哈   MainActivity: dispatchTouchEvent========
    onTouchEvent========
com.smart.kaifa I/哇哈哈   MainActivity: dispatchTouchEvent========
    onTouchEvent========
com.smart.kaifa I/哇哈哈   MainActivity: dispatchTouchEvent========
    onTouchEvent========
```

这里：在TestTouchView1中拦截`onInterceptTouchEvent`返回true，并且在TestTouchView中处理事件，TestTouchView的`onTouchEvent`方法返回true,这个时候事件会分发，虽然是在TestTouchView1中拦截的，但是由于不是在TestTouchView1中处理，系统在第一次down的时候知道了事件是TestTouchView处理的，就不会再分发给TestTouchView1了
```
com.smart.kaifa I/哇哈哈   MainActivity: dispatchTouchEvent========
com.smart.kaifa I/哇哈哈   TestTouchView: dispatchTouchEvent========
    onInterceptTouchEvent========
com.smart.kaifa I/哇哈哈   TestTouchView1: dispatchTouchEvent========
    onInterceptTouchEvent========
    onTouchEvent========
com.smart.kaifa I/哇哈哈   TestTouchView: onTouchEvent========
com.smart.kaifa I/哇哈哈   MainActivity: dispatchTouchEvent========
com.smart.kaifa I/哇哈哈   TestTouchView: dispatchTouchEvent========
    onTouchEvent========
com.smart.kaifa I/哇哈哈   MainActivity: dispatchTouchEvent========
com.smart.kaifa I/哇哈哈   TestTouchView: dispatchTouchEvent========
com.smart.kaifa I/哇哈哈   TestTouchView: onTouchEvent========
com.smart.kaifa I/哇哈哈   MainActivity: dispatchTouchEvent========
com.smart.kaifa I/哇哈哈   TestTouchView: dispatchTouchEvent========
    onTouchEvent========
```
4. 如果View不消耗除Action_Down以外的其他事件，那么这个点击事件就会消失。此时父元素的OnTouchEvent并不会被调用，并且当前View可以持续受到后续的事件，最终这些消失的点击事件会传递给Activity处理
默认都没有拦截事件，这个时候如果在View上面移动，可以看到只有down事件是全部传递了，move和up都没有经过子View，这个直接传递给了activity的onTouchEvent来处理
```
com.smart.kaifa I/哇哈哈   MainActivity: dispatchTouchEvent========
com.smart.kaifa I/哇哈哈   TestTouchView: dispatchTouchEvent========
    onInterceptTouchEvent========
com.smart.kaifa I/哇哈哈   TestTouchView1: dispatchTouchEvent========
    onInterceptTouchEvent========
com.smart.kaifa I/哇哈哈   Test1View: dispatchTouchEvent========
    onTouchEvent========
com.smart.kaifa I/哇哈哈   TestTouchView1: onTouchEvent========
com.smart.kaifa I/哇哈哈   TestTouchView: onTouchEvent========
com.smart.kaifa I/哇哈哈   MainActivity: onTouchEvent========
com.smart.kaifa I/哇哈哈   MainActivity: dispatchTouchEvent========
com.smart.kaifa I/哇哈哈   MainActivity: onTouchEvent========
com.smart.kaifa I/哇哈哈   MainActivity: dispatchTouchEvent========
    onTouchEvent========
com.smart.kaifa I/哇哈哈   MainActivity: dispatchTouchEvent========
    onTouchEvent========
```
5. 某个View一旦开始处理事件，如果它不消耗Action_Down事件，（`ontouchEvent`返回了false），那么，同一个事件序列中的其他事件都不会再交给他来处理，并且事件将重新交给他的父元素去处理，即父元素的`OnTouchEvent`方法会被调用。意思就是事件一旦交给一个View处理，那么他就必须消耗掉，否则同一事件序列中剩下的事件就不会再交给他来处理了。
6. ViewGroup默认不拦截任何事件。Android源码中ViewGroup的`onInterceptTouch-Event`方法默认返回false
7. View没有`onInterceptTouch-Event`方法，一旦有点击事件传递给他，那么他的OnTouchEvent方法就会被调用
8. View的`onTouchEvent`默认都会消耗事件，返回true，除非他是不可点击的（clickable和longClickable同时为false）。View的longClickable属性默认为false，clickable属性要分情况，比如Botton的clickable属性默认为true，而textview的clickable属性默认为false

Test1View 是一个button
```
com.smart.kaifa I/哇哈哈   MainActivity: dispatchTouchEvent========
com.smart.kaifa I/哇哈哈   TestTouchView: dispatchTouchEvent========
    onInterceptTouchEvent========
com.smart.kaifa I/哇哈哈   TestTouchView1: dispatchTouchEvent========
    onInterceptTouchEvent========
com.smart.kaifa I/哇哈哈   Test1View: dispatchTouchEvent========
    onTouchEvent========
com.smart.kaifa I/哇哈哈   MainActivity: dispatchTouchEvent========
com.smart.kaifa I/哇哈哈   TestTouchView: dispatchTouchEvent========
    onInterceptTouchEvent========
com.smart.kaifa I/哇哈哈   TestTouchView1: dispatchTouchEvent========
    onInterceptTouchEvent========
com.smart.kaifa I/哇哈哈   Test1View: dispatchTouchEvent========
    onTouchEvent========
com.smart.kaifa I/哇哈哈   MainActivity: dispatchTouchEvent========
com.smart.kaifa I/哇哈哈   TestTouchView: dispatchTouchEvent========
    onInterceptTouchEvent========
com.smart.kaifa I/哇哈哈   TestTouchView1: dispatchTouchEvent========
    onInterceptTouchEvent========
com.smart.kaifa I/哇哈哈   Test1View: dispatchTouchEvent========
    onTouchEvent========
com.smart.kaifa I/哇哈哈   MainActivity: dispatchTouchEvent========
com.smart.kaifa I/哇哈哈   TestTouchView: dispatchTouchEvent========
    onInterceptTouchEvent========
com.smart.kaifa I/哇哈哈   TestTouchView1: dispatchTouchEvent========
    onInterceptTouchEvent========
com.smart.kaifa I/哇哈哈   Test1View: dispatchTouchEvent========
    onTouchEvent========
```
TestView 是一个textview
```
com.smart.kaifa I/哇哈哈   MainActivity: dispatchTouchEvent========
com.smart.kaifa I/哇哈哈   TestTouchView: dispatchTouchEvent========
com.smart.kaifa I/哇哈哈   TestTouchView: onInterceptTouchEvent========
com.smart.kaifa I/哇哈哈   TestTouchView1: dispatchTouchEvent========
    onInterceptTouchEvent========
com.smart.kaifa I/哇哈哈   TestView: dispatchTouchEvent========
    onTouchEvent========
com.smart.kaifa I/哇哈哈   TestTouchView1: onTouchEvent========
com.smart.kaifa I/哇哈哈   TestTouchView: onTouchEvent========
com.smart.kaifa I/哇哈哈   MainActivity: onTouchEvent========
com.smart.kaifa I/哇哈哈   MainActivity: dispatchTouchEvent========
    onTouchEvent========
com.smart.kaifa I/哇哈哈   MainActivity: dispatchTouchEvent========
    onTouchEvent========
com.smart.kaifa I/哇哈哈   MainActivity: dispatchTouchEvent========
    onTouchEvent========
com.smart.kaifa I/哇哈哈   MainActivity: dispatchTouchEvent========
    onTouchEvent========
com.smart.kaifa I/哇哈哈   MainActivity: dispatchTouchEvent========
    onTouchEvent========
com.smart.kaifa I/哇哈哈   MainActivity: dispatchTouchEvent========
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
com.smart.kaifa I/哇哈哈   MainActivity: dispatchTouchEvent========
com.smart.kaifa I/哇哈哈   TestTouchView: dispatchTouchEvent========
com.smart.kaifa I/哇哈哈   TestTouchView: onInterceptTouchEvent========
com.smart.kaifa I/哇哈哈   TestTouchView1: dispatchTouchEvent========
com.smart.kaifa I/哇哈哈   MainActivity: dispatchTouchEvent========
com.smart.kaifa I/哇哈哈   TestTouchView: dispatchTouchEvent========
    onInterceptTouchEvent========
com.smart.kaifa I/哇哈哈   TestTouchView1: dispatchTouchEvent========
com.smart.kaifa I/哇哈哈   MainActivity: dispatchTouchEvent========
com.smart.kaifa I/哇哈哈   TestTouchView: dispatchTouchEvent========
    onInterceptTouchEvent========
com.smart.kaifa I/哇哈哈   TestTouchView1: dispatchTouchEvent========
com.smart.kaifa I/哇哈哈   MainActivity: dispatchTouchEvent========
com.smart.kaifa I/哇哈哈   TestTouchView: dispatchTouchEvent========
com.smart.kaifa I/哇哈哈   TestTouchView: onInterceptTouchEvent========
com.smart.kaifa I/哇哈哈   TestTouchView1: dispatchTouchEvent========
```



### 事件分发源码分析：有四个组件

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

## View的滑动冲突
这个是开发的时候经常遇到的问题
### 常见的滑动冲突场景
* 场景1 : 外部滑动方向和内部滑动方向不一致
* 场景2 ：外部华东方向和内部滑动方向一致
* 场景3 ：上面两种嵌套的情况

这里先说场景1 ： 这个主要是Viewpager和Fragment配合的时候所组成的页面滑动，主流应用几乎都会使用这个效果。在fragment中有listview。Viewpager内部帮助我们处理了这种冲突，但是如果是scrollView就会遇到冲突。
再说一下场景2 ： 这种情况就稍微复杂一点，因为是内部和外部同时都会监听同一个方向的滚动，所以系统不知道要将事件分发给谁。一般是内部滑动，外部跟着滑动
最后说一下场景3 ：场景1和场景2两种情况嵌套，因此这个比较复杂。

### 事件冲突的处理规则
不管冲突多么复杂，总归是有规则的。
第一种情况可以根据三角形来计算，如果是水平方向的dx大于垂直方向的dy，就用dx，反之一样
第二种情况要依据具体的情况来解答
第三种情况更加复杂，后面举个例子

### 滑动冲突的解决方式

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



# 第四章 View的工作原理
这一章主要介绍两个方面的内容，一个是View的工作原理，接着介绍自定义View的实现方式。
## 初识ViewRoot和DecorView
 在介绍View之前，这里介绍一下基本概念，这样才能了解View的measure，layout，draw过程。
 ViewRoot对应于ViewRootImpl类，它是连接WindowManager和DecorView的纽带，View的三大流程都是通过ViewRoot来完成的。在ActivityThread中，当Activity对象呗创建完成后，会将DecorView添加到window中，同时创建ViewRootImpl对象。并将ViewRootImpl对象和DecorView建立关联，这个过程可以看如下源码。
 ```
 root=new ViewRootImpl(view.getContext,display);
 root.setView(view,wparams,panelParentView)
 ```
View的绘制流程从ViewRoot的performTraversals方法开始的，他经过measure，layout，draw三个过程才能最终将一个View绘制出来，其中measure用拉力测量View的宽和高，layout用来确定View在父容器当中的位置，而draw负责将View绘制到屏幕上面去。针对performTraversals的大致流程如下

![Alt text](图像1523889820.png "流程图")

如图所示，performTraversals会一次调用performMeasure，performLayout和performDraw三个方法，这三个方法分别完成顶级View的measure，layout，draw这三大流程。其中performMeasure会调用measure方法，在measure方法中会调用onMeasure方法，在onMeasure方法会对所有子元素进行measure过程，这个时候measure流程就从父容器传递到子容器当中了。这样就完成了一次measure过程。接着子元素会重复父容器的measure过程，如此反复就完成了整个View树的遍历，同理performLayout和performDraw的传递流程和performMeasure是类似的，唯一不同的是performDraw的传递过程是在draw方法加载dispatchDraw来实现的，不过本质并没有什么区别。
measure过程决定了View的宽和高，Measure完成后，可以通过getMeasuredWidth和getMeasureHeight方法来获取到View测量后的宽和高。在几乎所有的情况下他都等于View最终的宽和高，但是特殊情况除外，这点在本章后面会进行说明。Layout过程决定了View的四个顶点和View的实际宽和高，完成以后可以通过`getTop`和`getBottom`和`getRight`和`getLeft`并且通过`getWidth`和`getHeight`获取最终的宽和高，draw过程决定了View的显示。只有draw之后才能在屏幕上面显示。

![Alt text](图像1523974134.png "顶级View：DecorView的结构")
如图：DecorView作为最顶级的View，一般情况下他的内部包含一个Linearlayout,分为两个部分：上方标题栏和下方内容栏。在Actvity中通过`setContentView`的内容是被加入到内容栏中间的，而内容栏的id是content，所以这个方法就叫做`setContentView`。如何得到这个内容栏呢? 可以通过`findViewById(R.id.content)`找到内容栏。如何得到我们设置的View呢?可以通过`content.getChildAt(0)`来获取

## 理解MeasureSpec
为了更好的理解View的测量流程，我们还需要理解MeasureSpec。从名字上来看 MeasureSpec像是测量规格。 MeasureSpec是干什么的呢？它在很大程度上决定了一个View的尺寸规格，之所以很大程度上是因为这个会受到父控件的影响，因为父控件影响View的MeasureSpec的创建过程。在测量过程中，系统会将View的LayoutParems根据父控件所施加的规则转换成对应的MeasureSpec，然后在更具这个measureSpec来测量View的宽高,上面提到过，这里测量的宽和高并不一定是View的最终宽高。MeasureSpec看起来有点复杂。其实他的实现很简单。


### MeasureSpec
这个是一个32位的int值，高两位代表测量模式：specMode，低两位代表某一种模式的测量大小：SpecSize。
```
public static class MeasureSpec {
       private static final int MODE_SHIFT = 30;
       private static final int MODE_MASK  = 0x3 << MODE_SHIFT;

       /** @hide */
       @IntDef({UNSPECIFIED, EXACTLY, AT_MOST})
       @Retention(RetentionPolicy.SOURCE)
       public @interface MeasureSpecMode {}

       /**
        * Measure specification mode: The parent has not imposed any constraint
        * on the child. It can be whatever size it wants.
        */
       public static final int UNSPECIFIED = 0 << MODE_SHIFT;

       /**
        * Measure specification mode: The parent has determined an exact size
        * for the child. The child is going to be given those bounds regardless
        * of how big it wants to be.
        */
       public static final int EXACTLY     = 1 << MODE_SHIFT;

       /**
        * Measure specification mode: The child can be as large as it wants up
        * to the specified size.
        */
       public static final int AT_MOST     = 2 << MODE_SHIFT;

       /**
        * Creates a measure specification based on the supplied size and mode.
        *
        * The mode must always be one of the following:
        * <ul>
        *  <li>{@link android.view.View.MeasureSpec#UNSPECIFIED}</li>
        *  <li>{@link android.view.View.MeasureSpec#EXACTLY}</li>
        *  <li>{@link android.view.View.MeasureSpec#AT_MOST}</li>
        * </ul>
        *
        * <p><strong>Note:</strong> On API level 17 and lower, makeMeasureSpec's
        * implementation was such that the order of arguments did not matter
        * and overflow in either value could impact the resulting MeasureSpec.
        * {@link android.widget.RelativeLayout} was affected by this bug.
        * Apps targeting API levels greater than 17 will get the fixed, more strict
        * behavior.</p>
        *
        * @param size the size of the measure specification
        * @param mode the mode of the measure specification
        * @return the measure specification based on size and mode
        */
       public static int makeMeasureSpec(@IntRange(from = 0, to = (1 << MeasureSpec.MODE_SHIFT) - 1) int size,
                                         @MeasureSpecMode int mode) {
           if (sUseBrokenMakeMeasureSpec) {
               return size + mode;
           } else {
               return (size & ~MODE_MASK) | (mode & MODE_MASK);
           }
       }

       /**
        * Like {@link #makeMeasureSpec(int, int)}, but any spec with a mode of UNSPECIFIED
        * will automatically get a size of 0. Older apps expect this.
        *
        * @hide internal use only for compatibility with system widgets and older apps
        */
       public static int makeSafeMeasureSpec(int size, int mode) {
           if (sUseZeroUnspecifiedMeasureSpec && mode == UNSPECIFIED) {
               return 0;
           }
           return makeMeasureSpec(size, mode);
       }

       /**
        * Extracts the mode from the supplied measure specification.
        *
        * @param measureSpec the measure specification to extract the mode from
        * @return {@link android.view.View.MeasureSpec#UNSPECIFIED},
        *         {@link android.view.View.MeasureSpec#AT_MOST} or
        *         {@link android.view.View.MeasureSpec#EXACTLY}
        */
       @MeasureSpecMode
       public static int getMode(int measureSpec) {
           //noinspection ResourceType
           return (measureSpec & MODE_MASK);
       }

       /**
        * Extracts the size from the supplied measure specification.
        *
        * @param measureSpec the measure specification to extract the size from
        * @return the size in pixels defined in the supplied measure specification
        */
       public static int getSize(int measureSpec) {
           return (measureSpec & ~MODE_MASK);
       }

       static int adjust(int measureSpec, int delta) {
           final int mode = getMode(measureSpec);
           int size = getSize(measureSpec);
           if (mode == UNSPECIFIED) {
               // No need to adjust size for UNSPECIFIED mode.
               return makeMeasureSpec(size, UNSPECIFIED);
           }
           size += delta;
           if (size < 0) {
               Log.e(VIEW_LOG_TAG, "MeasureSpec.adjust: new size would be negative! (" + size +
                       ") spec: " + toString(measureSpec) + " delta: " + delta);
               size = 0;
           }
           return makeMeasureSpec(size, mode);
       }

   }
```

MeasureSpec通过将SpecMode和SpecSize打包成为一个int值来避免过多的对象内存分配，为了方便操作。提供了打包和解包的方法。specMode和SpecSIze也是一个int值，一组SpecMode和SpecSize可以打包为一个MeasureSpec。而一个MeasureSpec可以通过解包的形式获取SpecMode和SpecSize，需要注意这里的MeasureSpec所代表的是一个int值，不是对象本身。

SpecMode有三类，每一类都代表特殊的含义，如下所示
* UNSPECIFIED
父容器不对View有限制，要多他给多大，一般情况用于系统内部，表示测量状态
* EXACTLY
父容器已经检测出View所遇到的精确大小，这个时候View的最终大小就是SpecSize指定的值。他对应于LayoutParems中的match_parent和具体数值这两种模式
* AT__MOST
父容器指定了一个可用大小即SpecSize，View的大小不能大于这个值，具体是什么要看不同View的具体实现，他对于LayoutParams中的wrap_content
### MeasureSpec和LayoutParams的对应关系

上面提到，系统内部是通过MeasureSpec来进行测量，但是正常情况下我们使用View指定MeasureSpec，尽管如此，但是我们可以给View设置LayoutParems。在View测量的时候，系统会将LayoutParams在父容器约束下转换成对应的MeasureSpec，然后在更具这个MeasureSpec来确定测量后的宽和高。需要注意的是，MeasureSpec不是唯一由LayoutParams决定的，LayoutParams需要和父容器一起才能决定View的MeasureSpec，从而进一步决定View的宽和高。另外，对于顶级View（DecorView）和普通View来说，MeasureSpec的转换过程略有不同，对于DecorView，其MeasureSpec有窗口尺寸和其自身的LayoutParams来共同决定；对于普通View，其MeasureSpec由父容器的MeasureSpec和自身的LayoutParams来共同决定，MeasureSpec一旦确定后，onMeasure中就可以测量View的宽和高了
对于DecorView来说，在ViewRootImpl中的MeasureHierarchy方法中有如下一段代码，他展示了DecorView的MeasureSpec的创建过程，其中desiredWindowWidth和desiredWindowHeight是屏幕的尺寸
```
childWidthMeasureSpec=getRootMeasureSpec(desireWindowWidth,lp.width);
childHeightMeasureSpec=getRootMeasureSpec(desiredwindowHeight,lp.height);
performMeasure(childWidthMeasureSpec,childHeightMeasureSpec);
```


接着在看一下`getRootMeasureSpec`的方法实现
```
private static int getRootMeasureSpec(int windowSize,int rootDimension){
  int measureSpec;
  switch(rootDimension){
      // MATCH_PARENT 和MeasureSpec.AT_MOST有关联 ViewGroup.LayoutParams.MATCH_PARENT是-1
    case ViewGroup.LayoutParams.MATCH_PARENT：
    measureSpec=MeasureSpec.makeMeasureSpec(windowSize,MeasureSpec.EXACELY);
    break;
    // WRAP_CONTENT 和MeasureSpec.AT_MOST有关联   public static final int WRAP_CONTENT = -2;
    case ViewGroup.LayoutParams.WRAP_CONTENT：
    measureSpec=MeasureSpec.makeMeasureSpec(windowSize,MeasureSpec.AT_MOST);
    break;
    // 固定大小
    default:
    measureSpec=MeasureSpec.makeMeasureSpec(rootDimension,MeasureSpec.EXACTLY)
    break;

  }
  return measureSpec;
}
```
通过上述代码，DecorView的MeasureSpec的产生过程就很明确了，终于清楚了
* LayoutParams.MATCH_PARENT：精确模式，大小就是窗口的大小
* LayoutParams.WRAP_CONTENT:最大模式，大小不确定，但是不能超过窗口大小
* 固定大小（比如100dp）：精确模式，大小为LayoutParams中指定的大小

对于普通View来说，这里是指我们布局中的View，View的measure过程由ViewGroup传递而来，先看一下ViewGroup的measureChildWithMargins方法

```
protected void measureChildWithMargins(View child,
         int parentWidthMeasureSpec, int widthUsed,
         int parentHeightMeasureSpec, int heightUsed) {
           //获取子控件的布局参数
     final MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
            //获取子控件的MeasureSpec 这里需要传入父元素的MeasureSpec和子元素的LayoutParams ，所以和这两个都有关系
     final int childWidthMeasureSpec = getChildMeasureSpec(parentWidthMeasureSpec,
             mPaddingLeft + mPaddingRight + lp.leftMargin + lp.rightMargin
                     + widthUsed, lp.width);
     final int childHeightMeasureSpec = getChildMeasureSpec(parentHeightMeasureSpec,
             mPaddingTop + mPaddingBottom + lp.topMargin + lp.bottomMargin
                     + heightUsed, lp.height);

     child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
 }


```
上面的方法会对子元素进行measure，在调用子元素的measure方法之前会同时getChildMeasureSpec方法来得到子元素的MeasureSpec。从代码来看，很显然，子元素MeasureSpec的创建与父容器的MeasureSpec和子元素本身的LayoutParams有关，此外还和View的marigin和padding有关，可以看一下`getChildMeasureSpec`


```
public static int getChildMeasureSpec(int spec, int padding, int childDimension) {
    // 解析父元素的MeasureSpec
     int specMode = MeasureSpec.getMode(spec);
     int specSize = MeasureSpec.getSize(spec);
     // 获取大小最大值
     int size = Math.max(0, specSize - padding);

     int resultSize = 0;
     int resultMode = 0;
     // 依据父元素的测量模式进行处理
     switch (specMode) {
     // Parent has imposed an exact size on us
     case MeasureSpec.EXACTLY:// 父元素是精确模式
         if (childDimension >= 0) {// 子控件的LayoutParams大小,如果大于0 就是固定模式，知道大小
             resultSize = childDimension;
             resultMode = MeasureSpec.EXACTLY;
         } else if (childDimension == LayoutParams.MATCH_PARENT) {// 子控件的LayoutParams大小,如果小于0 就是固定模式，不知道大小，设置了 LayoutParams.MATCH_PARENT
             // Child wants to be our size. So be it.
             resultSize = size;
             resultMode = MeasureSpec.EXACTLY;
         } else if (childDimension == LayoutParams.WRAP_CONTENT) {
             // Child wants to determine its own size. It can't be
             // bigger than us.
             resultSize = size;
             resultMode = MeasureSpec.AT_MOST;
         }
         break;

     // Parent has imposed a maximum size on us
     case MeasureSpec.AT_MOST:
         if (childDimension >= 0) {
             // Child wants a specific size... so be it
             resultSize = childDimension;
             resultMode = MeasureSpec.EXACTLY;
         } else if (childDimension == LayoutParams.MATCH_PARENT) {
             // Child wants to be our size, but our size is not fixed.
             // Constrain child to not be bigger than us.
             resultSize = size;
             resultMode = MeasureSpec.AT_MOST;
         } else if (childDimension == LayoutParams.WRAP_CONTENT) {
             // Child wants to determine its own size. It can't be
             // bigger than us.
             resultSize = size;
             resultMode = MeasureSpec.AT_MOST;
         }
         break;

     // Parent asked to see how big we want to be
     case MeasureSpec.UNSPECIFIED:
         if (childDimension >= 0) {
             // Child wants a specific size... let him have it
             resultSize = childDimension;
             resultMode = MeasureSpec.EXACTLY;
         } else if (childDimension == LayoutParams.MATCH_PARENT) {
             // Child wants to be our size... find out how big it should
             // be
             resultSize = View.sUseZeroUnspecifiedMeasureSpec ? 0 : size;
             resultMode = MeasureSpec.UNSPECIFIED;
         } else if (childDimension == LayoutParams.WRAP_CONTENT) {
             // Child wants to determine its own size.... find out how
             // big it should be
             resultSize = View.sUseZeroUnspecifiedMeasureSpec ? 0 : size;
             resultMode = MeasureSpec.UNSPECIFIED;
         }
         break;
     }
     //noinspection ResourceType
    // 将specMode和SpecSize组合
     return MeasureSpec.makeMeasureSpec(resultSize, resultMode);
 }

```


可以看出来主要是依据父容器的MeasureSpec同时结合View本身的LayoutParams来确定子元素的MeasureSpec。参数中的padding指的是父容器已经占用的大小，所以子元素可用大小为父容器的尺寸减去padding
```
int specSize = MeasureSpec.getSize(spec);
// 获取大小最大值
int size = Math.max(0, specSize - padding);
```
`getChildMeasureSpec`清楚的展示了普通View的MeasureSpec的创建规则，为了更加清晰的理解`getChildMeasureSpec`这里提供一个表针对`getChildMeasureSpec`的原理进行梳理

||parentSpecMode|EXACTLY|AT_MOST|UNSPECIFIED|
|:---:|:---:|:---:|:----:|:----:|
|childLayoutParams|dp/px|EXACTLY/childSize|AT_MOST/childSize|UNSPECIFIED/childSize|
|childLayoutParams|MATCH_PARENT|EXACTLY/parentSize|AT_MOST/parentSize|UNSPECIFIED/0|
|childLayoutParams|WRAP_CONTENT|AT_MOST/parentSiz|AT_MOST/parentSiz|UNSPECIFIED/0|

针对表这里在做一下说明，前面已经提到，对于普通的View，其MeasureSpec由父控件的MeasureSpec和自身的LayoutParams来共同决定，那么针对不同的父容器和View本身不同的LayoutParams，View就可以有多重MeasureSpec。这里说一下当View采用固定狂傲的时候，不管父容器是什么模式，都是精确模式，并且大小遵循子控件的LayoutParams，当View的宽度/高度是match_parent时，如果父控件的模式是精确模式，那么View也是精确模式并且其大小不会超过其父容器的大小，如果父容器是最大模式，那么View也是最大模式，并且其大小不会超过父容器的剩余空间，当View的宽/高是wrap_content时，不管父容器的模式是精确还是最大化，View的模式总是最大化并且不能超过父容器的剩余空间。分析的时候溜掉了UNSPECIFIED模式，这个模式主要用于系统内部多次Measure的情景，一般不用

## View的绘制流程
View的工作流程主要是指measure、layout和draw这三个过程，即测量，布局和绘制，其中measure确定View的测量宽和高，layout确定View的四个顶点位置，而draw则将View绘制到屏幕上
### measure过程
measure过程要分情况来看，如果只是一个原始的View，那么通过measure方法就完成了其测量过程，如果是一个ViewGroup，除了完成自己的测量过程外，还会遍历调用所有的子元素的measure方法。各个子元素在递归去执行这个流程。下面针对这两种情况分别讨论。
1. View的measure过程
view的measure过程由其measure方法来完成，measure是一个final类型方法，这意味着不能重写此方法，在View的measure方法中会调用View的onMeasure方法，因此，只要看onMeasure方法实现即可，View的onMeasure方法如下所示。
```
protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    setMeasuredDimension(getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec),
            getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec));
}

```

上面的代码很简单，但是简洁并不代表简单，setMeasuredDimension方法会设置View的宽和高的测量值，因此我们只需要看getDefaultSize这个方法即可。

```
// 传入最大值：size
// 如果是UNSPECIFIED 自定义的模式，就使用传入的最大值
// 否则就根据MeasureSpec获取大小
public static int getDefaultSize(int size, int measureSpec) {
       int result = size;
       int specMode = MeasureSpec.getMode(measureSpec);
       int specSize = MeasureSpec.getSize(measureSpec);

       switch (specMode) {
       case MeasureSpec.UNSPECIFIED:
           result = size;
           break;
       case MeasureSpec.AT_MOST:
       case MeasureSpec.EXACTLY:
           result = specSize;
           break;
       }
       return result;
   }

```
UNSPECIFIED这种情况是系统内部后者自定义的时候使用，或得到size 这个size是通过下面的方法` getSuggestedMinimumWidth()`获取的

```
// 如果控件没有背景 取minwidth 否则 取最小值和背景的最大值
protected int getSuggestedMinimumWidth() {
    return (mBackground == null) ? mMinWidth : max(mMinWidth, mBackground.getMinimumWidth());
}

protected int getSuggestedMinimumHeight() {
    return (mBackground == null) ? mMinHeight : max(mMinHeight, mBackground.getMinimumHeight());

}

```

分析`getSuggestedMinimumWidth`和`getSuggestedMinimumHeight`的原理是一样的，如果没有设置背景，那么View的宽度为mMinWidth，而mMinWidth对应控件的android:minWidth这个属性所指的值，因此View的宽度就是android:minWidth属性所指定的值，这个属性如果不指定，那么默认值就是0，如果View指定了背景，则View的宽度为`max(mMinWidth, mBackground.getMinimumWidth())`的最大值，mMinWidth这个我们已经知道含义了，那么`mBackground.getMinimumWidth()`是什么意思呢？
```
public int getMinimumHeight() {
    final int intrinsicHeight = getIntrinsicHeight();
    return intrinsicHeight > 0 ? intrinsicHeight : 0;
}

public int getIntrinsicWidth() {
      return -1;
  }

drawable的子类重写了这个方法 这个是NinePatchDrawable
@Override
public int getIntrinsicWidth() {
    return mBitmapWidth;
}

```

可以看到getMinimumHeight返回的是drawable原始的高度。这里举个例子说明一下ShapeDrawable无原始宽和高，而BitmapDrawable有原始宽和高，详细内容会在后面介绍的
这里在总结一下`getMinimumHeight`的逻辑，如果View没有设置背景，那么返回android：minWidth这个属性所属的值。这个值可以为0，如果View设置了背景，则返回android：minWidth和背景最小宽度这两个钟的最大值，这样他的返回值就是View在UNSPECIFIED下的测量宽和高
从getDefaultSize方法的实现来看，View的狂傲由specSize决定，我们得出结论：直接继承View的自定义控件需要重写onMeasure方法并设置wrap_content是的自身大小，否则在布局中使用wrap_content就相当于使用match_parent。为什么呢？这个原因需要结合上述代码和之前的表来理解。从上述代码我们知道，如果View在布局中使用wrap_content,那么他的specMode的模式就是AT_MOST，在这种模式下，他的宽和高就是specSize；并且根据上面的表，可以看到这种情况下的specSize是parentSIze，而parentSize就是容器目前可以使用的大小，就是父容器当前剩余的空间大小。很显然，View的宽和高就相当于父控件当前剩余的空间大小，这种效果和在布局中使用match_parent完全一样。如何解决这个问题呢？
```

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthSpecMode=MeasureSpec.getMode(widthMeasureSpec);
        int heightSpecMode=MeasureSpec.getMode(heightMeasureSpec);
        int widthSpecSize=MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecSize=MeasureSpec.getSize(heightMeasureSpec);

        if(widthSpecMode==MeasureSpec.AT_MOST&&heightSpecMode==MeasureSpec.AT_MOST){
            setMeasuredDimension(mWidth.mHeight);
        }else   if(widthSpecMode==MeasureSpec.AT_MOST){
            setMeasuredDimension(mWidth.heightSpecSize);
        }else   if(heightSpecMode==MeasureSpec.AT_MOST){
            setMeasuredDimension(widthSpecSize.mHeight);
        }
    }
```
在上面的代码中，我们只需要给View指定一个默认的内部宽和高(mWidth和mHeight)并且在wrap_content的时候设置宽和高就可以了。针对非wrap_content情景，我们沿用西永的测量值就可以了。至于这个默认内部宽和高的大小如何指定，这个没有固定依据，更具需要灵活指定即可。查看TextView和ImageView等源码就可以知道，针对wrap_content情形。他们的onMeasure方法都做了特殊处理，读者可以自行查看他们的源码

```
这个是imageView的onMeasure方法
@Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
      resolveUri();
      int w;
      int h;

      // Desired aspect ratio of the view's contents (not including padding)
      float desiredAspect = 0.0f;

      // We are allowed to change the view's width
      boolean resizeWidth = false;

      // We are allowed to change the view's height
      boolean resizeHeight = false;

      final int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
      final int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);

      if (mDrawable == null) {
          // If no drawable, its intrinsic size is 0.
          mDrawableWidth = -1;
          mDrawableHeight = -1;
          w = h = 0;
      } else {
          w = mDrawableWidth;
          h = mDrawableHeight;
          if (w <= 0) w = 1;
          if (h <= 0) h = 1;
          //判断是否开启值测量宽和高
          // We are supposed to adjust view bounds to match the aspect
          // ratio of our drawable. See if that is possible.
          if (mAdjustViewBounds) {
              resizeWidth = widthSpecMode != MeasureSpec.EXACTLY;
              resizeHeight = heightSpecMode != MeasureSpec.EXACTLY;

              desiredAspect = (float) w / (float) h;
          }
      }

```

textView的onMeasure方法重写后特别长，这里就不列出来了

2. ViewGroup的measure处理过程
对于ViewGroup，除了完成自己的measure过程以外，还会遍历调用所有子控件的measure方法，和View不同的是，ViewGroup是一个抽象类，因此他没有重写View的onMeasure方法，但是他提供了一个叫measureChildren的方法，如下所示。
```
protected void measureChildren(int widthMeasureSpec, int heightMeasureSpec) {
    final int size = mChildrenCount;
    final View[] children = mChildren;
    for (int i = 0; i < size; ++i) {
        final View child = children[i];
        if ((child.mViewFlags & VISIBILITY_MASK) != GONE) {
            measureChild(child, widthMeasureSpec, heightMeasureSpec);
        }
    }
}

```
从源码来看，ViewGroup在measure是会对每一个子元素进行measure,measureChild的方法也很好理解

```
protected void measureChild(View child, int parentWidthMeasureSpec,
        int parentHeightMeasureSpec) {
    final LayoutParams lp = child.getLayoutParams();

    final int childWidthMeasureSpec = getChildMeasureSpec(parentWidthMeasureSpec,
            mPaddingLeft + mPaddingRight, lp.width);
    final int childHeightMeasureSpec = getChildMeasureSpec(parentHeightMeasureSpec,
            mPaddingTop + mPaddingBottom, lp.height);

    child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
}

```

很显然，`measureChild`的意思其实就是取出子元素的layoutParams，然后通过getChildMeasureSpec获取MeasureSpec，然后将得到的MeasureSpec传递给子控件的measure方法
我们知道ViewGroup没有定义测量过程，这个是因为ViewGroup是一个抽象类，其测量过程的OnMeasure需要在每一个子类中实现，比如LinearLayout，RelativeLayout等。为什么不像View一样对其OnMeasure进行统一处理呢，这个是因为每一个ViewGroup子类的布局特性不同，导致细节不同，所以不能统一实现。这里使用LinearLayout的onMeasure进行分析



```
@Override
protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    if (mOrientation == VERTICAL) {
        measureVertical(widthMeasureSpec, heightMeasureSpec);
    } else {
        measureHorizontal(widthMeasureSpec, heightMeasureSpec);
    }
}

```

这个代码很简单，这里就先分析一下`measureVertical`

```
void measureVertical(int widthMeasureSpec, int heightMeasureSpec) {
       mTotalLength = 0;
       int maxWidth = 0;
       int childState = 0;
       int alternativeMaxWidth = 0;
       int weightedMaxWidth = 0;
       boolean allFillParent = true;
       float totalWeight = 0;

       final int count = getVirtualChildCount();

       final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
       final int heightMode = MeasureSpec.getMode(heightMeasureSpec);

       boolean matchWidth = false;
       boolean skippedMeasure = false;

       final int baselineChildIndex = mBaselineAlignedChildIndex;
       final boolean useLargestChild = mUseLargestChild;

       int largestChildHeight = Integer.MIN_VALUE;
       int consumedExcessSpace = 0;

       int nonSkippedChildCount = 0;

       // See how tall everyone is. Also remember max width.
       //这里对子控件进行遍历
       for (int i = 0; i < count; ++i) {
           final View child = getVirtualChildAt(i);
           if (child == null) {
               mTotalLength += measureNullChild(i);
               continue;
           }

           if (child.getVisibility() == View.GONE) {
              i += getChildrenSkipCount(child, i);
              continue;
           }

           nonSkippedChildCount++;
           if (hasDividerBeforeChildAt(i)) {
               mTotalLength += mDividerHeight;
           }

           final LayoutParams lp = (LayoutParams) child.getLayoutParams();

           totalWeight += lp.weight;

           final boolean useExcessSpace = lp.height == 0 && lp.weight > 0;
           if (heightMode == MeasureSpec.EXACTLY && useExcessSpace) {
               final int totalLength = mTotalLength;
               mTotalLength = Math.max(totalLength, totalLength + lp.topMargin + lp.bottomMargin);
               skippedMeasure = true;
           } else {
               if (useExcessSpace) {

                   lp.height = LayoutParams.WRAP_CONTENT;
               }

               final int usedHeight = totalWeight == 0 ? mTotalLength : 0;
               // 对子控件执行measureChildBeforeLayout
               measureChildBeforeLayout(child, i, widthMeasureSpec, 0,
                       heightMeasureSpec, usedHeight);

               final int childHeight = child.getMeasuredHeight();
               if (useExcessSpace) {
                   lp.height = 0;
                   consumedExcessSpace += childHeight;
               }

               final int totalLength = mTotalLength;
               mTotalLength = Math.max(totalLength, totalLength + childHeight + lp.topMargin +
                      lp.bottomMargin + getNextLocationOffset(child));

               if (useLargestChild) {
                   largestChildHeight = Math.max(childHeight, largestChildHeight);
               }
           }

           /**
            * If applicable, compute the additional offset to the child's baseline
            * we'll need later when asked {@link #getBaseline}.
            */
           if ((baselineChildIndex >= 0) && (baselineChildIndex == i + 1)) {
              mBaselineChildTop = mTotalLength;
           }

           // if we are trying to use a child index for our baseline, the above
           // book keeping only works if there are no children above it with
           // weight.  fail fast to aid the developer.
           if (i < baselineChildIndex && lp.weight > 0) {
               throw new RuntimeException("A child of LinearLayout with index "
                       + "less than mBaselineAlignedChildIndex has weight > 0, which "
                       + "won't work.  Either remove the weight, or don't set "
                       + "mBaselineAlignedChildIndex.");
           }

           boolean matchWidthLocally = false;
           if (widthMode != MeasureSpec.EXACTLY && lp.width == LayoutParams.MATCH_PARENT) {
               // The width of the linear layout will scale, and at least one
               // child said it wanted to match our width. Set a flag
               // indicating that we need to remeasure at least that view when
               // we know our width.
               matchWidth = true;
               matchWidthLocally = true;
           }

           final int margin = lp.leftMargin + lp.rightMargin;
           final int measuredWidth = child.getMeasuredWidth() + margin;
           maxWidth = Math.max(maxWidth, measuredWidth);
           childState = combineMeasuredStates(childState, child.getMeasuredState());

           allFillParent = allFillParent && lp.width == LayoutParams.MATCH_PARENT;
           if (lp.weight > 0) {
               /*
                * Widths of weighted Views are bogus if we end up
                * remeasuring, so keep them separate.
                */
               weightedMaxWidth = Math.max(weightedMaxWidth,
                       matchWidthLocally ? margin : measuredWidth);
           } else {
               alternativeMaxWidth = Math.max(alternativeMaxWidth,
                       matchWidthLocally ? margin : measuredWidth);
           }

           i += getChildrenSkipCount(child, i);
       }

       if (nonSkippedChildCount > 0 && hasDividerBeforeChildAt(count)) {
           mTotalLength += mDividerHeight;
       }

       if (useLargestChild &&
               (heightMode == MeasureSpec.AT_MOST || heightMode == MeasureSpec.UNSPECIFIED)) {
           mTotalLength = 0;

           for (int i = 0; i < count; ++i) {
               final View child = getVirtualChildAt(i);
               if (child == null) {
                   mTotalLength += measureNullChild(i);
                   continue;
               }

               if (child.getVisibility() == GONE) {
                   i += getChildrenSkipCount(child, i);
                   continue;
               }

               final LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams)
                       child.getLayoutParams();
               // Account for negative margins
               final int totalLength = mTotalLength;
               mTotalLength = Math.max(totalLength, totalLength + largestChildHeight +
                       lp.topMargin + lp.bottomMargin + getNextLocationOffset(child));
           }
       }

       // Add in our padding
       mTotalLength += mPaddingTop + mPaddingBottom;

       int heightSize = mTotalLength;

       // Check against our minimum height
       heightSize = Math.max(heightSize, getSuggestedMinimumHeight());

       // Reconcile our calculated size with the heightMeasureSpec
       int heightSizeAndState = resolveSizeAndState(heightSize, heightMeasureSpec, 0);
       heightSize = heightSizeAndState & MEASURED_SIZE_MASK;
       // Either expand children with weight to take up available space or
       // shrink them if they extend beyond our current bounds. If we skipped
       // measurement on any children, we need to measure them now.
       int remainingExcess = heightSize - mTotalLength
               + (mAllowInconsistentMeasurement ? 0 : consumedExcessSpace);
       if (skippedMeasure || remainingExcess != 0 && totalWeight > 0.0f) {
           float remainingWeightSum = mWeightSum > 0.0f ? mWeightSum : totalWeight;

           mTotalLength = 0;

           for (int i = 0; i < count; ++i) {
               final View child = getVirtualChildAt(i);
               if (child == null || child.getVisibility() == View.GONE) {
                   continue;
               }

               final LayoutParams lp = (LayoutParams) child.getLayoutParams();
               final float childWeight = lp.weight;
               if (childWeight > 0) {
                   final int share = (int) (childWeight * remainingExcess / remainingWeightSum);
                   remainingExcess -= share;
                   remainingWeightSum -= childWeight;

                   final int childHeight;
                   if (mUseLargestChild && heightMode != MeasureSpec.EXACTLY) {
                       childHeight = largestChildHeight;
                   } else if (lp.height == 0 && (!mAllowInconsistentMeasurement
                           || heightMode == MeasureSpec.EXACTLY)) {
                       // This child needs to be laid out from scratch using
                       // only its share of excess space.
                       childHeight = share;
                   } else {
                       // This child had some intrinsic height to which we
                       // need to add its share of excess space.
                       childHeight = child.getMeasuredHeight() + share;
                   }

                   final int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(
                           Math.max(0, childHeight), MeasureSpec.EXACTLY);
                   final int childWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec,
                           mPaddingLeft + mPaddingRight + lp.leftMargin + lp.rightMargin,
                           lp.width);
                   child.measure(childWidthMeasureSpec, childHeightMeasureSpec);

                   // Child may now not fit in vertical dimension.
                   childState = combineMeasuredStates(childState, child.getMeasuredState()
                           & (MEASURED_STATE_MASK>>MEASURED_HEIGHT_STATE_SHIFT));
               }

               final int margin =  lp.leftMargin + lp.rightMargin;
               final int measuredWidth = child.getMeasuredWidth() + margin;
               maxWidth = Math.max(maxWidth, measuredWidth);

               boolean matchWidthLocally = widthMode != MeasureSpec.EXACTLY &&
                       lp.width == LayoutParams.MATCH_PARENT;

               alternativeMaxWidth = Math.max(alternativeMaxWidth,
                       matchWidthLocally ? margin : measuredWidth);

               allFillParent = allFillParent && lp.width == LayoutParams.MATCH_PARENT;

               final int totalLength = mTotalLength;
               mTotalLength = Math.max(totalLength, totalLength + child.getMeasuredHeight() +
                       lp.topMargin + lp.bottomMargin + getNextLocationOffset(child));
           }

           // Add in our padding
           mTotalLength += mPaddingTop + mPaddingBottom;
           // TODO: Should we recompute the heightSpec based on the new total length?
       } else {
           alternativeMaxWidth = Math.max(alternativeMaxWidth,
                                          weightedMaxWidth);


           // We have no limit, so make all weighted views as tall as the largest child.
           // Children will have already been measured once.
           if (useLargestChild && heightMode != MeasureSpec.EXACTLY) {
               for (int i = 0; i < count; i++) {
                   final View child = getVirtualChildAt(i);
                   if (child == null || child.getVisibility() == View.GONE) {
                       continue;
                   }

                   final LinearLayout.LayoutParams lp =
                           (LinearLayout.LayoutParams) child.getLayoutParams();

                   float childExtra = lp.weight;
                   if (childExtra > 0) {
                       child.measure(
                               MeasureSpec.makeMeasureSpec(child.getMeasuredWidth(),
                                       MeasureSpec.EXACTLY),
                               MeasureSpec.makeMeasureSpec(largestChildHeight,
                                       MeasureSpec.EXACTLY));
                   }
               }
           }
       }

       if (!allFillParent && widthMode != MeasureSpec.EXACTLY) {
           maxWidth = alternativeMaxWidth;
       }

       maxWidth += mPaddingLeft + mPaddingRight;

       // Check against our minimum width
       maxWidth = Math.max(maxWidth, getSuggestedMinimumWidth());

       setMeasuredDimension(resolveSizeAndState(maxWidth, widthMeasureSpec, childState),
               heightSizeAndState);

       if (matchWidth) {
           forceUniformWidth(count, heightMeasureSpec);
       }
   }




```

从上面的代码可以看到会遍历子控件 在遍历子控件的时候会调用`measureChildBeforeLayout`方法,具体代码参照下面。
在调用`measureChildWithMargins`方法，这样就可以调用子控件的measure方法，在调用子控件的measure的时候，会调用`onMeasure`方法，这样遍历调用，也看出遍历子控件是深度优先的遍历
使用`mTotalLength`来存储竖直方向的高度，包括竖直方向上面的margin和子元素的高度等



当子元素测量完毕之后会测量自己的大小
```
void measureChildBeforeLayout(View child, int childIndex,
     int widthMeasureSpec, int totalWidth, int heightMeasureSpec,
     int totalHeight) {
 measureChildWithMargins(child, widthMeasureSpec, totalWidth,
         heightMeasureSpec, totalHeight);
}

```

```

protected void measureChildWithMargins(View child,
         int parentWidthMeasureSpec, int widthUsed,
         int parentHeightMeasureSpec, int heightUsed) {
     final MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();

     final int childWidthMeasureSpec = getChildMeasureSpec(parentWidthMeasureSpec,
             mPaddingLeft + mPaddingRight + lp.leftMargin + lp.rightMargin
                     + widthUsed, lp.width);
     final int childHeightMeasureSpec = getChildMeasureSpec(parentHeightMeasureSpec,
             mPaddingTop + mPaddingBottom + lp.topMargin + lp.bottomMargin
                     + heightUsed, lp.height);

     child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
 }
```

View的Measure是三大流程中最复杂的一个，measure完成之后，通过getMeasureWidth/Height方法就可以正确的获取到View的测量宽和高了，需要注意，在某种极端情况下，需要多次measure才能测量出最终的宽和高，在onMeasure中的宽和高是不稳定的，最好的方式是在onLayout中获取宽和高
上面已经对View的measure进行了详细的分析，现在需要考虑到一种情况，如果在Activity已经启动的时候做一个任务，但是这个任务需要获取某一个View的高度，这个时候在onCreate，onResume和onStart中都是无法正确获取宽和高的。因为View的measure方法和Activity的生命周期方法不是同步执行的。所以需要在
1. Activity/View# `OnWindowFocusChanged`
这个方法的含义是View已经初始化完毕了，宽和高已经测量完毕了，需要注意当Activity的窗口得到焦点和失去焦点是均会被调用一次`OnWindowFocusChanged`，如果频繁的onResume和onPause，那么这个方法也会被调用
2. View.post(runnable)
通过post可以将一个runnableto投递到消息队列的尾部，然后等待looper调用此runnable的时候，View已经初始化完毕了
```
protected void onStart(){
super.OnStart();
View.post(new Runnable((){
  public void run(){
    int height=view.getMeasureHeight();
    int width=view.getMeasureWidth(();)
  }
  }));

}
```

3. ViewTreeObserver
使用ViewTreeObserver的众多回调可以完成这个功能，比如使用onGloablLayoutListener这个接口，当View的树结构发生变化或者View树内部可见性发生变化时候，`onGlobalLayout`方法将会被回调，因此这是获取View的宽和高的一次好机会，需要注意随着View树结构状态的变化，`onGlobalLayout`会被调用多次。
```
@Override
protected void onStart() {
    super.onStart();

    ViewTreeObserver viewTreeObserver=view.getViewTreeObserver();
    viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            int measuredHeight = view.getMeasuredHeight();
            int measuredWidth = view.getMeasuredWidth();
        }
    });
}

```

4. view.measure(xxx,xxx)
通过手动对View进行measure得到。这个情况适合复杂情况的处理

这种情况需要依据LayoutParams来区分
MATCH_PARENT
直接放弃，无法measure出具体的宽和高，原因很简单，依据之前的表和measure过程，构造这种MeasureSpec需要知道parentSize，即父控件的剩余空间，但是这个时候无法获取parentSize大小，所以理论上不可能测量出View的大小

具体数值
比如宽和高都是100dp,如下

```
int  widthMeasureSpec =measureSpec.makeMeasureSpec(100,MeasureSpec.EXACTLY)
int  heightMeasureSpec =measureSpec.makeMeasureSpec(100,MeasureSpec.EXACTLY)
view.measure(widthMeasureSpec,heightMeasureSpec)

```

WRAP_CONTENT
```
int  widthMeasureSpec =measureSpec.makeMeasureSpec((1<<30)-1,MeasureSpec.AT_MOST)
int  heightMeasureSpec =measureSpec.makeMeasureSpec((1<<30)-1,MeasureSpec.AT_MOST)
view.measure(widthMeasureSpec,heightMeasureSpec)

```
注意到(1<<30)-1 通过分析MeasureSpec可以知道View的尺寸使用30位二进制表示。也就是最大是30个1 理论上使用最大值构造是合理的
这里有两种错误的做法，为什么错误是因为违背了系统内部的实现规范

第一种：
```
int widthMeasureSpec=measureSpec.makeMeasureSpec(-1,NeasureSpec.UNSPECIFIED);
int heightMeasureSpec=measureSpec.makeMeasureSpec(-1,NeasureSpec.UNSPECIFIED);
view.measure(widthMeasureSpec,heightMeasureSpec)


```
第二种：

```
view.measure(LayoutParams.wrap_content,LayoutParams.Wrap_content)
```

### layout过程

layoutde 作用是用来确定子元素的位置，当ViewGroup的位置被确定之后，他在onLayout中会遍历子元素并调用他的layout方法。在layout中onLayout方法又会被调用，layout比measure过程要简单很多，layout方法确定了View本身的位置，而onLayout方法会确定所有子元素的位置。先看View的layout方法


```
public void layout(int l, int t, int r, int b) {
      if ((mPrivateFlags3 & PFLAG3_MEASURE_NEEDED_BEFORE_LAYOUT) != 0) {
          onMeasure(mOldWidthMeasureSpec, mOldHeightMeasureSpec);
          mPrivateFlags3 &= ~PFLAG3_MEASURE_NEEDED_BEFORE_LAYOUT;
      }

      int oldL = mLeft;
      int oldT = mTop;
      int oldB = mBottom;
      int oldR = mRight;

      boolean changed = isLayoutModeOptical(mParent) ?
              setOpticalFrame(l, t, r, b) : setFrame(l, t, r, b);

      if (changed || (mPrivateFlags & PFLAG_LAYOUT_REQUIRED) == PFLAG_LAYOUT_REQUIRED) {
          onLayout(changed, l, t, r, b);

          if (shouldDrawRoundScrollbar()) {
              if(mRoundScrollbarRenderer == null) {
                  mRoundScrollbarRenderer = new RoundScrollbarRenderer(this);
              }
          } else {
              mRoundScrollbarRenderer = null;
          }

          mPrivateFlags &= ~PFLAG_LAYOUT_REQUIRED;

          ListenerInfo li = mListenerInfo;
          if (li != null && li.mOnLayoutChangeListeners != null) {
              ArrayList<OnLayoutChangeListener> listenersCopy =
                      (ArrayList<OnLayoutChangeListener>)li.mOnLayoutChangeListeners.clone();
              int numListeners = listenersCopy.size();
              for (int i = 0; i < numListeners; ++i) {
                  listenersCopy.get(i).onLayoutChange(this, l, t, r, b, oldL, oldT, oldR, oldB);
              }
          }
      }

      mPrivateFlags &= ~PFLAG_FORCE_LAYOUT;
      mPrivateFlags3 |= PFLAG3_IS_LAID_OUT;

      if ((mPrivateFlags3 & PFLAG3_NOTIFY_AUTOFILL_ENTER_ON_LAYOUT) != 0) {
          mPrivateFlags3 &= ~PFLAG3_NOTIFY_AUTOFILL_ENTER_ON_LAYOUT;
          notifyEnterOrExitForAutoFillIfNeeded(true);
      }
  }


  protected boolean setFrame(int left, int top, int right, int bottom) {
        boolean changed = false;

        if (DBG) {
            Log.d("View", this + " View.setFrame(" + left + "," + top + ","
                    + right + "," + bottom + ")");
        }

        if (mLeft != left || mRight != right || mTop != top || mBottom != bottom) {
            changed = true;

            // Remember our drawn bit
            int drawn = mPrivateFlags & PFLAG_DRAWN;

            int oldWidth = mRight - mLeft;
            int oldHeight = mBottom - mTop;
            int newWidth = right - left;
            int newHeight = bottom - top;
            boolean sizeChanged = (newWidth != oldWidth) || (newHeight != oldHeight);

            // Invalidate our old position
            invalidate(sizeChanged);

            mLeft = left;
            mTop = top;
            mRight = right;
            mBottom = bottom;
            mRenderNode.setLeftTopRightBottom(mLeft, mTop, mRight, mBottom);

            mPrivateFlags |= PFLAG_HAS_BOUNDS;


            if (sizeChanged) {
                sizeChange(newWidth, newHeight, oldWidth, oldHeight);
            }

            if ((mViewFlags & VISIBILITY_MASK) == VISIBLE || mGhostView != null) {
                // If we are visible, force the DRAWN bit to on so that
                // this invalidate will go through (at least to our parent).
                // This is because someone may have invalidated this view
                // before this call to setFrame came in, thereby clearing
                // the DRAWN bit.
                mPrivateFlags |= PFLAG_DRAWN;
                invalidate(sizeChanged);
                // parent display list may need to be recreated based on a change in the bounds
                // of any child
                invalidateParentCaches();
            }

            // Reset drawn bit to original value (invalidate turns it off)
            mPrivateFlags |= drawn;

            mBackgroundSizeChanged = true;
            mDefaultFocusHighlightSizeChanged = true;
            if (mForegroundInfo != null) {
                mForegroundInfo.mBoundsChanged = true;
            }

            notifySubtreeAccessibilityStateChangedIfNeeded();
        }
        return changed;
    }

```

layout方法的大致流程如下，首先会通过setFrame方法来设定View的四个顶点的位置，即初始化mLeft，mRight，mTop，mBottom这四个值，View的四个顶点一旦确定，View在父容器中的位置也就确定了；接着会调用onLayout方法，这个用于确定子元素的位置，和onMeasure方法一样，具体的实现和实现类有关，所以View和ViewGroup都没有真正实现onLayout方法。接下来我们可以看一下LinearLayout的onLayout方法。

```
protected void onLayout(boolean changed, int l, int t, int r, int b) {
     if (mOrientation == VERTICAL) {
         layoutVertical(l, t, r, b);
     } else {
         layoutHorizontal(l, t, r, b);
     }
 }

```

和onMeasure一样。他分为数值方向和水平方向，这里分析一下layoutVertical的代码逻辑

```
void layoutVertical(int left, int top, int right, int bottom) {
     final int paddingLeft = mPaddingLeft;

     int childTop;
     int childLeft;

     // Where right end of child should go
     final int width = right - left;
     int childRight = width - mPaddingRight;

     // Space available for child
     int childSpace = width - paddingLeft - mPaddingRight;

     final int count = getVirtualChildCount();

     final int majorGravity = mGravity & Gravity.VERTICAL_GRAVITY_MASK;
     final int minorGravity = mGravity & Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK;

     switch (majorGravity) {
        case Gravity.BOTTOM:
            // mTotalLength contains the padding already
            childTop = mPaddingTop + bottom - top - mTotalLength;
            break;

            // mTotalLength contains the padding already
        case Gravity.CENTER_VERTICAL:
            childTop = mPaddingTop + (bottom - top - mTotalLength) / 2;
            break;

        case Gravity.TOP:
        default:
            childTop = mPaddingTop;
            break;
     }

     for (int i = 0; i < count; i++) {
         final View child = getVirtualChildAt(i);
         if (child == null) {
             childTop += measureNullChild(i);
         } else if (child.getVisibility() != GONE) {
             final int childWidth = child.getMeasuredWidth();
             final int childHeight = child.getMeasuredHeight();

             final LinearLayout.LayoutParams lp =
                     (LinearLayout.LayoutParams) child.getLayoutParams();

             int gravity = lp.gravity;
             if (gravity < 0) {
                 gravity = minorGravity;
             }
             final int layoutDirection = getLayoutDirection();
             final int absoluteGravity = Gravity.getAbsoluteGravity(gravity, layoutDirection);
             switch (absoluteGravity & Gravity.HORIZONTAL_GRAVITY_MASK) {
                 case Gravity.CENTER_HORIZONTAL:
                     childLeft = paddingLeft + ((childSpace - childWidth) / 2)
                             + lp.leftMargin - lp.rightMargin;
                     break;

                 case Gravity.RIGHT:
                     childLeft = childRight - childWidth - lp.rightMargin;
                     break;

                 case Gravity.LEFT:
                 default:
                     childLeft = paddingLeft + lp.leftMargin;
                     break;
             }

             if (hasDividerBeforeChildAt(i)) {
                 childTop += mDividerHeight;
             }

             childTop += lp.topMargin;
             // 在这里
             setChildFrame(child, childLeft, childTop + getLocationOffset(child),
                     childWidth, childHeight);
             childTop += childHeight + lp.bottomMargin + getNextLocationOffset(child);

             i += getChildrenSkipCount(child, i);
         }
     }
 }

```

看一下上方的代码。可以看到会遍历所有的子元素并调用getChildFram方法来为子元素指定位置。其中childTop会逐渐增大，这就意味着后面的子元素会被放置在靠下的位置，这刚好符合竖直方向的特性，至于getChildFram，他有调用子元素的layout方法，这样父元素在layout方法中完成了堆自己的定位后，就通过onLayout方法去调用子元素的layout方法，子元素优惠通过layout来确定自己的位置，这样一层一层传递下去，就完成了对View树的遍历过程。setChildFram方法如下

```
private void setChildFrame(View child, int left, int top, int width, int height) {
     child.layout(left, top, left + width, top + height);
 }

```

setChildFrame的值是child的width和height。
这里来回答一个之前的问题，View的测量宽高和最终宽高的区别：这个问题可以翻译为View的getMeasureWidth和getWidth这两个方法有什么区别
看一下getWidth的方法实现

```
public final int getWidth(){
  return mRight-mLeft;
}
```

从getWidth的源码和结合mLeft、mRight、mTop、mBottom这四个变量的负值过程来看，getWIdth方法返回的就是View的测量宽度。经过上述分析，可以回答这个问题。在View的默认实现中，View的测量宽和高和最终宽和高是相等的，只不过测量宽和高形成在View的measure过程，而最终宽和高形成与View的layout过程，即两者的赋值时机不同，测量宽和高的赋值时机稍微早一些。但是在一些情况下确实会不一样
比如

```
重写了View的layout方法

public void layout(int l,int t,int r,int b){
  suber.layout(l,t,r+100,b+100);

}
```

这样会导致View的最终宽和高总是比测量宽和高大100px，这样会导致View显示不正常。还有一种是要多次测量的情况，前几次测量的宽和高可能和最终宽和高不一致

### draw过程

draw过程就比较简单了，他的作用是将View绘制到屏幕上面，View的绘制过程遵循下面几步：
1. 绘制背景
2. 绘制自己
3. 绘制children
4. 绘制装饰

这个可以从的draw源码可以很明显的看出


```
public void draw(Canvas canvas) {
       final int privateFlags = mPrivateFlags;
       final boolean dirtyOpaque = (privateFlags & PFLAG_DIRTY_MASK) == PFLAG_DIRTY_OPAQUE &&
               (mAttachInfo == null || !mAttachInfo.mIgnoreDirtyState);
       mPrivateFlags = (privateFlags & ~PFLAG_DIRTY_MASK) | PFLAG_DRAWN;

       /*
        * Draw traversal performs several drawing steps which must be executed
        * in the appropriate order:
        *
        *      1. Draw the background
        *      2. If necessary, save the canvas' layers to prepare for fading
        *      3. Draw view's content
        *      4. Draw children
        *      5. If necessary, draw the fading edges and restore layers
        *      6. Draw decorations (scrollbars for instance)
        */

       // Step 1, draw the background, if needed
       int saveCount;

       if (!dirtyOpaque) {
           drawBackground(canvas);
       }

       // skip step 2 & 5 if possible (common case)
       final int viewFlags = mViewFlags;
       boolean horizontalEdges = (viewFlags & FADING_EDGE_HORIZONTAL) != 0;
       boolean verticalEdges = (viewFlags & FADING_EDGE_VERTICAL) != 0;
       if (!verticalEdges && !horizontalEdges) {
           // Step 3, draw the content
           if (!dirtyOpaque) onDraw(canvas);

           // Step 4, draw the children
           dispatchDraw(canvas);

           drawAutofilledHighlight(canvas);

           // Overlay is part of the content and draws beneath Foreground
           if (mOverlay != null && !mOverlay.isEmpty()) {
               mOverlay.getOverlayView().dispatchDraw(canvas);
           }

           // Step 6, draw decorations (foreground, scrollbars)
           onDrawForeground(canvas);

           // Step 7, draw the default focus highlight
           drawDefaultFocusHighlight(canvas);

           if (debugDraw()) {
               debugDrawFocus(canvas);
           }

           // we're done...
           return;
       }

       /*
        * Here we do the full fledged routine...
        * (this is an uncommon case where speed matters less,
        * this is why we repeat some of the tests that have been
        * done above)
        */

       boolean drawTop = false;
       boolean drawBottom = false;
       boolean drawLeft = false;
       boolean drawRight = false;

       float topFadeStrength = 0.0f;
       float bottomFadeStrength = 0.0f;
       float leftFadeStrength = 0.0f;
       float rightFadeStrength = 0.0f;

       // Step 2, save the canvas' layers
       int paddingLeft = mPaddingLeft;

       final boolean offsetRequired = isPaddingOffsetRequired();
       if (offsetRequired) {
           paddingLeft += getLeftPaddingOffset();
       }

       int left = mScrollX + paddingLeft;
       int right = left + mRight - mLeft - mPaddingRight - paddingLeft;
       int top = mScrollY + getFadeTop(offsetRequired);
       int bottom = top + getFadeHeight(offsetRequired);

       if (offsetRequired) {
           right += getRightPaddingOffset();
           bottom += getBottomPaddingOffset();
       }

       final ScrollabilityCache scrollabilityCache = mScrollCache;
       final float fadeHeight = scrollabilityCache.fadingEdgeLength;
       int length = (int) fadeHeight;

       // clip the fade length if top and bottom fades overlap
       // overlapping fades produce odd-looking artifacts
       if (verticalEdges && (top + length > bottom - length)) {
           length = (bottom - top) / 2;
       }

       // also clip horizontal fades if necessary
       if (horizontalEdges && (left + length > right - length)) {
           length = (right - left) / 2;
       }

       if (verticalEdges) {
           topFadeStrength = Math.max(0.0f, Math.min(1.0f, getTopFadingEdgeStrength()));
           drawTop = topFadeStrength * fadeHeight > 1.0f;
           bottomFadeStrength = Math.max(0.0f, Math.min(1.0f, getBottomFadingEdgeStrength()));
           drawBottom = bottomFadeStrength * fadeHeight > 1.0f;
       }

       if (horizontalEdges) {
           leftFadeStrength = Math.max(0.0f, Math.min(1.0f, getLeftFadingEdgeStrength()));
           drawLeft = leftFadeStrength * fadeHeight > 1.0f;
           rightFadeStrength = Math.max(0.0f, Math.min(1.0f, getRightFadingEdgeStrength()));
           drawRight = rightFadeStrength * fadeHeight > 1.0f;
       }

       saveCount = canvas.getSaveCount();

       int solidColor = getSolidColor();
       if (solidColor == 0) {
           final int flags = Canvas.HAS_ALPHA_LAYER_SAVE_FLAG;

           if (drawTop) {
               canvas.saveLayer(left, top, right, top + length, null, flags);
           }

           if (drawBottom) {
               canvas.saveLayer(left, bottom - length, right, bottom, null, flags);
           }

           if (drawLeft) {
               canvas.saveLayer(left, top, left + length, bottom, null, flags);
           }

           if (drawRight) {
               canvas.saveLayer(right - length, top, right, bottom, null, flags);
           }
       } else {
           scrollabilityCache.setFadeColor(solidColor);
       }

       // Step 3, draw the content
       if (!dirtyOpaque) onDraw(canvas);

       // Step 4, draw the children
       dispatchDraw(canvas);

       // Step 5, draw the fade effect and restore layers
       final Paint p = scrollabilityCache.paint;
       final Matrix matrix = scrollabilityCache.matrix;
       final Shader fade = scrollabilityCache.shader;

       if (drawTop) {
           matrix.setScale(1, fadeHeight * topFadeStrength);
           matrix.postTranslate(left, top);
           fade.setLocalMatrix(matrix);
           p.setShader(fade);
           canvas.drawRect(left, top, right, top + length, p);
       }

       if (drawBottom) {
           matrix.setScale(1, fadeHeight * bottomFadeStrength);
           matrix.postRotate(180);
           matrix.postTranslate(left, bottom);
           fade.setLocalMatrix(matrix);
           p.setShader(fade);
           canvas.drawRect(left, bottom - length, right, bottom, p);
       }

       if (drawLeft) {
           matrix.setScale(1, fadeHeight * leftFadeStrength);
           matrix.postRotate(-90);
           matrix.postTranslate(left, top);
           fade.setLocalMatrix(matrix);
           p.setShader(fade);
           canvas.drawRect(left, top, left + length, bottom, p);
       }

       if (drawRight) {
           matrix.setScale(1, fadeHeight * rightFadeStrength);
           matrix.postRotate(90);
           matrix.postTranslate(right, top);
           fade.setLocalMatrix(matrix);
           p.setShader(fade);
           canvas.drawRect(right - length, top, right, bottom, p);
       }

       canvas.restoreToCount(saveCount);

       drawAutofilledHighlight(canvas);

       // Overlay is part of the content and draws beneath Foreground
       if (mOverlay != null && !mOverlay.isEmpty()) {
           mOverlay.getOverlayView().dispatchDraw(canvas);
       }

       // Step 6, draw decorations (foreground, scrollbars)
       onDrawForeground(canvas);

       if (debugDraw()) {
           debugDrawFocus(canvas);
       }
   }
```


首先调用`drawBackground`方法，其次调用`dispatchDraw(canvas);`来传递绘制过程，看一下ViewGroup的的`dispatchDraw(canvas)`过程，可以看到会调用`drawChild`方法，这个时候就会调用child的`draw`方法，这样就一层层传递下去了，View有一个特殊的方法，serWillNotDraw，先看一下他的源码
```
/**
 * If this view doesn't do any drawing on its own, set this flag to
 * allow further optimizations. By default, this flag is not set on
 * View, but could be set on some View subclasses such as ViewGroup.
 *
 * Typically, if you override {@link #onDraw(android.graphics.Canvas)}
 * you should clear this flag.
 *
 * @param willNotDraw whether or not this View draw on its own
 */
public void setWillNotDraw(boolean willNotDraw) {
    setFlags(willNotDraw ? WILL_NOT_DRAW : 0, DRAW_MASK);
}

```

从这个方法的注释可以看出，如果一个View不需要绘制任何内容，那么设置这个标记位为true之后，系统会进行优化。默认情况下，View没有启动这个标记位，但是Viewgroup模式启动这个优化标记位，这个标记位对于实际看法的意义是：当我们自定义控件ViewGroup并且本身不具备绘制功能时，就可以开启这个标记位以便系统进行优化。当然明确知道一个ViewGroup需要onDraw来绘制内容是，需要显示关闭这个WILL_NOT_DRAW这个标记位。


## 自定义View
这一节讲解一下自定义View相关知识，自定义View的作用就不用多说了，下面进入正题。

### 自定义View的分类
自定义View这里，书的作者分为四类
1. 重写View的onDraw方法
这种方法主要用来实现一些不规则的效果，即这种显示效果不方便使用布局直接叠加或者组合得到，往往需要静态或者动态显示一些不规则图形，比如股票的折线图，圆饼图等等。这个就要求重写onDraw方法，采用这种方式需要自己支持wrap_content，并且padding也需要自己处理
2. 继承ViewGroup派生特殊的Layout
这种方式主要实现了自定义布局，即除了LinearLayout、RelativeLayout、FrameLayout这几种系统布局之外，重新定义了一种新布局。当某种效果看起来很像几种View组合在一起的时候，可以采用这种方法，一般要处理ViewGroup的测量和布局这两个过程，并同时处理子元素的测量和布局过程
3. 继承特定的View(比如：TextView)
这种方式比较常见，一般是用于拓展某种已有的View的功能，比如TextView，这个方式比较容易，不用自己处理wrap_content和padding
4. 继承特定的ViewGroup(比如：LinearLayout)
这种方式也是比较常见的，当某种效果看起来像是几个View组合在一起的时候，可以使用这种方式。采用这种方式不需要自己处理测量和布局这两个过程。

### 自定义View须知
介绍一下自定义View过程中的一些注意事项，这些问题如果处理不好，会影响View的正常使用，而有些则会导致内存泄漏等。
1. 让View支持wrap_content
这个是因为直接继承View或者ViewGroup的控件，如果不在onMeasure中对wrap_content做特殊处理，那么当外界在不居中使用wrap_content的时候就无法达到预期的效果，具体原因在之前已经介绍过了
2. 如果有必要，让你的View支持padding
这个是因为直接继承View的控件，如果不在draw方法中处理padding，那么padding属性是无法起作用的，另外，直接继承自ViewGroup的控件需要在onMeasure和onLayout中考虑padding和子元素的margin对其造成的影响
3. 尽量不要在View中使用Handler，没必要
这个是因为View本身提供了post系列方法，完全可以替代Handler的作用，当然除非你很明确的要使用handler来发送消息
4. View中如果有动画或者线程，需要及时停止，参考View#OnDetachedFromWindow
这一条也很好理解，如果有线程或者动画需要停止，那么OnDetachedFromWindow是一个很好的时机，当包含View的Activity退出当前View或者当前View被remove时，View的onDetachedFromWindow方法会被调用，和此方法对应的是onAttachedToWindow。当包含此View的Activity启动时，这个方法就会被调用。当View不可见或者我们需要停止线程或者动画的时候，如果不及时处理，可能导致内存泄漏
5. View带有滑动嵌套的情景时，需要处理好滑动冲突
如果有滑动冲突的话，那么要合适的处理滑动冲突。否则将严重影响View的效果，如何处理之前介绍过


### 自定义View的示例
这里按照自定义View的分类来进行
1. 继承View重写onDraw方法
这里看主要用于实现一些特殊的图形，一般需要重写onDraw方法采用这用方式需要自己支持wrap_content，并且padding也需要自己来处理。这里就来一个demo练习一下。
为了更好的展示一些平时不容易注意的问题，这里选择实现一个很简单的自定义控件，绘制一个园。为了实现一个规范的自定义控件，需要考虑到wrap_content模式以及padding，同时为了提高便捷性，还要对外提供自定义属性。这里来看一下
第一个粗糙版本
```
public class CircleView extends View {
    private int mColor= Color.RED;
    private Paint mPaint=new Paint(Paint.ANTI_ALIAS_FLAG);


    public CircleView(Context context) {
        super(context);
        init();
    }
    public CircleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CircleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width=getWidth();
        int height=getHeight();
        int radius=Math.min(width,height)/2;
        canvas.drawCircle(width/2,height/2,radius,mPaint);
    }
    private void init() {
        mPaint.setColor(mColor);
    }
}

```

来看一下在不同布局情况下的显示
第一种情况：
```
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="#ffffff"
    tools:context="com.smart.kaifa.MainActivity">

    <com.smart.kaifa.CircleView
        android:layout_width="match_parent"
        android:layout_height="100dp"
        android:background="#000000" />
</FrameLayout>

```

![Alt text](device-2018-04-30-121159.png "没有设置margin的情况")

第二种情况： 设置margin的情况
```
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="#ffffff"
    tools:context="com.smart.kaifa.MainActivity">
    <com.smart.kaifa.CircleView
        android:layout_width="match_parent"
        android:layout_height="100dp"
        android:layout_margin="20dp"
        android:background="#000000" />
</FrameLayout>

```

![Alt text](device-2018-04-30-121943.png "设置margin的情况")

第三种情况： 设置margin的情况

```
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="#ffffff"
    tools:context="com.smart.kaifa.MainActivity">
    <com.smart.kaifa.CircleView
        android:layout_width="match_parent"
        android:layout_height="100dp"
        android:layout_margin="20dp"
        android:padding="20dp"
        android:background="#000000" />
</FrameLayout>
```
![Alt text](device-2018-04-30-121943.png "设置margin和padding的情况")
可以看到和之前一样，这个就说明padding需要我们自己来处理
第四种情况： 设置wrap_content的情况
```
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="#ffffff"
    tools:context="com.smart.kaifa.MainActivity">
    <com.smart.kaifa.CircleView
        android:layout_width="wrap_content"
        android:layout_height="100dp"
        android:layout_margin="20dp"
        android:padding="20dp"
        android:background="#000000" />
</FrameLayout>

```
![Alt text](device-2018-04-30-121943.png "设置wrap_content的情况")
可以看到并没有任何变化，不能满足我们的需求，这里的wrap_content和match_parent没有任何区别，可以看一下上面对ViewGroup的`getChildMeasureSpec`这个方法的分析就明白了


* 首先针对第一种情况wrap_content
```
@Override
protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    int widthSpecMode=MeasureSpec.getMode(widthMeasureSpec);
    int heightSpecMode=MeasureSpec.getMode(heightMeasureSpec);
    int widthSpecSize=MeasureSpec.getSize(widthMeasureSpec);
    int heightSpecSize=MeasureSpec.getSize(heightMeasureSpec);
    if(widthSpecMode==MeasureSpec.AT_MOST&&heightSpecMode==MeasureSpec.AT_MOST){
        setMeasuredDimension(mWidth.mHeight);
    }else   if(widthSpecMode==MeasureSpec.AT_MOST){
        setMeasuredDimension(mWidth.heightSpecSize);
    }else   if(heightSpecMode==MeasureSpec.AT_MOST){
        setMeasuredDimension(widthSpecSize.mHeight);
    }
}
```
可以参考之前的处理方式在4.3.1那一段已经做了介绍，这里就不再次说明了。
* 其次，针对padding的问题，只需要在绘制的时候考虑一下padding就可以


```
public class CircleView extends View {
    private int mColor= Color.RED;
    private Paint mPaint=new Paint(Paint.ANTI_ALIAS_FLAG);
    public CircleView(Context context) {
        super(context);
        init();
    }
    public CircleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CircleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthSpecMode=MeasureSpec.getMode(widthMeasureSpec);
        int heightSpecMode=MeasureSpec.getMode(heightMeasureSpec);
        int widthSpecSize=MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecSize=MeasureSpec.getSize(heightMeasureSpec);
        if(widthSpecMode==MeasureSpec.AT_MOST&&heightSpecMode==MeasureSpec.AT_MOST){
            setMeasuredDimension(200,200);
        }else   if(widthSpecMode==MeasureSpec.AT_MOST){
            setMeasuredDimension(200,heightSpecSize);
        }else   if(heightSpecMode==MeasureSpec.AT_MOST){
            setMeasuredDimension(widthSpecSize,200);
        }
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int paddingBottom = getPaddingBottom();
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        int paddingTop = getPaddingTop();
        int width=getWidth()-paddingLeft-paddingRight;
        int height=getHeight()-paddingTop-paddingBottom;
        int radius=Math.min(width,height)/2;
        canvas.drawCircle(paddingLeft+width/2,paddingTop+height/2,radius,mPaint);
    }
    private void init() {
        mPaint.setColor(mColor);
    }
}

```

看一下修改之后2.0版本的代码
```
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="#ffffff"
    tools:context="com.smart.kaifa.MainActivity">
    <com.smart.kaifa.CircleView
        android:layout_width="wrap_content"
        android:layout_height="100dp"
        android:layout_margin="20dp"
        android:padding="20dp"
        android:background="#000000" />
</FrameLayout>

```
现在看一下效果
![Alt text](device-2018-04-30-134320.png "2.0版本效果")

最后为了让这个控件更加容易使用，我们使用自定义属性来强化他
如何添加自定义属性呢？
第一步是在attrs.xml中或者新建attrs_circle_view.xml这类以attrs开头的文件。在value文件夹中新建一个attres.xml或者attrs_circle_view.xml文件

```
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <declare-styleable name="CircleView">
        <attr name="circle_color" format="color"/>
    </declare-styleable>
</resources>
```
使用自定义属性
```
public class CircleView extends View {
    private int mColor= Color.RED;
    private Paint mPaint=new Paint(Paint.ANTI_ALIAS_FLAG);
    public CircleView(Context context) {
        super(context);
        init();
    }
    public CircleView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
        init();
    }

    public CircleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a=context.obtainStyledAttributes(attrs,R.styleable.CircleView);
        mColor=a.getColor(R.styleable.CircleView_circle_color,Color.RED);
        a.recycle();
        init();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthSpecMode=MeasureSpec.getMode(widthMeasureSpec);
        int heightSpecMode=MeasureSpec.getMode(heightMeasureSpec);
        int widthSpecSize=MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecSize=MeasureSpec.getSize(heightMeasureSpec);
        if(widthSpecMode==MeasureSpec.AT_MOST&&heightSpecMode==MeasureSpec.AT_MOST){
            setMeasuredDimension(200,200);
        }else   if(widthSpecMode==MeasureSpec.AT_MOST){
            setMeasuredDimension(200,heightSpecSize);
        }else   if(heightSpecMode==MeasureSpec.AT_MOST){
            setMeasuredDimension(widthSpecSize,200);
        }
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int paddingBottom = getPaddingBottom();
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        int paddingTop = getPaddingTop();
        int width=getWidth()-paddingLeft-paddingRight;
        int height=getHeight()-paddingTop-paddingBottom;
        int radius=Math.min(width,height)/2;
        canvas.drawCircle(paddingLeft+width/2,paddingTop+height/2,radius,mPaint);
    }
    private void init() {
        mPaint.setColor(mColor);
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
    android:background="#ffffff"
    tools:context="com.smart.kaifa.MainActivity">
    <com.smart.kaifa.CircleView
        android:layout_width="wrap_content"
        android:layout_height="100dp"
        android:layout_margin="20dp"
        android:padding="20dp"
        app:circle_color="#00ffff"
        android:background="#000000" />
</FrameLayout>

```

![Alt text](device-2018-04-30-153738.png "添加入自定义属性之后的效果")


2. 继承ViewGroup派生特殊的Layout
这种方式主要用于实现自定义的布局，采用这种方式稍微复杂一些，需要处理ViewGroup的测量和布局这两个过程，并同时处理子元素的测量和布局过程。
在第三章中我们分析了滑动冲突的两种情况，并实现两个自定义View：HorizontalScrollViewEx和StickyLayout，其中HorizontalScrollViewEx就是通过继承ViewGroup来实现的自定义View，这里会再次分析一下他的measure和layout过程。
需要说明的是，如果要采用这种方式实现一个很规范的自定义View，是有一定代价的，这里通过点击LinearLayout等的源码就可以知道，他们的实现都很复杂，对于这一个控件来说，这里不打算实现他的方方面面，仅仅完成主要的功能，但是会对需要优化的地方做出说明。
这里在回顾一下HorizontalScrollViewEx的功能，他主要是一个类似ViewPager的控件，也可以说是一个类似水平方向LinearLayout的控件，他的内部子元素可以进行水平滑动，并且子元素内部可以进行竖直滑动。这显然是存在冲突的，但是HorizontalScrollView内部解决了水平和竖直滑动冲突的问题，如何解决冲突，可以参照一下之前的方法，在父布局处理或者在子布局处理。
这里有一个假设：就是所有子元素的宽和高都是一样的。先看一下`onMeasure`方法和`onLayout`方法的具体实现。

```
@Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int measureWidth=0;
        int measureHeight=0;
        final int childCount=getChildCount();
        //测量子控件
        measureChildren(widthMeasureSpec,heightMeasureSpec);
        int widthSpaceSize=MeasureSpec.getSize(widthMeasureSpec);
        int widthSpeceMode=MeasureSpec.getMode(widthMeasureSpec);
        int heightSpaceSize=MeasureSpec.getSize(heightMeasureSpec);
        int heightSpaceMode=MeasureSpec.getMode(heightMeasureSpec);
        if(childCount==0){
            //设置宽和高
            setMeasuredDimension(0,0);
        }else if(widthSpeceMode==MeasureSpec.AT_MOST&&heightSpaceMode==MeasureSpec.AT_MOST){
            final View childView=getChildAt(0);
            measureWidth=childView.getMeasuredWidth()*childCount;
            measureHeight=childView.getMeasuredHeight();
            setMeasuredDimension(measureWidth,measureHeight);
        }else if(widthSpeceMode==MeasureSpec.AT_MOST){
            final View childView=getChildAt(0);
            measureHeight=childView.getMeasuredHeight();
            setMeasuredDimension(widthSpaceSize,measureHeight);
        }else if(heightSpaceMode==MeasureSpec.AT_MOST){
            final View childView=getChildAt(0);
            measureWidth=childView.getMeasuredWidth()*childCount;
            setMeasuredDimension(measureWidth,heightSpaceSize);
        }
    }
```
这里说明一下下上述代码的逻辑，首先会判断是否有子元素，如果没有子元素，就通过`setMeasuredDimension`设置自身的宽和高为0，然后就是判断宽和高是不是采用了wrap_content，如果宽采用了wrap_content，那么就是所有子元素宽度之和，高度也一样
这里不规范的地方相比大家也可以看出来：第一个是没有子元素的时候不应该把宽和高直接设置成0，而是应当根据LayoutParams中的宽和高来做相对应的处理；第二个是在测量HorizontalScrollViewEx的宽和高时，没有考虑到他的padding以及子元素的margin，这样会影响HorizontalScrollViewEx的宽和高。这个很好理解，因为不管是子元素的margin还是自己的padding,都会影响HorizontalScrollViewEx所占用的空间。
在看一下onLayout方法

```
@Override
 protected void onLayout(boolean changed, int l, int t, int r, int b) {
     int childLeft=0;
     final int childCound=getChildCount();
     mChildSize=childCound;
     for (int i = 0; i < childCound; i++) {
         final View childView=getChildAt(i);
         if(childView.getVisibility()!=View.GONE){
             final int childWidth=childView.getMeasuredWidth();
             mChildWidth=childWidth;
             //不断向右边移动，x不断加大
             childView.layout(childLeft,0,childLeft+childWidth,childView.getMeasuredHeight());
             childLeft+=childWidth;
         }
     }
 }

```
上面的代码逻辑并不是非常复杂，作用是完成对子View的定位。首先会遍历所有的子元素，如果这个子元素不是处于Gone状态，就通过layout方法将其放在合适的位置。从上面代码来看是从左向右的，这个和水平方向的LinearLayout比较相似，然而代码依旧不完善。没有考虑到自身的padding以及子元素的margin，而从一个规范的控件的角度来讲，这些都应该是要考虑的。

这里给出HorizontalScrollViewEx的源码如下


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


继承特定的View比如TextView和继承特定ViewGroup这个比较简单，这里就不说了，可以参考android群英传

这里在写一下StickyLayout的源码 https://github.com/singwhatiwanna/PinnedHeaderExpandableListView/tree/master/PinnedHeaderExpandableListView 这个是作者的地址
```
public class StickyLayout extends LinearLayout {
    private static final String TAG = "StickyLayout";
    private static final boolean DEBUG = true;

    public interface OnGiveUpTouchEventListener {
        public boolean giveUpTouchEvent(MotionEvent event);
    }

    private View mHeader;
    private View mContent;
    private OnGiveUpTouchEventListener mGiveUpTouchEventListener;

    // header的高度  单位：px
    private int mOriginalHeaderHeight;
    private int mHeaderHeight;

    private int mStatus = STATUS_EXPANDED;
    public static final int STATUS_EXPANDED = 1;
    public static final int STATUS_COLLAPSED = 2;

    private int mTouchSlop;

    // 分别记录上次滑动的坐标
    private int mLastX = 0;
    private int mLastY = 0;

    // 分别记录上次滑动的坐标(onInterceptTouchEvent)
    private int mLastXIntercept = 0;
    private int mLastYIntercept = 0;

    // 用来控制滑动角度，仅当角度a满足如下条件才进行滑动：tan a = deltaX / deltaY > 2
    private static final int TAN = 2;

    private boolean mIsSticky = true;
    private boolean mInitDataSucceed = false;
    private boolean mDisallowInterceptTouchEventOnHeader = true;

    public StickyLayout(Context context) {
        super(context);
    }

    public StickyLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public StickyLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (hasWindowFocus && (mHeader == null || mContent == null)) {
            initData();
        }
    }

    private void initData() {
        int headerId= getResources().getIdentifier("sticky_header", "id", getContext().getPackageName());
        int contentId = getResources().getIdentifier("sticky_content", "id", getContext().getPackageName());
        if (headerId != 0 && contentId != 0) {
            mHeader = findViewById(headerId);
            mContent = findViewById(contentId);
            mOriginalHeaderHeight = mHeader.getMeasuredHeight();
            mHeaderHeight = mOriginalHeaderHeight;
            mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
            if (mHeaderHeight > 0) {
                mInitDataSucceed = true;
            }
            if (DEBUG) {
                Log.d(TAG, "mTouchSlop = " + mTouchSlop + "mHeaderHeight = " + mHeaderHeight);
            }
        } else {
            throw new NoSuchElementException("Did your view with id \"sticky_header\" or \"sticky_content\" exists?");
        }
    }

    public void setOnGiveUpTouchEventListener(OnGiveUpTouchEventListener l) {
        mGiveUpTouchEventListener = l;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        int intercepted = 0;
        int x = (int) event.getX();
        int y = (int) event.getY();

        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN: {
            mLastXIntercept = x;
            mLastYIntercept = y;
            mLastX = x;
            mLastY = y;
            intercepted = 0;
            break;
        }
        case MotionEvent.ACTION_MOVE: {
            int deltaX = x - mLastXIntercept;
            int deltaY = y - mLastYIntercept;
            if (mDisallowInterceptTouchEventOnHeader && y <= getHeaderHeight()) {
                intercepted = 0;
            } else if (Math.abs(deltaY) <= Math.abs(deltaX)) {
                intercepted = 0;
            } else if (mStatus == STATUS_EXPANDED && deltaY <= -mTouchSlop) {
                intercepted = 1;
            } else if (mGiveUpTouchEventListener != null) {
                if (mGiveUpTouchEventListener.giveUpTouchEvent(event) && deltaY >= mTouchSlop) {
                    intercepted = 1;
                }
            }
            break;
        }
        case MotionEvent.ACTION_UP: {
            intercepted = 0;
            mLastXIntercept = mLastYIntercept = 0;
            break;
        }
        default:
            break;
        }

        if (DEBUG) {
            Log.d(TAG, "intercepted=" + intercepted);
        }
        return intercepted != 0 && mIsSticky;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!mIsSticky) {
            return true;
        }
        int x = (int) event.getX();
        int y = (int) event.getY();
        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN: {
            break;
        }
        case MotionEvent.ACTION_MOVE: {
            int deltaX = x - mLastX;
            int deltaY = y - mLastY;
            if (DEBUG) {
                Log.d(TAG, "mHeaderHeight=" + mHeaderHeight + "  deltaY=" + deltaY + "  mlastY=" + mLastY);
            }
            mHeaderHeight += deltaY;
            setHeaderHeight(mHeaderHeight);
            break;
        }
        case MotionEvent.ACTION_UP: {
            // 这里做了下判断，当松开手的时候，会自动向两边滑动，具体向哪边滑，要看当前所处的位置
            int destHeight = 0;
            if (mHeaderHeight <= mOriginalHeaderHeight * 0.5) {
                destHeight = 0;
                mStatus = STATUS_COLLAPSED;
            } else {
                destHeight = mOriginalHeaderHeight;
                mStatus = STATUS_EXPANDED;
            }
            // 慢慢滑向终点
            this.smoothSetHeaderHeight(mHeaderHeight, destHeight, 500);
            break;
        }
        default:
            break;
        }
        mLastX = x;
        mLastY = y;
        return true;
    }

    public void smoothSetHeaderHeight(final int from, final int to, long duration) {
        smoothSetHeaderHeight(from, to, duration, false);
    }

    public void smoothSetHeaderHeight(final int from, final int to, long duration, final boolean modifyOriginalHeaderHeight) {
        final int frameCount = (int) (duration / 1000f * 30) + 1;
        final float partation = (to - from) / (float) frameCount;
        new Thread("Thread#smoothSetHeaderHeight") {

            @Override
            public void run() {
                for (int i = 0; i < frameCount; i++) {
                    final int height;
                    if (i == frameCount - 1) {
                        height = to;
                    } else {
                        height = (int) (from + partation * i);
                    }
                    post(new Runnable() {
                        public void run() {
                            setHeaderHeight(height);
                        }
                    });
                    try {
                        sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                if (modifyOriginalHeaderHeight) {
                    setOriginalHeaderHeight(to);
                }
            };

        }.start();
    }

    public void setOriginalHeaderHeight(int originalHeaderHeight) {
        mOriginalHeaderHeight = originalHeaderHeight;
    }

    public void setHeaderHeight(int height, boolean modifyOriginalHeaderHeight) {
        if (modifyOriginalHeaderHeight) {
            setOriginalHeaderHeight(height);
        }
        setHeaderHeight(height);
    }

    public void setHeaderHeight(int height) {
        if (!mInitDataSucceed) {
            initData();
        }

        if (DEBUG) {
            Log.d(TAG, "setHeaderHeight height=" + height);
        }
        if (height <= 0) {
            height = 0;
        } else if (height > mOriginalHeaderHeight) {
            height = mOriginalHeaderHeight;
        }

        if (height == 0) {
            mStatus = STATUS_COLLAPSED;
        } else {
            mStatus = STATUS_EXPANDED;
        }

        if (mHeader != null && mHeader.getLayoutParams() != null) {
            mHeader.getLayoutParams().height = height;
            mHeader.requestLayout();
            mHeaderHeight = height;
        } else {
            if (DEBUG) {
                Log.e(TAG, "null LayoutParams when setHeaderHeight");
            }
        }
    }

    public int getHeaderHeight() {
        return mHeaderHeight;
    }

    public void setSticky(boolean isSticky) {
        mIsSticky = isSticky;
    }

    public void requestDisallowInterceptTouchEventOnHeader(boolean disallowIntercept) {
        mDisallowInterceptTouchEventOnHeader = disallowIntercept;
    }

}
```



![Alt text](滑动分析.gif "具体效果")

# 第五章 理解RemoteViews
本章讲述的主题是remoteView。可以从名字可以看出是一种远程View。那么什么是远程View呢?主要有两种一个是通知栏，一个是桌面小插件

## RemoteViews的应用
RemoteView在实际开发过程中主要用于通知栏和桌面小组件的开发。通知栏大家都不陌生，主要是通过使用NotificationManager来实现。除了默认布局，还可以自定义布局。桌面小部件则通过AppWidgetProvide来实现。AppWidgetProvide本质上是一个广播。在开发RemoteView的过程中会用到RemoteViews。他们更新界面不能像在Activity中那样更新，因为二者的界面都运行在其他进程中，准确的说是SystemServer中，为了更新UI，这里提供了一系列更新的方法。所以这里会简单介绍一下用法，重点是讲解一下RemoteView的内在机制

### RemoteView在通知栏的应用

首先看一下RemoteView在通知栏的使用。我们知道，通知栏除了默认效果以外，还可以支持自定义布局。下面分别说一下这两种情况。使用系统默认的样式弹出一个通知是很简单的，代码如下：
```
E/NotificationService: No Channel found for pkg=com.smart.kaifa, channelId=100, id=1000000, tag=null, opPkg=com.smart.kaifa, callingUid=10190, userId=0, incomingUserId=0, notificationUid=10190, notification=Notification(channel=100 pri=0 contentView=null vibrate=null sound=null defaults=0x0 flags=0x0 color=0x00000000 vis=PRIVATE)
```

在使用的时候遇到了这个问题
```
//获取NotificationManager实例
   NotificationManager notifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
   Intent intent = new Intent(this, SecondActivity.class);

   PendingIntent pendingIntent = PendingIntent.getActivity(this, 9, intent, PendingIntent.FLAG_CANCEL_CURRENT);
   //实例化NotificationCompat.Builde并设置相关属性
   NotificationCompat.Builder builder = new NotificationCompat.Builder(this, 100 + "")
           //设置小图标
           .setSmallIcon(R.mipmap.ic_launcher)
           //设置通知标题
           .setContentTitle("最简单的Notification")
           .setOngoing(true)
           //设置通知内容
           .setContentText("只有小图标、标题、内容").setContentIntent(pendingIntent)
           //设置通知时间，默认为系统发出通知的时间，通常不用设置
           .setWhen(System.currentTimeMillis());
   //通过builder.build()方法生成Notification对象,并发送通知,id=1
   notifyManager.notify(1000000, builder.build());
```
这个是因为在android 8.0及以上改变了通知，要设置channelId。
https://stackoverflow.com/questions/46990995/on-android-8-1-api-27-notification-does-not-display
```
private NotificationManager notifManager;

    public void createNotification(String aMessage) {
        final int NOTIFY_ID = 1002;

        // There are hardcoding only for show it's just strings
        String name = "my_package_channel";
        String id = "my_package_channel_1"; // The user-visible name of the channel.
        String description = "my_package_first_channel"; // The user-visible description of the channel.

        Intent intent;
        PendingIntent pendingIntent;
        NotificationCompat.Builder builder;

        if (notifManager == null) {
            notifManager =
                    (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = notifManager.getNotificationChannel(id);
            if (mChannel == null) {
                mChannel = new NotificationChannel(id, name, importance);
                mChannel.setDescription(description);
                mChannel.enableVibration(true);
                mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                notifManager.createNotificationChannel(mChannel);
            }
            builder = new NotificationCompat.Builder(this, id);

            intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

            builder.setContentTitle(aMessage)  // required
                    .setSmallIcon(android.R.drawable.ic_popup_reminder) // required
                    .setContentText(this.getString(R.string.app_name))  // required
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setTicker(aMessage)
                    .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
        } else {

            builder = new NotificationCompat.Builder(this);

            intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

            builder.setContentTitle(aMessage)                           // required
                    .setSmallIcon(android.R.drawable.ic_popup_reminder) // required
                    .setContentText(this.getString(R.string.app_name))  // required
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setTicker(aMessage)
                    .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400})
                    .setPriority(Notification.PRIORITY_HIGH);
        } // else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

        Notification notification = builder.build();
        notifManager.notify(NOTIFY_ID, notification);
    }

```
这样就可以使用penddingIntent打开SecondActivity了。


![Alt text](device-2018-05-01-093823.png "Android 原生8.0弹出的默认样式的通知")




```

    private NotificationManager notifManager;

    public void createNotification(String aMessage) {
        final int NOTIFY_ID = 1002;

        // There are hardcoding only for show it's just strings
        String name = "my_package_channel";
        String id = "my_package_channel_1"; // The user-visible name of the channel.
        String description = "my_package_first_channel"; // The user-visible description of the channel.

        Intent intent;
        PendingIntent pendingIntent;
        NotificationCompat.Builder builder;

        if (notifManager == null) {
            notifManager =
                    (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = notifManager.getNotificationChannel(id);
            if (mChannel == null) {
                mChannel = new NotificationChannel(id, name, importance);
                mChannel.setDescription(description);
                mChannel.enableVibration(true);
                mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                notifManager.createNotificationChannel(mChannel);
            }
            builder = new NotificationCompat.Builder(this, id);

            intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
            RemoteViews remoteViews=new RemoteViews(getPackageName(),R.layout.notification_compat_remote);

            remoteViews.setTextViewText(R.id.tv_first,"帅气");
            remoteViews.setTextViewText(R.id.tv_second,"英俊");
            remoteViews.setOnClickPendingIntent(R.id.tv_second,pendingIntent);
            builder.setContentTitle(aMessage)  // required
                    .setSmallIcon(android.R.drawable.ic_popup_reminder) // required
                    .setContentText(this.getString(R.string.app_name))  // required
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setTicker(aMessage).setContent(remoteViews)
                    .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
        } else {

            builder = new NotificationCompat.Builder(this);

            intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

            builder.setContentTitle(aMessage)                           // required
                    .setSmallIcon(android.R.drawable.ic_popup_reminder) // required
                    .setContentText(this.getString(R.string.app_name))  // required
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setTicker(aMessage)
                    .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400})
                    .setPriority(Notification.PRIORITY_HIGH);
        } // else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

        Notification notification = builder.build();
        notifManager.notify(NOTIFY_ID, notification);
    }

```

```
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent">
    <TextView
        android:id="@+id/tv_first"
        android:layout_width="0dp"
        android:layout_height="match_parent"
        android:layout_weight="1"
        android:background="#00ff00" />
    <TextView
        android:id="@+id/tv_second"
        android:layout_width="0dp"
        android:layout_height="match_parent"
        android:layout_weight="1"
        android:background="#ff00ff" />
</LinearLayout>
```
![Alt text](device-2018-05-01-095037.png "一个自定义的通知")
虽然不好看，但是效果有了就可以
RemoteViews使用起来是很简单的，只要提供当前应用的包名和布局文件资源的id就可以创建一个RemoteViews对象了。这里由于RemoteViews是显示在其他进程中的，所以只能使用它提供的set方法修改布局的内容。来看一下上面的`setTextViewText`这个方法。设置imageView也是类似的。

### RemoteViews在桌面小部件的应用
AppWidgetProvide是Android提供的用于实现桌面小部件的类。其本质是一个广播：broadcaskReceiver。可以看一下他的继承关系：在AndroidStudio中双击Shift ->AppWidgetProvider ->然后 ctrl+h 就可以看到他的继承关系了.
```
public class AppWidgetProvider extends BroadcastReceiver {}
```
下面来写一个小Demo来测试一下


1. 定义小部件界面

在res/layout 下新建一个XML文件，命名为widget.xml，名称和内容可以自定义。看这个小部件的样子

```
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout
    android:orientation="vertical"
    xmlns:android="http://schemas.android.com/apk/res/android" android:layout_width="match_parent"
    android:layout_height="match_parent">

    <ImageView
        android:id="@+id/imageView1"
        android:layout_width="wrap_content"
        android:src="@mipmap/ic_launcher"
        android:layout_height="wrap_content"
        />
</LinearLayout>
```

然后在res/xml下面新建一个XML文件`appwidget_provide_info`
```
<?xml version="1.0" encoding="utf-8"?>
<appwidget-provider xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:initialKeyguardLayout="@layout/widget"
    android:minHeight="84dp"
    android:minWidth="84dp"
    android:orientation="vertical"
    //这个是自动更新周期
    android:updatePeriodMillis="864000000">

</appwidget-provider>
```

这里发现一个问题在android8.0上由于取消了隐式广播，所以如果要使用隐式广播会报错
```
BroadcastQueue: Background execution not allowed: receiving Intent
```
解决方案使用明确的广播

这里还有一个问题就是` bitmap = BitmapFactory.decodeResource(context.getResources(), vectorDrawableId);`会在5.0以上返回null,这里可以看一下如何处理

```
private static Bitmap getBitmap(Context context,int vectorDrawableId) {
    Bitmap bitmap=null;
    if (Build.VERSION.SDK_INT> Build.VERSION_CODES.LOLLIPOP){
        Drawable vectorDrawable = context.getDrawable(vectorDrawableId);
        bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        vectorDrawable.draw(canvas);
    }else {
        bitmap = BitmapFactory.decodeResource(context.getResources(), vectorDrawableId);
    }
    return bitmap;
}
```
```
public class AppEidgetProvider extends AppWidgetProvider {

    public static final String TAG = "AppEidgetProvider";
    public static final String CLICK_ACTION = "com.smart.kaifa111";

    /**
     * 这个是广播的内置方法，具体操作交给其他的东西
     *
     * @param context
     * @param intent
     */
    @Override
    public void onReceive(final Context context, Intent intent) {
        super.onReceive(context, intent);
        Log.i("输出", "action: " + CLICK_ACTION);
        Log.i("输出", "action: ---------------------" );
        Log.i("输出", "action: " + intent.getAction());

        if (intent.getAction().equals(CLICK_ACTION)) {
            Toast.makeText(context, "click it", Toast.LENGTH_SHORT).show();
            new Thread(new Runnable() {
                @Override
                public void run() {


                    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                    for (int i = 0; i < 37; i++) {
                        float degree = (i * 10) % 360;
                        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);
                        remoteViews.setImageViewBitmap(R.id.imageView1, remoteBitmap(context, getBitmap(context,R.mipmap.ic_launcher), degree));
                        // 声明明确的广播  vs     Intent intentClick = new Intent();
                        Intent intentClick = new Intent(context,AppEidgetProvider.class);
                        intentClick.setAction(CLICK_ACTION);
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intentClick, 0);
                        remoteViews.setOnClickPendingIntent(R.id.imageView1, pendingIntent);
                        appWidgetManager.updateAppWidget(new ComponentName(context, AppEidgetProvider.class), remoteViews);
                        SystemClock.sleep(30);

                    }
                }
            }).start();
        }
    }



    private static Bitmap getBitmap(Context context,int vectorDrawableId) {
        Bitmap bitmap=null;
        if (Build.VERSION.SDK_INT> Build.VERSION_CODES.LOLLIPOP){
            Drawable vectorDrawable = context.getDrawable(vectorDrawableId);
            bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                    vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            vectorDrawable.draw(canvas);
        }else {
            bitmap = BitmapFactory.decodeResource(context.getResources(), vectorDrawableId);
        }
        return bitmap;
    }
    /**
     * 每删除一次窗口小部件，就调用一次
     *
     * @param context
     * @param appWidgetIds
     */
    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        Log.i("onDeleted", "action: " + CLICK_ACTION);
    }

    /**
     * 小部件第一次创建时调用
     *
     * @param context
     */
    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        Log.i("onEnabled", "action: " + CLICK_ACTION);
    }

    /**
     * 当最后一个窗口小部件被删除时，调用此方法
     *
     * @param context
     */
    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        Log.i("onDeleted", "action: " + CLICK_ACTION);

        Log.i("onDeleted", "action: " + CLICK_ACTION);
    }

    /**
     * 每次桌面小部件更新时都调用一次该方法
     *
     * @param context
     * @param appWidgetManager
     * @param appWidgetIds
     */
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        Log.i("更新", "action: " + CLICK_ACTION);
        final int counter = appWidgetIds.length;
        for (int i = 0; i < counter; i++) {
            int appWidgetId = appWidgetIds[i];
            onWidgetUpdate(context, appWidgetManager, appWidgetId);
        }
    }


    private void onWidgetUpdate(Context context, AppWidgetManager manager, int appWidgetId) {

        Log.i(TAG, "appWidgetId =" + appWidgetId);
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);
        Intent intentClick = new Intent(context,AppEidgetProvider.class);
        intentClick.setAction(CLICK_ACTION);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intentClick, 0);
        remoteViews.setOnClickPendingIntent(R.id.imageView1, pendingIntent);
        manager.updateAppWidget(appWidgetId, remoteViews);
    }

    private Bitmap remoteBitmap(Context context, Bitmap srcbBitmap, float degree) {

        Matrix matrix = new Matrix();
        matrix.reset();
        matrix.setRotate(degree);
        Bitmap tempBitmap = Bitmap.createBitmap(srcbBitmap, 0, 0, srcbBitmap.getWidth(), srcbBitmap.getHeight(), matrix, true);
        return tempBitmap;

    }
}

```

清单文件配置
```
<receiver android:name=".AppEidgetProvider">
           <intent-filter>
               <action android:name="com.smart.kaifa111" />
               <action android:name="android.appwidget.action.APPWIDGET_UPDATE"/>
               <action android:name="android.appwidget.action.APPWIDGET_UPDATE_OPTIONS"/>
               <action android:name="android.appwidget.action.APPWIDGET_RESTORED"/>
               <action android:name="android.appwidget.action.APPWIDGET_DELETED"/>
           </intent-filter>

           <meta-data
               android:name="android.appwidget.provider"
               android:resource="@xml/appwidget_provide_info" />
       </receiver>
```

appwidget_provide_info.xml 在res/xml 下面
```
<?xml version="1.0" encoding="utf-8"?>
<appwidget-provider xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:initialKeyguardLayout="@layout/widget"
    android:minHeight="84dp"
    android:minWidth="84dp"
    android:orientation="vertical"
    android:updatePeriodMillis="864000000">

</appwidget-provider>
```
widget.xml  在res/layout下面
```
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout
    android:orientation="vertical"
    xmlns:android="http://schemas.android.com/apk/res/android" android:layout_width="match_parent"
    android:layout_height="match_parent">

    <ImageView
        android:id="@+id/imageView1"
        android:background="#ff0000"
        android:layout_width="match_parent"
        android:src="@mipmap/ic_launcher"
        android:layout_height="match_parent"
        />
</LinearLayout>
```

这里来看一下AppWidgetProvider的`onReceive`的源码和具体的分发过程

```
public void onReceive(Context context, Intent intent) {
     // Protect against rogue update broadcasts (not really a security issue,
     // just filter bad broacasts out so subclasses are less likely to crash).
     String action = intent.getAction();
     //判断不同的action，进行条件删选，并且调用方法
     if (AppWidgetManager.ACTION_APPWIDGET_UPDATE.equals(action)) {
         Bundle extras = intent.getExtras();
         if (extras != null) {
             int[] appWidgetIds = extras.getIntArray(AppWidgetManager.EXTRA_APPWIDGET_IDS);
             if (appWidgetIds != null && appWidgetIds.length > 0) {
                 this.onUpdate(context, AppWidgetManager.getInstance(context), appWidgetIds);
             }
         }
     } else if (AppWidgetManager.ACTION_APPWIDGET_DELETED.equals(action)) {
         Bundle extras = intent.getExtras();
         if (extras != null && extras.containsKey(AppWidgetManager.EXTRA_APPWIDGET_ID)) {
             final int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);
             this.onDeleted(context, new int[] { appWidgetId });
         }
     } else if (AppWidgetManager.ACTION_APPWIDGET_OPTIONS_CHANGED.equals(action)) {
         Bundle extras = intent.getExtras();
         if (extras != null && extras.containsKey(AppWidgetManager.EXTRA_APPWIDGET_ID)
                 && extras.containsKey(AppWidgetManager.EXTRA_APPWIDGET_OPTIONS)) {
             int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);
             Bundle widgetExtras = extras.getBundle(AppWidgetManager.EXTRA_APPWIDGET_OPTIONS);
             this.onAppWidgetOptionsChanged(context, AppWidgetManager.getInstance(context),
                     appWidgetId, widgetExtras);
         }
     } else if (AppWidgetManager.ACTION_APPWIDGET_ENABLED.equals(action)) {
         this.onEnabled(context);
     } else if (AppWidgetManager.ACTION_APPWIDGET_DISABLED.equals(action)) {
         this.onDisabled(context);
     } else if (AppWidgetManager.ACTION_APPWIDGET_RESTORED.equals(action)) {
         Bundle extras = intent.getExtras();
         if (extras != null) {
             int[] oldIds = extras.getIntArray(AppWidgetManager.EXTRA_APPWIDGET_OLD_IDS);
             int[] newIds = extras.getIntArray(AppWidgetManager.EXTRA_APPWIDGET_IDS);
             if (oldIds != null && oldIds.length > 0) {
                 this.onRestored(context, oldIds, newIds);
                 this.onUpdate(context, AppWidgetManager.getInstance(context), newIds);
             }
         }
     }
 }
```
还是很简单的。
### PendingIntent的概述
前面多次提及pendingIntent，那么他是一个什么东西呢？他和Intent的区别是什么呢？
1. 先看一下他们的继承关系

```
public final class PendingIntent implements Parcelable {}
public class Intent implements Parcelable, Cloneable {}
```
可以看到都是继承自Parcelable这个类。而这个类我们很熟悉，是android用于信息传递的或者说信息序列化的,Parcelable的中文意思是打包... 还是要学好英语啊
2. PendingIntent 顾名思义，他是一种处于pending状态的intent，就是将要发生的意图。可以拆开有道一下。从这里可以看到PendingIntent是一种在将来某一个时刻发生的Intent，而Intent是立即发生
3. PendingIntent典型的使用场景是给RemoteViews添加点击事件，因为RemoteViews是运行在远程进程中的，所以无法通过setOnClickListener来设置点击事件。要设置RemoteViews中的点击事件，就必须使用PendingIntent，PendingIntent通过send和cancel方法来发送和取消特定的待定的Intent。
PendingIntent支持三种特定意图：启动Activity、启动Service和发送广播


|方法|作用|
|:--:|:--:|
|getActivity(Context context,int requestCode,int flags)|获取一个PendingIntent，该特定意图发生时相当于startActivity|
|getService(Context context,int requestCode,int flags)|获取一个PendingIntent，该特定意图发生时相当于startService|
|getBroadCast(Context context,int requestCode,int flags)|获取一个PendingIntent，该特定意图发生时相当于sendBroadCask|

上面第一个参数比较好理解，主要说一下第二个参数和第三个参数。
requestCode表示PendingIntent的请求码，多数情况下设定为0就可以了，另外requestCode会影响到flags的效果。flags常见的类型有FLAG_ONE_SHOT,FLAG_NO_CREATE,FLAG_CANCEL_CURRENT和FLAG_UPDATE_CURRENT。这里需要明白一个概念，这个就是Pending的匹配规则。即在什么情况下两个pendingIntent是相同的。
PendingIntent的匹配规则是：如果两个PendingIntent他们内部的Intent相同，并且requestCode也相同，那么这两个PendingIntent就相同。resultCode这个比较好理解，那么如何确定两个Intent是相同的呢。Intent的匹配规则就是ComponentName和intent-filter都相同。只要ComponentName和intent-filter是相同的，即时他的Extras不同，那么这两个Intent也是相同的。
不同flags的含义
* FLAG_ONE_SHOT
当前描述的PendingIntent只能被使用一次，然后他就会被自动cancel。如果后续还有相同的PendingIntent，那么他们的send方法就会调用失败。对于通知栏消息来说，如果采用此标记位。那么同类的通知只能使用一次，后续的通知单击后将无法打开。
* FLAG_NO_CREATE
当前描述的Pending不会主动创建，如果当前的PendingIntent之前不存在，那么getActivity，getService，getBroadCast方法会直接返回null，即获取PendingIntent失败。这个标记位很少见，他无法单独使用，在开发中没有太多的意义
* FLAG_CANCEL_CURRENT
当前描述的PendingIntent如果已经存在，那么他们都会被cancel，然后系统会创建一个新的PendingIntent。对于通知栏消息来说，那些cancel的消息单击后无法打开
* FLAG_UPDATE_CURRENT
当前描述的PendingIntent如果已经存在，那么他们都会被更新，即他们的Intent中的Extras会被替换成最新
从上面的分析来讲，还是不是非常好理解这四个标记位
这里在结合通知栏消息在描述一遍。
`Manager.notify(1,nootification)`如果notifi的第一个参数id是常量，那么多次调用notify只能弹出一个通知，后续的通知会把前面的通知完全替代掉，而如果每一次的id都不同，那么多次调用notify会弹出多个通知。如果notify方法中的id是常量，那么不管pendingIntent是否匹配，后面的通知会直接替换前面的通知。
如果notify方法的id每一次都不一样，那么当pendingIntent不匹配时，这里的匹配规则指的是PendingIntent中的Intent相同并且requestCode相同，这种情况下不管采用什么标记位，这些通知都不会互相干扰。如果pendingIntent处于匹配状态时，这个时候要分情况讨论，如果采用FLAG_ONE_SHOT标记位，那么后续的通知中的PendingIntent会和第一条通知保持一致，如果其中包含Extras，单击任何一条通知后，剩余的通知将无法打开，当所有的通知被清除后，又会重复这个过程。如果采用FLAG_CANCEL_CURRENT标记位，那么只有最新的通知可以打开，之前弹出的通知都无法打开；如果使用FLAG_UPDATE_CURRENT，那么之前通知中用的PendingIntent会被更新和最新的一条保持一致，包括其中的Extras，并且这些通知都是可以打开的

## RemoteViews的内部机制
RemoteViews的作用是在其他进程中显示并更新View的显示。为了更好的理解他的内部机制，我们来看一下他的主要功能。首先看一下构造方法，这里介绍一种最常用的构造方法`new RemoteViews(context.getPackageName(), R.layout.widget);`。它接受两个值，一个是应用包名，一个是待加载的布局文件。这里很好理解。
RemoteViews不支持所有的View类型
目前支持的类型如下
* Layout
FramLayout,LinearLayout,RelativeLayout,CridLayout
* View
AnalogClock,Button,CHronometer,ImageButton,ImageView,ProgressBar,TestView,ViewFlipper，ListView，GridView，StackView，AdapterViewFlipper，ViewStub
上面描述的是RemoteViews所支持的所有View的类型，RemoteViews不支持他们的子类以及其他View类型。也就是说RemoteViews中不能使用除了上述列表意外的View也无法使用自定义View。比如我们在通知栏的RemoteViews中使用系统的EditText，那么通知栏消息将无法弹出并会报错。
```
? W/AppWidgetHostView: updateAppWidget couldn't find any view, using error view
    android.view.InflateException: Binary XML file line #14: Binary XML file line #14: Error inflating class android.widget.EditText
    Caused by: android.view.InflateException: Binary XML file line #14: Error inflating class android.widget.EditText
    Caused by: android.view.InflateException: Binary XML file line #14: Class not allowed to be inflated android.widget.EditText
        at android.view.LayoutInflater.failNotAllowed(LayoutInflater.java:686)
        at android.view.LayoutInflater.createView(LayoutInflater.java:612)
        at com.android.internal.policy.PhoneLayoutInflater.onCreateView(PhoneLayoutInflater.java:58)
        at android.view.LayoutInflater.onCreateView(LayoutInflater.java:720)
        at android.view.LayoutInflater.createViewFromTag(LayoutInflater.java:788)
        at android.view.LayoutInflater.createViewFromTag(LayoutInflater.java:730)
        at android.view.LayoutInflater.rInflate(LayoutInflater.java:863)
        at android.view.LayoutInflater.rInflateChildren(LayoutInflater.java:824)
        at android.view.LayoutInflater.inflate(LayoutInflater.java:515)
        at android.view.LayoutInflater.inflate(LayoutInflater.java:423)
        at android.widget.RemoteViews.inflateView(RemoteViews.java:3278)
        at android.widget.RemoteViews.apply(RemoteViews.java:3255)
        at android.appwidget.AppWidgetHostView.applyRemoteViews(AppWidgetHostView.java:451)
        at android.appwidget.AppWidgetHostView.updateAppWidget(AppWidgetHostView.java:380)
        at com.android.launcher3.LauncherAppWidgetHostView.updateAppWidget(SourceFile:19)
        at android.appwidget.AppWidgetHost.updateAppWidgetView(AppWidgetHost.java:404)
        at android.appwidget.AppWidgetHost.startListening(AppWidgetHost.java:202)
        at com.android.launcher3.LauncherAppWidgetHost.startListening(SourceFile:9)
        at com.android.launcher3.Launcher.onStart(SourceFile:577)
        at android.app.Instrumentation.callActivityOnStart(Instrumentation.java:1333)
        at android.app.Activity.performStart(Activity.java:6992)
        at android.app.Activity.performRestart(Activity.java:7066)
        at android.app.Activity.performResume(Activity.java:7071)
        at android.app.ActivityThread.performResumeActivity(ActivityThread.java:3620)
        at android.app.ActivityThread.handleResumeActivity(ActivityThread.java:3685)
        at android.app.ActivityThread$H.handleMessage(ActivityThread.java:1643)
        at android.os.Handler.dispatchMessage(Handler.java:105)
        at android.os.Looper.loop(Looper.java:164)
        at android.app.ActivityThread.main(ActivityThread.java:6541)
        at java.lang.reflect.Method.invoke(Native Method)
        at com.android.internal.os.Zygote$MethodAndArgsCaller.run(Zygote.java:240)
        at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:767)
```

意思是无法找到这个控件，无法inflate。然后就无法显示控件


RemoteViews没有提供findViewById的方法，因此我们无法直接访问里面的View的数据。而必须通过RemoteViews所提供的一系列set方法来完成。当然这个是因为RemoteViews在其他进程中，所以无法直接findViewById。这里列举一些常用的方法

|方法名|作用|
|:--:|:--:|
|setTextViewText(int viewId, CharSequence text) |设置文字内容|
|setTextViewTextSize(int viewId, int units, float size)|设置文字大小|
|setViewVisibility(int viewId, int visibility)|设置是否可见|
|setImageViewBitmap(int viewId, Bitmap bitmap)|设置图片|
|setEmptyView(int viewId, int emptyViewId)|设置空图片|

从表可以看出，原本可以直接调用View的方法，但是现在必须通过RemoteViews的set方法才能完成，而且冲方法声明上来看，很像是通过反射来完成的
下面来描述一下RemoteViews的内部机制，由于RemoteViews主要用于通知栏和桌面小部件中，这里就通过他们来分析一下RemoteViews的工作流程。我们知道，通知栏和桌面小插件部分是分别由NotifacationManager和AppWidgetmanager来管理的，而NotificationManager和AppWidgetManager通过Binder分别和SystemServer进程中的NotificationMangaerService以及AppWidgetService进行通信。由此可见，通知栏和左面小部件中的布局是在NotificationManagerServcice和AppWidgetService中加载的，他们运行在系统的SystemServer中，这就和我们构成了跨进程通讯的场景。

首先RemoteViews会通过Binder传递到SystemServer进程，这是因为RemoteViews实现了Parcelable接口
```
public class RemoteViews implements Parcelable, Filter {...}
```
因此他可以跨进程传输，系统会根据RemoteViews中的包名的该信息去得到该应用的资源。然后会通过layoutInflater去加载RemoteViews中的布局文件。在SystemServer进程中加载后的布局文件是一个普通的View，只不过相对于我们的进程而言是一个RemoteViews，但是要记住，RemoteViews不是一个View，他是一个数据结构，实现了Parcelable接口，用来跨进程通讯的。接着系统会对View执行一系列更新界面的任务。这些人无语是我们通过set方法来提交的，但是set方法对于View的更新不是立即就做的，在RemoteViews内部会记录所有的更新操作，直到RemoteViews被加载以后才会执行，这样RemoteViews就可以在SYstemServer进程中显示了。这就是我们所看到了通知栏或者小部件中的内容。当我们需要跟新UI的时候，我们需要调用一系列的set方法，并通过NotificationManager和AppWidgetManager来提交跟新任务。具体的跟新操作也是在SystemServer进程中完成的。
从理论上来说。系统完全可以通过Binder去支持所有的View和View的操作，但是这样的代价太大了，因为View的方法太多了，另外就是大量的IPC操作会影响效率。为了解决这个问题，系统并没有通过BInder去直接支持View的跨进程通讯，而是提供了一个Action的概念，Action代表一个View的操作，Action同样实现了Parcelable接口。
```
private abstract static class Action implements Parcelable {}

```

系统首先将View对象中的具体操作分装到Action对象中，并将这些对象跨进程传输到远程进程。接着在远程进程中执行Action对象的具体操作。在我们的应用中每调用一次set方法，RemoteViews中就会添加一个对应的Action对象，当我们通过NotificationManager和AppWidgetManager，来提交我们的更新时，这些Action对象就会传输到远程进程中并在远程进程中依次执行。

![Alt text](图像1525593096.png "RemoteViews内部机制")

远程进程的RemoteViews的apply方法进行View的更新操作，具体的View更新操作是由Action对象内部的apply来完成的。上述方法的好处显而易见，首先不需要定义大量的Binder接口，其次通过在远程进程中批量执行RemoteViews的修改操作从而避免大量的IPC操作，这就提高了程序的性能，由此可见，Android系统在这方面的设计很巧妙。
上面从理论分析了RemoteViews的机制,现在从源码的角度来分析一下RemoteViews的，先看一下一系列的set方法
```
public void setImageViewBitmap(int viewId, Bitmap bitmap) {
     setBitmap(viewId, "setImageBitmap", bitmap);
 }

```

可以看到set方法最终调用了一个setBitmap方法
```
public void setBitmap(int viewId, String methodName, Bitmap value) {
    addAction(new BitmapReflectionAction(viewId, methodName, value));
}

```
可以看到，这里就出现了action
```
private void addAction(Action a) {
    if (hasLandscapeAndPortraitLayouts()) {
        throw new RuntimeException("RemoteViews specifying separate landscape and portrait" +
                " layouts cannot be modified. Instead, fully configure the landscape and" +
                " portrait layouts individually before constructing the combined layout.");
    }
    if (mActions == null) {
        mActions = new ArrayList<Action>();
    }
    mActions.add(a);

    // update the memory usage stats
    a.updateMemoryUsageEstimate(mMemoryUsageCounter);
}

```
从上面的代码可以看到，RemoteViews内部有一个Action集合，外界每调用一次set方法，RemoteViews就会为其创建一个Action对象并加入到这个集合中。需要注意的是这里仅仅是将action对象保存了起来，并未对View进行实际操作。这一点在上面的理论分析中已经提到过了。一个set方法的源码已经分析完毕了，但是我们还是不知道什么时候会将这个集合传递出去。这里要看一下RemoteViews的apply方法
```
public View apply(Context context, ViewGroup parent, OnClickHandler handler) {
        RemoteViews rvToApply = getRemoteViewsToApply(context);

        View result = inflateView(context, rvToApply, parent);
        loadTransitionOverride(context, handler);

        rvToApply.performApply(result, parent, handler);

        return result;
    }
```
从上面的代码可以看出，首先会通过LayoutLnflater去加载RemoteViews中的布局文件，RemoteViews中布局文件可以通过getLayoutId这个方法获得，加载完布局文件后会通过performApply去执行一些更新操作


```
private void performApply(View v, ViewGroup parent, OnClickHandler handler) {
      if (mActions != null) {
          handler = handler == null ? DEFAULT_ON_CLICK_HANDLER : handler;
          final int count = mActions.size();
          for (int i = 0; i < count; i++) {
              Action a = mActions.get(i);
              a.apply(v, parent, handler);
          }
      }
  }

```
可以看到他的作用是遍历action的集合并执行action对象。每一次的set操作对应一个action对象，所以说这里才是真正操作View的地方。

RemoteViews在通知栏和桌面小部件的工作过程和上面描述的过程是一致的，当我们调用RemoteViews的set方法时，并不会立即更新界面，而是必须要通过NotificationManager的notify方法以及AppWidgetManager的updateAppWidget才能更新他们的界面。实际上在AppWidgetManger的updateAppWidget的内部实现的，apply和reApply的区别在于：apply会加载布局并更新界面，reApply只会更新界面。通知栏和桌面小插件在初始化界面会调用apply方法，而在后续的更新界面则会调用reApply方法。这里看看一下AppWidgetHostView的updateAppWidget方法

```

public void updateAppWidget(RemoteViews remoteViews) {
    applyRemoteViews(remoteViews, true);
}

protected void applyRemoteViews(RemoteViews remoteViews, boolean useAsyncIfPossible) {
       if (LOGD) Log.d(TAG, "updateAppWidget called mOld=" + mOld);

       boolean recycled = false;
       View content = null;
       Exception exception = null;

       // Capture the old view into a bitmap so we can do the crossfade.
       if (CROSSFADE) {
           if (mFadeStartTime < 0) {
               if (mView != null) {
                   final int width = mView.getWidth();
                   final int height = mView.getHeight();
                   try {
                       mOld = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                   } catch (OutOfMemoryError e) {
                       // we just won't do the fade
                       mOld = null;
                   }
                   if (mOld != null) {
                       //mView.drawIntoBitmap(mOld);
                   }
               }
           }
       }

       if (mLastExecutionSignal != null) {
           mLastExecutionSignal.cancel();
           mLastExecutionSignal = null;
       }

       if (remoteViews == null) {
           if (mViewMode == VIEW_MODE_DEFAULT) {
               // We've already done this -- nothing to do.
               return;
           }
           content = getDefaultView();
           mLayoutId = -1;
           mViewMode = VIEW_MODE_DEFAULT;
       } else {
           if (mAsyncExecutor != null && useAsyncIfPossible) {
               inflateAsync(remoteViews);
               return;
           }
           // Prepare a local reference to the remote Context so we're ready to
           // inflate any requested LayoutParams.
           mRemoteContext = getRemoteContext();
           int layoutId = remoteViews.getLayoutId();

           // If our stale view has been prepared to match active, and the new
           // layout matches, try recycling it
           if (content == null && layoutId == mLayoutId) {
               try {
                   remoteViews.reapply(mContext, mView, mOnClickHandler);
                   content = mView;
                   recycled = true;
                   if (LOGD) Log.d(TAG, "was able to recycle existing layout");
               } catch (RuntimeException e) {
                   exception = e;
               }
           }

           // Try normal RemoteView inflation
           if (content == null) {
               try {
                 //在这里===========================
                   content = remoteViews.apply(mContext, this, mOnClickHandler);
                   if (LOGD) Log.d(TAG, "had to inflate new layout");
               } catch (RuntimeException e) {
                   exception = e;
               }
           }

           mLayoutId = layoutId;
           mViewMode = VIEW_MODE_CONTENT;
       }

       applyContent(content, recycled, exception);
       updateContentDescription(mInfo);
   }
```
可以看到有一行` content = remoteViews.apply(mContext, this, mOnClickHandler);`

现在了解了具体的做法，现在来看一下action他儿子
```
private final class ReflectionAction extends Action {
        static final int TAG = 2;

        static final int BOOLEAN = 1;
        static final int BYTE = 2;
        static final int SHORT = 3;
        static final int INT = 4;
        static final int LONG = 5;
        static final int FLOAT = 6;
        static final int DOUBLE = 7;
        static final int CHAR = 8;
        static final int STRING = 9;
        static final int CHAR_SEQUENCE = 10;
        static final int URI = 11;
        // BITMAP actions are never stored in the list of actions. They are only used locally
        // to implement BitmapReflectionAction, which eliminates duplicates using BitmapCache.
        static final int BITMAP = 12;
        static final int BUNDLE = 13;
        static final int INTENT = 14;
        static final int COLOR_STATE_LIST = 15;
        static final int ICON = 16;

        String methodName;
        int type;
        Object value;

        ReflectionAction(int viewId, String methodName, int type, Object value) {
            this.viewId = viewId;
            this.methodName = methodName;
            this.type = type;
            this.value = value;
        }

        ReflectionAction(Parcel in) {
            this.viewId = in.readInt();
            this.methodName = in.readString();
            this.type = in.readInt();
            //noinspection ConstantIfStatement
            if (false) {
                Log.d(LOG_TAG, "read viewId=0x" + Integer.toHexString(this.viewId)
                        + " methodName=" + this.methodName + " type=" + this.type);
            }

            // For some values that may have been null, we first check a flag to see if they were
            // written to the parcel.
            switch (this.type) {
                case BOOLEAN:
                    this.value = in.readInt() != 0;
                    break;
                case BYTE:
                    this.value = in.readByte();
                    break;
                case SHORT:
                    this.value = (short)in.readInt();
                    break;
                case INT:
                    this.value = in.readInt();
                    break;
                case LONG:
                    this.value = in.readLong();
                    break;
                case FLOAT:
                    this.value = in.readFloat();
                    break;
                case DOUBLE:
                    this.value = in.readDouble();
                    break;
                case CHAR:
                    this.value = (char)in.readInt();
                    break;
                case STRING:
                    this.value = in.readString();
                    break;
                case CHAR_SEQUENCE:
                    this.value = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
                    break;
                case URI:
                    if (in.readInt() != 0) {
                        this.value = Uri.CREATOR.createFromParcel(in);
                    }
                    break;
                case BITMAP:
                    if (in.readInt() != 0) {
                        this.value = Bitmap.CREATOR.createFromParcel(in);
                    }
                    break;
                case BUNDLE:
                    this.value = in.readBundle();
                    break;
                case INTENT:
                    if (in.readInt() != 0) {
                        this.value = Intent.CREATOR.createFromParcel(in);
                    }
                    break;
                case COLOR_STATE_LIST:
                    if (in.readInt() != 0) {
                        this.value = ColorStateList.CREATOR.createFromParcel(in);
                    }
                    break;
                case ICON:
                    if (in.readInt() != 0) {
                        this.value = Icon.CREATOR.createFromParcel(in);
                    }
                default:
                    break;
            }
        }

        public void writeToParcel(Parcel out, int flags) {
            out.writeInt(TAG);
            out.writeInt(this.viewId);
            out.writeString(this.methodName);
            out.writeInt(this.type);
            //noinspection ConstantIfStatement
            if (false) {
                Log.d(LOG_TAG, "write viewId=0x" + Integer.toHexString(this.viewId)
                        + " methodName=" + this.methodName + " type=" + this.type);
            }

            // For some values which are null, we record an integer flag to indicate whether
            // we have written a valid value to the parcel.
            switch (this.type) {
                case BOOLEAN:
                    out.writeInt((Boolean) this.value ? 1 : 0);
                    break;
                case BYTE:
                    out.writeByte((Byte) this.value);
                    break;
                case SHORT:
                    out.writeInt((Short) this.value);
                    break;
                case INT:
                    out.writeInt((Integer) this.value);
                    break;
                case LONG:
                    out.writeLong((Long) this.value);
                    break;
                case FLOAT:
                    out.writeFloat((Float) this.value);
                    break;
                case DOUBLE:
                    out.writeDouble((Double) this.value);
                    break;
                case CHAR:
                    out.writeInt((int)((Character)this.value).charValue());
                    break;
                case STRING:
                    out.writeString((String)this.value);
                    break;
                case CHAR_SEQUENCE:
                    TextUtils.writeToParcel((CharSequence)this.value, out, flags);
                    break;
                case URI:
                    out.writeInt(this.value != null ? 1 : 0);
                    if (this.value != null) {
                        ((Uri)this.value).writeToParcel(out, flags);
                    }
                    break;
                case BITMAP:
                    out.writeInt(this.value != null ? 1 : 0);
                    if (this.value != null) {
                        ((Bitmap)this.value).writeToParcel(out, flags);
                    }
                    break;
                case BUNDLE:
                    out.writeBundle((Bundle) this.value);
                    break;
                case INTENT:
                    out.writeInt(this.value != null ? 1 : 0);
                    if (this.value != null) {
                        ((Intent)this.value).writeToParcel(out, flags);
                    }
                    break;
                case COLOR_STATE_LIST:
                    out.writeInt(this.value != null ? 1 : 0);
                    if (this.value != null) {
                        ((ColorStateList)this.value).writeToParcel(out, flags);
                    }
                    break;
                case ICON:
                    out.writeInt(this.value != null ? 1 : 0);
                    if (this.value != null) {
                        ((Icon)this.value).writeToParcel(out, flags);
                    }
                    break;
                default:
                    break;
            }
        }

        private Class<?> getParameterType() {
            switch (this.type) {
                case BOOLEAN:
                    return boolean.class;
                case BYTE:
                    return byte.class;
                case SHORT:
                    return short.class;
                case INT:
                    return int.class;
                case LONG:
                    return long.class;
                case FLOAT:
                    return float.class;
                case DOUBLE:
                    return double.class;
                case CHAR:
                    return char.class;
                case STRING:
                    return String.class;
                case CHAR_SEQUENCE:
                    return CharSequence.class;
                case URI:
                    return Uri.class;
                case BITMAP:
                    return Bitmap.class;
                case BUNDLE:
                    return Bundle.class;
                case INTENT:
                    return Intent.class;
                case COLOR_STATE_LIST:
                    return ColorStateList.class;
                case ICON:
                    return Icon.class;
                default:
                    return null;
            }
        }

        @Override
        public void apply(View root, ViewGroup rootParent, OnClickHandler handler) {
            final View view = root.findViewById(viewId);
            if (view == null) return;

            Class<?> param = getParameterType();
            if (param == null) {
                throw new ActionException("bad type: " + this.type);
            }

            try {
                getMethod(view, this.methodName, param).invoke(view, wrapArg(this.value));
            } catch (ActionException e) {
                throw e;
            } catch (Exception ex) {
                throw new ActionException(ex);
            }
        }

        @Override
        public Action initActionAsync(ViewTree root, ViewGroup rootParent, OnClickHandler handler) {
            final View view = root.findViewById(viewId);
            if (view == null) return ACTION_NOOP;

            Class<?> param = getParameterType();
            if (param == null) {
                throw new ActionException("bad type: " + this.type);
            }

            try {
                Method method = getMethod(view, this.methodName, param);
                Method asyncMethod = getAsyncMethod(method);

                if (asyncMethod != null) {
                    Runnable endAction = (Runnable) asyncMethod.invoke(view, wrapArg(this.value));
                    if (endAction == null) {
                        return ACTION_NOOP;
                    } else {
                        // Special case view stub
                        if (endAction instanceof ViewStub.ViewReplaceRunnable) {
                            root.createTree();
                            // Replace child tree
                            root.findViewTreeById(viewId).replaceView(
                                    ((ViewStub.ViewReplaceRunnable) endAction).view);
                        }
                        return new RunnableAction(endAction);
                    }
                }
            } catch (ActionException e) {
                throw e;
            } catch (Exception ex) {
                throw new ActionException(ex);
            }

            return this;
        }

        public int mergeBehavior() {
            // smoothScrollBy is cumulative, everything else overwites.
            if (methodName.equals("smoothScrollBy")) {
                return MERGE_APPEND;
            } else {
                return MERGE_REPLACE;
            }
        }

        public String getActionName() {
            // Each type of reflection action corresponds to a setter, so each should be seen as
            // unique from the standpoint of merging.
            return "ReflectionAction" + this.methodName + this.type;
        }

        @Override
        public boolean prefersAsyncApply() {
            return this.type == URI || this.type == ICON;
        }
    }
```
可以看到，他是一个反射动作，通过他对使用反射的方式`  getMethod(view, this.methodName, param).invoke(view, wrapArg(this.value));`

```
private class TextViewSizeAction extends Action {
    public TextViewSizeAction(int viewId, int units, float size) {
        this.viewId = viewId;
        this.units = units;
        this.size = size;
    }

    public TextViewSizeAction(Parcel parcel) {
        viewId = parcel.readInt();
        units = parcel.readInt();
        size  = parcel.readFloat();
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(TAG);
        dest.writeInt(viewId);
        dest.writeInt(units);
        dest.writeFloat(size);
    }

    @Override
    public void apply(View root, ViewGroup rootParent, OnClickHandler handler) {
        final TextView target = root.findViewById(viewId);
        if (target == null) return;
        target.setTextSize(units, size);
    }

    public String getActionName() {
        return "TextViewSizeAction";
    }

    int units;
    float size;

    public final static int TAG = 13;
}
```
这个比较简单，就不分析了。
关于单击事件，RemoteVies中只支持发起PendingIntent，不支持onClickListener模式。另外我们需要注意setOnClickPendingIntent，setPengdingIntentTemplate，setOnClickFillIInIntent之间的联系。
首先：setOnClickPendingIntent用于给普通的View设置单击事件，但是不能给集合设置单击事件（ListView和StackView）中的View设置单击事件；其次要给ListView和stackView中的Item设置单击事件，必须将SetPendingIntentTemplate和setOnClickFillInIntent组合才可以。

## RemoteViews的意义
在之前的章节，已经分析了RemoteViews的内部机制，了解RemoteViews的内部机制可以让我妈更加清晰的了解通知栏和桌面小工具的实现原理。但是本章对RemoteViews的探索并没有终止。在本章节中我们将打造一个模拟通知栏并实现跨进程的UI更新操作。


首先是两个Activityy分别运行在不同的进程中，一个名字叫A，一个名字叫B，其中A扮演模拟通知栏的角色，B则可以不听的发送通知栏消息，当然这是模拟消息的信息。为了模拟通知栏效果，我们修改A的Progress属性，使其运行在单独的进程。这样A和B就构成了多进程通讯。我们在B中创建RemoteViews对象，然后通知A显示这个RemoteViews对象。如何通过通知A显示B中的remoteViews呢？我们可以像系统一样使用Binder来实现，但是为了简单期间就使用广播。B每发送一次通知，就会发送一个特定广播，然后A接受到这个特定广播，后开始显示B中定义的RemoteViews对象，这个过程和系统的通知栏消息的显示过程几乎一致，或者说这里就是负值了通知栏的显示过程而已。
首先看一下B的实现。B只要构造RemoteViews并将其传递给A就可以了。这一过程通知栏采用Binder实现，这里采用广播来实现


```
public class AActivity extends Activity {


    private static final String TAG = "MainActivity";
    private LinearLayout mRemoteViewsContent;
    private BroadcastReceiver mRemoteViewReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
          RemoteViews remoteViews = intent.getParcelableExtra("xxx,xxx");
                 if (remoteViews != null) {
                     updateUI(remoteViews);
                 }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        mRemoteViewsContent=findViewById(R.id.test);
        IntentFilter filter=new IntentFilter("xxx,xxx");
        registerReceiver(mRemoteViewReceiver,filter);
    }



    private void  updateUI(RemoteViews remoteViews){
        View view=remoteViews.apply(this,mRemoteViewsContent);
        mRemoteViewsContent.addView(view);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mRemoteViewReceiver);
        super.onDestroy();


    }
}

```

```
public class BActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.b_activity);

        remoteViews.setTextViewText(R.id.msg, "msg from process :");
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, DemoActivity_1.class), PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent openActivity2 = PendingIntent.getActivity(this, 0, new Intent(this, DemoActivity_2.class), PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.item_holder, pendingIntent);


        Intent intent = new Intent("xxx.xxx");
        intent.putExtra("xxx.xxx", remoteViews);
        sendBroadcast(intent);

    }
}

```

就是一个数据的传递。

# 第六章 Android的Drawable
本章的话题是Android的drawable，drawable表示一种可以在Canvas上进行绘制的抽象的概念，他的种类有很多，这里介绍一下。然后drawable在定制一些特殊View时很方便。他有很多优点，比如使用简单，比自定义View成本低很多。其次，非图片类型的Drawable占用空间小，对减小apk体积有很大的帮助。
## Drawable简介
drawable有很多种


![Alt text](图像1525616978.png "这个是在Drawable上面所有的子类")

在实际的开发中，Drawable常被用来作为View的背景使用，Drawable一般是通过XML来定义的。当然，我们可以通过代码创建具体的drawable对象。只是用代码创建比较复杂。


## Drawable的分类
看到上面的截图没有，种类有很多，这里就主要讲解几种常用的，比如BitmapDrawable，ShapeDrawable,layerDrawable,StateListDrawable等，这里就不一一列举了，下面分别介绍一下他们的细节

### BitmapDrawable
这个是最简单的Drawable了，他表示的就是一张图片。在实际开发过程中，我们可以直接引用原始的图片就可以了，但是也可以使用XML来描述他，通过XML来描述BitmapDrawable可以设置更多的效果：
```
<?xml version="1.0" encoding="utf-8"?>
<bitmap xmlns:android="http://schemas.android.com/apk/res/android"
    android:src="@mipmap/ic_launcher"
    android:antialias="false"
    android:dither="false"
    android:gravity="center"
    android:mipMap="false"
    android:tileMode="clamp"
    >

</bitmap>
```
看一下这些属性的含义

* android:src
  这个很简单，就是图片资源的id
* android:antialias
  是否开启图片抗锯齿功能，开启后会让图片变得平滑，同时也会在一定程度上面降低图片的清晰度，但是这个降低的幅度可以忽略。所以应当开启这个选项。
* android:dither
  是否开启抖动效果。当图片的像素配置和手机屏幕的像素配置不一致的时候，开启这个选项可以让高质量的图片在低质量的屏幕上还能保持比较好的显示效果，比如图片的色彩模式是ARGB8888，但是设备所支持的色彩模式为RGB555，这个时候开启抖动选项可以让图片不会过于失真。在ANdroiid中创建的Bitmap一般会选用ARGB8888这个模式，即ARGB各占8位，在这种色彩模式下，一个像素所占的大小为4个字节，一个像素的位数总和越高，图像也就越逼真。因此，这个也应该开启
* android:filter
  是否开启过滤效果，当图片尺寸被拉伸或者压缩时，开启过滤效果可以保持较好的显示效果，因此也应该开启
* android:gravity
  当图片大小小于容器大小是，可以设置这个选项来定位图片，这个属性的可选项较多，不过可以使用|来组合使用

|可选项|含义|
|:--:|:--:|
|top|将图片放在容器顶部，不改变图片大小|
|bottom|将图片放在容器底部，不改变图片大小|
|left|将图片放在容器左部，不改变图片大小|
|right|将图片放在容器右部，不改变图片大小|
|center_vertical|将图片竖直居中，不改变图片大小|
|center_horizontal|将图片水平居中，不改变图片大小|
|center|将图片居中，不改变图片大小|
|fill_vertical|将图片竖直方向填充容器|
|fill_horizontal|将图片水平方向填充容器|
|fill|将图片填充容器，包含水平和竖直两个方向，这个是默认值|
|clip_vertical|附加选项，表示竖直方向剪裁，较少使用|
|clip_horizontal|附加选项，表示水平方向剪裁，较少使用|

* android:mipMap
这是一种图像相关的处理技术，也叫纹理映射，比较抽象，这里也不对他进行深究，默认值为false，这个选项开发中不常用，具体可以看一下 https://baike.baidu.com/item/%E7%BA%B9%E7%90%86%E6%98%A0%E5%B0%84/7366346 了解一下。这个做游戏会用的比较多

* android:tileMode
平铺模式，这个选项有如下几个值，disabled，clamp，repeat，mirror。其中disabled表示关闭平铺模式，这个是默认值。当开启平铺模式之后，gravity属性会被忽略，这里主要说一下repeat，mirror，clamp的区别。
这里直接看效果图。这个样子比较明显


![Alt text](huaji.jpg "原图")

clamp效果

```
<?xml version="1.0" encoding="utf-8"?>
<bitmap xmlns:android="http://schemas.android.com/apk/res/android"
    android:src="@mipmap/test"
    android:antialias="true"
    android:dither="true"
    android:gravity="center"
    android:mipMap="true"
    android:tileMode="clamp"
    >
</bitmap>
```

![Alt text](device-2018-05-07-105130.png "clamp")

mirror效果

```
<?xml version="1.0" encoding="utf-8"?>
<bitmap xmlns:android="http://schemas.android.com/apk/res/android"
    android:src="@mipmap/test"
    android:antialias="true"
    android:dither="true"
    android:gravity="center"
    android:mipMap="true"
    android:tileMode="mirror"
    >
</bitmap>
```

![Alt text](device-2018-05-07-105505.png "mirror")


repeat效果

```
<?xml version="1.0" encoding="utf-8"?>
<bitmap xmlns:android="http://schemas.android.com/apk/res/android"
    android:src="@mipmap/test"
    android:antialias="true"
    android:dither="true"
    android:gravity="center"
    android:mipMap="true"
    android:tileMode="repeat"
    >
</bitmap>
```

![Alt text](device-2018-05-07-105557.png "repeat")

可以看到明显的区别
接下来介绍一下ninePatchDrawable，他表示一张.9格式的图片，.9格式的图片可以自动的根据所需要的宽和高进行缩放并保证不失真，之所以把它和BitmapDrawable放在一起介绍是因为他们都表示一张图片。和BitmapDrawable一样，在实际使用中直接引用图片即可。但是也可以通过xml来描述.9图

```
<?xml version="1.0" encoding="utf-8"?>
<nine-patch xmlns:android="http://schemas.android.com/apk/res/android"
    android:src="@mipmap/test">

</nine-patch>
```
可以把它作为背景使用

### ShapeDrawable
shapeDrable是也是一个常用的drawable。他的作用是绘制一些比较复杂的图形，比如圆形

```
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="oval"
    android:dither="true">
    <corners android:radius="2dp" />
    <gradient android:angle="20" />
    <padding android:bottom="20dp" />
    <size android:height="20dp" />
    <solid android:color="#ff0000" />
    <stroke android:color="#00ff00" />

</shape>
```

这里先来看一下一个最基本的shape写法，然后分析一下常用的几个属性

* android:shape
  这个表示图形的形状，有四个选项rectangle(矩形)，oval（椭圆），line(横线)，ring（圆环）。他默认是矩形的，另外，line和ring这两个选项必须要通过<stroke>这个标签来指定线条的宽度和颜色信息，否则将无法达到预期的显示效果，针对ring这个形状，有5个特殊的属性：android:innerRadius,android:thickness,android:innerRadiusRatio,android:thicknessRatio和android:useLevel，他们的含义

  |value|desciption|
  |:---:|:---:|
  |android:innerRadius|圆环的内半径，和innerRadiusRatio属性同时存在，以innerRadius为准|
  |android:thickness|圆环厚度，即外半径减去内半径的大小，和innerRadiusRatio同时存在，以innerRadius为准|
  |android:innerRadiusRatio|外半径占整个drawable宽度的比例。默认值为9，如果值为n，那么内半径的宽度=宽度/n|
  |android:thicknessRatio|厚度占整个drawable宽度的比例。默认值为3，如果值为n，那么厚度的宽度=厚度/n|
  |android:useLevel|一般使用false，否则无法达到效果，除非他被当做LevelListDrawable|

* conners
  表示4个角的角度，他只适用于shape，这里是指圆角的程度，用px或者dp来表示。
  * android:radius  为四个角度同时设定相同的角度，优先级低
  * android:bottomLeftRadius 不同角度的高度，优先级高
  * android:bottomRightRadius
  * android:topLeftRadius
  * android:topRightRadius

* gradient 他与sold标签互相排斥，其中solid表示纯色填充，而gradient则表示渐变效果，gradient有如下几个属性
  * angle 渐变的角度，默认值是0，其值必须是45的倍数，0表示从左到右，90表示从下到上，具体的效果需要看显示的效果来微调。
  * centerX 渐变的中心的横坐标
  * centerY 渐变的中心的纵坐标
  * startColor 开始颜色
  * centerColor 中间颜色
  * endColor 渐变的结束色
  * gradientRadius 渐变半径 仅当type=radial是有效
  * useLevel 一般为false，当drawable所谓stateListDrawable使用时有效
  * type 渐变类别，有linear（线性渐变），radial（径向渐变），sweep（扫描渐变）三种，其中默认是线性渐变
* sold 表示填充色
* stroke 描边
* padding 表示空白，不是shape的空白，而是View的空白
* size 表示这个shape的固有大小 如果设置，那么这个就是shape的固有大小，但是作为背景时会被拉伸。
### layerDrawable
laywerDrawable对应的XML是标签layer-list，他表示一种层次化的drawable集合，通过不同的drawable放置在不同层面上从而达到叠加后的效果
```
<?xml version="1.0" encoding="utf-8"?>
<layer-list xmlns:android="http://schemas.android.com/apk/res/android">

    <item android:id="@+id/ddd" android:drawable="@drawable/huaji2"/>
    <item android:id="@+id/ddd333" android:drawable="@drawable/huaji2"/>
</layer-list>
```
每一个item都表示一个drawable，item的结构也比较简单，下面的会覆盖上层的item，通过合理分层可以实现一些特殊的效果
这里实现一个微信中文本输入框的效果

```
<?xml version="1.0" encoding="utf-8"?>
<layer-list xmlns:android="http://schemas.android.com/apk/res/android">
    <item  >
        <shape android:shape="rectangle">
            <solid android:color="#0ac39e"/>
        </shape>

       </item>
    <item  android:bottom="6dp">
        <shape android:shape="rectangle">
            <solid android:color="#ffffff"/>
        </shape>
    </item>
    <item  android:bottom="1dp" android:left="1dp" android:right="1dp">
        <shape android:shape="rectangle">
            <solid android:color="#ffffff"/>

        </shape>
    </item>
</layer-list>
```

![Alt text](device-2018-05-11-174618.png "效果图")

### StateListDrawable
这个对应selector标签，表示drawable集合，每个drawable都对应着View的一种状态，这样系统会根据View的状态来选择合适的drawable。
```
<?xml version="1.0" encoding="utf-8"?>
<selector xmlns:android="http://schemas.android.com/apk/res/android">
<item
    android:drawable="@drawable/test3"
    android:state_accelerated="true"
    android:state_active="true"
    android:state_checkable="true"
    android:state_checked="true"
    android:state_drag_can_accept="true"
    android:state_drag_hovered="true"
    android:state_enabled="true"
    android:state_first="true"
    android:state_focused="true"
    android:state_hovered="true"
    android:state_last="true"
    android:state_middle="true"
    android:state_pressed="true"
    android:state_selected="true"
    android:state_single="true"
    android:state_window_focused="true"
    android:state_activated="true"/>
</selector>
```
可以看一下有这么多种状态量
* android:constantSize
  StateListDrawable的固定大小是不随着其状态的改变而改变的，改变状态会导致stateListDrawable切换到具体的drawable,而不同的Drawable具有不同的固定大小，True表示StateListDrawable的固定大小保持不变，这是他的固定大小是内部所有drawable的最大值，false则会随着状态的改变而改变，此选项默认值为false
* android:mDisallowInterceptTouchEventOnHeader
  是否开启抖动效果，之前说过，最好开启
* android:variablePadding
  stateListDrawable的padding表示你是否随着其状态改变而改变，true表示会随着状态的改变而改变，false表示stateListDrawable的padding是内部所有Drawable的padding的最大值，默认值是false。建议不要开启

|状态|含义|
|:--:|:--:|
|android:state_pressed|表示按下状态，比如Button被按下后没有松开时的状态|
|android:state_focus|表示View已经获取了焦点|
|android:state_selected|表示用户选择了View|
|android:state_checked|表示用户选中了View，一般适用于CheckBox这类在选中和非选中状态之前切换的View|
|android:state_enable|表示View当前处于可用状态|

### LevelListDrawable
LevelListDrawable对应于level-list标签，他同样表示一个drawable集合，集合中的每一个Drawable都有一个等级level的概念，根据不同的等级，LevelListDrawable会切换为对应的Drawable，他的语法如下
```
<?xml version="1.0" encoding="utf-8"?>
<level-list xmlns:android="http://schemas.android.com/apk/res/android">
<item android:drawable="@drawable/test3"
    android:maxLevel="1"
    android:minLevel="2"
    />
</level-list>
```

上面的语法中，每个item表示一个drawable，并且有对应的等级范围，由android：min_level和android:maxLevel来指定，最大值和最小值会对应此item中的drawable。下面是一个例子，当他作为View的背景时。可以通过drawable的setLevel方法来设置不同的等级从而切换不同的drawable。如果它被用来所imageView的背前景，还可以通过imageView的setImageLevel方法来切换drawable，最后的drawable的顶级是有范围的，即0-10000，最小等级是0，也就是默认值，最大等级是10000。

### TransitionDrawable
TransitionDrawable对应于transition标签，他用于实现两个drawable之间的淡入淡出效果，他的语法如下

```
<?xml version="1.0" encoding="utf-8"?>
<transition xmlns:android="http://schemas.android.com/apk/res/android">
<item android:drawable="@drawable/test333"></item>
    <item android:drawable="@drawable/huaji2"></item>
</transition>
```
这样就定义了一组淡入淡出变换
然后通过
```
    TextView tv = findViewById(R.id.tx);
    TransitionDrawable background = (TransitionDrawable) tv.getBackground();
    background.startTransition(1000);
    background.reverseTransition(1000);
```
通过这两种方式进行调用，注意，这个一开始两张图片就会叠加在一起，然后会有一个淡入淡出的过程。
从滑稽头像变成狗狗
![Alt text](device-2018-05-12-160955.png "效果图")

### insetDrawable
这个对应inset标签，他可以将其他drawable内嵌到自己当中，可以在周围流出一定的间距，当一个View希望自己的背景比自己的实际区域小的时候就可以通过insetDrawable

```
<?xml version="1.0" encoding="utf-8"?>
<inset xmlns:android="http://schemas.android.com/apk/res/android"
    android:insetBottom="15dp"
    android:insetRight="15dp"
    android:insetLeft="15dp"
    android:insetTop="15dp"

    >
    <shape android:shape="rectangle">
        <solid android:color="#ff0000"></solid>
    </shape>
</inset>
```

```
<?xml version="1.0" encoding="utf-8"?>
<android.support.constraint.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity">

    <TextView
        android:id="@+id/tx"
        android:text="jdkfjksd"
        android:background="@drawable/test7"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />

</android.support.constraint.ConstraintLayout>
```
![Alt text](device-2018-05-12-161610.png "效果图")

### ScaleDrawable
对应的标签是scale，他可以根据自己的等级将指定的drawable缩放到一定的比例。
```
<?xml version="1.0" encoding="utf-8"?>
<scale xmlns:android="http://schemas.android.com/apk/res/android"
android:drawable="@drawable/test333"
android:level="10"
android:scaleHeight="50%">
</scale>
```

注意：这里的scaleHeight和scaleWidth是要百分比，是要缩小或者放大的百分比，如果是scaleHeight=25%，那么就是高度缩小25%

![Alt text](device-2018-05-12-163022.png "效果图")

这个是高度缩小25% 本来是全屏的

scaleDrawable有一点难以理解，这里我们先要弄明白他的等级这个概念，`android:level`的默认值是0，表示不可见，这个是默认值，想要scaleDrawable可见，他的等级不能为0，这一点可以从源码看出来

```
@Override
 public void draw(Canvas canvas) {
     final Drawable d = getDrawable();
     if (d != null && d.getLevel() != 0) {
         d.draw(canvas);
     }
 }

```
如果等级为0，是不会绘制的。
这里在看一下他的`onBoundsChanges`方法，入下所示

```
@Override
   protected void onBoundsChange(Rect bounds) {
       final Drawable d = getDrawable();
       final Rect r = mTmpRect;
       final boolean min = mState.mUseIntrinsicSizeAsMin;
       final int level = getLevel();

       int w = bounds.width();
       if (mState.mScaleWidth > 0) {
           final int iw = min ? d.getIntrinsicWidth() : 0;
           w -= (int) ((w - iw) * (MAX_LEVEL - level) * mState.mScaleWidth / MAX_LEVEL);
       }

       int h = bounds.height();
       if (mState.mScaleHeight > 0) {
           final int ih = min ? d.getIntrinsicHeight() : 0;
           h -= (int) ((h - ih) * (MAX_LEVEL - level) * mState.mScaleHeight / MAX_LEVEL);
       }

       final int layoutDirection = getLayoutDirection();
       Gravity.apply(mState.mGravity, w, h, bounds, r, layoutDirection);

       if (w > 0 && h > 0) {
           d.setBounds(r.left, r.top, r.right, r.bottom);
       }
   }

```

在这里可以看到drawable的大小和等级以及缩放比例的关系，这里那高度来说
```
int h = bounds.height();
if (mState.mScaleHeight > 0) {
    final int ih = min ? d.getIntrinsicHeight() : 0;
    h -= (int) ((h - ih) * (MAX_LEVEL - level) * mState.mScaleHeight / MAX_LEVEL);
}
```
一般ih都是0，所以上面可以简化为  h -= (int) (h * (10000 - level) * mState.mScaleHeight / 10000)，可见，如果scaleDrawable的级别越大，那么内部的drawable看起来越大，如果scaleDrawable的XML中所指定的缩放比例越大，那么内部的drawable看起来越小，所以看效果，scaleDrawable更加偏向于缩小一个特定的drawable。

```
TextView tv = findViewById(R.id.tx);
 ScaleDrawable scaleDrawable= (ScaleDrawable) tv.getBackground();
scaleDrawable.setLevel(1);
```

### ClipDrawable
clipDrawable 对应clip标签，他可以根据自己当前的等级来裁切另一个drawable,剪裁方向可以通过clipOrientation和gravity来控制

clipDrawable
```
<?xml version="1.0" encoding="utf-8"?>
<clip xmlns:android="http://schemas.android.com/apk/res/android"
    android:clipOrientation="vertical"
    android:gravity="bottom"

    android:drawable="@drawable/test333">

</clip>
```

imageView
```

    <ImageView
        android:id="@+id/tx"
        android:src="@drawable/test9"
        android:layout_width="100dp"
        android:layout_height="100dp" />
```
设置优先级
```
ImageView tv = findViewById(R.id.tx);
ClipDrawable clipDrawable= (ClipDrawable) tv.getDrawable();
lipDrawable.setLevel(5000);
```

![Alt text](device-2018-05-12-165851.png "效果图")

* clipOrientation表示剪裁方向
  上面的是竖直方向
* gravity表示从哪开始剪裁

|gravity选项|含义|
|:---:|:---:|
|top|将内部的drawable放在容器的顶部，不改变他的大小，如果为竖直剪裁，那么从顶部开始剪裁|
|bottom|将内部的drawable放在容器的底部，不改变他的大小，如果为竖直剪裁，那么从底部开始剪裁|
|left|将内部的drawable放在容器的左部，不改变他的大小，如果为水平剪裁，那么从左部开始剪裁|
|right|将内部的drawable放在容器的右部，不改变他的大小，如果为水平剪裁，那么从右部开始剪裁|
|center_vertical|将内部的drawable放在容器的竖直居中，不改变他的大小，如果为竖直剪裁，那么从底部和头部同时开始剪裁|
|fill_vertical|将内部的drawable在竖直方向上填充容器，不改变他的大小，如果为竖直剪裁，那么仅当clipDrawable的等级为0（0表示clipDrawable被完全剪裁，即不可见）时，才能有剪裁行为|
|center_horizontal|将内部的drawable放在容器的水平居中，不改变他的大小，如果为水平剪裁，那么从左部和右部同时开始剪裁|
|fill_horizontal|将内部的drawable在水平方向上填充容器，不改变他的大小，如果为水平剪裁，那么仅当clipDrawable的等级为0（0表示clipDrawable被完全剪裁，即不可见）时，才能有剪裁行为|
|center|将内部的drawable放在容器的居中，不改变他的大小，如果为水平剪裁，那么从左部和右部同时开始剪裁，如果为竖直剪裁，那么从底部和头部同时开始剪裁|
|fill|使内部的Drawable在水平和竖直方向上同时填充容器，仅当ClipDrawable的等级为0时，才能有剪裁行为|
|clip_vertical|表示竖直方向剪裁，较少使用|
|clip_horizontal|表示水平方向剪裁，较少使用|

前面也提及到他的等级是有范围的，就是0-10000，最小是0，最大是10000，如果是0这个等级，就表示完全剪裁，就是什么都没了，如果是10000，表示不剪裁，如果设置为8000，表示剪裁了20%，对于clipDrawable来说，等级越大，剪裁的部分越小

### 自定义Drawable
drawable的使用范围很单一，一个是作为imageView中图像来显示，一个是作为View的背景显示，大多数情况下drawable都是以View的背景出现的，drawable的原理很简单，其核心就是draw方法，在之前分析了draw的过程。这里可以通过使用重写drawable的draw方法来自定义drawable
通常我们没有必要去自定义drawable，这是因为自定义的drawable无法在XML中使用，这就降低了自定义Drawable的使用范围，下面演示一个自定义View的过程

```
public class CustomDrawable extends Drawable {
    private Paint mPaint;

    public CustomDrawable() {

        mPaint= new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.parseColor("#ff0000"));
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        final Rect r=getBounds();
        float cx=r.exactCenterX();
        float cy=r.exactCenterY();
        canvas.drawCircle(cx,cy,Math.min(cx,cy),mPaint);
    }

    @Override
    public void setAlpha(int alpha) {
        mPaint.setAlpha(alpha);
        invalidateSelf();
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        mPaint.setColorFilter(colorFilter);
        invalidateSelf();
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}

```
如果要实现具体的，可以参考一下BitmapDrawable和ShapeDrawable

# 第七章 Android 动画深入分析  
Android的动画可以分为三种：View动画，帧动画和属性动画，其实帧动画也属于View动画的一种，只不过他和平移。旋转等常见的View动画在表现形式上略有不用而已。View动画通过场景里的对象不断做图像变换（平移，缩放，旋转，透明度）从而产生动画效果，他是一种渐进式的动画，并且View动画支持自定义。帧动画通过顺序播放一系列图像从而产生动画效果，可以简单理解为图片切换动画。属性动画是API11的新特性，在低版本无法使用水性动画，但是我们任然可以通过使用兼容库来使用它。

## View动画
View动画的作用对象是View，他支持4中动画效果，分别是平移动画，缩放动画，旋转动画，透明动画，处理这四种经典的动画，帧动画也属于View动画，但是帧动画的表现形式和上面四种动画不太一样，为了更好了区分这四种变换和帧动画。这里的View动画就表示4中变换，不包括帧动画。
### View动画的种类
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

### 自定义动画

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

### 帧动画
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

## View动画的特殊使用场景
一些特殊的animation
### LayoutAnimation
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

### Activity的切换效果
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
## 属性动画
属性动画是API11新加入的特性，和View动画不同，他对作用对象进行了脱产，属性动画可以对任意对象做动画，甚至还可以没有对象...
属性动画中的ValueAnimator，ObjectAnimator和AnimatorSet等概念，通过他们可以实现不同的效果
### 使用属性动画
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
### 理解插值器和估值器
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
### 属性动画的监听器
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

### 对任意属性做动画
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


## 使用动画注意事项
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

# 第八章 理解Windows和WindowsManager
window表示一个窗口的概念，这里不是windows操作系统哦！在日常开发中直接接触window的机会并不多，但是在某些特殊的时候我们需要在桌面上显示类似悬浮窗的东西，那么这个就需要使用window了。window是一个抽象类，他的具体实现是photoWindow，创建一个window是很简单的事情，只需要通过windowManager即可完成。windowManager是外界访问window的入口，window的具体实现位于windowManagerService中，windowManager和WindowManagerService的交互是一个IPC过程，android中所有的视图都是通过window来呈现的，不管是activity。dialog。还是toast，他们的视图实际上都是附加在window上面的，因此window是View的直接管理者，从第四章View的事件分发机制也可以看出来，单击事件是有window传递给decorView的，然后再有decorView传递给我们的View，就连activity的设置视图的方法setContentView在底层也是通过window来完成的。
## window和wondowManager
为了分析window的工作机制，我们需要先了解如何使用windowManager添加一个window。下面的代码展示了如何通过windowManger添加window的过程，是不是很简单呢
如果是android8.0及以上，需要加权限
```
  <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW"/>
```
不然会报错：permission denied for window type 2010
```
public class MainActivity extends AppCompatActivity {

    private View bt;
    int OVERLAY_PERMISSION_CODE = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bt = findViewById(R.id.bt);

    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        addOverlay();

    }

    public void addOverlay() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, OVERLAY_PERMISSION_CODE);
            }else{
                Button mFloatButton = new Button(this);
                mFloatButton.setText("button");
                mFloatButton.setBackgroundColor(Color.RED);
                WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, 0, 0, PixelFormat.TRANSPARENT);
                layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
                layoutParams.gravity = Gravity.LEFT | Gravity.TOP;
                layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
                layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
                layoutParams.x = 100;
                layoutParams.y = 100;
                layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
                getWindowManager().addView(mFloatButton, layoutParams);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OVERLAY_PERMISSION_CODE) {

            if (Settings.canDrawOverlays(this)) {
                Button mFloatButton = new Button(this);
                mFloatButton.setText("button");
                WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, 0, 0, PixelFormat.TRANSPARENT);
                layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
                layoutParams.gravity = Gravity.LEFT | Gravity.TOP;
                layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
                layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
                layoutParams.x = 100;
                layoutParams.y = 100;
                layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
                getWindowManager().addView(mFloatButton, layoutParams);
            }
        }
    }
}

```
![Alt text](device-2018-05-16-160142.png "红色的是添加的button")
这里列举一下比较常用的flags属性
* FLAT_NOT_FOCUSABLE
表示window不需要焦点，也不需要接收各种输入时间，此标记会同时启用FLAG_NOT_TOUCH_MODAL，最终会将事件直接传递给下层具有焦点的window
* FLAG_NOT_TOUCH_MODAL
此模式下，系统会将当前window区域以外的单机事件传递给底层的window，当前window区域以内的单击事件则自己处理，这个标记很重要，一般来说都需要开启，否则其他window无法接收到单击事件
* FLAG_SHOW_WHEN_LOCKED
开启此模式可以让window显示在锁屏桌面上

Type参数表示window的类型，window有三种类型，分别是应用window，子window和系统window。应用类window对应着一个activity，子window不能单独存在，他需要衣服在特定的父window中，比如常见的一些dialog就是一个子window。系统window是需要声明权限才能创建的window，比如toast和系统状态栏都是系统window。
window是分层的，每个window都有对应的z-ordered，层级大的会覆盖在层级晓得window的上面，这个和html的z-index的概念是完全一致的。在三类window中，应用window的层级范围是1-99，子window的层级范围是1000-9999，系统window的层级范围是2000-2999，这些层级范围对应着windowManager.layoutParams的type参数，如果想要window位于所有window的最顶层，那么采用较大的层数即可，很显然系统window的层级是最大的，而且系统层级有很多只，一般我们选用 TYPE_SYSTEM_OVERLAY或者TYPE_SYSTEM_ERROR,如果采用TYPE_SYSTEM_ERROR，只需要为type之地当这个层级就可以mLayoutParams.type=layoutParams.TYPE_SYSTEM_ERROR，同时声明权限
```
  <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW"/>
```
因为系统类型的window是需要权限检查的，如果不在AndroidManifest中添加权限，会提示申请权限失败
windowManager所提供的功能很简单，常用的只有三个方法，即添加View、更新View和删除View。这三个方法定义在Viewmanager中，而WindowManager继承ViewManager
```
public interface ViewManager
{
    /**
     * Assign the passed LayoutParams to the passed View and add the view to the window.
     * <p>Throws {@link android.view.WindowManager.BadTokenException} for certain programming
     * errors, such as adding a second view to a window without removing the first view.
     * <p>Throws {@link android.view.WindowManager.InvalidDisplayException} if the window is on a
     * secondary {@link Display} and the specified display can't be found
     * (see {@link android.app.Presentation}).
     * @param view The view to be added to this window.
     * @param params The LayoutParams to assign to view.
     */
    public void addView(View view, ViewGroup.LayoutParams params);
    public void updateViewLayout(View view, ViewGroup.LayoutParams params);
    public void removeView(View view);
}
```
对于开发者来说，windowManager常用的就这三个功能而已，但是这三个功能已经足够我们使用了，他可以创建一个window并向其添加View，还可以更新window中的View，如果想要删除一个window，只需要删除他里面的View就可以。由此看来，windowManager操作window过程更像是在操作window中的view。我们时常见到那种可以拖动的window效果，其实是很好实现的，只需要根据手机的位置来指定layoutParams中的x和y就可以改变window的位置，首先给View设置onTouchListener，在ontouch中不断更新就可以
```
final Button mFloatButton = new Button(this);
              mFloatButton.setText("button");
              mFloatButton.setBackgroundColor(Color.RED);
              final WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, 0, 0, PixelFormat.TRANSPARENT);
              layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
              layoutParams.gravity = Gravity.LEFT | Gravity.TOP;
              layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
              layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
              layoutParams.x = 100;
              layoutParams.y = 100;
              layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;

              mFloatButton.setOnTouchListener(new View.OnTouchListener() {
                  @Override
                  public boolean onTouch(View v, MotionEvent event) {
                      float rawX = event.getRawX();
                      float rawY = event.getRawY();
                      switch (event.getAction()) {
                          case MotionEvent.ACTION_MOVE:
                              layoutParams.x= (int) rawX;
                              layoutParams.y= (int) rawY;
                              getWindowManager().updateViewLayout(mFloatButton, layoutParams);
                              break;
                      }
                      return false;
                  }
              });
              getWindowManager().addView(mFloatButton, layoutParams);
```

## window的内部机制
window是一个抽象的概念，每一个window都对应着一个view和一个ViewRootImpl，window和View通过ViewRootImpl来建立联系，因此window并不是实际存在的，他是以View的形式存在的，这一点从windowManager的定义可以看出来，他提供了三个方法addview，updateViewLayout和removeView都是针对View的，这说明View是window存在的尸体。在实际使用过程中无法访问window，对window的访问过程必须公国windowmanager。为了分析window的颞部机制，这里从window的添加，删除以及更新说起

### window的添加过程
window的添加过程需要通过windowManager的addView来实现，windowManager是一个接口，他的真正实现是windowManagerImpl类，在windowManagerInpl中window的三大操作如下:这里移除了一些代码
```
public final class WindowManagerImpl implements WindowManager {
    private final WindowManagerGlobal mGlobal = WindowManagerGlobal.getInstance();
    private final Context mContext;
    private final Window mParentWindow;
    @Override
    public void addView(@NonNull View view, @NonNull ViewGroup.LayoutParams params) {
        applyDefaultToken(params);
        mGlobal.addView(view, params, mContext.getDisplay(), mParentWindow);
    }

    @Override
    public void updateViewLayout(@NonNull View view, @NonNull ViewGroup.LayoutParams params) {
        applyDefaultToken(params);
        mGlobal.updateViewLayout(view, params);
    }



    @Override
    public void removeView(View view) {
        mGlobal.removeView(view, false);
    }

}
```

从这里可以看出windowMangerImpl并没有直接实现window的三大操作，而是全部交给了WindowManagerGlobal，WindowManagerGlobal用工厂方法向外界提供自己的实例

```
public void addView(View view, ViewGroup.LayoutParams params,
          Display display, Window parentWindow) {

      //检查参数是否合法，如果是子window，那么还要调整一下布局
      if (view == null) {
          throw new IllegalArgumentException("view must not be null");
      }
      if (display == null) {
          throw new IllegalArgumentException("display must not be null");
      }
      if (!(params instanceof WindowManager.LayoutParams)) {
          throw new IllegalArgumentException("Params must be WindowManager.LayoutParams");
      }

      final WindowManager.LayoutParams wparams = (WindowManager.LayoutParams) params;
      if (parentWindow != null) {
          parentWindow.adjustLayoutParamsForSubWindow(wparams);
      } else {
          // If there's no parent, then hardware acceleration for this view is
          // set from the application's hardware acceleration setting.
          final Context context = view.getContext();
          if (context != null
                  && (context.getApplicationInfo().flags
                          & ApplicationInfo.FLAG_HARDWARE_ACCELERATED) != 0) {
              wparams.flags |= WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;
          }
      }

      ViewRootImpl root;
      View panelParentView = null;

      synchronized (mLock) {
          // Start watching for system property changes.
          if (mSystemPropertyUpdater == null) {
              mSystemPropertyUpdater = new Runnable() {
                  @Override public void run() {
                      synchronized (mLock) {
                          for (int i = mRoots.size() - 1; i >= 0; --i) {
                              mRoots.get(i).loadSystemProperties();
                          }
                      }
                  }
              };
              SystemProperties.addChangeCallback(mSystemPropertyUpdater);
          }

          int index = findViewLocked(view, false);
          if (index >= 0) {
              if (mDyingViews.contains(view)) {
                  // Don't wait for MSG_DIE to make it's way through root's queue.
                  mRoots.get(index).doDie();
              } else {
                  throw new IllegalStateException("View " + view
                          + " has already been added to the window manager.");
              }
              // The previous removeView() had not completed executing. Now it has.
          }

          // If this is a panel window, then find the window it is being
          // attached to for future reference.
          if (wparams.type >= WindowManager.LayoutParams.FIRST_SUB_WINDOW &&
                  wparams.type <= WindowManager.LayoutParams.LAST_SUB_WINDOW) {
              final int count = mViews.size();
              for (int i = 0; i < count; i++) {
                  if (mRoots.get(i).mWindow.asBinder() == wparams.token) {
                      panelParentView = mViews.get(i);
                  }
              }
          }

          root = new ViewRootImpl(view.getContext(), display);

          view.setLayoutParams(wparams);

          mViews.add(view);
          mRoots.add(root);
          mParams.add(wparams);

          // do this last because it fires off messages to start doing things
          try {
              root.setView(view, wparams, panelParentView);
          } catch (RuntimeException e) {
              // BadTokenException or InvalidDisplayException, clean up.
              if (index >= 0) {
                  removeViewLocked(index, true);
              }
              throw e;
          }
      }
  }

```

一些比较重要的参数
```
private final ArrayList<View> mViews = new ArrayList<View>();
  private final ArrayList<ViewRootImpl> mRoots = new ArrayList<ViewRootImpl>();
  private final ArrayList<WindowManager.LayoutParams> mParams =
          new ArrayList<WindowManager.LayoutParams>();
  private final ArraySet<View> mDyingViews = new ArraySet<View>();

```
上面代码声明了mViews存储的是所有window所对应的View，mRoots存储的是所有window岁对应的ViewRootImol，mparams存储的是所有window所对应的布局参数，而mDyingView存储的是那些正在被删除的View对象，但是还没有完成删除的window对象，add通过如下方式将window的一系列对象添加到列表中
```

root = new ViewRootImpl(view.getContext(), display);
view.setLayoutParams(wparams);
mViews.add(view);
mRoots.add(root);
mParams.add(wparams);
```

通过ViewRootImpl来更新界面并完成window的添加过程
这个不愁由ViewRootImol的setView方法来完成
```
 root.setView(view, wparams, panelParentView);
```
在setView的内部会通过requestLayout来完成异步刷新请求。在下面的代码中，scheduleTraversals实际是View的绘制入口

```
@Override
 public void requestLayout() {
     if (!mHandlingLayoutInLayoutRequest) {
         checkThread();
         mLayoutRequested = true;
         scheduleTraversals();
     }
 }

```
接着会通过windowsession最终完成windoow的添加过程。在下面代码中mWindowSession的类型是IWindowSession，他是一个Binder对象，真正的实现类是Session，也就是说window的添加过程其实是一次ipc调用
setView中的一行代码
```
res = mWindowSession.addToDisplay(mWindow, mSeq, mWindowAttributes,
                       getHostVisibility(), mDisplay.getDisplayId(),
                       mAttachInfo.mContentInsets, mAttachInfo.mStableInsets,
                       mAttachInfo.mOutsets, mInputChannel);
```

```
public static IWindowSession getWindowSession() {
    synchronized (WindowManagerGlobal.class) {
        if (sWindowSession == null) {
            try {
                InputMethodManager imm = InputMethodManager.getInstance();
                IWindowManager windowManager = getWindowManagerService();
                sWindowSession = windowManager.openSession(
                        new IWindowSessionCallback.Stub() {
                            @Override
                            public void onAnimatorScaleChanged(float scale) {
                                ValueAnimator.setDurationScale(scale);
                            }
                        },
                        imm.getClient(), imm.getInputContext());
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
        return sWindowSession;
    }
}
```

session是这个包下面的package com.android.server.wm;

在session内部会通过windowManagerService来实现window的添加
```
@Override
 public int addToDisplay(IWindow window, int seq, WindowManager.LayoutParams attrs,
         int viewVisibility, int displayId, Rect outContentInsets, Rect outStableInsets,
         Rect outOutsets, InputChannel outInputChannel) {
     return mService.addWindow(this, window, seq, attrs, viewVisibility, displayId,
             outContentInsets, outStableInsets, outOutsets, outInputChannel);
 }

```
这样addwindow就交给windowManagerservice去处理了


### window的删除过程
window的删除过程和添加过程是类似的，都是先通过windowmanagerImpl后在进一步通过windowManagerGlobal来实现，下面是windowManagerImpl来实现。下面是windowManagerGlobal的removeView的实现：
```

public void removeView(View view, boolean immediate) {
    if (view == null) {
        throw new IllegalArgumentException("view must not be null");
    }

    synchronized (mLock) {
        int index = findViewLocked(view, true);
        View curView = mRoots.get(index).getView();
        removeViewLocked(index, immediate);
        if (curView == view) {
            return;
        }

        throw new IllegalStateException("Calling with view " + view
                + " but the ViewAncestor is attached to " + curView);
    }
}
```
removeView的逻辑很清晰，首先通过findViewLocked来查找待删除的View的索引，这个查找过程就是建立的数据遍历，然后在调用removeViewLocked来做进一步删除操作，如下所示
```
private void removeViewLocked(int index, boolean immediate) {
    ViewRootImpl root = mRoots.get(index);
    View view = root.getView();

    if (view != null) {
        InputMethodManager imm = InputMethodManager.getInstance();
        if (imm != null) {
            imm.windowDismissed(mViews.get(index).getWindowToken());
        }
    }
    boolean deferred = root.die(immediate);
    if (view != null) {
        view.assignParent(null);
        if (deferred) {
            mDyingViews.add(view);
        }
    }
}
```

removeViewLocked是通过ViewRootImpl来完成删除操作的，在windowManager中提供了两种删除接口`removeView`和`removeViewImmediate`，他们分别表示异步删除和同步删除，其中removeViewImmediate使用起来需要特别注意，一般来说不需要使用此方法来删除window以避免意外的错误发生，这里主要说一下异步删除的情况，据图的操作由ViewRootImol的die方法来完成，在异步删除的情况下，die方法只是发送一个请求删除的消息后就立刻返回了，这个时候View并没有完成删除操作，所以最后会将其添加到mDyingViews中，mDyingViews表示待删除的View列表，ViewRootImpl的die方法如下所示
```
boolean die(boolean immediate) {
    // Make sure we do execute immediately if we are in the middle of a traversal or the damage
    // done by dispatchDetachedFromWindow will cause havoc on return.
    if (immediate && !mIsInTraversal) {
        doDie();
        return false;
    }

    if (!mIsDrawing) {
        destroyHardwareRenderer();
    } else {
        Log.e(mTag, "Attempting to destroy the window while drawing!\n" +
                "  window=" + this + ", title=" + mWindowAttributes.getTitle());
    }
    mHandler.sendEmptyMessage(MSG_DIE);
    return true;
}

```
在爹方法内部只是做了简单的判断，如果是异步操作，那么久发送一个MSG_DIE的消息，ViewRootImpl中的handler会处理此消息并调用doDie方法，如果是同步删除（立即删除），那么久不需要发送消息，直接调用doDie方法，这就是这两种删除方式的区别，在dodie内部会调用dispatchDetachedFromWindow方法，真正删除View的逻辑在dispatchDetachedFromWindow方法内部实现，dispatchDetachedFromWindow方法主要做四件事

```
void dispatchDetachedFromWindow() {
       if (mView != null && mView.mAttachInfo != null) {
           mAttachInfo.mTreeObserver.dispatchOnWindowAttachedChange(false);
           mView.dispatchDetachedFromWindow();
       }

       mAccessibilityInteractionConnectionManager.ensureNoConnection();
       mAccessibilityManager.removeAccessibilityStateChangeListener(
               mAccessibilityInteractionConnectionManager);
       mAccessibilityManager.removeHighTextContrastStateChangeListener(
               mHighContrastTextManager);
       removeSendWindowContentChangedCallback();

       destroyHardwareRenderer();

       setAccessibilityFocus(null, null);

       mView.assignParent(null);
       mView = null;
       mAttachInfo.mRootView = null;

       mSurface.release();

       if (mInputQueueCallback != null && mInputQueue != null) {
           mInputQueueCallback.onInputQueueDestroyed(mInputQueue);
           mInputQueue.dispose();
           mInputQueueCallback = null;
           mInputQueue = null;
       }
       if (mInputEventReceiver != null) {
           mInputEventReceiver.dispose();
           mInputEventReceiver = null;
       }
       try {
           mWindowSession.remove(mWindow);
       } catch (RemoteException e) {
       }

       // Dispose the input channel after removing the window so the Window Manager
       // doesn't interpret the input channel being closed as an abnormal termination.
       if (mInputChannel != null) {
           mInputChannel.dispose();
           mInputChannel = null;
       }

       mDisplayManager.unregisterDisplayListener(mDisplayListener);

       unscheduleTraversals();
   }

```


1. 垃圾回收相关工作，比如清除数据和消息，移除回调
2. 通过Session的remove方法删除Window：mWindowSession.remove(mWindow)，这同样是一个IPC过程，最终会调用WindowManagerService的removeWindow方法
3. 调用View的DispatchDetachedFromWindow方法，在内部会调用View的onDetachedFromWindow()以及onDetachedFromWindowInternal()。对于onDetachedFromWindow（）方法大家一定不陌生，当View从window上面移除的时候，这个方法就会被调用，这个方法内部做一些资源回收的工作，比如终止动画，停止线程等
4. 调用WindowmanagerGlobal的doRemoveView方法刷新数据，包括mRoots，mParams以及mDyingViews，需要将当前Window所关联的这三个对象从列表中删除

### window的更新过程
到这里，Window的产出过程已经分析完毕，现在来分析一下更新过程，还是要看WindowManagerGlobal的updateViewLayout方法，如下所示

```

public void updateViewLayout(View view, ViewGroup.LayoutParams params) {
      if (view == null) {
          throw new IllegalArgumentException("view must not be null");
      }
      if (!(params instanceof WindowManager.LayoutParams)) {
          throw new IllegalArgumentException("Params must be WindowManager.LayoutParams");
      }

      final WindowManager.LayoutParams wparams = (WindowManager.LayoutParams)params;

      view.setLayoutParams(wparams);

      synchronized (mLock) {
          int index = findViewLocked(view, true);
          ViewRootImpl root = mRoots.get(index);
          mParams.remove(index);
          mParams.add(index, wparams);
          root.setLayoutParams(wparams, false);
      }
  }
```

updateViewlayout方法做的事情就比较简单了，首先他需要更新View的LayoutParams并替换掉老的LayoutParams，接着在更新ViewRootImol中的LayoutParams，这一步是通过ViewRootImpl的setlayoutParams方法来实现的，在ViewRootImpl中会通过scheduleTraversals方法来对View重新布局，包括测量、布局、重绘这三个过程。除了View本身的重绘以外，ViewRootImol还会通过WindowSession来更新Window的视图，这个过程最终是由WindowManagerService的relayoutWindow来实现的，同样他是一个IPC过程

## window的创建过程

通过上面的分析可以看出，View是Android中视图的呈现方式，但是View并不能单独存在，他必须附着在Window这个抽象的概念上面，因此有视图的地方就有window。那些地方有视图呢？这个读者都比较清楚，Android中刻印提供视图的地方有Activity，dialog，toast初次之外，还有一些依托window而实现的视图，比如PopUpWIndow，菜单，他们也是视图，有视图的地方就有window，因此Activity、dialog、Toast等视图都对应着一个window。本节将分析的地方就有window的 创建过程，通过这个过程加深对window的进一步理解。


### Activity的window创建过程
要分析activitty中的window的创建过程就必须了解activity的启动过程，详细的过程会在下一章介绍，这里先介绍一个大概过程，先有点印象。Activity的启动过程很复杂，最终会由ActivityThread中的performLaunchActivity来完成整个启动过程，在这个方法内部会通过类加载器创建activity的实例，并调用attach方法为其关联运行中所依赖的一系列上下文环境变量，代码如下所示
activityThread
```
private Activity performLaunchActivity(ActivityClientRecord r, Intent customIntent) {
    // System.out.println("##### [" + System.currentTimeMillis() + "] ActivityThread.performLaunchActivity(" + r + ")");

    ActivityInfo aInfo = r.activityInfo;
    if (r.packageInfo == null) {
        r.packageInfo = getPackageInfo(aInfo.applicationInfo, r.compatInfo,
                Context.CONTEXT_INCLUDE_CODE);
    }

    ComponentName component = r.intent.getComponent();
    if (component == null) {
        component = r.intent.resolveActivity(
            mInitialApplication.getPackageManager());
        r.intent.setComponent(component);
    }

    if (r.activityInfo.targetActivity != null) {
        component = new ComponentName(r.activityInfo.packageName,
                r.activityInfo.targetActivity);
    }

    ContextImpl appContext = createBaseContextForActivity(r);
    Activity activity = null;
    try {
        java.lang.ClassLoader cl = appContext.getClassLoader();
        activity = mInstrumentation.newActivity(
                cl, component.getClassName(), r.intent);
        StrictMode.incrementExpectedActivityCount(activity.getClass());
        r.intent.setExtrasClassLoader(cl);
        r.intent.prepareToEnterProcess();
        if (r.state != null) {
            r.state.setClassLoader(cl);
        }
    } catch (Exception e) {
        if (!mInstrumentation.onException(activity, e)) {
            throw new RuntimeException(
                "Unable to instantiate activity " + component
                + ": " + e.toString(), e);
        }
    }

    try {
        Application app = r.packageInfo.makeApplication(false, mInstrumentation);

        if (localLOGV) Slog.v(TAG, "Performing launch of " + r);
        if (localLOGV) Slog.v(
                TAG, r + ": app=" + app
                + ", appName=" + app.getPackageName()
                + ", pkg=" + r.packageInfo.getPackageName()
                + ", comp=" + r.intent.getComponent().toShortString()
                + ", dir=" + r.packageInfo.getAppDir());

        if (activity != null) {
            CharSequence title = r.activityInfo.loadLabel(appContext.getPackageManager());
            Configuration config = new Configuration(mCompatConfiguration);
            if (r.overrideConfig != null) {
                config.updateFrom(r.overrideConfig);
            }
            if (DEBUG_CONFIGURATION) Slog.v(TAG, "Launching activity "
                    + r.activityInfo.name + " with config " + config);
            Window window = null;
            if (r.mPendingRemoveWindow != null && r.mPreserveWindow) {
                window = r.mPendingRemoveWindow;
                r.mPendingRemoveWindow = null;
                r.mPendingRemoveWindowManager = null;
            }
            appContext.setOuterContext(activity);
            activity.attach(appContext, this, getInstrumentation(), r.token,
                    r.ident, app, r.intent, r.activityInfo, title, r.parent,
                    r.embeddedID, r.lastNonConfigurationInstances, config,
                    r.referrer, r.voiceInteractor, window, r.configCallback);

            if (customIntent != null) {
                activity.mIntent = customIntent;
            }
            r.lastNonConfigurationInstances = null;
            checkAndBlockForNetworkAccess();
            activity.mStartedActivity = false;
            int theme = r.activityInfo.getThemeResource();
            if (theme != 0) {
                activity.setTheme(theme);
            }

            activity.mCalled = false;
            if (r.isPersistable()) {
                mInstrumentation.callActivityOnCreate(activity, r.state, r.persistentState);
            } else {
                mInstrumentation.callActivityOnCreate(activity, r.state);
            }
            if (!activity.mCalled) {
                throw new SuperNotCalledException(
                    "Activity " + r.intent.getComponent().toShortString() +
                    " did not call through to super.onCreate()");
            }
            r.activity = activity;
            r.stopped = true;
            if (!r.activity.mFinished) {
                activity.performStart();
                r.stopped = false;
            }
            if (!r.activity.mFinished) {
                if (r.isPersistable()) {
                    if (r.state != null || r.persistentState != null) {
                        mInstrumentation.callActivityOnRestoreInstanceState(activity, r.state,
                                r.persistentState);
                    }
                } else if (r.state != null) {
                    mInstrumentation.callActivityOnRestoreInstanceState(activity, r.state);
                }
            }
            if (!r.activity.mFinished) {
                activity.mCalled = false;
                if (r.isPersistable()) {
                    mInstrumentation.callActivityOnPostCreate(activity, r.state,
                            r.persistentState);
                } else {
                    mInstrumentation.callActivityOnPostCreate(activity, r.state);
                }
                if (!activity.mCalled) {
                    throw new SuperNotCalledException(
                        "Activity " + r.intent.getComponent().toShortString() +
                        " did not call through to super.onPostCreate()");
                }
            }
        }
        r.paused = true;

        mActivities.put(r.token, r);

    } catch (SuperNotCalledException e) {
        throw e;

    } catch (Exception e) {
        if (!mInstrumentation.onException(activity, e)) {
            throw new RuntimeException(
                "Unable to start activity " + component
                + ": " + e.toString(), e);
        }
    }

    return activity;
}
```
activity的attach方法
```
final void attach(Context context, ActivityThread aThread,
           Instrumentation instr, IBinder token, int ident,
           Application application, Intent intent, ActivityInfo info,
           CharSequence title, Activity parent, String id,
           NonConfigurationInstances lastNonConfigurationInstances,
           Configuration config, String referrer, IVoiceInteractor voiceInteractor,
           Window window, ActivityConfigCallback activityConfigCallback) {
       attachBaseContext(context);

       mFragments.attachHost(null /*parent*/);

       mWindow = new PhoneWindow(this, window, activityConfigCallback);
       mWindow.setWindowControllerCallback(this);
       mWindow.setCallback(this);
       mWindow.setOnWindowDismissedCallback(this);
       mWindow.getLayoutInflater().setPrivateFactory(this);
       if (info.softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_UNSPECIFIED) {
           mWindow.setSoftInputMode(info.softInputMode);
       }
       if (info.uiOptions != 0) {
           mWindow.setUiOptions(info.uiOptions);
       }
       mUiThread = Thread.currentThread();

       mMainThread = aThread;
       mInstrumentation = instr;
       mToken = token;
       mIdent = ident;
       mApplication = application;
       mIntent = intent;
       mReferrer = referrer;
       mComponent = intent.getComponent();
       mActivityInfo = info;
       mTitle = title;
       mParent = parent;
       mEmbeddedID = id;
       mLastNonConfigurationInstances = lastNonConfigurationInstances;
       if (voiceInteractor != null) {
           if (lastNonConfigurationInstances != null) {
               mVoiceInteractor = lastNonConfigurationInstances.voiceInteractor;
           } else {
               mVoiceInteractor = new VoiceInteractor(voiceInteractor, this, this,
                       Looper.myLooper());
           }
       }

       mWindow.setWindowManager(
               (WindowManager)context.getSystemService(Context.WINDOW_SERVICE),
               mToken, mComponent.flattenToString(),
               (info.flags & ActivityInfo.FLAG_HARDWARE_ACCELERATED) != 0);
       if (mParent != null) {
           mWindow.setContainer(mParent.getWindow());
       }
       mWindowManager = mWindow.getWindowManager();
       mCurrentConfig = config;

       mWindow.setColorMode(info.colorMode);
   }
```

在Activity的attach方法里面，系统会创建activity所属的window对象并未其设置回调接口，window对象的创建是PhoneWindow。由于activity实现了window的callback接口，因此，当window接收到外界状态改变时会回调activity的方法，Callback接口中方法很多，但是有几个确实我们非常熟悉的，比如onAttachToWindow、onDetachedFromWindow、dispatchTouchEvent等等，代码如下所示


从activityy的setContentView的实现可以看出，Activity将具体实现交给了Window来处理，而Window的具体实现是phoneWindow，所以只需要看phonewindow的关联逻辑即可，PhoneWindow的setContentView方法大致遵循如下几个步骤
1. 如果没有decorView就创建他
DecorView是一个FrameLayout。他是Activity中顶级的View，一般来说他包含内部标题栏和内部蓝，但是这个会随着主题的变化而改变，不管怎么样，内容栏是一定要存在的，并且内容栏固定具体的id，就是content，那么他完整的id是android.R.content。decorView的创建过程由installDecor方法来完成，在方法内部会通过generateDecor方法来直接创建DecorView，这个时候DecorView还是一个空白的FramLayout

看一下DecorView的构造方法
```
DecorView(Context context, int featureId, PhoneWindow window,
          WindowManager.LayoutParams params) {
      super(context);
      mFeatureId = featureId;

      mShowInterpolator = AnimationUtils.loadInterpolator(context,
              android.R.interpolator.linear_out_slow_in);
      mHideInterpolator = AnimationUtils.loadInterpolator(context,
              android.R.interpolator.fast_out_linear_in);

      mBarEnterExitDuration = context.getResources().getInteger(
              R.integer.dock_enter_exit_duration);
      mForceWindowDrawsStatusBarBackground = context.getResources().getBoolean(
              R.bool.config_forceWindowDrawsStatusBarBackground)
              && context.getApplicationInfo().targetSdkVersion >= N;
      mSemiTransparentStatusBarColor = context.getResources().getColor(
              R.color.system_bar_background_semi_transparent, null /* theme */);

      updateAvailableWidth();

      setWindow(window);

      updateLogTag(params);

      mResizeShadowSize = context.getResources().getDimensionPixelSize(
              R.dimen.resize_shadow_size);
      initResizingPaints();
  }

```

看一下phonewindow的setContentView这个方法

```
@Override
    public void setContentView(int layoutResID) {
        // Note: FEATURE_CONTENT_TRANSITIONS may be set in the process of installing the window
        // decor, when theme attributes and the like are crystalized. Do not check the feature
        // before this happens.
        if (mContentParent == null) {
            installDecor();
        } else if (!hasFeature(FEATURE_CONTENT_TRANSITIONS)) {
            mContentParent.removeAllViews();
        }

        if (hasFeature(FEATURE_CONTENT_TRANSITIONS)) {
            final Scene newScene = Scene.getSceneForLayout(mContentParent, layoutResID,
                    getContext());
            transitionTo(newScene);
        } else {
            mLayoutInflater.inflate(layoutResID, mContentParent);
        }
        mContentParent.requestApplyInsets();
        final Callback cb = getCallback();
        if (cb != null && !isDestroyed()) {
            cb.onContentChanged();
        }
        mContentParentExplicitlySet = true;
    }
```

看一下他的generateLayout方法和installDecor方法

decorView的结构和系统版本以及主题都有关系

### 将View添加到decoordView的mContentParent中
这里setContentView方法会inflate，这个时候activity的布局就加载到decorView中了,他只是添加到mContentParent中，因此叫做setContentView

### 回调Activity的onContentChanged方法通知Activity视图已经发生改变

这个过程就更简单了，由于Activity实现了Window的Callback接口，这里表示Activity的布局已经被添加到DecorView的mContentParent中了，使其可以做相应的处理。Activity的onContentChanged方法是个空的实现，我们可以在子Activity中处理这个回调
```
      final Callback cb = getCallback();
      if (cb != null && !isDestroyed()) {
          cb.onContentChanged();
      }
```
经过了上面的三个步骤，到这里为止，decorView已经被创建并初始化完毕，activity的布局文件已经成功添加到了DecorView的mContentParent中，但是这个时候DecorView还没有被WindowManager正式添加到Window，这里需要正确的理解window的抽象概念，window跟多表示一种抽象功能的集合，虽然说早在Activity的attach方法中window就已经被创建了，但是这个时候由于decorView没有被windowManager识别，所以这个时候window无法提供具体的功能，因为他还无法识别外界传出的输入信息，。在activityThread的handleResumeActivity方法中，首先会调用activity的onResume，接着会调用activity的makeVisible，正是在makeVisible方法，decorView真正完成了添加和显示的过程，到这里，Activitty视图才能被用户看到

```
void makeVisible() {
       if (!mWindowAdded) {
           ViewManager wm = getWindowManager();
           wm.addView(mDecor, getWindow().getAttributes());
           mWindowAdded = true;
       }
       mDecor.setVisibility(View.VISIBLE);
   }

```
### dialog的window创建过程


dialog的window的创建过程和activity类似，有如下几个步骤
1. 创建window
dialog中window的创建同样通过policyManager的makeNewWindow方法来完成，创建后的对象实际就是phoneWindow，这个过程和activity的window创建过程一致，这里就不详细说明了
2. 初始化decorView并将dialog视图添加到decorView中
初始化dialog，这个过程和activity类似
3. 将decorView添加到window中并显示
在dialog的show方法中，会通过windowmanager将decorView添加到window中显示

从上面三个步骤可以发现，dialog的window创建和activity的window创建过程很类似，二者几乎没有什么区别，当dialog被关闭是，他会通过windowmanager来移除decorView
普通的dialog有一个特殊之处就是必须采用activity的context，如果采用application的context，那么就会报错
提示如下
```
token null is not for an application
```
这里信息很明确，是没有应用token所导致的，而应用token一般只有activity拥有，所以这里只需要activity作为context来显示对话框，另外，系统window比较特殊，他可以不需要token，因此在上面的例子，只需要指定对话框的window为系统类型就可以正常弹出对话框，在本章一开始就讲到，windowmanager。layoutParams中的type表示window的类型，而系统window的层级范围是2000-2999，这些成绩范围就对应着type参数，系统window的层级有很多值3，对于本利来说，可以选用TYPE_SYSTEM_OVERLAY来指定对话框的类型为系统window类型

```
Dialog dialog
             =new Dialog(this.getApplicationContext());
     dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY);
```
然后别忘忘记了在androidManifest中声明权限从而可以使用系统window

```
<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW"/>
```


### Toast的window创建过程
toast和dialog不同，他的工作过程就稍微复杂一点，首先toast也是基于window来实现的，但是由于toast具有定时取消这一个功能，所以系统采用handler。在toast的内部由拉令中IPC过程，第一类是Toast访问NotificationManagerService，第二类是NotifacationManagerService回调Toast的TN接口，关于IPC的一些知识，可以参照之前的内容，这里将NotificationManagerService简称为NMS

Toast属于系统window，他的内部视图由两种方式指定，一种是系统默认样式，一种是通过setView方法来指定一个自定义View，不管怎么样，他们都对应Toast的一个View类型的内部成员mNextView。Toast提供了show和cancel方法分别用于显示和影藏Toast，他们的内部是一个IPC过程，show方法和cancel方法实现如下

```
public void show() {
       if (mNextView == null) {
           throw new RuntimeException("setView must have been called");
       }

       INotificationManager service = getService();
       String pkg = mContext.getOpPackageName();
       TN tn = mTN;
       tn.mNextView = mNextView;

       try {
           service.enqueueToast(pkg, tn, mDuration);
       } catch (RemoteException e) {
           // Empty
       }
   }


   static private INotificationManager getService() {
        if (sService != null) {
            return sService;
        }
        sService = INotificationManager.Stub.asInterface(ServiceManager.getService("notification"));
        return sService;
    }


   public void cancel() {
       mTN.cancel();
   }
```
从上面的代码可以看到，显示和隐藏都是通过NMS来实现的，由于NMS运行在系统进程中，所以只能通过远程调用来显示和隐藏Toast,看一下`getService`这个方法。需要注意的是TN这个类，他是一个Binder类，在Toast和NMS进行IPC过程中，当NMS处理TOast的显示或者隐藏的过程都会回调TN中的方法，这个时候由于TN运行在Binder线程池中，所以需要通过Handler将其切换到当前线程中，这里的当前线程是指发送Toast请求所在的线程。注意，由于这里使用handler，所以这意味着Toast无法在没有Looper的线程中弹出，这是因为Handler需要使用looper才能完成线程的切换功能，关于handler和looper的具体介绍可以看后面的内容

首先看一下Toast的显示过程，他调用了NMS的enqueueToast

可以看到在show的时候调用了

```
public void show() {
       if (mNextView == null) {
           throw new RuntimeException("setView must have been called");
       }

       INotificationManager service = getService();
       String pkg = mContext.getOpPackageName();
       TN tn = mTN;
       tn.mNextView = mNextView;

       try {
           service.enqueueToast(pkg, tn, mDuration);  调用了enqueueToast方法
       } catch (RemoteException e) {
           // Empty
       }
   }

```

NMS的enqueueToast方法的第一个参数表示当前应用的包名，第二个参数tn表示远程回调，第三个表示Toast显示的时长。enqueueToast首先将Toast请求封装为ToastRecord对象并将其添加到一个mToastQueue的队列中。mToastQueue其实是一个ArrayList。对于非系统应用来说，mToastQueue最多同时存在50个ToastRecord，这样所示为了防止DOS(Denial of Service)。如果不这么做，事项一下，如果我们通过大量循环去接连弹出Toast，那么其他应用就没有机会弹出Toast，那么对于其他应用的Toast请求，系统的行为就是拒绝服务，这就是拒绝服务攻击的含义，这种手段常用于网络攻击中。这里对每一个应用都进行了判断，每一个应用最多有50个

```
@Override
        public void enqueueToast(String pkg, ITransientNotification callback, int duration)
        {
            if (DBG) {
                Slog.i(TAG, "enqueueToast pkg=" + pkg + " callback=" + callback
                        + " duration=" + duration);
            }

            if (pkg == null || callback == null) {
                Slog.e(TAG, "Not doing toast. pkg=" + pkg + " callback=" + callback);
                return ;
            }

            final boolean isSystemToast = isCallerSystemOrPhone() || ("android".equals(pkg));
            final boolean isPackageSuspended =
                    isPackageSuspendedForUser(pkg, Binder.getCallingUid());

            if (ENABLE_BLOCKED_TOASTS && !isSystemToast &&
                    (!areNotificationsEnabledForPackage(pkg, Binder.getCallingUid())
                            || isPackageSuspended)) {
                Slog.e(TAG, "Suppressing toast from package " + pkg
                        + (isPackageSuspended
                                ? " due to package suspended by administrator."
                                : " by user request."));
                return;
            }

            synchronized (mToastQueue) {
                int callingPid = Binder.getCallingPid();
                long callingId = Binder.clearCallingIdentity();
                try {
                    ToastRecord record;
                    int index = indexOfToastLocked(pkg, callback);
                    // If it's already in the queue, we update it in place, we don't
                    // move it to the end of the queue.
                    if (index >= 0) {
                        record = mToastQueue.get(index);
                        record.update(duration);
                    } else {
                        //就是这里
                        // Limit the number of toasts that any given package except the android
                        // package can enqueue.  Prevents DOS attacks and deals with leaks.
                        if (!isSystemToast) {
                            int count = 0;
                            final int N = mToastQueue.size();
                            for (int i=0; i<N; i++) {
                                 final ToastRecord r = mToastQueue.get(i);
                                 if (r.pkg.equals(pkg)) {
                                     count++;
                                     if (count >= MAX_PACKAGE_NOTIFICATIONS) {// 判断是否大于50个
                                         Slog.e(TAG, "Package has already posted " + count
                                                + " toasts. Not showing more. Package=" + pkg);
                                         return;
                                     }
                                 }
                            }
                        }

                        Binder token = new Binder();
                        mWindowManagerInternal.addWindowToken(token, TYPE_TOAST, DEFAULT_DISPLAY);
                        record = new ToastRecord(callingPid, pkg, callback, duration, token);
                        mToastQueue.add(record);
                        index = mToastQueue.size() - 1;
                        keepProcessAliveIfNeededLocked(callingPid);
                    }
                    // If it's at index 0, it's the current toast.  It doesn't matter if it's
                    // new or just been updated.  Call back and tell it to show itself.
                    // If the callback fails, this will remove it from the list, so don't
                    // assume that it's valid after this.
                    if (index == 0) {
                        showNextToastLocked();
                    }
                } finally {
                    Binder.restoreCallingIdentity(callingId);
                }
            }
        }
```
正常情况下，一个应用不可能达到上限，当ToastRecord被添加到mToastQueue中后，NMS就会通过shwoNextToastLocked方法来显示当前的toast。下面的代码很好理解，需要注意的是，Toast的显示时由ToastRecord的Callback来完成的，这个callback实际上就是Toast中的TN对象的远程Binder，通过callBack来访问TN中的方法是需要跨进程来完成的，最终被调用的TN中的方法会运行在发起Toast请求的应用的Binder线程池中

```
void showNextToastLocked() {
        ToastRecord record = mToastQueue.get(0);
        while (record != null) {
            if (DBG) Slog.d(TAG, "Show pkg=" + record.pkg + " callback=" + record.callback);
            try {
                record.callback.show(record.token);
                // 这里是发送一条延时消息
                scheduleTimeoutLocked(record);
                return;
            } catch (RemoteException e) {
                Slog.w(TAG, "Object died trying to show notification " + record.callback
                        + " in package " + record.pkg);
                // remove it from the list and let the process die
                int index = mToastQueue.indexOf(record);
                if (index >= 0) {
                    mToastQueue.remove(index);
                }
                keepProcessAliveIfNeededLocked(record.pid);
                if (mToastQueue.size() > 0) {
                    record = mToastQueue.get(0);
                } else {
                    record = null;
                }
            }
        }
    }

```
延时消息
```
@GuardedBy("mToastQueue")
    private void scheduleTimeoutLocked(ToastRecord r)
    {
        mHandler.removeCallbacksAndMessages(r);
        Message m = Message.obtain(mHandler, MESSAGE_TIMEOUT, r);
        long delay = r.duration == Toast.LENGTH_LONG ? LONG_DELAY : SHORT_DELAY;
        mHandler.sendMessageDelayed(m, delay);
    }
```
上面的LONG_DELAY是3.5s，而SHORT_DELAY是2s。延迟相应的时间后，NMS会通过cancelToastLocked方法来隐藏Toast并将其从mToastQueue中移除，这个时候如果mToastQueue还有其他Toast，那么NMS就继续显示其他的Toast。
Toast的隐藏也是通过ToastRecord的callback来完成的，这同样越是一次IPC过程，他的工作方式和TOast的显示过程类似，如下所示。
```
@GuardedBy("mToastQueue")
 void cancelToastLocked(int index) {
     ToastRecord record = mToastQueue.get(index);
     try {
         record.callback.hide();
     } catch (RemoteException e) {
         Slog.w(TAG, "Object died trying to hide notification " + record.callback
                 + " in package " + record.pkg);
         // don't worry about this, we're about to remove it from
         // the list anyway
     }

     ToastRecord lastToast = mToastQueue.remove(index);
     mWindowManagerInternal.removeWindowToken(lastToast.token, true, DEFAULT_DISPLAY);

     keepProcessAliveIfNeededLocked(record.pid);
     if (mToastQueue.size() > 0) {
         // Show the next one. If the callback fails, this will remove
         // it from the list, so don't assume that the list hasn't changed
         // after this point.
         showNextToastLocked();
     }
 }
```
通过上面的分析可以知道，Toast的显示和隐藏过程其实是通过Toast中的TN这个类来实现的，他有两个方法show和hide，分别对应Toast的显示与隐藏。这两个方法是被NMS以跨进程的方式调用的，因此他们运行在Binder线程池中，为了将执行环境切换到Tooast所在请求所在的线程，在他们内部使用了handler,TN是Toast的一个内部类

```

mHandler = new Handler(looper, null) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case SHOW: {
                        IBinder token = (IBinder) msg.obj;
                        handleShow(token);
                        break;
                    }
                    case HIDE: {
                        handleHide();
                        // Don't do this in handleHide() because it is also invoked by
                        // handleShow()
                        mNextView = null;
                        break;
                    }
                    case CANCEL: {
                        handleHide();
                        // Don't do this in handleHide() because it is also invoked by
                        // handleShow()
                        mNextView = null;
                        try {
                            getService().cancelToast(mPackageName, TN.this);
                        } catch (RemoteException e) {
                        }
                        break;
                    }
                }
            }
        };


@Override
    public void show(IBinder windowToken) {
        if (localLOGV) Log.v(TAG, "SHOW: " + this);
        mHandler.obtainMessage(SHOW, windowToken).sendToTarget();
    }

    /**
     * schedule handleHide into the right thread
     */
    @Override
    public void hide() {
        if (localLOGV) Log.v(TAG, "HIDE: " + this);
        mHandler.obtainMessage(HIDE).sendToTarget();
    }
```
这里内部调用了handleShow和handleHide这里才是真正影藏和显示Toast的地方
添加handleShow会调用
```
   mWM.addView(mView, mParams);
```
添加handleHide会调用
```
   mWM.removeViewImmediate(mView);
```


这里Toast的Window的创建过程分析已经完成了。任何View都是依赖一个Window的，是附着在Window上面显示的。
# 第九章 四大组件的工作流程
这一章讲述一下四大组件的工作过程,说到四大组件，开发者都再熟悉不过了，他们是Activity、Service、BroadcastReceiver、ContentProvide。如何使用四大组件这个不是本章的话题，这个都是基础的内容。这里按照如下逻辑来分析Android的四大组件：首先对四大组件的运行状态和工作方式做一个概括化的描述，接着对四大组件的工作过程进行分析，通过本章节对四大组件有一个更加深刻的认识。

本章主要侧重于四大组件工作过程的分析，通过分析他们的工作过程我们可以更好的理解系统的运行机制。本章的意义在于加深读者对四大组件的工作方式的认识。本章的意义在于加深读者对四大组件工作方式的认识，由于四大组件的特殊性，我们有必要对他有一定的了解。

## 四大组件的运行状态
Android四大组件中处理BroadcaskReceiver意外，其他三种组件必须都在ANdroidManifest中注册，对于BroadcastReceiver来说，他既可以在AndroidManifest中注册，也可以使用代码来注册。在调用方式上，Activity、Service、BroadcastReceiver需要借助Intent，而ContentProvide不需要借助Intent

Activity是一种展示型的组件，用于向用户直接展示一个界面，并且可以接受用户的输入信息从而进行交互。Activity是最重要的一种组件，对用户来说Activity就是一个Android应用的全部，这是因为其他三大组件对用户来说都是不可感知的。Activity的启动由Intent触发，其中Intent可以分为显示Intent和隐式Intent，显示Intent可以明确指向一个Activity组件，隐式Intent则指向一个或者多个目标Activity组件，当然也可能没有任何一个Activity组件可以处理这个Intent。一个Activity组件可以具有特定的启动模式。关于启动模式在之前已经做了介绍了，同一个Activity组件在不同的启动模式下会有不同的效果。Activity组件也是可以停止的，在实际开发中可以通过Activity的finish方法来结束一个Activity组件的运行。由此看来，Activity组件的主要作用是展示一个界面并和用户交互，他扮演的是一种前台界面的角色。

Service是一种计算型组件，用于在后台执行一系列计算任务。由于Service组件工作在后台，因此用户无法直接感知到他的存在。Service组件和Activity组件略有不同，Activity组件只有一种运行模式，即Activity处于启动装填，但是Service组件却又两种状态：启动状态和绑定状态。当Service组件处于启动状态时，这个时候Service内部可以做一些后台计算，并不需要和外界有直接的交互。尽管Service组件是用于执行后台计算的，但是它本身是运行在主线程中的，因此耗时操作需要在独立的线程中执行，也可以使用IntentService，这个默认是在新建的线程中执行。当Service组件处于绑定状态时，这个时候Service内部同样可以进行后台计算，但是处于这种状态是，外界可以很方便的和Service组件进行通信。Service组件也可以停止的，停止一个Service组件比较复杂，需要灵活使用stopService和unBindService这两个方法才能完全停止一个Service组件。


BroadcastReceiver是一种消息性组件，用于在不同的组件乃至不同的应用之间传递消息。BroadcastReceiver同样无法被用户直接感知，因为他工作在系统内部。BroadcastReceiver也叫广播，广播的注册有两种方式：静态注册和动态注册。静态注册时指在AndroidManifest中注册管你胳膊，这种广播在应用安装的时候会被系统解析，因此这种形式的广播不需要启动就可以收到广播。动态注册广播需要通过Context.registerReceiver()来实现，并且不需要的时候可以通过Context.unregisterReceiver()来解除广播，此种形态的广播必须要应用启动之后才能注册并接受广播，应为应用不启动就无法注册广播，无法注册广播就无法收到相应的广播。在开发中通过Context的一系列send方法来发送广播，被发送的广播会被系统发送给该兴趣的接受者，发送和接受过程的匹配是通过<Intent-filter>来描述的。可以发现，BroadcastReceiver组件可以通过实现低耦合的观察者模式，观察者和接受者没有任何耦合。由于BroadcastReceiver的特性，他不适合用来执行耗时操作。BroadcastReceiver一般来说不需要停止，动态注册的要及时解开注册

ContentProvider是一种共享性组件，用于向其他组件内置其他应用共享数据。和BroadcastReceiver一样，ContentProvider无法被用户感知。对于一个ContentProvider组件来说，他的内部需要实现增删改查这四种操作，在他的内部维持着一份数据集合，这个数据集合既可以通过数据库来实现，可以通过其他任意类型来实现，比如List和Map。ContentProvider对数据集合的具体实现方式没有任何要求。需要注意的是ContentProvider内部的insert、delete。update和query方法需要处理好线程同步，因此这几个方法在Binder线程池汇总被调用，另外ContentProvider组件也不需要手动停止。

## Activity的工作流程
本节讲述的内容是Activity的工作过程。为了方便日常的开发工作，系统对四大组件的工作过程进行了很大程度上的封装，这使得开发者无需关注实现细节就可以快速的使用四大组件。Activity作为很重要的一个组件，其内部工作过程当然是做了很多的封装，这种封装是的启动一个Activity变得异常简单。在显示调用形式下，只需要调用如下代码就可以完成

```
Intent intent =new Intent(this,Main2Activity.class);
 startActivity(intent);
```

通过上面的代码即可启动一个具体的Activity，然后新Activity就会被系统启动并展示在用户的眼前。这个过程对于Android开发者来说在普通不过了，但是有没有想过系统内部是如何启动一个Activity的呢？比如新Activity是在什么时候创建？Activity的onCreate方法又是在什么时候被系统回调的呢？这里就是普通开发者到高级开发者再到架构师的必经之路了。从另外一个方面来说，Android作为一个优秀的基于Linux的移动操作系统，其内部一定有很多地方值得我们学习和借鉴的地方，因此了解的过程就是学习Android操作系统。通过对Android操作系统的学习可以提高我们对操作系统在技术实现上的理解，这对于加强开发人员的内功是有帮助的。但是有一点，由于Android的内部实现多数细节都比较复杂，在研究内部实现上来说更加侧重对整体流程的把握，而不能局限在代码细节而无法自拔。

本章节主要分析Activity的启动过程，通过本章节，可以对Activity的启动过程有一个感性的认识。

我们从startActivity开始，发现最终调用了startActivityForResult方法，使用startActivity的requestCode是-1
```
public void startActivityForResult(@RequiresPermission Intent intent, int requestCode,
        @Nullable Bundle options) {
    if (mParent == null) {
        options = transferSpringboardActivityOptions(options);
        Instrumentation.ActivityResult ar =
            mInstrumentation.execStartActivity(
                this, mMainThread.getApplicationThread(), mToken, this,
                intent, requestCode, options);
        if (ar != null) {
            mMainThread.sendActivityResult(
                mToken, mEmbeddedID, requestCode, ar.getResultCode(),
                ar.getResultData());
        }
        if (requestCode >= 0) {
            // If this start is requesting a result, we can avoid making
            // the activity visible until the result is received.  Setting
            // this code during onCreate(Bundle savedInstanceState) or onResume() will keep the
            // activity hidden during this time, to avoid flickering.
            // This can only be done when a result is requested because
            // that guarantees we will get information back when the
            // activity is finished, no matter what happens to it.
            mStartedActivity = true;
        }

        cancelInputsAndStartExitTransition(options);
        // TODO Consider clearing/flushing other event sources and events for child windows.
    } else {
        if (options != null) {
            mParent.startActivityFromChild(this, intent, requestCode, options);
        } else {
            // Note we want to go through this method for compatibility with
            // existing applications that may have overridden it.
            mParent.startActivityFromChild(this, intent, requestCode);
        }
    }
}
```
注意一下，在第一次启动的时候，mParent是空，然后会调用
```
Instrumentation.ActivityResult ar =
              mInstrumentation.execStartActivity(
                  this, mMainThread.getApplicationThread(), mToken, this,
                  intent, requestCode, options);
```
要知道这个方法的作用，就要看一下Instrumentation的execStartActivity做了什么操作，首先看一下穿的的参数，mMainThread.getApplicationThread()这个方法获取的类型是ApplicationThread，这个是ActiviityThread的一个内部类，通过分析发现，ApplicationThread和ActiviityThread在启动Activity的过程中发生了很重要的作用


```
public ActivityResult execStartActivity(
           Context who, IBinder contextThread, IBinder token, Activity target,
           Intent intent, int requestCode, Bundle options) {
       IApplicationThread whoThread = (IApplicationThread) contextThread;
       Uri referrer = target != null ? target.onProvideReferrer() : null;
       if (referrer != null) {
           intent.putExtra(Intent.EXTRA_REFERRER, referrer);
       }
       if (mActivityMonitors != null) {
           synchronized (mSync) {
               final int N = mActivityMonitors.size();
               for (int i=0; i<N; i++) {
                   final ActivityMonitor am = mActivityMonitors.get(i);
                   ActivityResult result = null;
                   if (am.ignoreMatchingSpecificIntents()) {
                       result = am.onStartActivity(intent);
                   }
                   if (result != null) {
                       am.mHits++;
                       return result;
                   } else if (am.match(who, null, intent)) {
                       am.mHits++;
                       if (am.isBlocking()) {
                           return requestCode >= 0 ? am.getResult() : null;
                       }
                       break;
                   }
               }
           }
       }
       try {
           intent.migrateExtraStreamToClipData();
           intent.prepareToLeaveProcess(who);
           int result = ActivityManager.getService()
               .startActivity(whoThread, who.getBasePackageName(), intent,
                       intent.resolveTypeIfNeeded(who.getContentResolver()),
                       token, target != null ? target.mEmbeddedID : null,
                       requestCode, 0, null, options);
           checkStartActivityResult(result, intent);
       } catch (RemoteException e) {
           throw new RuntimeException("Failure from system", e);
       }
       return null;
   }
```

这里分析一下这个方法,debug跟踪activity的启动，发现最后调用了这些代码
```
intent.migrateExtraStreamToClipData();
    intent.prepareToLeaveProcess(who);
    int result = ActivityManager.getService()
        .startActivity(whoThread, who.getBasePackageName(), intent,
                intent.resolveTypeIfNeeded(who.getContentResolver()),
                token, target != null ? target.mEmbeddedID : null,
                requestCode, 0, null, options);
    checkStartActivityResult(result, intent);
```
发现这里使用了ActivityManagerService(下面简称为AMS)，在得到AMS之后，调用AMS的startActivity的方法。
这里的getService方法获取IActivityManager这个Binder接口,因此AMS也是一个Binder，他是IActivityManager的具体实现。可以发现AMS这个Binder对象采用单例模式对外提供，SIngleton是一个单例封装类，第一次调用他会通过create方法初始化AMS这个Binder对象，在后续的调用过程中则返回之前创建的对象


```

public static IActivityManager getService() {
       return IActivityManagerSingleton.get();
   }


private static final Singleton<IActivityManager> IActivityManagerSingleton =
          new Singleton<IActivityManager>() {
              @Override
              protected IActivityManager create() {
                  final IBinder b = ServiceManager.getService(Context.ACTIVITY_SERVICE);
                  final IActivityManager am = IActivityManager.Stub.asInterface(b);
                  return am;
              }
          };


public abstract class Singleton<T> {
              private T mInstance;

              protected abstract T create();

              public final T get() {
                  synchronized (this) {
                      if (mInstance == null) {
                          mInstance = create();
                      }
                      return mInstance;
                  }
              }
      }   




```

ServiceManager的getService方法
```
public static IBinder getService(String name) {
      try {
          IBinder service = sCache.get(name);
          if (service != null) {
              return service;
          } else {
              return Binder.allowBlocking(getIServiceManager().getService(name));
          }
      } catch (RemoteException e) {
          Log.e(TAG, "error in getService", e);
      }
      return null;
  }
```

这里先来看一下Instrumentation的checkStartActivityResult方法

```
public static void checkStartActivityResult(int res, Object intent) {
    if (!ActivityManager.isStartResultFatalError(res)) {
        return;
    }

    switch (res) {
        case ActivityManager.START_INTENT_NOT_RESOLVED:
        case ActivityManager.START_CLASS_NOT_FOUND:
            if (intent instanceof Intent && ((Intent)intent).getComponent() != null)
                throw new ActivityNotFoundException(
                        "Unable to find explicit activity class "
                        + ((Intent)intent).getComponent().toShortString()
                        + "; have you declared this activity in your AndroidManifest.xml?");
            throw new ActivityNotFoundException(
                    "No Activity found to handle " + intent);
        case ActivityManager.START_PERMISSION_DENIED:
            throw new SecurityException("Not allowed to start activity "
                    + intent);
        case ActivityManager.START_FORWARD_AND_REQUEST_CONFLICT:
            throw new AndroidRuntimeException(
                    "FORWARD_RESULT_FLAG used while also requesting a result");
        case ActivityManager.START_NOT_ACTIVITY:
            throw new IllegalArgumentException(
                    "PendingIntent is not an activity");
        case ActivityManager.START_NOT_VOICE_COMPATIBLE:
            throw new SecurityException(
                    "Starting under voice control not allowed for: " + intent);
        case ActivityManager.START_VOICE_NOT_ACTIVE_SESSION:
            throw new IllegalStateException(
                    "Session calling startVoiceActivity does not match active session");
        case ActivityManager.START_VOICE_HIDDEN_SESSION:
            throw new IllegalStateException(
                    "Cannot start voice activity on a hidden session");
        case ActivityManager.START_ASSISTANT_NOT_ACTIVE_SESSION:
            throw new IllegalStateException(
                    "Session calling startAssistantActivity does not match active session");
        case ActivityManager.START_ASSISTANT_HIDDEN_SESSION:
            throw new IllegalStateException(
                    "Cannot start assistant activity on a hidden session");
        case ActivityManager.START_CANCELED:
            throw new AndroidRuntimeException("Activity could not be started for "
                    + intent);
        default:
            throw new AndroidRuntimeException("Unknown error code "
                    + res + " when starting " + intent);
    }
}

```
可以看到他是检查activity是否启动成功的，当无法启动一个activity是，这个方法就会抛出异常，这里看一下我们经常看到的错误
```
 have you declared this activity in your AndroidManifest.xml
```
activity没有在Manifest.xml注册

这里继续分析startActivity方法，是在AMS中
```
@Override
  public final int startActivity(IApplicationThread caller, String callingPackage,
          Intent intent, String resolvedType, IBinder resultTo, String resultWho, int requestCode,
          int startFlags, ProfilerInfo profilerInfo, Bundle bOptions) {
      return startActivityAsUser(caller, callingPackage, intent, resolvedType, resultTo,
              resultWho, requestCode, startFlags, profilerInfo, bOptions,
              UserHandle.getCallingUserId());
  }
```

可以看到他其实是调用了startActivityAsUser这个方法

```
@Override
public final int startActivityAsUser(IApplicationThread caller, String callingPackage,
        Intent intent, String resolvedType, IBinder resultTo, String resultWho, int requestCode,
        int startFlags, ProfilerInfo profilerInfo, Bundle bOptions, int userId) {
    enforceNotIsolatedCaller("startActivity");
    userId = mUserController.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(),
            userId, false, ALLOW_FULL_ONLY, "startActivity", null);
    // TODO: Switch to user app stacks here.
    return mActivityStarter.startActivityMayWait(caller, -1, callingPackage, intent,
            resolvedType, null, null, resultTo, resultWho, requestCode, startFlags,
            profilerInfo, null, null, bOptions, false, userId, null, null,
            "startActivityAsUser");
}

```
看到这里又进行了转移,转移到了ActivityStarter的`startActivityMayWait`方法,这个方法比较长，这里看一下他关键的方法
```
int res = startActivityLocked(caller, intent, ephemeralIntent, resolvedType,
               aInfo, rInfo, voiceSession, voiceInteractor,
               resultTo, resultWho, requestCode, callingPid,
               callingUid, callingPackage, realCallingPid, realCallingUid, startFlags,
               options, ignoreTargetSecurity, componentSpecified, outRecord, container,
               inTask, reason);
```

在进行了对activity启动的一系列判断之后会调用`startActivityLocked`这个方法来启动activity,看一下这个方法

```
int startActivityLocked(IApplicationThread caller, Intent intent, Intent ephemeralIntent,
            String resolvedType, ActivityInfo aInfo, ResolveInfo rInfo,
            IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor,
            IBinder resultTo, String resultWho, int requestCode, int callingPid, int callingUid,
            String callingPackage, int realCallingPid, int realCallingUid, int startFlags,
            ActivityOptions options, boolean ignoreTargetSecurity, boolean componentSpecified,
            ActivityRecord[] outActivity, ActivityStackSupervisor.ActivityContainer container,
            TaskRecord inTask, String reason) {

        if (TextUtils.isEmpty(reason)) {
            throw new IllegalArgumentException("Need to specify a reason.");
        }
        mLastStartReason = reason;
        mLastStartActivityTimeMs = System.currentTimeMillis();
        mLastStartActivityRecord[0] = null;

        mLastStartActivityResult = startActivity(caller, intent, ephemeralIntent, resolvedType,
                aInfo, rInfo, voiceSession, voiceInteractor, resultTo, resultWho, requestCode,
                callingPid, callingUid, callingPackage, realCallingPid, realCallingUid, startFlags,
                options, ignoreTargetSecurity, componentSpecified, mLastStartActivityRecord,
                container, inTask);

        if (outActivity != null) {
            // mLastStartActivityRecord[0] is set in the call to startActivity above.
            outActivity[0] = mLastStartActivityRecord[0];
        }
        return mLastStartActivityResult;
    }

```

这里又调用了startActivity方法，这个方法比较长 ，找一下关键代码
看到有一次调用了startActivity的重载方法

```
private int startActivity(final ActivityRecord r, ActivityRecord sourceRecord,
          IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor,
          int startFlags, boolean doResume, ActivityOptions options, TaskRecord inTask,
          ActivityRecord[] outActivity) {
      int result = START_CANCELED;
      try {
          mService.mWindowManager.deferSurfaceLayout();
          result = startActivityUnchecked(r, sourceRecord, voiceSession, voiceInteractor,
                  startFlags, doResume, options, inTask, outActivity);
      } finally {
          // If we are not able to proceed, disassociate the activity from the task. Leaving an
          // activity in an incomplete state can lead to issues, such as performing operations
          // without a window container.
          if (!ActivityManager.isStartResultSuccessful(result)
                  && mStartActivity.getTask() != null) {
              mStartActivity.getTask().removeActivity(mStartActivity);
          }
          mService.mWindowManager.continueSurfaceLayout();
      }

      postStartActivityProcessing(r, result, mSupervisor.getLastStack().mStackId,  mSourceRecord,
              mTargetStack);

      return result;
  }

```
这里调用了startActivityUnchecked这个方法,我们继续跟踪，可以看到调用了mSupervisor的 `resumeFocusedStackTopActivityLocked`这个方法


```
if (mDoResume) {
                 mSupervisor.resumeFocusedStackTopActivityLocked();
             }
```
mDoResume这个一定是true

mSupervisor的resumeFocusedStackTopActivityLocked方法会调用这个方法
```
boolean resumeFocusedStackTopActivityLocked(
        ActivityStack targetStack, ActivityRecord target, ActivityOptions targetOptions) {
    if (targetStack != null && isFocusedStack(targetStack)) {
        return targetStack.resumeTopActivityUncheckedLocked(target, targetOptions);
    }
    final ActivityRecord r = mFocusedStack.topRunningActivityLocked();
    if (r == null || r.state != RESUMED) {
        mFocusedStack.resumeTopActivityUncheckedLocked(null, null);
    } else if (r.state == RESUMED) {
        // Kick off any lingering app transitions form the MoveTaskToFront operation.
        mFocusedStack.executeAppTransition(targetOptions);
    }
    return false;
}

```

这个时候又会调用mFocusedStack的`resumeTopActivityUncheckedLocked`方法


```
boolean resumeTopActivityUncheckedLocked(ActivityRecord prev, ActivityOptions options) {
     if (mStackSupervisor.inResumeTopActivity) {
         // Don't even start recursing.
         return false;
     }

     boolean result = false;
     try {
         // Protect against recursion.
         mStackSupervisor.inResumeTopActivity = true;
         result = resumeTopActivityInnerLocked(prev, options);
     } finally {
         mStackSupervisor.inResumeTopActivity = false;
     }
     // When resuming the top activity, it may be necessary to pause the top activity (for
     // example, returning to the lock screen. We suppress the normal pause logic in
     // {@link #resumeTopActivityUncheckedLocked}, since the top activity is resumed at the end.
     // We call the {@link ActivityStackSupervisor#checkReadyForSleepLocked} again here to ensure
     // any necessary pause logic occurs.
     mStackSupervisor.checkReadyForSleepLocked();

     return result;
 }

```

这个时候可以看到会调用resumeTopActivityInnerLocked方法,这里会调用mStackSupervisor的`startSpecificActivityLocked`
```
              mStackSupervisor.startSpecificActivityLocked(next, true, false);
```
看一下startSpecificActivityLocked方法

```
void startSpecificActivityLocked(ActivityRecord r,
         boolean andResume, boolean checkConfig) {
     // Is this activity's application already running?
     ProcessRecord app = mService.getProcessRecordLocked(r.processName,
             r.info.applicationInfo.uid, true);

     r.getStack().setLaunchTime(r);

     if (app != null && app.thread != null) {
         try {
             if ((r.info.flags&ActivityInfo.FLAG_MULTIPROCESS) == 0
                     || !"android".equals(r.info.packageName)) {
                 // Don't add this if it is a platform component that is marked
                 // to run in multiple processes, because this is actually
                 // part of the framework so doesn't make sense to track as a
                 // separate apk in the process.
                 app.addPackage(r.info.packageName, r.info.applicationInfo.versionCode,
                         mService.mProcessStats);
             }
             realStartActivityLocked(r, app, andResume, checkConfig);
             return;
         } catch (RemoteException e) {
             Slog.w(TAG, "Exception when starting activity "
                     + r.intent.getComponent().flattenToShortString(), e);
         }

         // If a dead object exception was thrown -- fall through to
         // restart the application.
     }

     mService.startProcessLocked(r.processName, r.info.applicationInfo, true, 0,
             "activity", r.intent.getComponent(), false, false, true);
 }
```

这里会调用 **真正启动Activity** 的方法`realStartActivityLocked`
这里会调用这个方法
```
app.thread.scheduleLaunchActivity(new Intent(r.intent), r.appToken,
                System.identityHashCode(r), r.info,
                // TODO: Have this take the merged configuration instead of separate global and
                // override configs.
                mergedConfiguration.getGlobalConfiguration(),
                mergedConfiguration.getOverrideConfiguration(), r.compat,
                r.launchedFromPackage, task.voiceInteractor, app.repProcState, r.icicle,
                r.persistentState, results, newIntents, !andResume,
                mService.isNextTransitionForward(), profilerInfo);
```

这段代码很重要，其中app.thread的类型是IAppLicationThread,他的声明如下

```
public interface IApplicationThread extends IInterface {}
```
熟悉AIDL的都知道这个是一个Binder类型的接口。从IApplicationThread声明的接口方法可以看出，其内部包含了大量启动、停止Activity的接口，此外还包含了启动和停止服务的接口。从接口方法的命名可以猜测，这个类接口实现者完成了大量和Activity以及Service相关的启动功能，事实却是如此
IApplicationThread他的具体实现是什么，是ApplicationThread，这个是ActivityThread中的匿名内部类

```
    private class ApplicationThread extends IApplicationThread.Stub {}
```

这里可以发现IApplicationThread是Aidl自动生成的代码
绕了一大圈发现这里回到ApplicationThread并且使用了scheduleLaunchActivity这个方法

```
public final void scheduleLaunchActivity(Intent intent, IBinder token, int ident,
             ActivityInfo info, Configuration curConfig, Configuration overrideConfig,
             CompatibilityInfo compatInfo, String referrer, IVoiceInteractor voiceInteractor,
             int procState, Bundle state, PersistableBundle persistentState,
             List<ResultInfo> pendingResults, List<ReferrerIntent> pendingNewIntents,
             boolean notResumed, boolean isForward, ProfilerInfo profilerInfo) {

         updateProcessState(procState, false);

         ActivityClientRecord r = new ActivityClientRecord();

         r.token = token;
         r.ident = ident;
         r.intent = intent;
         r.referrer = referrer;
         r.voiceInteractor = voiceInteractor;
         r.activityInfo = info;
         r.compatInfo = compatInfo;
         r.state = state;
         r.persistentState = persistentState;

         r.pendingResults = pendingResults;
         r.pendingIntents = pendingNewIntents;

         r.startsNotResumed = notResumed;
         r.isForward = isForward;

         r.profilerInfo = profilerInfo;

         r.overrideConfig = overrideConfig;
         updatePendingConfiguration(curConfig);

         sendMessage(H.LAUNCH_ACTIVITY, r);
     }
```

这里只是使用Hadler处理Activity的启动消息

看一下这个Handler的处理
```
  private class H extends Handler {
public void handleMessage(Message msg) {
    if (DEBUG_MESSAGES) Slog.v(TAG, ">>> handling: " + codeToString(msg.what));
    switch (msg.what) {
        case LAUNCH_ACTIVITY: {
            Trace.traceBegin(Trace.TRACE_TAG_ACTIVITY_MANAGER, "activityStart");
            final ActivityClientRecord r = (ActivityClientRecord) msg.obj;

            r.packageInfo = getPackageInfoNoCheck(
                    r.activityInfo.applicationInfo, r.compatInfo);
            handleLaunchActivity(r, null, "LAUNCH_ACTIVITY");
            Trace.traceEnd(Trace.TRACE_TAG_ACTIVITY_MANAGER);
        } break;
        case RELAUNCH_ACTIVITY: {
            Trace.traceBegin(Trace.TRACE_TAG_ACTIVITY_MANAGER, "activityRestart");
            ActivityClientRecord r = (ActivityClientRecord)msg.obj;
            handleRelaunchActivity(r);
            Trace.traceEnd(Trace.TRACE_TAG_ACTIVITY_MANAGER);
        } break;
        case PAUSE_ACTIVITY: {
            Trace.traceBegin(Trace.TRACE_TAG_ACTIVITY_MANAGER, "activityPause");
            SomeArgs args = (SomeArgs) msg.obj;
            handlePauseActivity((IBinder) args.arg1, false,
                    (args.argi1 & USER_LEAVING) != 0, args.argi2,
                    (args.argi1 & DONT_REPORT) != 0, args.argi3);
            maybeSnapshot();
            Trace.traceEnd(Trace.TRACE_TAG_ACTIVITY_MANAGER);
        } break;
        case PAUSE_ACTIVITY_FINISHING: {
            Trace.traceBegin(Trace.TRACE_TAG_ACTIVITY_MANAGER, "activityPause");
            SomeArgs args = (SomeArgs) msg.obj;
            handlePauseActivity((IBinder) args.arg1, true, (args.argi1 & USER_LEAVING) != 0,
                    args.argi2, (args.argi1 & DONT_REPORT) != 0, args.argi3);
            Trace.traceEnd(Trace.TRACE_TAG_ACTIVITY_MANAGER);
        } break;
        case STOP_ACTIVITY_SHOW: {
            Trace.traceBegin(Trace.TRACE_TAG_ACTIVITY_MANAGER, "activityStop");
            SomeArgs args = (SomeArgs) msg.obj;
            handleStopActivity((IBinder) args.arg1, true, args.argi2, args.argi3);
            Trace.traceEnd(Trace.TRACE_TAG_ACTIVITY_MANAGER);
        } break;
        case STOP_ACTIVITY_HIDE: {
            Trace.traceBegin(Trace.TRACE_TAG_ACTIVITY_MANAGER, "activityStop");
            SomeArgs args = (SomeArgs) msg.obj;
            handleStopActivity((IBinder) args.arg1, false, args.argi2, args.argi3);
            Trace.traceEnd(Trace.TRACE_TAG_ACTIVITY_MANAGER);
        } break;
        case SHOW_WINDOW:
            Trace.traceBegin(Trace.TRACE_TAG_ACTIVITY_MANAGER, "activityShowWindow");
            handleWindowVisibility((IBinder)msg.obj, true);
            Trace.traceEnd(Trace.TRACE_TAG_ACTIVITY_MANAGER);
            break;
        case HIDE_WINDOW:
            Trace.traceBegin(Trace.TRACE_TAG_ACTIVITY_MANAGER, "activityHideWindow");
            handleWindowVisibility((IBinder)msg.obj, false);
            Trace.traceEnd(Trace.TRACE_TAG_ACTIVITY_MANAGER);
            break;
        case RESUME_ACTIVITY:
            Trace.traceBegin(Trace.TRACE_TAG_ACTIVITY_MANAGER, "activityResume");
            SomeArgs args = (SomeArgs) msg.obj;
            handleResumeActivity((IBinder) args.arg1, true, args.argi1 != 0, true,
                    args.argi3, "RESUME_ACTIVITY");
            Trace.traceEnd(Trace.TRACE_TAG_ACTIVITY_MANAGER);
            break;
        case SEND_RESULT:
            Trace.traceBegin(Trace.TRACE_TAG_ACTIVITY_MANAGER, "activityDeliverResult");
            handleSendResult((ResultData)msg.obj);
            Trace.traceEnd(Trace.TRACE_TAG_ACTIVITY_MANAGER);
            break;
        case DESTROY_ACTIVITY:
            Trace.traceBegin(Trace.TRACE_TAG_ACTIVITY_MANAGER, "activityDestroy");
            handleDestroyActivity((IBinder)msg.obj, msg.arg1 != 0,
                    msg.arg2, false);
            Trace.traceEnd(Trace.TRACE_TAG_ACTIVITY_MANAGER);
            break;
        case BIND_APPLICATION:
            Trace.traceBegin(Trace.TRACE_TAG_ACTIVITY_MANAGER, "bindApplication");
            AppBindData data = (AppBindData)msg.obj;
            handleBindApplication(data);
            Trace.traceEnd(Trace.TRACE_TAG_ACTIVITY_MANAGER);
            break;
        case EXIT_APPLICATION:
            if (mInitialApplication != null) {
                mInitialApplication.onTerminate();
            }
            Looper.myLooper().quit();
            break;
        case NEW_INTENT:
            Trace.traceBegin(Trace.TRACE_TAG_ACTIVITY_MANAGER, "activityNewIntent");
            handleNewIntent((NewIntentData)msg.obj);
            Trace.traceEnd(Trace.TRACE_TAG_ACTIVITY_MANAGER);
            break;
        case RECEIVER:
            Trace.traceBegin(Trace.TRACE_TAG_ACTIVITY_MANAGER, "broadcastReceiveComp");
            handleReceiver((ReceiverData)msg.obj);
            maybeSnapshot();
            Trace.traceEnd(Trace.TRACE_TAG_ACTIVITY_MANAGER);
            break;
        case CREATE_SERVICE:
            Trace.traceBegin(Trace.TRACE_TAG_ACTIVITY_MANAGER, ("serviceCreate: " + String.valueOf(msg.obj)));
            handleCreateService((CreateServiceData)msg.obj);
            Trace.traceEnd(Trace.TRACE_TAG_ACTIVITY_MANAGER);
            break;
        case BIND_SERVICE:
            Trace.traceBegin(Trace.TRACE_TAG_ACTIVITY_MANAGER, "serviceBind");
            handleBindService((BindServiceData)msg.obj);
            Trace.traceEnd(Trace.TRACE_TAG_ACTIVITY_MANAGER);
            break;
        case UNBIND_SERVICE:
            Trace.traceBegin(Trace.TRACE_TAG_ACTIVITY_MANAGER, "serviceUnbind");
            handleUnbindService((BindServiceData)msg.obj);
            Trace.traceEnd(Trace.TRACE_TAG_ACTIVITY_MANAGER);
            break;
        case SERVICE_ARGS:
            Trace.traceBegin(Trace.TRACE_TAG_ACTIVITY_MANAGER, ("serviceStart: " + String.valueOf(msg.obj)));
            handleServiceArgs((ServiceArgsData)msg.obj);
            Trace.traceEnd(Trace.TRACE_TAG_ACTIVITY_MANAGER);
            break;
        case STOP_SERVICE:
            Trace.traceBegin(Trace.TRACE_TAG_ACTIVITY_MANAGER, "serviceStop");
            handleStopService((IBinder)msg.obj);
            maybeSnapshot();
            Trace.traceEnd(Trace.TRACE_TAG_ACTIVITY_MANAGER);
            break;
        case CONFIGURATION_CHANGED:
            Trace.traceBegin(Trace.TRACE_TAG_ACTIVITY_MANAGER, "configChanged");
            mCurDefaultDisplayDpi = ((Configuration)msg.obj).densityDpi;
            mUpdatingSystemConfig = true;
            try {
                handleConfigurationChanged((Configuration) msg.obj, null);
            } finally {
                mUpdatingSystemConfig = false;
            }
            Trace.traceEnd(Trace.TRACE_TAG_ACTIVITY_MANAGER);
            break;
        case CLEAN_UP_CONTEXT:
            ContextCleanupInfo cci = (ContextCleanupInfo)msg.obj;
            cci.context.performFinalCleanup(cci.who, cci.what);
            break;
        case GC_WHEN_IDLE:
            scheduleGcIdler();
            break;
        case DUMP_SERVICE:
            handleDumpService((DumpComponentInfo)msg.obj);
            break;
        case LOW_MEMORY:
            Trace.traceBegin(Trace.TRACE_TAG_ACTIVITY_MANAGER, "lowMemory");
            handleLowMemory();
            Trace.traceEnd(Trace.TRACE_TAG_ACTIVITY_MANAGER);
            break;
        case ACTIVITY_CONFIGURATION_CHANGED:
            Trace.traceBegin(Trace.TRACE_TAG_ACTIVITY_MANAGER, "activityConfigChanged");
            handleActivityConfigurationChanged((ActivityConfigChangeData) msg.obj,
                    INVALID_DISPLAY);
            Trace.traceEnd(Trace.TRACE_TAG_ACTIVITY_MANAGER);
            break;
        case ACTIVITY_MOVED_TO_DISPLAY:
            Trace.traceBegin(Trace.TRACE_TAG_ACTIVITY_MANAGER, "activityMovedToDisplay");
            handleActivityConfigurationChanged((ActivityConfigChangeData) msg.obj,
                    msg.arg1 /* displayId */);
            Trace.traceEnd(Trace.TRACE_TAG_ACTIVITY_MANAGER);
            break;
        case PROFILER_CONTROL:
            handleProfilerControl(msg.arg1 != 0, (ProfilerInfo)msg.obj, msg.arg2);
            break;
        case CREATE_BACKUP_AGENT:
            Trace.traceBegin(Trace.TRACE_TAG_ACTIVITY_MANAGER, "backupCreateAgent");
            handleCreateBackupAgent((CreateBackupAgentData)msg.obj);
            Trace.traceEnd(Trace.TRACE_TAG_ACTIVITY_MANAGER);
            break;
        case DESTROY_BACKUP_AGENT:
            Trace.traceBegin(Trace.TRACE_TAG_ACTIVITY_MANAGER, "backupDestroyAgent");
            handleDestroyBackupAgent((CreateBackupAgentData)msg.obj);
            Trace.traceEnd(Trace.TRACE_TAG_ACTIVITY_MANAGER);
            break;
        case SUICIDE:
            Process.killProcess(Process.myPid());
            break;
        case REMOVE_PROVIDER:
            Trace.traceBegin(Trace.TRACE_TAG_ACTIVITY_MANAGER, "providerRemove");
            completeRemoveProvider((ProviderRefCount)msg.obj);
            Trace.traceEnd(Trace.TRACE_TAG_ACTIVITY_MANAGER);
            break;
        case ENABLE_JIT:
            ensureJitEnabled();
            break;
        case DISPATCH_PACKAGE_BROADCAST:
            Trace.traceBegin(Trace.TRACE_TAG_ACTIVITY_MANAGER, "broadcastPackage");
            handleDispatchPackageBroadcast(msg.arg1, (String[])msg.obj);
            Trace.traceEnd(Trace.TRACE_TAG_ACTIVITY_MANAGER);
            break;
        case SCHEDULE_CRASH:
            throw new RemoteServiceException((String)msg.obj);
        case DUMP_HEAP:
            handleDumpHeap(msg.arg1 != 0, (DumpHeapData)msg.obj);
            break;
        case DUMP_ACTIVITY:
            handleDumpActivity((DumpComponentInfo)msg.obj);
            break;
        case DUMP_PROVIDER:
            handleDumpProvider((DumpComponentInfo)msg.obj);
            break;
        case SLEEPING:
            Trace.traceBegin(Trace.TRACE_TAG_ACTIVITY_MANAGER, "sleeping");
            handleSleeping((IBinder)msg.obj, msg.arg1 != 0);
            Trace.traceEnd(Trace.TRACE_TAG_ACTIVITY_MANAGER);
            break;
        case SET_CORE_SETTINGS:
            Trace.traceBegin(Trace.TRACE_TAG_ACTIVITY_MANAGER, "setCoreSettings");
            handleSetCoreSettings((Bundle) msg.obj);
            Trace.traceEnd(Trace.TRACE_TAG_ACTIVITY_MANAGER);
            break;
        case UPDATE_PACKAGE_COMPATIBILITY_INFO:
            handleUpdatePackageCompatibilityInfo((UpdateCompatibilityData)msg.obj);
            break;
        case TRIM_MEMORY:
            Trace.traceBegin(Trace.TRACE_TAG_ACTIVITY_MANAGER, "trimMemory");
            handleTrimMemory(msg.arg1);
            Trace.traceEnd(Trace.TRACE_TAG_ACTIVITY_MANAGER);
            break;
        case UNSTABLE_PROVIDER_DIED:
            handleUnstableProviderDied((IBinder)msg.obj, false);
            break;
        case REQUEST_ASSIST_CONTEXT_EXTRAS:
            handleRequestAssistContextExtras((RequestAssistContextExtras)msg.obj);
            break;
        case TRANSLUCENT_CONVERSION_COMPLETE:
            handleTranslucentConversionComplete((IBinder)msg.obj, msg.arg1 == 1);
            break;
        case INSTALL_PROVIDER:
            handleInstallProvider((ProviderInfo) msg.obj);
            break;
        case ON_NEW_ACTIVITY_OPTIONS:
            Pair<IBinder, ActivityOptions> pair = (Pair<IBinder, ActivityOptions>) msg.obj;
            onNewActivityOptions(pair.first, pair.second);
            break;
        case CANCEL_VISIBLE_BEHIND:
            handleCancelVisibleBehind((IBinder) msg.obj);
            break;
        case BACKGROUND_VISIBLE_BEHIND_CHANGED:
            handleOnBackgroundVisibleBehindChanged((IBinder) msg.obj, msg.arg1 > 0);
            break;
        case ENTER_ANIMATION_COMPLETE:
            handleEnterAnimationComplete((IBinder) msg.obj);
            break;
        case START_BINDER_TRACKING:
            handleStartBinderTracking();
            break;
        case STOP_BINDER_TRACKING_AND_DUMP:
            handleStopBinderTrackingAndDump((ParcelFileDescriptor) msg.obj);
            break;
        case MULTI_WINDOW_MODE_CHANGED:
            handleMultiWindowModeChanged((IBinder) ((SomeArgs) msg.obj).arg1,
                    ((SomeArgs) msg.obj).argi1 == 1,
                    (Configuration) ((SomeArgs) msg.obj).arg2);
            break;
        case PICTURE_IN_PICTURE_MODE_CHANGED:
            handlePictureInPictureModeChanged((IBinder) ((SomeArgs) msg.obj).arg1,
                    ((SomeArgs) msg.obj).argi1 == 1,
                    (Configuration) ((SomeArgs) msg.obj).arg2);
            break;
        case LOCAL_VOICE_INTERACTION_STARTED:
            handleLocalVoiceInteractionStarted((IBinder) ((SomeArgs) msg.obj).arg1,
                    (IVoiceInteractor) ((SomeArgs) msg.obj).arg2);
            break;
        case ATTACH_AGENT:
            handleAttachAgent((String) msg.obj);
            break;
        case APPLICATION_INFO_CHANGED:
            mUpdatingSystemConfig = true;
            try {
                handleApplicationInfoChanged((ApplicationInfo) msg.obj);
            } finally {
                mUpdatingSystemConfig = false;
            }
            break;
    }
    Object obj = msg.obj;
    if (obj instanceof SomeArgs) {
        ((SomeArgs) obj).recycle();
    }
    if (DEBUG_MESSAGES) Slog.v(TAG, "<<< done: " + codeToString(msg.what));
}
}
```

可以看到使用了这个方法来处理     handleLaunchActivity(r, null, "LAUNCH_ACTIVITY");
```
private void handleLaunchActivity(ActivityClientRecord r, Intent customIntent, String reason) {
  // If we are getting ready to gc after going to the background, well
  // we are back active so skip it.
  unscheduleGcIdler();
  mSomeActivitiesChanged = true;

  if (r.profilerInfo != null) {
      mProfiler.setProfiler(r.profilerInfo);
      mProfiler.startProfiling();
  }

  // Make sure we are running with the most recent config.
  handleConfigurationChanged(null, null);

  if (localLOGV) Slog.v(
      TAG, "Handling launch of " + r);

  // Initialize before creating the activity
  WindowManagerGlobal.initialize();

  Activity a = performLaunchActivity(r, customIntent);

  if (a != null) {
      r.createdConfig = new Configuration(mConfiguration);
      reportSizeConfigurations(r);
      Bundle oldState = r.state;
      handleResumeActivity(r.token, false, r.isForward,
              !r.activity.mFinished && !r.startsNotResumed, r.lastProcessedSeq, reason);

      if (!r.activity.mFinished && r.startsNotResumed) {
          performPauseActivityIfNeeded(r, reason);
          if (r.isPreHoneycomb()) {
              r.state = oldState;
          }
      }
  } else {
      // If there was an error, for any reason, tell the activity manager to stop us.
      try {
          ActivityManager.getService()
              .finishActivity(r.token, Activity.RESULT_CANCELED, null,
                      Activity.DONT_FINISH_TASK_WITH_ACTIVITY);
      } catch (RemoteException ex) {
          throw ex.rethrowFromSystemServer();
      }
  }
}

```

可以看到执行了这个方法来创建Activity`performLaunchActivity`

```
private Activity performLaunchActivity(ActivityClientRecord r, Intent customIntent) {
    // System.out.println("##### [" + System.currentTimeMillis() + "] ActivityThread.performLaunchActivity(" + r + ")");

    ActivityInfo aInfo = r.activityInfo;
    if (r.packageInfo == null) {
        r.packageInfo = getPackageInfo(aInfo.applicationInfo, r.compatInfo,
                Context.CONTEXT_INCLUDE_CODE);
    }

    ComponentName component = r.intent.getComponent();
    if (component == null) {
        component = r.intent.resolveActivity(
            mInitialApplication.getPackageManager());
        r.intent.setComponent(component);
    }

    if (r.activityInfo.targetActivity != null) {
        component = new ComponentName(r.activityInfo.packageName,
                r.activityInfo.targetActivity);
    }

    ContextImpl appContext = createBaseContextForActivity(r);
    Activity activity = null;
    try {
        java.lang.ClassLoader cl = appContext.getClassLoader();
        activity = mInstrumentation.newActivity(
                cl, component.getClassName(), r.intent);
        StrictMode.incrementExpectedActivityCount(activity.getClass());
        r.intent.setExtrasClassLoader(cl);
        r.intent.prepareToEnterProcess();
        if (r.state != null) {
            r.state.setClassLoader(cl);
        }
    } catch (Exception e) {
        if (!mInstrumentation.onException(activity, e)) {
            throw new RuntimeException(
                "Unable to instantiate activity " + component
                + ": " + e.toString(), e);
        }
    }

    try {
        Application app = r.packageInfo.makeApplication(false, mInstrumentation);

        if (localLOGV) Slog.v(TAG, "Performing launch of " + r);
        if (localLOGV) Slog.v(
                TAG, r + ": app=" + app
                + ", appName=" + app.getPackageName()
                + ", pkg=" + r.packageInfo.getPackageName()
                + ", comp=" + r.intent.getComponent().toShortString()
                + ", dir=" + r.packageInfo.getAppDir());

        if (activity != null) {
            CharSequence title = r.activityInfo.loadLabel(appContext.getPackageManager());
            Configuration config = new Configuration(mCompatConfiguration);
            if (r.overrideConfig != null) {
                config.updateFrom(r.overrideConfig);
            }
            if (DEBUG_CONFIGURATION) Slog.v(TAG, "Launching activity "
                    + r.activityInfo.name + " with config " + config);
            Window window = null;
            if (r.mPendingRemoveWindow != null && r.mPreserveWindow) {
                window = r.mPendingRemoveWindow;
                r.mPendingRemoveWindow = null;
                r.mPendingRemoveWindowManager = null;
            }
            appContext.setOuterContext(activity);
            activity.attach(appContext, this, getInstrumentation(), r.token,
                    r.ident, app, r.intent, r.activityInfo, title, r.parent,
                    r.embeddedID, r.lastNonConfigurationInstances, config,
                    r.referrer, r.voiceInteractor, window, r.configCallback);

            if (customIntent != null) {
                activity.mIntent = customIntent;
            }
            r.lastNonConfigurationInstances = null;
            checkAndBlockForNetworkAccess();
            activity.mStartedActivity = false;
            int theme = r.activityInfo.getThemeResource();
            if (theme != 0) {
                activity.setTheme(theme);
            }

            activity.mCalled = false;
            if (r.isPersistable()) {
                mInstrumentation.callActivityOnCreate(activity, r.state, r.persistentState);
            } else {
                mInstrumentation.callActivityOnCreate(activity, r.state);
            }
            if (!activity.mCalled) {
                throw new SuperNotCalledException(
                    "Activity " + r.intent.getComponent().toShortString() +
                    " did not call through to super.onCreate()");
            }
            r.activity = activity;
            r.stopped = true;
            if (!r.activity.mFinished) {
                activity.performStart();
                r.stopped = false;
            }
            if (!r.activity.mFinished) {
                if (r.isPersistable()) {
                    if (r.state != null || r.persistentState != null) {
                        mInstrumentation.callActivityOnRestoreInstanceState(activity, r.state,
                                r.persistentState);
                    }
                } else if (r.state != null) {
                    mInstrumentation.callActivityOnRestoreInstanceState(activity, r.state);
                }
            }
            if (!r.activity.mFinished) {
                activity.mCalled = false;
                if (r.isPersistable()) {
                    mInstrumentation.callActivityOnPostCreate(activity, r.state,
                            r.persistentState);
                } else {
                    mInstrumentation.callActivityOnPostCreate(activity, r.state);
                }
                if (!activity.mCalled) {
                    throw new SuperNotCalledException(
                        "Activity " + r.intent.getComponent().toShortString() +
                        " did not call through to super.onPostCreate()");
                }
            }
        }
        r.paused = true;

        mActivities.put(r.token, r);

    } catch (SuperNotCalledException e) {
        throw e;

    } catch (Exception e) {
        if (!mInstrumentation.onException(activity, e)) {
            throw new RuntimeException(
                "Unable to start activity " + component
                + ": " + e.toString(), e);
        }
    }

    return activity;
}

```

可以看到 使用Instrumentation的newActivity来创建Activity
```
activity = mInstrumentation.newActivity(
             cl, component.getClassName(), r.intent);
```

```
public Activity newActivity(ClassLoader cl, String className,
          Intent intent)
          throws InstantiationException, IllegalAccessException,
          ClassNotFoundException {
      return (Activity)cl.loadClass(className).newInstance();
  }
```

终于找到了，通过loadClass来创建对象.
回到这个方法performLaunchActivity，看一下他还做了什么操作
```
ActivityInfo aInfo = r.activityInfo;
    if (r.packageInfo == null) {
        r.packageInfo = getPackageInfo(aInfo.applicationInfo, r.compatInfo,
                Context.CONTEXT_INCLUDE_CODE);
    }
```
获取activity的信息

这里创建Application

```
            Application app = r.packageInfo.makeApplication(false, mInstrumentation);
```
 r.packageInfo得到LoadedApk对象，然后调用他的makeApplication
```
public Application makeApplication(boolean forceDefaultAppClass,
        Instrumentation instrumentation) {
    if (mApplication != null) {
        return mApplication;
    }

    Trace.traceBegin(Trace.TRACE_TAG_ACTIVITY_MANAGER, "makeApplication");

    Application app = null;

    String appClass = mApplicationInfo.className;
    if (forceDefaultAppClass || (appClass == null)) {
        appClass = "android.app.Application";
    }

    try {
        java.lang.ClassLoader cl = getClassLoader();
        if (!mPackageName.equals("android")) {
            Trace.traceBegin(Trace.TRACE_TAG_ACTIVITY_MANAGER,
                    "initializeJavaContextClassLoader");
            initializeJavaContextClassLoader();
            Trace.traceEnd(Trace.TRACE_TAG_ACTIVITY_MANAGER);
        }
        ContextImpl appContext = ContextImpl.createAppContext(mActivityThread, this);
        app = mActivityThread.mInstrumentation.newApplication(
                cl, appClass, appContext);
        appContext.setOuterContext(app);
    } catch (Exception e) {
        if (!mActivityThread.mInstrumentation.onException(app, e)) {
            Trace.traceEnd(Trace.TRACE_TAG_ACTIVITY_MANAGER);
            throw new RuntimeException(
                "Unable to instantiate application " + appClass
                + ": " + e.toString(), e);
        }
    }
    mActivityThread.mAllApplications.add(app);
    mApplication = app;

    if (instrumentation != null) {
        try {
            instrumentation.callApplicationOnCreate(app);
        } catch (Exception e) {
            if (!instrumentation.onException(app, e)) {
                Trace.traceEnd(Trace.TRACE_TAG_ACTIVITY_MANAGER);
                throw new RuntimeException(
                    "Unable to create application " + app.getClass().getName()
                    + ": " + e.toString(), e);
            }
        }
    }

    // Rewrite the R 'constants' for all library apks.
    SparseArray<String> packageIdentifiers = getAssets().getAssignedPackageIdentifiers();
    final int N = packageIdentifiers.size();
    for (int i = 0; i < N; i++) {
        final int id = packageIdentifiers.keyAt(i);
        if (id == 0x01 || id == 0x7f) {
            continue;
        }

        rewriteRValues(getClassLoader(), packageIdentifiers.valueAt(i), id);
    }

    Trace.traceEnd(Trace.TRACE_TAG_ACTIVITY_MANAGER);

    return app;
}

```

这里是一个单例，如果Application已经创建了，就不会再创建,在创建Applicaiton的时候会调用makeApplication里面有这样一段代码
```
if (instrumentation != null) {
        try {
            instrumentation.callApplicationOnCreate(app);
        } catch (Exception e) {
            if (!instrumentation.onException(app, e)) {
                Trace.traceEnd(Trace.TRACE_TAG_ACTIVITY_MANAGER);
                throw new RuntimeException(
                    "Unable to create application " + app.getClass().getName()
                    + ": " + e.toString(), e);
            }
        }
    }
```
这里调用Applicaiton的onCreate方法
```
public void callApplicationOnCreate(Application app) {
     app.onCreate();
 }
```

再回到ActivityThread的performLaunchActivity方法，看一下其中另外一部分代码的调用，看一下
```
activity.attach(appContext, this, getInstrumentation(), r.token,
                      r.ident, app, r.intent, r.activityInfo, title, r.parent,
                      r.embeddedID, r.lastNonConfigurationInstances, config,
                      r.referrer, r.voiceInteractor, window, r.configCallback);
```

```
final void attach(Context context, ActivityThread aThread,
           Instrumentation instr, IBinder token, int ident,
           Application application, Intent intent, ActivityInfo info,
           CharSequence title, Activity parent, String id,
           NonConfigurationInstances lastNonConfigurationInstances,
           Configuration config, String referrer, IVoiceInteractor voiceInteractor,
           Window window, ActivityConfigCallback activityConfigCallback) {
       attachBaseContext(context);

       mFragments.attachHost(null /*parent*/);

       mWindow = new PhoneWindow(this, window, activityConfigCallback);
       mWindow.setWindowControllerCallback(this);
       mWindow.setCallback(this);
       mWindow.setOnWindowDismissedCallback(this);
       mWindow.getLayoutInflater().setPrivateFactory(this);
       if (info.softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_UNSPECIFIED) {
           mWindow.setSoftInputMode(info.softInputMode);
       }
       if (info.uiOptions != 0) {
           mWindow.setUiOptions(info.uiOptions);
       }
       mUiThread = Thread.currentThread();

       mMainThread = aThread;
       mInstrumentation = instr;
       mToken = token;
       mIdent = ident;
       mApplication = application;
       mIntent = intent;
       mReferrer = referrer;
       mComponent = intent.getComponent();
       mActivityInfo = info;
       mTitle = title;
       mParent = parent;
       mEmbeddedID = id;
       mLastNonConfigurationInstances = lastNonConfigurationInstances;
       if (voiceInteractor != null) {
           if (lastNonConfigurationInstances != null) {
               mVoiceInteractor = lastNonConfigurationInstances.voiceInteractor;
           } else {
               mVoiceInteractor = new VoiceInteractor(voiceInteractor, this, this,
                       Looper.myLooper());
           }
       }

       mWindow.setWindowManager(
               (WindowManager)context.getSystemService(Context.WINDOW_SERVICE),
               mToken, mComponent.flattenToString(),
               (info.flags & ActivityInfo.FLAG_HARDWARE_ACCELERATED) != 0);
       if (mParent != null) {
           mWindow.setContainer(mParent.getWindow());
       }
       mWindowManager = mWindow.getWindowManager();
       mCurrentConfig = config;

       mWindow.setColorMode(info.colorMode);
   }

```
这里面创建Activity所需要的window

再回到ActivityThread的performLaunchActivity方法，看一下其中另外一部分代码的调用，看一下

```
if (r.isPersistable()) {
               mInstrumentation.callActivityOnCreate(activity, r.state, r.persistentState);
           } else {
               mInstrumentation.callActivityOnCreate(activity, r.state);
           }
```
Instrumentation
```
public void callActivityOnCreate(Activity activity, Bundle icicle,
        PersistableBundle persistentState) {
    prePerformCreate(activity);
    activity.performCreate(icicle, persistentState);
    postPerformCreate(activity);
}
```
activity
```

final void performCreate(Bundle icicle) {
    restoreHasCurrentPermissionRequest(icicle);
    onCreate(icicle);
    mActivityTransitionState.readState(icicle);
    performCreateCommon();
}
```

Activity的Oncreate方法在这里调用

在回到ActivityThread的performLaunchActivity方法
看方法的内容
```
 activity.performStart();
```
调用Activity的onStart方法

再次回到ActivityThread的handleLaunchActivity方法
```
handleResumeActivity(r.token, false, r.isForward,
                   !r.activity.mFinished && !r.startsNotResumed, r.lastProcessedSeq, reason);

```
调用activity的onresume方法

这里Activity的生命周期就调用完毕了


![Alt text](activity.svg "Android 启动,最好下载下来看")

## Service 工作过程
上面分析了Activity的工作过程，加深了对Activity的理解。这里来折腾一下Service的
这里从StartService开始

```
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intentService=new Intent(this,MyService.class);
        startService(intentService);
        bindService(intentService, new ServiceConnection() {
         @Override
         public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

         }

         @Override
         public void onServiceDisconnected(ComponentName componentName) {

         }
     },1);
    }
}

```

```
public class MyService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

```
### Service的启动过程：startService过程

Service启动过程是从ContextWrapper的startService方法开始的
```
@Override
public ComponentName startService(Intent service) {
    return mBase.startService(service);
}

```
看一下mBase：他是contextImpl，这个是ContextWrapper的具体实现，是一个装饰者模式。关于context的内容，大家可以百度一下，这个在android里面是一个非常重要的概念，后面我也会整理一下

这里看一下contextImpl中的startService方法
```
@Override
public ComponentName startService(Intent service) {
    warnIfCallingFromSystemProcess();
    return startServiceCommon(service, false, mUser);
}
```
他调用了startServiceCommon，那我们看一下这个方法做了什么操作
```
private ComponentName startServiceCommon(Intent service, boolean requireForeground,
        UserHandle user) {
    try {
        validateServiceIntent(service);
        service.prepareToLeaveProcess(this);
        ComponentName cn = ActivityManager.getService().startService(
            mMainThread.getApplicationThread(), service, service.resolveTypeIfNeeded(
                        getContentResolver()), requireForeground,
                        getOpPackageName(), user.getIdentifier());
        if (cn != null) {
            if (cn.getPackageName().equals("!")) {
                throw new SecurityException(
                        "Not allowed to start service " + service
                        + " without permission " + cn.getClassName());
            } else if (cn.getPackageName().equals("!!")) {
                throw new SecurityException(
                        "Unable to start service " + service
                        + ": " + cn.getClassName());
            } else if (cn.getPackageName().equals("?")) {
                throw new IllegalStateException(
                        "Not allowed to start service " + service + ": " + cn.getClassName());
            }
        }
        return cn;
    } catch (RemoteException e) {
        throw e.rethrowFromSystemServer();
    }
}

```

这里又看到了一个熟悉的身影ActivityManager，之后调用了他的getService，得到ActivityManagerService,就是之前的AMS
```
public static IActivityManager getService() {
    return IActivityManagerSingleton.get();
}

private static final Singleton<IActivityManager> IActivityManagerSingleton =
        new Singleton<IActivityManager>() {
            @Override
            protected IActivityManager create() {
                final IBinder b = ServiceManager.getService(Context.ACTIVITY_SERVICE);
                final IActivityManager am = IActivityManager.Stub.asInterface(b);
                return am;
            }
        };
```

得到AMS之后，调用他的startService方法

```
@Override
public ComponentName startService(IApplicationThread caller, Intent service,
        String resolvedType, boolean requireForeground, String callingPackage, int userId)
        throws TransactionTooLargeException {
    enforceNotIsolatedCaller("startService");
    // Refuse possible leaked file descriptors
    if (service != null && service.hasFileDescriptors() == true) {
        throw new IllegalArgumentException("File descriptors passed in Intent");
    }

    if (callingPackage == null) {
        throw new IllegalArgumentException("callingPackage cannot be null");
    }

    if (DEBUG_SERVICE) Slog.v(TAG_SERVICE,
            "*** startService: " + service + " type=" + resolvedType + " fg=" + requireForeground);
    synchronized(this) {
        final int callingPid = Binder.getCallingPid();
        final int callingUid = Binder.getCallingUid();
        final long origId = Binder.clearCallingIdentity();
        ComponentName res;
        try {
            res = mServices.startServiceLocked(caller, service,
                    resolvedType, callingPid, callingUid,
                    requireForeground, callingPackage, userId);
        } finally {
            Binder.restoreCallingIdentity(origId);
        }
        return res;
    }
}
```
可以看到这里AMS在这个方法里面调用了mServices.startServiceLocked。mServices是一个ActiveServices对象。他调用startServiceLocked方法
ActiveServices是一个辅助AMS进行Service管理的类，他包括Service的启动、绑定。停止等。

```

    ComponentName startServiceLocked(IApplicationThread caller, Intent service, String resolvedType,
            int callingPid, int callingUid, boolean fgRequired, String callingPackage, final int userId)
            throws TransactionTooLargeException {
        ...
        ComponentName cmp = startServiceInnerLocked(smap, service, r, callerFg, addToStarting);
        return cmp;
    }

```

startServiceLocked方法比较长，就省略了一部分，重点是最后一段代码：
看一下这一段代码做了什么

```
ComponentName startServiceInnerLocked(ServiceMap smap, Intent service, ServiceRecord r,
        boolean callerFg, boolean addToStarting) throws TransactionTooLargeException {
    ServiceState stracker = r.getTracker();
    if (stracker != null) {
        stracker.setStarted(true, mAm.mProcessStats.getMemFactorLocked(), r.lastActivity);
    }
    r.callStart = false;
    synchronized (r.stats.getBatteryStats()) {
        r.stats.startRunningLocked();
    }
    String error = bringUpServiceLocked(r, service.getFlags(), callerFg, false, false);
    if (error != null) {
        return new ComponentName("!!", error);
    }

    if (r.startRequested && addToStarting) {
        boolean first = smap.mStartingBackground.size() == 0;
        smap.mStartingBackground.add(r);
        r.startingBgTimeout = SystemClock.uptimeMillis() + mAm.mConstants.BG_START_TIMEOUT;
        if (DEBUG_DELAYED_SERVICE) {
            RuntimeException here = new RuntimeException("here");
            here.fillInStackTrace();
            Slog.v(TAG_SERVICE, "Starting background (first=" + first + "): " + r, here);
        } else if (DEBUG_DELAYED_STARTS) {
            Slog.v(TAG_SERVICE, "Starting background (first=" + first + "): " + r);
        }
        if (first) {
            smap.rescheduleDelayedStartsLocked();
        }
    } else if (callerFg || r.fgRequired) {
        smap.ensureNotStartingBackgroundLocked(r);
    }

    return r.name;
}
```

ServiceRecoed是描述一个Service记录，还记得ActivityRecord吗?，这个和ActivityRecord功能是类似的，ServiceRecord一直贯穿着整个Service的启动过程。但是startServiceInnerLocked并没有完成Service的启动，而是把工作交给了bringUpServiceLocked这个方法，我们看一下他的具体逻辑

```
private String bringUpServiceLocked(ServiceRecord r, int intentFlags, boolean execInFg,
        boolean whileRestarting, boolean permissionsReviewRequired)
        throws TransactionTooLargeException {
          ...
              realStartServiceLocked(r, app, execInFg);
          ...
        }
```
这个方法又调用了realStartServiceLocked方法。是不是很熟悉，Activity也是一样的，这里才是真正启动


```
private final void realStartServiceLocked(ServiceRecord r,
        ProcessRecord app, boolean execInFg) throws RemoteException {
    if (app.thread == null) {
        throw new RemoteException();
    }
    if (DEBUG_MU)
        Slog.v(TAG_MU, "realStartServiceLocked, ServiceRecord.uid = " + r.appInfo.uid
                + ", ProcessRecord.uid = " + app.uid);
    r.app = app;
    r.restartTime = r.lastActivity = SystemClock.uptimeMillis();

    final boolean newService = app.services.add(r);
    bumpServiceExecutingLocked(r, execInFg, "create");
    mAm.updateLruProcessLocked(app, false, null);
    updateServiceForegroundLocked(r.app, /* oomAdj= */ false);
    mAm.updateOomAdjLocked();

    boolean created = false;
    try {
        if (LOG_SERVICE_START_STOP) {
            String nameTerm;
            int lastPeriod = r.shortName.lastIndexOf('.');
            nameTerm = lastPeriod >= 0 ? r.shortName.substring(lastPeriod) : r.shortName;
            EventLogTags.writeAmCreateService(
                    r.userId, System.identityHashCode(r), nameTerm, r.app.uid, r.app.pid);
        }
        synchronized (r.stats.getBatteryStats()) {
            r.stats.startLaunchedLocked();
        }
        mAm.notifyPackageUse(r.serviceInfo.packageName,
                             PackageManager.NOTIFY_PACKAGE_USE_SERVICE);
        app.forceProcessStateUpTo(ActivityManager.PROCESS_STATE_SERVICE);
        app.thread.scheduleCreateService(r, r.serviceInfo,
                mAm.compatibilityInfoForPackageLocked(r.serviceInfo.applicationInfo),
                app.repProcState);
        r.postNotification();
        created = true;
    } catch (DeadObjectException e) {
        Slog.w(TAG, "Application dead when creating service " + r);
        mAm.appDiedLocked(app);
        throw e;
    } finally {
        if (!created) {
            // Keep the executeNesting count accurate.
            final boolean inDestroying = mDestroyingServices.contains(r);
            serviceDoneExecutingLocked(r, inDestroying, inDestroying);

            // Cleanup.
            if (newService) {
                app.services.remove(r);
                r.app = null;
            }

            // Retry.
            if (!inDestroying) {
                scheduleServiceRestartLocked(r, false);
            }
        }
    }

    if (r.whitelistManager) {
        app.whitelistManager = true;
    }

    requestServiceBindingsLocked(r, execInFg);

    updateServiceClientActivitiesLocked(app, null, true);

    // If the service is in the started state, and there are no
    // pending arguments, then fake up one so its onStartCommand() will
    // be called.
    if (r.startRequested && r.callStart && r.pendingStarts.size() == 0) {
        r.pendingStarts.add(new ServiceRecord.StartItem(r, false, r.makeNextStartId(),
                null, null, 0));
    }

    sendServiceArgsLocked(r, execInFg, true);

    if (r.delayed) {
        if (DEBUG_DELAYED_STARTS) Slog.v(TAG_SERVICE, "REM FR DELAY LIST (new proc): " + r);
        getServiceMapLocked(r.userId).mDelayedStartList.remove(r);
        r.delayed = false;
    }

    if (r.delayedStop) {
        // Oh and hey we've already been asked to stop!
        r.delayedStop = false;
        if (r.startRequested) {
            if (DEBUG_DELAYED_STARTS) Slog.v(TAG_SERVICE,
                    "Applying delayed stop (from start): " + r);
            stopServiceLocked(r);
        }
    }
}
```
这个方法里面先调用了   
```
 app.thread.scheduleCreateService(r, r.serviceInfo,
            mAm.compatibilityInfoForPackageLocked(r.serviceInfo.applicationInfo),
            app.repProcState);
```
这个东西，是不是有点熟悉，activity启动里面也有一个类似的是app.thread.scheduleLaunchActivity，然后这里就是回到ActivityThread通过Handler H来完成


```
public final void scheduleCreateService(IBinder token,
          ServiceInfo info, CompatibilityInfo compatInfo, int processState) {
          updateProcessState(processState, false);
          CreateServiceData s = new CreateServiceData();
          s.token = token;
          s.info = info;
          s.compatInfo = compatInfo;
          sendMessage(H.CREATE_SERVICE, s);
}
```
这里看一下handler H里面的处理
```
Trace.traceBegin(Trace.TRACE_TAG_ACTIVITY_MANAGER, ("serviceCreate: " + String.valueOf(msg.obj)));
handleCreateService((CreateServiceData)msg.obj);
Trace.traceEnd(Trace.TRACE_TAG_ACTIVITY_MANAGER);
```
最终调用了handleCreateService方法，来看一下这个方法
```

    private void handleCreateService(CreateServiceData data) {
        // If we are getting ready to gc after going to the background, well
        // we are back active so skip it.
        unscheduleGcIdler();

        LoadedApk packageInfo = getPackageInfoNoCheck(
                data.info.applicationInfo, data.compatInfo);
        Service service = null;
        try {
            java.lang.ClassLoader cl = packageInfo.getClassLoader();
            service = (Service) cl.loadClass(data.info.name).newInstance();
        } catch (Exception e) {
            if (!mInstrumentation.onException(service, e)) {
                throw new RuntimeException(
                    "Unable to instantiate service " + data.info.name
                    + ": " + e.toString(), e);
            }
        }

        try {
            if (localLOGV) Slog.v(TAG, "Creating service " + data.info.name);

            ContextImpl context = ContextImpl.createAppContext(this, packageInfo);
            context.setOuterContext(service);

            Application app = packageInfo.makeApplication(false, mInstrumentation);
            service.attach(context, this, data.info.name, data.token, app,
                    ActivityManager.getService());
            service.onCreate();
            mServices.put(data.token, service);
            try {
                ActivityManager.getService().serviceDoneExecuting(
                        data.token, SERVICE_DONE_EXECUTING_ANON, 0, 0);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } catch (Exception e) {
            if (!mInstrumentation.onException(service, e)) {
                throw new RuntimeException(
                    "Unable to create service " + data.info.name
                    + ": " + e.toString(), e);
            }
        }
    }
```
这个方法分为几个步骤：
1. 首先通过类加载器来创建Service对象
2. 接着创建Application对象并调用其onCreate方法，当然Application创建过程只会只会有一次
3. 接着创建ConTextImpl对象病通过Service的attach方法建立两者之间的联系，这个和Activity是类似的。
4. 最后调用Service的onCreate方法并将Service对象存储到ActivityThread中的一个列表中
由于Service的onCreate方法已经被执行了，这意味着Service已经启动了，除此之外，ActivityThread中还会通过handleServiceArgs方法调用Service的onStartCommand方法
```
private void handleServiceArgs(ServiceArgsData data) {
    Service s = mServices.get(data.token);
    if (s != null) {
        try {
            if (data.args != null) {
                data.args.setExtrasClassLoader(s.getClassLoader());
                data.args.prepareToEnterProcess();
            }
            int res;
            if (!data.taskRemoved) {
                res = s.onStartCommand(data.args, data.flags, data.startId);
            } else {
                s.onTaskRemoved(data.args);
                res = Service.START_TASK_REMOVED_COMPLETE;
            }

            QueuedWork.waitToFinish();

            try {
                ActivityManager.getService().serviceDoneExecuting(
                        data.token, SERVICE_DONE_EXECUTING_START, data.startId, res);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
            ensureJitEnabled();
        } catch (Exception e) {
            if (!mInstrumentation.onException(s, e)) {
                throw new RuntimeException(
                        "Unable to start service " + s
                        + " with " + data.args + ": " + e.toString(), e);
            }
        }
    }
}
```
这个样子startService的几个重要生命周期方法都调用了，Service的启动过程就分析完毕了

和上面的Activity一样，用一张UML来总结一下





![Alt text](service启动流程图.svg "Service 启动,最好下载下来看")
### Service的绑定过程


# 第十章 Android的消息机制
# 第十一章 Android的线程和线程池
# 第十二章 Bitmap的加载和Cache
# 第十三章 综合技术
# 第十四章 JNI和NDK编程
# 第十五章 Android 性能优化
