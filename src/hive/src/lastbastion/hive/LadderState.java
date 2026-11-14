package lastbastion.hive;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;

/** Physical support state travels with normal entity metadata to all tracking clients. */
public final class LadderState {
 public static final EntityDataAccessor<Boolean> SUPPORT=SynchedEntityData.defineId(Zombie.class,EntityDataSerializers.BOOLEAN);
 private LadderState(){}
 public static boolean supports(Zombie zombie){return zombie.isAlive()&&!zombie.isRemoved()&&zombie.getEntityData().get(SUPPORT);}
 public static void setSupport(Zombie zombie,boolean value){
  if(zombie.level().isClientSide())throw new IllegalStateException("Server owns ladder state");
  zombie.getEntityData().set(SUPPORT,value);
 }
}
