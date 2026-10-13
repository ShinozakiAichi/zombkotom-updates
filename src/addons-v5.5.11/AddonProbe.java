package addonqa;
import com.google.gson.GsonBuilder;
import com.simibubi.create.content.contraptions.bearing.MechanicalBearingBlockEntity;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity;
import umpaz.brewinandchewin.common.block.entity.KegBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import java.nio.file.*;
import java.util.*;

/** Isolated prepared fixture; no main profile, survival, or multiplayer claims. */
@Mod(value="addons_qa",dist=Dist.CLIENT)
public class AddonProbe {
 final Path root=Minecraft.getInstance().gameDirectory.toPath();
 final BlockPos base=new BlockPos(544,66,512),barrel=base.offset(0,1,1),keg=new BlockPos(549,65,512),lamp=keg.above(2).east(2);
 ServerLevel level;ServerPlayer player;int ticks,stage,total,changes,delay,clientTicks;float previous;boolean resume,continuation;volatile boolean finished;volatile int musicalTicks;
 final Map<String,Object> report=new LinkedHashMap<>();
 public AddonProbe()throws Exception{
  if(!root.toAbsolutePath().getParent().getFileName().toString().equals("ZombKotom-Quest-QA")||!Files.exists(root.resolve("addons-qa.enabled")))return;
  String mode=Files.readString(root.resolve("addons-qa.enabled")).trim();resume=mode.equals("resume");continuation=mode.equals("continue");NeoForge.EVENT_BUS.addListener(this::serverTick);NeoForge.EVENT_BUS.addListener(this::clientTick);
 }
 static ResourceLocation id(String s){return ResourceLocation.parse(s);}
 static BlockState block(String s){if(!BuiltInRegistries.BLOCK.containsKey(id(s)))throw new IllegalStateException("Missing block "+s);return BuiltInRegistries.BLOCK.get(id(s)).defaultBlockState();}
 void require(boolean ok,String why){if(!ok)throw new IllegalStateException(why);}
 MechanicalBearingBlockEntity bearing(){return (MechanicalBearingBlockEntity)level.getBlockEntity(base);}
 int cargo(){var inv=bearing().getMovedContraption().getContraption().getStorage().getAllItems();int count=0;for(int i=0;i<inv.getSlots();i++)if(inv.getStackInSlot(i).is(Items.DIAMOND))count+=inv.getStackInSlot(i).getCount();return count;}
 KegBlockEntity keg(){return (KegBlockEntity)level.getBlockEntity(keg);}
 boolean beer(){var f=keg().getFluidTank().getAbstractedFluid();return BuiltInRegistries.FLUID.getKey(f.fluid()).toString().equals("brewinandchewin:beer")&&f.amount()==1000;}
 void next(int n){stage=n;ticks=0;}
 void music(){var lute=new ItemStack(BuiltInRegistries.ITEM.get(id("immersive_melodies:lute")));player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,lute);var instrument=(immersive_melodies.item.InstrumentItem)lute.getItem();var melody=id("immersive_melodies:melody/wet_hands.midi");require(immersive_melodies.resources.ServerMelodyManager.getDatapackMelodies().containsKey(melody),"Bundled melody absent");instrument.play(lute,melody,level,player);require(instrument.isPlaying(lute),"Instrument failed to start");report.put("instrument_play_state",true);}
 void serverTick(ServerTickEvent.Post e){
  if(finished)return;
  try{
   var server=e.getServer();if(server.getPlayerList().getPlayerCount()!=1)return;level=server.overworld();player=server.getPlayerList().getPlayers().getFirst();ticks++;total++;
   if(stage==0){server.tickRateManager().setTickRate(100);report.put("test_target_tick_rate",100);player.setGameMode(net.minecraft.world.level.GameType.CREATIVE);player.getAbilities().flying=true;player.onUpdateAbilities();if(ticks==1)player.connection.teleport(549.5,71,504.5,20,25);if(ticks<100)return;
    for(String recipe:List.of("brewinandchewin:keg","brewinandchewin:fermenting/beer_from_water","immersive_melodies:lute"))require(server.getRecipeManager().byKey(id(recipe)).isPresent(),"Missing recipe "+recipe);
    if(resume||continuation){if(resume){require(beer(),"Fermented fluid lost on restart");report.put("beer_persisted",true);}else{require(keg().saveWithFullMetadata(level.registryAccess()).getInt("FermentTime")>0,"Fermentation progress lost");report.put("continued_ferment_time",keg().saveWithFullMetadata(level.registryAccess()).getInt("FermentTime"));}require(bearing()!=null&&bearing().isRunning()&&cargo()==23,"Mounted cargo not saved");report.put("mounted_cargo_persisted",true);music();next(2);return;}
    for(int x=541;x<=553;x++)for(int z=509;z<=516;z++){level.setBlock(new BlockPos(x,64,z),Blocks.SMOOTH_STONE.defaultBlockState(),3);for(int y=65;y<=70;y++)level.setBlock(new BlockPos(x,y,z),Blocks.AIR.defaultBlockState(),3);}
    level.setBlock(base,block("create:mechanical_bearing").setValue(BlockStateProperties.FACING,Direction.UP),3);
    level.setBlock(base.above(),Blocks.SLIME_BLOCK.defaultBlockState(),3);level.setBlock(barrel,block("sophisticatedstorage:barrel"),3);
    level.setBlock(base.below(),block("create:creative_motor").setValue(BlockStateProperties.FACING,Direction.UP),3);
    var inv=level.getCapability(Capabilities.ItemHandler.BLOCK,barrel,null);require(inv!=null&&inv.insertItem(0,new ItemStack(Items.DIAMOND,23),false).isEmpty(),"Barrel input rejected");
    level.setBlock(keg,block("brewinandchewin:keg"),3);
    for(int i=0;i<3;i++)keg().getInventory().setStackInSlot(i,new ItemStack(List.of(Items.WHEAT,Items.WHEAT_SEEDS,Items.BROWN_MUSHROOM).get(i)));
    var fluid=level.getCapability(Capabilities.FluidHandler.BLOCK,keg,Direction.UP);require(fluid!=null&&fluid.fill(new FluidStack(Fluids.WATER,1000),IFluidHandler.FluidAction.EXECUTE)==1000,"Water input rejected");
    var state=block("amendments:wall_lantern");var direction=state.getValue(BlockStateProperties.HORIZONTAL_FACING);level.setBlock(lamp.relative(direction.getOpposite()),Blocks.STONE_BRICKS.defaultBlockState(),3);level.setBlock(lamp,state,3);
    level.setBlock(keg.west(2),Blocks.WATER_CAULDRON.defaultBlockState().setValue(BlockStateProperties.LEVEL_CAULDRON,3),3);
    music();next(1);return;
   }
   if(stage==1&&ticks>=60){((CreativeMotorBlockEntity)level.getBlockEntity(base.below())).generatedSpeed.setValue(8);if(!bearing().isRunning())bearing().assemble();require(bearing().getMovedContraption()!=null&&cargo()==23,"Mounting failed");report.put("initial_mounted_cargo",cargo());report.put("mounted_storage_types",bearing().getMovedContraption().getContraption().getStorage().getClass().getName());next(2);return;}
   if(stage==2){require(bearing().getMovedContraption()!=null&&bearing().isRunning(),"Contraption stopped");float a=bearing().getMovedContraption().getAngle(1);require(Float.isFinite(a),"Invalid angle");if(a!=previous)changes++;previous=a;
    if(ticks%400==0)System.out.println("ADDONS_SAMPLE tick="+ticks+" cargo="+cargo()+" beer="+beer()+" keg="+keg().saveWithFullMetadata(level.registryAccess()));
    if(!resume&&beer()&&!report.containsKey("beer_at_tick"))report.put("beer_at_tick",total);
    if(ticks>11000)throw new IllegalStateException("Fermentation timeout: "+keg().saveWithFullMetadata(level.registryAccess()));
    if(ticks<(resume?240:1200)||!beer())return;
    require(cargo()==23&&changes>200,"Moving cargo mismatch");require(musicalTicks>10,"Client melody notes not observed");require(level.getBlockState(lamp).is(BuiltInRegistries.BLOCK.get(id("amendments:wall_lantern"))),"Wall lantern did not survive");report.put("client_melody_note_ticks",musicalTicks);report.put("rotation_angle_changes",changes);report.put("fermented_beer_mb",1000);report.put("wall_lantern_stable",true);
    if(resume){bearing().disassemble();next(3);return;}report.put("left_assembled_for_restart",true);report.put("passed",true);finished=true;
   }
   if(stage==3&&ticks>=40){int count=0,barrels=0;for(var p:BlockPos.betweenClosed(base.offset(-3,1,-3),base.offset(3,1,3)))if(level.getBlockState(p).is(BuiltInRegistries.BLOCK.get(id("sophisticatedstorage:barrel")))){barrels++;var inv=level.getCapability(Capabilities.ItemHandler.BLOCK,p,null);require(inv!=null,"Restored inventory missing");for(int i=0;i<inv.getSlots();i++)if(inv.getStackInSlot(i).is(Items.DIAMOND))count+=inv.getStackInSlot(i).getCount();}require(barrels==1&&count==23,"Disassembly cargo mismatch");report.put("restored_barrels",barrels);report.put("restored_diamonds",count);report.put("passed",true);finished=true;}
   if(finished)server.tickRateManager().setTickRate(20);
  }catch(Throwable t){e.getServer().tickRateManager().setTickRate(20);t.printStackTrace();report.put("passed",false);report.put("error",t.toString());finished=true;}
 }
 void clientTick(ClientTickEvent.Post e){var mc=Minecraft.getInstance();if(mc.player!=null){mc.player.setYRot(20);mc.player.setXRot(25);if(mc.getOverlay()==null&&mc.screen!=null)mc.setScreen(null);var music=immersive_melodies.client.MelodyProgressManager.INSTANCE.getProgress(mc.player);if(music.getCurrentlyPlaying().equals(id("immersive_melodies:melody/wet_hands.midi"))&&music.isPlaying())musicalTicks++;}if(++clientTicks>16000&&!finished){report.put("passed",false);report.put("error","Client timeout");finished=true;}if(!finished||++delay!=30)return;
  try{Path out=root.resolve("addons-qa-"+(resume?"resume":"first"));Files.createDirectories(out);report.put("scope","One native local client, Iris + Complementary. Prepared fixture, creative motor, raw keg inputs. Native fermentation and moving cargo tested; no real co-op, listening test, survival resource cost, upgrades, or network transport coverage.");Files.writeString(out.resolve("result.json"),new GsonBuilder().setPrettyPrinting().create().toJson(report));if(Files.getFileStore(out).getUsableSpace()>512L*1024*1024)try(var img=Screenshot.takeScreenshot(mc.getMainRenderTarget())){img.writeToFile(out.resolve("result.png"));}System.out.println("ADDONS_RESULT "+report);}catch(Throwable t){t.printStackTrace();}finally{mc.stop();}
 }
}
