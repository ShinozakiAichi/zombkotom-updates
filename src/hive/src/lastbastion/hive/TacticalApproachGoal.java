package lastbastion.hive;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.BlockPos;
import java.util.EnumSet;

/** Brief visible-contact maneuvers followed by normal melee, never perpetual strafing. */
public final class TacticalApproachGoal extends Goal {
 private final Zombie zombie;
 private LivingEntity target;
 private Path path;
 private long nextAttempt,deadline;
 private boolean feint;
 private String decision="idle";
 public TacticalApproachGoal(Zombie zombie){this.zombie=zombie;setFlags(EnumSet.of(Flag.MOVE));}
 @Override public boolean canUse(){
  if(zombie.isNoAi()||!(zombie.level() instanceof ServerLevel level))return false;
  long now=level.getGameTime();if(now<nextAttempt)return false;
  long night=Math.max(1,level.getDayTime()/24000+1);
  if(night<15||Math.floorMod(zombie.getUUID().hashCode(),100)>=Math.min(55,(night-10)/2))return false;
  target=zombie.getTarget();
  if(target==null||!target.isAlive()||!zombie.getSensing().hasLineOfSight(target))return false;
  double distance=zombie.distanceToSqr(target);if(distance<36||distance>784)return false;
  if(!zombie.onGround()&&!zombie.isInWater())return false;
  nextAttempt=now+5+Math.floorMod(zombie.getId(),5);
  int role=Math.floorMod(zombie.getUUID().hashCode(),3);
  feint=role==2&&night>=40&&zombie.getHealth()<zombie.getMaxHealth()*0.75;
  Vec3 direction=target.position().subtract(zombie.position()).multiply(1,0,1).normalize();
  Vec3 point;
  if(feint)point=zombie.position().subtract(direction.scale(5));
  else if(role==1){
   Vec3 velocity=target.getDeltaMovement().multiply(1,0,1);
   if(velocity.lengthSqr()>0.16)velocity=velocity.normalize().scale(0.4);
   point=target.position().add(velocity.scale(12));
  }else{
   double side=(zombie.getUUID().getLeastSignificantBits()&1)==0?1:-1;
   point=zombie.position().lerp(target.position(),0.65).add(-direction.z*5*side,0,direction.x*5*side);
  }
  if(!level.hasChunkAt(BlockPos.containing(point))||!PatrolPathBudget.reserve(level)){decision="chunk_or_budget";return false;}
  path=zombie.getNavigation().createPath(BlockPos.containing(point),1);
  decision="requested="+BlockPos.containing(point)+" path="+(path==null?"null":path.getTarget()+" reach="+path.canReach()+" end="+path.getEndNode());
  if(path==null||!path.canReach())return false;
  deadline=now+(feint?100:70);return true;
 }
 @Override public void start(){decision="started "+decision;zombie.getNavigation().moveTo(path,feint?1.05:1.15);}
 @Override public boolean canContinueToUse(){
  return target!=null&&target==zombie.getTarget()&&target.isAlive()&&!zombie.isNoAi()
   &&zombie.level().getGameTime()<deadline&&!zombie.getNavigation().isDone()
   &&zombie.distanceToSqr(target)>9&&zombie.getSensing().hasLineOfSight(target);
 }
 @Override public void stop(){
  zombie.getNavigation().stop();target=null;path=null;
  nextAttempt=zombie.level().getGameTime()+180+Math.floorMod(zombie.getId(),80);
 }
}
