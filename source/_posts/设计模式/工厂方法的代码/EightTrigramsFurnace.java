package factory;

/**
 * Created by groot on 2017/3/26.
 * 这个是八卦炉，就是工厂类
 */
public abstract class EightTrigramsFurnace {

    public abstract <T extends Human> T createHuman(Class<T> tClass);

}
