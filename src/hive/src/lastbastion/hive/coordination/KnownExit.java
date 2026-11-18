package lastbastion.hive.coordination;
import lastbastion.hive.HiveMemory;
import java.util.*;

/** A watch position must come from entrance evidence, never a guessed base layout. */
public final class KnownExit {
 private KnownExit(){}
 public static Optional<HiveMemory.Key> choose(List<HiveMemory.Observation> observations,int x,int y,int z,long now){
  return observations.stream().filter(o->o.key().kind()==HiveMemory.Kind.ENTRANCE&&HiveMemory.relevance(o,now)>0)
   .filter(o->distance(o.key(),x,y,z)<=24*24)
   .min(Comparator.comparingDouble((HiveMemory.Observation o)->distance(o.key(),x,y,z)))
   .map(HiveMemory.Observation::key);
 }
 private static double distance(HiveMemory.Key k,int x,int y,int z){return Math.pow((double)k.x()-x,2)+Math.pow((double)k.y()-y,2)+Math.pow((double)k.z()-z,2);}
}
