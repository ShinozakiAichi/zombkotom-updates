package lastbastion.mixin;

import java.lang.ref.WeakReference;
import java.util.Map;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/** Railways registers sprites while loading, before the block atlas is uploaded. */
@Mixin(targets="com.railwayteam.railways.content.custom_tracks.phantom.PhantomSpriteManager",remap=false)
public abstract class PhantomSpriteUploadMixin {
 @Shadow public static boolean hasChanged;
 @Shadow @Final private static Map<ResourceLocation,WeakReference<SpriteContents>> map;

 @WrapMethod(method="renderTick",remap=false)
 private static void lastbastion$uploadToReadyAtlas(Operation<Void> original) {
  if(!hasChanged || map.isEmpty()){original.call();return;}
  var atlas=Minecraft.getInstance().getModelManager().getAtlas(InventoryMenu.BLOCK_ATLAS);
  var uploaded=((TextureAtlasContentsAccessor)atlas).lastbastion$getUploadedSprites();
  for(var entry:map.entrySet()){
   var contents=entry.getValue().get();
   if(contents==null)continue;
   var sprite=uploaded.get(entry.getKey());
   // Keep the pending flag until these exact sprites belong to the uploaded atlas.
   if(sprite==null || sprite.contents()!=contents)return;
  }
  int previous=GlStateManager._getInteger(32873); // GL_TEXTURE_BINDING_2D
  GlStateManager._bindTexture(atlas.getId());
  try{original.call();}finally{GlStateManager._bindTexture(previous);}
 }
}
