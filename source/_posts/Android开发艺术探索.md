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
|Messenger|功能一般，支持一对多串行通信，支持实时通信|不能很好的处理高并发情形，不能支持RPC，数据通过Message进行传输，英雌只能传输Bundle支持的数据类型|低并发的一对多即时通信，五RPC需求，或者无需返回结果的RPC需求|
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
* 第三种是通过改变VIew的LayoutParams是的View重新布局从而实现滑动

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
当一个VIew需要处理事件的时候，如果他设置了`OnTouchListener`，那么`OnTouchListener`中的`OnTouch`方法就会被调用。这个时候事件的处理要看`OnTouch`的返回值，如果返回false,则当前的`OnTouch`方法就会被调用。如果返回true,则当前的`OnTouch`方法就不会被调用。因此，给View设置OnTouchListener，其优先级比OnTouchRvent要搞。在Ontouch方法中如果设置有OnClickListener,那么他的OnClick方法就会被调用，OnClickListener是优先级最低的。

当一个点击事件产生之后，他的传递过程遵循如下顺序：Activity->Window->View，即事件总是先传递给Activity，Activity传递给Window，最后Window在传递给顶级的View，然后在按照事件分发的及机制分发事件。考虑到一种情况，如果一个VIew的OnTouchEvent返回false，那么他的父容器的OnTouchEvent就会被调用，以此类推，如果所有的元素都不处理这个事件，那么这个事件将会最终传递给Activity处理。即Activity的OnTouchEvent 会被调用

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
最后说一下场景3 ：场景1和场景2两种情况嵌套，英雌这个比较复杂。

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

![Alt text](图像1523974134.png "顶级VIew：DecorView的结构")
如图：DecorView作为最顶级的View，一般情况下他的内部包含一个Linearlayout,分为两个部分：上方标题栏和下方内容栏。在Actvity中通过`setContentView`的内容是被加入到内容栏中间的，而内容栏的id是content，所以这个方法就叫做`setContentView`。如何得到这个内容栏呢? 可以通过`findViewById(R.id.content)`找到内容栏。如何得到我们设置的View呢?可以通过`content.getChildAt(0)`来获取

## 理解MeasureSpec
为了更好的理解View的测量流程，我们还需要理解MeasureSpec。从名字上来看 MeasureSpec像是测量规格。 MeasureSpec是干什么的呢？它在很大程度上决定了一个View的尺寸规格，之所以很大程度上是因为这个会受到父控件的影响，因为父控件影响View的MeasureSpec的创建过程。在测量过程中，系统会将View的LayoutParems根据父控件所施加的规则转换成对应的MeasureSpec，然后在更具这个measureSpec来测量VIew的宽高,上面提到过，这里测量的宽和高并不一定是View的最终宽高。MeasureSpec看起来有点复杂。其实他的实现很简单。


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
父容器不对VIew有限制，要多他给多大，一般情况用于系统内部，表示测量状态
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

对于普通View来说，这里是指我们布局中的View，VIew的measure过程由ViewGroup传递而来，先看一下ViewGroup的measureChildWithMargins方法

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
`getChildMeasureSpec`清楚的展示了普通View的MeasureSpec的创建规则，为了更加清晰的理解`getChildMeasureSpec`这里提供一个表针对`getChildMeasureSpec`的原理尽心梳理

||parentSpecMode|EXACTLY|AT_MOST|UNSPECIFIED|
|:---:|:---:|:---:|:----:|:----:|
|childLayoutParams|dp/px|EXACTLY/childSize|AT_MOST/childSize|UNSPECIFIED/childSize|
|childLayoutParams|MATCH_PARENT|EXACTLY/parentSize|AT_MOST/parentSize|UNSPECIFIED/0|
|childLayoutParams|WRAP_CONTENT|AT_MOST/parentSiz|AT_MOST/parentSiz|UNSPECIFIED/0|

针对表这里在做一下说明，前面已经提到，对于普通的View，其MeasureSpec由父控件的MeasureSpec和自身的LayoutParams来共同决定，那么针对不同的父容器和View本身不同的LayoutParams，View就可以有多重MeasureSpec。这里说一下当View采用固定狂傲的时候，不管父容器是什么模式，都是精确模式，并且大小遵循子控件的LayoutParams，当View的宽度/高度是match_parent时，如果父控件的模式是精确模式，那么View也是精确模式并且其大小不会超过其父容器的大小，如果父容器是最大模式，那么View也是最大模式，并且其大小不会超过父容器的剩余空间，当VIew的宽/高是wrap_content时，不管父容器的模式是精确还是最大化，View的模式总是最大化并且不能超过父容器的剩余空间。分析的时候溜掉了UNSPECIFIED模式，这个模式主要用于系统内部多次Measure的情景，一般不用

## View的绘制流程
View的工作流程主要是指measure、layout和draw这三个过程，即测量，布局和绘制，其中measure确定View的测量宽和高，layout确定View的四个顶点位置，而draw则将VIew绘制到屏幕上
### measure过程
measure过程要分情况来看，如果只是一个原始的View，那么通过measure方法就完成了其测量过程，如果是一个ViewGroup，除了完成自己的测量过程外，还会遍历调用所有的子元素的measure方法。各个子元素在递归去执行这个流程。下面针对这两种情况分别讨论。
1. View的measure过程
view的measure过程由其measure方法来完成，measure是一个final类型方法，这意味着不能重写此方法，在View的measure方法中会调用View的onMeasure方法，因此，只要看onMeasure方法实现即可，VIew的onMeasure方法如下所示。
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
从getDefaultSize方法的实现来看，View的狂傲由specSize决定，我们得出结论：直接继承View的自定义控件需要重写onMeasure方法并设置wrap_content是的自身大小，否则在布局中使用wrap_content就相当于使用match_parent。为什么呢？这个原因需要结合上述代码和之前的表来理解。从上述代码我们知道，如果View在布局中使用wrap_content,那么他的specMode的模式就是AT_MOST，在这种模式下，他的宽和高就是specSize；并且根据上面的表，可以看到这种情况下的specSize是parentSIze，而parentSize就是容器目前可以使用的大小，就是父容器当前剩余的空间大小。很显然，VIew的宽和高就相当于父控件当前剩余的空间大小，这种效果和在布局中使用match_parent完全一样。如何解决这个问题呢？
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
在上面的代码中，我们只需要给VIew指定一个默认的内部宽和高(mWidth和mHeight)并且在wrap_content的时候设置宽和高就可以了。针对非wrap_content情景，我们沿用西永的测量值就可以了。至于这个默认内部宽和高的大小如何指定，这个没有固定依据，更具需要灵活指定即可。查看TextView和ImageView等源码就可以知道，针对wrap_content情形。他们的onMeasure方法都做了特殊处理，读者可以自行查看他们的源码

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
我们知道ViewGroup没有定义测量过程，这个是因为ViewGroup是一个抽象类，其测量过程的OnMeasure需要在每一个子类中实现，比如LinearLayout，RelativeLayout等。为什么不像VIew一样对其OnMeasure进行统一处理呢，这个是因为每一个ViewGroup子类的布局特性不同，导致细节不同，所以不能统一实现。这里使用LinearLayout的onMeasure进行分析



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
通过手动对VIew进行measure得到。这个情况适合复杂情况的处理

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

layoutde 作用是用来确定子元素的位置，当ViewGroup的位置被确定之后，他在onLayout中会遍历子元素并调用他的layout方法。在layout中onLayout方法又会被调用，layout比measure过程要简单很多，layout方法确定了VIew本身的位置，而onLayout方法会确定所有子元素的位置。先看View的layout方法


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
这里来回答一个之前的问题，VIew的测量宽高和最终宽高的区别：这个问题可以翻译为View的getMeasureWidth和getWidth这两个方法有什么区别
看一下getWidth的方法实现

```
public final int getWidth(){
  return mRight-mLeft;
}
```

从getWidth的源码和结合mLeft、mRight、mTop、mBottom这四个变量的负值过程来看，getWIdth方法返回的就是VIew的测量宽度。经过上述分析，可以回答这个问题。在View的默认实现中，View的测量宽和高和最终宽和高是相等的，只不过测量宽和高形成在View的measure过程，而最终宽和高形成与View的layout过程，即两者的赋值时机不同，测量宽和高的赋值时机稍微早一些。但是在一些情况下确实会不一样
比如

```
重写了View的layout方法

public void layout(int l,int t,int r,int b){
  suber.layout(l,t,r+100,b+100);

}
```

这样会导致View的最终宽和高总是比测量宽和高大100px，这样会导致View显示不正常。还有一种是要多次测量的情况，前几次测量的宽和高可能和最终宽和高不一致

### draw过程

draw过程就比较简单了，他的作用是将VIew绘制到屏幕上面，View的绘制过程遵循下面几步：
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
这一节讲解一下自定义View


### 自定义View的分类

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
