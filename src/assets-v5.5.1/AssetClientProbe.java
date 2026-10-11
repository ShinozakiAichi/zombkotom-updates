package assetqa;
import net.neoforged.fml.common.Mod;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.common.NeoForge;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import java.nio.file.*;
import java.util.*;
import software.bernie.geckolib.cache.GeckoLibCache;

/** Private automation fixture. Only runs in the isolated asset QA game directory. */
@Mod(value="asset_qa",dist=Dist.CLIENT)
public class AssetClientProbe {
 int ticks,screenTicks,pass,fail;boolean shown,shot;
 final List<String> results=new ArrayList<>();
 static final String[] ITEMS={"create_power_loader:andesite_chunk_loader","create_power_loader:brass_chunk_loader","ntgl:hammer_stone","ntgl:hammer_diamond","superbwarfare:qbz_95","superbwarfare:vector"};
 public AssetClientProbe(){NeoForge.EVENT_BUS.addListener(this::tick);}
 void check(boolean ok,String label){String s="ASSET_QA_"+(ok?"PASS ":"FAIL ")+label;System.out.println(s);results.add(s);if(ok)pass++;else fail++;}
 void tick(net.neoforged.neoforge.client.event.ClientTickEvent.Post event){
  var mc=Minecraft.getInstance();if(!Files.exists(mc.gameDirectory.toPath().resolve("asset-qa.enabled")))return;
  if(mc.level==null || mc.player==null || mc.getOverlay()!=null)return;
  if(!shown && ++ticks>=80){shown=true;try{try(var img=net.minecraft.client.Screenshot.takeScreenshot(mc.getMainRenderTarget())){img.writeToFile(mc.gameDirectory.toPath().resolve("asset-world.png"));}inspect(mc);phantom(mc);mc.setScreen(new ContactSheet());}catch(Throwable t){check(false,t.toString());t.printStackTrace();finish(mc);}return;}
  if(!shown)return;
  if(++screenTicks==40 && !shot){shot=true;try(var img=net.minecraft.client.Screenshot.takeScreenshot(mc.getMainRenderTarget())){img.writeToFile(mc.gameDirectory.toPath().resolve("asset-contact-sheet.png"));}catch(Throwable t){check(false,"screenshot "+t);}finish(mc);}
  if(screenTicks>=80)mc.stop();
 }
 void phantom(Minecraft mc)throws Exception {
  var manager=Class.forName("com.railwayteam.railways.content.custom_tracks.phantom.PhantomSpriteManager");
  var field=manager.getDeclaredField("map");field.setAccessible(true);
  var refs=(Map<ResourceLocation,java.lang.ref.WeakReference<net.minecraft.client.renderer.texture.SpriteContents>>)field.get(null);
  var atlas=mc.getModelManager().getAtlas(net.minecraft.world.inventory.InventoryMenu.BLOCK_ATLAS);
  var name=refs.keySet().stream().findFirst().orElseThrow();var sprite=atlas.getSprite(name);
  var oldMain=mc.player.getMainHandItem();var oldOff=mc.player.getOffhandItem();
  try{
   mc.player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND,net.minecraft.world.item.ItemStack.EMPTY);
   mc.player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.OFFHAND,net.minecraft.world.item.ItemStack.EMPTY);
   manager.getMethod("tick",Minecraft.class).invoke(null,mc);
   manager.getField("hasChanged").setBoolean(null,true);
   int bind=com.mojang.blaze3d.platform.GlStateManager._getInteger(32873);
   manager.getMethod("renderTick").invoke(null);
   check(com.mojang.blaze3d.platform.GlStateManager._getInteger(32873)==bind,"phantom upload preserves previous texture binding");
   int hidden=pixels(atlas.getId(),sprite);
   check(!manager.getField("hasChanged").getBoolean(null),"phantom pending update consumed after atlas upload");
   mc.player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND,BuiltInRegistries.ITEM.get(ResourceLocation.parse("railways:track_phantom")).getDefaultInstance());
   manager.getMethod("tick",Minecraft.class).invoke(null,mc);manager.getMethod("renderTick").invoke(null);
   int visible=pixels(atlas.getId(),sprite);
   check(hidden!=visible,"phantom track held in hand changes actual GPU sprite pixels");
   mc.player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND,net.minecraft.world.item.ItemStack.EMPTY);
   manager.getMethod("tick",Minecraft.class).invoke(null,mc);manager.getMethod("renderTick").invoke(null);
   check(pixels(atlas.getId(),sprite)==hidden,"removing phantom track restores actual hidden GPU pixels");
  }finally{mc.player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND,oldMain);mc.player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.OFFHAND,oldOff);manager.getMethod("tick",Minecraft.class).invoke(null,mc);manager.getMethod("renderTick").invoke(null);}
 }
 int pixels(int texture,net.minecraft.client.renderer.texture.TextureAtlasSprite sprite){
  int w=sprite.contents().width(),h=sprite.contents().height();var bytes=org.lwjgl.BufferUtils.createByteBuffer(w*h*4);
  org.lwjgl.opengl.GL45.glGetTextureSubImage(texture,0,sprite.getX(),sprite.getY(),0,w,h,1,6408,5121,bytes);
  int hash=1;for(int i=0;i<bytes.capacity();i++)hash=31*hash+bytes.get(i);return hash;
 }
 void inspect(Minecraft mc){
  for(var pair:new String[][]{{"superbwarfare:animations/qbz_95.animation.json","animation.qbz_95.edit"},{"superbwarfare:animations/vector.animation.json","animation.vector.edit"},{"undead_revamp2:animations/clogger.animation.json","ded"}}){
   var baked=GeckoLibCache.getBakedAnimations().get(ResourceLocation.parse(pair[0]));check(baked!=null && baked.getAnimation(pair[1])!=null,"native GeckoLib baked clip "+pair[1]);
  }
  for(String id:ITEMS){
   var item=BuiltInRegistries.ITEM.get(ResourceLocation.parse(id));var stack=item.getDefaultInstance();check(!stack.isEmpty(),"item registry "+id);
   var model=mc.getItemRenderer().getModel(stack,mc.level,mc.player,0);check(model!=mc.getModelManager().getMissingModel(),"item model "+id);
   if(id.startsWith("ntgl:"))check(!model.isCustomRenderer(),"attachment head has its own baked model "+id);
   if(id.startsWith("create_power_loader:") || id.startsWith("ntgl:")){
    var quads=new ArrayList<net.minecraft.client.renderer.block.model.BakedQuad>();quads.addAll(model.getQuads(null,null,RandomSource.create(0)));
    for(var d:Direction.values())quads.addAll(model.getQuads(null,d,RandomSource.create(0)));
    check(!quads.isEmpty() && quads.stream().noneMatch(q->q.getSprite().contents().name().getPath().equals("missingno")),"all baked item face textures "+id);
   }
  }
  for(String action:new String[]{"break","step","place","hit","fall"}){
   var sound=mc.getSoundManager().getSoundEvent(ResourceLocation.parse("dndecor:large_chain_"+action));check(sound!=null && sound.getWeight()>0,"resolved chain sound "+action);
  }
 }
 void finish(Minecraft mc){
  try{Files.writeString(mc.gameDirectory.toPath().resolve("asset-qa-result.txt"),String.join("\n",results)+"\nASSET_QA_RESULT pass="+pass+" fail="+fail+" real_network_clients=0\n");}catch(Exception e){e.printStackTrace();}
  System.out.println("ASSET_QA_RESULT pass="+pass+" fail="+fail+" real_network_clients=0");
 }
 class ContactSheet extends Screen {
  ContactSheet(){super(Component.literal("LAST BASTION - asset QA"));}
  @Override public boolean isPauseScreen(){return false;}
  @Override public void render(GuiGraphics g,int mouseX,int mouseY,float delta){
   g.fill(0,0,width,height,0xFF18212A);g.drawCenteredString(font,"LAST BASTION / RESOURCE CHECK",width/2,12,0xFFFFFF);
   int cell=Math.min(130,(width-24)/3),left=(width-cell*3)/2;
   for(int i=0;i<ITEMS.length;i++){
    int x=left+(i%3)*cell,y=38+(i/3)*100;g.fill(x+3,y,x+cell-3,y+90,0xFF293642);
    var stack=BuiltInRegistries.ITEM.get(ResourceLocation.parse(ITEMS[i])).getDefaultInstance();
    g.pose().pushPose();g.pose().translate(x+cell/2-24,y+8,0);g.pose().scale(3,3,1);
    try{g.renderItem(stack,0,0);}catch(Throwable t){if(screenTicks<2)check(false,"item render "+ITEMS[i]+": "+t.getClass().getSimpleName());}finally{g.pose().popPose();}
    g.drawCenteredString(font,ITEMS[i].substring(ITEMS[i].indexOf(':')+1),x+cell/2,y+66,0xFFFFFF);
   }
   g.drawCenteredString(font,"PASS "+pass+" / FAIL "+fail,width/2,height-20,fail==0?0x66FFAA:0xFF9966);
  }
 }
}
