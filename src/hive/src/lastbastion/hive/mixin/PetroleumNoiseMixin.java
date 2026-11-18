package lastbastion.hive.mixin;
import lastbastion.hive.observation.GeneratorNoise;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
/** Native active state requires fuel, enabled redstone control and room for generated energy. */
@Pseudo
@Mixin(targets="flaxbeard.immersivepetroleum.common.blocks.tileentities.GasGeneratorTileEntity",remap=false)
public abstract class PetroleumNoiseMixin {
 @Shadow protected boolean isActive;
 @Inject(method="tickServer",at=@At("RETURN"),remap=false)
 private void hive$workingPetroleum(CallbackInfo ci){if(isActive)GeneratorNoise.emit((BlockEntity)(Object)this,88);}
}
