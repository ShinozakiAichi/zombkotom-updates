package lastbastion.hive;
import de.nikpottbecker.fpvcraft.entity.DroneEntity;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/** Armed rotors are an observed moving distraction, not knowledge of the pilot's position. */
public final class FpvNoiseEvents {
 private FpvNoiseEvents(){}
 @SubscribeEvent public static void tick(EntityTickEvent.Post event){
  if(!(event.getEntity() instanceof DroneEntity drone)||!(drone.level() instanceof ServerLevel level)
    ||!drone.isAlive()||!drone.isArmed()||Math.floorMod(level.getGameTime()+drone.getId(),20)!=0)return;
  WorldHearing.emit(level,drone.getUUID(),drone.position(),24);
 }
}
