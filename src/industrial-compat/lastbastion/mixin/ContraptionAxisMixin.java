package lastbastion.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Create 6.0.10: exact-centre overlaps can leave either response axis unset. */
@Mixin(targets="com.simibubi.create.foundation.collision.ContinuousOBBCollider", remap=false)
public abstract class ContraptionAxisMixin {
    // Guard only the six response-vector reads. Keep reset/separate null sentinels
    // and every valid axis intact, so ordinary collision calculations are unchanged.
    @ModifyExpressionValue(method="collideMany", at=@At(value="FIELD", target="Lcom/simibubi/create/foundation/collision/ContinuousOBBCollider$ContinuousSeparationManifold;axis:Lnet/minecraft/world/phys/Vec3;", remap=false), require=3, expect=3)
    private static Vec3 lastbastion$axis(Vec3 axis) { return axis == null ? Vec3.ZERO : axis; }

    @ModifyExpressionValue(method="collideMany", at=@At(value="FIELD", target="Lcom/simibubi/create/foundation/collision/ContinuousOBBCollider$ContinuousSeparationManifold;normalAxis:Lnet/minecraft/world/phys/Vec3;", remap=false), require=3, expect=3)
    private static Vec3 lastbastion$normal(Vec3 axis) { return axis == null ? Vec3.ZERO : axis; }
}
