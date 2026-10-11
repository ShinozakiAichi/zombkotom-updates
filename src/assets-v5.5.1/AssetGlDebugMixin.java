package assetqa.mixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.lwjgl.system.MemoryUtil;

/** Observer only, private QA build; retains the game's original OpenGL error handler. */
@Mixin(targets="com.mojang.blaze3d.platform.GlDebug",remap=false)
public abstract class AssetGlDebugMixin {
 @Unique private static int lastbastion$logged;
 @Inject(method="printDebugLog",at=@At("HEAD"),remap=false)
 private static void lastbastion$trace(int source,int type,int id,int severity,int length,long message,long user,CallbackInfo ci){
  if(lastbastion$logged>=3)return;
  if(MemoryUtil.memUTF8(message,length).contains("Invalid texture format")){
   lastbastion$logged++;new Throwable("ASSET_QA_GL_ORIGIN "+lastbastion$logged).printStackTrace();
  }
 }
}
