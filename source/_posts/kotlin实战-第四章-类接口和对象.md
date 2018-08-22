---
title: kotlin实战-第四章-类接口和对象
date: 2018-08-20 21:12:43
tags:
- 基础
- kotlin
categories: android
---

这一章节主要是加深kotlin的类的理解
# 定义类集成结构
## kotlin中的接口
定义一个kotlin接口
```
interface  Clickable{
    fun click()
}
```

这里定义了一个拥有名为`click`抽象方法的接口。所有实现这个接口了抽象类都需要提供这个方法的实现
```
class Button:Clickable{
    override fun click()= println("I was clicked")
}
```

调用改类的`click`方法


```
fun main(args: Array<String>) {
Button().click()
}
```
输出结果
```
I was clicked
```

kotlin在类的后面使用: 冒号替代java中的extends和implements关键字。和java一样，一个类可以实现多个接口，但是只能继承一个类。和java一样对于重写方法使用override修饰符修饰。但是在kotlin中时强制使用override修饰的。

接口的方法可以有一个默认实现，与java8不同的是，java8中需要你在这样的实现上标注default关键字，对于这样的方法，kotlin没有特殊的注解。这里来实现一个带有方法体的接口
```

interface  Clickable{
    fun click()
    
    fun  showOff()= println("I'm clickable!")
}

```
如果实现这个接口，你需要为click提供一个实现。可以重新定义showOff方法的行为，或者如果你对默认行为感到满意也可以直接省略它。
现在我们来看一下实现同样方法的接口的另一种形式

```
interface Focusable {
    fun setFocus(b: Boolean) = println("I ${if (b) "get" else "lost"}")
    fun showOff()= println("I'm focusable!")
}
```
但是如果在一个类中同时继承者两个接口，会发生什么样呢？
```
Error:(20, 1) Kotlin: Class 'Button' must override public open fun showOff(): Unit defined in pkg2.Clickable because it inherits multiple interface methods of it
```
要求一定要实现这个接口，无论之前是否实现

```
class Button : Clickable, Focusable {
    override fun showOff() {
        super<Clickable>.showOff()
        super<Focusable>.showOff()
    }

    override fun click() = println("I was clicked")
}
```
现在Button类实现了两个接口。通过调用继承两个父类中的实现来实现`showOff`要调用一个继承的实现，可以通过Java相同的字段`super`，但是选择一个特定的实现语法是不同的
java中的做法
```
Clickable.super.showOff()
```
而kotlin的做法
```
  super<Clickable>.showOff()
```

我们来看一下调用
```
fun main(args: Array<String>) {
    Button().showOff()
    Button().setFocus(true)
    Button().click()
}

```
输出结果
```
I'm clickable!
I'm focusable!
I get
I was clicked
```

## open final和abstract修饰符：默认为final

java的类和方法默认是open的，kotlin默认都是final的，意味着不能继承和重写

如果想要一个类允许创建子类，需要使用open修饰符

```
open class RichButton:Clickable{
    fun disable(){}
    open fun animate(){}
    override fun click() {}
} 
```

注意，如果重写了一个基类或者接口的成员，重写了的成员同样默认是open的。如果想改变这一行为，需要显示的将类成员标记为final
```
open class RichButton:Clickable{
   final override fun click() {}
}
```

在kotlin中，同java一样，可以将一个类声明为`abstract`的，这种类不能被实例化。一个抽象类通常包含一些没有实现并且必须在子类被重写的抽象方法。抽象方法始终是open的，所以不需要显示的使用open修饰符。接下来是示例

```
abstract class animated {
    abstract fun animate()
    open fun stopAnimating() {

    }

    fun animateTwice() {
        
    }
}
```

下面列出了kotlin中的访问修饰符。表中的评注适用于类中的修饰符；在接口中，不能使用final、open或者abstract。接口中的成员始终是open的。不能将其声明为final。如果他没有函数体，他就是abstract的。但是这个关键字不是必须的

|修饰符|相关成员|评注|
|:---:|:---:|:---:|
|final|不能被重写|类中成员默认使用|
|open|可以被重写|需要明确的表明|
|abstract|必须被重写|只能在抽象类中使用，抽象成员不能有实现|
|override|重写父类的节后或者成员|如果没有使用final表明，重写的成员默认是开放的|

## 可见性修饰符：默认为public
总体来说，kotlin中的修饰符和java中类似。同样可以使用public，protected和private修饰符。但是默认的可见性是不一样的：如果省略了修饰符，声明就是public的。
java中默认的可见性是包私有，在kotlin中没有使用。kotlin只是把包作为在命名空间里组织代码的一种形式使用，没有将其作为可见性控制。
作为替代方案，kotlin提供了新的关键字 internal表示在模块内部可见。一个模块就是一组一起编译的kotlin文件。这里可能是一个Intellij Idea模块、一个eclipse项目...

|修饰符|类成员|顶层声明|
|:---:|:---:|:---:|
|public(默认)|所有地方可见|所有地方可见|
|internal|模块中可见|模块中可见|
|protected|子类中可见|-|
|private|类中可见|文件中可见|

来看一个错误的demo,这个例子在违反上面的规则

```
internal  open class  TalkativeButton:Focusable{
    private  fun  yell()=println("Hey!")
    protected  fun whisper()= println("let's talk!")
}


fun TalkativeButton.giveSpeach(){//Error:(66, 5) Kotlin: 'public' member exposes its 'internal' receiver type TalkativeButton
    yell()//Error:(67, 5) Kotlin: Cannot access 'yell': it is private in 'TalkativeButton'
    whisper()//Error:(68, 5) Kotlin: Cannot access 'whisper': it is protected in 'TalkativeButton'
}
```

注意：
类的拓展函数不能访问private和protected成员
java中，可以从同一个包访问一个protected成员，但是kotlin不允许这么做。
kotlin和java中的protect修饰符行为不同。
尽量避免使用internal类

另一个kotlin与java之间的可见性规则的齐布尔是kotlin中一个外部类不能查看其内部类的private成员

## 内部类和嵌套类：默认是嵌套类

在kotlin中kotlin的嵌套类是不能访问外部类的事例的。除非你做了特殊的要求。

```
interface State : Serializable
interface View {
    fun getCurrentState(): State
    fun restoreState(state: State) {
    }
}
```
可以方便的定义一个保存按钮状态的Button类。java中的做法

```
public class Buttonx implements  View{

    @NotNull
    @Override
    public State getCurrentState() {
        return new ButtonState();
    }

    @Override
    public void restoreState(@NotNull State state) {
        
    }
    
    
    public  class ButtonState implements  State{
        
    }
}
```

但是在java中，ButtonState会隐式持有外部类的引用。需要使用static来修饰内部类，将隐式引用移除。

```
public class Buttonx implements  View{

    @NotNull
    @Override
    public State getCurrentState() {
        return new ButtonState();
    }

    @Override
    public void restoreState(@NotNull State state) {
        
    }
    
    
    public static class ButtonState implements  State{
        
    }
}
```



看一下kotlin中的例子

```
class Button : View {
    override fun getCurrentState(): State = ButtonState();
    override  fun restoreState(state: State) {
        super.restoreState(state)
    }
    class ButtonState : State {}
}
```

kotlin中没有显示修复hi福的嵌套类与java中的static嵌套类是一样的。要把它变成一个内部类持有一个外部类的引用的话需要使用inner修饰符。


|类A在另一个类B中声明|java中|顶kotlin中声明|
|:---:|:---:|:---:|
|嵌套类|static class A|class A|
|内部类|class A|inner class A|

在kotlin中引用外部类的语法也和java不同

```
class Outer{
    inner class  Inner{
        fun  getOuterRefrence():Outer=this@Outer
    }
}
```

### 密封类：定义受限的类继承结构

回想一下之前的一个例子

```

interface  Expr
class Num(val value:Int):Expr
class Sum(val left:Expr,val right:Expr):Expr

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
当使用when结构来执行表达式的时候，kotlin编译器会强制检查默认选项，在这个例子中不能返回一个有意义的值，所以直接抛出一个异常。
总是不得不添加一个默认分支是很不方便的。更重要的是如果添加一个新的子类，编译器并不能发现有地方改变了。如果你忘记了添加一个新分支，就会选择默认的选项，这有可能导致潜在的bug。
kotlin对这个问题提供了解决方案：sealed类。为父类添加一个sealed修饰符，对可能创建的子类做出严格的限制。

```
sealed class Expr {
    class Num(val Value: Int) : Expr()
    class Sum(val left: Expr, val right: Expr) : Expr()
}

fun  eval(e:Expr):Int=
        when(e){
            is Expr.Num->e.Value
            is Expr.Sum-> eval(e.right)+eval(e.left)
        }
```
如果在when表达式中处理所有的sealed类的子类，你就不需要提供默认分支。注意sealed修饰符隐含的这个类是一个open类，不需要显示的添加open修饰符。密封类的不能拥有外部子类
当你在when中使用sealed类并且添加一个新的子类的时候，有返回值的when表达式会导致编译失败，他会告诉你哪里的代码必须修改。
不能声明一个sealed接口。因为不能保证任何人不能再java代码中实现这个接口


# 声明一个带非默认构造方法的属性的类
## 初始化类：主构造方法和初始化语句块
声明一个简单的类
```
class User(val nickName:String)
```
不省略的版本：

```
class User constructor(val _nickName:String){
    val nickName:String
    init{
        nickName=_nickName
    }
}
```
init用来引入一个初始化语句块
如果主构造方法没有注解或者可见性修饰符，可以去掉`constructor`

```
class User(_nickName:String){
    val nickName=_nickName
}
```

可以像函数一样给构造方法声明一个默认值

```
class User(_nickName:String,val isSubscribed:Boolean=true)
```
看一下如何使用这个类

```
fun main(args: Array<String>) {
    val alice = User("alice")
    println(alice.isSubscribed)
    val bob = User("bob", false)
    println(bob.isSubscribed)
    val carol = User("carol", isSubscribed = false)
    println(carol.isSubscribed)
}


```

输出结果
```
true
false
false
```

如果类有一个父类，需要初始化父类。

```
open class User(_nickName: String, val isSubscribed: Boolean = true)
class TwitterUser(nickName:String):User(nickName)
```
由于子类只有一个构造参数，所以只能调用单个参数的构造

```
 val alice = TwitterUser("alice")
    println(alice.isSubscribed)
```

如果继承一个无参构造

```
open class Button
class RadioButton:Button()
```
需要显示调用父类的构造方法，即使没有任何参数
如果想要确保类不被其他地方的代码初始化，必须把构造方法标记为private
```
class Secretive private constructor(){}
```
一般会使用静态方法来访问这个类的示例，提供单例。


## 构造方法：用不同的方式来初始化父类
通常来讲，使用多个构造方法的类在kotlin中比java中的要少。
```
open class View{
    constructor(ctx:String)
    constructor(ctx:String,attr:Int)
}
```
这个类没有声明一个主构造方法
看一下他的子类
```
class MyButton:View{
    constructor(ctx:String):super(ctx){
        
    }
    constructor(ctx:String,attr:Int):super(ctx,attr){
        
    }
}
```

也可以使用this关键字
```
class MyButton:View{
    constructor(ctx:String):this(ctx,2){

    }
    constructor(ctx:String,attr:Int):super(ctx,attr){

    }
}
```
如果累没有主构造方法，那么每一个从构造方法必须初始化基类或者委托给另一个构造方法。

## 实现在接口中声明的属性