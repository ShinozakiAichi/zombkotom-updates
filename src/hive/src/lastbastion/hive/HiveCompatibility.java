package lastbastion.hive;
import net.minecraft.world.entity.monster.Zombie;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import java.util.Map;
import java.util.WeakHashMap;

/** Remove obsolete navigation ownership after all join handlers have installed their goals. */
public final class HiveCompatibility {
 private static final Map<Zombie,Boolean> CHECKED=new WeakHashMap<>();
 private HiveCompatibility(){}
 @SubscribeEvent
 public static void beforeTick(EntityTickEvent.Pre event){
  if(event.getEntity().level().isClientSide()||!(event.getEntity() instanceof Zombie zombie)||CHECKED.put(zombie,Boolean.TRUE)!=null)return;
  zombie.goalSelector.removeAllGoals(goal->goal.getClass().getName().equals("com.tik.zbb.ai.goals.BreakAndBuildGoal"));
  zombie.setCanBreakDoors(false);
 }
}
