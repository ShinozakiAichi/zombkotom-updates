package lastbastion.hive;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.minecraft.world.entity.monster.Zombie;

@Mod("lastbastion_hive")
public final class HiveMod {
    public HiveMod(){
        NeoForge.EVENT_BUS.register(NoiseEvents.class);
        NeoForge.EVENT_BUS.register(HiveLossEvents.class);
        NeoForge.EVENT_BUS.register(FootstepEvents.class);
        NeoForge.EVENT_BUS.register(ModSoundEvents.class);
        NeoForge.EVENT_BUS.register(HiveCompatibility.class);
        NeoForge.EVENT_BUS.register(FormationTrigger.class);
        NeoForge.EVENT_BUS.register(FormationLifecycle.class);
        NeoForge.EVENT_BUS.register(FpvNoiseEvents.class);
        NeoForge.EVENT_BUS.register(DecoyNoiseEvents.class);
        NeoForge.EVENT_BUS.register(FpvBurst.class);
        NeoForge.EVENT_BUS.register(FpvMineLifecycle.class);
        NeoForge.EVENT_BUS.addListener(this::joined);
    }
    private void joined(EntityJoinLevelEvent event){
        if(event.getLevel().isClientSide()||!(event.getEntity() instanceof Zombie zombie))return;
        if(zombie.goalSelector.getAvailableGoals().stream().noneMatch(g->g.getGoal() instanceof InvestigateNoiseGoal))
            zombie.goalSelector.addGoal(1,new InvestigateNoiseGoal(zombie));
        if(zombie.goalSelector.getAvailableGoals().stream().noneMatch(g->g.getGoal() instanceof ObservePlayerGoal))
            zombie.goalSelector.addGoal(8,new ObservePlayerGoal(zombie));
        if(zombie.goalSelector.getAvailableGoals().stream().noneMatch(g->g.getGoal() instanceof MemoryPatrolGoal))
            zombie.goalSelector.addGoal(5,new MemoryPatrolGoal(zombie));
        if(zombie.goalSelector.getAvailableGoals().stream().noneMatch(g->g.getGoal() instanceof TacticalApproachGoal))
            zombie.goalSelector.addGoal(1,new TacticalApproachGoal(zombie));
        if(zombie.goalSelector.getAvailableGoals().stream().noneMatch(g->g.getGoal() instanceof LivingLadderGoal))
            zombie.goalSelector.addGoal(0,new LivingLadderGoal(zombie));
        zombie.removeTag("lb_hive_dormant");
        zombie.removeTag("lb_hive_ambush");
        if(zombie.goalSelector.getAvailableGoals().stream().noneMatch(g->g.getGoal() instanceof SleeperGoal))
            zombie.goalSelector.addGoal(0,new SleeperGoal(zombie));
    }
}
