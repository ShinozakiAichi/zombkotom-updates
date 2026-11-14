package lastbastion.hive;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.Zombie;
import java.util.*;

/** Loaded-entity assignments only: finite life, no chunk tickets, no teleports. */
public final class LivingFormation {
 public record Assignment(Group group,int index){}
 public record Group(List<UUID> members,List<PyramidPlan.Slot> slots,long deadline){}
 private static final Map<ServerLevel,Map<UUID,Assignment>> WORLDS=new WeakHashMap<>();
 private LivingFormation(){}
 public static Assignment assignment(Zombie zombie){
  if(!(zombie.level() instanceof ServerLevel level))return null;
  var world=WORLDS.get(level);if(world==null)return null;
  var assignment=world.get(zombie.getUUID());
  if(assignment!=null&&level.getGameTime()>=assignment.group.deadline){release(level,assignment.group);return null;}
  return assignment;
 }
 public static boolean form(ServerLevel level,List<Zombie> participants,List<PyramidPlan.Slot> slots){
  if(participants.size()!=slots.size()||slots.size()<3||slots.size()>15)return false;
  var world=WORLDS.computeIfAbsent(level,l->new HashMap<>());
  // Expire abandoned groups even when their entities have unloaded.
  for(var old:new HashSet<>(world.values()))if(level.getGameTime()>=old.group.deadline)release(level,old.group);
  if(world.size()+participants.size()>120)return false;
  var ids=new HashSet<UUID>();
  for(var zombie:participants)if(!zombie.isAlive()||zombie.level()!=level||zombie.isBaby()||zombie.isNoAi()
   ||Math.abs(zombie.getBbHeight()-1.95)>0.1||world.containsKey(zombie.getUUID())||!ids.add(zombie.getUUID()))return false;
  var group=new Group(participants.stream().map(Zombie::getUUID).toList(),List.copyOf(slots),level.getGameTime()+600);
  for(int i=0;i<participants.size();i++)world.put(participants.get(i).getUUID(),new Assignment(group,i));
  return true;
 }
 public static Zombie member(ServerLevel level,Group group,int index){
  var entity=level.getEntity(group.members.get(index));return entity instanceof Zombie zombie&&zombie.isAlive()?zombie:null;
 }
 public static boolean ready(ServerLevel level,Group group,int index){
  var zombie=member(level,group,index);var slot=group.slots.get(index);
  return zombie!=null&&LadderState.supports(zombie)&&zombie.distanceToSqr(slot.x(),slot.y(),slot.z())<0.1;
 }
 /** Only the apex may leave without taking a support away from another climber. */
 public static void departApex(Zombie zombie){
  var assignment=assignment(zombie);if(assignment==null)return;
  if(assignment.index()!=assignment.group().slots().size()-1)return;
  var world=WORLDS.get((ServerLevel)zombie.level());
  if(world!=null)world.remove(zombie.getUUID());
  LadderState.setSupport(zombie,false);
 }
 public static void release(ServerLevel level,Group group){
  var world=WORLDS.get(level);if(world==null)return;
  for(var id:group.members){
   var current=world.get(id);if(current!=null&&current.group==group)world.remove(id);
   if(level.getEntity(id) instanceof Zombie zombie)LadderState.setSupport(zombie,false);
  }
 }
}
