---
title: hexo配置心得
date: 2018-06-25 23:29:07
tags:
---


近期在写3-hexo主题时，发现文章（site.posts）排序按照.md文件的创建时间排序，而没有按照文章中的date排序。

这就导致了一个问题，我重装了一次电脑，.md文件通过git备份了，还原回来的时候，md的创建时间都是一样的，所以文章列表就按照文章标题排序了

随后就想起了以前使用yilia主题时，设置过置顶文章。所以做了排序，顺便做了置顶的功能。

@牵猪的松鼠根据这篇文章写了一个npm插件 hexo-generator-topindex
安装插件命令： npm install hexo-generator-topindex --save
如果安装插件，可跳过第一部分 #修改hexo的js代码，直接看第二部分 #设置置顶


---
title: 每天一个linux命令
date: 2017-01-23 11:41:48
top: 1
categories:
- 运维
tags:
- linux命令
---

如果存在多个置顶文章，top后的参数越大，越靠前。
