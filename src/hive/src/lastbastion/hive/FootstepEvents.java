package lastbastion.hive;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import java.util.Map;
import java.util.WeakHashMap;

/** Server-observed travel, not key presses. Sneaking is a deliberate silent counterplay. */
public final class FootstepEvents {
 private record Sample(ServerLevel level,Vec3 position,long tick){}
 private static final Map<Player,Sample> LAST=new WeakHashMap<>();
 private FootstepEvents(){}
 @SubscribeEvent
 public static void moved(PlayerTickEvent.Post event){
  var player=event.getEntity();if(!(player.level() instanceof ServerLevel level))return;
  long now=level.getGameTime();var old=LAST.get(player);
  if(old!=null&&old.level==level&&now>=old.tick&&now-old.tick<5)return;
  LAST.put(player,new Sample(level,player.position(),now));
  if(old==null||old.level!=level||now<old.tick||now-old.tick>20||!player.isAlive()||player.isSpectator()||player.isCreative()||player.isShiftKeyDown())return;
  double travel=player.position().distanceToSqr(old.position);
  if(travel<0.04||travel>64)return; // stationary or teleport, not a footstep
  if(!player.onGround()&&!player.isInWater())return;
  WorldHearing.emit(level,player.getUUID(),player.position(),player.isSprinting()?16:5);
 }
}
