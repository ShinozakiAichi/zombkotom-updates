package lastbastion.hud;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.scores.*;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class BastionNetwork {
    public record Payload(BastionState state) implements CustomPacketPayload {
        public static final Type<Payload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath("lastbastion_compat","status"));
        public static final StreamCodec<RegistryFriendlyByteBuf,Payload> CODEC=StreamCodec.of((b,p)->{
            var s=p.state(); b.writeVarInt(s.night()); b.writeVarInt(s.stage()); b.writeVarInt(s.event());
            b.writeVarInt(s.daytime()); b.writeVarInt(s.online()); b.writeBoolean(s.valid()); b.writeBoolean(s.frozen());
        },b->new Payload(new BastionState(b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readBoolean(),b.readBoolean())));
        public Type<? extends CustomPacketPayload> type(){return TYPE;}
    }
    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").optional().playToClient(Payload.TYPE,Payload.CODEC,
            (payload,context)->context.enqueueWork(()->BastionState.accept(payload.state())));
    }
    private static int score(Scoreboard board,Objective objective,String name,int missing) {
        var value=board.getPlayerScoreInfo(ScoreHolder.forNameOnly(name),objective);
        return value==null?missing:value.value();
    }
    public static BastionState snapshot(MinecraftServer server) {
        var world=server.overworld();var board=server.getScoreboard();var objective=board.getObjective("lb_apoc");
        int night=0,stage=0,profile=-1;
        if(objective!=null) {night=score(board,objective,"#night",0);stage=score(board,objective,"#stage",0);profile=score(board,objective,"#event",-1);}
        return new BastionState(night,stage,profile,(int)Math.floorMod(world.getDayTime(),24000L),server.getPlayerCount(),
            night>0&&stage>0&&stage<=21, !world.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)||server.tickRateManager().isFrozen());
    }
    private static void send(ServerPlayer player,BastionState state) {
        if(player.connection.hasChannel(Payload.TYPE))PacketDistributor.sendToPlayer(player,new Payload(state));
    }
    public static void tick(ServerTickEvent.Post event) {
        var server=event.getServer();if(server.getTickCount()%20!=0||server.getPlayerCount()==0)return;
        var state=snapshot(server);for(var player:server.getPlayerList().getPlayers())send(player,state);
    }
    public static void login(PlayerEvent.PlayerLoggedInEvent event) {
        if(event.getEntity() instanceof ServerPlayer player)send(player,snapshot(player.server));
    }
}
