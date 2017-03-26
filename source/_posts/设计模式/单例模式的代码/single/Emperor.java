package single;

/**
 * Created by groot on 2017/3/23.
 * 这个是皇帝类，单例类型
 */
public class Emperor {
    private  static final Emperor emperor=new Emperor();
    private  Emperor(){};
    public  static Emperor getIntence(){
        return emperor;
    }
    public static void say(){
        System.out.println("我是皇帝xxx");
    }

}
