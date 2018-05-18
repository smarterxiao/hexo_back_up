---
title: kotlin学习
date: 2018-05-06 23:46:10
tags: kotlin
---

> 这个是kotlin的学习笔记，kotlin成为android开发第一语言快一年了，现在才看是学习，感觉有点晚。书名是kotlin实战
# kotlin的定义和目的
> 这一章简单带过
## kotlin初体验
先来一个简单的例子

```
data class Person(val  name:String,val age:Int?=null)
fun main(args: Array<String>) {
    val persons= listOf<Person>(Person("alice"),Person("Bob",age=29))
    val oldest=persons.maxBy { it.age?:0 }
    println("The oldest is: $oldest")
}
```
运行的输出结果是

```
The oldest is: Person(name=Bob, age=29)
```
来解释一下这段代码的意思。这里申明了简答的数据类，他包括了两个属性：name和age。age属性默认为null（如果没有指定），在创建person的列表时，省略了alice的年龄，所以这里的年龄使用的是默认值null，然后你调用了maxBy函数来查找列表中你年纪最大的那个的那个人，传递给这个函数的是一个lambda表达式需要一个参数，使用it作为这个参数的默认名称，如果age属性为null，elvis运算符（？：）会返回零，因为alice的年龄没有指定，elvis运算符使用0来替代他，所以bob就成了年龄最大的人
感觉不错
## kotlin的主要特征
### 目标平台：服务器端，android和任何java运行的地方
kotlin的首要目标是提供一种更加简洁、高效、安全的替代java的语言，并适用于现在所有的java环境

### 静态类型
kotlin和java一样是一种静态类型的编程语言，这意味着所有表达式的类型在编译期已经确定，而编译器就能验证对象是否包含了你想访问的方法或者字段
这个和动态类型语言形成了鲜明的对比，groovy是动态的。这些语言允许你定义可以存储任何数据类型的变量。
另外kotlin不需要你明确的申明每个变量的类型。
```
val x=1
```
在申明变量的时候，由于变量初始化为整形值，kotlin自动判断出他的类型是int，这个和java不同

### 函数式和面向对象
支持lambda
# kotlin基础
## 基本要素：函数和变量
