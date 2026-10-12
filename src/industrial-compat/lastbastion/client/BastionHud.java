package lastbastion.client;

import com.mojang.blaze3d.platform.InputConstants;
import lastbastion.hud.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;

@EventBusSubscriber(modid="lastbastion_compat",value=Dist.CLIENT)
public final class BastionHud {
    public static final KeyMapping TOGGLE=new KeyMapping("key.lastbastion.hud_toggle",InputConstants.UNKNOWN.getValue(),"key.categories.lastbastion");
    public static final KeyMapping POSITION=new KeyMapping("key.lastbastion.hud_position",InputConstants.UNKNOWN.getValue(),"key.categories.lastbastion");
    public static final KeyMapping SCALE=new KeyMapping("key.lastbastion.hud_scale",InputConstants.UNKNOWN.getValue(),"key.categories.lastbastion");
    private static BastionPreferences preferences;
    private static BastionState lastHeader;
    public static BastionPreferences preferences(){if(preferences==null)preferences=new BastionPreferences();return preferences;}
    @EventBusSubscriber(modid="lastbastion_compat",value=Dist.CLIENT,bus=EventBusSubscriber.Bus.MOD)
    public static final class Keys {
        @SubscribeEvent public static void register(RegisterKeyMappingsEvent e){e.register(TOGGLE);e.register(POSITION);e.register(SCALE);}
    }
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut e){reset();}
    @SubscribeEvent public static void login(ClientPlayerNetworkEvent.LoggingIn e){reset();}
    private static void reset(){BastionState.clear();lastHeader=null;}
    @SubscribeEvent public static void tick(ClientTickEvent.Post e) {
        var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null)return;
        var prefs=preferences();boolean changed=false;
        while(TOGGLE.consumeClick()){prefs.visible=!prefs.visible;changed=true;}
        while(POSITION.consumeClick()){prefs.position=(prefs.position+1)%4;changed=true;}
        while(SCALE.consumeClick()){prefs.size=(prefs.size+1)%3;changed=true;}
        if(changed)prefs.save();
        if(!BastionState.active())return;
        var s=BastionState.client();if(s.equals(lastHeader))return;lastHeader=s;
        boolean night=BastionStatus.isNight(s.daytime());
        String day=s.valid()?Integer.toString(s.night()):"—";
        var header=Component.literal("LAST BASTION").withStyle(ChatFormatting.GOLD,ChatFormatting.BOLD)
            .append(Component.literal("\n"+(night?"Ночь ":"День ")+day+"  •  Угроза "+BastionStatus.threat(s.stage())+"  •  В сети "+s.online())
            .withStyle(style->style.withBold(false).withColor(ChatFormatting.GRAY)));
        var footer=Component.literal("\n"+BastionStatus.profile(s.event(),night)).withStyle(night?ChatFormatting.YELLOW:ChatFormatting.GRAY);
        mc.gui.getTabList().setHeader(header);mc.gui.getTabList().setFooter(footer);
    }
    @SubscribeEvent public static void render(RenderGuiEvent.Post event) {
        var mc=Minecraft.getInstance();
        if(!BastionState.active()||mc.player==null||mc.level==null||mc.screen!=null||mc.options.hideGui||mc.getDebugOverlay().showDebugScreen()||mc.options.keyPlayerList.isDown()||!preferences().visible)return;
        draw(event.getGuiGraphics(),BastionState.client());
    }
    public static void draw(GuiGraphics g,BastionState s) {
        var mc=Minecraft.getInstance();var font=mc.font;var prefs=preferences();boolean night=BastionStatus.isNight(s.daytime());
        String[] labels={night?"Ночь":"День","Угроза",night?"До рассвета":"До ночи","В сети"};
        String[] values={s.valid()?Integer.toString(s.night()):"—",BastionStatus.threat(s.stage()),BastionStatus.countdown(s.daytime(),s.frozen()),Integer.toString(s.online())};
        int width=font.width("LAST BASTION")+20;
        for(int i=0;i<4;i++)width=Math.max(width,font.width(labels[i])+font.width(values[i])+32);
        int height=83;float scale=prefs.scale();
        scale=Math.min(scale,Math.min((g.guiWidth()-16F)/width,(g.guiHeight()-16F)/height));if(scale<=0)return;
        int screenWidth=(int)(g.guiWidth()/scale),screenHeight=(int)(g.guiHeight()/scale);
        int x=prefs.position==3?8:screenWidth-width-8;
        int y=switch(prefs.position){case 1->28;case 2->screenHeight-height-52;default->(screenHeight-height)/2;};
        y=Math.max(8,Math.min(y,screenHeight-height-8));x=Math.max(8,x);
        g.pose().pushPose();g.pose().scale(scale,scale,1);
        g.fill(x,y,x+width,y+height,0xBE11171C);g.fill(x,y,x+2,y+height,0xFFE1B867);
        g.fill(x+2,y,x+width,y+22,0xA5242A30);
        g.drawString(font,"LAST BASTION",x+10,y+7,0xFFE1B867,false);
        for(int i=0;i<4;i++) {
            int rowY=y+29+i*12;
            g.drawString(font,labels[i],x+10,rowY,0xFFA2ADB5,false);
            int color=i==1&&s.stage()>=15?0xFFEB8075:0xFFE3E8E9;
            g.drawString(font,values[i],x+width-10-font.width(values[i]),rowY,color,false);
        }
        g.pose().popPose();
    }
}
