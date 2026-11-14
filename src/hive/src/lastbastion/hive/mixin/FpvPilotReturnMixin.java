package lastbastion.hive.mixin;

import de.nikpottbecker.fpvcraft.entity.DroneEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** FPVCraft 0.2.0's authorized stop packet passes null and skips native body return. */
@Mixin(value = DroneEntity.class, remap = false)
public abstract class FpvPilotReturnMixin {
    @ModifyVariable(method = "releasePilot", at = @At("HEAD"), argsOnly = true, ordinal = 0, remap = false)
    private ServerPlayer hive$resolveReturningPilot(ServerPlayer supplied) {
        var drone = (DroneEntity) (Object) this;
        if (supplied != null || !(drone.level() instanceof ServerLevel level)) return supplied;
        return drone.getPilot().map(level.getServer().getPlayerList()::getPlayer)
            .filter(player -> player.level() == level && player.isAlive()).orElse(null);
    }
}
