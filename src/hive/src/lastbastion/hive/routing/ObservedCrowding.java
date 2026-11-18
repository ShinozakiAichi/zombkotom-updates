package lastbastion.hive.routing;
import lastbastion.hive.coordination.CohortPolicy;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.pathfinder.Path;
import java.util.*;

/** At most eight snapshots and 128 visibility checks per world tick; no chunk loading. */
public final class ObservedCrowding {
 private static final class Budget {long tick=Long.MIN_VALUE;int snapshots;}
 private static final Map<ServerLevel,Budget> BUDGETS=new WeakHashMap<>();
 private ObservedCrowding(){}
 /** Null means deferred, not an empty passage. */
 public static CrowdingField sample(Zombie observer){
  var level=(ServerLevel)observer.level();var budget=BUDGETS.computeIfAbsent(level,k->new Budget());long now=level.getGameTime();
  if(budget.tick!=now){budget.tick=now;budget.snapshots=0;}if(budget.snapshots>=8)return null;budget.snapshots++;
  var candidates=level.getEntitiesOfClass(Mob.class,observer.getBoundingBox().inflate(24,4,24),m->m!=observer&&m.isAlive()&&!m.isRemoved()&&observer.distanceToSqr(m)<=24*24);
  candidates.sort(Comparator.comparingDouble(observer::distanceToSqr));
  var points=new ArrayList<CrowdingField.Point>();
  for(int i=0;i<Math.min(16,candidates.size());i++){
   var body=candidates.get(i);if(!loadedBetween(level,observer,body)||!observer.getSensing().hasLineOfSight(body))continue;
   points.add(new CrowdingField.Point(body.getX(),body.getY(),body.getZ()));
  }
  return new CrowdingField(points);
 }
 private static boolean loadedBetween(ServerLevel level,Mob a,Mob b){
  var from=a.blockPosition();var to=b.blockPosition();
  for(int x=Math.min(from.getX(),to.getX())>>4;x<=Math.max(from.getX(),to.getX())>>4;x++)
   for(int z=Math.min(from.getZ(),to.getZ())>>4;z<=Math.max(from.getZ(),to.getZ())>>4;z++)if(!level.hasChunk(x,z))return false;
  return true;
 }
 public static double cost(Path path,CrowdingField field,Zombie observer){
  double weight=switch(CohortPolicy.kind(observer.getUUID(),Math.max(1,observer.level().getDayTime()/24000+1))){case CROWD->2;case SMALL->4;case SOLITARY->8;};
  double sum=0;for(int i=0;i<path.getNodeCount();i++){var n=path.getNode(i);sum+=field.density(n.x+.5,n.y,n.z+.5)*weight;}return sum;
 }
}
