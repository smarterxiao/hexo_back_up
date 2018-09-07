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

在kotlin中接口可以包含抽象属性。这里有一个例子
```
interface  User{
    val nickName:String
}
```
这意味着实现User接口的类需要提供一个取得nickName值的方式。接口并没有说明这个值应该存储到一个支持字段还是通过getter来取。接口本身不包含任何状态。来一个例子

```

class PrivateUser(override val nickName: String) : User

class SubscribingUser(val email: String) : User {
    override val nickName: String
        get() = email.substringBefore('@')
}

class FacebookUser(val accountId: Int) : User {
    override val nickName: String = getFacebookName(accountId)

    private fun getFacebookName(accountId: Int): String {
        return ""
    }

}
```

对于PrivateUser来说，你使用了简洁的语法直接在主构造方法中声明了一个属性。这个属性来自于User的抽象属性，说以使用标记override
对于SubscribingUser来说，nickname属性通过一个自定义的getter实现。这个属性没有一个字段支持他来存储值，他只有一个getter在每次调用时从email中得到昵称。
对于FacebookUser来说，在初始化时讲nickname属性与值关联。你使用了被认可可以通过账号id获取facebook昵称的函数。这个函数开销巨大:他需要与faceBook建立链接来获取想要的数据。这也就是为什么只在初始化阶段调用一次的原因。
除了抽象属性外，接口还可以包含具有getter和setter的属性，只要他们没有引用一个一个支持字段(支持字段需要在接口中存储状态，这是不允许的)

```

interface Teacher {
    val email: String
    
    val name:String
    get() = email.substringBefore(email)
}


class Bob(override val email: String) :Teacher{
    
}
```

可以发现第一个抽象属性必须在子类中重写，第二个时可以被继承的


## 通过getter和setter方法支持字段

```
fun main(args: Array<String>) {
    val user=SuperUser("bob")
    user.address="Els 47,80678 Muenchen"
}

class SuperUser(val name: String){
    var address:String="unspecified"
    set(value:String) {
        println("""
            Address was changed for $name:"$field" ->"$value".""".trimIndent())
        field=value


    }
}
```

输出结果
```
Address was changed for bob:"unspecified" ->"Els 47,80678 Muenchen".
```
可以像平常一样通过`user.address=“new value”`来修饰一个属性的值，这其实在底层封装了setter。

## 修改访问器的可见性

访问器的可见性默认与属性的可见性相同。但是如果需要可以在get和set关键字前放置可见性修饰符的方式来修改它。


```


class LengthCounter {
    var counter: Int = 0
        private set//不能在类外部修改这个属性

    fun addWord(word: String) {
        counter += word.length
    }
}
```
调用
```
fun main(args: Array<String>) {
    val lengthCounter = LengthCounter()
    lengthCounter.addWord("hi")
    println(lengthCounter.counter)
}
```
输出:
```
2
```

# 编译器生成的方法：数据类和委托类

## 通用方法
和java一样kotlin也有通用类。举个栗子：`toString`,`equals`,`hashCode`。看一个例子

```
class Client(val name:String,val postalCode:Int)
```

重写`toString()`

```
class Client(val name: String,val postalCode: Int){
    override fun toString():String="Client(name=$name,postalCode=$postalCode)"
}


fun main(args: Array<String>) {
    val  client=Client("Alice",2345)
    println(client)
}
```

看一下输出
```
Client(name=Alice,postalCode=2345)
//对比一下默认输出，是不是好了很多
Client@6f94fa3e
```

对象相等性：`equals()`
先看一下默认输出
```
fun main(args: Array<String>) {
    val  client=Client("Alice",2345)
    val  client1=Client("Alice",2345)
    println(client==client1)
}

```

输出结果：
```
false
```
在kotlin中如果想进行引用比较，可以使用`===`来进行比较，这个与java中的`==`是等价的。

看一下重写`equal`后的代码
```
class Client(val name: String, val postalCode: Int) {
    override fun equals(other: Any?): Boolean {
        if (other == null || other !is Client) {
            return false
        }
        return name == other.name && postalCode == other.postalCode
    }
    override fun toString(): String = "Client(name=$name,postalCode=$postalCode)"
}

```

提醒你一下，kotlin中is检查是Java中instanceof的模拟，用来检查类型。
我们运行看一下
```
fun main(args: Array<String>) {
    val client = Client("Alice", 2345)
    val client1 = Client("Alice", 2345)
    println(client == client1)
}

```
输出结果
```
true

```

Hash容器：hashCode()
hashCode方法通常与equals一起被重写。
```
    val  processed= hashSetOf<Client>(Client("Alice",123456))
    println(processed.contains(Client("Alice",123456)))
```

输出结果
```
false
```

原因时Client类缺少了hashCode方法。因为违反了hashCode的契约。如果一个对象相同，那么应当具有相同的hash值。
现在来修复这个问题.看一下完全体的代码
```
class Client(val name: String, val postalCode: Int) {
    override fun equals(other: Any?): Boolean {
        if (other == null || other !is Client) {
            return false
        }
        return name == other.name && postalCode == other.postalCode
    }

    override fun hashCode(): Int =name.hashCode()*31+postalCode
    override fun toString(): String = "Client(name=$name,postalCode=$postalCode)"
}

```
再一次调用
```
    val  processed= hashSetOf<Client>(Client("Alice",123456))
    println(processed.contains(Client("Alice",123456)))
```
输出结果
```
true
```

## 数据类：自动生成通用方法的实现
在kotlin中，可以使用关键字data避免生成上面的三个方法，`equal`,`toString`,`hashCode`。
来测试一波
```
data  class Client(val name: String, val postalCode: Int) {

}

```

调用代码
```
fun main(args: Array<String>) {
    val client = Client("Alice", 2345)
    val client1 = Client("Alice", 2345)
    println(client == client1)


    val processed = hashSetOf<Client>(Client("Alice", 123456))
    println(processed.contains(Client("Alice", 123456)))
}

```

输出结果
```
true
true
```

## 数据类和不可变性：copy()方法
```
class Client(val name: String, val postalCode: Int) {
   fun copy(name: String = this.name, postalCode: Int = this.postalCode) = Client(name, postalCode)
}
```

调用
```
fun main(args: Array<String>) {

    val client = Client("Alice", 123456)
    println(client.copy(postalCode = 654321))

}
```

输出结果
```
Client(name=Alice,postalCode=654321)
```
可以发现复制成功了
当然系统也为我们提供了实现，和上面一样，用data修饰
```
data  class Client(val name: String, val postalCode: Int) {

}

```

调用
```

fun main(args: Array<String>) {

    val client = Client("Alice", 123456)
    println(client.copy(postalCode = 654321))

}

```

输出
```
Client(name=Alice, postalCode=654321)
```

## 类委托：使用by关键字
很多时候需要类进行拓展，这个时候在java中可以使用装饰者模式，看一下下面的例子
```
class  DelegatingCollection<T>:Collection<T>{
    private  val innerList= arrayListOf<T>()
    
    override val size: Int
        get() = innerList.size

    override fun contains(element: T): Boolean =innerList.contains(element)

    override fun containsAll(elements: Collection<T>): Boolean =innerList.containsAll(elements)
    override fun isEmpty(): Boolean =innerList.isEmpty()
    override fun iterator(): Iterator<T> =innerList.iterator()

}
```
kotlin提供了一个关键字替代我们实现这些样板代码
```
class  DelegatingCollection<T>(innerList:Collection<T> =ArrayList<T>()) :Collection<T> by innerList
```
当需要改变方法时，可以重写而不使用默认方法

来看一下栗子：

```

class CountingSet<T>(val innerSet:MutableCollection<T>):MutableCollection<T> by innerSet{
    var objectsAdd=0

    override fun add(element: T): Boolean {

        objectsAdd++
        return innerSet.add(element)
    }
    override fun addAll(c:Collection<T>):Boolean{
        objectsAdd++
        return innerSet.addAll(c)
    }
}




```

调用
```
fun main(args: Array<String>) {

val  cset=CountingSet<Int>(HashSet<Int>())
    cset.addAll(listOf(1,1,2))
    println("${cset.objectsAdd} objects were added, ${cset.size} remain")
}

```

结果:
```
1 objects were added, 2 remain
```

# object关键字
## 单例
通过object对象声明出来的对象是一个单例
```
object  CaseINsensitiveFileComparator:Comparator<File>{
    override fun compare(o1: File, o2: File): Int {
        return o1.path.compareTo(o2.path,ignoreCase = true)
    }
}
```
调用
```
fun main(args: Array<String>) {

println(CaseINsensitiveFileComparator.compare(File("/User"),File("/user")))
}
```

输出结果
```
0
```

同样可以在类中声明对象，这样的对象同样时一个单例
```

data class Person(val name:String){
    object NameComparator:Comparator<Person>{
        override fun compare(p1: Person, p2: Person): Int =p1.name.compareTo(p2.name)
    }
}

```
在这个栗子中，NameComparator是一个单栗

看一下调用
```
fun main(args: Array<String>) {
    val persons = listOf<Person>(Person("Bob"), Person("Alice"))
    println(persons.sortedWith(Person.NameComparator))

}
```
输出结果
```
[Person(name=Alice), Person(name=Bob)]
```

在java中可以这样调用kotlin的单例

```
    Object ob=CaseINsensitiveFileComparator.INSTANCE.compare(new File("user"),new File("User"));
```

## 半生对象

kotlin中没有静态成员类；Java的static关键字不是Kotlin语法的一部分。半生对象时他的替代部分

```
class A{
    companion object {
        fun  bar(){
            println("Companion object called")
        }
    }
}
```

调用
```

fun main(args: Array<String>) {
    A.bar()

}
```

结果
```
Companion object called
```

半生对象可以通过容器.xxx来调用
我们可以通过定义伴生对象来访问私有API，类似一个工厂模式
一个通用的类
```
class User {
    val nickName: String

    constructor(email: String) {
        nickName = email.substringBefore("@")
    }

    constructor(faceBookAccountId: Int) {
        nickName = getFaceBookName(faceBookAccountId)
    }

    private fun getFaceBookName(faceBookAccountId: Int): String = "Alice"
}
```
使用工厂方法替代构造函数
```
class User private constructor(val nickName:String){
  companion object {
      fun newSubcribingUser(email:String)=User(email.substringBefore("@"))
      fun newFaceBookUser(accountId:Int)=User(getFaceBookName(accountId))
      private fun getFaceBookName(faceBookAccountId: Int): String = "Alice"
  }
}
```
伴生对象时不能被子类重写的。要注意

## 作为普通对象使用的伴生对象

一个半生对象
```
class Person(val  name:String){
    companion object Loader {
        fun  fromJson(jsonText:String):Person{
            return Person(jsonText)
        }
    }
}
```


```
fun main(args: Array<String>) {
   var person=Person.Loader.fromJson("{name:  'Dmitry'}")
   println(person.name)
     person=   Person.Companion.fromJson("{name:  'Dmitry'}")
    println(person.name)
   person=Person.fromJson("{name:  'Dmitry'}")
   println(person.name)
}

```


输出
```
{name:  'Dmitry'}
{name:  'Dmitry'}
{name:  'Dmitry'}
```


在伴生对象中实现接口

```

class Person(val  name:String){
    companion object :JsonFactory<Person> {
        override fun  fromJson(jsonText:String):Person{
            return Person(jsonText)
        }
    }
}

interface JsonFactory<T> {
    fun fromJson(jsonText: String):T
}

```

调用
```
   val x: JsonFactory<Person> =Person
    val fromJson = x.fromJson("{name:  'Dmitry'}")
    println(fromJson.name)
```

输出
```
{name:  'Dmitry'}
```

可以吧Person的雷鸣当做JsonFactory的实例

伴生对象拓展



```

class Person(val  name:String){
    companion object :JsonFactory<Person> {
        override fun  fromJson(jsonText:String):Person{
            return Person(jsonText)
        }
    }
}



interface JsonFactory<T> {
    fun fromJson(jsonText: String):T
}
// 拓展对象
fun  Person.Companion.readName(){
    
}
```

## 对象表达式：改变写法的匿名内部类

```
window.addMouseListener(object:MouseAdapter(){
    override fun mouseClicked(e: MouseEvent?) {
        super.mouseClicked(e)
    }
})

```
可以把内部类存储到一个变量中
```
val listener=object :MouseAdapter(){

}

```

与java匿名内部类只能拓展一个类或实现一个接口不同，kotlin的匿名对象那个可以实现多个接口或不实现接口

如果访问局部变量可以不用final修饰变量

```

fun countClicks(window:Window){
    var clickCount=0
    window.addMouseListener(object:MouseAdapter()){
        override fun mouseClicked(e:MouseEvent){
            clickCount++
        }
    }
}

```

clickCount 可以不用final