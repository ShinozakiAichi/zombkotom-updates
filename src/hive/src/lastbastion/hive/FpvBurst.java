package lastbastion.hive;
import de.nikpottbecker.fpvcraft.entity.DroneEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import java.util.*;
public final class FpvBurst {
 private record Pending(UUID pilot,BurstSequence sequence){}
 private static final Map<DroneEntity,Pending> PENDING=new WeakHashMap<>();
 private FpvBurst(){}
 public static boolean request(DroneEntity drone,ServerPlayer player){
  if(PENDING.containsKey(drone)||!FpvPetalRack.release(drone,player))return false;
  int remaining=Math.min(4,FpvPetalRack.state(drone).ammo());
  if(remaining>0)PENDING.put(drone,new Pending(player.getUUID(),new BurstSequence(remaining,drone.level().getGameTime())));
  return true;
 }
 @SubscribeEvent public static void tick(EntityTickEvent.Post event){
  if(!(event.getEntity() instanceof DroneEntity drone)||!(drone.level() instanceof ServerLevel level))return;
  var task=PENDING.get(drone);if(task==null)return;
  var player=level.getServer().getPlayerList().getPlayer(task.pilot());
  if(player==null||!player.isAlive()||player.level()!=level||!drone.isArmed()||!drone.isPilotedBy(player)
    ||!task.sequence().advance(level.getGameTime(),()->FpvPetalRack.release(drone,player)))PENDING.remove(drone);
 }
 @SubscribeEvent public static void leave(EntityLeaveLevelEvent event){if(event.getEntity() instanceof DroneEntity drone)PENDING.remove(drone);}
}
