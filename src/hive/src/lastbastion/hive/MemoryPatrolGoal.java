package lastbastion.hive;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.pathfinder.Path;
import java.util.*;

/** Individual, reachable patrols to remembered activity. Never queries hidden players. */
public final class MemoryPatrolGoal extends Goal {
 private final Zombie zombie;
 private long nextAttempt,deadline;
 private int waiting;
 private Path path;
 private boolean ambush;
 private BlockPos watch,hide;
 public MemoryPatrolGoal(Zombie zombie){this.zombie=zombie;setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK));}
 private boolean free(){return !zombie.isNoAi()&&(zombie.getTarget()==null||!zombie.getTarget().isAlive());}
 @Override public boolean canUse(){
  if(!free()||!(zombie.level() instanceof ServerLevel level))return false;
  long now=level.getGameTime();if(now<nextAttempt)return false;
  nextAttempt=now+100+Math.floorMod(zombie.getId(),60);
  // Early nights retain mostly simple opponents. More patrol units unlock over 100 nights.
  long night=Math.max(1,level.getDayTime()/24000+1);
  int percent=(int)Math.min(65,10+night/2);
  if(Math.floorMod(zombie.getUUID().hashCode(),100)>=percent)return false;
  var memory=HiveSavedData.get(level);
  var dangers=memory.nearby(HiveMemory.Kind.DANGER,zombie.getX(),zombie.getY(),zombie.getZ(),80,now,64);
  var sites=new ArrayList<HiveMemory.Observation>();
  sites.addAll(memory.nearby(HiveMemory.Kind.ENTRANCE,zombie.getX(),zombie.getY(),zombie.getZ(),64,now,8));
  sites.addAll(memory.nearby(HiveMemory.Kind.SIGHTING,zombie.getX(),zombie.getY(),zombie.getZ(),64,now,8));
  sites.addAll(memory.nearby(HiveMemory.Kind.SUPPLY_ROUTE,zombie.getX(),zombie.getY(),zombie.getZ(),64,now,8));
  sites.addAll(memory.nearby(HiveMemory.Kind.SUCCESSFUL_ROUTE,zombie.getX(),zombie.getY(),zombie.getZ(),64,now,8));
  if(sites.isEmpty())return false;
  Collections.rotate(sites,Math.floorMod(zombie.getUUID().hashCode()+(int)(now/1200),sites.size()));
  ambush=false;watch=null;hide=null;
  if(night>=25&&Math.floorMod(zombie.getUUID().getLeastSignificantBits()/100,100)<Math.min(60,night/2)
    &&prepareAmbush(level,sites,dangers,now,night))return true;
  int tried=0;
  for(var site:sites){
   if(++tried>3)break;
   var k=site.key();
   // Split approach directions per zombie, and avoid remembered killing grounds.
   int side=Math.floorMod(zombie.getUUID().hashCode()+tried,4);
   int dx=side==0?5:side==1?-5:0,dz=side==2?5:side==3?-5:0;
   // Learned approach points are actual traversed cells, not guessed offsets through a wall.
   var pos=k.kind()==HiveMemory.Kind.SUCCESSFUL_ROUTE?new BlockPos(k.x(),k.y(),k.z()):new BlockPos(k.x()+dx,k.y(),k.z()+dz);
   if(!level.hasChunkAt(pos)||!memory.nearby(HiveMemory.Kind.DANGER,pos.getX(),pos.getY(),pos.getZ(),6,now,1).isEmpty())continue;
   if(zombie.distanceToSqr(pos.getX(),pos.getY(),pos.getZ())<9)continue;
   if(!PatrolPathBudget.reserve(level))return false;
   path=zombie.getNavigation().createPath(pos,1);
   if(path!=null&&path.canReach()&&!crossesDanger(path,dangers)){
    deadline=now+400;waiting=(int)Math.min(240,40+night*2);return true;
   }
  }
  return false;
 }
 private boolean prepareAmbush(ServerLevel level,List<HiveMemory.Observation> sites,List<HiveMemory.Observation> dangers,long now,long night){
  int[][] offsets={{4,0},{-4,0},{0,4},{0,-4},{4,4},{-4,4},{4,-4},{-4,-4}};
  for(var site:sites.subList(0,Math.min(3,sites.size()))){
   if(site.key().kind()!=HiveMemory.Kind.ENTRANCE&&HiveMemory.relevance(site,now)<3)continue;
   var k=site.key();var watched=new BlockPos(k.x(),k.y(),k.z());if(!level.hasChunkAt(watched))continue;
   for(int i=0;i<offsets.length;i++){
    var offset=offsets[Math.floorMod(i+zombie.getUUID().hashCode(),offsets.length)];
    var pos=watched.offset(offset[0],0,offset[1]);if(!level.hasChunkAt(pos))continue;
    var from=net.minecraft.world.phys.Vec3.atBottomCenterOf(watched).add(0,1.6,0);
    var to=net.minecraft.world.phys.Vec3.atBottomCenterOf(pos).add(0,1.6,0);
    var ray=level.clip(new net.minecraft.world.level.ClipContext(from,to,net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,zombie));
    if(ray.getType()!=net.minecraft.world.phys.HitResult.Type.BLOCK||ray.getBlockPos().equals(pos))continue;
    if(!PatrolPathBudget.reserve(level))return false;
    var candidate=zombie.getNavigation().createPath(pos,0);
    if(candidate==null||!candidate.canReach()||crossesDanger(candidate,dangers))continue;
    path=candidate;ambush=true;watch=watched;hide=pos;
    waiting=(int)Math.min(1200,400+night*8);deadline=now+400+waiting;return true;
   }
  }
  return false;
 }
 private boolean crossesDanger(Path candidate,List<HiveMemory.Observation> dangers){
  for(var danger:dangers){
   var k=danger.key();double start=zombie.distanceToSqr(k.x(),k.y(),k.z());
   for(int i=0;i<candidate.getNodeCount();i++){
    var n=candidate.getNode(i);
    double distance=(double)(n.x-k.x())*(n.x-k.x())+(double)(n.y-k.y())*(n.y-k.y())+(double)(n.z-k.z())*(n.z-k.z());
    // A unit already in danger may leave; never choose to approach deeper into it.
    if(distance<=25&&(start>25||distance+1<start))return true;
   }
  }
  return false;
 }
 @Override public void start(){zombie.getNavigation().moveTo(path,0.85);}
 @Override public boolean requiresUpdateEveryTick(){return true;}
 @Override public boolean canContinueToUse(){return free()&&zombie.level().getGameTime()<deadline&&waiting>0;}
 @Override public void tick(){
  if(zombie.getNavigation().isDone()){
   waiting--;
   if(ambush&&zombie.distanceToSqr(hide.getX()+.5,hide.getY(),hide.getZ()+.5)<2.25){
    zombie.addTag("lb_hive_ambush");zombie.getLookControl().setLookAt(watch.getX()+.5,watch.getY()+1.6,watch.getZ()+.5);
   }
  }
 }
 @Override public void stop(){zombie.removeTag("lb_hive_ambush");zombie.getNavigation().stop();path=null;nextAttempt=zombie.level().getGameTime()+160+Math.floorMod(zombie.getId(),80);}
}
