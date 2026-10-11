package lastbastion.mixin;
import com.google.gson.JsonElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import lastbastion.client.AnimationJsonRepair;

@Mixin(targets="software.bernie.geckolib.loading.json.typeadapter.BakedAnimationsAdapter",remap=false)
public abstract class AnimationJsonMixin {
 @ModifyVariable(method="deserialize(Lcom/google/gson/JsonElement;Ljava/lang/reflect/Type;Lcom/google/gson/JsonDeserializationContext;)Lsoftware/bernie/geckolib/loading/object/BakedAnimations;",at=@At("HEAD"),argsOnly=true,ordinal=0,remap=false)
 private JsonElement lastbastion$repairExportedVectors(JsonElement input){return AnimationJsonRepair.repairAnimations(input);}
}
