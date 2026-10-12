package lastbastion.mixin;
import lastbastion.hud.BastionState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.*;
import net.minecraft.world.scores.DisplaySlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value=Gui.class,remap=false)
public abstract class SingleplayerTabMixin {
    @Inject(method="renderTabList",at=@At("HEAD"),cancellable=true,require=1)
    private void lastbastion$tab(GuiGraphics graphics,DeltaTracker delta,CallbackInfo ci){
        var mc=Minecraft.getInstance();
        if(!BastionState.active()||mc.level==null||mc.getConnection()==null)return;
        boolean visible=mc.options.keyPlayerList.isDown();var tab=mc.gui.getTabList();tab.setVisible(visible);
        if(visible){var board=mc.level.getScoreboard();tab.render(graphics,graphics.guiWidth(),board,board.getDisplayObjective(DisplaySlot.LIST));}
        ci.cancel();
    }
}
