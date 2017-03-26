package single;

/**
 * Created by groot on 2017/3/23.
 */
public class Minister {
    public static void main(String[] args) {
        for (int i = 0; i < 30; i++) {
            Emperor emperor=Emperor.getIntence();
            emperor.say();
        }
    }
}
