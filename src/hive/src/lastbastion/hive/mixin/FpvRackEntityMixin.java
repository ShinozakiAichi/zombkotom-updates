package lastbastion.hive.mixin;
import lastbastion.hive.FpvPetalRack;
import de.nikpottbecker.fpvcraft.entity.DroneEntity;
import de.nikpottbecker.fpvcraft.item.DroneItem;
import de.nikpottbecker.fpvcraft.physics.build.DroneBuild;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value=DroneEntity.class,remap=false)
public abstract class FpvRackEntityMixin {
 @Inject(method="interact",at=@At("HEAD"),cancellable=true,remap=false)
 private void hive$rackInteraction(Player player,InteractionHand hand,CallbackInfoReturnable<InteractionResult> cir){
  var drone=(DroneEntity)(Object)this;if(drone.level().isClientSide())return;
  if(!FpvPetalRack.state(drone).allowed(player)){cir.setReturnValue(InteractionResult.FAIL);return;}
  var stack=player.getItemInHand(hand);
  if(FpvPetalRack.isMine(stack))cir.setReturnValue(FpvPetalRack.load(drone,player,stack)?InteractionResult.CONSUME:InteractionResult.FAIL);
 }
 @Redirect(method={"interact","hurt","getPickResult"},at=@At(value="INVOKE",target="Lde/nikpottbecker/fpvcraft/item/DroneItem;stackOf(Lde/nikpottbecker/fpvcraft/physics/build/DroneBuild;)Lnet/minecraft/world/item/ItemStack;"),remap=false)
 private ItemStack hive$preserveRack(DroneBuild build){return FpvPetalRack.preserve((DroneEntity)(Object)this,DroneItem.stackOf(build));}
}
