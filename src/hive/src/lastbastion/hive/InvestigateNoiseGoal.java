package lastbastion.hive;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import java.util.EnumSet;
import java.util.UUID;
import java.util.List;
import java.util.Objects;
import java.util.ArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.pathfinder.Path;
import lastbastion.hive.routing.*;
import lastbastion.hive.coordination.CohortPolicy;

/** Investigate an expiring heard position; the smart subset evaluates observed route costs. */
public final class InvestigateNoiseGoal extends Goal {
    private final Zombie mob;
    private long nextListen, expires;
    private UUID source;
    private Vec3 destination;
    private int pathCooldown;
    private boolean smart,running;
    private ObservedRoutePlanner planner;
    private List<Path> legs=List.of();private int leg;
    private long nextRoute,lastProgress;private Vec3 lastPosition;
    private RouteGraph.Route lastRoute;
    public RouteGraph.Route lastRoute(){return lastRoute;}
    public InvestigateNoiseGoal(Zombie mob){this.mob=mob;setFlags(EnumSet.of(Flag.MOVE));}
    private boolean visibleThreat(){return mob.getTarget()!=null&&mob.getTarget().isAlive()&&mob.getSensing().hasLineOfSight(mob.getTarget());}
    private boolean listen(){
        var level=(ServerLevel)mob.level();long now=level.getGameTime();
        if(now<nextListen)return false;
        nextListen=now+20+Math.floorMod(mob.getId(),10);
        var heard=WorldHearing.listen(level,mob.getEyePosition(),source);
        if(heard.isEmpty())return false;
        var noise=heard.get().noise();var p=noise.position();
        var heardPosition=new Vec3(p.x(),p.y(),p.z());
        boolean changed=destination==null||!Objects.equals(source,noise.source())||destination.distanceToSqr(heardPosition)>4;
        source=noise.source();destination=heardPosition;expires=noise.tick()+200;
        if(changed){clearRoute();nextRoute=now;}
        return mob.position().distanceToSqr(destination)>4;
    }
    @Override public boolean canUse(){return !mob.isNoAi()&&mob.level() instanceof ServerLevel&&!visibleThreat()&&listen();}
    @Override public void start(){
        running=true;pathCooldown=0;nextRoute=0;clearRoute();
        smart=CohortPolicy.routeEligible(mob.getUUID(),Math.max(1,mob.level().getDayTime()/24000+1));
    }
    @Override public boolean requiresUpdateEveryTick(){return true;}
    @Override public boolean canContinueToUse(){return mob.isAlive()&&!mob.isNoAi()&&!visibleThreat()&&destination!=null&&mob.level().getGameTime()<expires&&mob.position().distanceToSqr(destination)>4;}
    @Override public void tick(){
        listen();
        if(smart){followObservedRoute();return;}
        if(--pathCooldown<=0&&destination!=null&&PatrolPathBudget.reserve((ServerLevel)mob.level(),mob.getUUID())){
            pathCooldown=20+Math.floorMod(mob.getId(),10);
            mob.getNavigation().moveTo(destination.x,destination.y,destination.z,1.0);
        }
    }
    private void clearRoute(){planner=null;legs=List.of();leg=0;if(running)mob.getNavigation().stop();}
    private void retry(long now){clearRoute();nextRoute=now+40;}
    private void followObservedRoute(){
        long now=mob.level().getGameTime();
        if(destination==null||now>=expires){clearRoute();return;}
        if(NativeRouteSearch.crossesClosedGate((ServerLevel)mob.level(),legs,leg)){retry(now);return;}
        if(planner==null&&legs.isEmpty()){
            if(now<nextRoute)return;
            var local=LocalRouteLeg.toward(new LocalRouteLeg.Position(mob.getX(),mob.getY(),mob.getZ()),
                new LocalRouteLeg.Position(destination.x,destination.y,destination.z));
            planner=new ObservedRoutePlanner(mob,BlockPos.containing(local.x(),local.y(),local.z()));
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
    @Override public void stop(){clearRoute();running=false;destination=null;source=null;}
}
