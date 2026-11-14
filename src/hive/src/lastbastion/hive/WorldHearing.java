package lastbastion.hive;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import java.util.*;

public final class WorldHearing {
    private static final Map<ServerLevel,NoiseBuffer> BUFFERS=new WeakHashMap<>();
    private static final Map<ServerLevel,HearingBudget> BUDGETS=new WeakHashMap<>();
    private static final class Deferred extends RuntimeException {
        private Deferred(){super(null,null,false,false);}
    }
    private static final Deferred DEFERRED=new Deferred();
    public static final Hearing.Settings SETTINGS=new Hearing.Settings(200,64,0.01,1.25);
    private WorldHearing(){}
    public static void emit(ServerLevel level,UUID source,Vec3 location,double strength){
        if(!level.getServer().isSameThread())throw new IllegalStateException("Noise must be emitted on server thread");
        BUFFERS.computeIfAbsent(level,l->new NoiseBuffer(512,4,200)).emit(
            new Hearing.Noise(source,new Hearing.Position(location.x,location.y,location.z),level.getGameTime(),strength),level.getGameTime());
    }
    public static Optional<Hearing.Heard> listen(ServerLevel level,Vec3 ears,UUID previous){
        var buffer=BUFFERS.get(level);if(buffer==null)return Optional.empty();
        var budget=BUDGETS.computeIfAbsent(level,l->new HearingBudget(32,8192));
        if(!budget.begin(level.getGameTime()))return Optional.empty();
        var position=new Hearing.Position(ears.x,ears.y,ears.z);
        try {
        return Hearing.choose(buffer.snapshot(level.getGameTime()),position,level.getGameTime(),previous,
            noise->{var p=noise.position();return transmission(level,ears,new Vec3(p.x(),p.y(),p.z()),budget);},SETTINGS);
        } catch(Deferred deferred){return Optional.empty();}
    }
    /** Bounded sampling, no chunk loading. A solid block attenuates rather than reveals a target. */
    public static double transmission(ServerLevel level,Vec3 from,Vec3 to){
        return transmission(level,from,to,null);
    }
    private static double transmission(ServerLevel level,Vec3 from,Vec3 to,HearingBudget budget){
        double distance=from.distanceTo(to);if(!Double.isFinite(distance)||distance>64)return 0;
        int steps=Math.max(1,(int)Math.ceil(distance*2));double result=1;
        Set<BlockPos> visited=new HashSet<>();BlockPos source=BlockPos.containing(to),listener=BlockPos.containing(from);
        for(int i=1;i<steps;i++){
            if(budget!=null&&!budget.sample())throw DEFERRED;
            BlockPos pos=BlockPos.containing(from.lerp(to,(double)i/steps));
            if(pos.equals(source)||pos.equals(listener)||!visited.add(pos))continue;
            if(!level.hasChunkAt(pos))return 0;
            var state=level.getBlockState(pos);
            if(!state.getCollisionShape(level,pos).isEmpty())result*=0.35;
            if(result<0.001)return 0;
        }
        return result;
    }
}
