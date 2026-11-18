package lastbastion.hive.observation;
import lastbastion.hive.WorldHearing;
import com.jesz.createdieselgenerators.content.diesel_engine.IEngine;
import com.jesz.createdieselgenerators.content.diesel_engine.modular.ModularDieselEngineBlockEntity;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.content.kinetics.steamEngine.SteamEngineBlockEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import java.util.UUID;
import java.nio.charset.StandardCharsets;

/** Active output predicates mirror the installed engines' actual fuel/overload guards. */
public final class GeneratorNoise {
 private GeneratorNoise(){}
 public static void diesel(GeneratingKineticBlockEntity machine,IEngine engine){
  if(!(machine.getLevel() instanceof ServerLevel level)||Math.floorMod(level.getGameTime()+machine.getBlockPos().asLong(),40)!=0)return;
  if(machine instanceof ModularDieselEngineBlockEntity modular&&!modular.isController())return;
  float speed=machine.getSpeed(),generated=machine.getGeneratedSpeed();
  if(!Float.isFinite(speed)||!Float.isFinite(generated)||Math.abs(speed)<.1||Math.abs(generated)<.1
    ||machine.isOverStressed()||!engine.enabled()||engine.getThrottle()<=0)return;
  emit(machine,Math.min(128,32+Math.abs(speed)*.3));
 }
 public static void emit(BlockEntity machine,double strength){
  if(machine.getLevel() instanceof ServerLevel level)emit(level,machine.getBlockPos(),strength);
 }
 public static void emit(ServerLevel level,net.minecraft.core.BlockPos position,double strength){
  if(!level.hasChunkAt(position)||Math.floorMod(level.getGameTime()+position.asLong(),40)!=0)return;
  var source=UUID.nameUUIDFromBytes(("working_generator:"+position.asLong()).getBytes(StandardCharsets.UTF_8));
  WorldHearing.emit(level,source,Vec3.atCenterOf(position),strength);
 }
 public static void steam(SteamEngineBlockEntity engine){
  if(!(engine.getLevel() instanceof ServerLevel level)||Math.floorMod(level.getGameTime()+engine.getBlockPos().asLong(),40)!=0)return;
  var tank=engine.source.get();var shaft=engine.target.get();
  if(tank==null||shaft==null||tank.isRemoved()||shaft.isRemoved()||!level.hasChunkAt(tank.getBlockPos())||!level.hasChunkAt(shaft.getBlockPos()))return;
  var controllerPos=tank.getController();if(controllerPos==null||!level.hasChunkAt(controllerPos))return;
  var controller=tank.getControllerBE();if(controller==null||!engine.isValid()||shaft.isOverStressed())return;
  float efficiency=controller.boiler.getEngineEfficiency(controller.getTotalTankSize()),speed=shaft.getSpeed();
  if(!Float.isFinite(efficiency)||efficiency<=0||!Float.isFinite(speed)||Math.abs(speed)<.1)return;
  emit(engine,Math.min(112,24+Math.abs(speed)*.3+32*Math.min(1,efficiency)));
 }
}
