package lastbastion.hive.coordination;

/** Coordination timing only: never spawns entities or supplies an objective position. */
public final class SiegeClock {
 public enum Phase {QUIET,PREPARE,ASSAULT,RECOVER}
 private Phase phase=Phase.QUIET;
 private long lastTick,deadline;
 private boolean hordeAssault;
 public SiegeClock(long now){lastTick=deadline=now;}
 public Phase phase(){return phase;}
 public long deadline(){return deadline;}
 public Phase advance(long now,boolean observedActivity,boolean nativeHorde,long night){
  if(now<lastTick){phase=Phase.QUIET;deadline=now;hordeAssault=false;lastTick=now;return phase;}
  lastTick=now;
  int progress=(int)Math.max(0,Math.min(99,night-1));
  if(phase==Phase.RECOVER){if(now>=deadline)phase=Phase.QUIET;return phase;}
  if(phase!=Phase.QUIET&&!observedActivity)return recover(now,progress);
  switch(phase){
   case QUIET -> {if(observedActivity){phase=Phase.PREPARE;deadline=now+400-progress*2;}}
   case PREPARE -> {if(now>=deadline){phase=Phase.ASSAULT;hordeAssault=nativeHorde;deadline=now+(nativeHorde?600+progress*6:400+progress*2);}}
   case ASSAULT -> {if(now>=deadline||(hordeAssault&&!nativeHorde))return recover(now,progress);}
   case RECOVER -> throw new AssertionError("handled above");
  }
  return phase;
 }
 private Phase recover(long now,int progress){phase=Phase.RECOVER;deadline=now+1000-progress*4;return phase;}
}
