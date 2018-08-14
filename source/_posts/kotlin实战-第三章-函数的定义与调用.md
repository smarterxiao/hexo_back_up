---
title: kotlin实战_第三章_函数的定义与调用
date: 2018-08-11 21:25:10
tags:
- 基础
- kotlin
categories: android
---

# 在kotlin中创建集合

* 创建一个set
```
  val set= hashSetOf<Int>(1,7,53)
```

* 创建一个list
```
    val list = arrayListOf<Int>(1, 7, 53)
```

* 创建一个map
```
    hashMapOf<Int,String>(1 to "one",2 to "two",3 to "three")
```

这里说一下to不是一个关键字，而是一个函数，后面会详细介绍
```
fun main(args: Array<String>) {
   val set= hashSetOf<Int>(1,7,53)
    val list = arrayListOf<Int>(1, 7, 53)
    val hashMapOf = hashMapOf<Int, String>(1 to "one", 2 to "two", 3 to "three")

    println(set.javaClass)
    println(list.javaClass)
    println(hashMapOf.javaClass)
}

```

看一下输出结果
```
class java.util.HashSet
class java.util.ArrayList
class java.util.HashMap
```
kotlin并没有采用自己的集合，而是采用标准java集合类。但是kotlin在这个基础上进行了拓展

```
    val strings = listOf<String>("first", "second", "three")
    println(strings.last())
```
输出结果是
```
three
```


```
    val of = setOf<Int>(1, 14, 2)
    println(of.max())
```

输出结果
```
14
```

后面会探究一下他的工作原理，以及java类上的新增方法的由来

# 让函数更好调用
我们来看一下java中一个经常使用的函数`toString`的输出
```
   val listOf = listOf<Int>(1, 2, 3)
    println(listOf)
```
输出结果
```
[1, 2, 3]
```
但是现在产品经理要求使用方括号的形式`(1, 2, 3)`，这个时候java很多时候回使用guava和apache Commons或者重写打印的函数。在kotlin中，他的标准库中有一个专门的函数来处理这种情况。
我们先自己写一个函数看一下代码

```
fun <T> joinToString(collection:Collection<T>,separator:String,prefix:String,postfix:String):String{

    val result=StringBuilder(prefix)
    for ((index,element) in collection.withIndex()){
        if(index>0) result.append(separator)
        result.append(element)
    }
    result.append(postfix)
    return  result.toString()
}
```

我们来试用一下
```
    val listOf = listOf<Int>(1, 2, 3)
    println(joinToString(listOf,";","(",")"))
```

输出结果
```
(1;2;3)
```

这个方法是可行的。接下来我们要聚焦到他的声明，如何修改才能让声明更加简单呢?

## 命名参数
我们现在关注的问题是一个函数的可读性。
举个栗子
```
joinToString(listOf,";","(",")")
```
在java中，这个如果不看注释的话很难理清楚这个函数这几个参数的具体含义。
而在kotlin中，可以做的更优雅：
```
   println(joinToString(listOf,separator = "; ",prefix = "(",postfix = ")"))
```
在使用kotlin定义的函数是，可以显示的表明一些参数的名称，为了避免混淆，从它之后的所有参数都需要标明名称。但是调用java和android函数不行。因为把参数名称存到.class文件是java8以及更高版本的可选功能，而kotlin需要保持和java6的兼容性。

## 默认参数值
java普遍存在的一个问题是重载函数实在太多了。比如Thread有8个构造方法。

kotlin可以在声明函数的时候，指定参数的默认值，这样就可以避免创建重载的函数。

```
fun <T> joinToString(collection:Collection<T>,separator:String=",",prefix:String="(",postfix:String=")"):String{

    val result=StringBuilder(prefix)
    for ((index,element) in collection.withIndex()){
        if(index>0) result.append(separator)
        result.append(element)
    }
    result.append(postfix)
    return  result.toString()
}
```
来使用一下
```
    val listOf = listOf<Int>(1, 2, 3)
    println(joinToString(listOf))
```

看一下输出结果
```
(1,2,3)
```
是不是很赞。如果使用命名参数，中间尅省略一些参数值，也可以按照自己的顺序给定需要的参数。
但是java并没有默认参数的概念。当从java中调用kotlin时，必须显式的指定所有参数。如果平凡的调用，可以使用`@JvmOverloads`注解，这样编译器会重新重新生成java重载函数。

## 消除静态工具类:顶层函数和属性
通常在写代码的时候，会写一个Utils来存放常用的方法。
在kotlin中不需要创建一个单独的类来存放常用的方法。可以吧这些函数直接放到代码文件的顶层，不用从属于任何的类。如果需要从包外面访问，需要import
创建一个名为join.kt的文件
```
package pkg2

@JvmOverloads
fun <T> joinToString(collection:Collection<T>,separator:String=",",prefix:String="(",postfix:String=")"):String{

    val result=StringBuilder(prefix)
    for ((index,element) in collection.withIndex()){
        if(index>0) result.append(separator)
        result.append(element)
    }
    result.append(postfix)
    return  result.toString()
}

class join{

}
```
看一下生成的.class文件，发现生成了两个文件
join.class
```
package pkg2

public final class join public constructor() {
}
```
JoinKt.class
```
package pkg2

@kotlin.jvm.JvmOverloads public fun <T> joinToString(collection: kotlin.collections.Collection<T>, separator: kotlin.String /* = compiled code */, prefix: kotlin.String /* = compiled code */, postfix: kotlin.String /* = compiled code */): kotlin.String { /* compiled code */ }


```
可以发现，他将这个方法单独的放在一个类中。
在java中可以通过`JoinKt.joinToString(...)`来调用。

### 顶层属性
和函数一样，属性也可以放到文件的顶层。在一个类外面单独保存的数据片段虽然不经常使用，但还是有它的价值。
举个例子，可以用val属性来计算一些函数被执行的次数：
```
var opCount=0
fun performOperation(){
    opCount++
}
fun  reportOperationCount(){
    println("Operation perform $opCount times")
}

```
就像这个值被存储到一个静态字段中一样。

如果想要一个变量像java一样以`public static final`属性暴露给java。可以用const来修饰
```
const val X=0
```
等同于java的
```
public static final X=0
```
对于`joinToString`这个方法我们已经改进了很多。现在让他更加好用一些。

#给别人的类添加方法：拓展函数和属性
kotlin的一大特色，就是可以平滑地于现有代码集成。甚至，纯kotlin的项目都可以基于java库构建，如JDK、Android框架。在一个现有项目中集成kotlin的时候，很多时候是不能将项目转换成为kotlin的。在使用这些API的时候，如果不重写，就能够使用kotlin为它带来的方便，这就很赞了。这里，可以使用kotlin拓展函数来实现。
理论上来说，拓展函数非常简单，就是一个类的成员函数，不过定义在类的外面。为了方便解释，我们添加一个方法来计算第一个和最后一个字符：

```
fun String.lastChar():Char=this.get(this.length-1)
```
看一下调用
```
fun main(args: Array<String>) {
println("kotlin".lastChar())
}

```

输出结果：
```
n
```

在这个例子中，String就是接收者类型，而"kotlin"就是接收者对象。
从某种意义来说，你已经为String类添加了自己的方法。即使字符串不是代码的一部分，也没有类的源代码，依然可以在自己的项目中根据需要去拓展方法，不管是kotlin，java，还是Groovy等其他JVM语言，只要他会被编译为java类。

在这个拓展函数中，可以像其他成员函数一样使用this。而且也可以像普通的成员函数一样，省略他
```
fun String.lastChar():Char=get(length-1)
```
在拓展函数中，可以直接访问被拓展类的其他方法和属性，就好像是在这个类自己的方法中访问一样。但是，拓展函数不允许打破封装性。不能访问私有或者受保护的方法。

## 导入和拓展函数
对于一个拓展函数，他不会自动的在整个项目范围内生效。相反，如果你要使用它，需要进行导入，就像其他任何的类或者函数一样。这是为了避免偶然的命名冲突。
```
import pkg2.lastChar
   "x".lastChar()
```
当然也可以使用*来导入
```
import pkg2.*
   "x".lastChar()
```
也可以使用关键字as来修改导入的类或者函数名称：
```
import pkg2.lastChar as last
 "x".last()
```
可以通过as解决重名的问题。

## 从java中调用拓展函数
实质上，拓展函数是静态函数，他吧调用对象作为他的第一个参数。调用拓展函数，不会创建适配的对象或者任何运行时的额外消耗。
这使得从java中调用kotlin的拓展函数变得简单：调用这个静态函数，然后把接收者对象作为第一个参数传递进去即可。和其他顶层函数一样，包含这个函数的java类的名称，是由这个函数声明的文件名称决定的。假设在JakeKotlin.kt这个文件中声明，那么使用JakeKotlinKt来调用
```
   char c= JakeKotlinKt.lastChar("xx");
```
可以发现，拓展函数的实质就是一个静态方法。kotlin把它变成了一种语法糖。

## 作为拓展函数的工具函数

现在可以为`joinToString（）`函数写一个最终版本了，他和你在kotlin标准库中看到的一模一样

```
@JvmOverloads
fun <T> Collection<T>.oinToString(separator:String=",",prefix:String="(",postfix:String=")"):String{

    val result=StringBuilder(prefix)
    for ((index,element) in withIndex()){
        if(index>0) result.append(separator)
        result.append(element)
    }
    result.append(postfix)
    return  result.toString()
}

```
函数调用：
```
fun main(args: Array<String>) {
    val set = hashSetOf<Int>(1, 7, 53)
    println(set.joinToString())
}
```

输出结果：
```
(1,53,7)
```
是不是看上去简洁了很多

如果使用其他类型调用，会提示报错

### 不可重写的拓展函数
在kotlin中，重写成员函数是很平常的意见事情。但是不能重写拓展函数。假设这里有里两个类，View和他的子类Button，然后Button重写了父类的`click`函数

```
open class  View{
    open fun click()= println("view clicked")
}
open class  Button:View(){
    override fun click()= println("Button clicked")
}
```
但是对于拓展函数来说并不是这样的。拓展函数并不是类的一部分。从java调用拓展函数可以看出，他是一个声明在类之外的一个静态方法。尽管可以给基类和子类都定义一个同样的拓展方法，当这个方法被调用时，他会调用哪一个呢?这里是由该变量的静态类型决定的，而不是这个变量的运行时类型。这里来一个栗子。

```
fun View.showOff()= println("我是View")
fun Button.showOff()= println("我是Button")

open class  View{
    open fun click()= println("view clicked")
}
open class  Button:View(){
    override fun click()= println("Button clicked")
}
```

来看一下调用：
```
fun main(args: Array<String>) {
    val view: View = Button()
    view.showOff()
     view.click()
}

```

输出结果：
```
我是View
Button clicked
```

可以发现，一个是拓展方法，一个是成员方法。两个的输出是不一致的。当调用一个类型为View的变量的showOff方法时，对应的拓展函数会被调用，尽管这个变量实际上是Button。想一下成员方法在java中的调用，你就明白了。

## 拓展属性
拓展属性提供了一种方法，用来拓展类的API，可以用来访问属性，用的是属性语法而不是函数语法。

```
val String.lastChar: Char
    get() = get(length - 1)
```
可以看到，和拓展方法类似，拓展属性也像接收者的一个普通成员属性一样。这里必须定义getter函数，因为没有支持字段，因此没有默认的getter实现，同理也不可以初始化。
如果在StringBuilder上定义一个相同的属性，可以置为var，因为StringBuilder的内容是可变的。
```
var StringBuilder.lastChar: Char
    get() = get(length - 1)
    set(value) = setCharAt(length - 1, value)
```

```
fun main(args: Array<String>) {
    println("kotlin".lastChar)
    val sb = StringBuilder("kotlin");
    println(sb.lastChar)
    sb.lastChar = '!'
    println(sb.lastChar)
    println(sb)
}

```

看一下输出结果
```
n
n
!
kotli!
```
在java中访问时，要使用getter和setter方法

```
  char c= JakeKotlinKt.getLastChar("xx");
```
# 处理集合：可变参数、中缀调用和库的支持