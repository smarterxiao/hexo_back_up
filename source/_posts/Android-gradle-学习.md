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

强制刷新依赖

gradle --refresh-dependencies assemble

多个任务调用
gradle clean jar 先clean 在jar

通过任务名字缩写执行
gradle connectCheck ==》 gradle cc


# 第一章 groovy基础
