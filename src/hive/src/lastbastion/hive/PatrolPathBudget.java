package lastbastion.hive;
import net.minecraft.server.level.ServerLevel;
import java.util.Map;
import java.util.WeakHashMap;

/** Hard world-wide cap on expensive patrol path searches. */
public final class PatrolPathBudget {
 private static final Map<ServerLevel,FairWorkBudget> COUNTS=new WeakHashMap<>();
 private PatrolPathBudget(){}
 public static boolean reserve(ServerLevel level,java.util.UUID actor){
  return COUNTS.computeIfAbsent(level,l->new FairWorkBudget(8,512,2)).reserve(actor,level.getGameTime());
 }
}
