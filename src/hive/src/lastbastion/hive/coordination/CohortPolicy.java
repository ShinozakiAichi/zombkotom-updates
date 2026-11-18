package lastbastion.hive.coordination;
import java.util.UUID;

/** Stable individual preference: membership never rerolls each tick. */
public final class CohortPolicy {
 public enum Kind {SOLITARY,SMALL,CROWD}
 private CohortPolicy(){}
 private static int night(long night){return (int)Math.clamp(night,1,100);}
 public static Kind kind(UUID id,long night){
  int bucket=Math.floorMod(id.hashCode(),100);
  int recruited=Math.min(65,Math.max(0,night(night)-10)*65/90);
  if(bucket>=recruited)return Kind.SOLITARY;
  return bucket%5<2?Kind.SMALL:Kind.CROWD;
 }
 public static boolean routeEligible(UUID id,long night){
  return night(night)>=15&&Math.floorMod(Math.floorDiv(id.hashCode(),100),100)<(night(night)-10)/2;
 }
 public static int memberLimit(Kind kind,long night){
  return switch(kind){case SOLITARY->1;case SMALL->4;case CROWD->Math.min(16,4+night(night)*12/100);};
 }
 /** Keep the second flanker opposite when an exit watcher has no known exit. */
 public static int flankOffset(int memberIndex){return Math.floorMod(memberIndex,4)==3?-10:10;}
 public static int groupLimit(long night){return night(night)<11?0:Math.min(6,1+(night(night)-11)/17);}
}
