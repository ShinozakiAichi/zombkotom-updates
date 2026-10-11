"""Build a reviewable quest/data overlay from the installed v5.4 book, never saves."""
from pathlib import Path
import sys, json, hashlib, shutil, copy, collections
ROOT=Path('C:/Users/allen/AppData/Roaming/ElyPrismLauncher/instances/ZombKotom')
OUT=Path(__file__).parent
sys.path.insert(0,str(ROOT/'tools'))
from quest_snbt import read
BASE=ROOT/'minecraft/config/ftbquests/quests'
STAGE=OUT/'stage'; BOOK=STAGE/'config/ftbquests/quests'
DATA=STAGE/'config/openloader/packs/last_bastion/data/last_bastion'
def uid(s):return f'{int.from_bytes(hashlib.sha256(s.encode()).digest()[:8],"big") & 0x7fffffffffffffff:016X}'
def write(p,obj):p.parent.mkdir(parents=True,exist_ok=True);p.write_bytes((json.dumps(obj,ensure_ascii=False,indent=2)+'\n').encode())
def text(p,s):p.parent.mkdir(parents=True,exist_ok=True);p.write_bytes(s.encode())
if not (OUT/'before').exists():shutil.copytree(BASE,OUT/'before')
chapters={p.stem:read(p) for p in (OUT/'before/chapters').glob('*.snbt')}
old=copy.deepcopy(chapters);lang=read(OUT/'before/lang/ru_ru.snbt');oldlang=copy.deepcopy(lang)
refs={f'{s}:{i}':q for s,c in chapters.items() for i,q in enumerate(c['quests'])}
def q(ref):return refs[ref]
def title(ref,t):lang['quest.'+q(ref)['id']+'.title']=t
def lesson(ref,*lines):lang['quest.'+q(ref)['id']+'.quest_desc']=list(lines)
def adv(ref,name,replace=False):
 t={'id':uid(q(ref)['id']+':v55:adv:'+name),'type':'advancement','advancement':name,'criterion':''}
 if replace:q(ref)['tasks']=[t]
 else:q(ref)['tasks'].append(t)
def item(ref,name,count):
 oldtask=next(t for t in q(ref)['tasks'] if t['type']=='item')
 oldtask.update(id=uid(q(ref)['id']+':v55:output:'+name),item={'id':name,'count':1},count=count,consume_items=False)
def practice(ref):
 if not any(t['type']=='checkmark' for t in q(ref)['tasks']):q(ref)['tasks'].append({'id':uid(q(ref)['id']+':v55:practice'),'type':'checkmark'})
def supply(ref,name,count):
 r=next((r for r in q(ref)['rewards'] if r['type']=='item'),None)
 if r is None:r={'id':uid(q(ref)['id']+':v55:supply'),'type':'item'};q(ref)['rewards'].append(r)
 r.update(item={'id':name,'count':1},count=count)

# Native Create criteria prove actions, unlike a machine held in inventory.
for ref,a,replace in [('workshop:0','water_wheel',True),('workshop:1','mechanical_press',False),('workshop:2','fan_processing',True),('workshop:3','mechanical_mixer',True),('industry:6','steam_engine',True),('industry:5','packager',False),('transport:1','train',True),('transport:1','conductor',False),('supply:5','portable_storage_interface',False)]:adv(ref,'create:'+a,replace)
lesson('workshop:0','Результат: водяное колесо действительно вращает привод мастерской.','Удерживайте W над колесом в JEI для Ponder. Поставьте колесо в поток, выведите вал, затем подключите нагрузку. Если всё остановилось, проверьте направление воды и индикатор перегрузки.','Автоматически: достижение Create «Обузданная гидравлика», которое выдаётся за работающее колесо. Одного предмета в рюкзаке недостаточно.','Дальше можно параллельно освоить пресс, обработку воздухом и смешивание. Награда — материал для передачи вращения.')
lesson('workshop:1','Результат: первые 8 железных листов для мастерской.','Под прессом поставьте депо, подайте вращение и положите железные слитки. Проверьте ход головки и заберите листы.','Автоматически: достижение Create за прессование и 8 листов в инвентаре. Листы сохраняются; достижение подтверждает хотя бы одну операцию, а не происхождение всей партии.','Если листы нужны в постройке, сначала дайте книге их обнаружить, затем используйте. Не разбирайте установленный пресс.')
lesson('workshop:2','Результат: первая обработка предмета воздушным потоком.','Подключите вентилятор к приводу и направьте поток через воду. По JEI выберите рецепт промывки, например гравий, положите сырьё на депо в потоке и дождитесь выхода.','Автоматически: Create «Обработка воздухом». Наличие вентилятора не завершает урок.','Вода, огонь и лава дают разные виды обработки. Откройте применения исходного предмета в JEI перед сменой среды; лава не заменяет воду. Рабочие вентиляторы пригодятся для переработки руды.')
lesson('workshop:3','Результат: миксер выполнил первый рецепт в чаше.','Откройте в JEI смешивание андезитового сплава, загрузите ингредиенты в чашу и подключите миксер. Проверьте требуемую скорость; перегрузку нельзя исправить одним ускорением.','Автоматически: Create «Смешать, но не взбалтывать». Сам миксер в инвентаре не считается.','Для латуни понадобится нагрев. Следующая ветка объясняет экспедицию в Нижний мир и заполненную горелку.')
lesson('industry:6','Результат: котёл выдаёт вращение через паровой двигатель.','По Ponder соберите водяной резервуар-котёл, подведите воду насосом, установите нагрев и двигатель. Подключите выход к нагрузке. Проверяйте уровни воды, тепла и размера котла в очках инженера.','Автоматически: Create «С мёртвой точки!» — получение вращения от парового двигателя.','Сначала обеспечьте устойчивую подачу воды. Дополнительный двигатель сам по себе не повышает мощность недостаточного котла. Для отключения используйте муфту, сохраняя доступ к насосу.')
lesson('industry:5','Результат: адресованная посылка дошла до нужного склада.','Свяжите склад с упаковщиком, задайте адрес, упакуйте 16 железных слитков и отправьте их на приёмный узел. Проверьте содержимое после распаковки; повторите с другим предметом.','Автоматически: упаковщик в инвентаре и достижение Create «Почтовое дело». Ручная приёмка: обе посылки пришли на правильный склад, входные материалы не застряли.','Достижение подтверждает упаковку, но не расстояние и доставку. Если упаковщик уже установлен, достаточно прежнего зачтённого предметного условия; для нового прохождения дайте книге увидеть его перед установкой.')
lesson('transport:1','Результат: собранный поезд получает рабочее расписание.','Поставьте две станции, соберите поезд с управлением и местом машиниста. Назначьте машиниста по Ponder и передайте ему расписание обеих остановок. Начните с пустого вагона; исправьте сигналы и условия ожидания до загрузки ценностей.','Автоматически: достижения Create «Все на борт!» и «Инструктор машиниста». Станция в рюкзаке не доказывает готовность маршрута.','Книга подтверждает сборку поезда и передачу расписания. Грузовой рейс с разгрузкой — отдельное испытание в проектах снабжения.')
lesson('supply:5','Результат: механизм передаёт груз через портативный интерфейс.','Поставьте пару складских интерфейсов: один на неподвижном складе, второй на движущейся конструкции. Оставьте правильный зазор по Ponder; подайте 16 булыжника в грузовой отсек и проверьте выгрузку.','Автоматически: пара складских и пара жидкостных интерфейсов, а также Create «Обмен на ходу» за работу складского интерфейса.','Жидкостная пара работает отдельно. Проверьте её ведром воды, прежде чем возить топливо; это учебная рекомендация, а не дополнительное автоматическое условие.')

# Output batches replace token quantities and later demands to dismantle working machines.
item('survival:3','minecraft:stone_bricks',32)
item('survival:4','farmersdelight:vegetable_soup',4)
lesson('survival:4','Результат: четыре порции овощного супа для вылазки.','Поставьте котёл Farmer’s Delight над источником тепла. R в JEI покажет рецепт овощного супа и варианты овощей; подготовьте миски и сварите четыре порции.','Автоматически: 4 порции супа в инвентаре, без расходования. Найденная еда тоже засчитывается; книга не определяет её происхождение.','Унесите еду на разведку, а наградную морковь посадите рядом с базой. Котёл оставьте на месте — показывать его книге не требуется.')
item('arsenal:5','superbwarfare:rifle_ammo',32)
lesson('arsenal:5','Результат: первая партия из 32 винтовочных патронов Superb Warfare.','В JEI сравните ручной рецепт и добавленное смешивание Create. Запустите подходящий способ, отделите готовые патроны от сырья и подпишите склад.','Автоматически: 32 патрона в инвентаре, без расходования. Ручной и машинный выпуск оба допустимы; поздний заводской проект проверит устойчивое снабжение.','Перед вылазкой сверьте боеприпас с оружием. Патроны Create Gunsmithing не заменяют патроны Superb Warfare. Наградный порох пригодится для следующей партии.')
item('hard_factory:2','create:iron_sheet',32);practice('hard_factory:2')
lesson('hard_factory:2','Результат: четыре независимые линии питания, а не четыре генератора в рюкзаке.','Выделите мастерскую, оборону, удалённую добычу и резерв. Используйте освоенные генераторы; по одному отключайте основные источники и проверяйте, что остальные линии продолжают работу. На восстановленном приводе изготовьте 32 листа.','Автоматически: 32 листа. Ручная приёмка: четыре назначения подписаны, отказ одной линии не останавливает другие, резерв запускается. Рабочие генераторы разбирать не нужно.')
item('hard_factory:3','create:iron_sheet',16)
lesson('hard_factory:3','Результат: замена отказавшего привода без остановки всей базы.','Подготовьте один запасной мотор рядом с тремя рабочими участками. Остановите привод пресса, замените его резервным, изготовьте 16 листов и верните исправный мотор в аварийный запас.','Автоматически: 16 листов. Ручная приёмка: пресс восстановлен заменой, остальные участки не остановлены и запасной привод снова доступен. Четыре установленных мотора не нужно снимать для книги.')
item('hard_siege:1','cgs:round_gatling',64);practice('hard_siege:1')
lesson('hard_siege:1','Результат: две позиции огневой поддержки могут сменять друг друга.','Используйте два уже собранных гатлинга. Подготовьте 64 совместимых патрона, разнесите позиции и склады; проведите стрельбу по мишеням, пока второй расчёт перезаряжается.','Автоматически: 64 cgs:round_gatling. Ручная приёмка: обе позиции стреляют, смена расчётов не оставляет сектор без прикрытия, путь снабжения не пересекает линию огня. Оружие не нужно сдавать книге.')
q('hard_siege:3')['tasks']=[];practice('hard_siege:3')
lesson('hard_siege:3','Результат: штаб получает предупреждения с четырёх постов.','Организуйте четыре направления наблюдения. Используйте совместимые цели Radars там, где они обнаруживаются; для обычных зомби разрешён визуальный пост с Redstone Link. Подпишите сектора в штабе.','Ручная приёмка: каждый пост передал отдельный сигнал, оператор правильно назвал направление, отключённый пост был замечен и восстановлен. Четыре монитора не заменяют этот опыт.')
practice('hard_siege:4')
lesson('hard_siege:4','Результат: две артиллерийские позиции обслуживаются независимо.','Подготовьте два крепления CBC и соберите совместимые орудия по Ponder. На отдельном полигоне проверьте наведение, подходящий снаряд, выстрел и перезаряжание каждого. Подпишите боеприпасы и оставьте безопасный выход для расчёта.','Автоматически: два крепления. Ручная приёмка: обе позиции выполнили полный цикл, для каждой есть доступный запас совместимых снарядов; одно лишь крепление стрелять не умеет.')

# Calendar milestones deliberately do not pretend to detect combat or surviving an entire night.
milestones=[(10,'Десять ночей: тыл', 'Подготовьте освещённый склад, отдельный запас еды и запасной выход. Пройдите маршрут отхода без разрушения блоков.', 'minecraft:torch',32),(25,'Двадцать пять: ремонт', 'Выделите ремонтный склад с двумя стаками стройматериалов. Проведите учебное повреждение пустого участка стены и восстановите его, не открыв жилую зону.', 'minecraft:iron_ingot',8),(40,'Сорок: смена рубежа','Во время настоящей осады организованно отойдите с внешнего рубежа на внутренний. После боя восстановите проход и пополните боеприпасы.', 'minecraft:gunpowder',16),(70,'Семьдесят: отказ питания','Переживите осаду с подготовленным резервом. После боя отключите основную линию на 60 секунд, запитайте ворота и изготовьте 16 листов на резервном приводе.', 'minecraft:redstone',16),(100,'Сто: ремонт после осады','На сотой ночи или позднее переживите настоящую осаду, восстановите повреждённую оборону и доставьте на аванпост два стака стройматериалов. Подтвердите это вместе с напарником.', 'minecraft:gold_ingot',8)]
cid=uid('v55:chapter:night_milestones')
c={'id':cid,'filename':'night_milestones','group':'10EAADE9D48B4724','order_index':3,'icon':{'id':'minecraft:clock'},'default_quest_shape':'hexagon','quests':[],'quest_links':[]}
lang['chapter.'+cid+'.title']='Рубежи кампании'
lang['chapter.'+cid+'.subtitle']='Календарь угрозы и полевые испытания; необязательная ветка'
commands=['# World calendar only; does not assert presence during a night or a survived horde.']
for i,(day,name,trial,reward,count) in enumerate(milestones):
 aid=f'last_bastion:campaign/night_{day}';qid=uid(f'v55:milestone:{day}')
 write(DATA/f'advancement/campaign/night_{day}.json',{'criteria':{'calendar':{'trigger':'minecraft:impossible'}}})
 commands.append(f'execute if score #day lb_apoc matches {day}.. run advancement grant @a[gamemode=!spectator,advancements={{{aid}=false}}] only {aid}')
 obj={'id':qid,'dependencies':[q('survival:0')['id']],'progression_mode':'linear','shape':'hexagon','x':float((i%3)*4),'y':float((i//3)*4),'icon':{'id':'minecraft:clock'},'tasks':[{'id':uid(qid+':calendar'),'type':'advancement','advancement':aid,'criterion':''},{'id':uid(qid+':trial'),'type':'checkmark'}],'rewards':[{'id':uid(qid+':reward'),'type':'item','item':{'id':reward,'count':1},'count':count}]}
 c['quests'].append(obj)
 lang['quest.'+qid+'.title']=name
 lang['quest.'+qid+'.quest_subtitle']='Рубеж мира + совместная полевая приёмка'
 lang['quest.'+qid+'.quest_desc']=[f'Автоматически: в календаре этого мира завершилось не меньше {day} ночей. Метка выдаётся при входе и раз в секунду; старые миры получают уже пройденные рубежи.', 'Полевая приёмка: '+trial,'Календарная метка не доказывает участие в осаде или выживание без смерти. Только после опыта ставьте ручную отметку. Административная смена времени также меняет календарь.','Необязательное испытание: оно не запирает рецепты и другие направления. Награда — расходники после приёмки; одна выдача каждому участнику команды.']
chapters['night_milestones']=c
text(DATA/'function/journal/milestones.mcfunction','\n'.join(commands)+'\n')
clock=ROOT/'minecraft/config/openloader/packs/last_bastion/data/last_bastion/function/apocalypse/clock.mcfunction'
text(DATA/'function/apocalypse/clock.mcfunction',clock.read_text(encoding='utf-8').rstrip()+'\nfunction last_bastion:journal/milestones\n')
for ref in ['hard_siege:7','hard_endgame:7','megaprojects:4']:
 adv(ref,'last_bastion:campaign/night_100')
 lines=lang['quest.'+q(ref)['id']+'.quest_desc']
 lines=[s.replace('Книга не распознаёт размер базы, тип волны или сохранность цепей автоматически.','Книга не распознаёт размер базы, тип волны или сохранность цепей автоматически.') for s in lines]
 lines.insert(0,'Автоматический рубеж: календарь мира после сотой ночи. Совместная приёмка ниже отдельно подтверждает осаду, ремонт и снабжение; календарь не является доказательством победы.')
 lang['quest.'+q(ref)['id']+'.quest_desc']=lines
supply('workshop:0','minecraft:andesite',16)
supply('workshop:2','minecraft:gravel',16)
supply('workshop:3','minecraft:copper_ingot',8)
supply('transport:1','minecraft:iron_ingot',16)

# Regenerate navigation for edited quests; old unchanged task/reward IDs and all quest IDs remain.
for slug,c in chapters.items():
 own={o['id'] for o in c['quests']};external=sorted({d for o in c['quests'] for d in o.get('dependencies',[]) if d not in own})
 if slug=='night_milestones':c['quest_links']=[{'id':uid('v55:link:'+d),'linked_quest':d,'x':-4.,'y':float(i*2),'size':.8} for i,d in enumerate(external)]
 for obj in c['quests']:
  k='quest.'+obj['id'];tasks=obj['tasks'];changed=slug not in old or obj not in old[slug]['quests'] or lang.get(k+'.quest_desc')!=oldlang.get(k+'.quest_desc')
  if changed:
   auto=any(t['type']!='checkmark' for t in tasks);manual=any(t['type']=='checkmark' for t in tasks)
   lang[k+'.quest_subtitle']='Автоматический результат + полевая приёмка' if auto and manual else 'Полевая приёмка команды' if manual else 'Автоматическая проверка результата'
   if any(t['type']=='advancement' and t['advancement'].startswith('create:') for t in tasks):lang[k+'.quest_desc'].append('Достижение Create сохраняется заранее; книга учитывает его после открытия задания. Если механизм построил напарник, пусть он выполнит действие и откроет общую книгу FTB Teams.')
 if slug not in old or c!=old[slug]:write(BOOK/'chapters'/f'{slug}.snbt',c)
intro='quest.'+q('survival:0')['id']+'.quest_desc'
lang[intro].append('В уроках Create есть собственные достижения мода за работающий механизм. В «Рубежах кампании» календарь мира отмечается автоматически, а опыт обороны принимает команда. На каждом задании указано, что именно проверяется.')
lang[intro].append('Как читать книгу: предметы не расходуются; установленные блоки не нужно разбирать ради поздних испытаний. Награды забираются отдельно. Проверяйте задания перед постройкой и распределяйте роли в общей команде.')
for c in chapters.values():
 for obj in c['quests']:
  for task in obj['tasks']:
   if task['type']=='advancement' and task['advancement'].startswith('last_bastion:campaign/night_'):
    day=task['advancement'].rsplit('_',1)[1];lang['task.'+task['id']+'.title']='Календарный рубеж: '+day+' ночей'
   if c['filename']=='night_milestones' and task['type']=='checkmark':lang['task.'+task['id']+'.title']='Полевое испытание проведено'
write(BOOK/'lang/ru_ru.snbt',lang)
# en_us contains only the FPV fallback strings. New text uses the already configured ru_ru fallback.
changes=[]
for slug,c in chapters.items():
 for i,obj in enumerate(c['quests']):
  before=next((o for o in old.get(slug,{}).get('quests',[]) if o['id']==obj['id']),None);k='quest.'+obj['id']
  if obj!=before or lang.get(k+'.quest_desc')!=oldlang.get(k+'.quest_desc'):
   changes.append({'chapter':slug,'id':obj['id'],'before':before,'after':obj,'old_description':oldlang.get(k+'.quest_desc'),'description':lang.get(k+'.quest_desc')})
write(OUT/'changes.json',changes)
print('Staged',len(changes),'quest revisions;',sum(len(c['quests']) for c in chapters.values()),'quests;',len(chapters),'chapters')
