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



# 第二章 IPC机制
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
