package mechanicsqa;

import com.google.gson.GsonBuilder;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.waterwheel.WaterWheelBlockEntity;
import com.simibubi.create.content.kinetics.mixer.MechanicalMixerBlockEntity;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.foundation.advancement.AdvancementBehaviour;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import java.nio.file.*;
import java.util.*;

/** Private native water-powered lesson; no assigned speed, recipe execution or advancement grants. */
@Mod(value="mechanics_qa",dist=Dist.CLIENT)
public class WaterMixerProbe {
 final BlockPos wheel=new BlockPos(192,67,192), water=new BlockPos(192,70,191);
 final BlockPos mixer=new BlockPos(196,67,194),basin=mixer.below(2);
 final List<BlockPos> drive=List.of(wheel,new BlockPos(193,67,192),new BlockPos(193,68,193),new BlockPos(194,68,193),new BlockPos(194,69,194),new BlockPos(195,69,194),new BlockPos(195,68,194),new BlockPos(195,67,194),mixer);
 int ticks,clientTicks,stage,produced,delay;boolean enabled;volatile boolean finished;
 ServerLevel level;ServerPlayer player;
 final Map<String,Object> report=new LinkedHashMap<>();final List<Map<String,Object>> samples=new ArrayList<>();
 static ResourceLocation id(String s){return ResourceLocation.parse(s);}
 static BlockState block(String s){return BuiltInRegistries.BLOCK.get(id(s)).defaultBlockState();}
 public WaterMixerProbe(){
  Path root=Minecraft.getInstance().gameDirectory.toPath().toAbsolutePath().normalize();
  enabled=root.getParent().getFileName().toString().equals("ZombKotom-Quest-QA")&&Files.exists(root.resolve("water-mixer-qa.enabled"));
  if(enabled){NeoForge.EVENT_BUS.addListener(this::serverTick);NeoForge.EVENT_BUS.addListener(this::clientTick);}
 }
 void gear(BlockPos pos,String name,Direction.Axis axis){level.setBlock(pos,block("create:"+name).setValue(BlockStateProperties.AXIS,axis),3);}
 void require(boolean condition,String message){if(!condition)throw new IllegalStateException(message);}
 void input(int count){var inv=((BasinBlockEntity)level.getBlockEntity(basin)).getInputInventory();require(inv.insertItem(0,new ItemStack(Items.ANDESITE,count),false).isEmpty(),"Andesite insertion failed");require(inv.insertItem(1,new ItemStack(Items.IRON_NUGGET,count),false).isEmpty(),"Nugget insertion failed");}
 void collect(){var inv=((BasinBlockEntity)level.getBlockEntity(basin)).getOutputInventory();for(int i=0;i<inv.getSlots();i++)if(inv.getStackInSlot(i).is(BuiltInRegistries.ITEM.get(id("create:andesite_alloy")))){ItemStack out=inv.extractItem(i,64,false);produced+=out.getCount();player.getInventory().add(out);}}
 void sample(){var row=new LinkedHashMap<String,Object>();row.put("stage",stage);row.put("ticks",ticks);row.put("output",produced);var speeds=new LinkedHashMap<String,Object>();for(BlockPos pos:drive){var be=level.getBlockEntity(pos);speeds.put(pos.toShortString(),be instanceof KineticBlockEntity k?Map.of("speed",k.getSpeed(),"overstressed",k.isOverStressed()):"missing");}row.put("drive",speeds);row.put("flow_score",((WaterWheelBlockEntity)level.getBlockEntity(wheel)).flowScore);samples.add(row);System.out.println("WATER_MIXER_SAMPLE "+row);}
 void serverTick(ServerTickEvent.Post event){
  if(finished)return;
  try{
   var server=event.getServer();if(server.getPlayerList().getPlayerCount()!=1)return;
   player=server.getPlayerList().getPlayers().getFirst();level=server.overworld();ticks++;
   if(stage==0){
    if(ticks<100)return;
    for(int x=189;x<=200;x++)for(int z=188;z<=198;z++){level.setBlock(new BlockPos(x,64,z),Blocks.SMOOTH_STONE.defaultBlockState(),3);for(int y=65;y<=73;y++)level.setBlock(new BlockPos(x,y,z),Blocks.AIR.defaultBlockState(),3);}
    level.setDayTime((level.getDayTime()/24000)*24000+6000);
    player.teleportTo(199.5,70,188.5);player.setYRot(35);player.setXRot(24);
    for(String name:List.of("create:water_wheel","create:mechanical_mixer")){var a=server.getAdvancements().get(id(name));require(a!=null,"Missing advancement "+name);var completed=new ArrayList<String>();player.getAdvancements().getOrStartProgress(a).getCompletedCriteria().forEach(completed::add);for(String c:completed)player.getAdvancements().revoke(a,c);}
    // Glass channel holds a naturally flowing waterfall beside a small wheel.
    for(int y=65;y<=71;y++){level.setBlock(new BlockPos(191,y,191),Blocks.GLASS.defaultBlockState(),3);level.setBlock(new BlockPos(193,y,191),Blocks.GLASS.defaultBlockState(),3);level.setBlock(new BlockPos(192,y,190),Blocks.GLASS.defaultBlockState(),3);if(y!=67)level.setBlock(new BlockPos(192,y,192),Blocks.GLASS.defaultBlockState(),3);}
    level.setBlock(wheel,block("create:water_wheel").setValue(BlockStateProperties.FACING,Direction.EAST),3);
    gear(drive.get(1),"large_cogwheel",Direction.Axis.X);gear(drive.get(2),"cogwheel",Direction.Axis.X);
    gear(drive.get(3),"large_cogwheel",Direction.Axis.X);gear(drive.get(4),"cogwheel",Direction.Axis.X);
    gear(drive.get(5),"gearbox",Direction.Axis.Z);gear(drive.get(6),"shaft",Direction.Axis.Y);gear(drive.get(7),"cogwheel",Direction.Axis.Y);
    level.setBlock(basin,block("create:basin"),3);level.setBlock(mixer,block("create:mechanical_mixer"),3);
    AdvancementBehaviour.setPlacedBy(level,wheel,player);AdvancementBehaviour.setPlacedBy(level,mixer,player);
    level.setBlock(water,Blocks.WATER.defaultBlockState(),3);
    stage=1;ticks=0;System.out.println("WATER_MIXER_SETUP");return;
   }
   if(ticks%100==0)sample();
   var machine=(MechanicalMixerBlockEntity)level.getBlockEntity(mixer);
   if(stage==1&&ticks>=200){
    require(Math.abs(machine.getSpeed())==32,"Expected ordinary geared drive at 32 rpm, got "+machine.getSpeed());
    require(!machine.isOverStressed(),"Single mixer overloads wheel");
    report.put("wheel_rpm",((WaterWheelBlockEntity)level.getBlockEntity(wheel)).getSpeed());report.put("mixer_rpm",machine.getSpeed());
    report.put("wheel_capacity_su",((KineticBlockEntity)level.getBlockEntity(wheel)).calculateAddedStressCapacity()*Math.abs(((KineticBlockEntity)level.getBlockEntity(wheel)).getSpeed()));
    report.put("mixer_stress_su",machine.calculateStressApplied()*Math.abs(machine.getSpeed()));
    input(8);stage=2;ticks=0;
   }
   if(stage==2){collect();if(produced==8){
    require(((BasinBlockEntity)level.getBlockEntity(basin)).getInputInventory().isEmpty(),"Recipe left unexpected input");
    require(server.getAdvancements().get(id("create:mechanical_mixer"))!=null,"Missing mixer advancement");
    report.put("mixer_advancement_earned",player.getAdvancements().getOrStartProgress(server.getAdvancements().get(id("create:mechanical_mixer"))).isDone());
    report.put("wheel_advancement_earned",player.getAdvancements().getOrStartProgress(server.getAdvancements().get(id("create:water_wheel"))).isDone());
    require(Boolean.TRUE.equals(report.get("mixer_advancement_earned")),"Mixer did not award advancement");
    require(Boolean.TRUE.equals(report.get("wheel_advancement_earned")),"Wheel did not award advancement");
    report.put("first_batch_ticks",ticks);level.setBlock(water,Blocks.AIR.defaultBlockState(),3);stage=3;ticks=0;
   }}
   if(stage==3&&ticks>=200){require(machine.getSpeed()==0,"Drive did not stop when water removed");report.put("water_removed_stops_drive",true);input(1);stage=4;ticks=0;}
   if(stage==4&&ticks>=100){collect();require(produced==8,"Recipe processed without power");report.put("no_processing_without_water",true);level.setBlock(water,Blocks.WATER.defaultBlockState(),3);stage=5;ticks=0;}
   if(stage==5){collect();if(produced==9){report.put("water_restored_resumes_processing",true);report.put("produced_alloy",produced);report.put("passed",true);finished=true;System.out.println("WATER_MIXER_RESULT pass=true output=9");}}
   if(ticks>1800)throw new IllegalStateException("Timed out stage="+stage+" produced="+produced);
  }catch(Throwable e){e.printStackTrace();report.put("passed",false);report.put("error",e.toString());finished=true;System.out.println("WATER_MIXER_FAIL "+e);}
 }
 void clientTick(ClientTickEvent.Post event){
  var mc=Minecraft.getInstance();if(++clientTicks>9000&&!finished){report.put("passed",false);report.put("error","Client fixture timeout");finished=true;}
  if(mc.player!=null&&mc.getOverlay()==null&&mc.screen!=null)mc.setScreen(null);
  if(!finished||++delay!=30)return;
  try{Path out=mc.gameDirectory.toPath().resolve("water-mixer-qa");Files.createDirectories(out);report.put("samples",samples);report.put("scope","Native water flow, gearing, basin and mixer; prepared blocks and inputs, not a survival crafting playthrough");report.put("creative_motors",0);report.put("assigned_speeds",0);report.put("manual_advancement_grants",0);report.put("manual_output_grants",0);Files.writeString(out.resolve("result.json"),new GsonBuilder().setPrettyPrinting().create().toJson(report));if(Files.getFileStore(out).getUsableSpace()>512L*1024*1024)try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(out.resolve("result.png"));}}
  catch(Throwable e){e.printStackTrace();}finally{mc.stop();}
 }
}
