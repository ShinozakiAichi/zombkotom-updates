package lastbastion.mixin;

import java.util.Map;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value=TextureAtlas.class,remap=false)
public interface TextureAtlasContentsAccessor {
 @Accessor("texturesByName") Map<ResourceLocation,TextureAtlasSprite> lastbastion$getUploadedSprites();
}
