package lastbastion.hive.routing;

import lastbastion.hive.PatrolPathBudget;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.level.pathfinder.PathfindingContext;
import net.minecraft.world.level.pathfinder.PathType;
import java.util.Set;
import java.util.List;
import lastbastion.hive.observation.MechanismObservations;

/** Bounded native walking search from a hypothetical waypoint, without moving the mob. */
public final class NativeRouteSearch {
 public enum Status { FOUND,UNREACHABLE,UNLOADED,OUT_OF_BOUNDS,DEFERRED }
 public record Result(Status status,Path path){}
 private NativeRouteSearch(){}
 public static boolean crossesClosedGate(ServerLevel level,Path path){
  return crossesClosedGate(level,List.of(path),0);
 }
 /** Checks current and queued segments, independently of observer entity enumeration caps. */
 public static boolean crossesClosedGate(ServerLevel level,List<Path> paths,int first){
  var closed=MechanismObservations.closedPassages(level);if(closed.isEmpty())return false;
  for(int leg=Math.max(0,first);leg<paths.size();leg++){
   var path=paths.get(leg);if(path.getNodeCount()>1024)return true;
   for(int i=path.getNextNodeIndex();i<path.getNodeCount();i++){
    var node=path.getNode(i);
    if(closed.contains(BlockPos.asLong(node.x,node.y,node.z))||closed.contains(BlockPos.asLong(node.x,node.y-1,node.z)))return true;
   }
  }
  return false;
 }
 public static Result find(Mob mob,BlockPos from,BlockPos to){
  if(!(mob.level() instanceof ServerLevel level)||!mob.isAlive()||from.distSqr(to)>48*48||Math.abs(from.getY()-to.getY())>12)
   return new Result(Status.OUT_OF_BOUNDS,null);
  if(!level.getServer().isSameThread())throw new IllegalStateException("Route planning must run on the server thread");
  int margin=16;
  var min=new BlockPos(Math.min(from.getX(),to.getX())-margin,Math.max(level.getMinBuildHeight(),Math.min(from.getY(),to.getY())-8),Math.min(from.getZ(),to.getZ())-margin);
  var max=new BlockPos(Math.max(from.getX(),to.getX())+margin,Math.min(level.getMaxBuildHeight()-1,Math.max(from.getY(),to.getY())+8),Math.max(from.getZ(),to.getZ())+margin);
  // PathNavigationRegion obtains chunks; verify the entire region first so no tickets/loading occur.
  for(int x=Math.floorDiv(min.getX(),16);x<=Math.floorDiv(max.getX(),16);x++)
   for(int z=Math.floorDiv(min.getZ(),16);z<=Math.floorDiv(max.getZ(),16);z++)
    if(!level.hasChunk(x,z))return new Result(Status.UNLOADED,null);
  if(!PatrolPathBudget.reserve(level,mob.getUUID()))return new Result(Status.DEFERRED,null);
  var closed=MechanismObservations.closedPassages(level);
  var evaluator=new WalkNodeEvaluator(){
   @Override public Node getStart(){return canStartAt(from)?getStartNode(from):null;}
   @Override public PathType getPathType(PathfindingContext context,int x,int y,int z){
    // World PathTypeCache is keyed only by coordinates, not by region/view.
    // Classify private observations here, never write fake blocks into that shared cache.
    if(closed.contains(BlockPos.asLong(x,y,z))||closed.contains(BlockPos.asLong(x,y-1,z)))return PathType.BLOCKED;
    return super.getPathType(context,x,y,z);
   }
  };
  evaluator.setCanOpenDoors(false);evaluator.setCanPassDoors(true);evaluator.setCanFloat(mob.getNavigation().canFloat());
  var finder=new PathFinder(evaluator,1024);
  var region=new PathNavigationRegion(level,min,max);
  var path=finder.findPath(region,mob,Set.of(to.immutable()),80,0,1);
  return path!=null&&path.canReach()?new Result(Status.FOUND,path):new Result(Status.UNREACHABLE,null);
 }
}
