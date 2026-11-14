package lastbastion.hive.mixin;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Zombie;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
/** Preserve wave spawning, but do not give hive zombies live omniscient coordinates. */
@Mixin(value=net.smileycorp.hordes.common.ai.HordeTrackPlayerGoal.class,remap=false)
public abstract class HordeNavigationMixin {
 @Shadow @Final protected Mob entity;
 @Inject(method={"canUse","canContinueToUse"},at=@At("HEAD"),cancellable=true,remap=false)
 private void hive$ownsNavigation(CallbackInfoReturnable<Boolean> cir){
  if(entity instanceof Zombie)cir.setReturnValue(false);
 }
}
