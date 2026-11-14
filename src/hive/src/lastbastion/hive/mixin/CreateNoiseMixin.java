package lastbastion.hive.mixin;
import lastbastion.hive.IndustrialNoise;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value=KineticBlockEntity.class,remap=false)
public abstract class CreateNoiseMixin {
 @Inject(method="tick",at=@At("TAIL"),remap=false)
 private void hive$machineNoise(CallbackInfo ci){IndustrialNoise.tick((KineticBlockEntity)(Object)this);}
}
