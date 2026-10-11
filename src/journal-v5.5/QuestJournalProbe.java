package journalqa;
import java.util.*;
import java.nio.file.*;
import com.mojang.authlib.GameProfile;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.item.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import dev.ftb.mods.ftbquests.quest.*;
import dev.ftb.mods.ftbquests.quest.task.*;
import dev.ftb.mods.ftbquests.quest.reward.*;
import dev.ftb.mods.ftbquests.quest.translation.TranslationKey;
import dev.ftb.mods.ftbquests.util.PlayerInventorySummary;
import dev.ftb.mods.ftblibrary.snbt.SNBT;

/** Isolated API test; deliberately not a claim of real multiplayer or survival playthrough. */
@Mod("journal_qa")
public class QuestJournalProbe {
 static class QAPlayer extends net.neoforged.neoforge.common.util.FakePlayer {
  QAPlayer(net.minecraft.server.level.ServerLevel level, GameProfile profile){super(level,profile);}
  @Override public boolean isFakePlayer(){return false;}
 }
 CommandSourceStack pending; int failed=0,passed=0;
 public QuestJournalProbe(){NeoForge.EVENT_BUS.addListener(this::register);NeoForge.EVENT_BUS.addListener(this::tick);}
 void register(RegisterCommandsEvent e){e.getDispatcher().register(Commands.literal("journalqa").requires(s->s.hasPermission(4)).executes(c->{pending=c.getSource();return 1;}));}
 void tick(net.neoforged.neoforge.event.tick.ServerTickEvent.Post e){
  if(calendarSource!=null){try{calendarTick();}catch(Throwable t){check(false,"calendar EXCEPTION "+t);t.printStackTrace();calendarCleanup();finish();}return;}
  if(pending!=null){var src=pending;pending=null;try{run(src);}catch(Throwable t){check(false,"EXCEPTION "+t);t.printStackTrace();calendarCleanup();}if(calendarSource==null)finish();}
 }
 CommandSourceStack calendarSource;
 QAPlayer calendarPlayer;
 java.util.List<net.minecraft.server.level.ServerPlayer> calendarPlayers;
 long calendarOldTime;
 int calendarIndex,calendarPhase;
 final int[] calendarDays={10,25,40,70,100};
 void finish(){System.out.println("JOURNAL_QA_RESULT pass="+passed+" fail="+failed+" real_clients=0");}
 void startCalendar(CommandSourceStack src)throws Exception{
  var server=src.getServer();calendarOldTime=server.overworld().getDayTime();calendarSource=src;
  calendarPlayer=new QAPlayer(server.overworld(),new GameProfile(UUID.randomUUID(),"CalendarProbe"));
  var pf=net.minecraft.server.players.PlayerList.class.getDeclaredField("players");pf.setAccessible(true);
  calendarPlayers=(java.util.List<net.minecraft.server.level.ServerPlayer>)pf.get(server.getPlayerList());
  calendarPlayers.add(calendarPlayer);server.overworld().addNewPlayer(calendarPlayer);
  calendarPlayer.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
  check(new net.minecraft.commands.arguments.selector.EntitySelectorParser(new com.mojang.brigadier.StringReader("@a[gamemode=!spectator]"),true).parse().findPlayers(src).contains(calendarPlayer),"calendar fixture visible to native selector");
  // Run inside this event: the disconnected fixture must not reach network time-sync ticks.
  while(calendarSource!=null)calendarTick();
 }
 void calendarCleanup(){
  if(calendarSource==null)return;
  var server=calendarSource.getServer();calendarPlayers.remove(calendarPlayer);calendarPlayer.discard();server.overworld().setDayTime(calendarOldTime);
  server.getCommands().performPrefixedCommand(calendarSource,"function last_bastion:apocalypse/clock");calendarSource=null;
 }
 void calendarTick(){
  var server=calendarSource.getServer();int day=calendarDays[calendarIndex];
  var a=server.getAdvancements().get(ResourceLocation.parse("last_bastion:campaign/night_"+day));
  switch(calendarPhase){
   case 0 -> {calendarPlayer.getAdvancements().revoke(a,"calendar");server.overworld().setDayTime((day-1)*24000L);}
   case 1 -> {check(!calendarPlayer.getAdvancements().getOrStartProgress(a).isDone(),"calendar before threshold "+day);server.overworld().setDayTime(day*24000L);}
   case 2 -> {check(calendarPlayer.getAdvancements().getOrStartProgress(a).isDone(),"calendar at threshold "+day);calendarPlayer.getAdvancements().revoke(a,"calendar");calendarPlayer.setGameMode(net.minecraft.world.level.GameType.SPECTATOR);}
   case 3 -> {check(!calendarPlayer.getAdvancements().getOrStartProgress(a).isDone(),"spectator excluded "+day);calendarPlayer.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);calendarIndex++;}
  }
  if(calendarIndex==calendarDays.length){calendarCleanup();return;}
  server.getCommands().performPrefixedCommand(calendarSource,"function last_bastion:apocalypse/clock");
  calendarPhase=(calendarPhase+1)%4;
 }

 void check(boolean value,String name){System.out.println("JOURNAL_QA_"+(value?"PASS ":"FAIL ")+name);if(value)passed++;else failed++;}
 List<Quest> quests(BaseQuestFile f){var list=new ArrayList<Quest>();for(var c:f.getAllChapters())list.addAll(c.getQuests());return list;}
 void run(CommandSourceStack src)throws Exception{
  var server=src.getServer();var file=ServerQuestFile.INSTANCE;var all=quests(file);var lookup=server.registryAccess();
  check(all.size()==165,"165 quests loaded by installed FTB parser");check(file.getAllChapters().size()==23,"23 chapters");
  int named=0;for(var g:file.getChapterGroups())if(!g.isDefaultGroup()){
   var ru=file.getTranslationManager().getStringTranslation(g,"ru_ru",TranslationKey.TITLE);
   var en=file.getTranslationManager().getStringTranslation(g,"en_us",TranslationKey.TITLE);
   check(ru.isPresent()&&ru.equals(en),"group title and English fallback "+g.getId());named++;
  }check(named==8,"8 named groups");
  int badItems=0,totalItems=0,rewards=0;
  for(var quest:all){
   var ru=file.getTranslationManager().getStringTranslation(quest,"ru_ru",TranslationKey.TITLE);
   var en=file.getTranslationManager().getStringTranslation(quest,"en_us",TranslationKey.TITLE);
   if(ru.isEmpty()||!ru.equals(en))badItems++;
   for(var task:quest.getTasks())if(task instanceof ItemTask it){totalItems++;if(it.getItemStack().isEmpty()||it.getItemStack().getItem() instanceof dev.ftb.mods.ftbquests.item.MissingItem)badItems++;}
   for(var reward:quest.getRewards())if(reward instanceof ItemReward ir){rewards++;if(ir.getItem().isEmpty()||ir.getItem().getItem() instanceof dev.ftb.mods.ftbquests.item.MissingItem)badItems++;}
  }check(badItems==0,"all item tasks/rewards resolve and 165 titles fall back; items="+totalItems+" rewards="+rewards);
  for(String site:List.of("hospital","factory","fuel_station","laboratory","rail_depot","radio_tower"))check(server.getAdvancements().get(ResourceLocation.parse("last_bastion:expedition/"+site))!=null,"advancement loaded "+site);
  for(var quest:all)for(var task:quest.getTasks())if(task instanceof AdvancementTask at){
   var n=new CompoundTag();at.writeData(n,lookup);
   check(server.getAdvancements().get(ResourceLocation.parse(n.getString("advancement")))!=null,"advancement resolves "+n.getString("advancement"));
  }
  var cases=SNBT.read(Path.of("qa-cases.snbt")).getList("cases",10);
  var player=new QAPlayer(server.overworld(),new GameProfile(UUID.fromString("b5d599b3-1a40-4a9f-9353-7838f9e20c52"),"JournalProbe"));
  var outsider=new QAPlayer(server.overworld(),new GameProfile(UUID.fromString("999fd7e5-45b0-4540-9f46-cdbf06efdb11"),"OtherInventory"));
  for(int i=0;i<cases.size();i++){
   var row=cases.getCompound(i);var quest=file.getQuest(Long.parseUnsignedLong(row.getString("id"),16));String name=row.getString("name");
   var team=new TeamData(UUID.randomUUID(),file,"isolated test");
   for(var other:all)if(other!=quest)team.setCompleted(other.getId(),new Date());
   player.getInventory().clearContent();outsider.getInventory().clearContent();int slot=0;
   for(var task:quest.getTasks())if(task instanceof ItemTask it){
    int remain=(int)it.getMaxProgress();while(remain>0){int n=Math.min(64,remain);var stack=it.getItemStack().copyWithCount(n);outsider.getInventory().setItem(slot++,stack);remain-=n;}
   }
   PlayerInventorySummary.build(player);
   for(var task:quest.getTasks())if(task instanceof ItemTask it)it.submitTask(team,player,ItemStack.EMPTY);
   check(quest.getTasks().stream().allMatch(t->team.getProgress(t)==0),name+" ignores other inventory");
   for(int s=0;s<36;s++)player.getInventory().setItem(s,outsider.getInventory().getItem(s).copy());
   int before=player.getInventory().items.stream().mapToInt(ItemStack::getCount).sum();
   PlayerInventorySummary.build(player);
   for(var task:quest.getTasks())if(task instanceof ItemTask it)it.submitTask(team,player,ItemStack.EMPTY);
   check(quest.getTasks().stream().filter(t->t instanceof ItemTask).allMatch(t->team.getProgress(t)==t.getMaxProgress()),name+" detects inventory quantities");
   check(before==player.getInventory().items.stream().mapToInt(ItemStack::getCount).sum(),name+" consumes no items");
   var manual=quest.getTasks().stream().filter(t->t instanceof CheckmarkTask).toList();
   if(!manual.isEmpty())check(!team.isCompleted(quest),name+" inventory alone cannot accept practical");
   for(var t:manual)team.setProgress(t,t.getMaxProgress());
   for(var task:quest.getTasks())if(task instanceof AdvancementTask)team.setProgress(task,task.getMaxProgress());
   team.checkAutoCompletion(quest);check(team.isCompleted(quest),name+" completes after all conditions (API simulation)");
   var unrelated=new TeamData(UUID.randomUUID(),file);check(!unrelated.isCompleted(quest),name+" isolated team unchanged");
  }
  player.getInventory().clearContent();outsider.getInventory().clearContent();PlayerInventorySummary.build(player);
  // A v5 save is serialized with the old book, then read against the real new definitions.
  var old=new ServerQuestFile(server);old.readDataFull(Path.of("D:/LastBastion/quality-v5.5/before"),lookup);
  var oldData=new TeamData(UUID.randomUUID(),old,"v5 migration fixture");var owner=UUID.randomUUID();
  for(var oq:quests(old)){
   oldData.setCompleted(oq.getId(),new Date());
   for(var r:oq.getRewards())oldData.markRewardAsClaimed(owner,r,System.currentTimeMillis());
  }
  var saved=oldData.serializeNBT();var migrationPath=Path.of("qa-migration.snbt").toAbsolutePath();
  check(SNBT.write(migrationPath,saved),"migration fixture saved to disk");
  var migrated=new TeamData(oldData.getTeamId(),file);migrated.deserializeNBT(SNBT.read(migrationPath));
  check(quests(old).stream().allMatch(oq->migrated.isCompleted(file.getQuest(oq.getId()))),"all 160 previous completed quest timestamps survive v5.4 to v5.5");
  int claimed=0,claimErrors=0;for(var oq:quests(old))for(var r:oq.getRewards()){var nr=file.getReward(r.getId());claimed++;if(nr==null||!migrated.isRewardClaimed(owner,nr))claimErrors++;}
  check(claimErrors==0&&claimed==270,"all 270 previous reward claims survive");
  
  for(var quest:all)for(var task:quest.getTasks())if(task instanceof AdvancementTask at){
   var n=new CompoundTag();at.writeData(n,lookup);var a=server.getAdvancements().get(ResourceLocation.parse(n.getString("advancement")));
   for(var key:a.value().criteria().keySet())player.getAdvancements().revoke(a,key);
   var testTeam=new TeamData(UUID.randomUUID(),file);
   check(!at.canSubmit(testTeam,player),"FTB rejects unearned advancement "+n.getString("advancement"));
   for(var key:a.value().criteria().keySet())player.getAdvancements().award(a,key);
   check(at.canSubmit(testTeam,player),"FTB detects saved advancement "+n.getString("advancement"));
  }
  check(all.stream().filter(z->old.getQuest(z.getId())==null).noneMatch(migrated::isCompleted),"five new milestones are not auto-completed by old save");
  startCalendar(src);

  var roundtrip=Path.of("qa-roundtrip-book").toAbsolutePath();file.writeDataFull(roundtrip,lookup);
  var reread=new ServerQuestFile(server);reread.readDataFull(roundtrip,lookup);
  check(quests(reread).size()==165&&reread.getAllTasks().size()==234,"native FTB write/read roundtrip");
 }
}
