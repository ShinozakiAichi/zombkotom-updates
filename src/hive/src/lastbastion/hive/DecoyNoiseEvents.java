package lastbastion.hive;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.event.level.NoteBlockEvent;
import java.util.*;
import java.nio.charset.StandardCharsets;

/** Redstone note-block decoys use normal occlusion and perceived-strength ranking. */
public final class DecoyNoiseEvents {
 private static final Map<ServerLevel,LinkedHashMap<Long,Long>> LAST=new WeakHashMap<>();
 private DecoyNoiseEvents(){}
 public static UUID source(long position){return UUID.nameUUIDFromBytes(("note_decoy:"+position).getBytes(StandardCharsets.UTF_8));}
 @SubscribeEvent(priority=EventPriority.LOWEST)
 public static void play(NoteBlockEvent.Play event){
  if(event.isCanceled()||!(event.getLevel() instanceof ServerLevel level))return;
  long pos=event.getPos().asLong(),now=level.getGameTime();
  var entries=LAST.computeIfAbsent(level,l->new LinkedHashMap<>());var last=entries.get(pos);
  if(last!=null&&now>=last&&now-last<10)return;
  entries.remove(pos);entries.put(pos,now);
  while(entries.size()>1024)entries.remove(entries.keySet().iterator().next());
  WorldHearing.emit(level,source(pos),Vec3.atCenterOf(event.getPos()),48);
 }
}
