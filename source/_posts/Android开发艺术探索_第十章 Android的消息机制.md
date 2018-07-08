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
消息队列在Android中指定的是MessageQueue，MessageQueue主要包含两个操作：插入和读取。读取操作本身会伴随着删除操作，插入和读取对应的方法分别是`enqueueMessage`和`next`，其中`enqueueMessage`的作用是往消息队列中插入一条消息，而`next`的作用是从消息队列中取出一条消息并将其从消息队列中移除。尽管MessageQueue叫消息队列，但是他的内部实现并不是用的队列，实际上他通过一个单链表的数据结构来维护消息列表，单链表在插入和删除上比较有优势。下面主要看一下他的`enqueueMessage`和`next`方法的实现，`enqueueMessage`的源码如下所示
```
boolean enqueueMessage(Message msg, long when) {
    if (msg.target == null) {
        throw new IllegalArgumentException("Message must have a target.");
    }
    if (msg.isInUse()) {
        throw new IllegalStateException(msg + " This message is already in use.");
    }

    synchronized (this) {
        if (mQuitting) {
            IllegalStateException e = new IllegalStateException(
                    msg.target + " sending message to a Handler on a dead thread");
            Log.w(TAG, e.getMessage(), e);
            msg.recycle();
            return false;
        }

        msg.markInUse();
        msg.when = when;
        Message p = mMessages;
        boolean needWake;
        if (p == null || when == 0 || when < p.when) {
            // New head, wake up the event queue if blocked.
            msg.next = p;
            mMessages = msg;
            needWake = mBlocked;
        } else {
            needWake = mBlocked && p.target == null && msg.isAsynchronous();
            Message prev;
            for (;;) {
                prev = p;
                p = p.next;
                if (p == null || when < p.when) {
                    break;
                }
                if (needWake && p.isAsynchronous()) {
                    needWake = false;
                }
            }
            msg.next = p; // invariant: p == prev.next
            prev.next = msg;
        }

        // We can assume mPtr != 0 because mQuitting is false.
        if (needWake) {
            nativeWake(mPtr);
        }
    }
    return true;
}
```

从`enqueueMessage`的实现来看，他的主要操作其实就是单链表的插入操作，可以看一下Message这个类，mMessages是当前消息的一个引用，当添加新的msg时，会将msg作为最后一个节点。，其他的这里就不再过多解释了，上图。


![Alt text](2018-07-08-14-13-16.png "enqueueMessage的过程")

下面看一下`next`方法的实现，`next`的主要逻辑如下所示
```
    Message next() {
        final long ptr = mPtr;
        if (ptr == 0) {
            return null;
        }
        int pendingIdleHandlerCount = -1; // -1 only during first iteration
        int nextPollTimeoutMillis = 0;
        for (;;) {
            if (nextPollTimeoutMillis != 0) {
                Binder.flushPendingCommands();
            }
            nativePollOnce(ptr, nextPollTimeoutMillis);
            synchronized (this) {
                final long now = SystemClock.uptimeMillis();
                Message prevMsg = null;
                Message msg = mMessages;
                if (msg != null && msg.target == null) {
                    do {
                        prevMsg = msg;
                        msg = msg.next;
                    } while (msg != null && !msg.isAsynchronous());
                }
                if (msg != null) {
                    if (now < msg.when) {
                        nextPollTimeoutMillis = (int) Math.min(msg.when - now, Integer.MAX_VALUE);
                    } else {
                        // Got a message.
                        mBlocked = false;
                        if (prevMsg != null) {
                            prevMsg.next = msg.next;
                        } else {
                            mMessages = msg.next;
                        }
                        msg.next = null;
                        if (DEBUG) Log.v(TAG, "Returning message: " + msg);
                        msg.markInUse();
                        return msg;
                    }
                } else {
                    nextPollTimeoutMillis = -1;
                }
                if (mQuitting) {
                    dispose();
                    return null;
                }
                if (pendingIdleHandlerCount < 0
                        && (mMessages == null || now < mMessages.when)) {
                    pendingIdleHandlerCount = mIdleHandlers.size();
                }
                if (pendingIdleHandlerCount <= 0) {
                    // No idle handlers to run.  Loop and wait some more.
                    mBlocked = true;
                    continue;
                }

                if (mPendingIdleHandlers == null) {
                    mPendingIdleHandlers = new IdleHandler[Math.max(pendingIdleHandlerCount, 4)];
                }
                mPendingIdleHandlers = mIdleHandlers.toArray(mPendingIdleHandlers);
            }
            for (int i = 0; i < pendingIdleHandlerCount; i++) {
                final IdleHandler idler = mPendingIdleHandlers[i];
                mPendingIdleHandlers[i] = null; // release the reference to the handler

                boolean keep = false;
                try {
                    keep = idler.queueIdle();
                } catch (Throwable t) {
                    Log.wtf(TAG, "IdleHandler threw exception", t);
                }

                if (!keep) {
                    synchronized (this) {
                        mIdleHandlers.remove(idler);
                    }
                }
            }
            pendingIdleHandlerCount = 0;
            nextPollTimeoutMillis = 0;
        }
    }
```
可以发现这个是一个无限循环的方法，如果消息队列中没有消息，那么next方法就会一直阻塞在这里，当有新的消息时，next就会返回这条消息并将这条消息移除
![Alt text](2018-07-08-14-46-47.png "enqueueMessage的过程")

## Looper的工作原理
Looper在Android的消息机制中扮演者消息循环的角色，具体来说他是会不停的从MessageQueue中查看是否有新的消息，如果有新的消息就会立刻处理，否则就一直阻塞在哪里。首先看一下他的构造方法，在构造方法中他会创建一个MessageQueue即消息队列，然后将当前线程对象保存起来，如下所示。

```
private Looper(boolean quitAllowed) {
    mQueue = new MessageQueue(quitAllowed);
    mThread = Thread.currentThread();
}
```
我们知道，Handler的工作需要Looper，没有Looper的线程就会报错，那么如何为一个线程创建Looper呢？其实很简单，通过`Looper.prepare()`即可为当前线程创建一个Looper，接着通过`Looper.loop()`来开启消息循环，如下所示
Looper开启前的准备工作：
```
private static void prepare(boolean quitAllowed) {
     if (sThreadLocal.get() != null) {
         throw new RuntimeException("Only one Looper may be created per thread");
     }
     sThreadLocal.set(new Looper(quitAllowed));
 }
```
除了这个方法，系统还提供`prepareMainLooper`方法
```
public static void prepareMainLooper() {
    prepare(false);
    synchronized (Looper.class) {
        if (sMainLooper != null) {
            throw new IllegalStateException("The main Looper has already been prepared.");
        }
        sMainLooper = myLooper();
    }
}

```
这个方法主要是给主线程就是ActivityThread创建Looper使用的，其本质也是通过`prepare()`方法来实现的，由于主线程中的Looper比较特殊，所以系统还提供了`getMainLooper`方法，通过他可以在任何地方获取到主线程的Looper。Looper也是可以退出的，Looper提供了`quit`和`quitSafely`来退出一个Looper，二者的区别是：`quit`会直接退出Looper。而`quitSafely`只是设定一个退出标记，然后把消息队列中的已有消息处理完毕才安全退出。Looper退出后，通过Handler发送的消息会失败，这个时候Handler的`send`方法会返回false。在子线程中，如果手动为其创建了一个Looper，那么在所有的事情完成以后应该调用`quit`方法来终止消息循环，否则这个子线程创建的Looper会一直处于等待状态，而如果Looper退出以后，这个线程就会立刻终止，因此建议不需要使用的时候终止Looper。
现在来看一下`loop`方法
Looper开始工作：
```
public static void loop() {
       final Looper me = myLooper();
       if (me == null) {
           throw new RuntimeException("No Looper; Looper.prepare() wasn't called on this thread.");
       }
       final MessageQueue queue = me.mQueue;

       // Make sure the identity of this thread is that of the local process,
       // and keep track of what that identity token actually is.
       Binder.clearCallingIdentity();
       final long ident = Binder.clearCallingIdentity();

       for (;;) {
           Message msg = queue.next(); // might block
           if (msg == null) {
               // No message indicates that the message queue is quitting.
               return;
           }

           // This must be in a local variable, in case a UI event sets the logger
           final Printer logging = me.mLogging;
           if (logging != null) {
               logging.println(">>>>> Dispatching to " + msg.target + " " +
                       msg.callback + ": " + msg.what);
           }

           final long slowDispatchThresholdMs = me.mSlowDispatchThresholdMs;

           final long traceTag = me.mTraceTag;
           if (traceTag != 0 && Trace.isTagEnabled(traceTag)) {
               Trace.traceBegin(traceTag, msg.target.getTraceName(msg));
           }
           final long start = (slowDispatchThresholdMs == 0) ? 0 : SystemClock.uptimeMillis();
           final long end;
           try {
               msg.target.dispatchMessage(msg);
               end = (slowDispatchThresholdMs == 0) ? 0 : SystemClock.uptimeMillis();
           } finally {
               if (traceTag != 0) {
                   Trace.traceEnd(traceTag);
               }
           }
           if (slowDispatchThresholdMs > 0) {
               final long time = end - start;
               if (time > slowDispatchThresholdMs) {
                   Slog.w(TAG, "Dispatch took " + time + "ms on "
                           + Thread.currentThread().getName() + ", h=" +
                           msg.target + " cb=" + msg.callback + " msg=" + msg.what);
               }
           }

           if (logging != null) {
               logging.println("<<<<< Finished to " + msg.target + " " + msg.callback);
           }

           // Make sure that during the course of dispatching the
           // identity of the thread wasn't corrupted.
           final long newIdent = Binder.clearCallingIdentity();
           if (ident != newIdent) {
               Log.wtf(TAG, "Thread identity changed from 0x"
                       + Long.toHexString(ident) + " to 0x"
                       + Long.toHexString(newIdent) + " while dispatching to "
                       + msg.target.getClass().getName() + " "
                       + msg.callback + " what=" + msg.what);
           }

           msg.recycleUnchecked();
       }
   }

```
可以看到`loop()`方法内部有一个无限循环，不断调用MessageQueue的`next`方法，唯一跳出循环的方式是MessageQueue的`next`方法返回了null。当Looper的`quit`方法被调用时，Looper就会调用MessageQueue的`quit`或者`quitSafely`方法来通知消息队列退出，当消息队列被标记位退出状态时，他的`next`方法就会返回null。也就是说Looper必须退出，否则loop方法会无限循环下去。`loop`方法会调用MessageQueue的next方法获取新消息，而`next`是一个阻塞操作，当没有消息时，`next`会一直阻塞在哪里，这导致`loop`一直阻塞在哪里。如果MessageQueue的`next`方法返回了新的消息，Looper就会处理这条消息：`msg.target.dispatchMessage(msg)`这里的msg.target是发送消息的Handler对象，这样Handler发送的消息最终交给他的`dispatchMessage`方法来处理了。但是这里不同的是，Handler的`dispatchMessage`方法是在创建Handler时所使用的Looper中执行的，这样就成功的将代码逻辑切换到指定的线程中去执行了。
handler的`dispatchMessage`方法
```
public void dispatchMessage(Message msg) {
      if (msg.callback != null) {
          handleCallback(msg);
      } else {
          if (mCallback != null) {
              if (mCallback.handleMessage(msg)) {
                  return;
              }
          }
          handleMessage(msg);
      }
  }

```
而`handleMessage`是创建handler我们需要实现的一个方法...

```
Handler handler=new Handler(){
           @Override
           public void handleMessage(Message msg) {
             //TODO
           }
       };
```

## Handler的工作原理
Handler的工作主要包含消息的发送和接收过程，消息的发送可以通过`post`的一系列方法最终通过`send`的一系列方法来实现。发送一条消息典型的过程如下
首先调用`sendMessage`方法
```
public final boolean sendMessage(Message msg)
  {
      return sendMessageDelayed(msg, 0);
  }
```
`sendMessage`方法其实在handler内部调用`sendMessageDelayed`方法
```
public final boolean sendMessageDelayed(Message msg, long delayMillis)
{
    if (delayMillis < 0) {
        delayMillis = 0;
    }
    return sendMessageAtTime(msg, SystemClock.uptimeMillis() + delayMillis);
}
```
`sendMessageDelayed`方法又调用了`sendMessageAtTime`方法
```
public boolean sendMessageAtTime(Message msg, long uptimeMillis) {
     MessageQueue queue = mQueue;
     if (queue == null) {
         RuntimeException e = new RuntimeException(
                 this + " sendMessageAtTime() called with no mQueue");
         Log.w("Looper", e.getMessage(), e);
         return false;
     }
     return enqueueMessage(queue, msg, uptimeMillis);
 }

```
`sendMessageAtTime`方法调用了`enqueueMessage`方法，`enqueueMessage`方法之前分析过，就是讲message加入消息队列

```
private boolean enqueueMessage(MessageQueue queue, Message msg, long uptimeMillis) {
     msg.target = this;
     if (mAsynchronous) {
         msg.setAsynchronous(true);
     }
     return queue.enqueueMessage(msg, uptimeMillis);
 }
```

综合之前MessageQueue的`enqueueMessage`方法的分析,可以发现在`send`的时候其实并没有做什么复杂的操作，仅仅是在MessageQueue插入了一个消息。由于Looper一直的`loop`方法一直在调用MessageQueue的`next`方法，这个时候就会将这条msg返回给Looper，Looper收到消息后开始处理了，最终消息由Looper交给Handler处理，即Handler的`dispatchMessage`方法会被调用，这时Handler消息就进入了消息处理阶段。`dispatchMessage`的实现已经介绍过来，这里在来一次。
```
public void dispatchMessage(Message msg) {
      if (msg.callback != null) {
          handleCallback(msg);
      } else {
          if (mCallback != null) {
              if (mCallback.handleMessage(msg)) {
                  return;
              }
          }
          handleMessage(msg);
      }
  }

```

首先检查Message的callback是否为null,不为null就通过`handlerCallback`来处理消息。Message的callback是一个Runnable对象，实际上就是Handler的`post`方法所传递的Runnable参数。`handlerCallback`的逻辑也很简单，如下所示
```
private static void handleCallback(Message message) {
     message.callback.run();
 }
```
如何为callback赋值
```
handler.post(new Runnable() {
         @Override
         public void run() {

         }
     });
handler.sendMessage(Message.obtain(handler, new Runnable() {
         @Override
         public void run() {

         }
     }));
```
通过callback，可以实现在其他线程回调，而不仅仅是在主线程回调
其次，检查mCallback是否为null，不为null就调用mCallback的`handleMessage`方法来处理消息。Callback是个接口，他的定义如下：

```
public interface Callback {
    public boolean handleMessage(Message msg);
}
```
通过callbacl可以采用如下方式来创建Handler对象：`Handler handler=new Handler(callback)`。那么Callback的意义是什么呢？源码里面的注释已经做了说明：可以用来创建一个Handler的实例单并不需要派生Handler的子类。在日常开发中，创建Handler最常见的方式就是派生一个子类并重写其`handlerMessage`方法来处理消息，而Callback给我们提供了另外一种使用Handler的方式，当我们不想派生子类时，就可以通过Callback来实现。
```
Handler handler=new Handler(new Handler.Callback() {
      @Override
      public boolean handleMessage(Message msg) {
          return false;
      }
  });
```
最后才是调用Handler的handleMessage方法来处理消息。Handler处理消息的过程可以归纳为一个流程图，如图所示

![Alt text](Handler流程图.svg "Handler流程图")

handler还有一个特殊的构造方法，那就是通过一个特定的Looper来构造Handler，他的实现如下所示。通过这个构造方法可以实现一些特殊的功能？这个功能要问一下作者，书里面没有涉及！！！
```
public Handler(Looper looper) {
    this(looper, null, false);
}
```
下面看一下Handler的一个默认构造方法`public Handler()`，这个构造方法会调用下面的构造方法。很明显，如果当前线程中没有Looper的话，就会抛出`   "Can't create handler inside thread that has not called Looper.prepare()"`异常，这也解释了在没有Looper的子线程中创建handler会引发程序异常的原因
```
public Handler(Callback callback, boolean async) {
       if (FIND_POTENTIAL_LEAKS) {
           final Class<? extends Handler> klass = getClass();
           if ((klass.isAnonymousClass() || klass.isMemberClass() || klass.isLocalClass()) &&
                   (klass.getModifiers() & Modifier.STATIC) == 0) {
               Log.w(TAG, "The following Handler class should be static or leaks might occur: " +
                   klass.getCanonicalName());
           }
       }

       mLooper = Looper.myLooper();
       if (mLooper == null) {
           throw new RuntimeException(
               "Can't create handler inside thread that has not called Looper.prepare()");
       }
       mQueue = mLooper.mQueue;
       mCallback = callback;
       mAsynchronous = async;
   }
```

# 主线程消息循环
Android的主线程就是ActivityThread，主线程的入口方法是`main`,在main方法中系统会通过`looper.preparemainLooper`来创建主线程的Looper和MessageQueue，并通过`Looper.loop`来开启主线程的消息循环，这个过程如下所示
```
    public static void main(String[] args) {

        ...
        Looper.prepareMainLooper();

        ActivityThread thread = new ActivityThread();
        thread.attach(false);

        if (sMainThreadHandler == null) {
            sMainThreadHandler = thread.getHandler();
        }

        if (false) {
            Looper.myLooper().setMessageLogging(new
                    LogPrinter(Log.DEBUG, "ActivityThread"));
        }

        // End of event ActivityThreadMain.
        Trace.traceEnd(Trace.TRACE_TAG_ACTIVITY_MANAGER);
        Looper.loop();

        throw new RuntimeException("Main thread loop unexpectedly exited");
    }
```
主线程的消息循环开始以后，ActivityThread还需要一个Handler来和消息队列进行交互，这个Handler就是ActivityThread.H,之前已经和他打过很多次交道了，他内部定义了一系列的消息组件，主要包含四大组件的启动和停止过程，如下所示
```
private class H extends Handler {
    public static final int LAUNCH_ACTIVITY         = 100;
    public static final int PAUSE_ACTIVITY          = 101;
    public static final int PAUSE_ACTIVITY_FINISHING= 102;
    public static final int STOP_ACTIVITY_SHOW      = 103;
    public static final int STOP_ACTIVITY_HIDE      = 104;
    public static final int SHOW_WINDOW             = 105;
    public static final int HIDE_WINDOW             = 106;
    public static final int RESUME_ACTIVITY         = 107;
    public static final int SEND_RESULT             = 108;
    public static final int DESTROY_ACTIVITY        = 109;
    public static final int BIND_APPLICATION        = 110;
    public static final int EXIT_APPLICATION        = 111;
    public static final int NEW_INTENT              = 112;
    public static final int RECEIVER                = 113;
    public static final int CREATE_SERVICE          = 114;
    public static final int SERVICE_ARGS            = 115;
    public static final int STOP_SERVICE            = 116;

    public static final int CONFIGURATION_CHANGED   = 118;
    public static final int CLEAN_UP_CONTEXT        = 119;
    public static final int GC_WHEN_IDLE            = 120;
    public static final int BIND_SERVICE            = 121;
    public static final int UNBIND_SERVICE          = 122;
    public static final int DUMP_SERVICE            = 123;
    public static final int LOW_MEMORY              = 124;
    public static final int ACTIVITY_CONFIGURATION_CHANGED = 125;
    public static final int RELAUNCH_ACTIVITY       = 126;
    public static final int PROFILER_CONTROL        = 127;
    public static final int CREATE_BACKUP_AGENT     = 128;
    public static final int DESTROY_BACKUP_AGENT    = 129;
    public static final int SUICIDE                 = 130;
    public static final int REMOVE_PROVIDER         = 131;
    public static final int ENABLE_JIT              = 132;
    public static final int DISPATCH_PACKAGE_BROADCAST = 133;
    public static final int SCHEDULE_CRASH          = 134;
    public static final int DUMP_HEAP               = 135;
    public static final int DUMP_ACTIVITY           = 136;
    public static final int SLEEPING                = 137;
    public static final int SET_CORE_SETTINGS       = 138;
    public static final int UPDATE_PACKAGE_COMPATIBILITY_INFO = 139;
    public static final int TRIM_MEMORY             = 140;
    public static final int DUMP_PROVIDER           = 141;
    public static final int UNSTABLE_PROVIDER_DIED  = 142;
    public static final int REQUEST_ASSIST_CONTEXT_EXTRAS = 143;
    public static final int TRANSLUCENT_CONVERSION_COMPLETE = 144;
    public static final int INSTALL_PROVIDER        = 145;
    public static final int ON_NEW_ACTIVITY_OPTIONS = 146;
    public static final int CANCEL_VISIBLE_BEHIND = 147;
    public static final int BACKGROUND_VISIBLE_BEHIND_CHANGED = 148;
    public static final int ENTER_ANIMATION_COMPLETE = 149;
    public static final int START_BINDER_TRACKING = 150;
    public static final int STOP_BINDER_TRACKING_AND_DUMP = 151;
    public static final int MULTI_WINDOW_MODE_CHANGED = 152;
    public static final int PICTURE_IN_PICTURE_MODE_CHANGED = 153;
    public static final int LOCAL_VOICE_INTERACTION_STARTED = 154;
    public static final int ATTACH_AGENT = 155;
    public static final int APPLICATION_INFO_CHANGED = 156;
    public static final int ACTIVITY_MOVED_TO_DISPLAY = 157;
    ...
  }
```

ActivityThread通过ApplicaitonThread和AMS进行进程间通信，AMS以进程间通信的方式完成ActivityThread的请求后悔回调ApplicaitonThread中的Binder，然后ApplicationThread会向H发送信息，H收到信息后会将ApplicationThread中的逻辑切换到ActivityThread中去执行，即切换到主线程中执行，这个就是主线程的消息循环模型。这个是系统Handler H的处理方式。
