package lastbastion.hive;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import java.util.ArrayList;
import java.util.List;

/** One bounded hive per dimension; no dependency on loaded or surviving observers. */
public final class HiveSavedData extends SavedData {
    public static final int CAPACITY=4096;
    private static final int FORMAT=1;
    private final HiveMemory memory;
    private HiveSavedData(String dimension){memory=new HiveMemory(dimension,CAPACITY);}
    public static HiveSavedData get(ServerLevel level){
        String dimension=level.dimension().location().toString();
        return level.getDataStorage().computeIfAbsent(new SavedData.Factory<>(
            ()->new HiveSavedData(dimension),
            (tag,provider)->load(dimension,tag),null),"lastbastion_hive");
    }
    public void observe(HiveMemory.Key key,long tick,double evidence){memory.observe(key,tick,evidence);setDirty();}
    public void disprove(HiveMemory.Key key){if(memory.disprove(key))setDirty();}
    public List<HiveMemory.Observation> nearby(HiveMemory.Kind kind,double x,double y,double z,double radius,long tick,int limit){
        return memory.nearby(kind,x,y,z,radius,tick,limit);
    }
    public List<HiveMemory.Observation> snapshot(){return memory.snapshot();}
    static HiveSavedData load(String dimension,CompoundTag tag){
        if(tag.getInt("format")!=FORMAT)throw new IllegalArgumentException("Unsupported hive memory format");
        if(!dimension.equals(tag.getString("dimension")))throw new IllegalArgumentException("Hive dimension mismatch");
        ListTag entries=tag.getList("observations",Tag.TAG_COMPOUND);
        if(entries.size()>CAPACITY)throw new IllegalArgumentException("Oversized hive memory");
        var observations=new ArrayList<HiveMemory.Observation>();
        for(int i=0;i<entries.size();i++){
            CompoundTag entry=entries.getCompound(i);
            var kind=HiveMemory.Kind.valueOf(entry.getString("kind"));
            observations.add(new HiveMemory.Observation(new HiveMemory.Key(kind,entry.getInt("x"),entry.getInt("y"),entry.getInt("z")),entry.getLong("tick"),entry.getDouble("strength")));
        }
        var result=new HiveSavedData(dimension);result.memory.restore(dimension,observations);return result;
    }
    @Override public CompoundTag save(CompoundTag tag,HolderLookup.Provider registries){
        tag.putInt("format",FORMAT);tag.putString("dimension",memory.dimension());var entries=new ListTag();
        for(var observation:memory.snapshot()){
            var entry=new CompoundTag();var key=observation.key();
            entry.putString("kind",key.kind().name());entry.putInt("x",key.x());entry.putInt("y",key.y());entry.putInt("z",key.z());
            entry.putLong("tick",observation.observedAt());entry.putDouble("strength",observation.strength());entries.add(entry);
        }
        tag.put("observations",entries);return tag;
    }
}
