package lastbastion.hive;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import java.util.HashSet;

/** Door crossings inferred only between two close, visible samples of the same player. */
public final class EntranceDiscovery {
 private EntranceDiscovery(){}
 public static boolean crosses(double before,double after,double plane){
  return (before<plane-0.1&&after>plane+0.1)||(after<plane-0.1&&before>plane+0.1);
 }
 public static void observe(Zombie observer,Vec3 before,Vec3 after){
  if(!(observer.level() instanceof ServerLevel level)||before.distanceToSqr(after)>64)return;
  observeShelterTransition(observer,level,before,after);
  int steps=Math.max(1,(int)Math.ceil(before.distanceTo(after)*2));
  var visited=new HashSet<BlockPos>();
  for(int i=0;i<=steps;i++){
   var pos=BlockPos.containing(before.lerp(after,(double)i/steps));
   if(!visited.add(pos)||!level.hasChunkAt(pos))continue;
   var state=level.getBlockState(pos);if(!(state.getBlock() instanceof DoorBlock))continue;
   if(state.getValue(DoorBlock.HALF)==DoubleBlockHalf.UPPER)pos=pos.below();
   var axis=state.getValue(DoorBlock.FACING).getAxis();
   boolean crossing=axis==Direction.Axis.X?crosses(before.x,after.x,pos.getX()+0.5):crosses(before.z,after.z,pos.getZ()+0.5);
   if(!crossing)continue;
   var hit=level.clip(new ClipContext(observer.getEyePosition(),Vec3.atCenterOf(pos.above()),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,observer));
   if(hit.getType()!=HitResult.Type.MISS&&!hit.getBlockPos().equals(pos)&&!hit.getBlockPos().equals(pos.above()))continue;
   HiveSavedData.get(level).observe(new HiveMemory.Key(HiveMemory.Kind.ENTRANCE,pos.getX(),pos.getY(),pos.getZ()),level.getGameTime(),2);
  }
 }
 private static boolean visible(Zombie observer,ServerLevel level,Vec3 feet){
  return level.clip(new ClipContext(observer.getEyePosition(),feet.add(0,1.6,0),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,observer)).getType()==HitResult.Type.MISS;
 }
 /** Only a short observed crossing is sampled; no search for an unseen underground base. */
 private static void observeShelterTransition(Zombie observer,ServerLevel level,Vec3 before,Vec3 after){
  if(before.distanceToSqr(after)<1||Math.abs(before.y-after.y)>2||!visible(observer,level,before)||!visible(observer,level,after))return;
  Boolean previous=null;int steps=Math.max(1,(int)Math.ceil(before.distanceTo(after)*2));
  for(int i=0;i<=steps;i++){
   var point=before.lerp(after,(double)i/steps);var pos=BlockPos.containing(point);
   Boolean covered=sheltered(level,pos);if(covered==null)return;
   if(previous!=null&&previous!=covered&&visible(observer,level,Vec3.atBottomCenterOf(pos))){
    HiveSavedData.get(level).observe(new HiveMemory.Key(HiveMemory.Kind.ENTRANCE,pos.getX(),pos.getY(),pos.getZ()),level.getGameTime(),2);
    return;
   }
   previous=covered;
  }
 }
 private static Boolean sheltered(ServerLevel level,BlockPos feet){
  if(!level.hasChunkAt(feet))return null;
  if(!level.getBlockState(feet).getCollisionShape(level,feet).isEmpty()||!level.getBlockState(feet.above()).getCollisionShape(level,feet.above()).isEmpty())return null;
  boolean roof=false,wall=false;
  for(int y=2;y<=5;y++)if(!level.getBlockState(feet.above(y)).getCollisionShape(level,feet.above(y)).isEmpty()){roof=true;break;}
  if(!roof)return false;
  for(var direction:Direction.Plane.HORIZONTAL)for(int distance=1;distance<=2;distance++){
   var side=feet.above().relative(direction,distance);if(!level.hasChunkAt(side))return null;
   if(!level.getBlockState(side).getCollisionShape(level,side).isEmpty())wall=true;
  }
  return wall;
 }
}
