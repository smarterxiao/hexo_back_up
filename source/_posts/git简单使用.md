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


之前说的如果一个文件修改了，那他必须要先add这次修改，把修改添加到版本库，这个时候才能提交修改到版本库
这里对test内容在做一次修改

```
test
哈哈哈，今天天气不错
今天路上看到的妹子好漂亮
我要到那个妹子的微信了 O(∩_∩)O哈哈~
```
先用`git status`查看一下状态：

```
On branch master
Changes not staged for commit:
  (use "git add <file>..." to update what will be committed)
  (use "git checkout -- <file>..." to discard changes in working directory)

	modified:   test   // test 没有修改

no changes added to commit (use "git add" and/or "git commit -a")
```

然后`git add test`,我们在看一下`git status`状态


```
On branch master
Changes to be committed:
  (use "git reset HEAD <file>..." to unstage)

	modified:   test  //添加之后 test修改了

```
然后commit 一下，在 `git status`
```
➜  测试内容，可以删除 git:(master) ✗ git commit -m "妹子家好友了"
[master ab99189] 妹子家好友了
 1 file changed, 2 insertions(+), 1 deletion(-)
➜  测试内容，可以删除 git:(master) git status
On branch master
nothing to commit, working tree clean
➜  测试内容，可以删除 git:(master)
```

可以看到没有修改的内容要添加和提交


## 管理修改

这里要说一个问题，什么是修改？比如你新增了一行，这就是一个修改，删除了一行，也是一个修改，更改了某些字符，也是一个修改，删了一些又加了一些，也是一个修改，甚至创建一个新文件，也算一个修改。


这里在做一个实验加深对修改，工作区和暂存区的理解
原始状态：

```
test
哈哈哈，今天天气不错
今天路上看到的妹子好漂亮
我要到那个妹子的微信了 O(∩_∩)O哈哈~
```
这个时候要和妹子进行下一步联系

```
test
哈哈哈，今天天气不错
今天路上看到的妹子好漂亮
我要到那个妹子的微信了 O(∩_∩)O哈哈~
你好，我是xxxxx
```

这个时候将这个工作区的修改添加到缓冲区

`git add test`

这个时候你来不及提交这次修改到缓冲区，妹子就给你发了一个信息,你很激动

```
test
哈哈哈，今天天气不错
今天路上看到的妹子好漂亮
我要到那个妹子的微信了 O(∩_∩)O哈哈~
你好，我是xxxxx
我是yyyy
```

这个时候你才`commit`这次修改
这个时候看一下`git status`
发现
```
On branch master
Changes not staged for commit:
  (use "git add <file>..." to update what will be committed)
  (use "git checkout -- <file>..." to discard changes in working directory)

	modified:   test

no changes added to commit (use "git add" and/or "git commit -a")
```

第二次修改并没有被提交


第一次修改 -> git add -> 第二次修改 -> git commit

你看，我们前面讲了，Git管理的是修改，当你用git add命令后，在工作区的第一次修改被放入暂存区，准备提交，但是，在工作区的第二次修改并没有放入暂存区，所以，git commit只负责把暂存区的修改提交了，也就是第一次的修改被提交了，第二次的修改不会被提交。

提交后，用`git diff HEAD -- test`命令可以查看工作区和版本库里面最新版本的区别

```
git diff HEAD -- test diff --git a/test b/test
```
```
diff --git a/test b/test
index 6bd41ba..179a3bc 100644
--- a/test
+++ b/test
@@ -2,4 +2,6 @@ test
 哈哈哈，今天天气不错
 今天路上看到的妹子好漂亮
 我要到那个妹子的微信了 O(∩_∩)O哈哈~
-你好，我是xxxxx
\ No newline at end of file
+你好，我是xxxxx
+
+我是yyyy
\ No newline at end of file
(END)

```


怎么提交第二次修改呢？你可以继续git add再git commit，也可以别着急提交第一次修改，先git add第二次修改，再git commit，就相当于把两次修改合并后一块提交了：

第一次修改 -> git add -> 第二次修改 -> git add -> git commit


## 撤销修改
这个是在犯错的情况下进行补救的办法，人非圣贤，孰能无过
这个时候在修改的文件中加入了，what fack 这个本该发给基友的话
```
test
哈哈哈，今天天气不错
今天路上看到的妹子好漂亮
我要到那个妹子的微信了 O(∩_∩)O哈哈~
你好，我是xxxxx

我是yyyy

what fack
```
使用`git status`查看状态
```
On branch master
Changes not staged for commit:
  (use "git add <file>..." to update what will be committed)
  (use "git checkout -- <file>..." to discard changes in working directory)

	modified:   test

no changes added to commit (use "git add" and/or "git commit -a")
```
发现可以通过 `git checkout -- test`撤销这次修改
这样就撤销修改了

这里有两种情况：
一种是test自修改后还没有被放到暂存区，现在，撤销修改就回到和版本库一模一样的状态；
一种是test已经添加到暂存区后，又作了修改，现在，撤销修改就回到添加到暂存区后的状态。
总之，就是让这个文件回到最近一次git commit或git add时的状态。
现在，看看test的文件内容：
已经恢复了
```
test
哈哈哈，今天天气不错
今天路上看到的妹子好漂亮
我要到那个妹子的微信了 O(∩_∩)O哈哈~
你好，我是xxxxx
```


但是如果已经add了这个时候要怎么操作呢
Git同样告诉我们，用命令git reset HEAD file可以把暂存区的修改撤销掉（unstage），重新放回工作区：
```
git reset HEAD test
```
就是将add到暂存区的内容reset

```
git status
On branch master
Changes not staged for commit:
  (use "git add <file>..." to update what will be committed)
  (use "git checkout -- <file>..." to discard changes in working directory)

	modified:   test

no changes added to commit (use "git add" and/or "git commit -a")
```

可以看到暂存区的add被撤销了
这个时候如果要放弃对工作区的修改，可以使用
```
git checkout head test

```

这里做一下小结：
场景1 ：修改了工作区没有add，可以使用 git checkout head 文件名
场景2 ： 修改了工作区并且add到暂存区，可以使用 git reset HEAD 文件名，
场景3 ： 如果修改了工作区并commit到暂存区，可以通过git reset --hard HEAD xxx 来解决


## 删除文件

新建一个文件xxxx，然后添加到git暂存区

这个时候删除这个文件
```
git status
HEAD detached from ab99189
Changes not staged for commit:
  (use "git add/rm <file>..." to update what will be committed)
  (use "git checkout -- <file>..." to discard changes in working directory)

	deleted:    xxxx

no changes added to commit (use "git add" and/or "git commit -a")
```
这个时候要在暂存区要删除这个文件
```
➜  测试内容，可以删除 git:(d0410fb) ✗ git rm xxxx
rm 'xxxx'
➜  测试内容，可以删除 git:(d0410fb) ✗ git status
HEAD detached from ab99189
Changes to be committed:
  (use "git reset HEAD <file>..." to unstage)

	deleted:    xxxx

```

如果不想删除这个文件 ，就`git checkout -- xxxx`

## 添加远程仓库

目前，在GitHub上的这个learngit仓库还是空的，GitHub告诉我们，可以从这个仓库克隆出新的仓库，也可以把一个已有的本地仓库与之关联，然后，把本地仓库的内容推送到GitHub仓库。

现在，我们根据GitHub的提示，在本地的learngit仓库下运行命令：

$ git remote add origin git地址



添加远程库

阅读: 1246934
现在的情景是，你已经在本地创建了一个Git仓库后，又想在GitHub创建一个Git仓库，并且让这两个仓库进行远程同步，这样，GitHub上的仓库既可以作为备份，又可以让其他人通过该仓库来协作，真是一举多得。

首先，登陆GitHub，然后，在右上角找到“Create a new repo”按钮，创建一个新的仓库：

github-create-repo-1

在Repository name填入learngit，其他保持默认设置，点击“Create repository”按钮，就成功地创建了一个新的Git仓库：

github-create-repo-2

目前，在GitHub上的这个learngit仓库还是空的，GitHub告诉我们，可以从这个仓库克隆出新的仓库，也可以把一个已有的本地仓库与之关联，然后，把本地仓库的内容推送到GitHub仓库。

现在，我们根据GitHub的提示，在本地的learngit仓库下运行命令：

$ git remote add origin git@github.com:michaelliao/learngit.git
请千万注意，把上面的michaelliao替换成你自己的GitHub账户名，否则，你在本地关联的就是我的远程库，关联没有问题，但是你以后推送是推不上去的，因为你的SSH Key公钥不在我的账户列表中。

添加后，远程库的名字就是origin，这是Git默认的叫法，也可以改成别的，但是origin这个名字一看就知道是远程库。

下一步，就可以把本地库的所有内容推送到远程库上：
```
$ git push -u origin master
Counting objects: 19, done.
Delta compression using up to 4 threads.
Compressing objects: 100% (19/19), done.
Writing objects: 100% (19/19), 13.73 KiB, done.
Total 23 (delta 6), reused 0 (delta 0)
To git@github.com:michaelliao/learngit.git
 * [new branch]      master -> master
Branch master set up to track remote branch master from origin.
```
把本地库的内容推送到远程，用git push命令，实际上是把当前分支master推送到远程。

由于远程库是空的，我们第一次推送master分支时，加上了-u参数，Git不但会把本地的master分支内容推送的远程新的master分支，还会把本地的master分支和远程的master分支关联起来，在以后的推送或者拉取时就可以简化命令。

推送成功后，可以立刻在GitHub页面中看到远程库的内容已经和本地一模一样：


## 分支管理

真正开发的时候都是在dev分支上面开发的
所以创建一个dev 分支

```
git checkout -b dev
```
git checkout命令加上-b参数表示创建并切换，相当于以下两条命令：
```
$ git branch dev
$ git checkout dev
Switched to branch 'dev'
```
```
git branch
* dev
  master
```
这个时候在dev分支上修改并提交
这个时候切换到master 分支然后进行合并，这样就把dev分支的修改合并到了master

```
➜  测试内容，可以删除 git:(dev) ✗ git branch
* dev
  master
➜  测试内容，可以删除 git:(dev) ✗ git add test
➜  测试内容，可以删除 git:(dev) ✗ git commit -m "测试"
[dev e9ca890] 测试
 2 files changed, 4 insertions(+), 2 deletions(-)
 delete mode 100644 xxxx
➜  测试内容，可以删除 git:(dev) git branch master
fatal: A branch named 'master' already exists.
➜  测试内容，可以删除 git:(dev) git branch master
fatal: A branch named 'master' already exists.
➜  测试内容，可以删除 git:(dev) git checkout master
Switched to branch 'master'
➜  测试内容，可以删除 git:(master) git merge dev
Updating ab99189..e9ca890
Fast-forward
 test | 5 ++++-
 1 file changed, 4 insertions(+), 1 deletion(-)
➜  测试内容，可以删除 git:(master)
```

git merge命令用于合并指定分支到当前分支。合并后，再查看readme.txt的内容，就可以看到，和dev分支的最新提交是完全一样的。
注意到上面的Fast-forward信息，Git告诉我们，这次合并是“快进模式”，也就是直接把master指向dev的当前提交，所以合并速度非常快。
当然，也不是每次合并都能Fast-forward，我们后面会讲其他方式的合并。
合并完成后，就可以放心地删除dev分支了：

```
git branch -d dev
Deleted branch dev (was e9ca890).
➜  测试内容，可以删除 git:(master) git branch
* master
➜  测试内容，可以删除 git:(master)
```

* 查看分支：git branch
* 创建分支：git branch <name>
* 切换分支：git checkout <name>
* 创建+切换分支：git checkout -b <name>
* 合并某分支到当前分支：git merge <name>
* 删除分支：git branch -d <name>


## 冲突解决
这个是git在使用过程中常见的场景
这里我们创建两个分支 dev1 和dev2
在dev1和dev2上面修改，并add和commit，之后切换到master，这个时候要合并分支，由于dev1和dev2对同一个地方做了修改，git不知道要使用哪个，这个时候就产生了冲突，需要手动解决

dev1
```
test
哈哈哈，今天天气不错
今天路上看到的妹子好漂亮
我要到那个妹子的微信了 O(∩_∩)O哈哈~
sjfjsdlfjlsd

sfdkjsdksklfj份sdkjf

dev1上面的修改
```

dev2
```
test
哈哈哈，今天天气不错
今天路上看到的妹子好漂亮
我要到那个妹子的微信了 O(∩_∩)O哈哈~
sjfjsdlfjlsd

sfdkjsdksklfj份sdkjf

dev2 分支修改
```

```
➜  测试内容，可以删除 git:(master) git merge dev1
Updating e9ca890..d9e202b
Fast-forward
 test | 4 +++-
 1 file changed, 3 insertions(+), 1 deletion(-)
➜  测试内容，可以删除 git:(master) git merge dev2  //在这里可以看到会有冲突的提示，这个时候要手动处理冲突
Auto-merging test
CONFLICT (content): Merge conflict in test
Automatic merge failed; fix conflicts and then commit the result.
```


```
test
哈哈哈，今天天气不错
今天路上看到的妹子好漂亮
我要到那个妹子的微信了 O(∩_∩)O哈哈~
sjfjsdlfjlsd

sfdkjsdksklfj份sdkjf

<<<<<<< HEAD
dev1上面的修改
=======
dev2 分支修改
>>>>>>> dev2

```

Git用<<<<<<<，=======，>>>>>>>标记出不同分支的内容，我们修改如下后保存：


```
test
哈哈哈，今天天气不错
今天路上看到的妹子好漂亮
我要到那个妹子的微信了 O(∩_∩)O哈哈~
sjfjsdlfjlsd

sfdkjsdksklfj份sdkjf

dev1上面的修改
dev2 分支修改

```

之后将处理好的冲突合并之后再提交

```
➜  测试内容，可以删除 git:(master) ✗ git add test
➜  测试内容，可以删除 git:(master) ✗ git commit -m "dev1和dev2分支合并"
```

执行`git status `可以查看状态

```
commit 5bc564e39d7ed6e8a1857334822240d35070330c
Merge: d9e202b c588f19
Author: xiaolei <xiaolei@qeeniao.com>
Date:   Sat Apr 21 11:22:43 2018 +0800

    dev1和dev2分支合并

commit c588f194903d74a55b8e2d5615a62776b9d70ad1
Author: xiaolei <xiaolei@qeeniao.com>
Date:   Sat Apr 21 11:19:05 2018 +0800

    dev2分支修改

commit d9e202b8862e03bfadce7dd873af6163426ec647
Author: xiaolei <xiaolei@qeeniao.com>
Date:   Sat Apr 21 11:18:12 2018 +0800

```

可以使用这个命令
```
git log --graph --pretty=oneline --abbrev-commit
```
查看分支情况合并情况

```
*   5bc564e dev1和dev2分支合并
|\
| * c588f19 dev2分支修改
* | d9e202b git dev1分支修改
|/
* e9ca890 测试
* d0410fb xxxx提交
* ab99189 妹子家好友了
* 59e3cc9 美眉
* ffba6ca 加入今天天气
* 8de8547 初始化测试仓库
```

## 分支管理策略

通常，合并分支时，如果可能，Git会用Fast forward模式，但这种模式下，删除分支后，会丢掉分支信息。
这里来测试一下
首先创建一个test分支
```
git checkout -b test
```
然后修改test分支的内容

```
test
哈哈哈，今天天气不错
今天路上看到的妹子好漂亮
我要到那个妹子的微信了 O(∩_∩)O哈哈~
sjfjsdlfjlsd

sfdkjsdksklfj份sdkjf

dev1上面的修改
dev2 分支修改
test 分支修改
```
之后
```
git add test
git commit -m "test分支修改"
```


现在切换到master 分支，并且准备
准备合并test分支，请注意--no-ff参数，表示禁用Fast forward：


```
git merge --no-ff -m "merge with no-ff" test
```

查看git log
```
git log --graph --pretty=oneline --abbrev-commit
```
结果
```
*   3cba559 merge with no-ff   // 这个是作为一个commit来提交的
|\
| * 3dcd292 test分支修改
|/
*   5bc564e dev1和dev2分支合并
|\
| * c588f19 dev2分支修改
* | d9e202b git dev1分支修改
|/
* e9ca890 测试
* d0410fb xxxx提交
* ab99189 妹子家好友了
* 59e3cc9 美眉
* ffba6ca 加入今天天气
* 8de8547 初始化测试仓库
```

这里遇到了这个问题
```
error: There was a problem with the editor 'vi'.
Not committing merge; use 'git commit' to complete the merge.
```

解决方案
```
在vim中添加内容
```

## bug 分支管理
场景
软件开发中，bug就像家常便饭一样。有了bug就需要修复，在Git中，由于分支是如此的强大，所以，每个bug都可以通过一个新的临时分支来修复，修复后，合并分支，然后将临时分支删除。
当你接到一个修复一个代号101的bug的任务时，很自然地，你想创建一个分支issue-101来修复它，但是，等等，当前正在dev上进行的工作还没有提交：

并不是你不想提交，而是工作只进行到一半，还没法提交，预计完成还需1天时间。但是，必须在两个小时内修复该bug，怎么办？
幸好，Git还提供了一个stash功能，可以把当前工作现场“储藏”起来，等以后恢复现场后继续工作：
```
git stash
```
在保存了之后就可以切换达到test分支干活了
10分钟之后弄完了，这个时候切换到master分支
工作区是干净的，刚才的工作现场存到哪去了？用git stash list命令看看：
```
git stash list
stash@{0}: WIP on master: 22c576a Merge branch 'test'
(END)
```
工作现场还在，Git把stash内容存在某个地方了，但是需要恢复一下，有两个办法：
一是用git stash apply恢复，但是恢复后，stash内容并不删除，你需要用git stash drop来删除；
另一种方式是用git stash pop，恢复的同时把stash内容也删了：
```
git stash apply
On branch master
Changes not staged for commit:
 (use "git add <file>..." to update what will be committed)
 (use "git checkout -- <file>..." to discard changes in working directory)

 modified:   test

no changes added to commit (use "git add" and/or "git commit -a")
```

```
git stash pop
On branch master
Changes not staged for commit:
  (use "git add <file>..." to update what will be committed)
  (use "git checkout -- <file>..." to discard changes in working directory)

	modified:   test

no changes added to commit (use "git add" and/or "git commit -a")
Dropped refs/stash@{0} (09b29d2a32237dcecd7df3f81390a2aa8f63efc3)
```


你可以多次stash，恢复的时候，先用git stash list查看，然后恢复指定的stash，用命令：


```
stash@{0}: WIP on master: 50fd45b test
stash@{1}: WIP on master: 22c576a Merge branch 'test'
```
```
git stash apply stash@{0}
```
## 多人协作

当你从远程仓库克隆时，实际上Git自动把本地的master分支和远程的master分支对应起来了，并且，远程仓库的默认名称是origin。

要查看远程库的信息，用git remote：
```
git remote
origin
```
或者，用git remote -v显示更详细的信息：
```
git remote -v
origin	git@github.com:smarterxiao/test.git (fetch)
origin	git@github.com:smarterxiao/test.git (push)
```

推送分支

推送分支，就是把该分支上的所有本地提交推送到远程库。推送时，要指定本地分支，这样，Git就会把该分支推送到远程库对应的远程分支上：
```
git push origin master
```
如果要推送其他分支，比如dev，就改成：
```
git push origin dev
```
但是，并不是一定要把本地分支往远程推送，那么，哪些分支需要推送，哪些不需要呢？
master分支是主分支，因此要时刻与远程同步；
dev分支是开发分支，团队所有成员都需要在上面工作，所以也需要与远程同步；
bug分支只用于在本地修复bug，就没必要推到远程了，除非老板要看看你每周到底修复了几个bug；
feature分支是否推到远程，取决于你是否和你的小伙伴合作在上面开发。


抓取分支

这个时候不同的人在不同的分支上面开发，
现在，模拟一个你的小伙伴，可以在另一台电脑（注意要把SSH Key添加到GitHub）或者同一台电脑的另一个目录下克隆：
当你的小伙伴从远程库clone时，默认情况下，你的小伙伴只能看到本地的master分支。不信可以用git branch命令看看：
```
git clone git@github.com:smarterxiao/test.git
Cloning into 'test'...
remote: Counting objects: 52, done.
remote: Compressing objects: 100% (18/18), done.
remote: Total 52 (delta 15), reused 52 (delta 15), pack-reused 0
Receiving objects: 100% (52/52), done.
Resolving deltas: 100% (15/15), done.
```

```
git branch
* master
```
发现之后master分支
这个时候怎么处理呢？


```
 git checkout -b dev origin/dev
```

这样就拉去了服务器的dev分支
同理拉去test分支

修改test分支的test内容，add并commit，之后再推送的时候要先pull 在push
```
git push
Counting objects: 3, done.
Delta compression using up to 4 threads.
Compressing objects: 100% (2/2), done.
Writing objects: 100% (3/3), 263 bytes | 0 bytes/s, done.
Total 3 (delta 1), reused 0 (delta 0)
remote: Resolving deltas: 100% (1/1), completed with 1 local object.
To github.com:smarterxiao/test.git
   abe8963..ddcf3e6  test -> test
```

如果pull失败
原因是没有指定本地dev分支与远程origin/dev分支的链接，根据提示，设置dev和origin/dev的链接

```
git branch --set-upstream dev origin/dev
Branch dev set up to track remote branch dev from origin.
```


## git 标签

切换到想要打的分支
```
git tag "1.0"//这是标签
➜  test git:(test) git tag// 查看标签
1.0
```

默认标签是打在最新提交的commit上的。有时候，如果忘了打标签，比如，现在已经是周五了，但应该在周一打的标签没有打，怎么办？

方法是找到历史提交的commit id，然后打上就可以了：

$ git log --pretty=oneline --abbrev-commit
6a5819e merged bug fix 101
cc17032 fix bug 101
7825a50 merge with no-ff
6224937 add merge
59bc1cb conflict fixed
400b400 & simple
75a857c AND simple
fec145a branch test
d17efd8 remove test.txt
...
比方说要对add merge这次提交打标签，它对应的commit id是6224937，敲入命令：
```
$ git tag v0.9 6224937
``
再用命令git tag查看标签：

$ git tag
v0.9
v1.0

基本上git常用的东西就整理好了
