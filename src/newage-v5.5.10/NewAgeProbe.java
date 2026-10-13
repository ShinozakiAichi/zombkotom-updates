package newageqa;
import com.google.gson.GsonBuilder;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.press.MechanicalPressBlockEntity;
import org.antarcticgardens.cna.content.electricity.generation.coil.GeneratorCoilBlockEntity;
import org.antarcticgardens.cna.content.electricity.connector.AbstractElectricalConnector;
import org.antarcticgardens.cna.content.electricity.wire.WireType;
import org.antarcticgardens.cna.content.motor.MotorBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import java.nio.file.*;
import java.util.*;

/** Private natural-generation test; prepared blocks/input, native ticking and native wire graph API. */
@Mod(value="newage_qa",dist=Dist.CLIENT)
public class NewAgeProbe {
 final BlockPos brush=new BlockPos(520,70,512),source=brush.above(),motor=new BlockPos(528,70,512),sink=motor.above(),press=motor.east(),depot=press.below(2),chest=depot.south(2);
 final Path root=Minecraft.getInstance().gameDirectory.toPath();
 final Map<String,Object> report=new LinkedHashMap<>();final List<Map<String,Object>> samples=new ArrayList<>();
 ServerLevel level;ServerPlayer player;int ticks,stage,clientTicks,delay;boolean resume;volatile boolean finished;
 public NewAgeProbe()throws Exception{
  if(!root.toAbsolutePath().getParent().getFileName().toString().equals("ZombKotom-Quest-QA")||!Files.exists(root.resolve("newage-qa.enabled")))return;
  resume=Files.readString(root.resolve("newage-qa.enabled")).trim().equals("resume");
  NeoForge.EVENT_BUS.addListener(this::serverTick);NeoForge.EVENT_BUS.addListener(this::clientTick);
 }
 static ResourceLocation id(String s){return ResourceLocation.parse(s);}
 static BlockState block(String s){return BuiltInRegistries.BLOCK.get(id(s)).defaultBlockState();}
 void require(boolean b,String message){if(!b)throw new IllegalStateException(message);}
 KineticBlockEntity kinetic(BlockPos p){return (KineticBlockEntity)level.getBlockEntity(p);}
 AbstractElectricalConnector connector(BlockPos p){return (AbstractElectricalConnector)level.getBlockEntity(p);}
 void face(BlockPos p,String name,Direction facing){level.setBlock(p,block(name).setValue(BlockStateProperties.FACING,facing),3);}
 void water(boolean on){for(int x=510;x<=515;x++)level.setBlock(new BlockPos(x,73,511),(on?Blocks.WATER:Blocks.AIR).defaultBlockState(),3);}
 void input(int n){var inv=level.getCapability(Capabilities.ItemHandler.BLOCK,depot,null);require(inv!=null&&inv.insertItem(0,new ItemStack(Items.IRON_INGOT,n),false).isEmpty(),"Input rejected");}
 int output(){var inv=level.getCapability(Capabilities.ItemHandler.BLOCK,depot,null);var store=level.getCapability(Capabilities.ItemHandler.BLOCK,chest,null);
  for(int i=0;i<inv.getSlots();i++)if(inv.getStackInSlot(i).is(BuiltInRegistries.ITEM.get(id("create:iron_sheet")))){var extracted=inv.extractItem(i,64,false);for(int j=0;j<store.getSlots()&&!extracted.isEmpty();j++)extracted=store.insertItem(j,extracted,false);require(extracted.isEmpty(),"Output chest full");}
  int n=0;for(int i=0;i<store.getSlots();i++)if(store.getStackInSlot(i).is(BuiltInRegistries.ITEM.get(id("create:iron_sheet"))))n+=store.getStackInSlot(i).getCount();return n;}

 void sample(){var row=new LinkedHashMap<String,Object>();row.put("stage",stage);row.put("tick",ticks);row.put("output",output());row.put("wheel_rpm",kinetic(new BlockPos(512,70,512)).getSpeed());row.put("coil_rpm",kinetic(new BlockPos(516,70,512)).getSpeed());row.put("coil_stress_per_rpm",kinetic(new BlockPos(516,70,512)).calculateStressApplied());row.put("generator_overloaded",kinetic(new BlockPos(516,70,512)).isOverStressed());row.put("motor_rpm",kinetic(motor).getSpeed());row.put("motor_capacity_su",kinetic(motor).calculateAddedStressCapacity()*Math.abs(kinetic(motor).getSpeed()));row.put("press_rpm",kinetic(press).getSpeed());row.put("press_overloaded",kinetic(press).isOverStressed());row.put("wire_connected",connector(source).isConnected(sink));samples.add(row);System.out.println("NEWAGE_SAMPLE "+row);}
 void next(int value){stage=value;ticks=0;}
 void serverTick(ServerTickEvent.Post event){
  if(finished)return;
  try{
   var server=event.getServer();if(server.getPlayerList().getPlayerCount()!=1)return;
   player=server.getPlayerList().getPlayers().getFirst();level=server.overworld();ticks++;
   if(stage==0){
    player.setGameMode(net.minecraft.world.level.GameType.CREATIVE);player.getAbilities().flying=true;player.onUpdateAbilities();
    if(ticks==1){player.teleportTo(530.5,77,502.5);player.setYRot(30);player.setXRot(28);}
    if(ticks<100)return;
    if(resume){require(output()==24,"Saved output count differs");require(connector(source).isConnected(sink),"Wire lost across restart");report.put("saved_output",24);report.put("wire_survived_restart",true);next(10);return;}
    for(int x=509;x<=532;x++)for(int z=508;z<=516;z++){level.setBlock(new BlockPos(x,65,z),Blocks.SMOOTH_STONE.defaultBlockState(),3);for(int y=66;y<=77;y++)level.setBlock(new BlockPos(x,y,z),Blocks.AIR.defaultBlockState(),3);}
    for(int x=510;x<=515;x++){
     for(int y=67;y<=74;y++){level.setBlock(new BlockPos(x,y,510),Blocks.GLASS.defaultBlockState(),3);if(y!=70)level.setBlock(new BlockPos(x,y,512),Blocks.GLASS.defaultBlockState(),3);}
     face(new BlockPos(x,70,512),"create:water_wheel",Direction.EAST);
    }
    for(int x:new int[]{509,516})for(int y=67;y<=74;y++)level.setBlock(new BlockPos(x,y,511),Blocks.GLASS.defaultBlockState(),3);
    for(int x=516;x<=519;x++){
     BlockPos p=new BlockPos(x,70,512);level.setBlock(p,block("create_new_age:generator_coil").setValue(BlockStateProperties.AXIS,Direction.Axis.X),3);
     for(BlockPos magnet:((GeneratorCoilBlockEntity)level.getBlockEntity(p)).magnetPositions)level.setBlock(magnet,block("create_new_age:magnetite_block"),3);
    }
    face(brush,"create_new_age:carbon_brushes",Direction.EAST);face(source,"create_new_age:electrical_connector",Direction.UP);
    face(motor,"create_new_age:basic_motor",Direction.EAST);face(sink,"create_new_age:electrical_connector",Direction.UP);
    level.setBlock(press,block("create:mechanical_press").setValue(BlockStateProperties.HORIZONTAL_FACING,Direction.EAST),3);level.setBlock(depot,block("create:depot"),3);
    level.setBlock(chest,Blocks.CHEST.defaultBlockState(),3);water(true);next(1);return;
   }
   if(ticks%100==0)sample();
   if(stage==1&&ticks==60){connector(source).connect(connector(sink),WireType.COPPER);}
   if(stage==1&&ticks>=300){require(kinetic(press).getSpeed()!=0&&!kinetic(press).isOverStressed(),"Remote press lacks usable power");require(!kinetic(new BlockPos(516,70,512)).isOverStressed(),"Water bank overloaded");input(16);next(2);}
   if(stage==2&&output()==16){report.put("first_batch_ticks",ticks);report.put("first_batch_sheets",16);connector(source).disconnect(connector(sink));next(3);}
   if(stage==3&&ticks>=40&&kinetic(motor).getSpeed()==0){report.put("wire_removed_stops_motor",true);input(4);next(4);}
   if(stage==4&&ticks>=200){require(output()==16&&kinetic(press).getSpeed()==0,"Processed while disconnected");report.put("no_processing_without_wire",true);connector(source).connect(connector(sink),WireType.COPPER);next(5);}
   if(stage==5&&output()==20){report.put("wire_restored_resumes",true);water(false);next(6);}
   if(stage==6&&ticks>=200&&kinetic(motor).getSpeed()==0){require(kinetic(new BlockPos(516,70,512)).getSpeed()==0,"Coils still rotate without water");report.put("water_removed_stops_motor",true);input(4);next(7);}
   if(stage==7&&ticks>=200){require(output()==20&&kinetic(press).getSpeed()==0,"Processed without water power");report.put("no_processing_without_water",true);water(true);next(8);}
   if(stage==8&&output()==24){report.put("water_restored_resumes",true);report.put("output",24);report.put("passed",true);finished=true;}
   if(stage==10&&ticks>=200){require(kinetic(press).getSpeed()!=0&&!kinetic(press).isOverStressed(),"Power did not resume after restart");input(4);next(11);}
   if(stage==11&&output()==28){report.put("processing_after_restart",true);report.put("output",28);report.put("passed",true);finished=true;}
   if(ticks>2400)throw new IllegalStateException("Stage timeout "+stage);
  }catch(Throwable t){t.printStackTrace();report.put("passed",false);report.put("error",t.toString());finished=true;}
 }
 void clientTick(ClientTickEvent.Post event){
  var mc=Minecraft.getInstance();if(mc.player!=null){mc.player.setYRot(45);mc.player.setXRot(28);}
  if(mc.player!=null&&mc.getOverlay()==null&&mc.screen!=null)mc.setScreen(null);
  if(++clientTicks>15000&&!finished){report.put("passed",false);report.put("error","Client timeout");finished=true;}
  if(!finished||++delay!=30)return;
  try{
   Path out=root.resolve("newage-qa-"+(resume?"resume":"first"));Files.createDirectories(out);report.put("samples",samples);report.put("scope","One real local client; prepared blocks, input ingots and native wire graph connection. No injected energy, assigned rotation, output grants or recipe execution. Not survival crafting or network multiplayer.");
   report.put("water_wheels",6);report.put("generator_coils",4);report.put("magnetite_blocks",48);report.put("creative_motors",0);report.put("injected_energy",0);report.put("assigned_speeds",0);
   Files.writeString(out.resolve("result.json"),new GsonBuilder().setPrettyPrinting().create().toJson(report));
   if(Files.getFileStore(out).getUsableSpace()>512L*1024*1024)try(var img=Screenshot.takeScreenshot(mc.getMainRenderTarget())){img.writeToFile(out.resolve("result.png"));}
   System.out.println("NEWAGE_RESULT passed="+report.get("passed"));
  }catch(Throwable t){t.printStackTrace();}finally{mc.stop();}
 }
}
