---
title: 第五章 理解RemoteViews
date: 2018-01-27 17:08:26
top : 205
tags:
- 进阶
- Android开发艺术探索
categories: android
---
第五章 理解RemoteViews
本章讲述的主题是remoteView。可以从名字可以看出是一种远程View。那么什么是远程View呢?主要有两种一个是通知栏，一个是桌面小插件

# RemoteViews的应用
RemoteView在实际开发过程中主要用于通知栏和桌面小组件的开发。通知栏大家都不陌生，主要是通过使用NotificationManager来实现。除了默认布局，还可以自定义布局。桌面小部件则通过AppWidgetProvide来实现。AppWidgetProvide本质上是一个广播。在开发RemoteView的过程中会用到RemoteViews。他们更新界面不能像在Activity中那样更新，因为二者的界面都运行在其他进程中，准确的说是SystemServer中，为了更新UI，这里提供了一系列更新的方法。所以这里会简单介绍一下用法，重点是讲解一下RemoteView的内在机制

## RemoteView在通知栏的应用

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

## RemoteViews在桌面小部件的应用
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
## PendingIntent的概述
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

# RemoteViews的内部机制
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

# RemoteViews的意义
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
