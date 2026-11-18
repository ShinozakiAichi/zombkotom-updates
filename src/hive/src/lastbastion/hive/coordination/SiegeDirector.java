package lastbastion.hive.coordination;
import net.minecraft.server.level.ServerLevel;
import net.smileycorp.hordes.hordeevent.capability.HordeSavedData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import java.util.Map;
import java.util.WeakHashMap;

/** Observations schedule coordination, while Hordes alone owns spawning. No player positions are read. */
public final class SiegeDirector {
 private static final class World {
  final SiegeClock clock;long lastTick,observedUntil,nextHordePoll;boolean horde;
  World(long now){clock=new SiegeClock(now);lastTick=now;observedUntil=now;}
 }
 private static final Map<ServerLevel,World> WORLDS=new WeakHashMap<>();
 private SiegeDirector(){}
 private static World world(ServerLevel level){
  long now=level.getGameTime();var w=WORLDS.get(level);
  if(w==null||now<w.lastTick){w=new World(now);WORLDS.put(level,w);}w.lastTick=now;return w;
 }
 public static void observed(ServerLevel level){var w=world(level);w.observedUntil=level.getGameTime()+1200;advance(level,w);}
 public static SiegeClock.Phase phase(ServerLevel level){var w=WORLDS.get(level);return w==null?SiegeClock.Phase.QUIET:w.clock.phase();}
 public static boolean canRecruit(ServerLevel level){var p=phase(level);return p==SiegeClock.Phase.PREPARE||p==SiegeClock.Phase.ASSAULT;}
 private static void advance(ServerLevel level,World w){
  long now=level.getGameTime();
  if(now>=w.nextHordePoll){
   w.nextHordePoll=now+20;w.horde=false;
   if(!level.players().isEmpty()){
    var data=HordeSavedData.getData(level);int sampled=0;
    for(var player:level.players()){
     if(sampled++>=64)break;var horde=data.getEvent(player.getUUID());
     if(horde!=null&&horde.isActive()){w.horde=true;break;}
    }
   }
  }
  w.clock.advance(now,now<w.observedUntil,w.horde,Math.max(1,level.getDayTime()/24000+1));
 }
 @SubscribeEvent public static void tick(ServerTickEvent.Post event){
  for(var level:event.getServer().getAllLevels())if(WORLDS.containsKey(level))advance(level,world(level));
 }
 @SubscribeEvent public static void unloaded(LevelEvent.Unload event){if(event.getLevel() instanceof ServerLevel level)WORLDS.remove(level);}
}
