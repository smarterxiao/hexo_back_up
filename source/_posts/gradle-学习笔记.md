---
title: gradle_学习笔记
date: 2018-11-22 22:15:47
tags:
- gradle
---
# 为什么要学习gradle
最近在学习android组件化的过程中发现自己对android的打包编译工具gradle知其然不知其所以然，所以就有了这一篇学习笔记

# gradle的学习的前提条件

## groovy
这里我下载的是groovy3.0的版本 http://www.groovy-lang.org/download.html#distro 可以在这里找到下载地址，我用的是windows，所以这里直接下载exe文件就可以，在安装的时候会自动配置环境变量。
想要学号gradle，groovy是前提，gradle使用的是groovy的语法。

## gradle的安装和初始化
这个可以参考官方文档：https://gradle.org/install/ 里面有详细的安装教程，我使用的电脑是windows，所以这里说一下windows的安装过程
* 第一点：是java环境变量配置


在cmd中输入这个命令`java -version`,会显示如下结果
```
$ java -version
java version "1.8.0_121"
```
这样java环境变量就配置完毕了。为什么gradle会需要匹配java环境变量？
这里解释一下，gradle这个语言是基于java虚拟机的，和kotlin类似，只是有了一个高级的语法糖，gradle的语法用的是groovy。
如果提示命令没有找到，那么需要匹配一下环境变量，或者java开发环境没有安装，这里大家可以检查一下。
* 第二点：安装gradle
由于众所周知的原因，下载比较慢，这里我使用`Installing manually`安装的方式，下载一个gradle，http://services.gradle.org/distributions/ 这里有各种版本可以挑选，本人使用的是`gradle-4.10.2-all`这个版本。

![Alt text](gradle目录.png "gradle目录")

接着解压,并且将bin目录加入环境变量
![Alt text](gradle环境变量配置1.png "gradle环境变量配置1")
![Alt text](gradle环境变量配置2.png "gradle环境变量配置2")

环境变量配置完毕，打开cmd,输入 `gradle -v`
```
C:\Users\groot>gradle -v

------------------------------------------------------------
Gradle 4.10.2
------------------------------------------------------------

Build time:   2018-09-19 18:10:15 UTC
Revision:     b4d8d5d170bb4ba516e88d7fe5647e2323d791dd

Kotlin DSL:   1.0-rc-6
Kotlin:       1.2.61
Groovy:       2.4.15
Ant:          Apache Ant(TM) version 1.9.11 compiled on March 23 2018
JVM:          1.8.0_191 (Oracle Corporation 25.191-b12)
OS:           Windows 10 10.0 amd64

```
这样环境变量便配置成功

* 第三点：创建一个gradle工程
找到一个空文件夹，比如`D:\gradle_test`没有可以创建
然后打开cmd，切换到这个目录，输入
```
gradle init 
```
就可以得到要给gradle的工程

![Alt text](gradle工程 "gradle工程")

对比一下Android的项目：

![Alt text](Android工程 "Android工程")
有六个文件是一样的，抛开gitignore，发现Android工程比gradle工程多了两个东西
一个是app，这个是存放代码的地方
local.properties:存放本地的配置，不会被提交到git
这里可以发现，其实Android项目就是用gradle来打包的。ok

* 第四点，更新sdk
切换到`D:\gradle_test`,运行`gradlew task`命令

```
D:\gradle_test>gradlew task
Starting a Gradle Daemon, 1 incompatible and 1 stopped Daemons could not be reused, use --status for details

> Task :tasks

------------------------------------------------------------
All tasks runnable from root project
------------------------------------------------------------

Build Setup tasks
-----------------
init - Initializes a new Gradle build.
wrapper - Generates Gradle wrapper files.

Help tasks
----------
buildEnvironment - Displays all buildscript dependencies declared in root project 'gradle_test'.
components - Displays the components produced by root project 'gradle_test'. [incubating]
dependencies - Displays all dependencies declared in root project 'gradle_test'.
dependencyInsight - Displays the insight into a specific dependency in root project 'gradle_test'.
dependentComponents - Displays the dependent components of components in root project 'gradle_test'. [incubating]
help - Displays a help message.
model - Displays the configuration model of root project 'gradle_test'. [incubating]
projects - Displays the sub-projects of root project 'gradle_test'.
properties - Displays the properties of root project 'gradle_test'.
tasks - Displays the tasks runnable from root project 'gradle_test'.

To see all tasks and more detail, run gradlew tasks --all

To see more detail about a task, run gradlew help --task <task>

BUILD SUCCESSFUL in 9s
1 actionable task: 1 executed
```

如果发现更新失败，无法下载

```
D:\gradle_test>gradlew task
Downloading https://services.gradle.org/distributions/gradle-4.10.2-bin.zip

Exception in thread "main" java.net.ConnectException: Connection timed out: connect
        at java.net.DualStackPlainSocketImpl.connect0(Native Method)
        at java.net.DualStackPlainSocketImpl.socketConnect(DualStackPlainSocketImpl.java:79)
        at java.net.AbstractPlainSocketImpl.doConnect(AbstractPlainSocketImpl.java:350)
        at java.net.AbstractPlainSocketImpl.connectToAddress(AbstractPlainSocketImpl.java:206)
        at java.net.AbstractPlainSocketImpl.connect(AbstractPlainSocketImpl.java:188)
        at java.net.PlainSocketImpl.connect(PlainSocketImpl.java:172)
        at java.net.SocksSocketImpl.connect(SocksSocketImpl.java:392)
        at java.net.Socket.connect(Socket.java:589)
        at sun.security.ssl.SSLSocketImpl.connect(SSLSocketImpl.java:666)
        at sun.security.ssl.BaseSSLSocketImpl.connect(BaseSSLSocketImpl.java:173)
        at sun.net.NetworkClient.doConnect(NetworkClient.java:180)
        at sun.net.www.http.HttpClient.openServer(HttpClient.java:463)
        at sun.net.www.http.HttpClient.openServer(HttpClient.java:558)
        at sun.net.www.protocol.https.HttpsClient.<init>(HttpsClient.java:264)
        at sun.net.www.protocol.https.HttpsClient.New(HttpsClient.java:367)
        at sun.net.www.protocol.https.AbstractDelegateHttpsURLConnection.getNewHttpClient(AbstractDelegateHttpsURLConnection.java:191)
        at sun.net.www.protocol.http.HttpURLConnection.plainConnect0(HttpURLConnection.java:1156)
        at sun.net.www.protocol.http.HttpURLConnection.plainConnect(HttpURLConnection.java:1050)
        at sun.net.www.protocol.https.AbstractDelegateHttpsURLConnection.connect(AbstractDelegateHttpsURLConnection.java:177)
        at sun.net.www.protocol.http.HttpURLConnection.followRedirect0(HttpURLConnection.java:2729)
        at sun.net.www.protocol.http.HttpURLConnection.followRedirect(HttpURLConnection.java:2641)
        at sun.net.www.protocol.http.HttpURLConnection.getInputStream0(HttpURLConnection.java:1824)
        at sun.net.www.protocol.http.HttpURLConnection.getInputStream(HttpURLConnection.java:1492)
        at sun.net.www.protocol.https.HttpsURLConnectionImpl.getInputStream(HttpsURLConnectionImpl.java:263)
        at org.gradle.wrapper.Download.downloadInternal(Download.java:66)
        at org.gradle.wrapper.Download.download(Download.java:51)
        at org.gradle.wrapper.Install$1.call(Install.java:62)
        at org.gradle.wrapper.Install$1.call(Install.java:48)
        at org.gradle.wrapper.ExclusiveFileAccessManager.access(ExclusiveFileAccessManager.java:69)
        at org.gradle.wrapper.Install.createDist(Install.java:48)
        at org.gradle.wrapper.WrapperExecutor.execute(WrapperExecutor.java:107)
        at org.gradle.wrapper.GradleWrapperMain.main(GradleWrapperMain.java:61)
```

这个时候到`C:\Users\用户名\.gradle\wrapper\dists\gradle-4.10.2-bin`这个目录，手动在 http://services.gradle.org/distributions/ 下载对应的zip包放到`C:\Users\用户名\.gradle\wrapper\dists\gradle-4.10.2-bin\cghg6c4gf4vkiutgsab8yrnwv`这个文件夹下面，再次运行`gradlew task`就可以了

![Alt text](手动拷贝gradle "手动拷贝gradle")




# 第一个groovy程序，helloword
打开记事本，然后输入如下
```
class Example {
   static void main(String[] args) {
      // Using a simple println statement to print output to the console
      println('Hello World');
   }
}
```
然后保存为test.groovy，使用`D:\groovy_test>groovy test.groovy`运行程序

```
D:\groovy_test>groovy test.groovy
Hello World
```

我们第一个程序运行成功了

