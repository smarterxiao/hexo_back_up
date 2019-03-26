---
title: java_html
date: 2019-03-17 22:14:50
tags:
categories: java
---
# 简介
这一章节主要是对一些常用html标签的介绍吧，这里使用的工具是hbuilderx，后期会更换为intellij idea。hbuilderx主要是不用配置java环境，下载基础版本就可以 http://www.dcloud.io/hbuilderx.html 。浏览器个人推荐使用chrome
推荐大家可以上http://www.w3school.com.cn/ 这个里面有完整的html标签。用的时候可以查一下。我这里只是入门。

# 常用标签和显示
## HTML语法规范
1. 上面是一个文档声明 
2. 根标签 html
3. html文件主要包含两部分. 头部分和体部分
	* 头部分 : 主要是用来放置一些页面信息
	* 体部分 : 主要来放置我们的HTML页面内容
4. 通过标签来对内容进行描述,标签通常都是由开始标签和结束标签组成  
5. 标签不区分大小写, 官方建议使用小写


## 一个最简单的html代码示例


```
<!DOCTYPE html>
<html>
	<head>
		<meta charset="utf-8" />
		<title></title>
	</head>
	<body>
		
	</body>
</html>

```
这个是我使用Hbuilder创建的一个index.html文件的代码
我们看一下显示的效果，

![Alt text](blank.png "效果图")

可以发现，这是一个空页面。

## 在这个空页面上插入简单的内容，查看效果

```
<!DOCTYPE html>
<html>
	<head>
		123
		<meta charset="utf-8" />
		<title>这是title</title>
		456
	</head>
	<body>
		这是body
		</body>
	</body>
</html>

```
![Alt text](pic3.png "效果图")

## 一些常用的文字控制显示标签
这里我们使用常用的几个对文字有效果的标签查看效果
```
<!DOCTYPE html>
<html>
	<head>
		123
		<meta charset="utf-8" />
		<title>这是title</title>
		456
	</head>
		<hr />
	<body>
		这是body
		<p>王五</p>
		<p><font size="20" color="red">王五</font></p>
		<strong><p>王五</p></strong>
		<em>王五</em>
		<i>王五</i>
		<h1>王五</h1>
		<h2>王五</h2>
		<h3>王五</h3>
		</body>
	</body>
</html>

```
这里有很多个王五，我们看一下他的显示效果：

![Alt text](pic4.png "效果图")

# 疑问
* 为什么html能够被浏览器显示，浏览器是如何解释html的呢？
