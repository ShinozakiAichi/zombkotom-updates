package lastbastion.mixin;

import java.util.Set;
import net.smileycorp.hordes.hordeevent.HordeSpawnData;
import net.minecraft.world.entity.Mob;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Preserve conversion membership when the horde owner has disconnected.
 * The conversion caller already copied the owner UUID to the new mob.
 * Do not construct a tracking goal with a null target.
 */
@Mixin(targets = "net.smileycorp.hordes.hordeevent.capability.HordeEvent", remap = false)
public abstract class HordeOfflineOwnerMixin {
    @Shadow private Set<Mob> entitiesSpawned;
    @Shadow private HordeSpawnData spawnData;
    @Shadow public abstract boolean isActive();

    @Inject(method = "registerEntity", at = @At("HEAD"), cancellable = true, require = 1)
    private void lastbastion$offlineConversion(Mob entity, ServerPlayer player, CallbackInfo ci) {
        if (player != null || !isActive() || spawnData == null) return;
        entitiesSpawned.add(entity);
        ci.cancel();
    }
}
