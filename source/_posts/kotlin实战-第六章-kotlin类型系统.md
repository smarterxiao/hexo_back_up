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
但是使用if关键字会使得代码变得很长。kotlin对此进行了优化

## 安全调用运算符：“?.”
kotlin的弹药库中最有效的安全工具就是`?.`，它允许把一次检查null和一次方法的调用合并成一个操作
```
    s?.toUpperCase()
    if (s != null) s.toUpperCase() else null
```
这两个是等价的。
但是使用`?.`的返回值可能时null值。
举个例子
```
fun main(args: Array<String>) {
    printAllCaps(null)
    printAllCaps("Alice")
}
fun  printAllCaps(s:String?){
    val allCasps:String?=s?.toUpperCase()
    println(allCasps)
}

```
看一下输出结果
```
null
ALICE

```
再举一个例子
```
fun managerName(employee: Employee): String? = employee.manager?.name

class Employee(val name: String, val manager: Employee?)

fun main(args: Array<String>) {
    val ceo = Employee("Da Boss", null)
    val developer = Employee("Bob Smith", ceo)
    println(managerName(developer))
    println(managerName(ceo))
}
```

输出结果
```
Da Boss
null
```

多个`?.`一起使用
```

class Address(val streatAddress: String, val zipCode: Int, val city: String, val country: String)

class Company(val name: String, val address: Address?)
class Person(val name: String, val company: Company?)

fun Person.countryName(): String {
    val country = this.company?.address?.country
    return if (country != null) country else "Unknown"
}

fun main(args: Array<String>) {
    val person = Person("Dmitry", null)
    println(person.countryName())
}

```
输出结果:
```
Unknown
```
## Elvis运算符：“?:”

kotlin有方便的运算符来提供替代null的默认值。它被称为Elvis运算符(或者null合并运算符)

举个例子
```
fun  foo(s:String?){
    val t:String=s?:""
}
```

Elvis运算符经常和安全调用运算符一起使用，用一个值替代对null对象调用方法时返回的null值。

```
fun strLenSafe(s:String?):Int=if(s!=null)s.length else 0
fun strLenSafe(s: String?): Int = s?.length?:0
```
对比一下这两种写法，可以发现第二种方法会精简很多。

另外一个对比，person的写法
```
fun Person.countryName(): String {
    val country = this.company?.address?.country
    return  country ?: "Unknown"
}

fun Person.countryName(): String {
    val country = this.company?.address?.country
    return if (country != null) country else "Unknown"
}
```

同时使用Elvis运算符和throw

```


fun main(args: Array<String>) {
    val person1 = Person("Dmitry", Company("ad钙奶",Address("北京",123456,"东京","china")))
    printShippingLabel(person1)
    val person = Person("Dmitry", null)
    printShippingLabel(person)
}

class Address(val streatAddress: String, val zipCode: Int, val city: String, val country: String)
class Company(val name: String, val address: Address?)
class Person(val name: String, val company: Company?)
fun  printShippingLabel(person: Person){
    val address=person.company?.address?:throw IllegalArgumentException("NO Address")
    with(address){
        println(streatAddress)
        println("$zipCode $city,$country")
    }
}
```

输出结果
```
北京
123456 东京,china

Exception in thread "main" java.lang.IllegalArgumentException: NO Address
	at TestKt.printShippingLabel(Test.kt:42)
	at TestKt.main(Test.kt:11)
```
## 安全转换：“as?”

as?尝试把值转换为指定类型，如果不合适就返回null，这样就可以使用Elvis了
```
class  Person(val firstName:String,val lastName:String){
    override fun equals(other: Any?): Boolean {
        val otherPerson=other as?Person?:return false

        return otherPerson.firstName==firstName&&otherPerson.lastName==lastName
    }
    override fun hashCode(): Int=firstName.hashCode()*37+lastName.hashCode()
}

fun main(args: Array<String>) {
    val p1=Person("Dmitry","Jemerov")
    val p2=Person("Dmitry","Jemerov")
    println(p1==p2)
    println(p1.equals(p2))
    println(p1.equals(42))
}
```
输出结果
```
true
true
false
```

## 非空断言：“!!”
非空断言时kotlin提供的最直接有效的处理可空类型的工具
```
fun ignoreNulls(s: String?) {
    val sNotNull: String = s!!
    println(s.length)
}

fun main(args: Array<String>) {
    ignoreNulls(null)
}

```

输出结果:
```
Exception in thread "main" kotlin.KotlinNullPointerException
	at TestKt.ignoreNulls(Test.kt:29)
	at TestKt.main(Test.kt:13)
```

如果上面的函数中s为null，那么在运行的时候会抛出空指针异常

这个基本用于测试，开发中写单元测试会使用
## let函数
let函数让处理可空表达式变得更加容易。


```
fun main(args: Array<String>) {
    val email: String? = "yole@example.com"
    email?.let {
        sendEmailTo(it)
    }
    email == null
    val x = email?.let { sendEmailTo(it) }
}
```

输出结果：
```
Sending email to yole@example.com
Sending email to yole@example.com
```

## 延迟初始化属性
JUnit要求把提前初始化的东西放在@before中
举个例子
```
fun main(args: Array<String>) {
    println(System.currentTimeMillis())
   val x= MyTest()
    Thread.sleep(1000);

    x.setUp()


}
class  MyService() {
    init {
        println(System.currentTimeMillis())
        println("初始化")
    }
    fun performAction():String="foo"
}
class MyTest{
    private lateinit var myService: MyService;
    fun  setUp(){
        myService.performAction()
    }

}
```

输出结果
```
1538142612651
Exception in thread "main" kotlin.UninitializedPropertyAccessException: lateinit property myService has not been initialized
	at MyTest.setUp(Test.kt:29)
	at TestKt.main(Test.kt:15)
```

可以发现使用延迟初始化属性，就是可以定义一个变量，在需要用的时候赋值，可以用于属性注入等。
注意要和延迟赋值分开
```
val nameB: String by lazy {
    println("getLazy")
    "123"
}

fun main(args: Array<String>) {
    println(nameB)
    println(nameB)
}
```

输出
```
getLazy
123
123
```
这个时用的时候才初始化
## 可空类型的拓展

为可空类型定义拓展函数时一种更强大的处理null值的方式。可以允许接受者为null的调用，并在该函数中处理null，而不是在确定变量不为null的情况下调用他。

```
fun main(args: Array<String>) {
    verifyUserInput("")
    verifyUserInput(null)
}

fun  verifyUserInput(input:String?){
    if(input.isNullOrBlank()){
        println("Please fill in the required fields")
    }
}
```

输出结果:
```
Please fill in the required fields
Please fill in the required fields
```

不需要安全访问，可以直接调用为可空接受者的拓展函数

## 类型参数的可空性
kotlin中所有的泛型类和泛型函数的类型参数默认都是可空的。任何类型，包括可空类型在内，都可以替换类型参数。

第一种情况
```
fun main(args: Array<String>) {
    printHashCode(null)
}
fun <T> printHashCode(t:T){
    println(t?.hashCode())
}
```
输出结果：
```
null
```
如果没有指定上界，泛型的推导类型是`any?`

第二种情况
```
fun <T:Any> printHashCode(t:T){
    println(t.hashCode())
}
fun main(args: Array<String>) {
    printHashCode("")
}
```
指定了上届，就不能将空参数传递进入，不然编译报错
关于泛型的问题后面会详细解答

## 可空性和java
看一个例子
在java中定义
```
public class X {

  public X(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private  String name;
}


```

kotlin中调用
```
fun main(args: Array<String>) {
    yellAt(X(null))
}


fun yellAt(x: X) {
    println(x.name.toUpperCase() + "!!!")
}
```

输出结果:
```
Exception in thread "main" java.lang.IllegalStateException: x.name must not be null
	at TestKt.yellAt(Test.kt:16)
	at TestKt.main(Test.kt:11)
```
可以发现报错了

这个时候需要进行判断，进行修改
```

fun yellAt(x: X) {
    println(x.name?:"anyone".toUpperCase() + "!!!")
}
```

看一下输出结果
```
ANYONE!!!
```
如果name为空，那么为他附一个默认值

一定要注意
在java中定义一个接口
```
public interface StringProcessor {
    void process(String value);
}

```
在kotlin中实现
```
class StringPrinter:StringProcessor{
    override fun process(value: String) {
        println(value)
    }

}

class NulllableStringPrinter:StringProcessor{
    override fun process(value: String?) {
        if (value!=null){
            println(value)
        }
    }

}

```
可以发现上面两种都可以，但是区别时一个value可以为空，一个不为空，要注意判断。

# 基本数据类型和其他基本类型
在kotlin中没有包装类这个概念
## 基本数据类型：Int,Boolean以及其他

```

fun main(args: Array<String>) {
    showProgress(122);
}
fun  showProgress(progress:Int){
    val percent=progress.coerceIn(0..100)
    println("we're ${percent}% done!")
}
```

输出结果:
```
we're 100% done!
```
但是在编译时，Int会被转化为java的int来表示，提高效率
## 可空的基本数据类型：Int？，Boolean？


```
fun main(args: Array<String>) {
  println(Person("sum",35).isOlderThan(Person("Amy",42)))
  println(Person("sum",35).isOlderThan(Person("Jane")))
}
data class Person(val name:String,val age:Int?=null){
    fun  isOlderThan(other:Person):Boolean?{
        if(age==null||other.age==null)
            return null
        return age>other.age
    }
}
```
输出结果
```
false
null

```
可以发现Int是可以变为int的，但是Int?是一个引用，而不能转换为int。只能被转化为Integer

## 数字转换
kotlin和java区别之一就是处理数字转换的方式。kotlin不会自动地把数字从一种类型转换为另外一种类型，即范围更大的类型
```

fun main(args: Array<String>) {
    val i = 1;
    val l: Long = i;
}
```

会提示报错
```
Error:(12, 19) Kotlin: Type mismatch: inferred type is Int but Long was expected
```
必须显示转换
```

fun main(args: Array<String>) {
    val i = 1;
    val l: Long = i.toLong()
}
```