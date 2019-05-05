---
title: java_js
date: 2019-05-04 17:36:07
tags:
categories: java
---

# 简介
这一章节简单介绍一下js的使用

# js必知必会
- 定时器
  - setInterval : 每隔多少毫秒执行一次函数
  - setTimeout: 多少毫秒之后执行一次函数
  - clearInterval
  - clearTimeout
- 显示广告 img.style.display  = "block"
- 隐藏广告 img.style.display  = "none"


# js实战

## 开屏广告
### 需求 
一般网页，当我们刚打开的时候，它会5秒之后，显示一个广告，让我们看5秒钟，然后他的广告就自动消失了！

### 分析
1. 确定事件: 页面加载完成的事件 onload
2. 事件要触发函数:  init()
3. init函数里面做一件事: 
   1. 启动一个定时器 : setTimeout() 
   2. 显示一个广告
      1. 再去开启一个定时5秒钟之后,关闭广告
### 代码实现
```
<!DOCTYPE html>
<html>
	<head>
		<meta charset="utf-8" />
		<title></title>
		
		<script>
			
			
			function init(){
				setTimeout("showAD()",5000)
			}
			
			function showAD(){
				
			var img1=	document.getElementById("img1")
				img1.style.display="block"
				setTimeout("hideAd()",5000)
			}
			
			function hideAd(){
				
					var img1=	document.getElementById("img1")
					img1.style.display="none"
			
			}
		</script>
	</head>
	<body onload="init()">
		
		<img src="image/059b5245-e3c8-43bf-80fe-700f0e4e68b8-thumbnail.jpg" id="img1"  style="display: none;" />
	</body>
</html>

```

## 表单校验

### 需求

昨天我们做了一个简单的表单校验，每当用户输入出错的时候，我们是弹出了一个对话框，提示用户去修改。这样的用户体验效果非常不好好。我们今天就是需要来对他进行一个修改，当用户输入信息有问题的时候，我们就再输入框的后面给他一个友好提示。0