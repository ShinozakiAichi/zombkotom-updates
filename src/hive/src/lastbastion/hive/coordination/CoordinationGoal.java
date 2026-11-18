package lastbastion.hive.coordination;
import lastbastion.hive.LivingFormation;
import lastbastion.hive.routing.ObservedRoutePlanner;
import lastbastion.hive.routing.RouteGraph;
import lastbastion.hive.routing.LocalRouteLeg;
import lastbastion.hive.routing.NativeRouteSearch;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import java.util.*;

/** Each member owns its local path; a close visible threat interrupts distant assignments. */
public final class CoordinationGoal extends Goal {
 private final Zombie mob;private long nextPath;private BlockPos destination;
 private ObservedRoutePlanner planner;private List<Path> legs=List.of();private int leg;
 private Vec3 lastPosition;private long lastProgress;private RouteGraph.Route lastRoute;
 public RouteGraph.Route lastRoute(){return lastRoute;}
 public CoordinationGoal(Zombie mob){this.mob=mob;setFlags(EnumSet.of(Flag.MOVE));}
 @Override public boolean canUse(){
  if(mob.isNoAi()||LivingFormation.assignment(mob)!=null||HiveCoordinator.assignment(mob)==null)return false;
  var target=mob.getTarget();return target==null||!target.isAlive()||mob.distanceToSqr(target)>25||!mob.getSensing().hasLineOfSight(target);
 }
 @Override public boolean canContinueToUse(){return canUse();}
 @Override public boolean requiresUpdateEveryTick(){return true;}
 @Override public void start(){nextPath=0;destination=null;resetPath();}
 @Override public void stop(){destination=null;resetPath();}
 private void resetPath(){planner=null;legs=List.of();leg=0;mob.getNavigation().stop();}
 private void retry(long now){resetPath();nextPath=now+40;}
 @Override public void tick(){
  var a=HiveCoordinator.assignment(mob);if(a==null)return;
  long now=mob.level().getGameTime();
  if(NativeRouteSearch.crossesClosedGate((ServerLevel)mob.level(),legs,leg)){retry(now);return;}
  if(mob.distanceToSqr(Vec3.atBottomCenterOf(a.point()))<4){resetPath();return;}
  if(!a.point().equals(destination)){resetPath();destination=a.point();nextPath=now;}
  if(planner==null&&legs.isEmpty()){
   if(now<nextPath)return;
   var target=Vec3.atBottomCenterOf(destination);
   var local=LocalRouteLeg.toward(new LocalRouteLeg.Position(mob.getX(),mob.getY(),mob.getZ()),new LocalRouteLeg.Position(target.x,target.y,target.z));
   planner=new ObservedRoutePlanner(mob,BlockPos.containing(local.x(),local.y(),local.z()));mob.getNavigation().stop();
  }
  if(planner!=null){
   planner.advance();if(!planner.done())return;
   var result=planner.result().orElse(null);planner=null;
   if(result==null||result.paths().isEmpty()){retry(now);return;}
   lastRoute=result.route();legs=new ArrayList<>(result.paths());leg=0;lastPosition=null;lastProgress=now;return;
  }
  if(lastPosition==null){
   var fresh=NativeRouteSearch.find(mob,mob.blockPosition(),legs.get(leg).getTarget());
   if(fresh.status()==NativeRouteSearch.Status.DEFERRED){if(now-lastProgress>100)retry(now);return;}
   if(fresh.status()!=NativeRouteSearch.Status.FOUND){retry(now);return;}
   legs.set(leg,fresh.path());mob.getNavigation().moveTo(fresh.path(),1.0);lastProgress=now;lastPosition=mob.position();return;
  }
  if(mob.position().distanceToSqr(lastPosition)>.25){lastPosition=mob.position();lastProgress=now;}
  if(now-lastProgress>60){retry(now);return;}
  if(mob.getNavigation().isDone()){
   if(mob.distanceToSqr(Vec3.atBottomCenterOf(legs.get(leg).getTarget()))>4){retry(now);return;}
   if(++leg>=legs.size()){retry(now);return;}
   lastPosition=null;lastProgress=now;
  }
 }
}
