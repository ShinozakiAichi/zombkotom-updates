package lastbastion.hive;
import java.util.*;
/** Counts unloaded mines too; leases expire in world time, not wall-clock time. */
public final class MineLedger {
 public static final int TOTAL=512,PER_OWNER=80;
 public static final long LIFETIME=36000;
 public record Lease(UUID mine,UUID owner,long expires){
  public Lease{Objects.requireNonNull(mine);Objects.requireNonNull(owner);if(expires<0)throw new IllegalArgumentException();}
 }
 private final Map<UUID,Lease> leases=new LinkedHashMap<>();
 public boolean expire(long now){return leases.values().removeIf(l->l.expires()<=now);}
 public boolean reserve(UUID mine,UUID owner,long now){
  if(now<0||now>Long.MAX_VALUE-LIFETIME)throw new IllegalArgumentException();
  expire(now);
  if(leases.containsKey(mine)||leases.size()>=TOTAL||leases.values().stream().filter(l->l.owner().equals(owner)).count()>=PER_OWNER)return false;
  leases.put(mine,new Lease(mine,owner,now+LIFETIME));return true;
 }
 public boolean valid(UUID mine,UUID owner,long now){var lease=leases.get(mine);return lease!=null&&lease.owner().equals(owner)&&lease.expires()>now;}
 public boolean remove(UUID mine){return leases.remove(mine)!=null;}
 public List<Lease> snapshot(){return List.copyOf(leases.values());}
 public void restore(List<Lease> saved){
  if(saved.size()>TOTAL)throw new IllegalArgumentException("Mine ledger oversized");
  var validated=new LinkedHashMap<UUID,Lease>();var owners=new HashMap<UUID,Integer>();
  for(var lease:saved){
   if(validated.put(lease.mine(),lease)!=null||owners.merge(lease.owner(),1,Integer::sum)>PER_OWNER)throw new IllegalArgumentException("Invalid mine ledger");
  }
  leases.clear();leases.putAll(validated);
 }
}
