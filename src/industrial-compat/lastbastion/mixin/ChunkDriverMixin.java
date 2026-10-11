package lastbastion.mixin;

import net.minecraft.world.level.chunk.ChunkAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Lost Cities writes pending NBT before its damage pass writes final block states. */
@Mixin(targets="mcjty.lostcities.worldgen.ChunkDriver", remap=false)
public class ChunkDriverMixin {
    @Inject(method="actuallyGenerate", at=@At("RETURN"), require=1)
    private void lastbastion$pruneOrphans(ChunkAccess chunk, CallbackInfo ci) {
        lastbastion.IndustrialCompat.clean(chunk);
    }
}
