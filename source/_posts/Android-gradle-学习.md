---
title: Android_gradle_学习
date: 2017-12-25 20:53:30
tags: gradle
---
# 简介
AndroidGradle权威指南的读书笔记

# 第一章 gradle入门
## 1. 安装方法
下载 https://downloads.gradle.org/distributions/gradle-2.14-all.zip 然后解压 接着到`bin`目录 和java环境变量的配置方法一致
* gradle版本号：2.14.1
`gradle -v`查看版本信息

```
gradle -v
------------------------------------------------------------
Gradle 2.14.1
------------------------------------------------------------
Build time:   2016-07-18 06:38:37 UTC
Revision:     d9e2113d9fb05a5caabba61798bdb8dfdca83719

Groovy:       2.4.4
Ant:          Apache Ant(TM) version 1.9.6 compiled on June 29 2015
JVM:          1.8.0_152 (Oracle Corporation 25.152-b16)
OS:           Windows 10 10.0 amd64
```
## 2. gradle版本的Hello World
创建一个`build.gradle`文件
```
task hello{
  doLast{
    println 'hello World'
    }
}
```
然后切换到`build.gradle`所在的目录,执行 ` gradle -q hello`
这个脚本定义了一个Task，Task名字叫做hello，并且给hello任务添加了一个动作Action
## 3. Gradle Wrapper
### 1
是gradle的一个包装，用来统一Gradle的构建版本
生成Wrapper版本
` gradle wrapper`

wrapper配置
|参数名|说明|
|:----:|:----:|
|--gradle-version|用于指定gradle的版本|
|--gradle-distribution-url|用于下载gradle发行版的地址|
使用方法是：`gradle wrapper --gradle-version 2.4`

### 2 gradle-wrapper.properties
 gradle-wrapper.properties 配置字段
wrapper配置

|字段名|说明|
|:----:|:----:|
|distributionBase|下载gradle压缩包解压后的主目录|
|distributionPath|相对于distributionBase的解压后的Gradle压缩包的路径|
|zipStoreBase|distributionBase  用于存放Zip压缩包的|
|zipStorePath|同distributionPath  用于存放Zip压缩包的|
|distributionUrl|用于下载gradle发行版的地址|

```
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-2.14.1-bin.zip
```

### 自定义 Wrapper Task
```
task wrapper(type:wrapper){
gradleVersion='2.4'
distributionBase='GRADLE_USER_HOME'
distributionPath='wrapper/dists'
zipStoreBase='GRADLE_USER_HOME'
zipStorePath='wrapper/dists'
distributionUrl='https\://services.gradle.org/distributions/gradle-2.14.1-bin.zip'
}
```

###Gradle 日志
####
`gradle -q tasks`   //重要级别的日志
`gradle -i tasks`  //info级别的日志
`gradle -s tasks`  //输出关键性的堆栈信息
`gradle -S tasks`  //输出全部的堆栈信息

#### 日志：
`logger.quite('quiet 日志')`  
`logger.error('error 日志')`  
`logger.warn('warn 日志')`  
`logger.lifecycle('lifecycle 日志')`  
`logger.info('info 日志')`  
`logger.debug('debug 日志')`

#### 命令行：
帮助
`gradlew -h`
查看所有的`tasks`

```
:tasks
------------------------------------------------------------
All tasks runnable from root project
------------------------------------------------------------

Build Setup tasks
-----------------
init - Initializes a new Gradle build. [incubating]
wrapper - Generates Gradle wrapper files. [incubating]

Help tasks
----------
buildEnvironment - Displays all buildscript dependencies declared in root project ''.
components - Displays the components produced by root project ''. [incubating]
dependencies - Displays all dependencies declared in root project ''.
dependencyInsight - Displays the insight into a specific dependency in root project ''.
help - Displays a help message.
model - Displays the configuration model of root project ''. [incubating]
projects - Displays the sub-projects of root project ''.
properties - Displays the properties of root project ''.
tasks - Displays the tasks runnable from root project ''.

To see all tasks and more detail, run gradle tasks --all

To see more detail about a task, run gradle help --task <task>

BUILD SUCCESSFUL
```
gradle 和gradlew 的区别：
gradle使用本地版本
gradlew 是gradle wrapper的结合体，用的是wrapper中的版本

* 强制刷新依赖

gradle --refresh-dependencies assemble

* 多个任务调用
gradle clean jar 先clean 在jar



* 动态任务和定义任务链

```
task startSession <<{
  chant()
}

def chant(){
println 'Gradle chant'
}

3.times{
  task "yayGradle$it"<<{ //动态定义任务
    println 'Gradle rocks'
  }
}

yayGradle0.dependsOn startSession
yayGradle2.dependsOn yayGradle1， yayGradle0
task groupTherapy(dependsOn:yayGradle2)

```
运行任务
```
gradle groupTherapy
结果：
：startSession    //任务
Gradle chant
：yayGradle0    //任务
Gradle rocks
：yayGradle1    //任务
Gradle rocks
：yayGradle2    //任务
Gradle rocks
：groupTherapy    //任务
```

 ![Alt text](图像1514385779.png "任务依赖图")

* 通过任务名字缩写执行
gradle connectCheck ==》 gradle cc

* 运行的时候排除任务:运行A任务，并排除B相关的依赖

gradle A -x b
```
gradle ：groupTherapy -x yayGradle0
结果
：yayGradle1    //任务
Gradle rocks
：yayGradle2    //任务
Gradle rocks
：groupTherapy    //任务
```




# 第二章 groovy基础
`Gradle`是基于`Groovy`的语法，`groovy`是运行在`java`虚拟机上面的，所以和java可以完全兼容
这里先使用官网的教程，熟悉`groovy`的用法
## Groovy和Java的不同
### Default imports：默认导入的包
> `Groovy`会默认导入下面的包，不管你是否用到这些东西

```
java.io.*
java.lang.*
java.math.BigDecimal
java.math.BigInteger
java.net.*
java.util.*
groovy.lang.*
groovy.util.*
```

### Multi-methods：重载方法
```
int method(String arg) {
    return 1;
}
int method(Object arg) {
    return 2;
}
Object o = "Object";
int result = method(o);
```
在`java`中结果是：2
在`Groovy`中结果是：1
`java`在编译的的时候进行重载
`Groovy`在真正运行的的的时候进行重载，所以知道确定的类型

### Array initializers：数组的定义
在`Groovy`中`{}`是闭包，所以使用这种方式`int[] array = [1,2,3]`给数组赋值，而不是`int[] array = { 1, 2, 3}`

### Package scope visibility:私有变量
```
class Person {
    @PackageScope String name
}
```
不是
```
class Person {
    String name
}
```
### ARM(Automatic Resource Management) blocks:
这个是`java7`的新特性
之前的写法
```
FileInputStream in = null;
FileOutputStream out = null;
try {
  in = new FileInputStream("xanadu.txt");
  out = new FileOutputStream("outagain.txt");
  int c;
  while ((c = in.read()) != -1)
    out.write(c);
} finally {
  if (in != null)
    in.close();
  if (out != null)
    out.close();
}
```
这个是`java7`之后的写法
```
try (
  FileInputStream in = new FileInputStream("xanadu.txt");
  FileOutputStream out = new FileOutputStream("outagain.txt")
) {
  int c;
  while((c=in.read()) != -1 )
    out.write();
}
```
>> 就是在`try{}`里面如果是流操作，可以不用关闭流，`java会自动关闭流操作`

这个是`java`代码
```
Path file = Paths.get("/path/to/file");
Charset charset = Charset.forName("UTF-8");
try (BufferedReader reader = Files.newBufferedReader(file, charset)) {
    String line;
    while ((line = reader.readLine()) != null) {
        System.out.println(line);
    }

} catch (IOException e) {
    e.printStackTrace();
}
```
这个是`groovy`代码
```
new File('/path/to/file').eachLine('UTF-8') {
   println it
}
```
不能理解可以看一下这个
```
new File('/path/to/file').withReader('UTF-8') { reader ->
   reader.eachLine {
       println it
   }
}
```

###  Inner classes：内部类的写法
####  Static inner classes ：静态内部类的写法
```
class A {
    static class B {}
}
new A.B()
```
####  Anonymous Inner Classes ：匿名内部类的写法
这个没有多大的区别
```
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

CountDownLatch called = new CountDownLatch(1)

Timer timer = new Timer()
timer.schedule(new TimerTask() {
    void run() {
        called.countDown()
    }
}, 0)

assert called.await(10, TimeUnit.SECONDS)
```

####  Creating Instances of Non-Static Inner Classes:创建非静态内部类
`java`中的写法
```
public class Y {
    public class X {}
    public X foo() {
        return new X();
    }
    public static X createX(Y y) {
        return y.new X();
    }
}
```
`groovy`中的写法
```
public class Y {
    public class X {}
    public X foo() {
        return new X()
    }
    public static X createX(Y y) {
        return new X(y)
    }
}
```
### Lambdas
`groovy`不支持`Lambdas`，使用闭包替代

### GStrings
`groovy`在String的基础上对String进行了拓展，后面会详细讲述

## Closures:闭包
之前对闭包这个一直不是很理解，然后看了一下官方的文档，清晰了很多:http://groovy-lang.org/closures.html
### Syntax:语法
####Defining a closure：定义
`{ [closureParameters -> ] statements }` []里面的内容是可选的
闭包常见的几种形式
```
{ item++ }                                        

{ -> item++ }                                       

{ println it }       //使用了隐式参数it                               

{ it -> println it } //明确声明了参数it                     

{ name -> println name }                            

{ String x, int y ->                                
    println "hey ${x} the value is ${y}"
}

{ reader ->                                         
    def line = reader.readLine()
    line.trim()
}
```
#### Closures as an object：闭包是可以作为一个对象的
闭包在`groovy`中是一个类`groovy.lang.Closure`，这样理解对后面会有很多的帮助
```
def listener = { e -> println "Clicked on $e.source" }   //def 是Object类型，Closure是Object的子类     
assert listener instanceof Closure
Closure callback = { println 'Done!' }                      
Closure<Boolean> isTextFile = {
    File it -> it.name.endsWith('.txt')                     
}
```

####  Calling a closure：调用闭包

```
def code = { 123 }  //这里有一点：groovy闭包和方法如果没有return，就默认使用最后面的那个参数作为返回值
assert code() == 123 //所以这里是相等的


def isOdd = { int i -> i%2 != 0 }                           
assert isOdd(3) == true   //上方定义一个闭包：isOdd，有一个参数，这里调用闭包                                  
assert isOdd.call(2) == false      //闭包另外一种调用方式     

def isEven = { it%2 == 0 }                                  
assert isEven(3) == false                                   
assert isEven.call(2) == true  
```

### Parameters ： 闭包的参数

#### Normal parameters：默认参数

```
def closureWithOneArg = { str -> str.toUpperCase() }   //这个有一个参数
assert closureWithOneArg('groovy') == 'GROOVY'

def closureWithOneArgAndExplicitType = { String str -> str.toUpperCase() }
assert closureWithOneArgAndExplicitType('groovy') == 'GROOVY'

def closureWithTwoArgs = { a,b -> a+b }
assert closureWithTwoArgs(1,2) == 3

def closureWithTwoArgsAndExplicitTypes = { int a, int b -> a+b }
assert closureWithTwoArgsAndExplicitTypes(1,2) == 3

def closureWithTwoArgsAndOptionalTypes = { a, int b -> a+b }  //这个有两个参数
assert closureWithTwoArgsAndOptionalTypes(1,2) == 3

def closureWithTwoArgAndDefaultValue = { int a, int b=2 -> a+b }  //这个有两个参数，一个参数有默认值，所以可以传递一个或者两个参数
assert closureWithTwoArgAndDefaultValue(1) == 3
```

#### Implicit parameter：隐式参数
#### Varargs:可变参数
