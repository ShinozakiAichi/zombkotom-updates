package lastbastion.hive.mixin;
import lastbastion.hive.FpvPetalRack;
import de.nikpottbecker.fpvcraft.entity.DroneEntity;
import de.nikpottbecker.fpvcraft.item.DroneItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.InteractionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value=DroneItem.class,remap=false)
public abstract class FpvRackItemMixin {
 @Inject(method="useOn",at=@At("HEAD"),cancellable=true,remap=false)
 private void hive$validatePlacement(UseOnContext context,CallbackInfoReturnable<InteractionResult> cir){
  var state=FpvPetalRack.itemState(context.getItemInHand());
  if(!state.valid()||(state.owner()!=null&&(context.getPlayer()==null||!state.allowed(context.getPlayer()))))cir.setReturnValue(InteractionResult.FAIL);
 }
 @Redirect(method="useOn",at=@At(value="INVOKE",target="Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"),remap=false)
 private boolean hive$restoreRack(Level level,Entity entity,UseOnContext context){
  if(entity instanceof DroneEntity drone)FpvPetalRack.restore(drone,context.getItemInHand());
  return level.addFreshEntity(entity);
 }
}
