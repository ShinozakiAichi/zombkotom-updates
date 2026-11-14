package lastbastion.hive.mixin;
import lastbastion.hive.LadderState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Zombie;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value=LivingEntity.class,remap=false)
public abstract class SupportPushMixin {
 @Inject(method="isPushable",at=@At("HEAD"),cancellable=true,remap=false)
 private void hive$brace(CallbackInfoReturnable<Boolean> cir){
  if((Object)this instanceof Zombie zombie&&LadderState.supports(zombie))cir.setReturnValue(false);
 }
}
