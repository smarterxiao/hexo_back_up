---
title: Android开发艺术探索 第十一章 Android的线程和线程池
date: 2018-01-27 17:08:32
top : 211
tags:
- 进阶
- Android开发艺术探索
categories: android
---

>本节的主题是Android中的线程和线程池。线程在Android中是一个很重要的概念，从用途上来说,线程主要分为主线程和子线程，主线程主要处理和界面相关的事情，而子线程往往用于耗时操作。由于Android的特性，如果在主线程中执行耗时操作那么就会导致程序无法及时的相应，因此耗时操作必须放在子线程中去执行。除了Thread本身以外，在Android中可以扮演线程角色的还有很多，比如AsyncTask和IntentService，同时HandlerThread也是一种特殊的线程。尽管AsyncTask、IntentService以及HandlerThread的表现形式都有别于传统的线程，但是他们的本质任然是传统的线程。对于AsyncTask来说，他的底层使用到线程池，对于IntentService和HandlerThread来说，他们的底层直接使用了线程。
不同形式的线程虽然都是线程，但是他们任然具有不同的特性和使用场景。AsyncTask封装了线程池和Handler，他主要是为了方便开发者在子线程中更新UI。HandlerThread是一种具有消息循环的线程，在他的内部可以使用Handler。IntentService是一个服务，系统对其进行了封装使其可以更方便的执行后台任务，IntentService内部采用HandlerThread来执行任务，当任务执行完毕后IntentService会自动退出。从任务执行的角度来看，IntentService的作用更像是一个后台线程，但是IntentService是一种服务，他不容易被系统杀死从而尽量保证任务的执行，而如果是一个后台线程，由于这个进程中没有活动的四大组件，那么这个进程的优先级会非常低，很容易被系统杀死，这就是IntentService的优点。
在操作系统中，线程是操作系统调度的最小单元，同时线程又是一种受限的系统资源，即线程不可能无限制的产生，并且线程的创建和销毁都会有相应的开销。当系统中存在大量线程时，系统会通过时间片轮转的方式调度每个线程，因此线程不可能做到绝对的秉性，除非线程数量小于等于CPU核心数，一般来说这个是不可能的。想象一下，如果一个进程中频繁的创建和销毁线程，这显然不是高效的做法。正确的做法是采用线程池，一个线程池中会缓存一定数量的线程，通过线程池就可以避免因为频繁创建和销毁线程而带来的系统开销。Android中的线程池来源于java，主要是通过Executor来派生特定类型的线程池，不同种类的线程池又具有各自的特性，详细内容会在后面介绍。

# 主线程和子线程
主线程是指进程所拥有的线程，在java中默认情况下一个进程只有一个线程，这个线程就是主线程，主线程主要负责界面的交互相关逻辑，因为用户随时会和界面发生交互，因此主线程在任何时候都必须有较高的响应速度，否则就会产生一种界面卡顿的感觉。为了保持较高的响应速度，这就要求主线程中不能执行耗时任务，否则就会产生一种界面卡顿的感觉。为了保持较高的响应速度，这就要求主线程中不能执行耗时任务。这个时候子线程就派上用场了。子线程也叫工作线程，除了主线程以外的线程都是子线程。
Android沿用了java的线程模型，其中的线程也分为主线程和子线程，其中主线程也叫UI线程。主线程的作用是运行四大组件以及处理他们与用户的交互，而子线程的作用则是执行耗时操作，比如IO，网络等。Android从3.0开始系统要求网络访问必须在子线程中进行，不然就会抛出NetWorkOnMainThreadException这个异常，这样做是为了避免主线程由于被耗时操作所阻塞从而出现ANR现象。
# Android中线程形态
本节对Android中的线程形态做了一个全面的介绍，处理传统的Thread以外，还包含AsyncTask、HanderThread以及IntentService，这三者的底层实现也是狭隘难成，但是他们具有特殊的表现形式，同时在使用上也各有优缺点。为了简化子线程中访问UI的过程，系统提供了AsyncTask，AsyncTask经过几次修改，导致了对于不同的APi版本AsyncTask具有不同的表现形式，尤其是多任务并发执行上面。这里讲介绍AsyncTask的使用注意事项

## AsyncTask
AsyncTask是一种轻量级的异步任务类，他可以在线程池中执行后台任务，然后把执行的进度和最终结果传递给主线程并在UI线程中更新UI。从实现上来说AsyncTask封装了Handler和Thread，通过AsyncTask可以更加方便的执行后台任务以及主线程中访问UI ，但是AsyncTask并不适合进行特别耗时的后台任务，对于特别耗时的任务来说，建议使用线程池。
AsyncTask是一个抽象的泛型类
```
public abstract class AsyncTask<Params, Progress, Result> {...}
```
来解释一下这三个泛型的意思
* Params：是参数类型
* Progress：是任务进度类型
* Result：是返回结果类型
如果不需要传递具体的参数类型，可以用Void类型来替代
看一下这一个例子

```
public class TestAsy extends AsyncTask<URL, Integer, Long> {
    @Override
    protected Long doInBackground(URL... urls) {
        long size;
        for (int i = 0; i < urls.length; i++) {
            size+=Download.downloadFile(url);
            publishProgress(i+1/urls.length);
            if (isCancelled()) {
                return -1l;
            }
        }
        return size;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Long aLong) {
        Toast.makeText(mContext, ""+aLong, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        if (isCancelled()) {
            return;
        }
    setProgressPrecent(values[0]);
    }

    @Override
    protected void onCancelled(Long aLong) {
        super.onCancelled(aLong);
    }
}


```
AsyncTas提供了五个核心的方法，他们的含义如下所示

|方法名|含义|
|:--:|:--:|
|onPreExecute|在主线程中执行，在异步任务执行之前，此方法会被调用，一般可以用于一些准备工作|
|doInBackground|在线程池中执行，此方法用于执行异步任务，params参数表示异步任务输入的参数。此方法中可以通过publishProgress来更新任务进度，publishProgress会调用onProgressUpdate方法,此外此方法需要返回计算结果给onPostExecute|
|onProgressUpdate|在主线程中执行，当后台任务的执行进度发生改变是调用次方法|
|onPostExecute|在主线程中执行，在异步任务执行后，此方法会被调用，其中result参数是后台任务的返回值，即doInbackGround的返回类型|
|onCancelled|在取消任务是，调用cancel()后，在doInBackground（）return后 我们将会调用onCancelled(Object) 不在调用onPostExecute(Object)|


然后可以使用如下方式来启动任务

```
new TestAsy().execute(url1,url2)
```

在`doInBackground`中要判断具体任务是否被取消，当下载完成后，`doInBackground`会返回结果，下载的大小。
这里有一些注意点
1. AsyncTask的类必须在主线程中加载，这就意味着第一次访问AsyncTask必须发生在主线程，当然这个过程在Android4.1及以上版本已经被系统自动完成。在Android5.0的源码中，可以查看ActivityThrad的main方法，他会调用AsyncTask的init方法，这就满足了AsyncTask类必须在主线程中加载的这个条件。
2. AsyncTask对象必须在主线程中创建
3. execute方法必须在UI线程调用
4. 不要在程序中直接调用onPreExecute、doInBackground、onProgressUpdate、onPostExecute
5. 一个AsyncTask对象只能执行一次，即只能调用一次execute方法，否则会报运行时异常
6. 在Android1.6以前，AsyncTask是串行执行任务的，在Android1.6的时候AsyncTask开始采用线程池并行处理任务，但是从Android3.0开始，为了避免所带来的并发错误，AsyncTask又采用了一个线程串行执行任务。尽管如此，在Android3.0以后的版本中，我们可以使用AsyncTask的executeOnExecutor方法来并行执行任务
