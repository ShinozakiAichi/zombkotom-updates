package lastbastion.hive.mixin;
import lastbastion.hive.observation.GeneratorNoise;
import com.jesz.createdieselgenerators.content.diesel_engine.IEngine;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets={"com.jesz.createdieselgenerators.content.diesel_engine.normal.DieselEngineBlockEntity",
 "com.jesz.createdieselgenerators.content.diesel_engine.modular.ModularDieselEngineBlockEntity"},remap=false)
public abstract class DieselNoiseMixin {
 @Inject(method="tick",at=@At("TAIL"),remap=false)
 private void hive$workingDiesel(CallbackInfo ci){GeneratorNoise.diesel((GeneratingKineticBlockEntity)(Object)this,(IEngine)(Object)this);}
}
