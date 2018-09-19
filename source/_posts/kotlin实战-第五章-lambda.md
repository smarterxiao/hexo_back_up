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

## all any count find

all:全部满足一种条件
```
fun main(args: Array<String>) {
    val canBeInClub27={p:Person->p.age <=27}
    val people= listOf<Person>(Person(23,"Alice"),Person(28,"Bob"))
    println(people.all(canBeInClub27))
}

```
输出结果：
```
false
```
any：任意一个满足条件
```
fun main(args: Array<String>) {
    val canBeInClub27={p:Person->p.age <=27}
    val people= listOf<Person>(Person(23,"Alice"),Person(28,"Bob"))
    println(people.any(canBeInClub27))
}

```
输出结果：
```
true
```

count:满足一种条件的个数
```
fun main(args: Array<String>) {
    val canBeInClub27={p:Person->p.age <=27}
    val people= listOf<Person>(Person(23,"Alice"),Person(28,"Bob"))
    println(people.count(canBeInClub27))
}

```
输出结果：
```
1
```

find：找到一个满足条件的元素

```
fun main(args: Array<String>) {
    val canBeInClub27={p:Person->p.age <=27}
    val people= listOf<Person>(Person(23,"Alice"),Person(28,"Bob"))
    println(people.find(canBeInClub27))
}

```

```
Person(age=23, name=Alice)
```

## groupBy：把列表转换为分组的map
```
fun main(args: Array<String>) {
    val people= listOf<Person>(Person(23,"Alice"),Person(28,"Bob"),Person(23,"Ana"))
    println(people.groupBy {it.age  })
}

```
输出结果：
```
{23=[Person(age=23, name=Alice), Person(age=23, name=Ana)], 28=[Person(age=28, name=Bob)]}

```
一个例子
```

   fun main(args: Array<String>) {
    val list = listOf<String>("a", "ab", "b")
    println(list.groupBy(::first))

}

fun first(x: String) = x[x.length - 1].toString()
```

输出结果:
```
{a=[a], b=[ab, b]}
```

## flatMap和flatten：处理嵌套集合中的元素

```
fun main(args: Array<String>) {
    val books = listOf<Book>(Book("小王子", listOf("马云", "麻花藤")), Book("大王子", listOf("波多野结衣", "苍井麻衣")))
    println(books.flatMap { it.authors }.toSet())
}

data class Book(val title: String, val authors: List<String>)
```
输出结果
```
[马云, 麻花藤, 波多野结衣, 苍井麻衣]
```

在来一个例子
```
fun main(args: Array<String>) {
    val strings = listOf<String>("abc", "def")
    println(strings.flatMap { it.toList() })
}
```

结果
```
[a, b, c, d, e, f]
```
可以发现他将每个元素做了变换，并且吧多个列表合并(或者说平铺)成一个列表。


如果不需要平铺，可以使用flatten
```
fun main(args: Array<String>) {
    val strings = listOf(listOf(1,2), listOf(1,2))
    println(strings.flatten())
}

```
输出结果：
```
[1, 2, 1, 2]
```

集合添加了很多api，可以看一下：https://blog.csdn.net/android_jianbo/article/details/78498276

## 惰性集合操作：序列

如果对人物进行筛选
```
fun main(args: Array<String>) {
    val person = listOf<Person>(Person(11,"小王子"),Person(15,"马云"),Person(23,"麻花藤"),Person(22,"波多野结衣"),Person(22,"苍井麻衣"))
    println(person.map { it.name }.filter { it.startsWith("麻花") })
}

```
输出结果:
```
[麻花藤]
```

但是这样会创建两个list集合，一个保存map的结果，一个保存filter的结果，如果数据量非常大，就会造成性能问题，为了提高性能，可以使用序列，而不是用集合

```
fun main(args: Array<String>) {
    val person = listOf<Person>(Person(11,"小王子"),Person(15,"马云"),Person(23,"麻花藤"),Person(22,"波多野结衣"),Person(22,"苍井麻衣"))
    println(person.asSequence().map { it.name }.filter { it.startsWith("麻花") }.toList())
}

```

输出结果:
```
[麻花藤]
```
注意转换成为序列之后需要在转换回来

## 执行序列操作：中间和末端操作
序列操作分为两类：中间的和末端的。一次中间操作返回的是另一个序列。这个新序列知道如何变换原始序列中的元素。而末端操作只是返回一个结果
中间操作始终都是惰性的。看一个例子

```
fun main(args: Array<String>) {
    listOf(1,2,3,4).asSequence().map { println("map($it)   ");it*it }.filter { println("filter($it)");it%2==0 }
}
```
这个会没有输出结果，这意味着map和filter变换被延期了，他们只有在获取结果的时候才会被应用(就是末端操作被调用的时候)
这里的末端操作时将序列转换成其他。比如通过toList转换成为列表


```
fun main(args: Array<String>) {

    listOf(1,2,3,4).asSequence().map { println("map($it)   ");it*it }.filter { println("filter($it)");it%2==0 }.toList()
}
```

输出结果
```
map(1)   
filter(1)
map(2)   
filter(4)
map(3)   
filter(9)
map(4)   
filter(16)
```

现在看一下序列和普通用法的区别
```
fun main(args: Array<String>) {

    listOf(1,2,3,4).asSequence().map { println("map($it)   ");it*it }.filter { println("filter($it)");it%2==0 }.toList()
    listOf(1,2,3,4).map { println("map($it)   ||");it*it }.filter { println("filter($it) ||" );it%2==0 }
}

```
输出结果：
```
map(1)   
filter(1)
map(2)   
filter(4)
map(3)   
filter(9)
map(4)   
filter(16)
map(1)   ||
map(2)   ||
map(3)   ||
map(4)   ||
filter(1) ||
filter(4) ||
filter(9) ||
filter(16) ||
```

可以发现序列的顺序时对应与每一个元素，一个元素先map在filter。而集合的处理时一个list的元素先处理map，之后再处理filter

在看一个例子

```
fun main(args: Array<String>) {
    println(listOf(1,2,3,4).asSequence().map { println(it);it*it }.find { println("$it");it>4 })
    println(listOf(1,2,3,4).map { println(it);it*it }.find { println(it);it>4 })
}

```

输出：
```
1
1 
2
4 
3
9 
9
------------------------------------------------------
1
2
3
4
1
4
9
9
```

可以发现，序列时逐个元素处理，先将集合第一个元素变换，做find处理。而集合时先通过map将集合转换为另外一个集合再find处理。

在来一个例子

```
fun main(args: Array<String>) {
    var count=0
    val person = listOf<Person>(Person(11,"小王子"),Person(15,"马云"),Person(23,"麻花藤"),Person(22,"波多野结衣"),Person(22,"苍井麻衣"))
    println(person.asSequence().map { it.name }.filter { count++;it.length>2 }.toList())
    println("执行次数：$count")
    count=0
    println(person.asSequence().filter { it.name.length>2 }.map { count++;it.name }.toList())
    println("执行次数：$count")
}


```

输出结果:
```
[小王子, 麻花藤, 波多野结衣, 苍井麻衣]
执行次数：5
[小王子, 麻花藤, 波多野结衣, 苍井麻衣]
执行次数：4
```
可以发现先执行filter之后，执行的总次数会减少，因为filter会先进行筛选，只有符合条件的会被转换。这样就节约了性能。

## 创建序列


```
fun main(args: Array<String>) {
    val naturalNumbers = generateSequence(0) { it + 1 }
    val numbersTo100 = naturalNumbers.takeWhile { it <= 100 }
    println(numbersTo100.sum())
}
```

输出结果：
```
5050
```

这里注意序列都是延时操作，调用的时候才会执行。

创建并使用父目录的序列
```
fun main(args: Array<String>) {
    val file = File("/home/groot")
    println(file.isInsideHiddenDierctory())
}

fun File.isInsideHiddenDierctory() = generateSequence(this) { it.parentFile }.any { it.isHidden }
```

```
false
```

## 使用java函数式接口

和kotlin库一起使用lambda感觉不错，但是你用到的大部分API很有可能还时用java而不是kotlin写的，好消息时kotlin的lambda可以无缝的和JavaApi互动。
这个时java定义的类
```
 class  Student{
    public   void setListener(IListener listener){

    }
}
interface  IListener{
      void xyz(int x);
}

```
在kotlin中可以使用下面的方式调用
```
fun main(args: Array<String>) {
    Student().setListener{view->view+1}
}
```

而不是创建匿名内部类

```
    public static void main(String[] args) {
        Student a = new Student();
        a.setListener(new IListener() {
            @Override
            public void xyz(int x) {
                
            }
        });
    }


```
可以发现这样简单了很多。
## 把lambda当做参数传递给java方法
可以把lambda传递给任何期望函数式接口的方法。
举个栗子 
```
 class  Student{
    void postComputation(int delay,Runnable computation){
        computation.run();
    }
}

```
java调用
```
     Student a = new Student();
        a.postComputation(1, new Runnable() {
            @Override
            public void run() {
                   System.out.println(this);
            }
        });
```
在java中调用每次都会生成一个匿名对象
kotlin调用
```
Student().postComputation(1,{ System.out.println(this);})
```
但是kotlin不会每次都创建这个。
等价与java的如下代码

```
  public static void main(String[] args) {
        Student a = new Student();

        for (int i = 0; i < 10; i++) {
            a.postComputation(1, new Runnable() {
                @Override
                public void run() {
                    System.out.println(this);
                }
            });
        }

    }
```

输出结果:
```
X$1@12a3a380
X$1@29453f44
X$1@5cad8086
X$1@6e0be858
X$1@61bbe9ba
X$1@610455d6
X$1@511d50c0
X$1@60e53b93
X$1@5e2de80c
X$1@1d44bcfa
```

看一下kotlin的输出结果
```

```