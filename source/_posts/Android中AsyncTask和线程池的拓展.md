---
title: Android中AsyncTask和线程池的拓展
date: 2018-10-30 22:13:34
tags:
- Android
- 杂记
- 进阶
---

# AsyncTask的一些问题
今天在复习AsyncTask的源码，看之前写过的博客。发现自己有很多东西在之前的博客中忽略了。
## 第一个是同一个AsyncTask同时执行不同的任务
来看一下这一段代码，这个是在使用AsyncTask时，调用执行任务的一段代码，如果当前的状态不是**PENDING**，他会报错。
```
 public final AsyncTask<Params, Progress, Result> executeOnExecutor(Executor exec,
            Params... params) {
        if (mStatus != Status.PENDING) {
            switch (mStatus) {
                case RUNNING:
                    throw new IllegalStateException("Cannot execute task:"
                            + " the task is already running.");
                case FINISHED:
                    throw new IllegalStateException("Cannot execute task:"
                            + " the task has already been executed "
                            + "(a task can be executed only once)");
            }
        }

        mStatus = Status.RUNNING;

        onPreExecute();

        mWorker.mParams = params;
        exec.execute(mFuture);

        return this;
    }
```

看一下下面的例子
```
 AsyncTask asyncTask=new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                try {
                    //模拟耗时操作
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
            }

            @Override
            protected void onProgressUpdate(Object[] values) {
                super.onProgressUpdate(values);
            }

            @Override
            protected void onCancelled(Object o) {
                super.onCancelled(o);
            }

            @Override
            protected void onCancelled() {
                super.onCancelled();
            }
        };
        asyncTask.execute("x");
        asyncTask.execute("x456");
    }
```

然后会奔溃

```
2018-10-30 22:10:21.243 5484-5484/com.example.groot.myapplication E/AndroidRuntime: FATAL EXCEPTION: main
    Process: com.example.groot.myapplication, PID: 5484
    java.lang.RuntimeException: Unable to start activity ComponentInfo{com.example.groot.myapplication/com.example.groot.myapplication.MainActivity}: java.lang.IllegalStateException: Cannot execute task: the task is already running.
        at android.app.ActivityThread.performLaunchActivity(ActivityThread.java:2913)
        at android.app.ActivityThread.handleLaunchActivity(ActivityThread.java:3048)
        at android.app.servertransaction.LaunchActivityItem.execute(LaunchActivityItem.java:78)
        at android.app.servertransaction.TransactionExecutor.executeCallbacks(TransactionExecutor.java:108)
        at android.app.servertransaction.TransactionExecutor.execute(TransactionExecutor.java:68)
        at android.app.ActivityThread$H.handleMessage(ActivityThread.java:1808)
        at android.os.Handler.dispatchMessage(Handler.java:106)
        at android.os.Looper.loop(Looper.java:193)
        at android.app.ActivityThread.main(ActivityThread.java:6669)
        at java.lang.reflect.Method.invoke(Native Method)
        at com.android.internal.os.RuntimeInit$MethodAndArgsCaller.run(RuntimeInit.java:493)
        at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:858)
     Caused by: java.lang.IllegalStateException: Cannot execute task: the task is already running.
        at android.os.AsyncTask.executeOnExecutor(AsyncTask.java:637)
        at android.os.AsyncTask.execute(AsyncTask.java:595)
        at com.example.groot.myapplication.MainActivity.onCreate(MainActivity.java:72)
        at android.app.Activity.performCreate(Activity.java:7136)
        at android.app.Activity.performCreate(Activity.java:7127)
        at android.app.Instrumentation.callActivityOnCreate(Instrumentation.java:1271)
        at android.app.ActivityThread.performLaunchActivity(ActivityThread.java:2893)
```

这个表明同一个AsyncTask只能在同一时间执行一个耗时任务，如果执行多个，会报错...见识了。

## 第二个问题，AsyncTask内部使用了线程池，但是他是如何把asyncTask的几个方法连接连接起来的，之前的博客没有讲清楚。
在回答这个问题之前，首先看一下AsyncTask的构造方法



## 第三个问题线程池复用到底是什么东西？为什么可以复用，不用创建多个线程？

## 第四个问题，线程池中的线程的start方法在什么时候调用