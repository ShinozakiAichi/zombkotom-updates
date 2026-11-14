package lastbastion.hive;
import com.atsuishio.superbwarfare.entity.projectile.Blu43Entity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.*;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
public final class FpvMineLifecycle {
 public static final String MARKER="lastbastion_rack_mine";
 private FpvMineLifecycle(){}
 private static boolean managed(Entity entity){return entity instanceof Blu43Entity&&entity.getPersistentData().getBoolean(MARKER);}
 @SubscribeEvent public static void join(EntityJoinLevelEvent event){
  if(!(event.getLevel() instanceof ServerLevel level)||!managed(event.getEntity()))return;
  var mine=(Blu43Entity)event.getEntity();
  if(!FpvMineData.get(level).valid(mine.getUUID(),mine.getOwnerUUID(),FpvMineData.now(level)))event.setCanceled(true);
 }
 @SubscribeEvent public static void tick(EntityTickEvent.Pre event){
  if(!(event.getEntity().level() instanceof ServerLevel level)||!managed(event.getEntity()))return;
  var mine=(Blu43Entity)event.getEntity();
  if(Math.floorMod(level.getGameTime()+mine.getId(),20)==0&&!FpvMineData.get(level).valid(mine.getUUID(),mine.getOwnerUUID(),FpvMineData.now(level))){mine.discard();event.setCanceled(true);}
 }
 @SubscribeEvent public static void leave(EntityLeaveLevelEvent event){
  if(!(event.getLevel() instanceof ServerLevel level)||!managed(event.getEntity()))return;
  var reason=event.getEntity().getRemovalReason();
  if(reason==Entity.RemovalReason.KILLED||reason==Entity.RemovalReason.DISCARDED)FpvMineData.get(level).remove(event.getEntity().getUUID());
 }
}
