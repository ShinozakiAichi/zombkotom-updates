package lastbastion.hive;
import java.util.*;

/** Bounded FIFO admission; repeated callers cannot overtake waiting actors. */
public final class FairWorkBudget {
 private final int limit,capacity;private final long ttl;
 private final LinkedHashMap<UUID,Long> waiting=new LinkedHashMap<>();
 private long tick=Long.MIN_VALUE;private int used;
 public FairWorkBudget(int limit,int capacity,long ttl){
  if(limit<1||capacity<limit||ttl<1)throw new IllegalArgumentException();
  this.limit=limit;this.capacity=capacity;this.ttl=ttl;
 }
 public boolean reserve(UUID actor,long now){
  Objects.requireNonNull(actor);
  if(now!=tick){
   if(now<tick)waiting.clear();
   waiting.entrySet().removeIf(e->now-e.getValue()>ttl);
   tick=now;used=0;
  }
  if(!waiting.containsKey(actor)&&waiting.size()>=capacity)return false;
  waiting.put(actor,now);
  if(used>=limit)return false;
  int rank=0;
  for(UUID next:waiting.keySet()){
   if(next.equals(actor))break;
   if(++rank>=limit-used)return false;
  }
  waiting.remove(actor);used++;return true;
 }
 public int pending(){return waiting.size();}
}
