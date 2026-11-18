package lastbastion.hive.coordination;
import java.util.*;

/** Bounded recent observed areas, preferring underserved areas when a group slot opens. */
public final class ObjectiveAdmission {
 private record Request(long observed,long served){}
 private final Map<String,Request> requests=new LinkedHashMap<>();
 private final int capacity;private final long ttl;private long lastTick=Long.MIN_VALUE;
 public ObjectiveAdmission(int capacity,long ttl){
  if(capacity<1||ttl<1)throw new IllegalArgumentException();this.capacity=capacity;this.ttl=ttl;
 }
 private void advance(long now){
  if(now<lastTick)requests.clear();lastTick=now;
  requests.entrySet().removeIf(e->now-e.getValue().observed()>ttl);
 }
 public void observe(String area,long now){
  Objects.requireNonNull(area);if(area.isBlank())throw new IllegalArgumentException("Empty area");advance(now);
  var old=requests.get(area);requests.put(area,new Request(now,old==null?Long.MIN_VALUE:old.served()));
  if(requests.size()>capacity){
   var oldest=requests.entrySet().stream().min(Comparator.comparingLong(e->e.getValue().observed())).orElseThrow();
   requests.remove(oldest.getKey());
  }
 }
 public boolean mayAdmit(String area,Map<String,Integer> active,int limit,long now){
  advance(now);if(limit<=0||!requests.containsKey(area))return false;
  long total=0;for(int count:active.values()){if(count<0)throw new IllegalArgumentException("Negative groups");total+=count;}
  if(total>=limit)return false;
  return requests.keySet().stream().min(Comparator
   .comparingInt((String key)->active.getOrDefault(key,0))
   .thenComparingLong(key->requests.get(key).served())
   .thenComparing(Comparator.naturalOrder())).orElseThrow().equals(area);
 }
 public void admitted(String area,long now){
  advance(now);var request=requests.get(area);
  if(request==null)throw new IllegalArgumentException("Admission requires a recent observation");
  requests.put(area,new Request(request.observed(),now));
 }
 public int size(){return requests.size();}
}
