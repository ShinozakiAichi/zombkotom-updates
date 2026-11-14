package lastbastion.hive;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import java.util.UUID;

/** Server construction, mining and explosions; no client key-state guesses. */
public final class NoiseEvents {
    private NoiseEvents(){}
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void broken(BlockEvent.BreakEvent event){
        if(event.isCanceled()||!(event.getLevel() instanceof ServerLevel level))return;
        var player=event.getPlayer();if(player.isCreative()||player.isSpectator())return;
        WorldHearing.emit(level,player.getUUID(),Vec3.atCenterOf(event.getPos()),24);
    }
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void placed(BlockEvent.EntityPlaceEvent event){
        if(event.isCanceled()||!(event.getLevel() instanceof ServerLevel level)
            ||!(event.getEntity() instanceof net.minecraft.world.entity.player.Player player)
            ||player.isCreative()||player.isSpectator())return;
        // Quiet repairs remain possible, but replacing fortifications is not silent.
        WorldHearing.emit(level,player.getUUID(),Vec3.atCenterOf(event.getPos()),player.isShiftKeyDown()?3:12);
    }
    @SubscribeEvent
    public static void exploded(ExplosionEvent.Detonate event){
        if(event.getLevel() instanceof ServerLevel level)
            WorldHearing.emit(level,UUID.randomUUID(),event.getExplosion().center(),256);
    }
}
