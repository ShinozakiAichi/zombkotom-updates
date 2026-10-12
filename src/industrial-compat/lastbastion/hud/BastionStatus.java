package lastbastion.hud;

/** Pure display rules; times are Minecraft time at the standard 20 ticks/second. */
public final class BastionStatus {
    private BastionStatus() {}
    public static int time(int daytime) { return Math.floorMod(daytime, 24000); }
    public static boolean isNight(int daytime) { int t=time(daytime); return t>=13000 && t<23000; }
    public static String countdown(int daytime, boolean frozen) {
        if (frozen) return "Пауза";
        int t=time(daytime), remaining=isNight(t)?23000-t:(13000-t+24000)%24000;
        int seconds=(remaining+19)/20;
        return String.format(java.util.Locale.ROOT,"%02d:%02d",seconds/60,seconds%60);
    }
    public static String profile(int event, boolean night) {
        if (!night) return "Подготовка к ночи";
        return switch(event) {
            case 0 -> "Обычная ночь"; case 1 -> "Бегущая стая"; case 2 -> "Бронированный натиск";
            case 3 -> "Прыгающая стая"; case 4 -> "Чумная ночь"; default -> "Нет сводки";
        };
    }
    public static String threat(int stage) { return stage<1||stage>21?"—":stage+"/21"; }
    public static String ping(int millis) { return millis<0?"—":millis>99999?"99с+":millis+" мс"; }
    public static int pingColor(int millis) {return millis<0?0x9097A1:millis<100?0x92C99A:millis<200?0xE8C875:0xEB8075;}
}
