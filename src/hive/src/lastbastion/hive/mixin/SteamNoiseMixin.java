package lastbastion.hive.mixin;
import lastbastion.hive.observation.GeneratorNoise;
import com.simibubi.create.content.kinetics.steamEngine.SteamEngineBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=SteamEngineBlockEntity.class,remap=false)
public abstract class SteamNoiseMixin {
 // The native server branch returns before the final client-only return.
 @Inject(method="tick",at=@At("RETURN"),remap=false)
 private void hive$workingSteam(CallbackInfo ci){GeneratorNoise.steam((SteamEngineBlockEntity)(Object)this);}
}
