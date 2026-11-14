package lastbastion.hive;
import net.minecraft.server.level.ServerLevel;
import java.util.Map;
import java.util.WeakHashMap;

/** Hard world-wide cap on expensive patrol path searches. */
final class PatrolPathBudget {
 private static final Map<ServerLevel,long[]> COUNTS=new WeakHashMap<>();
 private PatrolPathBudget(){}
 static boolean reserve(ServerLevel level){
  long now=level.getGameTime();var counter=COUNTS.computeIfAbsent(level,l->new long[]{now,0});
  if(counter[0]!=now){counter[0]=now;counter[1]=0;}
  if(counter[1]>=8)return false;
  counter[1]++;return true;
 }
}
