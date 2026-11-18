package lastbastion.hive.routing;
import lastbastion.hive.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.*;
import java.util.*;

/** Small native graph comparing an observed wall crossing with the available walking route. */
public final class WallAssault {
 public record Decision(RouteGraph.Route route,WallLadderGeometry.Plan ladder,BlockPos exit){}
 private WallAssault(){}
 public static Optional<Decision> choose(Zombie scout,BlockPos objective,int bodies){
  if(!(scout.level() instanceof ServerLevel level)||scout.blockPosition().distSqr(objective)>40*40)return Optional.empty();
  var crowding=ObservedCrowding.sample(scout);if(crowding==null)return Optional.empty();
  var origin=scout.blockPosition();
  // Clip must not fetch unknown terrain. Native walking search applies its own larger margin check.
  for(int x=Math.min(origin.getX(),objective.getX())>>4;x<=(Math.max(origin.getX(),objective.getX())>>4);x++)
   for(int z=Math.min(origin.getZ(),objective.getZ())>>4;z<=(Math.max(origin.getZ(),objective.getZ())>>4);z++)if(!level.hasChunk(x,z))return Optional.empty();
  var direct=NativeRouteSearch.find(scout,origin,objective);
  if(direct.status()==NativeRouteSearch.Status.DEFERRED)return Optional.empty();
  var nodes=new ArrayList<RouteGraph.Node>();nodes.add(node("start",origin));nodes.add(node("goal",objective));
  var edges=new ArrayList<RouteGraph.Edge>();
  var danger=HiveSavedData.get(level).nearby(HiveMemory.Kind.DANGER,scout.getX(),scout.getY(),scout.getZ(),80,level.getGameTime(),64);
  if(direct.status()==NativeRouteSearch.Status.FOUND)edges.add(new RouteGraph.Edge("start","goal",cost(direct.path(),danger,level.getGameTime(),crowding,scout),RouteGraph.Mode.WALK,1));
  WallLadderGeometry.Plan geometry=null;BlockPos exit=null;
  var hit=level.clip(new ClipContext(scout.getEyePosition(),Vec3.atBottomCenterOf(objective).add(0,scout.getEyeHeight(),0),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,scout));
  if(hit.getType()==HitResult.Type.BLOCK&&hit.getDirection().getAxis().isHorizontal()){
   var wall=hit.getBlockPos();var outward=hit.getDirection();int height=0;
   for(int y=origin.getY();y<origin.getY()+10;y++){
    var p=new BlockPos(wall.getX(),y,wall.getZ());if(level.getBlockState(p).getCollisionShape(level,p).isEmpty())break;
    Vec3 face=Vec3.atCenterOf(p).add(outward.getStepX()*.501,0,outward.getStepZ()*.501);
    var visibility=level.clip(new ClipContext(scout.getEyePosition(),face,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,scout));
    if(visibility.getType()!=HitResult.Type.MISS&&!visibility.getBlockPos().equals(p)){height=0;break;}
    height++;
   }
   var candidate=WallLadderGeometry.plan(wall.getX(),origin.getY(),wall.getZ(),height,outward.getStepX(),outward.getStepZ(),Math.max(1,level.getDayTime()/24000+1),bodies).orElse(null);
   if(candidate!=null&&clear(scout,candidate)){
    var slot=candidate.slots().get(0);var base=BlockPos.containing(slot.x(),slot.y(),slot.z());
    var top=new BlockPos(candidate.wallX(),candidate.topY(),candidate.wallZ());
    var approach=NativeRouteSearch.find(scout,origin,base);var onward=NativeRouteSearch.find(scout,top,objective);
    if(approach.status()==NativeRouteSearch.Status.DEFERRED||onward.status()==NativeRouteSearch.Status.DEFERRED)return Optional.empty();
    if(approach.status()==NativeRouteSearch.Status.FOUND&&onward.status()==NativeRouteSearch.Status.FOUND){
     geometry=candidate;exit=top;nodes.add(node("base",base));nodes.add(node("top",top));
     edges.add(new RouteGraph.Edge("start","base",cost(approach.path(),danger,level.getGameTime(),crowding,scout),RouteGraph.Mode.WALK,1));
     edges.add(new RouteGraph.Edge("base","top",new RouteGraph.Cost(20,0,0,120+40*candidate.width(),30),RouteGraph.Mode.LADDER,candidate.slots().size()));
     edges.add(new RouteGraph.Edge("top","goal",cost(onward.path(),danger,level.getGameTime(),crowding,scout),RouteGraph.Mode.WALK,1));
    }
   }
  }
  var route=new RouteGraph(nodes,edges).shortestPath("start","goal",bodies);
  return route.isEmpty()?Optional.empty():Optional.of(new Decision(route.get(),geometry,exit));
 }
 public static boolean clear(Zombie scout,WallLadderGeometry.Plan geometry){
  var level=scout.level();var exit=new BlockPos(geometry.wallX(),geometry.topY(),geometry.wallZ());
  if(!level.hasChunkAt(exit)||!level.getBlockState(exit.below()).isFaceSturdy(level,exit.below(),Direction.UP)
    ||!level.getBlockState(exit).getCollisionShape(level,exit).isEmpty()||!level.getBlockState(exit.above()).getCollisionShape(level,exit.above()).isEmpty())return false;
  for(var slot:geometry.slots()){
   var p=BlockPos.containing(slot.x(),slot.y(),slot.z());if(!level.hasChunkAt(p))return false;
   if(level.getBlockCollisions(scout,new AABB(slot.x()-.3,slot.y(),slot.z()-.3,slot.x()+.3,slot.y()+1.95,slot.z()+.3)).iterator().hasNext())return false;
   if(slot.tier()==0&&!level.getBlockState(p.below()).isFaceSturdy(level,p.below(),Direction.UP))return false;
  }
  return true;
 }
 private static RouteGraph.Node node(String id,BlockPos p){return new RouteGraph.Node(id,p.getX(),p.getY(),p.getZ());}
 private static RouteGraph.Cost cost(Path path,List<HiveMemory.Observation> dangers,long now,CrowdingField crowding,Zombie observer){
  double exposure=0;
  for(int i=0;i<path.getNodeCount();i++){var n=path.getNode(i);
   for(var d:dangers){var k=d.key();double distance=Math.pow((double)k.x()-n.x,2)+Math.pow((double)k.y()-n.y,2)+Math.pow((double)k.z()-n.z,2);
    if(distance<=36)exposure+=HiveMemory.relevance(d,now)*12/(1+distance);
   }
  }
  return new RouteGraph.Cost(Math.max(0,path.getNodeCount()-1)*5,exposure,ObservedCrowding.cost(path,crowding,observer),0,path.getNodeCount()*.2);
 }
}
