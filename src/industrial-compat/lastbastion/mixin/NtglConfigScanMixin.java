package lastbastion.mixin;

import lastbastion.resources.ConfigScanResources;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** NTGL 3.2.0 rescans the same directory for every registry entry. */
@Mixin(targets="com.nukateam.ntgl.modules.datapack.ConfigUtils", remap=false)
public abstract class NtglConfigScanMixin {
    @ModifyVariable(method="getConfigMap", at=@At("HEAD"), argsOnly=true, ordinal=0, require=1)
    private static ResourceManager lastbastion$scanOnce(ResourceManager resources) {
        // Keep NTGL's item predicates, JSON parsing, overrides and error handling.
        return new ConfigScanResources(resources);
    }
}
