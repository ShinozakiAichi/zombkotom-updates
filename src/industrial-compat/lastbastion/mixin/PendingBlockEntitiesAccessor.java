package lastbastion.mixin;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(value=ChunkAccess.class, remap=false)
public interface PendingBlockEntitiesAccessor {
    @Accessor("pendingBlockEntities")
    Map<BlockPos, CompoundTag> lastbastion$pending();
}
