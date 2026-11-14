package lastbastion.hive;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.goal.Goal;

/** Records only the current visibly observed player; never searches through walls. */
public final class ObservePlayerGoal extends Goal {
 private final Zombie zombie;
 private long nextObservation;
 private long nextSighting,nextRoute;
 private java.util.UUID seenPlayer;
 private java.util.UUID seenVehicle;
 private net.minecraft.world.phys.Vec3 seenPosition;
 private long seenAt;
 private final ApproachTrail approach=new ApproachTrail();
 public ObservePlayerGoal(Zombie zombie){this.zombie=zombie;}
 @Override public boolean canUse(){return !zombie.isNoAi()&&zombie.level() instanceof ServerLevel;}
 @Override public boolean canContinueToUse(){return canUse();}
 @Override public void tick(){
  var level=(ServerLevel)zombie.level();long now=level.getGameTime();
  if(now<nextObservation)return;
  nextObservation=now+40+Math.floorMod(zombie.getId(),20);
  if(!(zombie.getTarget() instanceof Player player)||!player.isAlive()||player.isCreative()||player.isSpectator()
      ||zombie.distanceToSqr(player)>48*48||!zombie.getSensing().hasLineOfSight(player)){seenPlayer=null;seenPosition=null;seenVehicle=null;approach.reset();return;}
  var own=zombie.blockPosition();
  for(var point:approach.observe(player.getUUID(),new ApproachTrail.Point(own.getX(),own.getY(),own.getZ()),zombie.distanceToSqr(player),now))
   HiveSavedData.get(level).observe(new HiveMemory.Key(HiveMemory.Kind.SUCCESSFUL_ROUTE,point.x(),point.y(),point.z()),now,1);
  nextObservation=now+(zombie.distanceToSqr(player)<=24*24?10:30)+Math.floorMod(zombie.getId(),10);
  boolean same=player.getUUID().equals(seenPlayer)&&seenPosition!=null&&now>=seenAt&&now-seenAt<=100;
  var vehicle=player.getVehicle();var vehicleId=vehicle==null?null:vehicle.getUUID();
  if(same){
   EntranceDiscovery.observe(zombie,seenPosition,player.position());
   if(vehicleId!=null&&vehicleId.equals(seenVehicle)&&now>=nextRoute&&SupplyRouteObservation.observe(zombie,player,seenPosition,player.position()))nextRoute=now+40;
  }
  seenPlayer=player.getUUID();seenPosition=player.position();seenAt=now;
  seenVehicle=vehicleId;
  if(same&&now<nextSighting)return;
  nextSighting=now+60;
  var pos=player.blockPosition();
  // Four-block cells keep repeated nearby footsteps from exhausting memory.
  var key=new HiveMemory.Key(HiveMemory.Kind.SIGHTING,Math.floorDiv(pos.getX(),4)*4,pos.getY(),Math.floorDiv(pos.getZ(),4)*4);
  HiveSavedData.get(level).observe(key,now,1);
 }
 @Override public void stop(){approach.reset();seenPlayer=null;seenPosition=null;seenVehicle=null;}
}
