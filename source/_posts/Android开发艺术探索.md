---
title: Android开发艺术探索
date: 2018-01-27 17:08:20
tags: android进阶第一步
---

>  这个是Android 开发艺术探索的读书笔记，感觉在把android群英传整理之后对android的知识体系有了更加深入的理解，好记性不如烂笔头，还是记录一下。

# 第一章 Activity的生命周期和启动模式
> Activity的理解

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
通过上面的分析，可以了解了Binder的工作机制，但是还有两点需要额外说明一下：首先，当客户端发起远程请求时，由于房钱线程会被挂起知道服务端进程返回数据，所以如果一个远程方法很耗时，这个时候就不能在UI线程中发起此远程请求；其次由于服务端Binder方法运行在Binder线程池中，所以Binder方法不管是否耗时都应该采取同步的方式实现，应为他已经运行在一个线程中了。为了更好的说明Binder下面给出一个Binder的工作机制图



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

* 实现Stub类和Stub类中的Proxy代理类，这段代码我们可以自己写，但是写出来发现和系统生成的代码是一样的，英雌这个Stub类我们只需要参考系统生成的代码即可，只是结构上需要做一下调整，调整后的代码如下所示。

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

通过文件共享这种方式来共享数据对文件格式是没有具体要求的，比如可以是文本文件，也可以是XML文件，只要读写是双方约定的数据格式即可。通过文件共享的方式也是有局限性的，比如并发读写的问题，想上面的那些例子，如果并发读写，那么我们读出的内容就有可能不是最新的，如果是并发写的话就更加严重了，英雌我们要尽量避免并发写这种情况的操作或者使用线程同步来限制多个线程的写操作，通过上面的分析，我们可以知道，文件共享方式适合在对数据同步要求不高的进程之间进行通信，并用要妥善处理并发读写的问题。

当然SharedPrefences是个特例，众所周知，SharedPrefences是Android中提供的轻量级存储方案，他通过键值对的方式来存储数据，在底层实现上他采用XML文件来存储价值对，每个应用的SharedPrefences文件都可以在当前包所在的data目录下查看到。一般来说，他的目录位于/data/data/package name/shared_prefs 目录下，其中package name 表示的是当前应用的包名，从本质上来说，SharedPrefences也属于文件的一种，但是由于系统对他的读写有一定的缓存策略，记在内从重会有一份SharedPrefences文件的缓存，英雌在多进程模式下，系统对他的读写就变得不可靠，当面对高并发的读写访问，sharedPrefences有很大几率会丢失数据，因此，不建议在进程通信中使用SharePrefences

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

```

### 使用AIDL

### 使用ContentProvide

### 使用Socket

## Binder 连接池

## 选用合适的IPC方式



# 第三章 View的事件体系
# 第四章 View的工作原理
# 第五章 理解RemoteViews
# 第六章 Android的Drawable
# 第七章 Android 动画深入分析
# 第八章 理解Windows和WindowsManager
# 第九章 四大组件的工作流程
# 第十章 Android的消息机制
# 第十一章 Android的线程和线程池
# 第十二章 Bitmap的加载和Cache
# 第十三章 综合技术
# 第十四章 JNI和NDK编程
# 第十五章 Android 性能优化
