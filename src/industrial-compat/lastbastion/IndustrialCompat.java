package lastbastion;

import java.util.ArrayList;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.ChunkDataEvent;

/** Prunes records only where the final block cannot own a block entity. */
@Mod("lastbastion_compat")
public final class IndustrialCompat {
    public IndustrialCompat() { NeoForge.EVENT_BUS.addListener(this::load); }
    private void load(ChunkDataEvent.Load event) { clean(event.getChunk()); }
    public static int clean(ChunkAccess chunk) {
        int removed = 0;
        for (var pos : new ArrayList<>(chunk.getBlockEntitiesPos())) {
            // Lost Cities bulk section writes bypass the ordinary block replacement
            // cleanup. Stone is just as incapable of owning a chest/spawner as air.
            // Never inspect or replace inventories belonging to entity-bearing blocks.
            if (!chunk.getBlockState(pos).hasBlockEntity()) {
                ((lastbastion.mixin.PendingBlockEntitiesAccessor)chunk).lastbastion$pending().remove(pos);
                chunk.removeBlockEntity(pos);
                removed++;
            }
        }
        if (removed > 0) {
            chunk.setUnsaved(true);
            org.slf4j.LoggerFactory.getLogger("LastBastionCompat").info(
                "Removed {} orphan block entity records at {} (non-entity blocks only)", removed, chunk.getPos());
        }
        return removed;
    }
}
