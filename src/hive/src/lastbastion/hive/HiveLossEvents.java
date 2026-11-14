package lastbastion.hive;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.Zombie;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

public final class HiveLossEvents {
 private HiveLossEvents(){}
 @SubscribeEvent(priority=EventPriority.LOWEST)
 public static void died(LivingDeathEvent event){
  if(event.isCanceled()||!(event.getEntity() instanceof Zombie zombie)||!(zombie.level() instanceof ServerLevel level))return;
  var p=zombie.blockPosition();
  HiveSavedData.get(level).observe(new HiveMemory.Key(HiveMemory.Kind.DANGER,
   Math.floorDiv(p.getX(),4)*4,p.getY(),Math.floorDiv(p.getZ(),4)*4),level.getGameTime(),4);
 }
}
