---
title: git简单使用
date: 2018-04-20 10:21:17
tags:
---
# git使用
> 前几天项目有一个需求，就是需要在现有的项目上面拉出一个新的项目，在新的项目上面开发，但是由于进度的原因，部分之前项目里面部分功能并备有开发完毕，但是未开发完毕的东西需要在两个项目里面用到，这个时候就去更加深入的了解了一下git的概念和使用，通过分支的方式来处理。这里学习了廖雪峰老师的git 教程 https://www.liaoxuefeng.com/wiki/0013739516305929606dd18361248578c67b8067c8c017b000/，这里做一下笔记防止以后忘记，并加深理解。

这个是针对分支进行处理，为了避免污染线上的分支，先使用demo来测试一下自己的需求能不能被满足
# 测试进行时
## 需求的整理
首先在master分支创建一个文件，并加入master分支，之后从master分支拉去两个新的分支dev和dev1，dev这个模拟当前正在开发的分支，然后dev1模拟新的项目

## 操作
### 创建仓库版本
切换到目标目录
```
git init
```
### 把文件添加到版本库
创建一个文件 test
text的原始内容为
```
test
```
之后执行
```
git add test
```
添加文件test到git文件仓库

之后将文件提交到文件仓库
```
git commit -m “初始化测试仓库”
```
返回,不同git版本的返回可能不一样
```
[master (root-commit) 8de8547] 初始化测试仓库
 1 file changed, 1 insertion(+)
 create mode 100644 test
```



### 版本回退

这个时候我们更新了文件test的内容
```
test
哈哈哈，今天天气不错
```
然后把他添加到仓库

```
git add test
git commit -m "添加今天天气"
```
不能直接使用`git commit -m "添加今天天气"``
这里有一个概念，git是控制修改不是文件，就是说如果这次修改没有添加到git仓库，他是不知道文件修改的，所以在提交变化量到仓库的时候，他会提示
```
On branch master
Changes not staged for commit:  //没有状态量变化
	modified:   test
```

```
test
哈哈哈，今天天气不错
今天路上看到的妹子好漂亮
```
```
git add test
git commit -m "美眉"
```
这个时候我忘记了前面几个版本做了什么操作
使用git log
可以看到结果,这个是按照时间倒叙排列的
```
commit 59e3cc92035ddf56c1bbc772591ec94647fc2aee
Author: xiaolei <xiaolei@qeeniao.com>
Date:   Fri Apr 20 11:11:58 2018 +0800

    美眉

commit ffba6ca2c2b7d37b12d0a37c16ff93f81ce6ac81
Author: xiaolei <xiaolei@qeeniao.com>
Date:   Fri Apr 20 11:10:10 2018 +0800

    加入今天天气

commit 8de8547136031c2f91271db3d3f997218822bd49
Author: xiaolei <xiaolei@qeeniao.com>
Date:   Fri Apr 20 11:01:35 2018 +0800

    初始化测试仓库
```

如果嫌输出信息太多，看得眼花缭乱的，可以试试加上--pretty=oneline参数：
```
git log --pretty=oneline

```


```
59e3cc92035ddf56c1bbc772591ec94647fc2aee 美眉
ffba6ca2c2b7d37b12d0a37c16ff93f81ce6ac81 加入今天天气
8de8547136031c2f91271db3d3f997218822bd49 初始化测试仓库
```

需要友情提示的是，你看到的一大串类似3628164...882e1e0的是commit id（版本号），和SVN不一样，Git的commit id不是1，2，3……递增的数字，而是一个SHA1计算出来的一个非常大的数字，用十六进制表示，而且你看到的commit id和我的肯定不一样，以你自己的为准。为什么commit id需要用这么一大串数字表示呢？因为Git是分布式的版本控制系统，后面我们还要研究多人在同一个版本库里工作，如果大家都用1，2，3……作为版本号，那肯定就冲突了。

每提交一个新版本，实际上Git就会把它们自动串成一条时间线。如果使用可视化工具查看Git历史，就可以更清楚地看到提交历史的时间线：


现在，我想要启动时光机器，回到过去
首先，Git必须知道当前版本是哪个版本，在Git中，用HEAD表示当前版本，也就是最新的提交3628164...882e1e0（注意我的提交ID和你的肯定不一样），上一个版本就是HEAD^，上上一个版本就是HEAD^^，当然往上100个版本写100个^比较容易数不过来，所以写成HEAD~100。

现在，我们要把当前版本“美眉”回退到上一个版本“加入今天天气”，就可以使用git reset命令：
```
git reset --hard HEAD^
```

可以看到内容变化了，美眉的提交被还原了

```
test
哈哈哈，今天天气不错
```
使用 `git log`看一下仓库的状态
```
commit ffba6ca2c2b7d37b12d0a37c16ff93f81ce6ac81
Author: xiaolei <xiaolei@qeeniao.com>
Date:   Fri Apr 20 11:10:10 2018 +0800

    加入今天天气

commit 8de8547136031c2f91271db3d3f997218822bd49
Author: xiaolei <xiaolei@qeeniao.com>
Date:   Fri Apr 20 11:01:35 2018 +0800

    初始化测试仓库
```

美眉没有了

但是这个时候我又想回去找那个美眉怎么办

办法其实还是有的，只要上面的命令行窗口还没有被关掉，你就可以顺着往上找啊找啊，找到那个美眉的commit id是59e3cc92035ddf56c1bbc77...，于是就可以指定回到未来的某个版本：
```
➜  测试内容，可以删除 git:(master) git reset --hard 59e3cc92
HEAD is now at 59e3cc9 美眉

```

这样就穿梭到未来了，git的变化提交时不会丢失的

现在，你回退到了某个版本，关掉了电脑，第二天早上就后悔了，想恢复到新版本怎么办？找不到新版本的commit id怎么办？

在Git中，总是有后悔药可以吃的。当你用$ git reset --hard HEAD^回退到版本时，再想恢复到美眉，就必须找到美眉的commit id。Git提供了一个命令git reflog用来记录你的每一次命令：
```
ffba6ca HEAD@{0}: reset: moving to HEAD^
59e3cc9 HEAD@{1}: reset: moving to 59e3cc92
ffba6ca HEAD@{2}: reset: moving to HEAD^
59e3cc9 HEAD@{3}: commit: 美眉
ffba6ca HEAD@{4}: commit: 加入今天天气
8de8547 HEAD@{5}: commit (initial): 初始化测试仓库
```

这样就可以找到美眉了，找到她的联系方式了

## 工作区和暂存区

* 工作区（Working Directory）
就是你在电脑里能看到的目录，比如我的learngit文件夹就是一个工作区：
* 版本库（Repository）
工作区有一个隐藏目录.git，这个不算工作区，而是Git的版本库。
Git的版本库里存了很多东西，其中最重要的就是称为stage（或者叫index）的暂存区，还有Git为我们自动创建的第一个分支master，以及指向master的一个指针叫HEAD。
