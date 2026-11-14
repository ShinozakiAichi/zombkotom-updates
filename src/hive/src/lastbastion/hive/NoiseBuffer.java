package lastbastion.hive;
import java.util.*;

/** One transient buffer per ServerLevel. Never retains entity references or loads chunks. */
public final class NoiseBuffer {
    private final int capacity, perSource;
    private final long lifetime;
    private final ArrayDeque<Hearing.Noise> events=new ArrayDeque<>();
    private long lastTick=-1;
    public NoiseBuffer(int capacity,int perSource,long lifetime){
        if(capacity<1||perSource<1||perSource>capacity||lifetime<1)throw new IllegalArgumentException();
        this.capacity=capacity;this.perSource=perSource;this.lifetime=lifetime;
    }
    private void advance(long now){
        if(now<0)throw new IllegalArgumentException("Negative game time");
        // A clock reset must not make old noises appear to come from the future.
        if(now<lastTick)events.clear();
        lastTick=now;
        events.removeIf(e->(double)now-e.tick()>=lifetime);
    }
    public void emit(Hearing.Noise event,long now){
        Objects.requireNonNull(event);advance(now);
        if(event.tick()>now||(double)now-event.tick()>=lifetime)return;
        int count=0;for(var e:events)if(e.source().equals(event.source()))count++;
        if(count>=perSource){
            // Keep a loud recent shot when the same player subsequently takes quiet steps.
            // Always retain the newest position too; listener distance is evaluated later.
            var weakest=events.stream().filter(e->e.source().equals(event.source()))
                .min(Comparator.comparingDouble(e->e.strength()*(1-((double)now-e.tick())/lifetime))).orElseThrow();
            events.removeFirstOccurrence(weakest);
        }
        while(events.size()>=capacity)events.removeFirst();
        events.addLast(event);
    }
    public List<Hearing.Noise> snapshot(long now){advance(now);return List.copyOf(events);}
}
