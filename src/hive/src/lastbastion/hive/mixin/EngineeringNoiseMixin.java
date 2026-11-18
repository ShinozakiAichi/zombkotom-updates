package lastbastion.hive.mixin;
import lastbastion.hive.observation.GeneratorNoise;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.DieselGeneratorLogic;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Pseudo
@Mixin(targets="blusunrize.immersiveengineering.common.blocks.multiblocks.logic.DieselGeneratorLogic",remap=false)
public abstract class EngineeringNoiseMixin {
 @Inject(method="tickServer",at=@At("RETURN"),remap=false)
 private void hive$workingEngineering(IMultiblockContext<DieselGeneratorLogic.State> context,CallbackInfo ci){
  if(!context.getState().isActive()||!context.isValid().getAsBoolean())return;
  var world=context.getLevel();
  if(world.getRawLevel() instanceof ServerLevel level)GeneratorNoise.emit(level,world.getAbsoluteOrigin(),112);
 }
}
