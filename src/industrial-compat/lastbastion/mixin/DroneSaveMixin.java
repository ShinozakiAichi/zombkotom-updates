package lastbastion.mixin;

import com.atsuishio.superbwarfare.entity.vehicle.DroneEntity;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** SBW 0.8.9.2 checks a newly empty tag rather than currentItem before saving it. */
@Mixin(value=DroneEntity.class, remap=false)
public class DroneSaveMixin {
    @Inject(method="addAdditionalSaveData", at=@At("TAIL"), require=1)
    private void lastbastion$savePayload(CompoundTag tag, CallbackInfo ci) {
        var drone=(DroneEntity)(Object)this;
        var item=drone.getCurrentItem();
        tag.put("Item",item.isEmpty()?new CompoundTag():item.save(drone.level().registryAccess()));
        // The corresponding read method expects a compound, not its SNBT string.
        tag.put("DisplayEntityTag",drone.getEntityData().get(DroneEntity.DISPLAY_ENTITY_TAG).copy());
    }
}
