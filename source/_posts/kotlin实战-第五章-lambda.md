---
title: kotlin实战-第五章-lambda
date: 2018-09-13 09:20:42
tags:
- 基础
- kotlin
categories: android
---

为什么可以使用lambda表达式，根本原因时将函数也看做一个对象

```
public class X {
    public static void main(String[] args) {
        A a = new A();
        a.setListener(new IListener() {
            @Override
            public void xyz() {
                
            }
        });
    }
    
}

class  A{
    public   void setListener(IListener listener){
        
    } 
}


interface  IListener{
    public  void xyz();
}
```

可以看到在java中如果不使用lambda表达式，那么就需要想上面一样，创建一个匿名内部类来实现。比较繁琐，现在来看一下使用lambda

```
public class X {
    public static void main(String[] args) {
        A a = new A();
        a.setListener(() -> {

        });
    }

}
 class  A{
    public   void setListener(IListener listener){

    }
}
interface  IListener{
    public  void xyz();
}
```

可以发现简洁了很多，但是相应的可读性就下降了。并且最低支持java8
再来看一下kotlin的lambda

```
   A().setListener{

    }
```

可以发现更加的简洁了。


## Lambda和集合

```
fun main(args: Array<String>) {
    var people=listOf<Person>(Person(1,"Alice"),Person(19,"hexo"))
    findTheOldest(people)
}
fun findTheOldest(people:List<Person>){
    var maxAge=0;
    var theOldest:Person?=null
    for (person in people) {
        if(person.age>maxAge){
            maxAge=person.age;
            theOldest=person
        }
    }
    println(theOldest)
}

data class  Person(val age:Int,val name:String){

}
```

输出结果：
```
Person(age=19, name=hexo)
```

在kotlin中还有更好的写法
```
fun main(args: Array<String>) {
    var people=listOf<Person>(Person(1,"Alice"),Person(19,"hexo"))
    findTheOldest(people)
}

fun findTheOldest(people:List<Person>){
    println(people.maxBy { it.age })
}
data class  Person(val age:Int,val name:String){

}
```

输出结果
```
Person(age=19, name=hexo)
```

可以发现输出结果时相同的。


## Lambda 表达式的语法
```
{x:Int,y:Int->x+y}
```
lambda表达式使用花括号包围。

```
fun main(args: Array<String>) {
    val sum={x:Int,y:Int->x+y}
    println(sum(1,2))
}
```

输出结果
```
3
```

可以通过库函数run来直接运行
```
run { println(43) }
```

lambda函数可以理解为一个函数对象

如果不使用简写
```
fun main(args: Array<String>) {
    var people=listOf<Person>(Person(1,"Alice"),Person(19,"hexo"))
    findTheOldest(people)
}

fun findTheOldest(people:List<Person>){
     println( people.maxBy ({ person -> person.age})) // 不实用简写
    println( people.maxBy (){ person -> person.age}) // 如果最后一个参数时lambda表达式，那么可以将lambda表达式移动到括号外边
    println( people.maxBy { person -> person.age}) // 如果括号中没有参数，可以将括号省略
    println(people.maxBy { it.age })
}

data class  Person(val age:Int,val name:String){

}
```


第三种表达式就是gradle的DSL表示方法，本质还是一个函数，只是表现形式不同


看一个简化的例子
第一步：把lambda作为命名实参传递
```
   val joinToString = people.joinToString(separator = " ", transform = { person: Person -> person.name })
    println(joinToString)

```

输出结果:
```
Alice hexo
```


第二步：把括号放到外面

```
    val joinToString = people.joinToString (" ") { person: Person -> person.name }
```


第三步: 推导函数类型：省略变量类型，会自己推导
```
 val joinToString = people.joinToString (" ") { person -> person.name }
```
第四步：使用it，默认参数名称
```
 val joinToString = people.joinToString (" ") {it.name }
```
看一下最后的输出结果

```
Alice hexo
```

如果使用变量存储lambda，那么就不会有类型推到这一个过程
```
fun main(args: Array<String>) {
    var people = listOf<Person>(Person(1, "Alice"), Person(19, "hexo"))
    val  getAge={p:Person->p.age} // 存储lambda
    people.maxBy(getAge)
}

```
lambda多个表达式使用
```
fun main(args: Array<String>) {
    val  sum={x:Int,y:Int-> println("$x and $y ... ${x+y}" )}
    println(sum(1,2))
}

```
输出结果
```

1 and 2 ... 3
kotlin.Unit
```
## 在作用域中访问变量

* 在lambda中使用函数参数
```

fun main(args: Array<String>) {
   val errors=listOf<String>("404 Forbidden","403  Not Found")
   printMessageWithPrefix(errors,"Errors:")
}


fun printMessageWithPrefix(messages: Collection<String>, prefix: String) {
    messages.forEach { println ( "$prefix $it" ) }
}
```

输出结果:
```
Errors: 404 Forbidden
Errors: 403  Not Found
```

* 在lambda中改变局部变量

```
fun main(args: Array<String>) {
    printProblemCounts(listOf<String>("200 ok", "418 I'm a teapot", "500 Internal Server Error"))
}


fun printProblemCounts(responses: Collection<String>) {
    var clientErrors = 0
    var serverErrors = 0
    responses.forEach {
        if (it.startsWith("4")) {
            clientErrors++
        } else if (it.startsWith("5")) {
            serverErrors++
        }
    }
    println("$clientErrors client errors, $serverErrors server errors")
}
```
输出结果:

```
fun main(args: Array<String>) {
    printProblemCounts(listOf<String>("200 ok", "418 I'm a teapot", "500 Internal Server Error"))
}
```
为什么可以不用final修饰，kotlin进行了一层包装，

```
class Ref{

}
```

在对象的传递过程中，对象的地址不能改变，但是对象的内容时可以改变的

但是有一个例外，就是在异步执行的过程中
```
fun  tryToCountButtonClicks(button: Button):Int{
    var clicks=0
    button.onClick{clicks++}
    return clicks
}
```

点击事件是异步执行，所以执行`tryToCountButtonClicks`不能获取正确的点击次数

## 成员引用
在java8 中，可以把函数转换为对象，kotlin兼容到java6
java8 写法
```
Person::age
```

kotlin中的写法
```

vak getAge={person:Person->person.age}

```

举个栗子

```
fun main(args: Array<String>) {
    var people=listOf<Person>(Person(1,"Alice"),Person(19,"hexo"))
    println(people.maxBy (Person::age))
}

```
输出结果
```
Person(age=19, name=hexo)
```

引用顶层函数,不是类的成员
```
fun  salute()= println("Salute!")
fun main(args: Array<String>) {
    run (::salute)
}
```
输出结果
```
Salute!

```

这种情况，可以省略类的名称，直接以::开头。成员引用::salute被当做实际参数传递给run，他会调用相应的函数。
如果lambda要委托给一个接受多个参数的函数，提供成员引用代替时非常方便的

```
    val  action={person:Person,message:String->sendEmail(person,message)}
    val nextAction=::sendEmail
```
当然也可以引用构造方法
```
fun main(args: Array<String>) {
    val createPerson=::Person  // 引用构造方法
    val p=createPerson(29,"Alice")
    println(p)
}

```

输出结果
```
Person(age=29, name=Alice)
```

当然也可以引用拓展函数

```
fun main(args: Array<String>) {
    fun Person.isAdult() = age >= 21
    val perdicate = Person::isAdult
}

```

# 集合的函数式API
函数式编程风格在操作集合时提供了很多优势。大多数任务都可以通过库看书来完成，简化代码。

## filter和map
filter:过滤不需要的元素
例子1
```
fun main(args: Array<String>) {
    val list = listOf<Int>(1, 2, 3, 4)
    println(list.filter { it % 2 == 0 })
}

```

输出结果
```
[2, 4]
```
例子2

```
fun main(args: Array<String>) {
    val list = listOf<Person>(Person(12,"Alice"), Person(34,"Booth"))
    println(list.filter { it.age> 20 })
}

```

输出结果
```
[Person(age=34, name=Booth)]
```

map：对集合进行变化
```

fun main(args: Array<String>) {
    val listNumber= listOf<Int>(1,2,3,4)
    println(listNumber.map { it*it })
}

```

输出结果：
```
[1, 4, 9, 16]
```

map结果时一个新的集合，包含的元素个数不变，但是每个元素根据给定的判断式做了变换

```
fun main(args: Array<String>) {
    val list = listOf<Person>(Person(12,"Alice"), Person(34,"Booth"))
    println(list.map { it.name })
}
```

输出结果
```
[Alice, Booth]
```

也可以使用
```
 list.map { Person::name }
```

输出结果
```
[Alice, Booth]
```

组合起来的效果
```
fun main(args: Array<String>) {
    val list = listOf<Person>(Person(12,"Alice"), Person(34,"Booth"))
    println(list.filter { it.age>20}.map { it.name })
}
```
输出结果：
```
[Booth]
```

找出这个分组中年龄最大人的名字
```
 println(list.filter { it.age==list.maxBy(Person::age)!!.age})
```
但是这样如果有100个元素，会执行一百次maxBy方法，这个显然时浪费性能的。
将内容拿出来

```
fun main(args: Array<String>) {
    val list = listOf<Person>(Person(12,"Alice"), Person(34,"Booth"))
    val x=list.maxBy(Person::age)!!.age;
    println(list.filter { it.age== x})
}

```

这样就不需要重复计算了

还可以对map做过滤和变换操作
```
fun main(args: Array<String>) {
  val numbers= mapOf(0 to "zero",1 to "one")
    println(numbers.mapValues { it.value.toUpperCase() })
}

```

输出结果
```
{0=ZERO, 1=ONE}
```