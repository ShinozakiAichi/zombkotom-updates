package lastbastion.hive.routing;

import lastbastion.hive.HiveMemory;
import lastbastion.hive.HiveSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import java.util.*;

/** Incremental small graph, with real walking edges and danger from shared observations only. */
public final class ObservedRoutePlanner {
 private record Search(String from,String to){}
 private final Zombie mob;private final ServerLevel level;private final BlockPos goal;private final long started;
 private final Map<String,BlockPos> positions=new LinkedHashMap<>();
 private final List<Search> searches=new ArrayList<>();private final List<RouteGraph.Edge> edges=new ArrayList<>();
 private final Map<Search,Path> paths=new HashMap<>();private final List<HiveMemory.Observation> dangers;
 private int cursor;private boolean done;private RouteAssignment result;private long lastSearch;
 private CrowdingField crowding;
 public ObservedRoutePlanner(Zombie mob,BlockPos objective){
  this.mob=mob;level=(ServerLevel)mob.level();
  // Sound/role coordinates can lie above the walkable floor. Keep the observation
  // in its owner; graph endpoints, like waypoints, must describe a standing cell.
  var standing=standingPoint(objective);goal=(standing==null?objective:standing).immutable();
  started=level.getGameTime();lastSearch=started;
  var origin=mob.blockPosition();positions.put("start",origin);positions.put("goal",goal);searches.add(new Search("start","goal"));
  dangers=HiveSavedData.get(level).nearby(HiveMemory.Kind.DANGER,mob.getX(),mob.getY(),mob.getZ(),80,started,64);
  var forward=Vec3.atBottomCenterOf(goal).subtract(mob.position()).multiply(1,0,1).normalize();
  var middle=mob.position().lerp(Vec3.atBottomCenterOf(goal),.5);
  for(int offset:new int[]{8,-8,16,-16})addCandidate(BlockPos.containing(middle.add(-forward.z*offset,0,forward.x*offset)));
  for(var memory:HiveSavedData.get(level).nearby(HiveMemory.Kind.ENTRANCE,mob.getX(),mob.getY(),mob.getZ(),40,started,4)){
   var key=memory.key();addCandidate(new BlockPos(key.x(),key.y(),key.z()));
  }
 }
 private BlockPos standingPoint(BlockPos requested){
  if(!level.hasChunkAt(requested))return null;
  for(int dy:new int[]{0,1,-1,2,-2}){
   var p=requested.offset(0,dy,0);
   if(p.getY()<=level.getMinBuildHeight()||p.getY()+1>=level.getMaxBuildHeight())continue;
   if(level.getBlockState(p).getCollisionShape(level,p).isEmpty()
     &&level.getBlockState(p.above()).getCollisionShape(level,p.above()).isEmpty()
     &&level.getBlockState(p.below()).isFaceSturdy(level,p.below(),Direction.UP))return p;
  }
  return null;
 }
 private void addCandidate(BlockPos requested){
  if(positions.size()>=10)return;
  BlockPos point=standingPoint(requested);
  if(point==null||positions.containsValue(point)||point.distSqr(goal)>48*48||point.distSqr(positions.get("start"))>48*48)return;
  String id="waypoint"+(positions.size()-2);positions.put(id,point);
  searches.add(new Search("start",id));searches.add(new Search(id,"goal"));
 }
 public boolean done(){return done;}
 public Optional<RouteAssignment> result(){return Optional.ofNullable(result);}
 /** One bounded native search per call, subject to the shared world budget. */
 public void advance(){
  if(done)return;
  long now=level.getGameTime();
  if(!mob.isAlive()||mob.level()!=level||now-lastSearch>100||now-started>600||now<started){done=true;return;}
  if(crowding==null){crowding=ObservedCrowding.sample(mob);if(crowding==null)return;}
  var search=searches.get(cursor);
  var found=NativeRouteSearch.find(mob,positions.get(search.from()),positions.get(search.to()));
  if(found.status()==NativeRouteSearch.Status.DEFERRED)return;
  lastSearch=now;
  if(found.status()==NativeRouteSearch.Status.FOUND){
   var path=found.path();paths.put(search,path);
   edges.add(new RouteGraph.Edge(search.from(),search.to(),cost(path),RouteGraph.Mode.WALK,1));
  }
  if(++cursor==searches.size())finish();
 }
 private RouteGraph.Cost cost(Path path){
  double exposure=0;
  for(int i=0;i<path.getNodeCount();i++){
   var node=path.getNode(i);
   for(var danger:dangers){
    var k=danger.key();double d=BlockPos.containing(k.x(),k.y(),k.z()).distSqr(new BlockPos(node.x,node.y,node.z));
    if(d<=36)exposure+=HiveMemory.relevance(danger,level.getGameTime())*12/(1+d);
   }
  }
  // Unknown terrain is not marked safe; every unlearned walking segment carries a small cost.
  return new RouteGraph.Cost(Math.max(0,path.getNodeCount()-1)*5,exposure,ObservedCrowding.cost(path,crowding,mob),0,path.getNodeCount()*.2);
 }
 private void finish(){
  // A passage may close after an early edge was searched but before the graph is complete.
  edges.removeIf(e->NativeRouteSearch.crossesClosedGate(level,paths.get(new Search(e.from(),e.to()))));
  done=true;var nodes=positions.entrySet().stream().map(e->new RouteGraph.Node(e.getKey(),e.getValue().getX(),e.getValue().getY(),e.getValue().getZ())).toList();
  var chosen=new RouteGraph(nodes,edges).shortestPath("start","goal",1);
  if(chosen.isEmpty())return;
  var route=chosen.get();var legs=route.edges().stream().map(e->paths.get(new Search(e.from(),e.to()))).toList();
  result=new RouteAssignment(goal,started+800,route,legs);
 }
}
