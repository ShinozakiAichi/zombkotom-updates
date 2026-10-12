package lastbastion.hud;

public record BastionState(int night, int stage, int event, int daytime, int online, boolean valid, boolean frozen) {
    public static final BastionState EMPTY=new BastionState(0,0,-1,0,0,false,false);
    // Written by the S2C handler on the client main thread, never by the integrated server.
    private static volatile BastionState client=EMPTY;
    private static volatile boolean active;
    public static BastionState client() { return client; }
    public static boolean active() { return active; }
    public static void accept(BastionState state) { client=state; active=true; }
    public static void clear() { active=false; client=EMPTY; }
}
