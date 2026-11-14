package lastbastion.hive.mixin;

import com.vicmatskiv.pointblank.feature.SoundFeature;
import lastbastion.hive.WorldHearing;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Use the accepted fire path and attachment-selected volume, not sound name guesses. */
@Mixin(value = SoundFeature.class, remap = false)
public abstract class PointBlankNoiseMixin {
    @Redirect(method = "playFireSound", at = @At(value = "INVOKE", target =
        "Lnet/minecraft/world/level/Level;playSound(Lnet/minecraft/world/entity/player/Player;DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V"), remap = false)
    private static void hive$hearAcceptedShot(Level world, Player shooter, double x, double y, double z,
                                             SoundEvent sound, SoundSource category, float volume, float pitch) {
        world.playSound(shooter, x, y, z, sound, category, volume, pitch);
        if (world instanceof ServerLevel level && shooter != null && Float.isFinite(volume) && volume > 0)
            WorldHearing.emit(level, shooter.getUUID(), new Vec3(x, y, z), Math.min(512, 96.0 * volume * volume));
    }
}
