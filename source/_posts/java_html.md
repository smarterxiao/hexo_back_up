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



|常用标签|语意|
|:----:|:----:|
|b|加粗，没有语义|
|i|斜体，没有语义|
|strong|加粗，带有语义|
|em|斜体，带有语义|

## 图片标签的显示
上面说完常用的文字标签，现在来说一下图片如何显示

```
<!DOCTYPE html>
<html>
	<head>
		<meta charset="utf-8">
		<title></title>
	</head>
	<body>
		
		
		<img src="img/th.jpg" width="50%" alt="这张图片加载有问题" />
	</body>
</html>

```

这个是一个显示图片的案例，现在是按照百分比来显示的。宽度为浏览器大小的50%

```
<!DOCTYPE html>
<html>
	<head>
		<meta charset="utf-8">
		<title></title>
	</head>
	<body>
		
		
		<img src="img/th.jpg" width="500px" alt="这张图片加载有问题" />
	</body>
</html>

```

这个是按照宽度是500px来显示，并不会随着浏览器大小改变而改变。

## 拓展文件路径

|标识|语意|
|:----:|:----:|
|./|代表当前路径|
|../|代表上一级路径|
|../../|上上一级路径|

# 网站的友情链接

|标识|语意|
|:----:|:----:|
|ul|无序|
|ol|有序|
|li|ul和ol的行|
|type|控制显示方式|

看一个例子


```
<!DOCTYPE html>
<html>
	<head>
		<meta charset="utf-8">
		<title></title>
	</head>
	<body>
	<ul type="disc">
		<li>真爱网</li>
		<li>迷宫网</li>
		<li><a href="http://www.baidu.com">baidu</a></li>
		</ul>
		
		<ol type="disc">
			<li>真爱网</li>
			<li>迷宫网</li>
			<li><a href="http://www.baidu.com">baidu</a></li>
			</ol>

	</body>
</html>

```
![Alt text](pic1.png "效果图")

这里还有一个标签

|标识|语意|
|:----:|:----:|
|a|超链接|
|href|链接地址|
|target|打开方式|


## 表格

|标识|语意|
|:----:|:----:|
|tr|行|
|td/th|列|

```
<!DOCTYPE html>
<html>
	<head>
		<meta charset="utf-8">
		<title></title>
	</head>
	<body>
		<table width="100%" border="1">
			<tr>
				<th>Month</th>
				<th>Savings</th>
			</tr>
			<tr>
				<td>January</td>
				<td>$100</td>
			</tr>
		</table>
	</body>
</html>


```


![Alt text](pic2.png "效果图")

在调试表格的时候建议加一下边框`border`，方便定位

下面来看一个稍微复杂一点的例子
```
<!DOCTYPE html>
<html>
	<head>
		<meta charset="utf-8">
		<title></title>
	</head>
	<body>
		
		<table border="1" width="100%">
			<tr>
				
				<table>
					
					<tr>
						
						<td><img src="img/th.jpg" width="100px"></td>
						<td>登录</td>
						<td>注册</td>
						<td>其他</td>
					</tr>
				</table>
				
			</tr>
			
			<tr>
				<table>
				
				<tr>
					<td>首页</td>
					<td>手机数码</td>
					<td>鞋靴箱包</td>
					<td>电脑办公</td>
					<td>香烟酒水</td>
					
				</tr>
			</table>
				
				
			</tr>
				
			<tr>
				<table>
				
				<tr>
				<td> <strong>热门商品</strong></td>
					</tr>
				</table>
					
			</tr>
			
			<tr>
				<table width="100%">
					<tr>
						<td bgcolor="aliceblue" rowspan="2"><img src="products/1/cs10004.jpg"  >
						<p>rowspan</p>
						</td>
						<td colspan="2" bgcolor="red" ><img src="products/1/cs10004.jpg" >	<p>colspan</p></td>
						<td><img src="products/1/cs10004.jpg"></td>
						<td><img src="products/1/cs10004.jpg"></td>
				
						
					</tr>
					<tr>
						<td><img src="products/1/cs10004.jpg"></td>
						<td><img src="products/1/cs10004.jpg"></td>
						<td><img src="products/1/cs10004.jpg"></td>
						<td><img src="products/1/cs10004.jpg"></td>
		
						
					</tr>
					
				</table>
				
				
			</tr>
			
			<tr>
				
				<table>
					
					<tr>
						<td><a href="www.baidu.com"> 百度</a></td>
							<td><a href="www.alibaba.com"> 阿里</a></td>
								<td><a href="www.jd.com"> 二手东</a></td>
							
					</tr>
				</table>
			</tr>
			
			
		</table>
	</body>
</html>


```


这里有一个属性要说一下

|标识|语意|
|:----:|:----:|
|rowspan|行向占据几列，如果是0，则到表格最后一个|
|colspan/th|纵向占据几行，如果是0，则到表格最后一个|

## 一个网站登录页面

这里要介绍一下一个标签 form标签和input标签

form标签：
|标识|语意|
|:----:|:----:|
|action|提交的地址|
|method|提交方式：get和post|

input标签：

|标识|语意|
|:----:|:----:|
|type|提交类型|
|placeholder|默认提示信息|
|name|参数名称|
|id|便于操作|

现在我们来创建一个简易版本的登录

```
<!DOCTYPE html>
<html>
	<head>
		<meta charset="utf-8">
		<title></title>
	</head>
	<body>
		
		<form action="2.html">
			
			<table>
				
				
				<tr>
					<td colspan="2"><font color="blue" size="5">会员注册</font></td>
				</tr>
				
				
				<tr>
					
					<td>
						用户名
					</td>
					<td><input type="text" placeholder="请输入用户名"></td>
					
				</tr>
				
						<tr>
					
					<td>
						密码
					</td>
					<td><input type="password" placeholder="请输入密码"></td>
				
				</tr>
					<tr>
					
					<td>
						email
					</td>
				<td>	<input type="email" placeholder="请输入邮箱"></td>
				
				</tr>
					<tr>
			
					<td>
						性别
					</td>
					<td><input type="radio" name="sex" >男
					<input type="radio" name="sex" >女
					<input type="radio" name="sex" >妖</td>
				</tr>
				
				<tr> 
				<td>验证码</td>
				<td><input type="text" placeholder="请输入验证码"></td>
				</tr>
				
				<tr>
					<td colspan="2"><input type="submit" value="注册"></td>
				</tr>
			</table>
			
		</form>
		
	</body>
</html>

```
![Alt text](login.png "效果图")

## 最后在介绍一个 frameset标签
可以理解在一个页面中显示多个页面的内容

来一个例子

frameSet.html
```
<!DOCTYPE html>
<html>
	<head>
		<meta charset="utf-8">
		<title></title>
	</head>

		
		<frameset rows="15%,*">
			<frame src="aaa.html" />
			
			<frameset cols="30%,*">
					<frame src="bbb.html" name="bbb"/>
				<frame src="ccc.html" name="rightFrame" />
		
			</frameset>
		</frameset>
	
</html>


```
aaa.html
```
<!DOCTYPE html>
<html>
	<head>
		<meta charset="utf-8">
		<title></title>
	</head>
	<body bgcolor="yellow">
		aaa
	</body>
</html>

```

bbb.html
```
<!DOCTYPE html>
<html>
	<head>
		<meta charset="utf-8">
		<title></title>
	</head>
	<body bgcolor="red">
		bbb
		// 这里进行fram之间的跳转
		<a href="login.html" target="rightFrame"> 登录</a>
		<a href="#">其他</a>
	</body>
</html>

```

ccc.html
```
<!DOCTYPE html>
<html>
	<head>
		<meta charset="utf-8">
		<title></title>
	</head>
	<body bgcolor="green">
		ccc
	</body>
</html>

```






# 疑问
* 为什么html能够被浏览器显示，浏览器是如何解释html的呢？
