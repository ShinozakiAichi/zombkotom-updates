package lastbastion.hive.observation;
import java.util.*;

/** Short-lived observations of mechanical occupancy; unknown is never treated as open. */
public final class GateHistory {
 public enum Change {NONE,OPENED,CLOSED}
 private record Sample(boolean open,long tick){}
 private final int capacity;private final long lifetime;
 private final LinkedHashMap<Long,Sample> samples=new LinkedHashMap<>();private long lastTick=Long.MIN_VALUE;
 public GateHistory(int capacity,long lifetime){if(capacity<1||lifetime<1)throw new IllegalArgumentException();this.capacity=capacity;this.lifetime=lifetime;}
 private void prune(long now){if(now<lastTick)samples.clear();lastTick=now;samples.entrySet().removeIf(e->now-e.getValue().tick>lifetime);}
 public Change observe(long position,boolean open,long now){
  prune(now);var previous=samples.remove(position);samples.put(position,new Sample(open,now));
  while(samples.size()>capacity)samples.remove(samples.keySet().iterator().next());
  return previous==null||previous.open==open?Change.NONE:open?Change.OPENED:Change.CLOSED;
 }
 public List<Long> positions(long now){prune(now);return List.copyOf(samples.keySet());}
 public Set<Long> closedPositions(long now){
  prune(now);var closed=new HashSet<Long>();
  samples.forEach((position,sample)->{if(!sample.open())closed.add(position);});
  return Set.copyOf(closed);
 }
 public int size(){return samples.size();}
}
