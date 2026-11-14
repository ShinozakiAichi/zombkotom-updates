package lastbastion.hive;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import java.util.EnumSet;
import java.util.UUID;

/** Initial investigate behavior; hive ambush/patrol arbitration will build on this goal. */
public final class InvestigateNoiseGoal extends Goal {
    private final Zombie mob;
    private long nextListen, expires;
    private UUID source;
    private Vec3 destination;
    private int pathCooldown;
    public InvestigateNoiseGoal(Zombie mob){this.mob=mob;setFlags(EnumSet.of(Flag.MOVE));}
    private boolean visibleThreat(){return mob.getTarget()!=null&&mob.getTarget().isAlive()&&mob.getSensing().hasLineOfSight(mob.getTarget());}
    private boolean listen(){
        var level=(ServerLevel)mob.level();long now=level.getGameTime();
        if(now<nextListen)return false;
        nextListen=now+20+Math.floorMod(mob.getId(),10);
        var heard=WorldHearing.listen(level,mob.getEyePosition(),source);
        if(heard.isEmpty())return false;
        var noise=heard.get().noise();var p=noise.position();
        source=noise.source();destination=new Vec3(p.x(),p.y(),p.z());expires=noise.tick()+200;
        return mob.position().distanceToSqr(destination)>4;
    }
    @Override public boolean canUse(){return !mob.isNoAi()&&mob.level() instanceof ServerLevel&&!visibleThreat()&&listen();}
    @Override public void start(){pathCooldown=0;}
    @Override public boolean canContinueToUse(){return !visibleThreat()&&destination!=null&&mob.level().getGameTime()<expires&&mob.position().distanceToSqr(destination)>4;}
    @Override public void tick(){
        listen();
        if(--pathCooldown<=0&&destination!=null&&PatrolPathBudget.reserve((ServerLevel)mob.level())){
            pathCooldown=20+Math.floorMod(mob.getId(),10);
            mob.getNavigation().moveTo(destination.x,destination.y,destination.z,1.0);
        }
    }
    @Override public void stop(){mob.getNavigation().stop();destination=null;source=null;}
}
