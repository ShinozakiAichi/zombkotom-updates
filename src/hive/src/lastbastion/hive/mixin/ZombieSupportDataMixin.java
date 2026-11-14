package lastbastion.hive.mixin;
import lastbastion.hive.LadderState;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.network.syncher.SynchedEntityData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value=Zombie.class,remap=false)
public abstract class ZombieSupportDataMixin {
 @Inject(method="defineSynchedData",at=@At("TAIL"),remap=false)
 private void hive$supportData(SynchedEntityData.Builder builder,CallbackInfo ci){builder.define(LadderState.SUPPORT,false);}
}
