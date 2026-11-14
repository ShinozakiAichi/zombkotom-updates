package lastbastion.hive;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.Zombie;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;

/** An unloaded/dead support cannot hold up a formation through stale assignments. */
public final class FormationLifecycle {
 private FormationLifecycle(){}
 @SubscribeEvent public static void leave(EntityLeaveLevelEvent event){
  if(!(event.getLevel() instanceof ServerLevel level)||!(event.getEntity() instanceof Zombie zombie))return;
  var assignment=LivingFormation.assignment(zombie);
  if(assignment!=null)LivingFormation.release(level,assignment.group());
  LadderState.setSupport(zombie,false);
 }
}
