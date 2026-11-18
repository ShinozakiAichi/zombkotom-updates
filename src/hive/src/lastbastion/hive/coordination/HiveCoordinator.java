package lastbastion.hive.coordination;
import lastbastion.hive.LivingFormation;
import lastbastion.hive.HiveSavedData;
import lastbastion.hive.HiveMemory;
import lastbastion.hive.WorldHearing;
import lastbastion.hive.routing.WallAssault;
import lastbastion.hive.routing.RouteGraph;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import java.util.*;

/** Coordination stores only observed locations. It never retains or resolves a player UUID. */
public final class HiveCoordinator {
 public enum Role {SCOUT,PRESSURE,FLANK,EXIT_WATCH,LADDER}
 public record Assignment(UUID group,Role role,BlockPos point,EncounterState.Phase phase){}
 private record Objective(BlockPos point,Vec3 forward,long expires,BlockPos knownExit){}
 private static final class World {
  final GroupRoster roster=new GroupRoster();final Map<UUID,Objective> objectives=new HashMap<>();
  final Map<UUID,Long> cooldown=new LinkedHashMap<>();long lastTick;
  final Map<UUID,Long> wallRetry=new HashMap<>();final Set<UUID> ladderUsed=new HashSet<>();long nextWallTick;
  final Map<UUID,LivingFormation.Group> ladders=new HashMap<>();
  final ObjectiveAdmission admission=new ObjectiveAdmission(64,200);
 }
 private static final Map<ServerLevel,World> WORLDS=new WeakHashMap<>();
 private HiveCoordinator(){}
 private static long night(ServerLevel level){return Math.max(1,level.getDayTime()/24000+1);}
 private static String area(BlockPos point){return Math.floorDiv(point.getX(),32)+":"+Math.floorDiv(point.getY(),16)+":"+Math.floorDiv(point.getZ(),32);}
 private static EncounterState.Phase encounterPhase(ServerLevel level,UUID encounter){
  var w=WORLDS.get(level);if(w==null)return EncounterState.Phase.SEARCH;
  return w.roster.groups().stream().filter(g->g.id().equals(encounter)).map(g->g.state().phase()).findFirst().orElse(EncounterState.Phase.SEARCH);
 }
 public static boolean permitsAssembly(ServerLevel level,UUID encounter){return LadderPhasePolicy.assemble(encounterPhase(level,encounter),SiegeDirector.phase(level));}
 public static boolean permitsCrossing(ServerLevel level,UUID encounter){return LadderPhasePolicy.cross(encounterPhase(level,encounter),SiegeDirector.phase(level));}
 private static void releaseLadder(ServerLevel level,World world,UUID encounter){
  var ladder=world.ladders.remove(encounter);if(ladder!=null)LivingFormation.release(level,ladder);
 }
 public static Assignment assignment(Zombie zombie){
  if(!(zombie.level() instanceof ServerLevel level)||!zombie.isAlive()||zombie.isRemoved())return null;
  var w=WORLDS.get(level);if(w==null)return null;
  var group=w.roster.assignment(zombie.getUUID()).orElse(null);if(group==null)return null;
  var objective=w.objectives.get(group.id());if(objective==null)return null;
  var members=group.members();int index=members.indexOf(zombie.getUUID());if(index<0)return null;
  var phase=group.state().phase();Role role=switch(index%4){case 0->Role.SCOUT;case 1->Role.PRESSURE;case 2->Role.FLANK;default->Role.EXIT_WATCH;};
  var capabilities=ZombieUnitAdapter.INSTANCE.describe(zombie);
  if(!capabilities.supports(UnitCapabilities.Ability.valueOf(role.name())))return null;
  if(role==Role.EXIT_WATCH){
   if(objective.knownExit==null)role=Role.FLANK;
   else if(phase!=EncounterState.Phase.REGROUP&&phase!=EncounterState.Phase.RECOVER)
    return new Assignment(group.id(),role,objective.knownExit,phase);
  }
  Vec3 forward=objective.forward,side=new Vec3(-forward.z,0,forward.x);
  double back=phase==EncounterState.Phase.GATHER?14:phase==EncounterState.Phase.REGROUP||phase==EncounterState.Phase.RECOVER?20:phase==EncounterState.Phase.ASSAULT?0:8;
  double offset=role==Role.FLANK?CohortPolicy.flankOffset(index):role==Role.EXIT_WATCH?-10:(index%3-1)*2;
  if(phase==EncounterState.Phase.ASSAULT)offset*=.35;
  Vec3 point=Vec3.atBottomCenterOf(objective.point).subtract(forward.scale(back)).add(side.scale(offset));
  return new Assignment(group.id(),role,BlockPos.containing(point),phase);
 }
 @SubscribeEvent public static void consider(EntityTickEvent.Post event){
  if(!(event.getEntity() instanceof Zombie z)||!(z.level() instanceof ServerLevel level)||Math.floorMod(level.getGameTime()+z.getId(),40)!=0)return;
  if(!eligible(z)||LivingFormation.assignment(z)!=null)return;
  var kind=CohortPolicy.kind(z.getUUID(),night(level));if(kind==CohortPolicy.Kind.SOLITARY)return;
  var w=WORLDS.computeIfAbsent(level,k->new World());long now=level.getGameTime();
  if(w.roster.assignment(z.getUUID()).isPresent()||w.cooldown.getOrDefault(z.getUUID(),0L)>now)return;
  var target=z.getTarget();BlockPos observed;
  if(target!=null&&target.isAlive()&&z.distanceToSqr(target)<=40*40&&z.getSensing().hasLineOfSight(target))observed=target.blockPosition().immutable();
  else {
   var heard=WorldHearing.listen(level,z.getEyePosition(),null);if(heard.isEmpty())return;
   var p=heard.get().noise().position();observed=BlockPos.containing(p.x(),p.y(),p.z());
  }
  SiegeDirector.observed(level);if(!SiegeDirector.canRecruit(level))return;
  var forward=Vec3.atBottomCenterOf(observed).subtract(z.position()).multiply(1,0,1);if(forward.lengthSqr()<36)return;
  var candidates=level.getEntitiesOfClass(Zombie.class,z.getBoundingBox().inflate(12,3,12),m->eligible(m)
   &&CohortPolicy.kind(m.getUUID(),night(level))==kind&&LivingFormation.assignment(m)==null
   &&w.roster.assignment(m.getUUID()).isEmpty()&&w.cooldown.getOrDefault(m.getUUID(),0L)<=now);
  candidates.sort(Comparator.comparingDouble(z::distanceToSqr));
  int limit=CohortPolicy.memberLimit(kind,night(level));
  var ids=candidates.stream().limit(limit).map(Zombie::getUUID).toList();if(ids.size()<2)return;
  // Remember a genuinely observed area with enough eligible participants even at capacity.
  // Newly freed slots prefer underserved areas; hearing still selects each observer's loudest source.
  String area=area(observed);w.admission.observe(area,now);
  var active=new HashMap<String,Integer>();for(var existing:w.roster.groups())active.merge(existing.objective(),1,Integer::sum);
  if(!w.admission.mayAdmit(area,active,CohortPolicy.groupLimit(night(level)),now))return;
  // Conservative travel allowance for an observed point; director recovery still overrides it.
  long travelTicks=(long)Math.ceil(forward.length()/.1)+80;
  var group=w.roster.create(area,ids,now,travelTicks).orElse(null);if(group==null)return;
  w.admission.admitted(area,now);
  var entrances=HiveSavedData.get(level).nearby(HiveMemory.Kind.ENTRANCE,observed.getX(),observed.getY(),observed.getZ(),24,now,16);
  var exit=KnownExit.choose(entrances,observed.getX(),observed.getY(),observed.getZ(),now)
   .map(k->new BlockPos(k.x(),k.y(),k.z())).orElse(null);
  w.objectives.put(group.id(),new Objective(observed,forward.normalize(),now+1200,exit));
  group.state().advance(now,true,0,ids.size(),0);
 }
 private static boolean eligible(Zombie z){return z.isAlive()&&!z.isRemoved()&&!z.isNoAi()&&ZombieUnitAdapter.INSTANCE.describe(z).supports(UnitCapabilities.Ability.COORDINATE)&&z.onGround()&&!z.getTags().contains("lb_hive_dormant");}
 @SubscribeEvent public static void tick(ServerTickEvent.Post event){
  for(var level:event.getServer().getAllLevels()){
   var w=WORLDS.get(level);if(w==null)continue;long now=level.getGameTime();
   if(now<w.lastTick){for(var ladder:w.ladders.values())LivingFormation.release(level,ladder);WORLDS.remove(level);continue;}w.lastTick=now;if(now%10!=0)continue;
   w.cooldown.entrySet().removeIf(e->e.getValue()<=now);
   for(var group:w.roster.groups()){
    var objective=w.objectives.get(group.id());int ready=0;
    for(var id:group.members()){
     var entity=level.getEntity(id);
     if(!(entity instanceof Zombie z)||!z.isAlive()||z.isRemoved()){w.roster.remove(id);continue;}
     var a=assignment(z);var formation=LivingFormation.assignment(z);
     boolean assembled=formation!=null&&group.id().equals(formation.group().encounter())&&LivingFormation.ready(level,formation.group(),formation.index());
     if(assembled||a!=null&&z.distanceToSqr(Vec3.atBottomCenterOf(a.point))<9)ready++;
    }
    var siege=SiegeDirector.phase(level);
    int alive=group.members().size();var phase=group.state().advance(now,objective!=null&&now<objective.expires&&siege!=SiegeClock.Phase.RECOVER,ready,alive,group.losses(),siege==SiegeClock.Phase.ASSAULT);
    if(!LadderPhasePolicy.assemble(phase,siege))releaseLadder(level,w,group.id());
    if(objective!=null&&LadderPhasePolicy.assemble(phase,siege)&&alive>=3
      &&!w.ladderUsed.contains(group.id())&&now>=w.nextWallTick&&now>=w.wallRetry.getOrDefault(group.id(),0L)){
     w.nextWallTick=now+20;w.wallRetry.put(group.id(),now+100);tryWall(level,w,group,objective);
    }
    if(alive==0||phase==EncounterState.Phase.SEARCH){
     for(var id:group.members()){w.cooldown.put(id,now+400);if(w.cooldown.size()>512)w.cooldown.remove(w.cooldown.keySet().iterator().next());}
     releaseLadder(level,w,group.id());w.roster.release(group.id());w.objectives.remove(group.id());
    }
   }
   w.objectives.keySet().removeIf(id->w.roster.groups().stream().noneMatch(g->g.id().equals(id)));
   for(var id:new ArrayList<>(w.ladders.keySet()))if(!w.objectives.containsKey(id))releaseLadder(level,w,id);
   w.wallRetry.keySet().retainAll(w.objectives.keySet());w.ladderUsed.retainAll(w.objectives.keySet());
  }
 }
 private static void tryWall(ServerLevel level,World world,GroupRoster.Group group,Objective objective){
  var bodies=new ArrayList<Zombie>();
  for(var id:group.members())if(level.getEntity(id) instanceof Zombie z&&eligible(z)&&ZombieUnitAdapter.INSTANCE.describe(z).supports(UnitCapabilities.Ability.LADDER)&&LivingFormation.assignment(z)==null)bodies.add(z);
  if(bodies.size()<3)return;
  bodies.sort(Comparator.comparingDouble(z->z.distanceToSqr(Vec3.atBottomCenterOf(objective.point))));
  var scout=bodies.get(0);bodies.removeIf(z->z.distanceToSqr(scout)>24*24);
  if(bodies.size()<3)return;
  var choice=WallAssault.choose(scout,objective.point,bodies.size()).orElse(null);
  if(choice==null||choice.ladder()==null||choice.route().edges().stream().noneMatch(e->e.mode()==RouteGraph.Mode.LADDER))return;
  if(!WallAssault.clear(scout,choice.ladder()))return;
  var selected=new ArrayList<Zombie>();
  for(var slot:choice.ladder().slots()){
   bodies.sort(Comparator.comparingDouble(z->z.distanceToSqr(slot.x(),scout.getY(),slot.z())));selected.add(bodies.remove(0));
  }
  if(LivingFormation.form(level,selected,choice.ladder().slots(),choice.exit(),group.id())){
   world.ladderUsed.add(group.id());var assignment=LivingFormation.assignment(selected.get(0));
   if(assignment!=null)world.ladders.put(group.id(),assignment.group());
  }
 }
 @SubscribeEvent public static void leave(EntityLeaveLevelEvent event){
  if(!(event.getLevel() instanceof ServerLevel level))return;var w=WORLDS.get(level);if(w!=null)w.roster.remove(event.getEntity().getUUID());
 }
}
