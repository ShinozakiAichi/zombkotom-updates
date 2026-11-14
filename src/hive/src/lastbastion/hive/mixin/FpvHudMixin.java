package lastbastion.hive.mixin;

import de.nikpottbecker.fpvcraft.client.PilotController;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Preserve sidebar preferences while giving the native FPV telemetry its space. */
@Mixin(targets = "lastbastion.client.BastionHud", remap = false)
public abstract class FpvHudMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true, remap = false)
    private static void hive$hideSidebarDuringFlight(CallbackInfo ci) {
        if (PilotController.INSTANCE.isActive()) ci.cancel();
    }
}
