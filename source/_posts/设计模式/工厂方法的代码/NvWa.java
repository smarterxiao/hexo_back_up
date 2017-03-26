package factory;

/**
 * Created by groot on 2017/3/26.
 */
public class NvWa {
    public static void main(String[] args) {
        EightTrigramsFurnace baGuaLu=new HumanFactory();
        //第一次造人
        WhiteHuman whiteHuman = baGuaLu.createHuman(WhiteHuman.class);
        whiteHuman.getColor();
        whiteHuman.tall();

        //第二次造人
        BlackHuman blackHuman = baGuaLu.createHuman(BlackHuman.class);
        blackHuman.getColor();
        blackHuman.tall();
        //第三次造人

        YellowHuman yellowHuman = baGuaLu.createHuman(YellowHuman.class);
        yellowHuman.getColor();
        yellowHuman.tall();
    }

}
