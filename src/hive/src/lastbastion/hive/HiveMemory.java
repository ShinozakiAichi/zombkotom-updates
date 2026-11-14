package lastbastion.hive;

import java.util.*;

/** Bounded observations owned by a dimension, never by an observer entity. */
public final class HiveMemory {
    public enum Kind { ENTRANCE, SIGHTING, DANGER, SUCCESSFUL_ROUTE, SUPPLY_ROUTE }
    public record Key(Kind kind, int x, int y, int z) {
        public Key { Objects.requireNonNull(kind); }
    }
    public record Observation(Key key, long observedAt, double strength) {
        public Observation {
            Objects.requireNonNull(key);
            if(observedAt<0 || !Double.isFinite(strength) || strength<=0 || strength>100)
                throw new IllegalArgumentException("Invalid observation");
        }
    }
    private final String dimension;
    private final int capacity;
    private final Map<Key,Observation> records=new LinkedHashMap<>();
    public HiveMemory(String dimension,int capacity) {
        if(dimension==null||dimension.isBlank()||capacity<1)throw new IllegalArgumentException();
        this.dimension=dimension;this.capacity=capacity;
    }
    public String dimension(){return dimension;}
    public int size(){return records.size();}
    public static double relevance(Observation observation,long now) {
        if(now<observation.observedAt) return 0;
        if(observation.key.kind==Kind.ENTRANCE)return observation.strength;
        double age=(double)now-observation.observedAt;
        return observation.strength*Math.pow(0.5,age/24000.0);
    }
    public void observe(Key key,long now,double evidence) {
        if(now<0||!Double.isFinite(evidence)||evidence<=0)throw new IllegalArgumentException();
        Observation old=records.get(key);
        if(old!=null&&now<old.observedAt)return;
        double strength=Math.min(100,evidence+(old==null?0:relevance(old,now)));
        records.put(key,new Observation(key,now,strength));
        if(records.size()>capacity){
            // Evict weakest, then oldest; deterministic insertion order breaks remaining ties.
            Key weakest=records.values().stream().min(Comparator
                .comparingDouble((Observation o)->relevance(o,now))
                .thenComparingLong(Observation::observedAt)).orElseThrow().key;
            records.remove(weakest);
        }
    }
    /** Only call when actual observation disproves a landmark, not when an observer dies. */
    public boolean disprove(Key key){return records.remove(key)!=null;}
    public List<Observation> snapshot(){return List.copyOf(records.values());}
    public void restore(String savedDimension,List<Observation> saved) {
        if(!dimension.equals(savedDimension))throw new IllegalArgumentException("Wrong dimension");
        if(saved.size()>capacity)throw new IllegalArgumentException("Memory exceeds capacity");
        Map<Key,Observation> validated=new LinkedHashMap<>();
        for(var o:saved){Objects.requireNonNull(o);if(validated.put(o.key,o)!=null)throw new IllegalArgumentException("Duplicate landmark");}
        records.clear();records.putAll(validated);
    }
    public List<Observation> nearby(Kind kind,double x,double y,double z,double radius,long now,int limit) {
        if(!Double.isFinite(x)||!Double.isFinite(y)||!Double.isFinite(z)||!Double.isFinite(radius)||radius<0||limit<0)throw new IllegalArgumentException();
        return records.values().stream().filter(o->o.key.kind==kind)
            .filter(o->Math.pow(o.key.x-x,2)+Math.pow(o.key.y-y,2)+Math.pow(o.key.z-z,2)<=radius*radius)
            .filter(o->relevance(o,now)>=0.01)
            .sorted(Comparator.comparingDouble((Observation o)->relevance(o,now)).reversed())
            .limit(limit).toList();
    }
}
