package lastbastion.hive;
import de.nikpottbecker.fpvcraft.entity.DroneEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@EventBusSubscriber(modid="lastbastion_hive",bus=EventBusSubscriber.Bus.MOD)
public final class FpvRackNetwork {
 public record Release(int entityId) implements CustomPacketPayload {
  public static final Type<Release> TYPE=new Type<>(ResourceLocation.parse("lastbastion_hive:release"));
  public static final StreamCodec<RegistryFriendlyByteBuf,Release> CODEC=StreamCodec.of((b,p)->b.writeVarInt(p.entityId),b->new Release(b.readVarInt()));
  public Type<? extends CustomPacketPayload> type(){return TYPE;}
 }
 public static boolean handle(ServerPlayer player,int entityId){
  var entity=player.serverLevel().getEntity(entityId);
  return entity instanceof DroneEntity drone&&FpvPetalRack.release(drone,player);
 }
 public record Burst(int entityId) implements CustomPacketPayload {
  public static final Type<Burst> TYPE=new Type<>(ResourceLocation.parse("lastbastion_hive:burst"));
  public static final StreamCodec<RegistryFriendlyByteBuf,Burst> CODEC=StreamCodec.of((b,p)->b.writeVarInt(p.entityId),b->new Burst(b.readVarInt()));
  public Type<? extends CustomPacketPayload> type(){return TYPE;}
 }
 @SubscribeEvent public static void register(RegisterPayloadHandlersEvent event){
  event.registrar("1").playToServer(Burst.TYPE,Burst.CODEC,(payload,context)->context.enqueueWork(()->{
   if(context.player() instanceof ServerPlayer player&&player.serverLevel().getEntity(payload.entityId()) instanceof DroneEntity drone)FpvBurst.request(drone,player);
  }));
  event.registrar("1").playToServer(Release.TYPE,Release.CODEC,(payload,context)->context.enqueueWork(()->{
   if(context.player() instanceof ServerPlayer player)handle(player,payload.entityId());
  }));
 }
}
