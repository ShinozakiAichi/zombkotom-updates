package lastbastion.hive.mixin;
import lastbastion.hive.observation.GeneratorNoise;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** This branch runs only after fuel, throttle, connected shaft, direction and overload checks. */
@Pseudo
@Mixin(targets="com.jesz.createdieselgenerators.content.diesel_engine.huge.HugeDieselEngineBlockEntity",remap=false)
public abstract class HugeDieselNoiseMixin {
 @Inject(method="tick",at=@At(value="FIELD",target="Lcom/jesz/createdieselgenerators/content/diesel_engine/huge/HugeDieselEngineBlockEntity;fuelDebt:F",opcode=Opcodes.PUTFIELD,ordinal=0,shift=At.Shift.AFTER),remap=false)
 private void hive$workingHugeDiesel(CallbackInfo ci){GeneratorNoise.emit((BlockEntity)(Object)this,112);}
}
