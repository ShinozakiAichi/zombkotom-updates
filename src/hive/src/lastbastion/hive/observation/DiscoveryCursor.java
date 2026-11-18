package lastbastion.hive.observation;
import java.util.*;

/** Resume bounded discovery instead of repeatedly sampling the first map entries. */
public final class DiscoveryCursor<K> {
 private Map<K,?> source;private Iterator<K> iterator;private int size;
 public List<K> next(Map<K,?> blocks,int budget){
  if(budget<=0||blocks.isEmpty())return List.of();
  if(source!=blocks||size!=blocks.size()||iterator==null){
   source=blocks;size=blocks.size();iterator=blocks.keySet().iterator();
  }
  var result=new ArrayList<K>(Math.min(budget,blocks.size()));
  // A changed map may invalidate an iterator even when its size is unchanged.
  // Restart consumes an attempt, preserving the inspection budget under mutation.
  for(int attempts=0;attempts<budget;attempts++){
   try {
    if(!iterator.hasNext()){
     iterator=blocks.keySet().iterator();
     if(!result.isEmpty())break;
     if(!iterator.hasNext())break;
    }
    result.add(iterator.next());
   }catch(ConcurrentModificationException changed){iterator=blocks.keySet().iterator();}
  }
  return List.copyOf(result);
 }
}
