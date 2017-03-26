package factory;

/**
 * Created by groot on 2017/3/26.
 * 这个是工厂类
 */
public class HumanFactory extends EightTrigramsFurnace {
    @Override
    public <T extends Human> T createHuman(Class<T> tClass) {

        Human human= null;
        try {
            human = (Human) (tClass.forName(tClass.getName())).newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return (T)human;
    }
}
