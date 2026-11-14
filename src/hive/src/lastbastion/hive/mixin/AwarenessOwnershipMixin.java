package lastbastion.hive.mixin;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Zombie;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
/** Hive owns zombie sensing; other mob classes retain Zombie Awareness behavior. */
@Mixin(value=com.corosus.zombieawareness.ZombieAwareness.class,remap=false)
public abstract class AwarenessOwnershipMixin {
 @Inject(method="canEntityBeProcessedOverride",at=@At("HEAD"),cancellable=true,remap=false)
 private static void hive$ownsSenses(Entity entity,CallbackInfoReturnable<Boolean> cir){
  if(entity instanceof Zombie)cir.setReturnValue(false);
 }
}
