---
title: kotlin实战_第一章_kotlin基础
date: 2018-08-06 21:38:51
tags:
- 基础
- kotlin
categories: android
---
#  基本要素：函数和变量
## Hello,World!
我们从一个最经典的例子Hello,World讲解一下kotlin的用法，这个功能在kotlin中只需要一个函数就能够实现

```
fun main(args: Array<String>) {
    println("Hello,world!")
}
```
可以从这样简单的代码中看到一些kotlin的特性

* 关键词fun用来声明一个函数，没错，kotlin编程有很多乐趣（fun）
* 参数的类型卸载它的名称后面，稍后就可以看到，变量的声明也是这样的
* 函数可以定义在文件的最外层，不需要把他放在类中
* 数组就是类。和Java不同，Kotlin没有声明数组类型的特殊语法
* 使用println替代了System.out.println。kotlin标准库给java标准库提供了很多语法简单的包装，而println就是其中一个
* 和许多其他现代化语言一样，可以省略每行代码结尾的分号

##  函数
你已经可以看到怎么样声明一个没有返回任何东西的函数，但是如果函数有一个有意义的结果，返回类型应该放在那里呢，你可能会猜到他对应位于参数列表之后的某处：

```
fun main(args: Array<String>) {
    println(max(1,2))
}
fun  max(a:Int,b:Int):Int{
    return if (a>b)a else b
}
```

看一下输出结果
```
2
```


函数的声明以fun关键字开始，函数名称紧随其后：这个例子中函数名称是max，接下来是括号起来的参数列表，参数列表的后面跟着返回类型，他们之间用一个冒号隔开


```
//fun  函数名称(参数列表):返回值类型
fun  max(a:Int,b:Int):Int{
    return if (a>b)a else b//函数体
}
```
语句和表达式：
在kotlin中，if是表达式，而不是语句。语句和表达式的区别在于，表达式有值，并能作为另外一个表达式的一部分使用；而语句总是包围着它的代码中顶层元素，并且没有自己的值。在java中，所有的控制结构都是语句。而在kotlin中，除了循环（for、do和do/while）以外大多数的控制结构都是表达式。这种结合控制结构和其他表达式的能力能让你可以简明扼要的表示许多常见的模式，稍后你会在本书中看到这些内容。
另一方面，java中的复制操作是表达式，在kotlin中反而变成了语句。这有助于避免比较和赋值之间的混淆，而这种混淆是常见的错误来源。

表达式函数体
可以让前面的函数变得更简单。因为他的函数体是由单个表达式构成的，可以用这个表达式作为完整的函数体，并去掉花括号和return语句：
```
fun  max(a:Int,b:Int):Int = if (a>b)a else b
```

如果函数体写在花括号中，我们说这个函数有代码块体。如果他直接返回一个表达式，它就有表达式体
在kotlin代码中常常会看到表达式个体函数，这种风格不光用在一些简单的单行函数中，也会用在对更加复杂的单个表达式就只的函数中，比如if，when，try。这个可以在后面看到
当然还可以对max函数进一步简化

```
fun  max(a:Int,b:Int) = if (a>b)a else b

```
为什么有些函数可以不声明返回类型？作为一门静态类型语言，kotlin不是要求每一个表达式都应该在编译器有返回类型吗？事实上，每一个变量和表达式都有类型，每一个函数都有返回类型。但是作为对表达式体函数来说，编译器会分析作为函数体的表达式，并把他的类型作为函数的返回类型，即使没有显示的写出来。这就是 *类型推导*
kotlin中只有表达式体函数的返回类型可以省略，队友有返回值的代码块体函数，必须显示地写出返回类型和return语句。这个是可以的选择。真实项目中的函数一般很长且可以包含多个return语句，显示地写出返回类型和return语句能帮助你快速的理解函数能返回什么。

##  变量

在java中声明变量的时候回从类型开始，在kotlin中这样是不行的，应为许多变量声明的类型都是可以省略。所以在kotlin中以关键字开始，然后是变量名称，最后可以加上类型（不加也可以）
```
val question="红烧鸡翅膀，我最喜欢吃"
val answer=250
```
这个例子省略了类型声明，但是如果需要也可以显示的指明变量的类型：
```
val question:String="红烧鸡翅膀，我最喜欢吃"
```
和表达式体函数一样，如果不指定类型，编译器会分析初始化器表达式的值，并把它的类型作为变量的类型，在前面这个例子中，变量的初始化器是Int类型，那么变量就是这个类型
如果你使用浮点数常量，那么变量就是double类型：
···
val yearsToCompute=3.4e6
···
后面更加深入的介绍算数类型
如果变量没有初始化器，就需要显示的指定他的类型

```
    val answer:Int
    answer=100
```
但是这个只能在方法内部有效
如果不能提供付给这个变量值得信息，编译器就无法推断出它的类型
可变变量和不可变变量
* val(value)不可变，对应java的final
* var(variable)可变

默然情况下，应该尽可能的使用val关键词来修饰kotlin变量，仅在必要的时候换成var，使用不可变引用、不可变对象以及无副作用函数让代码更接近函数式编程风格。
在定义了val变量的代码执行期间，val只能进行唯一一次初始化。但是，如果编译器能确保只有唯一一条初始化语句会被执行，可以根据条件使用不同的值来初始化它

```

val message:String
if(true){
    message="Success"
}else{
    message="fail"
}

```

注意，尽管val引用自身是不可变的，但是他指向的对象是可变的，例如

```
val language= arrayListOf("java")
language.add("kotlin")
```


即使var关键字允许改变自己的值，但是他的类型确实改变不了的，例如

```
var answer=43
answer="no answer"
```
使用字符串字面值会发生错误，因为他的类型是String不是期望的类型(Int)，编译器只会根据初始化器来推断变量的类型，在决定类型的时候不会考虑后续复赋值操作。
如果需要在变量中存储不匹配类型的值，必须手动把值转换或强转到正确的类型。后面会讨论基本数据类型之间的转换
##  更简单的字符串格式化：字符串模板
我们回到本章开始的`hello world`例子。下面展示了如何完成这个经典案例的下一步，给别人打招呼

```
fun main(args:Array<String>){
    val name=if(args.sise>0) args[0] else "kotlin"
    println("Hello,$name!")
}
```
这个例子介绍了一个新特性叫做字符串模板。在代码中，你声明了一个变量name，在后面使用它。和许多脚本语言一样，kotlin让你可以在字符串字面值中引用局部变量，只需要在变量名称前加上符号$，它等价于`"hello,"+name+"!"`,效率是一样的，只是一个语法糖。如果需要打印`$`，这个时候需要转义符`println("\$x")`,会输出`$x`。

这里还有更加复杂的表达形式，不仅局限于简单的变量名称，只需要把表达式用花括号括起来：

```
fun main(args: Array<String>) {
    if(args.size>0){
        println("Hello,${args[0]}")
    }
}
```

还可以在双引号中直接嵌套双引号，只要他们处在某个表达式的范围内(花括号内)
```
fun main(args: Array<String>) {
    println("Hello,${if (args.size > 0) args[0] else "someone"}")
}
```
输出结果
```
Hello,someone
```

# 类和属性
## 类
kotlin优化了java面向对象的内容，可以用更少的代码实现和java相同的内容

```
public class Person {
    private   String name;

    public Person setName(String name) {
        this.name = name;
        return this;
    }

    public Person(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}


```
这是java经典的代码，类的代码。在java中，构造方法的方法体常常包含完全重复的代码：把参数赋值给有着相同名字的字段。在Kotlin中可以不用这么多样板代码来实现。
这里我们使用kotlin的工具，将上面的java代码转化为kotlin，看一下代码的样子。

```
class Person(private var name: String?) {

    fun setName(name: String): Person {
        this.name = name
        return this
    }

    fun getName(): String? {
        return name
    }
}

```
可以发现简短了很多，并且方法前面没有public，在kotlin中public是默认的可见性，可以省略。

## 属性
在java中，字段和其访问器的组合常常被称为属性，而许多框架严重依赖这个概念。在Kotlin中，属性是头等的语言特性，完全替代了字段和访问器方法。在类中声明一个属性和声明一个变量是一样的：使用val和var关键字。

```
class People {
    val name:String="hello"
    var isMarried:Boolean=false
}
```
这个是kotlin一个简单的类，但是如果不给他付初始值，那么编译器就会报错。
下面在java中调用这个类的属性
```
    public static void main(String[] args) {
        People people = new People();
        System.out.println(people.getName());
        System.out.println(people.isMarried());

         people.setMarried(true);
    }
```
得到如下结果
```
hello
false

```
可以发现如果使用kotlin定义一个类，他会自动生成getter和setter方法。但是有一个细节要注意，如果是以is开头的属性，getter是不会加get前缀的。setter会被正常替换为setxxx。所以在java方法中调用的是`isMarried()`。

如果将上面的代码转换成kotlin代码，将会得到如下的结果
```
@JvmStatic
    fun main(args: Array<String>) {
        val people = People()
        println(people.name)
        println(people.isMarried)

        people.isMarried = true
    }
```

可以发现在kotlin中可以直接引用属性，不在需要调用getter方法。逻辑没有变化，但是代码更加简洁了。setter属性同样如此：在java中`people.setMarried(true)`表示结婚，在kotlin中`people.isMarried = true`也表示结婚。
这里有一个小细节注意一下。如果在java中定义了getter和setter方法，那么在kotlin中可以直接通过属性访问。

```
public class People1 {
    private  String name;
   private boolean isMarried;

    public String getName() {
        return name;
    }

    public People1 setName(String name) {
        this.name = name;
        return this;
    }

    public boolean isMarried() {
        return isMarried;
    }

    public People1 setMarried(boolean married) {
        isMarried = married;
        return this;
    }
}

```
可以看到People1定义了两个private变量，并且定义了getter和setter方法。
```
fun main(args: Array<String>) {
  val  perple =  People1()
    perple.isMarried=true
    println(perple.isMarried)
}
```

可以发现在kotlin中可以正常访问这个私有属性...

## 自定义访问器
这一节将展示怎样写一个属性访问器的自定义实现。假设现在有一个需求，声明一个矩形，让他自己判断自己是否是正方形。不需要一个单独的字段来存储这个信息(是否是正方形)，因为可以通过检查矩形的长宽是否相等来判断。

```
class Rectangle(val height:Int,val width:Int) {
    val isSquare:Boolean
    get() {
        return  height==width;
    }
}
```
属性isSquare不需要字段来保存他的值。他只有一个自定义的getter实现。他的值都是每次访问属性的时候计算出来的。

```
fun main(args: Array<String>) {
val  rectangle=Rectangle(100,100)
    println(rectangle.isSquare)
}
```
输出结果
```
true
```
如果要在java中访问这个属性，可以向前面提到的，通过`isSquare()`方法访问
```
    public static void main(String[] args) {
     Rectangle rectangle=new Rectangle(100,100);
        System.out.println(rectangle.isSquare());
    }
```

后面还会看到更多的使用类和属性的例子，这里就先到这里。
##kotlin源码布局：目录和包
java把所有的类组织成包，kotlin也有和java类似的包的概念。每一个kotlin都可以以一个package语句开头，而文件中定义的所有声明(类，函数以及属性)都会被放到这个包中。如果其他文件中定义的声明也包含相同的包，这个文件可以直接使用它们；如果不相同，则需要导入它们。和java一样，使用import关键字。

```
package pkg2

import java.util.*
class Rectangle(val height:Int,val width:Int) {
    val isSquare:Boolean
        get() {
            return  height==width;
        }
}

fun  createRandomRectange():Rectangle{
    val random=Random();
    return  Rectangle(random.nextInt(),random.nextInt())
}
```
kotlin不区分导入的是类还是函数，而且，他允许使用import关键字导入任何类的声明。在kotlin中函数和类都是顶级的元素。而不是java中函数必须存在类中。
```
import pkg2.createRandomRectange

fun main(args: Array<String>) {
    println(createRandomRectange().isSquare)
}
```
这个例子就是直接导入了函数。
也可以在包名后面加上`.*`来导入所有的声明。
java中，要把类放到和包结构相匹配的文件与目录中。例如，如果你有一个包含若干类的名为shapes的包，必须把它放在有着类名字相同的包中。kotlin可以把多个类放置在同一个文件中，文件的名字还可以任意选择。kotlin没有对磁盘上的布局强加任何限制。

# 表示和处理选择：枚举和when
## 声明一个枚举类

一个简单的枚举类
```
enum class Color {
    RED,ORANGE,YELLOW,GREEN,BLUE,INDIGO,VIOLET
}
```
在kotlin中，声明枚举会比java多使用关键字，java只需要一个`enum`，Kotlin需要`enum class`。和java一样，枚举并不是值得列表，可以给枚举类声明属性和方法。下面的代码清单展示了这种方式

```
enum class Color (val r:Int,val g:Int,val  b:Int){
    RED(255,0,0),ORANGE(255,165,0),YELLOW(255,255,0),GREEN(0,255,0),BLUE(0,0,255),INDIGO(75,0,130),VIOLET(238,130,238);

    fun rgb()=(r*256+g)*256+b
}
```

看一下调用
```
fun main(args: Array<String>) {
println(Color.BLUE.rgb())
}
```
输出结果
```
255
```

如果需要在枚举中定义方法，就需要用`;`将枚举常量列表和方法定义分开。

## 使用when处理枚举类

这里来看一下如何使用when来处理枚举。when是一个带有返回值的表达式。

```
fun getMnemonic(color: Color) {
    when (color) {
        Color.BLUE -> "Richard"
        Color.ORANGE -> "Of"
        Color.YELLOW -> "york"
        Color.GREEN -> "Gave"
        Color.INDIGO -> "In"
        Color.VIOLET -> "Vain"
    }
}

fun main(args: Array<String>) {
    println(getMnemonic(Color.VIOLET))
}

```
输出结果
```
kotlin.Unit
```

上面的代码根据传递进来的color值找到对应的分支。和java不一样，不需要在每一个分支上面写上break语句
下面看一下如何合并分支
```
fun getMnemonic(color: Color) {
    when (color) {
        Color.RED, Color.ORANGE, Color.YELLOW -> "Warm"
        Color.GREEN -> "Green"
        Color.BLUE, Color.INDIGO, Color.VIOLET -> "Cool"
    }
}

```
只需要在后面用逗号隔开。

## 在when结构中使用任意对象
kotlin中的when结构比java中的switch强大很多。switch必须要使用常量（枚举常量，字符串常量或者字面值常量）作为分支条件，但是kotlin允许使用任何对象。看一下这个例子

```
fun mix(c1: Color, c2: Color) {
    when (setOf(c1, c2)) {
        setOf(Color.RED, Color.YELLOW) -> Color.ORANGE
        setOf(Color.YELLOW, Color.BLUE) -> Color.GREEN
        setOf(Color.BLUE, Color.VIOLET) -> Color.INDIGO
        else -> throw Exception("Dirty color")
    }
}

```
kotlin标准函数库中有一个setOf函数可以创建出一个Set，他会包含所有指定为函数实参的对象。set这种集合的条目顺序并不重要，只要两个set中包含同样的条目就相等。如果`setOf(c1,c2)`和` setOf(Color.BLUE, Color.VIOLET)`就表明c1等于Color.BLUE，c2等于Color.VIOLET,反过来也是相等的。但是这样的写法会创建很多垃圾。可以使用下面的写法进行优化

```
fun mixOptimized(c1: Color, c2: Color) =
        when {
            (c1 == Color.RED && c2 == Color.YELLOW || c2 == Color.RED && c1 == Color.YELLOW) -> Color.ORANGE
            (c1 == Color.YELLOW && c2 == Color.BLUE || c2 == Color.YELLOW && c1 == Color.BLUE) -> Color.ORANGE
            (c1 == Color.BLUE && c2 == Color.VIOLET || c2 == Color.BLUE && c1 == Color.VIOLET) -> Color.ORANGE
            else -> throw Exception("Dirty color")
        }

```
这种写法的优点是不创建额外的对象。但是更难理解。

## 只能转换：合并类型检查和转换
这里我们来写一个简单的函数`(1+2)+4`。

```
interface  Expr
class Num(val value:Int):Expr
class Sum(val left:Expr,val right:Expr):Expr
```

Sum存储了Expr类型的实参left和right的引用：在这个小例子中，他们要么是Num，要么是Sum。为了存储前面提到的表达式，这里会创建这样一个对象`Sum(Sum(Num(1),Num(2)),Num(4))`
看一下树状图
![Alt text](shuxingtu.png   "树形图")


看一下输出
```
println(eval(Sum(Sum(Num(1),Num(2)),Num(4))))
```
输出的结果应该是7

看一下求值方法
```
fun  eval(e:Expr):Int{
    if(e is Num){
        val n=e as Num
        return  n.value
    }
    if(e is Sum){

        return  eval(e.right)+ eval(e.left)
    }
    throw IllegalArgumentException("Unknown expression")
}

```


在Kotlin中，你要使用is检查来判断一个变量是否是某种类型。is和java中的InstanceOf相似。但是java中，如果你已经检查过一个变量是某种类型，在访问的时候还是要强转的，kotlin编译器帮我们完成了这个工作，这种方式在kotlin中被称为智能转换。使用as可以进行显示类型转换`val n=e as Num`。


## 重构：使用when替代if
kotlin 是没有三元运算符的`if(a>b)?a:b`，因为在kotlin中if表达式有返回值，这一点和java不同。但是if有返回值，就意味着上面的方法可以使用这种写法简写
```
fun eval(e: Expr): Int =
        if (e is Num) {
            val n = e as Num
            n.value
        } else if (e is Sum) {
            eval(e.right) + eval(e.left)
        } else {
            throw IllegalArgumentException("Unknown expression")
        }

```
这样if就就变成了一个表达式。
现在使用when来改造`eval`的方法
```

fun eval(e: Expr): Int =
        when (e) {
            is Num -> {
                e.value
            }
            is Sum -> {
                eval(e.right) + eval(e.left)
            }
            else -> {
                throw IllegalArgumentException("Unknown expression")
            }
        }
```
规则是代码块的最后表达式就是表达式的结果。
# 迭代事物：while循环和for循环
## while循环
kotlin的while和java的没有什么区别。这里就跳过
## 迭代数字：区间和数列
kotlin中没有常规的java的for循环。kotlin使用了区间的概念
```
    val oneToTen=1..10
```

一个起始值，一个结束值。中间使用`..`连接
看一个例子
```
fun fizzBuzz(i: Int) = when {
    i % 15 == 0 -> "FizzBuzz"
    i % 3 == 0 -> "Fizz"
    i % 5 == 0 -> "Buzz"
    else -> "$i "
}

for (i in 1..100){
       println(fizzBuzz(i)) 
    }

```

接着可以试一下将规则变得复杂一点，从100开始倒着数并且只计算偶数
```
 for (i in 100 downTo 1 step 2) {
        println(fizzBuzz(i))
    }
```
downTo是递减，setp是步长
后面会发现更多关于区间的用法

## 迭代map
```
 val binaryReps = TreeMap<Char, String>()
    for (c in 'A'..'F') {
        val binary = Integer.toBinaryString(c.toInt())
        binaryReps[c] = binary
    }

    for ((letter, binary) in binaryReps) {
        println("$letter =$binary")
    }
```
可以发现for可以迭代map中的集合元素`binaryReps[c] = binary`等价于`binaryReps。put(c,binary)`

输出结果:
```
A =1000001
B =1000010
C =1000011
D =1000100
E =1000101
F =1000110
```

```
    val list = arrayListOf<String>("10", "11", "1001")
    for ((index, element) in list.withIndex()) {
        println("$index:$element")
    }
```
这段代码打印出了你期待的内容:
```
0:10
1:11
2:1001
```
## 使用"in"检查集合和区间的成员
使用in运算符来检查一个值是否在区间中，或者他的逆运算，!n,来检查这个值是否不再区间中。下面展示了如何使用in来检查一个字符是否属于一个字符区间。


```
fun isLetter(c: Char) = c in 'a'..'z' || c in 'A'..'Z'
fun isNotDigit(c: Char) = c !in '0'..'9'

fun main(args: Array<String>) {
    println(isLetter('q'))
    println(isNotDigit('x'))
}
```

输出结果：
```
true
true
```

`c in 'a'..'z'`看起来简单，但是到了底层就会被转换成`a<=c&&c<=z` in运算符和!in也适用于when表达式。
```
fun  recognize(c:Char)=when (c){
    in '0'..'9'->"it's a digit!"
    in 'a'..'z'->"it's a letter!"
    else->"I don't know..."
}

fun main(args: Array<String>) {
println(recognize('8'))
}
```

输出结果是:
```
it's a digit!
```

他可以支持任意比较类(实现了java.lang.Comparable接口)，这个和java中比较list时需要适用比较器一样。底层适用的是Comparable 。

# kotlin中的异常
```
fun kotlinError(precentage: Int) {

    if (precentage !in 0..100) {
        throw  IllegalArgumentException("A precentage value must be between 0 and 100 $precentage")
    }
}
```
和java不同的是kotlin中throw结构是一个表达式，能作为另一个表达式的一部分使用:
```
    val precentage =
            if (number in 0..100)
                number
            else
                throw  IllegalArgumentException("A precentage value must be between 0 and 100 $number")
```

## try catch finally
```

fun  readNumber(reader:BufferedReader):Int?{
    try {
        val line=reader.readLine()
        return Integer.parseInt(line)
    }catch (e:NumberFormatException){
        return  null
    }finally {
        reader.close()
    }
}


fun main(args: Array<String>) {

   val test= BufferedReader(StringReader("12343"))
    println(readNumber(test))
}

```

## try作为表达式
```
fun readNumber1(reader: BufferedReader) {
    val number = try {
        val line = reader.readLine()
        Integer.parseInt(line)
    } catch (e: NumberFormatException) {
        return 
    }
    println(number)
}
fun main(args: Array<String>) {
    val test = BufferedReader(StringReader("fff"))
    readNumber1(test)
}



```
没有输出结果


在kotlin中，try关键字和when一样引入了一个表达式，可以赋值给变量

这个例子是将return放在cache代码中，因此该函数的执行在catch代码后就不会执行。如果想继续执行，catch子语句也需要一个返回值，他将是子语句中最后一个表达式
```
fun  readNumber1(reader: BufferedReader){
    val  number=try {
        val line=reader.readLine()
         Integer.parseInt(line)
    }catch (e:NumberFormatException){
         null
    }
    println(number)
}

fun main(args: Array<String>) {

   val test= BufferedReader(StringReader("fff"))
   readNumber1(test)
}

```

输出结果为：
```
null

```

