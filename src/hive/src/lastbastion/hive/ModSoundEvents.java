package lastbastion.hive;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.event.PlayLevelSoundEvent;
import java.util.UUID;
import java.nio.charset.StandardCharsets;

/** Captures sounds actually broadcast by mods on the server. Client-only loops need separate integration. */
public final class ModSoundEvents {
 private ModSoundEvents(){}
 @SubscribeEvent(priority=EventPriority.LOWEST)
 public static void sounded(PlayLevelSoundEvent event){
  if(event.isCanceled()||event.getSound()==null||!(event.getLevel() instanceof ServerLevel level))return;
  var id=event.getSound().value().getLocation();String namespace=id.getNamespace(),name=id.getPath();
  // Point Blank is captured at its accepted fire sound call, including attachment volume.
  boolean gun=(namespace.equals("superbwarfare")||namespace.equals("scguns"))
   &&(name.contains("fire")||name.contains("shoot")||name.contains("shot"));
  boolean industry=namespace.equals("create")||namespace.equals("createbigcannons")||namespace.equals("createnewage");
  if(!gun&&!industry)return;
  double volume=event.getNewVolume();if(!Double.isFinite(volume)||volume<=0)return;
  Vec3 pos;UUID source;
  if(event instanceof PlayLevelSoundEvent.AtEntity at){pos=at.getEntity().position();source=at.getEntity().getUUID();}
  else if(event instanceof PlayLevelSoundEvent.AtPosition at){
   pos=at.getPosition();source=UUID.nameUUIDFromBytes((id+":"+(int)Math.floor(pos.x)+":"+(int)Math.floor(pos.y)+":"+(int)Math.floor(pos.z)).getBytes(StandardCharsets.UTF_8));
  }else return;
  WorldHearing.emit(level,source,pos,Math.min(512,(gun?96:32)*volume*volume));
 }
}
