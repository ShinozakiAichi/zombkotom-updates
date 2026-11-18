package lastbastion.hive.coordination;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Zombie;
import java.util.EnumSet;
import static lastbastion.hive.coordination.UnitCapabilities.Ability.*;
/** Only adult zombies participate today; unusually sized bodies cannot become ladder supports. */
public final class ZombieUnitAdapter implements UnitCapabilities.Adapter<Mob> {
 public static final ZombieUnitAdapter INSTANCE=new ZombieUnitAdapter();
 private static final UnitCapabilities STANDARD=new UnitCapabilities(EnumSet.allOf(UnitCapabilities.Ability.class));
 private static final UnitCapabilities GROUND=new UnitCapabilities(EnumSet.of(COORDINATE,SCOUT,PRESSURE,FLANK,EXIT_WATCH));
 private ZombieUnitAdapter(){}
 @Override public UnitCapabilities describe(Mob unit){
  if(!(unit instanceof Zombie z)||z.isBaby())return UnitCapabilities.NONE;
  return Math.abs(z.getBbHeight()-1.95)<.1&&z.getBbWidth()<=.7?STANDARD:GROUND;
 }
}
