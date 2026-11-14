package lastbastion.hive.client;
import de.nikpottbecker.fpvcraft.entity.DroneEntity;
import lastbastion.hive.FpvRackNetwork;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid="lastbastion_hive",value=Dist.CLIENT)
public final class FpvRackKeys {
 private static final KeyMapping RELEASE=new KeyMapping("key.lastbastion_hive.release",InputConstants.Type.KEYSYM,-1,"key.categories.lastbastion_hive");
 private static final KeyMapping BURST=new KeyMapping("key.lastbastion_hive.burst",InputConstants.Type.KEYSYM,-1,"key.categories.lastbastion_hive");
 @SubscribeEvent public static void tick(ClientTickEvent.Post event){
  var mc=Minecraft.getInstance();
  while(RELEASE.consumeClick()){
   if(mc.screen!=null||mc.player==null||mc.level==null)continue;
   for(var entity:mc.level.entitiesForRendering())if(entity instanceof DroneEntity drone&&drone.isPilotedBy(mc.player)){
    PacketDistributor.sendToServer(new FpvRackNetwork.Release(drone.getId()));break;
   }
  }
  while(BURST.consumeClick()){
   if(mc.screen!=null||mc.player==null||mc.level==null)continue;
   for(var entity:mc.level.entitiesForRendering())if(entity instanceof DroneEntity drone&&drone.isPilotedBy(mc.player)){
    PacketDistributor.sendToServer(new FpvRackNetwork.Burst(drone.getId()));break;
   }
  }
 }
 @EventBusSubscriber(modid="lastbastion_hive",value=Dist.CLIENT,bus=EventBusSubscriber.Bus.MOD)
 public static final class Registration {
  @SubscribeEvent public static void keys(RegisterKeyMappingsEvent event){event.register(RELEASE);event.register(BURST);}
 }
}
