from pathlib import Path
import sys,json,shutil,subprocess,zipfile,re
OUT=Path(__file__).parent;ROOT=Path('C:/Users/allen/AppData/Roaming/ElyPrismLauncher/instances/ZombKotom');QA=Path('D:/LastBastion/fpv/qa')
sys.path.insert(0,str(ROOT/'tools'));from quest_snbt import read
# Reuse installed server libraries on D. No user's worlds are opened or modified.
shutil.copytree(ROOT/'minecraft/config/ftbquests',QA/'config/ftbquests',dirs_exist_ok=True)
shutil.copytree(OUT/'stage',QA,dirs_exist_ok=True)
book=QA/'config/ftbquests/quests';chapters={p.stem:read(p) for p in (book/'chapters').glob('*.snbt')}
tasks=sum(len(q['tasks']) for c in chapters.values() for q in c['quests'])
oldquests=[q for p in (OUT/'before/chapters').glob('*.snbt') for q in read(p)['quests']];rewards=sum(len(q['rewards']) for q in oldquests)
cases=[]
for slug,i in [('workshop',1),('survival',4),('arsenal',5),('hard_factory',2),('hard_factory',3),('hard_siege',1),('power_grid',2),('fuel',3),('layered_defense',4),('drones_payloads',2)]:
 q=chapters[slug]['quests'][i];cases.append({'id':q['id'],'name':slug+':'+str(i)})
(QA/'qa-cases.snbt').write_text(json.dumps({'cases':cases}))
s=(ROOT/'tools/QuestJournalProbe.java').read_text(encoding='utf-8')
# NeoForge's ordinary FakePlayer deliberately has no-op advancements. Use the real
# PlayerAdvancements implementation with a synthetic, disconnected QA player.
s=s.replace('FakePlayerFactory.get(', 'new QAPlayer(')
s=s.replace(' CommandSourceStack pending;', ' static class QAPlayer extends net.neoforged.neoforge.common.util.FakePlayer {\n  QAPlayer(net.minecraft.server.level.ServerLevel level, GameProfile profile){super(level,profile);}\n  @Override public boolean isFakePlayer(){return false;}\n }\n CommandSourceStack pending;')
s=s.replace('all.size()==153','all.size()==165').replace('153 quests loaded','165 quests loaded').replace('size()==21','size()==23').replace('21 chapters','23 chapters')
s=s.replace('153 titles','165 titles').replace('D:/LastBastion/quests-v5.2/before',str(OUT/'before').replace('\\','/'))
s=s.replace('all.stream().allMatch(migrated::isCompleted)','quests(old).stream().allMatch(oq->migrated.isCompleted(file.getQuest(oq.getId())))')
s=s.replace('all 153 completed quest timestamps survive v5 to v5.2','all 160 previous completed quest timestamps survive v5.4 to v5.5')
s=s.replace('claimed==246',f'claimed=={rewards}').replace('all 246 previous reward claims survive',f'all {rewards} previous reward claims survive')
s=s.replace('size()==153&&reread.getAllTasks().size()==199',f'size()==165&&reread.getAllTasks().size()=={tasks}')
# Allow the native advancement tasks in mixed lessons without treating fake possession as production.
s=s.replace('for(var t:manual)team.setProgress(t,t.getMaxProgress());','for(var t:manual)team.setProgress(t,t.getMaxProgress());\n   for(var task:quest.getTasks())if(task instanceof AdvancementTask)team.setProgress(task,task.getMaxProgress());')
# Inspect ALL advancement references through the actual loaded registry.
s=s.replace('var cases=SNBT.read', '''for(var quest:all)for(var task:quest.getTasks())if(task instanceof AdvancementTask at){
   var n=new CompoundTag();at.writeData(n,lookup);
   check(server.getAdvancements().get(ResourceLocation.parse(n.getString("advancement")))!=null,"advancement resolves "+n.getString("advancement"));
  }
  var cases=SNBT.read''')
extra='''
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
'''
s=s.replace('var roundtrip=Path.of',extra+'\n  var roundtrip=Path.of')
s=re.sub(r' void tick\(.*?\n void check', ' void tick(net.neoforged.neoforge.event.tick.ServerTickEvent.Post e){\n  if(calendarSource!=null){try{calendarTick();}catch(Throwable t){check(false,"calendar EXCEPTION "+t);t.printStackTrace();calendarCleanup();finish();}return;}\n  if(pending!=null){var src=pending;pending=null;try{run(src);}catch(Throwable t){check(false,"EXCEPTION "+t);t.printStackTrace();calendarCleanup();}if(calendarSource==null)finish();}\n }\n void check', s, flags=re.S)
s=s.replace(' void check', (OUT/'CalendarProbeMethods.java.txt').read_text()+'\n void check',1)
(OUT/'QuestJournalProbe.java').write_bytes(s.encode())
classes=OUT/'qa-classes';classes.mkdir(exist_ok=True)
jars=[p for p in (QA/'libraries').rglob('*.jar') if '21.1.238' not in str(p)];jars.sort(key=lambda p:0 if p.name=='neoforge-21.1.257-server.jar' else 1 if p.name.endswith('-srg.jar') else 2);jars+=list((QA/'mods').glob('*.jar'))
args=['-proc:none','-encoding','UTF-8','-classpath',';'.join(map(str,jars)),'-d',str(classes),str(OUT/'QuestJournalProbe.java')];a=classes/'javac.args';a.write_text('\n'.join('"'+s.replace('\\','/')+'"' for s in args))
subprocess.run(['C:/Program Files/Eclipse Adoptium/jdk-21.0.8.9-hotspot/bin/javac.exe','@'+str(a)],check=True)
with zipfile.ZipFile(QA/'mods/journal-qa.jar','w') as z:
 for p in classes.rglob('*.class'):z.write(p,p.relative_to(classes))
 z.writestr('META-INF/neoforge.mods.toml','modLoader="javafml"\nloaderVersion="[4,)"\nlicense="Private QA"\n[[mods]]\nmodId="journal_qa"\nversion="1.0"\ndisplayName="Journal QA"\n')
s=Path('D:/LastBastion/fpv/run_qa.py').read_text().replace("Path('D:/LastBastion/fpv')","Path('D:/LastBastion/quality-v5.5')").replace('FPV_QA','JOURNAL_QA').replace("send('fpvqa')","send('journalqa')")
(OUT/'run_qa.py').write_text(s)
print('Prepared journal runtime QA;',tasks,'tasks;',rewards,'preserved rewards')
