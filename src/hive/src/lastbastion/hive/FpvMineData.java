package lastbastion.hive;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import java.util.*;
public final class FpvMineData extends SavedData {
 private final MineLedger ledger=new MineLedger();
 private long lastPurge=Long.MIN_VALUE;
 public static FpvMineData get(ServerLevel level){return level.getServer().overworld().getDataStorage().computeIfAbsent(new SavedData.Factory<>(FpvMineData::new,(tag,provider)->load(tag),null),"lastbastion_fpv_mines");}
 public static long now(ServerLevel level){return level.getServer().overworld().getGameTime();}
 public boolean reserve(UUID mine,UUID owner,long now){boolean accepted=ledger.reserve(mine,owner,now);setDirty();return accepted;}
 public boolean valid(UUID mine,UUID owner,long now){
  if(lastPurge==Long.MIN_VALUE||now<lastPurge||now-lastPurge>=20){lastPurge=now;if(ledger.expire(now))setDirty();}
  return ledger.valid(mine,owner,now);
 }
 public void remove(UUID mine){if(ledger.remove(mine))setDirty();}
 public static FpvMineData load(CompoundTag tag){
  if(tag.getInt("format")!=1)throw new IllegalArgumentException("Unsupported mine ledger format");
  var entries=tag.getList("leases",Tag.TAG_COMPOUND);if(entries.size()>MineLedger.TOTAL)throw new IllegalArgumentException("Mine ledger oversized");var leases=new ArrayList<MineLedger.Lease>();
  for(int i=0;i<entries.size();i++){var e=entries.getCompound(i);leases.add(new MineLedger.Lease(e.getUUID("mine"),e.getUUID("owner"),e.getLong("expires")));}
  var result=new FpvMineData();result.ledger.restore(leases);return result;
 }
 @Override public CompoundTag save(CompoundTag tag,HolderLookup.Provider provider){
  tag.putInt("format",1);var entries=new ListTag();
  for(var lease:ledger.snapshot()){var e=new CompoundTag();e.putUUID("mine",lease.mine());e.putUUID("owner",lease.owner());e.putLong("expires",lease.expires());entries.add(e);}
  tag.put("leases",entries);return tag;
 }
}
