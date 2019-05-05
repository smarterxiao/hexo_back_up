---
title: java_css
date: 2019-05-03 17:45:16

tags:
categories: java
---

# 简介
这一章简单学习一下css。css是样式表，可以理解为一种装饰

# CSS正式学习
## 上一章节小结
上一章节主要看了html的一些标签，并且写了几个小demo。这次我们使用div和css替代之前的table来进行页面布局

## 如何使用css样式表
看一下css样式表的最基本结构
```
<style>
  选择器{
    属性名称:属性的值;
    属性名称2: 属性的值2;
  }
</style>
```
可以看到css通过选择器来定位元素。选择器有很多种
这里可以参考 http://www.w3school.com.cn/css/index.asp
我们这里只说一下三个常用的css选择器

1. 元素选择器

元素的名称{
  属性名称:属性的值;
  属性名称:属性的值;
}


2. ID选择器:

以#号开头  ID在整个页面中必须是唯一的
#ID的名称{
  属性名称:属性的值;
  属性名称:属性的值;
}

3. 类选择器

以 . 开头 
.类的名称{
   属性名称:属性的值;
  	属性名称:属性的值;
}


CSS的优先级：
按照选择器搜索精确度来编写:		 	行内样式 > ID选择器 > 类选择器  > 元素选择器

就近原则: 哪个离得近,就选用哪个的样式
这里有三种方式引用css样式表
1. 外部样式: 通过link标签引入一个外部的css文件

2. 内部样式: 直接在style标签内编写CSS代码

3. 行内样式: 直接在标签中添加一个style属性, 编写CSS样式

Css中还有一个浮动元素，可以脱离正常的文档流，在正常的文档流中不占用空间

float属性:
		left
		right
			
		clear属性: 清除浮动
				both : 两边都不允许浮动
				left: 左边不允许浮动
				right : 右边不允许浮动
		流式布局



## 实战

先写一个简单的例子

```
<!DOCTYPE html>
<html>
	
	<style>
		.logo{
			float: left;
			width: 33%;
			height: 60px;
			line-height: 60px;
			border: 1px;
			border-style: solid;
			border-color: red;
		}
		
		.amenu{
			color: white;
			text-decoration: none;
			height: 50px;
			line-height: 50px;
		}
			.product{
				float: left; text-align: center; width: 16%; height: 240px;
			}
	</style>
	<head>
		<meta charset="utf-8" />
		<title></title>
	</head>
	<body>
		
		<div>
		<div>
			<div class="logo">
				<img src="img/logo2.png" />
			</div>
			
			<div class="logo"><img src="img/header.png">
			</div>
			
			<div class="logo">
				<a href="#">登录</a>
				<a href="#">注册</a>
				<a href="#">购物车</a>
			</div>
			
			<div style="clear: both;"></div>
			
			</div>
			
			<div style="background-color: black;height: 50px;">
				<a href="#" class="amenu">首页</a>
				<a href="#" class="amenu">手机数码</a>
				<a href="#" class="amenu">电脑办公</a>
				<a href="#" class="amenu">鞋靴箱包</a>
				<a href="#" class="amenu">香烟酒水</a>
			</div>
			
			
			
			<div >
				
				<img width="100%" src="img/1.jpg" />
			</div>
			<div>
				
				<div> <h2>最新商品<img src="img/title2.jpg" /> </h2>	</div>
			
			<div style="width: 15%; height: 480px; float: left;">
				<img src="products/hao/big01.jpg"  width="100%" height="100%"/>
			</div>
			
			<div style="width: 84%; height: 480px; float: left;">
				<div style="height: 240px; width: 50%; float: left;">
					
					<img src="products/hao/middle01.jpg" width="100%" height="100%" />
					
				</div>
				
				
				<div class="product">
					
					<img src="products/hao/small08.jpg" />
					<p>高压锅</p>
					<p style="color: red;"> $998</p>
				</div>
				
				<div class="product">
					
					<img src="products/hao/small08.jpg" />
					<p>高压锅</p>
					<p style="color: red;"> $998</p>
				</div>
				<div class="product">
					
					<img src="products/hao/small08.jpg" />
					<p>高压锅</p>
					<p style="color: red;"> $998</p>
				</div>
					<div class="product">
					
					<img src="products/hao/small08.jpg" />
					<p>高压锅</p>
					<p style="color: red;"> $998</p>
				</div>	<div class="product">
					
					<img src="products/hao/small08.jpg" />
					<p>高压锅</p>
					<p style="color: red;"> $998</p>
				</div>	<div class="product">
					
					<img src="products/hao/small08.jpg" />
					<p>高压锅</p>
					<p style="color: red;"> $998</p>
				</div>	<div class="product">
					
					<img src="products/hao/small08.jpg" />
					<p>高压锅</p>
					<p style="color: red;"> $998</p>
				</div>	<div class="product">
					
					<img src="products/hao/small08.jpg" />
					<p>高压锅</p>
					<p style="color: red;"> $998</p>
				</div>	<div class="product">
					
					<img src="products/hao/small08.jpg" />
					<p>高压锅</p>
					<p style="color: red;"> $998</p>
				</div>
			</div>
				<div>
				<img src="products/hao/ad.jpg" width="100%"/>
			</div>
						<div>
				
				<div> <h2>最新商品<img src="img/title2.jpg" /> </h2>	</div>
			
			<div style="width: 15%; height: 480px; float: left;">
				<img src="products/hao/big01.jpg"  width="100%" height="100%"/>
			</div>
			
			<div style="width: 84%; height: 480px; float: left;">
				<div style="height: 240px; width: 50%; float: left;">
					
					<img src="products/hao/middle01.jpg" width="100%" height="100%" />
					
				</div>
				
				
				<div class="product">
					
					<img src="products/hao/small08.jpg" />
					<p>高压锅</p>
					<p style="color: red;"> $998</p>
				</div>
				
				<div class="product">
					
					<img src="products/hao/small08.jpg" />
					<p>高压锅</p>
					<p style="color: red;"> $998</p>
				</div>
				<div class="product">
					
					<img src="products/hao/small08.jpg" />
					<p>高压锅</p>
					<p style="color: red;"> $998</p>
				</div>
					<div class="product">
					
					<img src="products/hao/small08.jpg" />
					<p>高压锅</p>
					<p style="color: red;"> $998</p>
				</div>	<div class="product">
					
					<img src="products/hao/small08.jpg" />
					<p>高压锅</p>
					<p style="color: red;"> $998</p>
				</div>	<div class="product">
					
					<img src="products/hao/small08.jpg" />
					<p>高压锅</p>
					<p style="color: red;"> $998</p>
				</div>	<div class="product">
					
					<img src="products/hao/small08.jpg" />
					<p>高压锅</p>
					<p style="color: red;"> $998</p>
				</div>	<div class="product">
					
					<img src="products/hao/small08.jpg" />
					<p>高压锅</p>
					<p style="color: red;"> $998</p>
				</div>	<div class="product">
					
					<img src="products/hao/small08.jpg" />
					<p>高压锅</p>
					<p style="color: red;"> $998</p>
				</div>
			</div>
			<!--8. 第七部分: 放置一张图片-->
			<div>
				<img src="../img/footer.jpg" width="100%"/>
			</div>
			<!--9. 第八部分: 放一堆超链接-->
			<div style="text-align: center;">
				        
					<a href="#">关于我们</a>
					<a href="#">联系我们</a>
					<a href="#">招贤纳士</a>
					<a href="#">法律声明</a>
					<a href="#">友情链接</a>
					<a href="#">支付方式</a>
					<a href="#">配送方式</a>
					<a href="#">服务声明</a>
					<a href="#">广告声明</a>
					
					<br />
					
					Copyright © 2005-2016 小猪快跑 版权所有
			</div>
			
			</div>
			
			
		</div>
	</body>
</html>


```


现在写一个登录网页

```
<!DOCTYPE html>
<html>
	<head>
		<meta charset="utf-8">
		<title></title>
		
		<link rel="stylesheet" type="text/css"  href="css/main.css"/>
	</head>
	<body>
		
		
		<div>
			<div>
				<div class="logo">
					<img src="img/logo2.png" />
				</div>
				
				<div class="logo"><img src="img/header.png">
				</div>
				
				<div class="logo">
					<a href="#">登录</a>
					<a href="#">注册</a>
					<a href="#">购物车</a>
				</div>
				
				<div style="clear: both;"></div>
				
			</div>
			
			<div style="background-color: black;height: 50px;">
				<a href="#" class="amenu">首页</a>
				<a href="#" class="amenu">手机数码</a>
				<a href="#" class="amenu">电脑办公</a>
				<a href="#" class="amenu">鞋靴箱包</a>
				<a href="#" class="amenu">香烟酒水</a>
			</div>
			
			
			<div style="background: url(image/regist_bg.jpg); height: 500px;">
				
				
				<div style="position: absolute;top: 200px; left: 350px; border: 5px solid darkgray; width: 50%;height: 50%; background-color: white;">
				
				<table width="60%" align="center">
					<tr>
						<td colspan="2"><font color="blue" size="6"> 会员注册</font> USER REGISTER</td>
						
					</tr>
					
					<tr> <td> 用户名</td>
					<td> <input type="text" placeholder="请输入用户名"/></td>
					
					
					</tr>
						<tr> <td> 密码</td>
					<td> <input type="password" placeholder="请输入密码"/></td>
					</tr>
						<tr> <td> 确认密码</td>
					<td> <input type="password" placeholder="确认密码"/></td>
					</tr>
							<tr> <td> email</td>
					<td> <input type="email" placeholder="请输入邮箱"/></td>
					</tr>
					<tr> <td> 性别</td>
					<td> <input type="radio" name="sex"/>男
					<input type="radio" name="sex"/>女
					<input type="radio" name="sex"/>妖
					</td>
					
					
					</tr>
					
					<tr> <td></td><td><input value="注册" type="submit"/></td></tr>
					
					
				</table>
				</div>
				
				<!--5. 第四部分是FOOTER图片-->
		
			</div>
			<div>
				<img src="img/footer.jpg" width="100%"/>
			</div>
			<!--9. 第四部分: 放一堆超链接-->
			<div style="text-align: center;">
				        
					<a href="#">关于我们</a>
					<a href="#">联系我们</a>
					<a href="#">招贤纳士</a>
					<a href="#">法律声明</a>
					<a href="#">友情链接</a>
					<a href="#">支付方式</a>
					<a href="#">配送方式</a>
					<a href="#">服务声明</a>
					<a href="#">广告声明</a>
					
					<br />
					
					Copyright © 2005-2016  枫雲版权所有
			</div>
			</div>
		
	</body>
</html>

```


css通过引用的方式引入

main.css

```
.logo{
				float: left;
				width: 33%;
				/*border-width: 1px;
				border-style: solid;
				border-color: red;*/
				height: 60px;
				line-height: 60px;
		/*		border: 1px solid red;*/
			}
			
			
			.amenu{
				color: white;
				text-decoration: none;
				height: 50px;
				line-height: 50px;
			}
			
			.product{
				float: left; text-align: center; width: 16%; height: 240px;
			}
			
```




## 使用js完成简单的数据校验
这里不会讲解js的语法。js语法比较简单，可以去看一下

```
<!DOCTYPE html>
<html>
	<head>
		<meta charset="utf-8">
		<title></title>
		
		<script>
			
			
			
			function checkForm(){
				var inputObj=document.getElementById("username");
				var uValue=inputObj.value;
				// alert(uValue)
				if(uValue.length<6){
					alert("对不起，您的长度太短了")
					return false;
				}
				var input_passWord=document.getElementById("password");
				var uPass=input_passWord.value;
				if(uPass.length<6){
					alert("对不起，您的密码长度太短了")
					return false;
				}
				var input_repassWord=document.getElementById("repassword");
				
					var uRePass = input_repassWord.value;
				if(uPass != uRePass){
					alert("对不起,两次密码不一致!");
					return false;
				}
				
				//校验手机号
				var input_mobile = document.getElementById("mobile");
				var uMobile = input_mobile.value;
				//
				if(!/^[1][3578][0-9]{9}$/.test(uMobile)){
					
					alert("对不起,您的手机号无法识别!");
					
					return false;
				}
				
				//校验邮箱: /^([a-zA-Z0-9_-])+@([a-zA-Z0-9_-])+(\.[a-zA-Z0-9_-])+/
				var inputEmail = document.getElementById("email");
				var uEmail = inputEmail.value;
				
				if(!/^([a-zA-Z0-9_-])+@([a-zA-Z0-9_-])+(\.[a-zA-Z0-9_-])+/.test(uEmail)){
					alert("对不起,邮箱不合法");
					return false;
				}			
				return true;
				
			}
		</script>
		
		
	</head>
<body>
		<form action="JS开发步骤.html" onsubmit="return checkForm()">
			<div>用户名:<input id="username" type="text"  /></div>
			<div>密码:<input id="password" type="password"  /></div>
			<div>确认密码:<input id="repassword" type="password"  /></div>
			<div>手机号码:<input id="mobile"  type="number"  /></div>
			<div>邮箱:<input id="email" type="text"  /></div>
			<div><input type="submit" value="注册"  /></div>
		</form>
	</body>
</html>

```

## 使用js完成简单轮播图

```
<!DOCTYPE html>
<html>
	<head>
		<meta charset="utf-8">
		<title></title>
		
		<script>
			
			var index=1;
			function changeAd(){
				
		
		
		var imgAd=		document.getElementById("imgAd")
		
		imgAd.src="image/"+(index%3+1)+".jpg"
		index++
			}
			
			function init(){
				setInterval("changeAd()",3000)
			}
			
		</script>
	</head>
	<body onload="init()">
		
		
		<img src="image/1.jpg" id="imgAd" />
	</body>
</html>

```