package lastbastion.hive;
import de.nikpottbecker.fpvcraft.entity.DroneEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import java.util.UUID;

/** Game-only twenty-item rack. All mutation happens on the server. */
public final class FpvPetalRack {
 public static final String KEY="lastbastion_petals";
 public static final int CAPACITY=20;
 public record State(UUID owner,int ammo,boolean valid){
  public boolean allowed(Player player){return valid&&(owner==null||owner.equals(player.getUUID()));}
 }
 private FpvPetalRack(){}
 public static State decode(CompoundTag tag){
  if(tag.isEmpty())return new State(null,0,true);
  int ammo=tag.getInt("ammo");UUID owner=tag.hasUUID("owner")?tag.getUUID("owner"):null;
  boolean valid=tag.getInt("version")==1&&ammo>=0&&ammo<=CAPACITY&&owner!=null;
  return new State(owner,ammo,valid);
 }
 public static State state(DroneEntity drone){return decode(drone.getPersistentData().getCompound(KEY));}
 public static CompoundTag encode(State state){
  if(!state.valid||state.owner==null||state.ammo<0||state.ammo>CAPACITY)throw new IllegalArgumentException("Invalid rack state");
  var tag=new CompoundTag();tag.putInt("version",1);tag.putUUID("owner",state.owner);tag.putInt("ammo",state.ammo);return tag;
 }
 public static void write(DroneEntity drone,State state){
  if(drone.level().isClientSide())throw new IllegalStateException("Server owns ammunition");
  drone.getPersistentData().put(KEY,encode(state));
 }
 public static State itemState(ItemStack stack){return decode(stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getCompound(KEY));}
 public static ItemStack preserve(DroneEntity drone,ItemStack stack){
  var rack=drone.getPersistentData().getCompound(KEY);
  if(!rack.isEmpty()){
   var root=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();root.put(KEY,rack.copy());stack.set(DataComponents.CUSTOM_DATA,CustomData.of(root));
  }
  return stack;
 }
 public static void restore(DroneEntity drone,ItemStack stack){
  var root=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();
  if(root.contains(KEY))drone.getPersistentData().put(KEY,root.getCompound(KEY).copy());
 }
 public static boolean isMine(ItemStack stack){return stack.is(BuiltInRegistries.ITEM.get(ResourceLocation.parse("superbwarfare:blu_43_mine")));}
 /** Server-authoritative single release; callers never supply an ammunition count. */
 public static boolean release(DroneEntity drone,net.minecraft.server.level.ServerPlayer player){
  if(drone.level().isClientSide()||drone.isRemoved()||!drone.isAlive()||!player.isAlive()
    ||player.level()!=drone.level()||!drone.isPilotedBy(player)||!drone.isArmed())return false;
  var old=state(drone);
  if(!old.allowed(player)||old.owner==null||old.ammo<=0)return false;
  long now=drone.level().getGameTime();
  var data=drone.getPersistentData();
  if(data.contains("lastbastion_last_release")){
   long elapsed=now-data.getLong("lastbastion_last_release");
   if(elapsed>=0&&elapsed<10)return false;
  }
  var mine=new com.atsuishio.superbwarfare.entity.projectile.Blu43Entity(player,drone.level());
  mine.setOwnerUUID(player.getUUID());
  mine.setPos(drone.getX(),drone.getY()-.25,drone.getZ());
  mine.setDeltaMovement(drone.getDeltaMovement().scale(.5));
  var level=(net.minecraft.server.level.ServerLevel)drone.level();var ledger=FpvMineData.get(level);
  if(!ledger.reserve(mine.getUUID(),player.getUUID(),FpvMineData.now(level))){
   player.displayClientMessage(Component.literal("Лимит мин FPV: до 80 на игрока и 512 на мир. Дождитесь освобождения мест."),true);return false;
  }
  mine.getPersistentData().putBoolean(FpvMineLifecycle.MARKER,true);
  if(!drone.level().addFreshEntity(mine)){ledger.remove(mine.getUUID());return false;}
  write(drone,new State(old.owner,old.ammo-1,true));
  data.putLong("lastbastion_last_release",now);
  player.displayClientMessage(Component.literal("Кассета: "+(old.ammo-1)+" / "+CAPACITY),true);
  return true;
 }
 public static boolean load(DroneEntity drone,Player player,ItemStack stack){
  if(drone.level().isClientSide()||!isMine(stack)||!state(drone).allowed(player)||drone.getPilot().isPresent()||drone.isArmed())return false;
  var old=state(drone);int amount=Math.min(CAPACITY-old.ammo,player.isShiftKeyDown()?stack.getCount():1);
  if(amount<=0)return false;
  write(drone,new State(old.owner==null?player.getUUID():old.owner,old.ammo+amount,true));
  if(!player.getAbilities().instabuild)stack.shrink(amount);
  player.displayClientMessage(Component.literal("Кассета: "+(old.ammo+amount)+" / "+CAPACITY),true);return true;
 }
}
