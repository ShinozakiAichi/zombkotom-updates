package lastbastion.hive;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.Container;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.BlockPos;
public final class SupplyRouteObservation {
 private SupplyRouteObservation(){}
 public static boolean transport(net.minecraft.world.entity.Entity vehicle){
  return vehicle instanceof Container||vehicle instanceof com.simibubi.create.content.contraptions.AbstractContraptionEntity;
 }
 public static boolean observe(Zombie observer,Player player,Vec3 before,Vec3 after){
  if(!(observer.level() instanceof ServerLevel level)||player.level()!=level||!player.isAlive()||player.isCreative()||player.isSpectator()
    ||!transport(player.getVehicle())||before.distanceToSqr(after)<0.25||before.distanceToSqr(after)>144
    ||observer.distanceToSqr(player)>48*48||!observer.getSensing().hasLineOfSight(player))return false;
  var pos=BlockPos.containing(before.lerp(after,.5));if(!level.hasChunkAt(pos))return false;
  var key=new HiveMemory.Key(HiveMemory.Kind.SUPPLY_ROUTE,Math.floorDiv(pos.getX(),4)*4,pos.getY(),Math.floorDiv(pos.getZ(),4)*4);
  HiveSavedData.get(level).observe(key,level.getGameTime(),1);return true;
 }
}
