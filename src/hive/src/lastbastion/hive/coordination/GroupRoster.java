package lastbastion.hive.coordination;
import java.util.*;

/** Per-dimension, nonpersistent, server-thread roster; no entity references or chunk ownership. */
public final class GroupRoster {
 public static final int MAX_GROUPS=6,MAX_MEMBERS=16;
 public static final class Group {
  private final UUID id=UUID.randomUUID();
  private final String objective;
  private final LinkedHashSet<UUID> members;
  private final EncounterState state;
  private int losses;
  private Group(String objective,List<UUID> members,long now,long positionTicks){this.objective=objective;this.members=new LinkedHashSet<>(members);state=new EncounterState(now,positionTicks);}
  public UUID id(){return id;}
  public String objective(){return objective;}
  public List<UUID> members(){return List.copyOf(members);}
  public int losses(){return losses;}
  public EncounterState state(){return state;}
 }
 private final Map<UUID,Group> groups=new LinkedHashMap<>();
 private final Map<UUID,Group> assignments=new HashMap<>();
 public Optional<Group> create(String objective,List<UUID> members,long now){return create(objective,members,now,EncounterState.POSITION_TICKS);}
 public Optional<Group> create(String objective,List<UUID> members,long now,long positionTicks){
  Objects.requireNonNull(objective);Objects.requireNonNull(members);
  if(objective.isBlank()||members.isEmpty()||members.size()>MAX_MEMBERS||groups.size()>=MAX_GROUPS)return Optional.empty();
  var unique=new HashSet<UUID>();
  for(var id:members)if(id==null||assignments.containsKey(id)||!unique.add(id))return Optional.empty();
  var group=new Group(objective,members,now,positionTicks);groups.put(group.id,group);
  for(var id:members)assignments.put(id,group);
  return Optional.of(group);
 }
 public List<Group> groups(){return List.copyOf(groups.values());}
 public Optional<Group> assignment(UUID member){return Optional.ofNullable(assignments.get(member));}
 public boolean remove(UUID member){
  var group=assignments.remove(member);if(group==null)return false;
  group.members.remove(member);group.losses++;
  if(group.members.isEmpty())groups.remove(group.id);
  return true;
 }
 public void release(UUID groupId){
  var group=groups.remove(groupId);if(group==null)return;
  for(var member:group.members)assignments.remove(member,group);
  group.members.clear();
 }
}
