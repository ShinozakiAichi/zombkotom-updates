package questui;

import com.google.gson.*;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.quest.*;
import dev.ftb.mods.ftblibrary.ui.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import java.nio.file.*;
import java.util.*;

/** Private client fixture: uses the real FTB screen and never completes quests. */
@Mod(value="quest_ui_qa",dist=Dist.CLIENT)
public class QuestUiProbe {
    int ticks, totalTicks, step=-1, wait, stopDelay; boolean done, pendingBottom;
    record CaptureJob(Chapter chapter, Quest quest, int page, boolean bottom, String label) {}
    final List<CaptureJob> jobs=new ArrayList<>();
    QuestScreen screen;
    List<Chapter> chapters;
    final JsonArray evidence=new JsonArray();
    Path output;
    public QuestUiProbe(){NeoForge.EVENT_BUS.addListener(this::tick);}
    void tick(ClientTickEvent.Post event){
        var mc=Minecraft.getInstance();
        if(!Files.exists(mc.gameDirectory.toPath().resolve("quest-ui.enabled")))return;
        if(done){if(++stopDelay==40)mc.stop();return;}
        if(++totalTicks>7200){System.out.println("QUEST_UI_QA_FAIL timeout awaiting world or capture");done=true;return;}
        if(mc.screen instanceof net.minecraft.client.gui.screens.PauseScreen)mc.setScreen(null);
        if(mc.player==null || mc.level==null || mc.getOverlay()!=null || !ClientQuestFile.exists())return;
        try {
            if(screen==null){
                if(!mc.gameDirectory.getAbsolutePath().contains("ZombKotom-Quest-QA"))throw new IllegalStateException("Not isolated profile");
                if(Files.getFileStore(mc.gameDirectory.toPath()).getUsableSpace()<512L*1024*1024)throw new IllegalStateException("Disk reserve");
                if(mc.getWindow().getWidth()!=1280 || mc.getWindow().getHeight()!=720)throw new IllegalStateException("Unexpected capture resolution");
                if(++ticks==1){
                    var server=mc.getSingleplayerServer();
                    if(server!=null)server.execute(() -> {var p=server.getPlayerList().getPlayers().getFirst();p.setGameMode(net.minecraft.world.level.GameType.CREATIVE);p.getAbilities().flying=true;p.onUpdateAbilities();p.teleportTo(416.5,95,416.5);});
                }
                if(ticks<100)return;
                var file=ClientQuestFile.getInstance();if(file.selfTeamData==null)return;
                output=mc.gameDirectory.toPath().resolve("quest-ui-newage-fixes-v1");Files.createDirectories(output);
                chapters=new ArrayList<>(file.getAllChapters());
                chapters.removeIf(c -> !c.getFilename().equals("power_grid"));
                for(var c:chapters){
                    jobs.add(new CaptureJob(c,null,0,false,c.getFilename()+"-map"));
                    var selected=new LinkedHashSet<Quest>();
                    for(var q:c.getQuests())if(Set.of("0897FC0A5A015593","7FB6DC58EB28366F","7A49EBB5A1AAE36B").contains(String.format("%016X",q.id)))selected.add(q);
                    int qi=0;
                    for(var q:selected){
                        String label=c.getFilename()+"-q"+(++qi);
                        int pages=Math.max(1,q.buildDescriptionIndex().size());
                        for(int page=0;page<pages;page++){
                            String id=String.format("%016X",q.id);
                            if(id.equals("0897FC0A5A015593")&&page!=2 || id.equals("7FB6DC58EB28366F")&&page!=0 || id.equals("7A49EBB5A1AAE36B")&&page>1)continue;
                            jobs.add(new CaptureJob(c,q,page,false,label+"-p"+page+"-top"));
                            jobs.add(new CaptureJob(c,q,page,true,label+"-p"+page+"-bottom"));
                        }
                    }
                }
                if(jobs.size()!=9)throw new IllegalStateException("Expected exactly 9 jobs");
                file.setEditorPermission(false);
                screen=ClientQuestFile.openGui();
                System.out.println("QUEST_UI_QA opened chapters="+chapters.size());
                wait=30;return;
            }
            if(--wait>0)return;
            if(pendingBottom){scrollBottom(screen.viewQuestPanel);pendingBottom=false;wait=5;return;}
            String label=step<0?"000-home":String.format("%03d-",step+1)+jobs.get(step).label();
            try(var img=Screenshot.takeScreenshot(mc.getMainRenderTarget())){img.writeToFile(output.resolve(label+".png"));}
            JsonObject row=new JsonObject();row.addProperty("screen",label);row.addProperty("width",mc.getWindow().getGuiScaledWidth());row.addProperty("height",mc.getWindow().getGuiScaledHeight());row.addProperty("zoom",screen.getZoom());
            row.addProperty("selected",screen.getSelectedChapter().map(c->c.getTitle().getString()).orElse("none"));
            row.addProperty("detail",screen.getViewedQuest()==null?"":screen.getViewedQuest().getTitle().getString());
            if(step>=0)row.addProperty("page",jobs.get(step).page());
            row.add("widgets",widgets(screen,0));
            if(screen.getViewedQuest()!=null)row.add("detail_widgets",widgets(screen.viewQuestPanel,0));
            evidence.add(row);
            System.out.println("QUEST_UI_QA captured "+label);
            if(++step>=jobs.size()){
                Files.writeString(output.resolve("screens.json"),new GsonBuilder().setPrettyPrinting().create().toJson(evidence));
                Files.writeString(output.resolve("result.txt"),"chapters="+chapters.size()+" screenshots="+evidence.size()+" capture_errors=0 visual_review=pending real_network_clients=0\n");
                System.out.println("QUEST_UI_QA_RESULT chapters="+chapters.size()+" screenshots="+evidence.size()+" capture_errors=0 visual_review=pending");
                done=true;screen.closeGui();return;
            }
            var job=jobs.get(step);var c=job.chapter();
            screen.closeQuest();screen.selectChapter(c);
            c.getAutofocus().ifPresent(screen::scrollTo);
            if(job.quest()!=null){screen.viewQuest(job.quest());screen.viewQuestPanel.setCurrentPage(job.quest().id,job.page());screen.refreshViewQuestPanel();}
            pendingBottom=job.bottom();
            wait=15;
        }catch(Throwable t){
            t.printStackTrace();System.out.println("QUEST_UI_QA_FAIL "+t);done=true;
            try{if(output!=null)Files.writeString(output.resolve("failure.txt"),t.toString());}catch(Exception ignored){}
        }
    }
    void scrollBottom(Panel p){
        for(var w:p.getWidgets())if(w instanceof Panel child)scrollBottom(child);
        if(p.getContentHeight()>p.getHeight())p.setScrollY(p.getContentHeight()-p.getHeight());
    }
    JsonArray widgets(Panel p,int depth){
        JsonArray a=new JsonArray();
        if(depth>5)return a;
        for(var w:p.getWidgets()){
            JsonObject o=new JsonObject();o.addProperty("class",w.getClass().getSimpleName());o.addProperty("title",w.getTitle().getString());o.addProperty("x",w.getX());o.addProperty("y",w.getY());o.addProperty("w",w.getWidth());o.addProperty("h",w.getHeight());o.addProperty("enabled",w.isEnabled());
            if(w instanceof Panel child)o.add("children",widgets(child,depth+1));a.add(o);
        }return a;
    }
}
