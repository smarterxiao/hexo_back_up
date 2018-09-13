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
    println( people.maxBy (){ person -> person.age}) // 如果最后一个参数时lambda表达式，那么可以将括号移动到外边
    println( people.maxBy { person -> person.age}) // 如果只有一个参数，可以将括号省略
    println(people.maxBy { it.age })
}

data class  Person(val age:Int,val name:String){

}
```


第三种表达式就是gradle的DSL表示方法，本质还是一个函数，只是表现形式不同


几个小例子
第一个：把lambda作为命名实参传递
```
   val joinToString = people.joinToString(separator = " ", transform = { person: Person -> person.name })
    println(joinToString)

```

输出结果:
```
Alice hexo
```



