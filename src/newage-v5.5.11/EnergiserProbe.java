package energiserqa;
import com.google.gson.GsonBuilder;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import org.antarcticgardens.cna.content.energising.EnergiserBlockEntity;
import org.antarcticgardens.cna.content.electricity.connector.AbstractElectricalConnector;
import org.antarcticgardens.cna.content.electricity.wire.WireType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import java.nio.file.*;
import java.util.*;

/** Uses the previously saved ordinary water generator; never inserts energy or manufactures output. */
@Mod(value="energiser_qa",dist=Dist.CLIENT)
public class EnergiserProbe {
 final Path root=Minecraft.getInstance().gameDirectory.toPath();
 final BlockPos source=new BlockPos(520,71,512),oldSink=new BlockPos(528,71,512),machine=new BlockPos(527,71,513),sink=machine.above(),depot=machine.below(2),chest=depot.south(3),water=new BlockPos(524,73,511);
 ServerLevel level;ServerPlayer player;int ticks,stage,clientTicks,delay;boolean resume;volatile boolean finished;
 final Map<String,Object> report=new LinkedHashMap<>();final List<Map<String,Object>> samples=new ArrayList<>();
 public EnergiserProbe()throws Exception{
  if(!root.toAbsolutePath().getParent().getFileName().toString().equals("ZombKotom-Quest-QA")||!Files.exists(root.resolve("energiser-qa.enabled")))return;
  resume=Files.readString(root.resolve("energiser-qa.enabled")).trim().equals("resume");NeoForge.EVENT_BUS.addListener(this::serverTick);NeoForge.EVENT_BUS.addListener(this::clientTick);
 }
 static ResourceLocation id(String s){return ResourceLocation.parse(s);}
 static BlockState block(String s){return BuiltInRegistries.BLOCK.get(id(s)).defaultBlockState();}
 void require(boolean ok,String why){if(!ok)throw new IllegalStateException(why);}
 EnergiserBlockEntity be(){return (EnergiserBlockEntity)level.getBlockEntity(machine);}
 AbstractElectricalConnector connector(BlockPos p){return (AbstractElectricalConnector)level.getBlockEntity(p);}
 void input(){var inv=level.getCapability(Capabilities.ItemHandler.BLOCK,depot,null);require(inv!=null&&inv.insertItem(0,new ItemStack(Items.IRON_INGOT),false).isEmpty(),"Input rejected");}
 int output(){var inv=level.getCapability(Capabilities.ItemHandler.BLOCK,depot,null);var store=level.getCapability(Capabilities.ItemHandler.BLOCK,chest,null);var item=BuiltInRegistries.ITEM.get(id("create_new_age:overcharged_iron"));
  for(int i=0;i<inv.getSlots();i++)if(inv.getStackInSlot(i).is(item)){var extracted=inv.extractItem(i,64,false);for(int j=0;j<store.getSlots()&&!extracted.isEmpty();j++)extracted=store.insertItem(j,extracted,false);require(extracted.isEmpty(),"Chest full");}
  int n=0;for(int i=0;i<store.getSlots();i++)if(store.getStackInSlot(i).is(item))n+=store.getStackInSlot(i).getCount();return n;
 }
 void sample(){var r=new LinkedHashMap<String,Object>();r.put("stage",stage);r.put("tick",ticks);r.put("output",output());r.put("speed",be().getSpeed());r.put("overstressed",be().isOverStressed());r.put("energy",be().getEnergyStorage().getStoredEnergy());r.put("last_charged",be().lastCharged);r.put("wire",connector(source).isConnected(sink));samples.add(r);System.out.println("ENERGISER_SAMPLE "+r);}
 void next(int s){stage=s;ticks=0;}
 void serverTick(ServerTickEvent.Post event){
  if(finished)return;
  try{
   var server=event.getServer();if(server.getPlayerList().getPlayerCount()!=1)return;level=server.overworld();player=server.getPlayerList().getPlayers().getFirst();ticks++;
   if(stage==0){
    player.setGameMode(net.minecraft.world.level.GameType.CREATIVE);player.getAbilities().flying=true;player.onUpdateAbilities();if(ticks==1)player.teleportTo(533.5,76,505.5);if(ticks<100)return;
    require(((KineticBlockEntity)level.getBlockEntity(new BlockPos(516,70,512))).getSpeed()!=0,"Saved generator not working");
    if(resume){require(output()==3&&connector(source).isConnected(sink),"Saved output or wire lost");report.put("saved_output",3);report.put("wire_survived_restart",true);input();next(10);return;}
    for(var old:new ArrayList<>(connector(source).getConnectedConnectors().keySet()))connector(source).disconnect(old);
    for(BlockPos old:List.of(new BlockPos(526,71,512),new BlockPos(526,70,512),new BlockPos(526,68,512),new BlockPos(526,68,515)))level.setBlock(old,Blocks.AIR.defaultBlockState(),3);
    for(int y=67;y<=74;y++){level.setBlock(new BlockPos(523,y,511),Blocks.GLASS.defaultBlockState(),3);level.setBlock(new BlockPos(525,y,511),Blocks.GLASS.defaultBlockState(),3);level.setBlock(new BlockPos(524,y,510),Blocks.GLASS.defaultBlockState(),3);if(y!=70)level.setBlock(new BlockPos(524,y,512),Blocks.GLASS.defaultBlockState(),3);}
    level.setBlock(new BlockPos(524,70,512),block("create:water_wheel").setValue(BlockStateProperties.FACING,Direction.EAST),3);
    level.setBlock(new BlockPos(525,70,512),block("create:large_cogwheel").setValue(BlockStateProperties.AXIS,Direction.Axis.X),3);
    level.setBlock(new BlockPos(525,71,513),block("create:cogwheel").setValue(BlockStateProperties.AXIS,Direction.Axis.X),3);
    level.setBlock(new BlockPos(526,71,513),block("create:shaft").setValue(BlockStateProperties.AXIS,Direction.Axis.X),3);
    level.setBlock(machine,block("create_new_age:basic_energiser").setValue(BlockStateProperties.HORIZONTAL_FACING,Direction.EAST),3);
    level.setBlock(sink,block("create_new_age:electrical_connector").setValue(BlockStateProperties.FACING,Direction.UP),3);
    level.setBlock(depot,block("create:depot"),3);level.setBlock(chest,Blocks.CHEST.defaultBlockState(),3);level.setBlock(water,Blocks.WATER.defaultBlockState(),3);
    require(server.getRecipeManager().byKey(id("create_new_age:energising/overcharged_iron")).isPresent(),"Installed recipe missing");next(1);return;
   }
   if(ticks%50==0)sample();
   if(stage==1&&ticks>=100){require(be().getSpeed()!=0&&!be().isOverStressed(),"No mechanical drive");require(be().getEnergyStorage().getStoredEnergy()==0,"Unexpected initial FE");input();next(2);}
   if(stage==2&&ticks>=200){require(output()==0&&be().getEnergyStorage().getStoredEnergy()==0,"Output without electric supply");report.put("rotation_without_electricity_no_output",true);connector(source).connect(connector(sink),WireType.COPPER);next(3);}
   if(stage==3&&output()==1){report.put("first_ingot_ticks",ticks);input();next(4);}
   if(stage==4&&output()==2){report.put("second_ingot_ticks",ticks);report.put("repeat_processing",true);level.setBlock(water,Blocks.AIR.defaultBlockState(),3);next(5);}
   if(stage==5&&ticks>=200){require(be().getSpeed()==0,"Drive did not stop");require(be().getEnergyStorage().getStoredEnergy()>0,"No FE to isolate mechanical stop");input();next(6);}
   if(stage==6&&ticks>=200){require(output()==2&&be().getSpeed()==0&&be().getEnergyStorage().getStoredEnergy()>0,"Processed without rotation");report.put("electricity_without_rotation_no_output",true);level.setBlock(water,Blocks.WATER.defaultBlockState(),3);next(7);}
   if(stage==7&&output()==3){report.put("rotation_restored_resumes",true);report.put("output",3);report.put("passed",true);finished=true;}
   if(stage==10&&output()==4){report.put("processing_after_restart",true);report.put("output",4);report.put("passed",true);finished=true;}
   if(ticks>2400)throw new IllegalStateException("Stage timeout "+stage);
  }catch(Throwable t){t.printStackTrace();report.put("passed",false);report.put("error",t.toString());finished=true;}
 }
 void clientTick(ClientTickEvent.Post event){
  var mc=Minecraft.getInstance();if(mc.player!=null){mc.player.setYRot(45);mc.player.setXRot(29);if(mc.getOverlay()==null&&mc.screen!=null)mc.setScreen(null);}
  if(++clientTicks>12000&&!finished){report.put("passed",false);report.put("error","Client timeout");finished=true;}
  if(!finished||++delay!=30)return;
  try{Path out=root.resolve("energiser-qa-"+(resume?"resume":"first"));Files.createDirectories(out);report.put("samples",samples);report.put("scope","One local client; prepared blocks and raw ingots; native water generation and shaft drive; wire graph API and output extraction by fixture. No injected FE, assigned speed or output grants. Not survival crafting, belts or multiplayer.");Files.writeString(out.resolve("result.json"),new GsonBuilder().setPrettyPrinting().create().toJson(report));if(Files.getFileStore(out).getUsableSpace()>512L*1024*1024)try(var img=Screenshot.takeScreenshot(mc.getMainRenderTarget())){img.writeToFile(out.resolve("result.png"));}System.out.println("ENERGISER_RESULT passed="+report.get("passed"));}
  catch(Throwable t){t.printStackTrace();}finally{mc.stop();}
 }
}
