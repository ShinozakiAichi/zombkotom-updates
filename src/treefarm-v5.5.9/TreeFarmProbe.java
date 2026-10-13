package treefarmqa;
import com.google.gson.*;
import com.simibubi.create.content.contraptions.ControlledContraptionEntity;
import com.simibubi.create.content.contraptions.bearing.MechanicalBearingBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import net.minecraft.client.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import java.nio.file.*;
import java.util.*;

/** Native rotating saw/replanting test in a private world. No direct tree cutting or output grants. */
@Mod(value="treefarm_qa",dist=Dist.CLIENT)
public class TreeFarmProbe {
 final BlockPos base=new BlockPos(416,70,416),wheel=base.offset(-10,-1,0),water=wheel.offset(0,3,-1);
 final int[][] pads={{4,0},{3,3},{0,4},{-3,3},{-4,0},{-3,-3},{0,-4},{3,-3}};
 final Path out=Path.of("C:/LastBastion/treefarm-qa");
 final Gson gson=new GsonBuilder().setPrettyPrinting().create();
 final Map<String,Object> report=new LinkedHashMap<>();final List<Map<String,Object>> samples=new ArrayList<>();
 final String[] previousPads=new String[8];final Set<Integer> cutPads=new HashSet<>(),replantedPads=new HashSet<>();
 ServerLevel level;ServerPlayer player;boolean resume,protectedPlayer;int stage,ticks,runTicks,angleChanges,planted,grown,cut,serverContacts,mobContacts,originalRandom;boolean originalDayCycle;long originalDayTime,started;float previousAngle;
 volatile boolean finished,clientReady;volatile UUID movingId;volatile int clientContacts,clientFps;volatile String capture;int readyTicks,delay,clientTicks;int initialLogs;boolean lootShot;String error="";
 public TreeFarmProbe(){try{
  Path root=Minecraft.getInstance().gameDirectory.toPath().toAbsolutePath().normalize();var flag=root.resolve("treefarm-qa.enabled");
  if(!root.getParent().getFileName().toString().equals("ZombKotom-Quest-QA")||!Files.exists(flag))return;
  resume=Files.readString(flag).trim().equals("resume");Files.createDirectories(out.resolve(resume?"resume":"first"));Arrays.fill(previousPads,"air");
  NeoForge.EVENT_BUS.addListener(this::serverTick);NeoForge.EVENT_BUS.addListener(this::clientTick);
 }catch(Exception e){throw new RuntimeException(e);}}
 Path phase(){return out.resolve(resume?"resume":"first");}
 BlockState block(String name){return BuiltInRegistries.BLOCK.get(ResourceLocation.parse(name)).defaultBlockState();}
 void require(boolean b,String why){if(!b)throw new IllegalStateException(why);}
 void set(BlockPos pos,BlockState state){level.setBlock(pos,state,3);}
 void overview(){player.getAbilities().flying=true;player.onUpdateAbilities();player.connection.teleport(416.5,80,428.5,180,35);}
 Map<String,String> shape(ControlledContraptionEntity entity){var m=new TreeMap<String,String>();entity.getContraption().getBlocks().forEach((p,b)->m.put(p.toShortString(),b.state().toString()));return m;}
 Map<String,Integer> cargo(ControlledContraptionEntity entity){var m=new TreeMap<String,Integer>();var inv=entity.getContraption().getStorage().getAllItems();for(int i=0;i<inv.getSlots();i++){var s=inv.getStackInSlot(i);if(!s.isEmpty())m.merge(BuiltInRegistries.ITEM.getKey(s.getItem()).toString(),s.getCount(),Integer::sum);}return m;}
 void prepare(){
  set(water,Blocks.AIR.defaultBlockState());if(level.getBlockEntity(base) instanceof MechanicalBearingBlockEntity old&&old.isRunning())old.disassemble();
  for(var pos:BlockPos.betweenClosed(base.offset(-12,-5,-9),base.offset(9,15,9)))set(pos,Blocks.AIR.defaultBlockState());
  for(int x=-8;x<=8;x++)for(int z=-8;z<=8;z++)set(base.offset(x,0,z),Blocks.SMOOTH_STONE.defaultBlockState());
  for(var p:pads)set(base.offset(p[0],0,p[1]),Blocks.DIRT.defaultBlockState());
  set(base,block("create:mechanical_bearing").setValue(BlockStateProperties.FACING,Direction.UP));
  set(base.above(),Blocks.SLIME_BLOCK.defaultBlockState());
  for(int x=-3;x<=4;x++)set(base.offset(x,2,0),Blocks.SLIME_BLOCK.defaultBlockState());
  set(base.offset(-3,3,0),Blocks.SLIME_BLOCK.defaultBlockState());set(base.offset(-3,4,0),Blocks.SLIME_BLOCK.defaultBlockState());set(base.offset(-4,4,0),Blocks.SLIME_BLOCK.defaultBlockState());
  set(base.offset(4,1,0),block("create:mechanical_saw").setValue(BlockStateProperties.FACING,Direction.NORTH));
  set(base.offset(-4,3,0),block("create:deployer").setValue(BlockStateProperties.FACING,Direction.DOWN));
  var chest=base.offset(0,2,1);set(chest,Blocks.CHEST.defaultBlockState());var c=(ChestBlockEntity)level.getBlockEntity(chest);c.setItem(0,new ItemStack(Items.OAK_SAPLING,64));c.setItem(1,new ItemStack(Items.DIAMOND,7));
  for(int x=-9;x<0;x++)set(base.offset(x,-1,0),block("create:shaft").setValue(BlockStateProperties.AXIS,Direction.Axis.X));
  set(base.below(),block("create:gearbox").setValue(BlockStateProperties.AXIS,Direction.Axis.Z));
  set(wheel,block("create:water_wheel").setValue(BlockStateProperties.FACING,Direction.EAST));
 }
 void scanPads(){for(int i=0;i<pads.length;i++){
  var state=level.getBlockState(base.offset(pads[i][0],1,pads[i][1]));String current=state.is(Blocks.OAK_SAPLING)?"sapling":state.is(Blocks.OAK_LOG)?"log":state.isAir()?"air":"other";
  if(!current.equals(previousPads[i])){if(current.equals("sapling")){planted++;if(cutPads.contains(i))replantedPads.add(i);}if(current.equals("log"))grown++;if(previousPads[i].equals("log")&&!current.equals("log")){cut++;cutPads.add(i);}previousPads[i]=current;}
 }}
 void sample(ControlledContraptionEntity e){var row=new LinkedHashMap<String,Object>();row.put("tick",runTicks);row.put("angle",e.getAngle(1));row.put("cargo",cargo(e));row.put("planted",planted);row.put("grown",grown);row.put("cut",cut);row.put("replanted_pads",replantedPads.size());row.put("server_contact_ticks",serverContacts);row.put("client_contact_ticks",clientContacts);row.put("mob_contact_ticks",mobContacts);row.put("average_mspt",level.getServer().getAverageTickTimeNanos()/1_000_000.0);row.put("client_fps_background",clientFps);row.put("heap_used_mib",(Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory())/1048576);samples.add(row);System.out.println("TREEFARM_QA_SAMPLE "+gson.toJson(row));}
 void restoreRules(){if(level!=null&&protectedPlayer){level.getGameRules().getRule(GameRules.RULE_RANDOMTICKING).set(originalRandom,level.getServer());level.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(originalDayCycle,level.getServer());level.setDayTime(originalDayTime);}}
 void finish(){restoreRules();report.put("passed",error.isEmpty());report.put("error",error);report.put("resume",resume);report.put("rotating_ticks",runTicks);report.put("angle_change_ticks",angleChanges);report.put("native_plant_transitions",planted);report.put("native_growth_transitions",grown);report.put("native_cut_transitions",cut);report.put("pads_replanted_after_cut",replantedPads);report.put("server_contact_ticks",serverContacts);report.put("client_contact_ticks",clientContacts);report.put("mob_contact_ticks",mobContacts);report.put("samples",samples);report.put("elapsed_seconds",(System.nanoTime()-started)/1e9);report.put("original_random_tick_speed",originalRandom);report.put("qa_random_tick_speed",120);report.put("creative_motors",0);report.put("manual_log_grants",0);report.put("manual_tree_break_calls",0);report.put("manual_sapling_placements",0);finished=true;capture="final";System.out.println("TREEFARM_QA_RESULT pass="+error.isEmpty()+" resume="+resume);}
 void serverTick(ServerTickEvent.Post event){if(finished)return;try{
  var server=event.getServer();if(server.getPlayerList().getPlayerCount()!=1)return;player=server.getPlayerList().getPlayers().getFirst();level=server.overworld();ticks++;
  if(!protectedPlayer){originalRandom=level.getGameRules().getInt(GameRules.RULE_RANDOMTICKING);originalDayCycle=level.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT);originalDayTime=level.getDayTime();player.setGameMode(GameType.CREATIVE);player.getAbilities().flying=true;player.onUpdateAbilities();protectedPlayer=true;started=System.nanoTime();}
  if(stage==0){if(!clientReady)return;overview();level.getGameRules().getRule(GameRules.RULE_RANDOMTICKING).set(120,server);level.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false,server);level.setDayTime((originalDayTime/24000)*24000+6000);if(!resume)prepare();stage=1;ticks=0;return;}
  if(stage==1&&ticks>=100&&!resume){var deployer=level.getBlockEntity(base.offset(-4,3,0));var filter=BlockEntityBehaviour.get(deployer,FilteringBehaviour.TYPE);require(filter!=null,"No deployer filter before power");filter.setFilter(new ItemStack(Items.OAK_SAPLING));set(water,Blocks.WATER.defaultBlockState());stage=10;ticks=0;return;}
  if((stage==10||stage==1&&resume)&&ticks>=100){var b=(MechanicalBearingBlockEntity)level.getBlockEntity(base);require(b!=null,"Bearing missing");require(Math.abs(b.getSpeed())==8,"Water drive expected 8 rpm, got "+b.getSpeed());require(!b.isOverStressed(),"Water drive overstressed");
   if(!resume&&!b.isRunning())b.assemble();
   var e=b.getMovedContraption();require(e!=null&&b.isRunning(),"No rotating entity "+b.getLastAssemblyException());movingId=e.getUUID();var currentShape=shape(e);require(currentShape.size()==15,"Expected 15 blocks, got "+currentShape.size()+" "+currentShape);var inv=cargo(e);require(inv.getOrDefault("minecraft:diamond",0)==7,"Control cargo missing");initialLogs=inv.getOrDefault("minecraft:oak_log",0);
   if(resume){var old=JsonParser.parseString(Files.readString(out.resolve("first/result.json"))).getAsJsonObject();require(old.get("passed").getAsBoolean(),"First phase failed");require(old.get("shape").equals(gson.toJsonTree(currentShape)),"Saved geometry changed");require(initialLogs>=old.getAsJsonObject("final_cargo").get("minecraft:oak_log").getAsInt(),"Saved wood count decreased");report.put("saved_shape_matches",true);report.put("saved_control_cargo_matches",true);report.put("wood_at_least_previous_output",true);}
   report.put("shape",currentShape);report.put("initial_cargo",inv);report.put("bearing_rpm",b.getSpeed());stage=2;ticks=0;previousAngle=e.getAngle(1);capture="assembled";return;
  }
  if(stage==2){var b=(MechanicalBearingBlockEntity)level.getBlockEntity(base);var e=b.getMovedContraption();require(e!=null&&!e.isRemoved()&&b.isRunning(),"Contraption lost");runTicks++;float angle=e.getAngle(1);require(Float.isFinite(angle),"Invalid angle");if(angle!=previousAngle)angleChanges++;previousAngle=angle;
   if(e.collidingEntities.containsKey(player))serverContacts++;if(e.collidingEntities.keySet().stream().anyMatch(x->x instanceof net.minecraft.world.entity.Mob))mobContacts++;scanPads();
   if(runTicks%200==0)sample(e);var inv=cargo(e);int logs=inv.getOrDefault("minecraft:oak_log",0);
   if(!lootShot&&logs>initialLogs){lootShot=true;capture="harvest";}
   if(runTicks==1000||runTicks==2000){player.getAbilities().flying=false;player.onUpdateAbilities();player.connection.teleport(417.3,73.05,416.5,90,25);}
   if(runTicks==1600||runTicks==2600)overview();
   if(runTicks==1800)require(planted>0||resume,"Deployer never planted a sapling");if(runTicks==3600)require(logs>initialLogs,"No new wood after 3600 ticks");
   if(runTicks>=6000){require(logs>=initialLogs+16,"Insufficient native wood output: "+logs);require(replantedPads.size()>0,"No observed replant after harvest");require(angleChanges>1000,"Too little motion");require(inv.getOrDefault("minecraft:diamond",0)==7,"Control cargo changed");report.put("final_cargo",inv);report.put("left_assembled_for_restart",true);overview();finish();}
  }
  if(System.nanoTime()-started>700_000_000_000L)throw new IllegalStateException("Wall timeout stage="+stage);
 }catch(Throwable e){e.printStackTrace();error=e.toString();report.put("failed_stage",stage);finish();}}
 void clientTick(ClientTickEvent.Post event){var mc=Minecraft.getInstance();clientTicks++;if(mc.player!=null&&mc.getOverlay()==null&&mc.screen!=null)mc.setScreen(null);
  if(mc.player!=null&&mc.level!=null&&mc.getOverlay()==null&&++readyTicks>40)clientReady=true;clientFps=mc.getFps();
  if(mc.player!=null&&mc.level!=null){if(player!=null&&player.getAbilities().flying){mc.player.setYRot(180);mc.player.setXRot(35);}if(movingId!=null)for(var e:mc.level.entitiesForRendering())if(e instanceof ControlledContraptionEntity c&&c.getUUID().equals(movingId)&&c.collidingEntities.containsKey(mc.player))clientContacts++;}
  if(capture!=null&&mc.player!=null&&mc.getOverlay()==null){String name=capture;capture=null;try{if(Files.getFileStore(out).getUsableSpace()>512L*1024*1024)try(var img=Screenshot.takeScreenshot(mc.getMainRenderTarget())){img.writeToFile(phase().resolve(name+".png"));}}catch(Exception e){e.printStackTrace();}}
  if(finished&&++delay==40){try{Files.writeString(phase().resolve("result.json"),gson.toJson(report));}catch(Exception e){e.printStackTrace();}finally{mc.stop();}}
 }
}
