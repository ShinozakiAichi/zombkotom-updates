package lastbastion.hive.observation;
import lastbastion.hive.*;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import java.util.*;

/** Sparse visual evidence of real moving Create blocks; never scans an unseen base. */
public final class MechanismObservations {
 private static final class World {
  final GateHistory gates=new GateHistory(128,600);
  final LinkedHashMap<UUID,DiscoveryCursor<BlockPos>> discovery=new LinkedHashMap<>(16,.75f,true);
  long tick;int observers;
  long closedTick;Set<Long> closedSnapshot;
  GateHistory.Change record(long position,boolean open,long now){closedSnapshot=null;return gates.observe(position,open,now);}
  Set<Long> closed(long now){
   if(closedSnapshot==null||closedTick!=now){closedSnapshot=gates.closedPositions(now);closedTick=now;}
   return closedSnapshot;
  }
  DiscoveryCursor<BlockPos> cursor(UUID id){
   var result=discovery.computeIfAbsent(id,k->new DiscoveryCursor<>());
   if(discovery.size()>128)discovery.remove(discovery.keySet().iterator().next());
   return result;
  }
 }
 private static final Map<ServerLevel,World> WORLDS=new WeakHashMap<>();
 private MechanismObservations(){}
 /** Only recently witnessed closed passages, never a scan of hidden machinery. */
 public static Set<Long> closedPassages(ServerLevel level){
  var world=WORLDS.get(level);return world==null?Set.of():world.closed(level.getGameTime());
 }
 @SubscribeEvent public static void observe(EntityTickEvent.Post event){
  if(!(event.getEntity() instanceof Zombie z)||!(z.level() instanceof ServerLevel level)||!z.isAlive()||z.isNoAi()||Math.floorMod(level.getGameTime()+z.getId(),40)!=0)return;
  var world=WORLDS.computeIfAbsent(level,k->new World());long now=level.getGameTime();
  if(world.tick!=now){world.tick=now;world.observers=0;}if(world.observers++>=4)return;
  var machines=level.getEntitiesOfClass(AbstractContraptionEntity.class,z.getBoundingBox().inflate(24));
  // A truncated occupancy list would invent open passages behind omitted machines.
  // Leave this observation unknown when the bounded inspection cannot be complete.
  if(machines.size()>16)return;
  int samples=0;int[] rayBudget={4096};
  for(var entity:machines){var contraption=entity.getContraption();if(contraption==null)continue;
   var cursor=world.cursor(entity.getUUID());
   for(int inspected=0;inspected<Math.min(32,contraption.getBlocks().size())&&samples<8;inspected++){
    var batch=cursor.next(contraption.getBlocks(),1);if(batch.isEmpty())break;
    var local=batch.get(0);
    var info=contraption.getBlocks().get(local);if(info.state().isAir())continue;
    var center=entity.toGlobalVector(Vec3.atCenterOf(local),1);var p=BlockPos.containing(center);
    if(z.distanceToSqr(center)>24*24||!passageSite(level,p)||!visible(z,p,center)||!clearMovingRay(z,center,machines,entity,local,null,rayBudget))continue;
    if(world.record(p.asLong(),false,now)==GateHistory.Change.CLOSED)invalidate(level,p);
    samples++;
   }
  }
  var positions=new ArrayList<>(world.gates.positions(now));if(!positions.isEmpty())Collections.rotate(positions,(int)Math.floorMod(now/40+z.getId(),positions.size()));
  int watched=0;
  for(var packed:positions){var p=BlockPos.of(packed);if(z.distanceToSqr(Vec3.atCenterOf(p))>24*24||!visible(z,p))continue;
   if(++watched>8)break;
   if(!clearMovingRay(z,Vec3.atBottomCenterOf(p).add(0,1.6,0),machines,null,null,p,rayBudget))continue;
   boolean open=passageSite(level,p)&&level.getBlockState(p).getCollisionShape(level,p).isEmpty()
    &&level.getBlockState(p.above()).getCollisionShape(level,p.above()).isEmpty()&&!occupied(machines,p);
   var change=world.record(packed,open,now);
   if(change==GateHistory.Change.OPENED)HiveSavedData.get(level).observe(new HiveMemory.Key(HiveMemory.Kind.ENTRANCE,p.getX(),p.getY(),p.getZ()),now,2);
   if(change==GateHistory.Change.CLOSED)invalidate(level,p);
  }
 }
 private static boolean passageSite(ServerLevel level,BlockPos p){
  if(!level.hasChunkAt(p)||!level.getBlockState(p.below()).isFaceSturdy(level,p.below(),Direction.UP))return false;
  for(int y=2;y<=5;y++)if(!level.getBlockState(p.above(y)).getCollisionShape(level,p.above(y)).isEmpty())return true;
  for(var axis:new Direction[]{Direction.EAST,Direction.SOUTH}){
   var a=p.relative(axis,2).above();var b=p.relative(axis.getOpposite(),2).above();
   if(level.hasChunkAt(a)&&level.hasChunkAt(b)&&!level.getBlockState(a).getCollisionShape(level,a).isEmpty()&&!level.getBlockState(b).getCollisionShape(level,b).isEmpty())return true;
  }
  return false;
 }
 private static boolean visible(Zombie z,BlockPos p){return visible(z,p,Vec3.atBottomCenterOf(p).add(0,1.6,0));}
 private static boolean visible(Zombie z,BlockPos p,Vec3 destination){
  if(!z.level().hasChunkAt(p))return false;
  // Every intervening chunk must already exist in memory before ray tracing.
  var from=z.blockPosition();for(int x=Math.min(from.getX(),p.getX())>>4;x<=(Math.max(from.getX(),p.getX())>>4);x++)
   for(int q=Math.min(from.getZ(),p.getZ())>>4;q<=(Math.max(from.getZ(),p.getZ())>>4);q++)if(!z.level().hasChunk(x,q))return false;
  var hit=z.level().clip(new ClipContext(z.getEyePosition(),destination,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,z));
  return hit.getType()==HitResult.Type.MISS||hit.getBlockPos().equals(p)||hit.getBlockPos().equals(p.above());
 }
 /** Exact native voxel traversal in each moving construction's coordinates; no world chunk reads. */
 private static boolean clearMovingRay(Zombie observer,Vec3 destination,List<AbstractContraptionEntity> machines,
   AbstractContraptionEntity targetEntity,BlockPos targetLocal,BlockPos watchedPassage,int[] budget){
  for(var entity:machines){
   var contraption=entity.getContraption();if(contraption==null)continue;
   var from=entity.toLocalVector(observer.getEyePosition(),1);var to=entity.toLocalVector(destination,1);
   double distance=from.distanceToSqr(to);if(!Double.isFinite(distance)||distance>32*32)return false;
   Boolean clear=net.minecraft.world.level.BlockGetter.traverseBlocks(from,to,entity,(machine,local)->{
    if(--budget[0]<0)return Boolean.FALSE;
    var block=contraption.getBlocks().get(local);if(block==null||block.state().isAir())return null;
    if(machine==targetEntity&&local.equals(targetLocal))return Boolean.TRUE;
    if(watchedPassage!=null){
     var global=BlockPos.containing(machine.toGlobalVector(Vec3.atCenterOf(local),1));
     if(global.equals(watchedPassage)||global.equals(watchedPassage.above()))return Boolean.TRUE;
    }
    return Boolean.FALSE;
   },machine->Boolean.TRUE);
   if(!clear)return false;
  }
  return true;
 }
 private static boolean occupied(List<AbstractContraptionEntity> machines,BlockPos p){
  for(var e:machines){
   var contraption=e.getContraption();if(contraption==null)continue;
   // Conservative local bounds of a body-width passage. Center-only probes miss
   // partial overlap and points exactly on a moving block's far voxel boundary.
   double minX=Double.POSITIVE_INFINITY,minY=minX,minZ=minX;
   double maxX=Double.NEGATIVE_INFINITY,maxY=maxX,maxZ=maxX;
   for(double x:new double[]{.2,.8})for(double y:new double[]{.01,1.99})for(double z:new double[]{.2,.8}){
    var local=e.toLocalVector(new Vec3(p.getX()+x,p.getY()+y,p.getZ()+z),1);
    minX=Math.min(minX,local.x);minY=Math.min(minY,local.y);minZ=Math.min(minZ,local.z);
    maxX=Math.max(maxX,local.x);maxY=Math.max(maxY,local.y);maxZ=Math.max(maxZ,local.z);
   }
   if(!Double.isFinite(minX)||!Double.isFinite(minY)||!Double.isFinite(minZ)||!Double.isFinite(maxX)||!Double.isFinite(maxY)||!Double.isFinite(maxZ))return true;
   var low=BlockPos.containing(minX,minY,minZ);var high=BlockPos.containing(maxX-1e-7,maxY-1e-7,maxZ-1e-7);
   long dx=(long)high.getX()-low.getX()+1,dy=(long)high.getY()-low.getY()+1,dz=(long)high.getZ()-low.getZ()+1;
   if(dx<1||dy<1||dz<1||dx>4||dy>4||dz>4)return true;
   for(int x=0;x<dx;x++)for(int y=0;y<dy;y++)for(int z=0;z<dz;z++){
    var block=contraption.getBlocks().get(low.offset(x,y,z));if(block!=null&&!block.state().isAir())return true;
   }
  }
  return false;
 }
 private static void invalidate(ServerLevel level,BlockPos gate){
  int inspected=0;
  for(var z:level.getEntitiesOfClass(Zombie.class,new AABB(gate).inflate(24))){
   if(++inspected>32)break;var path=z.getNavigation().getPath();if(path==null)continue;
   for(int i=path.getNextNodeIndex();i<Math.min(path.getNodeCount(),path.getNextNodeIndex()+128);i++){
    var node=path.getNode(i);if(node.x==gate.getX()&&node.z==gate.getZ()&&Math.abs(node.y-gate.getY())<=1){z.getNavigation().stop();break;}
   }
  }
 }
 @SubscribeEvent public static void damaged(LivingDamageEvent.Post event){
  if(!(event.getEntity() instanceof Zombie z)||!(z.level() instanceof ServerLevel level)||event.getNewDamage()<=0)return;
  if(!event.getSource().typeHolder().unwrapKey().map(k->k.location().getNamespace().equals("create")).orElse(false))return;
  var p=z.blockPosition();HiveSavedData.get(level).observe(new HiveMemory.Key(HiveMemory.Kind.DANGER,Math.floorDiv(p.getX(),4)*4,p.getY(),Math.floorDiv(p.getZ(),4)*4),level.getGameTime(),Math.min(4,.5+event.getNewDamage()/2));
 }
}
