---
title: 第九章 四大组件的工作流程
date: 2018-01-27 17:08:30
top : 209
tags:
- 进阶
- Android开发艺术探索
categories: android
---
> 这一章讲述一下四大组件的工作过程,说到四大组件，开发者都再熟悉不过了，他们是Activity、Service、BroadcastReceiver、ContentProvide。如何使用四大组件这个不是本章的话题，这个都是基础的内容。这里按照如下逻辑来分析Android的四大组件：首先对四大组件的运行状态和工作方式做一个概括化的描述，接着对四大组件的工作过程进行分析，通过本章节对四大组件有一个更加深刻的认识。
本章主要侧重于四大组件工作过程的分析，通过分析他们的工作过程我们可以更好的理解系统的运行机制。本章的意义在于加深读者对四大组件的工作方式的认识。本章的意义在于加深读者对四大组件工作方式的认识，由于四大组件的特殊性，我们有必要对他有一定的了解。

# 四大组件的运行状态
Android四大组件中处理BroadcaskReceiver意外，其他三种组件必须都在ANdroidManifest中注册，对于BroadcastReceiver来说，他既可以在AndroidManifest中注册，也可以使用代码来注册。在调用方式上，Activity、Service、BroadcastReceiver需要借助Intent，而ContentProvide不需要借助Intent

Activity是一种展示型的组件，用于向用户直接展示一个界面，并且可以接受用户的输入信息从而进行交互。Activity是最重要的一种组件，对用户来说Activity就是一个Android应用的全部，这是因为其他三大组件对用户来说都是不可感知的。Activity的启动由Intent触发，其中Intent可以分为显示Intent和隐式Intent，显示Intent可以明确指向一个Activity组件，隐式Intent则指向一个或者多个目标Activity组件，当然也可能没有任何一个Activity组件可以处理这个Intent。一个Activity组件可以具有特定的启动模式。关于启动模式在之前已经做了介绍了，同一个Activity组件在不同的启动模式下会有不同的效果。Activity组件也是可以停止的，在实际开发中可以通过Activity的finish方法来结束一个Activity组件的运行。由此看来，Activity组件的主要作用是展示一个界面并和用户交互，他扮演的是一种前台界面的角色。

Service是一种计算型组件，用于在后台执行一系列计算任务。由于Service组件工作在后台，因此用户无法直接感知到他的存在。Service组件和Activity组件略有不同，Activity组件只有一种运行模式，即Activity处于启动装填，但是Service组件却又两种状态：启动状态和绑定状态。当Service组件处于启动状态时，这个时候Service内部可以做一些后台计算，并不需要和外界有直接的交互。尽管Service组件是用于执行后台计算的，但是它本身是运行在主线程中的，因此耗时操作需要在独立的线程中执行，也可以使用IntentService，这个默认是在新建的线程中执行。当Service组件处于绑定状态时，这个时候Service内部同样可以进行后台计算，但是处于这种状态是，外界可以很方便的和Service组件进行通信。Service组件也可以停止的，停止一个Service组件比较复杂，需要灵活使用stopService和unBindService这两个方法才能完全停止一个Service组件。


BroadcastReceiver是一种消息性组件，用于在不同的组件乃至不同的应用之间传递消息。BroadcastReceiver同样无法被用户直接感知，因为他工作在系统内部。BroadcastReceiver也叫广播，广播的注册有两种方式：静态注册和动态注册。静态注册时指在AndroidManifest中注册管你胳膊，这种广播在应用安装的时候会被系统解析，因此这种形式的广播不需要启动就可以收到广播。动态注册广播需要通过Context.registerReceiver()来实现，并且不需要的时候可以通过Context.unregisterReceiver()来解除广播，此种形态的广播必须要应用启动之后才能注册并接受广播，应为应用不启动就无法注册广播，无法注册广播就无法收到相应的广播。在开发中通过Context的一系列send方法来发送广播，被发送的广播会被系统发送给该兴趣的接受者，发送和接受过程的匹配是通过<Intent-filter>来描述的。可以发现，BroadcastReceiver组件可以通过实现低耦合的观察者模式，观察者和接受者没有任何耦合。由于BroadcastReceiver的特性，他不适合用来执行耗时操作。BroadcastReceiver一般来说不需要停止，动态注册的要及时解开注册

ContentProvider是一种共享性组件，用于向其他组件内置其他应用共享数据。和BroadcastReceiver一样，ContentProvider无法被用户感知。对于一个ContentProvider组件来说，他的内部需要实现增删改查这四种操作，在他的内部维持着一份数据集合，这个数据集合既可以通过数据库来实现，可以通过其他任意类型来实现，比如List和Map。ContentProvider对数据集合的具体实现方式没有任何要求。需要注意的是ContentProvider内部的insert、delete。update和query方法需要处理好线程同步，因此这几个方法在Binder线程池汇总被调用，另外ContentProvider组件也不需要手动停止。

# Activity的工作流程
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


![Alt text](activity工作流程.svg "Android 启动,最好下载下来看")

# Service 工作过程
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
## Service的启动过程：startService过程

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
## Service的绑定过程
和Service的启动过程是一样的，Service的绑定过程也是从ContextWrapper开始的，如下所示

bindService其实是调用ContextWrapper的bindService方法
```
@Override
public boolean bindService(Intent service, ServiceConnection conn,
        int flags) {
    return mBase.bindService(service, conn, flags);
}
```

看一下mBase：他是contextImpl，这个是ContextWrapper的具体实现，是一个装饰者模式。这个在介绍Service启动的时候介绍过
```
@Override
public boolean bindService(Intent service, ServiceConnection conn,
        int flags) {
    warnIfCallingFromSystemProcess();
    return bindServiceCommon(service, conn, flags, mMainThread.getHandler(),
            Process.myUserHandle());
}
```
和启动Service类似，这里调用了bindServiceCommon这个方法

```
private boolean bindServiceCommon(Intent service, ServiceConnection conn, int flags, Handler
        handler, UserHandle user) {
    // Keep this in sync with DevicePolicyManager.bindDeviceAdminServiceAsUser.
    IServiceConnection sd;
    if (conn == null) {
        throw new IllegalArgumentException("connection is null");
    }
    if (mPackageInfo != null) {
        sd = mPackageInfo.getServiceDispatcher(conn, getOuterContext(), handler, flags);
    } else {
        throw new RuntimeException("Not supported in system context");
    }
    validateServiceIntent(service);
    try {
        IBinder token = getActivityToken();
        if (token == null && (flags&BIND_AUTO_CREATE) == 0 && mPackageInfo != null
                && mPackageInfo.getApplicationInfo().targetSdkVersion
                < android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            flags |= BIND_WAIVE_PRIORITY;
        }
        service.prepareToLeaveProcess(this);
        int res = ActivityManager.getService().bindService(
            mMainThread.getApplicationThread(), getActivityToken(), service,
            service.resolveTypeIfNeeded(getContentResolver()),
            sd, flags, getOpPackageName(), user.getIdentifier());
        if (res < 0) {
            throw new SecurityException(
                    "Not allowed to bind to service " + service);
        }
        return res != 0;
    } catch (RemoteException e) {
        throw e.rethrowFromSystemServer();
    }
}

```

我们看一下这个方法的内容：这个方法主要完成两件事情
第一个是将ServiceConnection对象转换成InnerConnection这个对象

这里看一下这段代码` sd = mPackageInfo.getServiceDispatcher(conn, getOuterContext(), handler, flags);`mPackageInfo这个是一个LoadedApk类，我们跟进去看一下这个方法
```
public final IServiceConnection getServiceDispatcher(ServiceConnection c,
        Context context, Handler handler, int flags) {
    synchronized (mServices) {
        LoadedApk.ServiceDispatcher sd = null;
        ArrayMap<ServiceConnection, LoadedApk.ServiceDispatcher> map = mServices.get(context);
        if (map != null) {
            if (DEBUG) Slog.d(TAG, "Returning existing dispatcher " + sd + " for conn " + c);
            sd = map.get(c);
        }
        if (sd == null) {
            sd = new ServiceDispatcher(c, context, handler, flags);
            if (DEBUG) Slog.d(TAG, "Creating new dispatcher " + sd + " for conn " + c);
            if (map == null) {
                map = new ArrayMap<>();
                mServices.put(context, map);
            }
            map.put(c, sd);
        } else {
            sd.validate(context, handler);
        }
        return sd.getIServiceConnection();
    }
}

```
这里创建了`new ServiceDispatcher(c, context, handler, flags);`这个对象，ServiceDispatcher是LoadedApk的一个内部类.看一下他的构造方法里面做了什么
```
ServiceDispatcher(ServiceConnection conn,
            Context context, Handler activityThread, int flags) {
        mIServiceConnection = new InnerConnection(this);
        mConnection = conn;
        mContext = context;
        mActivityThread = activityThread;
        mLocation = new ServiceConnectionLeaked(null);
        mLocation.fillInStackTrace();
        mFlags = flags;
    }
```
这里创建了InnerConnection,而InnerConnection是ServiceDispatcher的内部类，看一下InnerConnection的具体实现

```
private static class InnerConnection extends IServiceConnection.Stub {
    final WeakReference<LoadedApk.ServiceDispatcher> mDispatcher;

    InnerConnection(LoadedApk.ServiceDispatcher sd) {
        mDispatcher = new WeakReference<LoadedApk.ServiceDispatcher>(sd);
    }

    public void connected(ComponentName name, IBinder service, boolean dead)
            throws RemoteException {
        LoadedApk.ServiceDispatcher sd = mDispatcher.get();
        if (sd != null) {
            sd.connected(name, service, dead);
        }
    }
}

```

这里继承了IServiceConnection.Stub,说明他是一个AIDL。这里解释一下为什么不能直接使用ServiceConnection对象，这是因为服务的绑定有可能是跨进程的，因此ServiceConnection对象必须借助于Binder才能让远程服务回调自己的方法，而ServiceDispatcher的内部类InnerConnection刚好充当了Binder这个角色
看一下ServiceConnection只是一个接口

```
public interface ServiceConnection {
    void onServiceConnected(android.content.ComponentName componentName, android.os.IBinder iBinder);

    void onServiceDisconnected(android.content.ComponentName componentName);

    default void onBindingDied(android.content.ComponentName name) { /* compiled code */ }
}
```
那么ServiceDispatcher的作用是什么呢？其实ServiceDispatcher起连接ServiceConnection和InnerConnection的作用
回到LoadedApk的`getServiceDispatcher`方法，看一下是怎么将ServiceConnection和InnerConnection连接的
这里使用mService是一个ArrayMap集合，他存储了一个应用当前活动的ServiceConnection和ServiceDispatcher的映射关系，他的定义如下
```
private final ArrayMap<Context, ArrayMap<ServiceConnection, LoadedApk.ServiceDispatcher>> mServices
      = new ArrayMap<>();
```
系统会首先查找是否存在相同的ServiceConnection,如果不存在就创建一个ServiceDispatcher对象并将其存储在mService中，其中映射关系的key是ServiceConnection,Value是LoadedApk.ServiceDispatcher。在LoadedApk.ServiceDispatcher内部又保存了ServiceConnection和InnerConnection对象。当Service和客户端建立连接之后，系统会通知InnerConnection来调用ServiceConnection中的OnServiceConnected方法，这个过程有可能是跨进程的。当ServiceDispatcher创建好了之后，getServiceDispatcher 会返回他保存的InnerConnection这个对象，接着bindServiceCOmmon方法

* 第二个
```
int res = ActivityManager.getService().bindService(
    mMainThread.getApplicationThread(), getActivityToken(), service,
    service.resolveTypeIfNeeded(getContentResolver()),
    sd, flags, getOpPackageName(), user.getIdentifier());
```
是不是有点熟悉，调用了AMS的bindService方法

```
public int bindService(IApplicationThread caller, IBinder token, Intent service,
            String resolvedType, IServiceConnection connection, int flags, String callingPackage,
            int userId) throws TransactionTooLargeException {
        enforceNotIsolatedCaller("bindService");

        // Refuse possible leaked file descriptors
        if (service != null && service.hasFileDescriptors() == true) {
            throw new IllegalArgumentException("File descriptors passed in Intent");
        }

        if (callingPackage == null) {
            throw new IllegalArgumentException("callingPackage cannot be null");
        }

        synchronized(this) {
            return mServices.bindServiceLocked(caller, token, service,
                    resolvedType, connection, flags, callingPackage, userId);
        }
}
```
接着调用ActiveServices的`bindServiceLocked`方法。
方法比较长
```

int bindServiceLocked(IApplicationThread caller, IBinder token, Intent service,
            String resolvedType, final IServiceConnection connection, int flags,
            String callingPackage, final int userId) throws TransactionTooLargeException {...
              bringUpServiceLocked
              ...}
```
这里调用了bringUpServiceLocked这个方法，看一下这个方法
```
private String bringUpServiceLocked(ServiceRecord r, int intentFlags, boolean execInFg,
        boolean whileRestarting, boolean permissionsReviewRequired)
        throws TransactionTooLargeException {
          ...
                    realStartServiceLocked(r, app, execInFg);
          ...
        }
```
bringUpServiceLocked这个方法调用了realStartServiceLocked，这里就回到了StartService要调用的那个方法了。和StartService一样。那区别在哪儿呢？
bindServiceLocked在这个方法里面也调用了requestServiceBindingLocked这个方法
```
private final boolean requestServiceBindingLocked(ServiceRecord r, IntentBindRecord i,
        boolean execInFg, boolean rebind) throws TransactionTooLargeException {
    if (r.app == null || r.app.thread == null) {
        // If service is not currently running, can't yet bind.
        return false;
    }
    if (DEBUG_SERVICE) Slog.d(TAG_SERVICE, "requestBind " + i + ": requested=" + i.requested
            + " rebind=" + rebind);
    if ((!i.requested || rebind) && i.apps.size() > 0) {
        try {
            bumpServiceExecutingLocked(r, execInFg, "bind");
            r.app.forceProcessStateUpTo(ActivityManager.PROCESS_STATE_SERVICE);
            r.app.thread.scheduleBindService(r, i.intent.getIntent(), rebind,
                    r.app.repProcState);
            if (!rebind) {
                i.requested = true;
            }
            i.hasBound = true;
            i.doRebind = false;
        } catch (TransactionTooLargeException e) {
            // Keep the executeNesting count accurate.
            if (DEBUG_SERVICE) Slog.v(TAG_SERVICE, "Crashed while binding " + r, e);
            final boolean inDestroying = mDestroyingServices.contains(r);
            serviceDoneExecutingLocked(r, inDestroying, inDestroying);
            throw e;
        } catch (RemoteException e) {
            if (DEBUG_SERVICE) Slog.v(TAG_SERVICE, "Crashed while binding " + r);
            // Keep the executeNesting count accurate.
            final boolean inDestroying = mDestroyingServices.contains(r);
            serviceDoneExecutingLocked(r, inDestroying, inDestroying);
            return false;
        }
    }
    return true;
}
```
requestServiceBindingLocked里面有一个比较关键的代码
```
r.app.thread.scheduleBindService(r, i.intent.getIntent(), rebind,
        r.app.repProcState);
```

这里又回到了ActivityThread
```
public final void scheduleBindService(IBinder token, Intent intent,
        boolean rebind, int processState) {
    updateProcessState(processState, false);
    BindServiceData s = new BindServiceData();
    s.token = token;
    s.intent = intent;
    s.rebind = rebind;

    if (DEBUG_SERVICE)
        Slog.v(TAG, "scheduleBindService token=" + token + " intent=" + intent + " uid="
                + Binder.getCallingUid() + " pid=" + Binder.getCallingPid());
    sendMessage(H.BIND_SERVICE, s);
}

```
使用handler H这个接受事件

```
case BIND_SERVICE:
    Trace.traceBegin(Trace.TRACE_TAG_ACTIVITY_MANAGER, "serviceBind");
    handleBindService((BindServiceData)msg.obj);
    Trace.traceEnd(Trace.TRACE_TAG_ACTIVITY_MANAGER);
```
在H接收到的BIND_SERVICE这类消息时，会交给handleBindService来处理，在handleBindService过程中，首先根据Service的Token去除Service对象，然后调用Service的onBind方法，Service的onBInd方法返回一个Binder对象给客户端使用，这个过程就比较熟悉了，但是onBind是Service的方法，这个时候客户端并不知道已经成功连接Service了，所以还必须调用客户端的ServiceConnection中的onServiceConnected,这个过程是有AMS的publishService来完成的

看一下关键代码handleBindService
Service有一个特性，就是多次绑定时，onBind方法只调用一次，除非Service终止了。
```
private void handleBindService(BindServiceData data) {
    Service s = mServices.get(data.token);
    if (DEBUG_SERVICE)
        Slog.v(TAG, "handleBindService s=" + s + " rebind=" + data.rebind);
    if (s != null) {
        try {
            data.intent.setExtrasClassLoader(s.getClassLoader());
            data.intent.prepareToEnterProcess();
            try {
                if (!data.rebind) {
                    IBinder binder = s.onBind(data.intent);
                    ActivityManager.getService().publishService(
                            data.token, data.intent, binder);
                } else {
                    s.onRebind(data.intent);
                    ActivityManager.getService().serviceDoneExecuting(
                            data.token, SERVICE_DONE_EXECUTING_ANON, 0, 0);
                }
                ensureJitEnabled();
            } catch (RemoteException ex) {
                throw ex.rethrowFromSystemServer();
            }
        } catch (Exception e) {
            if (!mInstrumentation.onException(s, e)) {
                throw new RuntimeException(
                        "Unable to bind to service " + s
                        + " with " + data.intent + ": " + e.toString(), e);
            }
        }
    }
}
```
AMS的publishService方法
```
public void publishService(IBinder token, Intent intent, IBinder service) {
    // Refuse possible leaked file descriptors
    if (intent != null && intent.hasFileDescriptors() == true) {
        throw new IllegalArgumentException("File descriptors passed in Intent");
    }

    synchronized(this) {
        if (!(token instanceof ServiceRecord)) {
            throw new IllegalArgumentException("Invalid service token");
        }
        mServices.publishServiceLocked((ServiceRecord)token, intent, service);
    }
}
```
从这里可以看出AMS的publishService方法将工作交给了ActiveServices来处理。

```
void publishServiceLocked(ServiceRecord r, Intent intent, IBinder service) {
    final long origId = Binder.clearCallingIdentity();
    try {
        if (DEBUG_SERVICE) Slog.v(TAG_SERVICE, "PUBLISHING " + r
                + " " + intent + ": " + service);
        if (r != null) {
            Intent.FilterComparison filter
                    = new Intent.FilterComparison(intent);
            IntentBindRecord b = r.bindings.get(filter);
            if (b != null && !b.received) {
                b.binder = service;
                b.requested = true;
                b.received = true;
                for (int conni=r.connections.size()-1; conni>=0; conni--) {
                    ArrayList<ConnectionRecord> clist = r.connections.valueAt(conni);
                    for (int i=0; i<clist.size(); i++) {
                        ConnectionRecord c = clist.get(i);
                        if (!filter.equals(c.binding.intent.intent)) {
                            if (DEBUG_SERVICE) Slog.v(
                                    TAG_SERVICE, "Not publishing to: " + c);
                            if (DEBUG_SERVICE) Slog.v(
                                    TAG_SERVICE, "Bound intent: " + c.binding.intent.intent);
                            if (DEBUG_SERVICE) Slog.v(
                                    TAG_SERVICE, "Published intent: " + intent);
                            continue;
                        }
                        if (DEBUG_SERVICE) Slog.v(TAG_SERVICE, "Publishing to: " + c);
                        try {
                            c.conn.connected(r.name, service, false);
                        } catch (Exception e) {
                            Slog.w(TAG, "Failure sending service " + r.name +
                                  " to connection " + c.conn.asBinder() +
                                  " (in " + c.binding.client.processName + ")", e);
                        }
                    }
                }
            }

            serviceDoneExecutingLocked(r, mDestroyingServices.contains(r), false);
        }
    } finally {
        Binder.restoreCallingIdentity(origId);
    }
}
```
ActiveServices的publishService方法很长，但是核心代码只有一句话`c.conn.connected(r.name, service, false);`
c是ConnectionRecord，c.conn是ServiceDispatcher.InnerConnection，service就是Service的onBind方法返回的binder对象。

这里在回来看一下InnerConnection

```
private static class InnerConnection extends IServiceConnection.Stub {
    final WeakReference<LoadedApk.ServiceDispatcher> mDispatcher;

    InnerConnection(LoadedApk.ServiceDispatcher sd) {
        mDispatcher = new WeakReference<LoadedApk.ServiceDispatcher>(sd);
    }

    public void connected(ComponentName name, IBinder service, boolean dead)
            throws RemoteException {
        LoadedApk.ServiceDispatcher sd = mDispatcher.get();
        if (sd != null) {
            sd.connected(name, service, dead);
        }
    }
}

```
看到connected这个方法调用的是ServiceDispatcher的connected方法，看一下他的具体实现

```
public void connected(ComponentName name, IBinder service, boolean dead) {
      if (mActivityThread != null) {
          mActivityThread.post(new RunConnection(name, service, 0, dead));
      } else {
          doConnected(name, service, dead);
      }
  }
```
通常情况下mActivityThread是不为空的，这样一来，就通过post方法回到主线程，而RunConnection的定义如下
```
private final class RunConnection implements Runnable {
          RunConnection(ComponentName name, IBinder service, int command, boolean dead) {
              mName = name;
              mService = service;
              mCommand = command;
              mDead = dead;
          }

          public void run() {
              if (mCommand == 0) {
                  doConnected(mName, mService, mDead);
              } else if (mCommand == 1) {
                  doDeath(mName, mService);
              }
          }

          final ComponentName mName;
          final IBinder mService;
          final int mCommand;
          final boolean mDead;
      }
```
这里判断了命令状态，调用ServiceDispatcher的doConnected方法
```
public void doConnected(ComponentName name, IBinder service, boolean dead) {
        ServiceDispatcher.ConnectionInfo old;
        ServiceDispatcher.ConnectionInfo info;

        synchronized (this) {
            if (mForgotten) {
                // We unbound before receiving the connection; ignore
                // any connection received.
                return;
            }
            old = mActiveConnections.get(name);
            if (old != null && old.binder == service) {
                // Huh, already have this one.  Oh well!
                return;
            }

            if (service != null) {
                // A new service is being connected... set it all up.
                info = new ConnectionInfo();
                info.binder = service;
                info.deathMonitor = new DeathMonitor(name, service);
                try {
                    service.linkToDeath(info.deathMonitor, 0);
                    mActiveConnections.put(name, info);
                } catch (RemoteException e) {
                    // This service was dead before we got it...  just
                    // don't do anything with it.
                    mActiveConnections.remove(name);
                    return;
                }

            } else {
                // The named service is being disconnected... clean up.
                mActiveConnections.remove(name);
            }

            if (old != null) {
                old.binder.unlinkToDeath(old.deathMonitor, 0);
            }
        }

        // If there was an old service, it is now disconnected.
        if (old != null) {
            mConnection.onServiceDisconnected(name);
        }
        if (dead) {
            mConnection.onBindingDied(name);
        }
        // If there is a new service, it is now connected.
        if (service != null) {
            mConnection.onServiceConnected(name, service);
        }
    }
```
因为ServiceDispatcher内部保存了客户端ServiceConnection对象，因此可以很方便的调用ServiceConnection对象的onServiceConnected方法
```  
if (service != null) {
    mConnection.onServiceConnected(name, service);
}
```
onServiceConnected就是连接成功的回调
这里Service的绑定过程就解决了
这里的UML图
![Alt text](Service绑定流程图.svg "Service 启动,最好下载下来看")
# BroadcastReceiver的工作过程
这一章节介绍BroadCastReceiver的工作过程，主要包含两个方面，一个是广播注册过程，一个是广播发送和接受过程。这里先简单回顾一下广播的使用方法
```
public class TestBR extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

    }
}

```
然后在清单文件中注册
```
 <receiver android:name=".TestBR" />
```
看一下动态注册
```
IntentFilter filter=new IntentFilter();
   filter.addAction("ccc");
   registerReceiver(new TestBR(),filter);
```
发送广播
```  
Intent intent =new Intent();
intent.setAction("ccc");
sendBroadcast(intent);
```

## 广播的注册过程
广播的注册分为动态注册和静态注册，其中静态注册的广播在应用安装的时候由系统自动完成，具体的说是由PMS（PackageManagerService）来完成整个注册过程的，除了广播以外，其他三大组件也是在应用安装时由PMS解析并注册的。这里只研究动态注册。

看下面动态注册的代码
```
IntentFilter filter=new IntentFilter();
   filter.addAction("ccc");
   registerReceiver(new TestBR(),filter);
```
可以发现，`registerReceiver`方法方法调用了其实是调用了ContextWrapper的，我们直接跳转到ContextWrapper，看一下他的`registerReceiver`方法
```
    @Override
    public Intent registerReceiver(
        BroadcastReceiver receiver, IntentFilter filter) {
        return mBase.registerReceiver(receiver, filter);
    }
```
mBase是ContextImpl,看一下他的`registerReceiver`方法

```
@Override
public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter,
        String broadcastPermission, Handler scheduler) {
    return registerReceiverInternal(receiver, getUserId(),
            filter, broadcastPermission, scheduler, getOuterContext(), 0);
}

```
可以看到调用了自己的`registerReceiverInternal`方法
```
private Intent registerReceiverInternal(BroadcastReceiver receiver, int userId,
        IntentFilter filter, String broadcastPermission,
        Handler scheduler, Context context, int flags) {
    IIntentReceiver rd = null;
    if (receiver != null) {
        if (mPackageInfo != null && context != null) {
            if (scheduler == null) {
                scheduler = mMainThread.getHandler();
            }
            rd = mPackageInfo.getReceiverDispatcher(
                receiver, context, scheduler,
                mMainThread.getInstrumentation(), true);
        } else {
            if (scheduler == null) {
                scheduler = mMainThread.getHandler();
            }
            rd = new LoadedApk.ReceiverDispatcher(
                    receiver, context, scheduler, null, true).getIIntentReceiver();
        }
    }
    try {
        final Intent intent = ActivityManager.getService().registerReceiver(
                mMainThread.getApplicationThread(), mBasePackageName, rd, filter,
                broadcastPermission, userId, flags);
        if (intent != null) {
            intent.setExtrasClassLoader(getClassLoader());
            intent.prepareToEnterProcess();
        }
        return intent;
    } catch (RemoteException e) {
        throw e.rethrowFromSystemServer();
    }
}
```
看一下这个关键的代码
```
rd = mPackageInfo.getReceiverDispatcher(
    receiver, context, scheduler,
    mMainThread.getInstrumentation(), true);
```

这里是不是很熟悉，service的bind过程也使用了类似的方式
mPackageInfo是一个LoadedApk

```
    public IIntentReceiver getReceiverDispatcher(BroadcastReceiver r,
            Context context, Handler handler,
            Instrumentation instrumentation, boolean registered) {
        synchronized (mReceivers) {
            LoadedApk.ReceiverDispatcher rd = null;
            ArrayMap<BroadcastReceiver, LoadedApk.ReceiverDispatcher> map = null;
            if (registered) {
                map = mReceivers.get(context);
                if (map != null) {
                    rd = map.get(r);
                }
            }
            if (rd == null) {
                rd = new ReceiverDispatcher(r, context, handler,
                        instrumentation, registered);
                if (registered) {
                    if (map == null) {
                        map = new ArrayMap<BroadcastReceiver, LoadedApk.ReceiverDispatcher>();
                        mReceivers.put(context, map);
                    }
                    map.put(r, rd);
                }
            } else {
                rd.validate(context, handler);
            }
            rd.mForgotten = false;
            return rd.getIIntentReceiver();
        }
    }
```

看一下ReceiverDispatcher的构造方法,如果猜的没错，这里应该有一个ReceiverDispatcher的内部类InnerReceiver，并且继承IIntentReceiver.Stub，和service的绑定过程一样

```
calss ReceiverDispatcher{
  ...
ReceiverDispatcher(BroadcastReceiver receiver, Context context,
        Handler activityThread, Instrumentation instrumentation,
        boolean registered) {
    if (activityThread == null) {
        throw new NullPointerException("Handler must not be null");
    }

    mIIntentReceiver = new InnerReceiver(this, !registered);
    mReceiver = receiver;
    mContext = context;
    mActivityThread = activityThread;
    mInstrumentation = instrumentation;
    mRegistered = registered;
    mLocation = new IntentReceiverLeaked(null);
    mLocation.fillInStackTrace();
}

final static class InnerReceiver extends IIntentReceiver.Stub {
    final WeakReference<LoadedApk.ReceiverDispatcher> mDispatcher;
    final LoadedApk.ReceiverDispatcher mStrongRef;

    InnerReceiver(LoadedApk.ReceiverDispatcher rd, boolean strong) {
        mDispatcher = new WeakReference<LoadedApk.ReceiverDispatcher>(rd);
        mStrongRef = strong ? rd : null;
    }
}
}
```
这里列出了一些关键的代码，其余部分省略。看到和Service的bind过程一样
然后第二步
```
final Intent intent = ActivityManager.getService().registerReceiver(
        mMainThread.getApplicationThread(), mBasePackageName, rd, filter,
        broadcastPermission, userId, flags);
```
得到AMS并调用他的registerReceiver方法
来看一下这个registerReceiver方法
方法比较长，看重点
```
public Intent registerReceiver(IApplicationThread caller, String callerPackage,
            IIntentReceiver receiver, IntentFilter filter, String permission, int userId,
            int flags) {
              if (rl == null) {
                  rl = new ReceiverList(this, callerApp, callingPid, callingUid,
                          userId, receiver);
                }
                mRegisteredReceivers.put(receiver.asBinder(), rl);
                BroadcastFilter bf = new BroadcastFilter(filter, rl, callerPackage,
                        permission, callingUid, userId, instantApp, visibleToInstantApps);
                rl.add(bf);
                if (!bf.debugCheck()) {
                    Slog.w(TAG, "==> For Dynamic broadcast");
                }
                mReceiverResolver.addFilter(bf);

            }
```
最终会把远程的InnerReceiver对象以及IntentFilter对象存储起来
## 广播的发送和接受过程
通过上面分析发现，广播的注册过程还是比较简单的,下面来分析一下广播的发送和接受过程。当通过send方法来发送广播时，AMS会查找出匹配的广播接受者，并将广播发送给他们处理。广播发送分为几种类型：普通广播、有序广播、粘性广播。有序广播和粘性广播与普通广播相比具有不同的特性，但是他们发送过程和接受过程是类似的，因此这里只分析普通广播。
广播的发送和接收，其本质是一个过程的两个阶段。这里从广播的发送说起，广播的发送任然起始于ContextWrapper的sendBroadcast方法，之所以不是Context，那是因为Context的sendBroadcast是一个抽象方法。具体的做法都是在ContextImpl中处理的
看一下调用
首先是在activity中发送广播
```
Intent intent =new Intent();
intent.setAction("ccc");
sendBroadcast(intent);
```
这时会调用ContextWrapper的`sendBroadcast`方法，接着调用ContextImp的`sendBroadcast`方法
```
@Override
public void sendBroadcast(Intent intent) {
    warnIfCallingFromSystemProcess();
    String resolvedType = intent.resolveTypeIfNeeded(getContentResolver());
    try {
        intent.prepareToLeaveProcess(this);
        ActivityManager.getService().broadcastIntent(
                mMainThread.getApplicationThread(), intent, resolvedType, null,
                Activity.RESULT_OK, null, null, null, AppOpsManager.OP_NONE, null, false, false,
                getUserId());
    } catch (RemoteException e) {
        throw e.rethrowFromSystemServer();
    }
}

```
可以看到`sendBroadcast`基本没有干什么事情，他是通过AMS来发送广播的，我们看一下AMS的`broadcastIntent`方法
```
public final int broadcastIntent(IApplicationThread caller,
        Intent intent, String resolvedType, IIntentReceiver resultTo,
        int resultCode, String resultData, Bundle resultExtras,
        String[] requiredPermissions, int appOp, Bundle bOptions,
        boolean serialized, boolean sticky, int userId) {
    enforceNotIsolatedCaller("broadcastIntent");
    synchronized(this) {
        intent = verifyBroadcastLocked(intent);

        final ProcessRecord callerApp = getRecordForAppLocked(caller);
        final int callingPid = Binder.getCallingPid();
        final int callingUid = Binder.getCallingUid();
        final long origId = Binder.clearCallingIdentity();
        int res = broadcastIntentLocked(callerApp,
                callerApp != null ? callerApp.info.packageName : null,
                intent, resolvedType, resultTo, resultCode, resultData, resultExtras,
                requiredPermissions, appOp, bOptions, serialized, sticky,
                callingPid, callingUid, userId);
        Binder.restoreCallingIdentity(origId);
        return res;
    }
}

```
可以看到这个方法内部又调用了`broadcastIntentLocked`方法,来看一下这个方法
```
final int broadcastIntentLocked(ProcessRecord callerApp,
        String callerPackage, Intent intent, String resolvedType,
        IIntentReceiver resultTo, int resultCode, String resultData,
        Bundle resultExtras, String[] requiredPermissions, int appOp, Bundle bOptions,
        boolean ordered, boolean sticky, int callingPid, int callingUid, int userId) {
          ...
          intent.addFlags(Intent.FLAG_EXCLUDE_STOPPED_PACKAGES);
          ...
          if (!ordered && NR > 0) {
              // If we are not serializing this broadcast, then send the
              // registered receivers separately so they don't wait for the
              // components to be launched.
              if (isCallerSystem) {
                  checkBroadcastFromSystem(intent, callerApp, callerPackage, callingUid,
                          isProtectedBroadcast, registeredReceivers);
              }
              final BroadcastQueue queue = broadcastQueueForIntent(intent);
              BroadcastRecord r = new BroadcastRecord(queue, intent, callerApp,
                      callerPackage, callingPid, callingUid, callerInstantApp, resolvedType,
                      requiredPermissions, appOp, brOptions, registeredReceivers, resultTo,
                      resultCode, resultData, resultExtras, ordered, sticky, false, userId);
              if (DEBUG_BROADCAST) Slog.v(TAG_BROADCAST, "Enqueueing parallel broadcast " + r);
              final boolean replaced = replacePending
                      && (queue.replaceParallelBroadcastLocked(r) != null);
              // Note: We assume resultTo is null for non-ordered broadcasts.
              if (!replaced) {
                  queue.enqueueParallelBroadcastLocked(r);
                  queue.scheduleBroadcastsLocked();
              }
              registeredReceivers = null;
              NR = 0;
          }
            ...
        }
```
方法比较长，列出重要的步骤
这里拆解分析一下

```
  intent.addFlags(Intent.FLAG_EXCLUDE_STOPPED_PACKAGES);
```
这个表示在Android5.0中，默认情况下广播不会发送给已经停止的应用，其实不仅仅是Android5.0，从Android3.1开始广播已经具有这种特性了。这时因为系统在Android3.1中添加了两个Intent标记位：FLAG_INCLUDE_STOPPED_PACKAGES和FLAG_EXCLUDE_STOPPED_PACKAGES,用来控制广播是否要对处于停滞状态的应用其作用
FLAG_INCLUDE_STOPPED_PACKAGES：表示对已经停止的应用奏效，就是说广播会发给已经停止的应用
FLAG_EXCLUDE_STOPPED_PACKAGES：表示对已经停止的应用不奏效，就是说广播会不发给已经停止的应用
从Android3.1开始，系统为所有的广播默认添加了FLAG_EXCLUDE_STOPPED_PACKAGES标致，这样做是为了防止广播无意间或者在不必要的时候调起已经停止运行的应用。如果的确需要调起未启动的应用，只需要在Intent添加FLAG_INCLUDE_STOPPED_PACKAGES这个flag就可以了。当这两者同时存在时，FLAG_INCLUDE_STOPPED_PACKAGES的优先级高于FLAG_EXCLUDE_STOPPED_PACKAGES.
在`broadcastIntentLocked`的内部，会根据intent-filter查找出匹配的广播接受者并经过一系列条件的过滤，最终将满足条件的广播接受者添加到BroadcastQueue中，接着BroadcastQueue就会将广播发送给相应的广播接受者

```
if (!ordered && NR > 0) {
    // If we are not serializing this broadcast, then send the
    // registered receivers separately so they don't wait for the
    // components to be launched.
    if (isCallerSystem) {
        checkBroadcastFromSystem(intent, callerApp, callerPackage, callingUid,
                isProtectedBroadcast, registeredReceivers);
    }
    final BroadcastQueue queue = broadcastQueueForIntent(intent);
    BroadcastRecord r = new BroadcastRecord(queue, intent, callerApp,
            callerPackage, callingPid, callingUid, callerInstantApp, resolvedType,
            requiredPermissions, appOp, brOptions, registeredReceivers, resultTo,
            resultCode, resultData, resultExtras, ordered, sticky, false, userId);
    if (DEBUG_BROADCAST) Slog.v(TAG_BROADCAST, "Enqueueing parallel broadcast " + r);
    final boolean replaced = replacePending
            && (queue.replaceParallelBroadcastLocked(r) != null);
    // Note: We assume resultTo is null for non-ordered broadcasts.
    if (!replaced) {
        queue.enqueueParallelBroadcastLocked(r);
        queue.scheduleBroadcastsLocked();
    }
    registeredReceivers = null;
    NR = 0;
}

```
可以看到在`broadcastIntentLocked`方法中调用了BroadcastQueue的`scheduleBroadcastsLocked`方法,看一下这个方法做了什么

```
public void scheduleBroadcastsLocked() {
    if (DEBUG_BROADCAST) Slog.v(TAG_BROADCAST, "Schedule broadcasts ["
            + mQueueName + "]: current="
            + mBroadcastsScheduled);

    if (mBroadcastsScheduled) {
        return;
    }
    mHandler.sendMessage(mHandler.obtainMessage(BROADCAST_INTENT_MSG, this));
    mBroadcastsScheduled = true;
}

```
可以看到他并没有立即发送广播，而是使用了Handler发送了一条消息BROADCAST_INTENT_MSG
看一下这个handler

```
private final class BroadcastHandler extends Handler {
    public BroadcastHandler(Looper looper) {
        super(looper, null, true);
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case BROADCAST_INTENT_MSG: {
                if (DEBUG_BROADCAST) Slog.v(
                        TAG_BROADCAST, "Received BROADCAST_INTENT_MSG");
                processNextBroadcast(true);
            } break;
            case BROADCAST_TIMEOUT_MSG: {
                synchronized (mService) {
                    broadcastTimeoutLocked(true);
                }
            } break;
        }
    }
}

```
可以看到其实是调用了`processNextBroadcast`这个方法,看一下这个方法。这个方法有点长，选重要的部分
```
while (mParallelBroadcasts.size() > 0) {
    r = mParallelBroadcasts.remove(0);
    r.dispatchTime = SystemClock.uptimeMillis();
    r.dispatchClockTime = System.currentTimeMillis();

    if (Trace.isTagEnabled(Trace.TRACE_TAG_ACTIVITY_MANAGER)) {
        Trace.asyncTraceEnd(Trace.TRACE_TAG_ACTIVITY_MANAGER,
            createBroadcastTraceTitle(r, BroadcastRecord.DELIVERY_PENDING),
            System.identityHashCode(r));
        Trace.asyncTraceBegin(Trace.TRACE_TAG_ACTIVITY_MANAGER,
            createBroadcastTraceTitle(r, BroadcastRecord.DELIVERY_DELIVERED),
            System.identityHashCode(r));
    }

    final int N = r.receivers.size();
    if (DEBUG_BROADCAST_LIGHT) Slog.v(TAG_BROADCAST, "Processing parallel broadcast ["
            + mQueueName + "] " + r);
    for (int i=0; i<N; i++) {
        Object target = r.receivers.get(i);
        if (DEBUG_BROADCAST)  Slog.v(TAG_BROADCAST,
                "Delivering non-ordered on [" + mQueueName + "] to registered "
                + target + ": " + r);
        deliverToRegisteredReceiverLocked(r, (BroadcastFilter)target, false, i);
    }
    addBroadcastToHistoryLocked(r);
    if (DEBUG_BROADCAST_LIGHT) Slog.v(TAG_BROADCAST, "Done with parallel broadcast ["
            + mQueueName + "] " + r);
}

```
这里会遍历mParallelBroadcasts并将其中的广播发送给他们所有的接受者，具体发送过程是通过`deliverToRegisteredReceiverLocked`方法来发送的，
这个方法比较长，看关键的部分
```
...
performReceiveLocked(filter.receiverList.app, filter.receiverList.receiver,
        new Intent(r.intent), r.resultCode, r.resultData,
        r.resultExtras, r.ordered, r.initialSticky, r.userId);
...
```

```

    void performReceiveLocked(ProcessRecord app, IIntentReceiver receiver,
            Intent intent, int resultCode, String data, Bundle extras,
            boolean ordered, boolean sticky, int sendingUser) throws RemoteException {
        // Send the intent to the receiver asynchronously using one-way binder calls.
        if (app != null) {
            if (app.thread != null) {
                // If we have an app thread, do the call through that so it is
                // correctly ordered with other one-way calls.
                try {
                    app.thread.scheduleRegisteredReceiver(receiver, intent, resultCode,
                            data, extras, ordered, sticky, sendingUser, app.repProcState);
                // TODO: Uncomment this when (b/28322359) is fixed and we aren't getting
                // DeadObjectException when the process isn't actually dead.
                //} catch (DeadObjectException ex) {
                // Failed to call into the process.  It's dying so just let it die and move on.
                //    throw ex;
                } catch (RemoteException ex) {
                    // Failed to call into the process. It's either dying or wedged. Kill it gently.
                    synchronized (mService) {
                        Slog.w(TAG, "Can't deliver broadcast to " + app.processName
                                + " (pid " + app.pid + "). Crashing it.");
                        app.scheduleCrash("can't deliver broadcast");
                    }
                    throw ex;
                }
            } else {
                // Application has died. Receiver doesn't exist.
                throw new RemoteException("app.thread must not be null");
            }
        } else {
            receiver.performReceive(intent, resultCode, data, extras, ordered,
                    sticky, sendingUser);
        }
    }
```
可以看到这里
```
app.thread.scheduleRegisteredReceiver(receiver, intent, resultCode,
        data, extras, ordered, sticky, sendingUser, app.repProcState);
```
是不是有点印象，

看一下ActivityThread中的这个方法
```
public void scheduleRegisteredReceiver(IIntentReceiver receiver, Intent intent,
        int resultCode, String dataStr, Bundle extras, boolean ordered,
        boolean sticky, int sendingUser, int processState) throws RemoteException {
    updateProcessState(processState, false);
    receiver.performReceive(intent, resultCode, dataStr, extras, ordered,
            sticky, sendingUser);
}
```
这里的receiver是一个InnerReceiver
```
@Override
public void performReceive(Intent intent, int resultCode, String data,
        Bundle extras, boolean ordered, boolean sticky, int sendingUser) {
    final LoadedApk.ReceiverDispatcher rd;
    if (intent == null) {
        Log.wtf(TAG, "Null intent received");
        rd = null;
    } else {
        rd = mDispatcher.get();
    }
    if (ActivityThread.DEBUG_BROADCAST) {
        int seq = intent.getIntExtra("seq", -1);
        Slog.i(ActivityThread.TAG, "Receiving broadcast " + intent.getAction()
                + " seq=" + seq + " to " + (rd != null ? rd.mReceiver : null));
    }
    if (rd != null) {
        rd.performReceive(intent, resultCode, data, extras,
                ordered, sticky, sendingUser);
    } else {
        // The activity manager dispatched a broadcast to a registered
        // receiver in this process, but before it could be delivered the
        // receiver was unregistered.  Acknowledge the broadcast on its
        // behalf so that the system's broadcast sequence can continue.
        if (ActivityThread.DEBUG_BROADCAST) Slog.i(ActivityThread.TAG,
                "Finishing broadcast to unregistered receiver");
        IActivityManager mgr = ActivityManager.getService();
        try {
            if (extras != null) {
                extras.setAllowFds(false);
            }
            mgr.finishReceiver(this, resultCode, data, extras, false, intent.getFlags());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }
}
```
他有调用了ReceiverDispatcher的`performReceive`方法
```
public void performReceive(Intent intent, int resultCode, String data,
        Bundle extras, boolean ordered, boolean sticky, int sendingUser) {
    final Args args = new Args(intent, resultCode, data, extras, ordered,
            sticky, sendingUser);
    if (intent == null) {
        Log.wtf(TAG, "Null intent received");
    } else {
        if (ActivityThread.DEBUG_BROADCAST) {
            int seq = intent.getIntExtra("seq", -1);
            Slog.i(ActivityThread.TAG, "Enqueueing broadcast " + intent.getAction()
                    + " seq=" + seq + " to " + mReceiver);
        }
    }
    if (intent == null || !mActivityThread.post(args.getRunnable())) {
        if (mRegistered && ordered) {
            IActivityManager mgr = ActivityManager.getService();
            if (ActivityThread.DEBUG_BROADCAST) Slog.i(ActivityThread.TAG,
                    "Finishing sync broadcast to " + mReceiver);
            args.sendFinished(mgr);
        }
    }
}

```
先调用`!mActivityThread.post(args.getRunnable())`
看一下`getRunnable`的内容,里面有一段关键的代码
```
ClassLoader cl = mReceiver.getClass().getClassLoader();
intent.setExtrasClassLoader(cl);
intent.prepareToEnterProcess();
setExtrasClassLoader(cl);
receiver.setPendingResult(this);
receiver.onReceive(mContext, intent);
```
这里调用了`onReceive`这个方法,mActivityThread这个是一个Handler，就是ActivityThread内部的那个H

这里会创建一个args对象，然后`sendFinished`是在Args父类的方法
```
final class Args extends BroadcastReceiver.PendingResult {
  public void sendFinished(IActivityManager am) {
      synchronized (this) {
          if (mFinished) {
              throw new IllegalStateException("Broadcast already finished");
          }
          mFinished = true;

          try {
              if (mResultExtras != null) {
                  mResultExtras.setAllowFds(false);
              }
              if (mOrderedHint) {
                  am.finishReceiver(mToken, mResultCode, mResultData, mResultExtras,
                          mAbortBroadcast, mFlags);
              } else {
                  // This broadcast was sent to a component; it is not ordered,
                  // but we still need to tell the activity manager we are done.
                  am.finishReceiver(mToken, 0, null, null, false, mFlags);
              }
          } catch (RemoteException ex) {
          }
      }
  }

}
```
am是AMS,最后调用了AMS的`finishReceiver`方法
```
public void finishReceiver(IBinder who, int resultCode, String resultData,
        Bundle resultExtras, boolean resultAbort, int flags) {
    if (DEBUG_BROADCAST) Slog.v(TAG_BROADCAST, "Finish receiver: " + who);

    // Refuse possible leaked file descriptors
    if (resultExtras != null && resultExtras.hasFileDescriptors()) {
        throw new IllegalArgumentException("File descriptors passed in Bundle");
    }

    final long origId = Binder.clearCallingIdentity();
    try {
        boolean doNext = false;
        BroadcastRecord r;

        synchronized(this) {
            BroadcastQueue queue = (flags & Intent.FLAG_RECEIVER_FOREGROUND) != 0
                    ? mFgBroadcastQueue : mBgBroadcastQueue;
            r = queue.getMatchingOrderedReceiver(who);
            if (r != null) {
                doNext = r.queue.finishReceiverLocked(r, resultCode,
                    resultData, resultExtras, resultAbort, true);
            }
        }

        if (doNext) {
            r.queue.processNextBroadcast(false);
        }
        trimApplications();
    } finally {
        Binder.restoreCallingIdentity(origId);
    }
}

```
这里面有是一个循环
# ContentProvider工作过程
