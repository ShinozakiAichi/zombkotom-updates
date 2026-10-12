package lastbastion.mixin;
import lastbastion.hud.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(value=PlayerTabOverlay.class,remap=false)
public abstract class PlayerTabMixin {
    @Inject(method="getNameForDisplay",at=@At("RETURN"),cancellable=true,require=1)
    private void lastbastion$ping(PlayerInfo info,CallbackInfoReturnable<Component> ci){
        if(BastionState.active())ci.setReturnValue(ci.getReturnValue().copy().append(
            Component.literal("  "+BastionStatus.ping(info.getLatency())).withStyle(style->style.withBold(false).withItalic(false).withColor(BastionStatus.pingColor(info.getLatency())))));
    }
    @Inject(method="renderPingIcon",at=@At("HEAD"),cancellable=true,require=1)
    private void lastbastion$hideBars(GuiGraphics g,int width,int x,int y,PlayerInfo info,CallbackInfo ci){if(BastionState.active())ci.cancel();}
}
