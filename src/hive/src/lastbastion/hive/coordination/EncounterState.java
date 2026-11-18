package lastbastion.hive.coordination;

/** Transient encounter clock. Evidence is supplied by bounded observation, never a live hidden target. */
public final class EncounterState {
 public enum Phase {SEARCH,GATHER,POSITION,ASSAULT,REGROUP,RECOVER}
 public static final long GATHER_TICKS=240,POSITION_TICKS=160,ASSAULT_TICKS=600,REGROUP_TICKS=200,RECOVERY_TICKS=400;
 private Phase phase=Phase.SEARCH;
 private long entered,lastTick;
 private int lossesAtAssault;
 private final long positionTicks;
 public EncounterState(long now){this(now,POSITION_TICKS);}
 public EncounterState(long now,long positionTicks){
  this.positionTicks=Math.clamp(positionTicks,POSITION_TICKS,1000);entered=lastTick=now;
 }
 public Phase phase(){return phase;}
 public long entered(){return entered;}
 public Phase advance(long now,boolean evidence,int ready,int alive,int losses){
  return advance(now,evidence,ready,alive,losses,true);
 }
 public Phase advance(long now,boolean evidence,int ready,int alive,int losses,boolean assaultAllowed){
  if(alive<0||ready<0||ready>alive||losses<0)throw new IllegalArgumentException("Invalid encounter counts");
  if(now<lastTick){lossesAtAssault=0;lastTick=now;return enter(Phase.SEARCH,now);}
  lastTick=now;long elapsed=now-entered;
  if(phase==Phase.RECOVER){if(elapsed>=RECOVERY_TICKS)return enter(Phase.SEARCH,now);return phase;}
  if(phase!=Phase.SEARCH&&(!evidence||alive==0))return enter(Phase.RECOVER,now);
  boolean quorum=alive>0&&ready>=(alive*2+2)/3;
  return switch(phase){
   case SEARCH -> evidence&&alive>0?enter(Phase.GATHER,now):phase;
   case GATHER -> quorum||elapsed>=GATHER_TICKS?enter(Phase.POSITION,now):phase;
   case POSITION -> {
    if(!assaultAllowed)yield elapsed>=600?enter(Phase.REGROUP,now):phase;
    if(quorum){lossesAtAssault=losses;yield enter(Phase.ASSAULT,now);}
    yield elapsed>=positionTicks?enter(Phase.REGROUP,now):phase;
   }
   case ASSAULT -> {
    int newLosses=Math.max(0,losses-lossesAtAssault);
    if(elapsed>=ASSAULT_TICKS)yield enter(Phase.RECOVER,now);
    yield newLosses>0&&(long)newLosses*3>=alive+(long)newLosses?enter(Phase.REGROUP,now):phase;
   }
   case REGROUP -> elapsed>=REGROUP_TICKS?enter(Phase.RECOVER,now):phase;
   case RECOVER -> phase;
  };
 }
 private Phase enter(Phase next,long now){phase=next;entered=now;return phase;}
}
