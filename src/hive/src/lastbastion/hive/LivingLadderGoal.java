package lastbastion.hive;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import java.util.EnumSet;

/** Travels and jumps under normal collision/gravity; position is never assigned here. */
public final class LivingLadderGoal extends Goal {
 private final Zombie zombie;
 private LivingFormation.Assignment assignment;
 private int jumpCooldown,pathCooldown;
 public LivingLadderGoal(Zombie zombie){this.zombie=zombie;setFlags(EnumSet.of(Flag.MOVE,Flag.JUMP));}
 @Override public boolean canUse(){assignment=LivingFormation.assignment(zombie);return !zombie.isNoAi()&&assignment!=null;}
 @Override public boolean canContinueToUse(){return zombie.isAlive()&&!zombie.isNoAi()&&LivingFormation.assignment(zombie)==assignment;}
 @Override public void start(){zombie.getNavigation().stop();jumpCooldown=0;pathCooldown=0;}
 @Override public boolean requiresUpdateEveryTick(){return true;}
 @Override public void tick(){
  var level=(ServerLevel)zombie.level();var group=assignment.group();var slot=group.slots().get(assignment.index());
  if(--jumpCooldown<0)jumpCooldown=0;
  if(slot.tier()>0&&(!LivingFormation.ready(level,group,slot.leftSupport())||!LivingFormation.ready(level,group,slot.rightSupport()))){
   LadderState.setSupport(zombie,false);zombie.getNavigation().stop();
   if(LivingFormation.member(level,group,slot.leftSupport())==null||LivingFormation.member(level,group,slot.rightSupport())==null)LivingFormation.release(level,group);
   return;
  }
  double dx=slot.x()-zombie.getX(),dz=slot.z()-zombie.getZ(),dy=slot.y()-zombie.getY();
  if(dx*dx+dz*dz<0.012&&Math.abs(dy)<0.08&&zombie.onGround()){
   var target=zombie.getTarget();
   if(assignment.index()==group.slots().size()-1&&target!=null&&target.isAlive()
     &&zombie.getSensing().hasLineOfSight(target)&&Math.abs(target.getY()-zombie.getY())<1.5){
    LivingFormation.departApex(zombie);return;
   }
   zombie.getNavigation().stop();zombie.setDeltaMovement(0,zombie.getDeltaMovement().y,0);LadderState.setSupport(zombie,true);return;
  }
  LadderState.setSupport(zombie,false);
  Vec3 destination=new Vec3(slot.x(),slot.y(),slot.z());
  if(slot.tier()==0){
   var upper=group.slots().stream().filter(s->s.tier()==1).findFirst().orElseThrow();
   var left=group.slots().get(upper.leftSupport());var right=group.slots().get(upper.rightSupport());
   var across=new Vec3(right.x()-left.x(),0,right.z()-left.z()).normalize();
   var forward=new Vec3(upper.x()-(left.x()+right.x())/2,0,upper.z()-(left.z()+right.z())/2).normalize();
   // Align from behind the row, so an already braced neighbour does not block crossing.
   if(Math.abs(new Vec3(dx,0,dz).dot(across))>0.15)destination=destination.subtract(forward.scale(1.0));
  }
  if(dy>2.2){
   var step=slot;
   while(step.tier()>0&&step.y()-zombie.getY()>0.3)step=group.slots().get(step.leftSupport());
   // Stand on the exposed rear edge of a lower support before taking the next jump.
   var left=group.slots().get(slot.leftSupport());var right=group.slots().get(slot.rightSupport());
   var forward=new Vec3(slot.x()-(left.x()+right.x())/2,0,slot.z()-(left.z()+right.z())/2).normalize();
   destination=new Vec3(step.x(),step.y()+1.95,step.z()).subtract(forward.scale(0.43));
  }
  double horizontal=destination.subtract(zombie.position()).multiply(1,0,1).length();
  if(slot.tier()==0&&horizontal>2.5){
   if(--pathCooldown<=0&&PatrolPathBudget.reserve(level)){pathCooldown=20;zombie.getNavigation().moveTo(destination.x,destination.y,destination.z,0.8);}
   return;
  }
  zombie.getNavigation().stop();
  var direction=destination.subtract(zombie.position()).multiply(1,0,1);
  double speed=Math.min(0.12,direction.length()*0.25);
  var horizontalVelocity=direction.lengthSqr()>0.0001?direction.normalize().scale(speed):Vec3.ZERO;
  double vy=zombie.getDeltaMovement().y;
  if(destination.y-zombie.getY()>0.3&&zombie.onGround()&&jumpCooldown==0){vy=0.68;jumpCooldown=15;}
  zombie.setDeltaMovement(horizontalVelocity.x,vy,horizontalVelocity.z);
 }
 @Override public void stop(){zombie.getNavigation().stop();LadderState.setSupport(zombie,false);assignment=null;}
}
