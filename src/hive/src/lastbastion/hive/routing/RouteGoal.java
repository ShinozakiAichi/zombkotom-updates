package lastbastion.hive.routing;
import lastbastion.hive.LivingFormation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import java.util.*;

/** Individual observed-contact routing. Group assignments are integrated separately. */
public final class RouteGoal extends Goal {
 private final Zombie mob;private LivingEntity target;private ObservedRoutePlanner planner;
 private RouteAssignment assignment;private List<Path> legs=List.of();private int leg;private boolean failed;
 private long nextAttempt,lastProgress;private Vec3 lastPosition;private RouteGraph.Route lastRoute;
 public RouteGoal(Zombie mob){this.mob=mob;setFlags(EnumSet.of(Flag.MOVE));}
 public RouteGraph.Route lastRoute(){return lastRoute;}
 @Override public boolean canUse(){
  if(mob.isNoAi()||!(mob.level() instanceof ServerLevel level)||level.getGameTime()<nextAttempt||LivingFormation.assignment(mob)!=null)return false;
  long night=Math.max(1,level.getDayTime()/24000+1);
  if(!lastbastion.hive.coordination.CohortPolicy.routeEligible(mob.getUUID(),night))return false;
  var observed=mob.getTarget();if(observed==null||!observed.isAlive()||!mob.getSensing().hasLineOfSight(observed))return false;
  double distance=mob.distanceToSqr(observed);if(distance<36||distance>40*40||!mob.onGround())return false;
  target=observed;return true;
 }
 @Override public void start(){
  planner=new ObservedRoutePlanner(mob,target.blockPosition());assignment=null;legs=List.of();leg=0;failed=false;
  lastPosition=mob.position();lastProgress=mob.level().getGameTime();mob.getNavigation().stop();
 }
 @Override public boolean requiresUpdateEveryTick(){return true;}
 @Override public boolean canContinueToUse(){
  return !failed&&!mob.isNoAi()&&target!=null&&target.isAlive()&&target==mob.getTarget()
   &&mob.getSensing().hasLineOfSight(target)&&mob.distanceToSqr(target)>9&&LivingFormation.assignment(mob)==null
   &&(assignment==null||mob.level().getGameTime()<assignment.expires()&&target.blockPosition().distSqr(assignment.objective())<=64);
 }
 @Override public void tick(){
  if(failed)return;
  long now=mob.level().getGameTime();
  if(NativeRouteSearch.crossesClosedGate((ServerLevel)mob.level(),legs,leg)){failed=true;mob.getNavigation().stop();legs=List.of();return;}
  if(assignment==null){
   mob.getNavigation().stop();planner.advance();
   if(planner.done()){
    assignment=planner.result().orElse(null);if(assignment==null){failed=true;return;}
    lastRoute=assignment.route();legs=new ArrayList<>(assignment.paths());if(legs.isEmpty()){failed=true;return;}
    lastProgress=now;lastPosition=null;
   }
   return;
  }
  if(lastPosition==null){
   var fresh=NativeRouteSearch.find(mob,mob.blockPosition(),legs.get(leg).getTarget());
   if(fresh.status()==NativeRouteSearch.Status.DEFERRED){if(now-lastProgress>100)failed=true;return;}
   if(fresh.status()!=NativeRouteSearch.Status.FOUND){failed=true;return;}
   legs.set(leg,fresh.path());mob.getNavigation().moveTo(fresh.path(),1.05);lastProgress=now;lastPosition=mob.position();return;
  }
  if(mob.position().distanceToSqr(lastPosition)>.25){lastProgress=now;lastPosition=mob.position();}
  if(now-lastProgress>60){failed=true;return;}
  if(mob.getNavigation().isDone()){
   var endpoint=legs.get(leg).getTarget();
   if(mob.distanceToSqr(endpoint.getX()+.5,endpoint.getY(),endpoint.getZ()+.5)>4){failed=true;return;}
   if(++leg>=legs.size()){failed=true;return;}
   lastPosition=null;lastProgress=now;
  }
 }
 @Override public void stop(){mob.getNavigation().stop();planner=null;assignment=null;target=null;legs=List.of();nextAttempt=mob.level().getGameTime()+100+Math.floorMod(mob.getId(),40);}
}
