---
title: kotlin实战-第六章-kotlin类型系统
date: 2018-09-25 09:38:58
tags:
- 基础
- kotlin
categories: android
---

# 可空性

kotlin最大的好处时可以避免很多空指针问题，这个时java中最常见的问题。
java和kotlin最大的区别在于kotlin对于空类型的显示支持

看一下java的代码
```
    int StrLen(String s){
        return  s.length();
    }
```
这个函数其实时不安全的，只需要传递一个`null`值
在kotlin中
```
fun  strLen(s:String)=s.length
```
如果在使用的时候调用如下
```
fun main(args: Array<String>) {
    strLen(null)
}

```
程序时无法运行的。直接在编译期报错
如果允许为空，必须显示声明
```
fun  strLen(s:String?)=s.length
```
在变量类型后面加`？`表明这个变量可以为空
但是这样还是不能通过编译,因为这个变量可能为空，必须要手动处理
看一下他的报错
```
Error:(12, 26) Kotlin: Only safe (?.) or non-null asserted (!!.) calls are allowed on a nullable receiver of type String?
```
并且不能把null赋值给非空类型变量
```
val x:String?=null
val y:String=
```
报错
```
Error:(12, 14) Kotlin: Type mismatch: inferred type is String? but String was expected
```
这个时候必须进行手动判断
```
fun strLenSafe(s:String?):Int=if(s!=null)s.length else 0
```
这样就可以正常编译了