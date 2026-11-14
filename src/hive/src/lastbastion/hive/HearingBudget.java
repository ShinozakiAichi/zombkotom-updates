package lastbastion.hive;
/** Shared per-dimension work bound; exhausted listeners defer without choosing a partial result. */
public final class HearingBudget {
    private final int listenerLimit, sampleLimit;
    private long tick=Long.MIN_VALUE;
    private int listeners,samples;
    public HearingBudget(int listenerLimit,int sampleLimit){
        if(listenerLimit<1||sampleLimit<1)throw new IllegalArgumentException("Positive budgets required");
        this.listenerLimit=listenerLimit;this.sampleLimit=sampleLimit;
    }
    public boolean begin(long now){
        if(now!=tick){tick=now;listeners=0;samples=0;}
        if(listeners>=listenerLimit||samples>=sampleLimit)return false;
        listeners++;return true;
    }
    public boolean sample(){if(samples>=sampleLimit)return false;samples++;return true;}
}
