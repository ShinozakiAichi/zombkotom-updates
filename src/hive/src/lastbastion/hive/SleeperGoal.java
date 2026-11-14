package lastbastion.hive;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import java.util.EnumSet;

/** Physical sheltered sleepers. Waking uses local sight, actual damage or perceived sound. */
public final class SleeperGoal extends Goal {
 private final Zombie zombie;
 private long nextAttempt,until,nextSense;
 private float health;
 private boolean awake;
 public SleeperGoal(Zombie zombie){this.zombie=zombie;setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK,Flag.JUMP));}
 public static int percentage(long night){return night<20?0:(int)Math.min(20,2+(night-20)/4);}
 private boolean eligible(){return zombie.isAlive()&&!zombie.isNoAi()&&!zombie.isBaby()&&zombie.onGround()
  &&!zombie.isInWater()&&!zombie.isOnFire()&&LivingFormation.assignment(zombie)==null;}
 private boolean stimulus(ServerLevel level){
  if(zombie.hurtTime>0||zombie.getHealth()<health)return true;
  for(var player:level.getEntitiesOfClass(Player.class,zombie.getBoundingBox().inflate(8),
    p->p.isAlive()&&!p.isCreative()&&!p.isSpectator())){
   if(zombie.distanceToSqr(player)<=64&&zombie.getSensing().hasLineOfSight(player)){
    zombie.setTarget(player);return true;
   }
  }
  var target=zombie.getTarget();
  if(target!=null&&target.isAlive()&&zombie.distanceToSqr(target)<=64&&zombie.getSensing().hasLineOfSight(target))return true;
  return WorldHearing.listen(level,zombie.getEyePosition(),null).map(h->h.score()>=0.04).orElse(false);
 }
 @Override public boolean canUse(){
  if(!(zombie.level() instanceof ServerLevel level)||!eligible()||level.getGameTime()<nextAttempt)return false;
  long now=level.getGameTime();nextAttempt=now+100+Math.floorMod(zombie.getId(),80);
  if(Math.floorMod(zombie.getUUID().hashCode(),100)>=percentage(Math.max(1,level.getDayTime()/24000+1))
    ||level.canSeeSky(zombie.blockPosition())||zombie.getTarget()!=null)return false;
  health=zombie.getHealth();return !stimulus(level);
 }
 @Override public void start(){
  long now=zombie.level().getGameTime();until=now+1200+Math.floorMod(zombie.getUUID().hashCode(),1200);
  nextSense=now;awake=false;health=zombie.getHealth();zombie.getNavigation().stop();zombie.addTag("lb_hive_dormant");
 }
 @Override public boolean requiresUpdateEveryTick(){return true;}
 @Override public boolean canContinueToUse(){return eligible()&&!awake&&zombie.getHealth()>=health&&zombie.hurtTime==0&&zombie.level().getGameTime()<until;}
 @Override public void tick(){
  var level=(ServerLevel)zombie.level();zombie.getNavigation().stop();
  zombie.setZza(0);zombie.setXxa(0);zombie.setJumping(false);
  if(level.getGameTime()>=nextSense){nextSense=level.getGameTime()+10;awake=stimulus(level);}
 }
 @Override public void stop(){zombie.removeTag("lb_hive_dormant");nextAttempt=zombie.level().getGameTime()+1200;}
}
