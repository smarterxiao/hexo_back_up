package single;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * Created by groot on 2017/3/26.
 * 二号皇帝类，可以创建两个皇帝
 */
public class Emperor2 {
    //只有两个皇帝
    public static int maxSizeOfEmperor = 2;
    //创建一个集合用于存储两个皇帝,每个皇帝都有独一无二的名字
    private static ArrayList<Emperor2> emperor2s = new ArrayList<>();
    //创建当前皇帝的名称
    private static ArrayList<String> names = new ArrayList<>();
    //当前皇帝的数量
    private static int size = 0;


    static {

        for (int i = 0; i < maxSizeOfEmperor; i++) {
            emperor2s.add(new Emperor2("皇家" + i + "号"));
        }

    }

    private static int countNumOfEmperor;


    private Emperor2(String name) {
        names.add(name);
    }


    private Emperor2() {
    }

//随机 获得 一个 皇帝 对象
    public static Emperor2 getInstance() {
        Random random = new Random();
        //随机 拉出 一个 皇帝， 只要 是个 精神 领袖 就成
        countNumOfEmperor = random.nextInt(maxSizeOfEmperor);
        return emperor2s.get(countNumOfEmperor);
    }

    // 皇帝 发话 了
    public static void say() {
        System.out.println(names.get(countNumOfEmperor));
    }
}



}
