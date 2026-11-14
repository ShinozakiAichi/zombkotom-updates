package lastbastion.hive;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.phys.Vec3;
import java.util.Set;
import java.util.UUID;
import java.nio.charset.StandardCharsets;

/** Samples real rotating machines even when their audible loop exists only on clients. */
public final class IndustrialNoise {
 private static final Set<String> MACHINES=Set.of("mechanical_press","mechanical_mixer","millstone","crushing_wheel",
  "mechanical_saw","mechanical_drill","mechanical_bearing","windmill_bearing","encased_fan","rope_pulley");
 private IndustrialNoise(){}
 public static void tick(KineticBlockEntity machine){
  if(!(machine.getLevel() instanceof ServerLevel level)||Math.floorMod(level.getGameTime()+machine.getBlockPos().asLong(),40)!=0)return;
  float speed=machine.getSpeed();if(!Float.isFinite(speed)||Math.abs(speed)<0.1||machine.isOverStressed())return;
  var id=BuiltInRegistries.BLOCK.getKey(machine.getBlockState().getBlock());
  if(!id.getNamespace().equals("create")||!MACHINES.contains(id.getPath()))return;
  var pos=machine.getBlockPos();
  var source=UUID.nameUUIDFromBytes(("create_machine:"+pos.asLong()).getBytes(StandardCharsets.UTF_8));
  WorldHearing.emit(level,source,Vec3.atCenterOf(pos),Math.min(96,8+Math.abs(speed)*0.2));
 }
}
