---
title: Android开发艺术探索 第十章 Android的消息机制
date: 2018-01-27 17:08:31
top : 210
tags:
- 进阶
- Android开发艺术探索
categories: android
---
这一章讲述的内容是Android消息机制。提及消息机制，大家应该都是很清楚的，日常开发用的还是挺多的。从开发的角度来说，Handler是Android消息机制的上层接口，这使得在开发过程中只需要和Handler交互即可。Handler的使用过程很简单，通过他可以很轻松的将任务切换到Handler所在的线程中去执行。很多人认为Handler的作用是更新UI，这的确没错，但是更新UI仅仅是Handler的一种使用场景。很多人认为Handler的作用就是更新UI，这个没错，但是太狭隘了。可以这样讲：需要在子线程中进行耗时的IO、网络等操作，都会使用Handler。因为Android在正常情况下是不允许在子线程中刷新UI的，并且不允许在主线程执行耗时操作，防止ANR的产生。
Android的消息机制主要指Handler的运行机制，Handler的运行需要底层MessageQueue和Looper的支撑。MessageQueue的中文翻译是消息队列，顾明司仪，他存储了一组消息，一队列的形式对外提供插入和删除操作，虽然叫做消息队列，但是他的内部存储结构并不是真正的队列，而是链表。Looper的中文翻译为循环，在这里可以理解为消息循环。由于MessageQueue只是一个消息的存储单元，并不能来处理消息，而Looper填补了这一个功能，Looper会以无限循环的形式去查找是否有新的消息，如果有的话就处理消息，否则就一直等待着。Looper中还有一个特殊的概念，那就是ThreadLocal，ThreadLocal并不是线程，他的作用是可以在每个线程中存储数据。Handler创建时会采用当前线程的Looper来构造消息循环系统，那么Handler内部如何得到Looper呢？就是通过ThreadLocal。之前在将ActivityThread的main函数时提到了Looper会在main里面初始化，这样我们才能正常的使用handler。
# Android消息机制概述
前面提到，Android的消息机制主要指Handler的运行机制以及Handler所附带的MessageQueue和Looper的工作过程，这三者实际上是一个整体，只不过在开发的时候我们一直在使用Handler。那么Android为什么会提供Handler呢？这个是因为只能在主线程刷新UI，否则就会抛出异常。在ViewRootImpl中有一个checkThread方法来检查线程。ViewRootImpl在讲解window那一节有描述。
```
void checkThread() {
    if (mThread != Thread.currentThread()) {
        throw new CalledFromWrongThreadException(
                "Only the original thread that created a view hierarchy can touch its views.");
    }
}

```
这个是不是都遇到过。正是由于这一点限制，必须在主线程中进行UI操作，但是又不建议在UI线程中执行耗时操作，这样会到时卡顿和ANR。因此就需要解决在子线程中无法访问UI的问题，系统为了帮助我们简化这个问题的处理，才提供了handler

这里在衍生一下，系统为什么不允许在子线程中访问UI呢？这是因为Android的UI控件不是线程安全的，如果在多线程中并发访问可能导致UI控件处于不可预期的状态，那么为什么不通过加锁机制来防止多线程操作的不可预计性呢？缺点有两个：首先加上锁机制会让UI访问的逻辑变得复杂l其次锁机制会降低UI访问的效率，因为锁机制会阻塞某些线程的执行。鉴于这两点，最简单的就是采用单线程模型来处理UI问题，对于开发者来说也不是非常的麻烦。

如果需要向主线程发消息，通过handler发送，那么需要在子线程中创建looper对象


![Alt text](2018-07-03-21-36-05.png "Handler的工作过程")


# Android消息机制分析
这里主要分析Handler、MessageQueue、Looper和ThreadLocal
## ThreadLocal的工作原理
ThreadLocal是一个线程内部的数据存储类，通过他可以在指定的线程中存储数据，数据存储以后，只有在指定的线程中可以获取到存储的数据，对于其他线程来说无法获取到数据。日常开发中使用ThreadLocal的地方很少，但是在某些特殊的场景下，通过ThreadLocal可以轻松实现一些看起来非常复杂的功能，这一点在Android的源码中也有所体现。比如Looper、ActivityThread以及AMS中都用到了ThreadLocal。具体到ThreadLocal的使用场景，这个不好统一来描述，一般来说，当某些数据是以线程作为作用域并且不同线程具有不同数据副本的时候，就可以考虑使用ThreadLocal。比如对于Handler来说，他需要获取当前线程的Looper，很显然Looper的作用域就是线程并且不同线程具有不同的Looper，这个时候通过ThreadLocal就可以轻松实现Looper在线程中的存取。如果不采用ThreadLocal，那么系统就必须提供一个全局的哈希表供Handler查找指定线程的Looper，这样一来就必须提供一个类似LooperManager的类了。但是系统并没有这么做而是选择了ThreadLocal，这就是ThreadLocal的好处。
ThreadLocal另一个使用场景是复杂逻辑下的对象传递，比如监听器的传递，有些时候一个线程中的任务过于复杂，这可能表现为函数调用栈比较深以及代码入口的多样性，在这种情况下，我们又需要监听器能够贯穿整个线程的执行过程，这个时候怎么做呢？其实这时就可以使用ThreadLocal，采用ThreadLocal可以让监听器作为线程内的全局对象而存在，在线程内部只要通过get方法爱就可以获取到监听器。如果不采用ThreadLocal，那么我们能想到的可能是如下两种方法：第一种方法是将监听器通过参数的形式在函数调用栈中进行传递，第二种方法就是讲监听器设置作为静态变量提供给线程访问。上述两种方法都有局限性。第一种方法的问题是当函数调用栈很深的时候，通过函数参数来传递监听器对象，这几乎不能接受，这会让程序的设计看起来很糟糕。第二种方法是可以接受的，但是这种状态是不具有可扩展性，比如同时又两个线程在执行，那么久需要提供两个静态的监听器对象，如果有10个呢？这显然是不可思议的，而采用ThreadLocal，每个监听器对象都在自己的线程内部存储，根本就不会有方法2的这种问题。
介绍了这么多ThreadLocal的知识，还是有点抽象，下面通过例子来演示一下ThreadLocal的真正含义。首先定义一个ThreadLocal对象，这里选择Boolean类型的，如下所示。
```
  ThreadLocal<Boolean> mBoolThreadLocal=new ThreadLocal<>();
```
然后分别在主线程，子线程1和子线程2设置他的访问值

```
mBoolThreadLocal = new ThreadLocal<>();
mBoolThreadLocal.set(true);
Log.i("main   ",""+mBoolThreadLocal.get());
new Thread("thread#1"){
    @Override
    public void run() {
        mBoolThreadLocal.set(false);
        Log.i("thread#1   ",""+mBoolThreadLocal.get());
    }
}.start();


new Thread("thread#2"){
    @Override
    public void run() {
        Log.i("thread#2   ",""+mBoolThreadLocal.get());
    }
}.start();
}
```
上面的代码中主线程设置mBoolThreadLocal的值为true，在子线程1中设置mBoolThreadLocal的值为false，在子线程2中不设置mBoolThreadLocal的值，我们按一下结果
```
I/main: true
I/thread#1: false
I/thread#2: null
```

从上面的结果看出，虽然在不同的线程中访问同一个ThreadLocal对象，但是他们通过ThreadLocal获取到的值是不一样的,这就是ThreadLocal的奇妙之处。结合这个例子然后再看一下指点对ThreadLocal两个使用场景的理论分析，我们应该就可以比较好的理解ThreadLocal的两个使用场景的理论分析，我们应该就能比较好的理解ThreadLocal的使用方法了。ThreadLocal之所以有这么神奇的效果，是因为不同线程访问同一个ThreadLocal的get方法，ThreadLocal内部会从各自的线程中取出一个数组，然后在从数组中根据当前ThreadLocal的索引去查找出对应的Value值。很显然，不同线程中的数组是不同的，这就是为什么通过ThreadLocal可以在不同的线程中维护一套数据的副本并且彼此互补干扰。
对ThreadLocal的使用方法和工作过程进行介绍之后，下面分析一下ThreadLocal内部实现，ThreadLocal是一个泛型类，他的定义为`public class ThreadLocal<T> `,只要弄清楚ThreadLocal的get和set方法就可以明白他的工作原理了。

先看一下他的set方法
```
public void set(T value) {
    Thread t = Thread.currentThread();
    ThreadLocalMap map = getMap(t);
    if (map != null)
        map.set(this, value);
    else
        createMap(t, value);
}

```
```
ThreadLocalMap getMap(Thread t) {
      return t.threadLocals;
  }

void createMap(Thread t, T firstValue) {
        t.threadLocals = new ThreadLocalMap(this, firstValue);
    }
```
在上面的set方法中，首先会通过`getMap`方法来获取当前线程中的ThreadLocal数据，如何获取呢？其实很简单，在Thread内部有一个成员专门用于存储线程的ThreadLocal的数据：ThreadLocalMap  threadLocals,因此获取当前线程的ThreadLocal数据就变得异常简单了。如果threadLocals的值为null，那么久需要对其进行初始化，初始化数据变得异常简单了。如果threadLocals的值为null，那么就需要对其进行初始化，初始化后再将ThreadLocal的值进行存储。下面看一下ThreadLocal的值是怎么在ThreadLocalMap中进行存储的。
看一下ThreadLocalMap的set方法
```
private void set(ThreadLocal<?> key, Object value) {
         Entry[] tab = table;
         int len = tab.length;
         int i = key.threadLocalHashCode & (len-1);

         for (Entry e = tab[i];
              e != null;
              e = tab[i = nextIndex(i, len)]) {
             ThreadLocal<?> k = e.get();

             if (k == key) {
                 e.value = value;
                 return;
             }

             if (k == null) {
                 replaceStaleEntry(key, value, i);
                 return;
             }
         }

         tab[i] = new Entry(key, value);
         int sz = ++size;
         if (!cleanSomeSlots(i, sz) && sz >= threshold)
             rehash();
     }
```
ThreadLocalMap内部有一个Entry[]的数组

```
static class Entry extends WeakReference<ThreadLocal<?>> {
       /** The value associated with this ThreadLocal. */
       Object value;

       Entry(ThreadLocal<?> k, Object v) {
           super(k);
           value = v;
       }
   }
```

Entry是ThreadLocalMap的内部类，是一个WeakReference，可以看到i的位置总是下一个Entry的位置，而table数组默认是16个

```
private static int nextIndex(int i, int len) {
       return ((i + 1 < len) ? i + 1 : 0);
   }
```
通过nextIndex方法找到下一个节点位置，之后
```
 tab[i] = new Entry(key, value);
```
i就是下一个节点的位置，这样就进行了赋值和存储。

下面来看一下get方法的逻辑
```
public T get() {
    Thread t = Thread.currentThread();
    ThreadLocalMap map = getMap(t);
    if (map != null) {
        ThreadLocalMap.Entry e = map.getEntry(this);
        if (e != null) {
            @SuppressWarnings("unchecked")
            T result = (T)e.value;
            return result;
        }
    }
    return setInitialValue();
}

```

这里如果ThreadLocalMap是null，就直接返回初始值
```
private Entry getEntry(ThreadLocal<?> key) {
      int i = key.threadLocalHashCode & (table.length - 1);
      Entry e = table[i];
      if (e != null && e.get() == key)
          return e;
      else
          return getEntryAfterMiss(key, i, e);
  }

```
否则通过计算hash值获取之前存储的位置，从数组中取值。这里`threadLocalHashCode`是一个常数。
这样一次set和get操作就完成了
从ThreadLocal的set和get可以看出他们所操作的对象是当前线程的ThreadLocalMap，而ThreadLocalMap的键值对是ThreadLocal和value，这样就同时存储了线程，ThreadLocal和value的信息，通过线程和ThreadLocal就可以去除对应的值。

## 消息队列的工作原理
