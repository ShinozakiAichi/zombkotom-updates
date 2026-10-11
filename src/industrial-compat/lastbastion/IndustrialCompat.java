package lastbastion;

import java.util.ArrayList;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.ChunkDataEvent;

/** Removes only impossible, empty-space spawner records, never actual blocks. */
@Mod("lastbastion_compat")
public final class IndustrialCompat {
    public IndustrialCompat() {
        NeoForge.EVENT_BUS.addListener(this::load);
    }
    private void load(ChunkDataEvent.Load event) { clean(event.getChunk()); }
    public static int clean(ChunkAccess chunk) {
        int removed = 0;
        for (var pos : new ArrayList<>(chunk.getBlockEntitiesPos())) {
            var tag = chunk.getBlockEntityNbt(pos);
            if (tag != null && "minecraft:mob_spawner".equals(tag.getString("id"))
                    && chunk.getBlockState(pos).isAir()) {
                ((lastbastion.mixin.PendingBlockEntitiesAccessor)chunk).lastbastion$pending().remove(pos);
                chunk.removeBlockEntity(pos);
                removed++;
            }
        }
        if (removed > 0) {
            chunk.setUnsaved(true);
            org.slf4j.LoggerFactory.getLogger("LastBastionCompat").info(
                "Removed {} orphan spawner records at {} (air only)", removed, chunk.getPos());
        }
        return removed;
    }
}
