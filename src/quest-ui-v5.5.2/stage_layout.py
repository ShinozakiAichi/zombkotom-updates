from pathlib import Path
import json,sys,math,copy
OUT=Path(__file__).parent
sys.path.insert(0,'C:/Users/allen/AppData/Roaming/ElyPrismLauncher/instances/ZombKotom/tools')
from quest_snbt import read
STAGE=OUT/'stage/config/ftbquests/quests';(STAGE/'chapters').mkdir(exist_ok=True)
# Coordinates follow each actual dependency branch, not the list's row wrapping.
layouts={
 'survival':[(-3,-3),(0,0),(3,-1.5),(3,1.5),(0,4),(3,4),(6,0),(-3,0)],
 'workshop':[(0,0),(3,0),(3,3),(6,0),(9,0),(12,0),(12,3),(15,0),(6,-3),(9,-3)],
 'enchanting':[(0,0),(3,-2),(6,-3.5),(6,-.5),(3,3),(6,2),(6,5),(9,2.25)],
 'equipment':[(0,0),(-3,-3),(0,-4),(3,-3),(4,0),(3,3)],
 'fuel':[(0,0),(3,-2),(6,-2),(9,-2),(3,2),(6,2),(9,2),(12,2)],
 'industry':[(0,-2),(3,-2),(3,1),(6,-.5),(0,4),(3,4),(0,7),(6,4)],
 'transport':[(0,-4),(3,-4),(0,-1),(0,3),(3,0),(3,3),(3,6),(6,4.5)],
 'supply':[(0,0),(3,-4),(3,-1),(3,2),(3,5),(6,5)],
 'recovery':[(-4,0),(0,0),(-1.5,-3),(2,-3),(3.5,0),(2,3)],
 'layered_defense':[(0,0),(3,0),(6,0),(9,0),(12,0)],
 'tracked_logistics':[(0,0),(3,0),(6,0),(9,0),(12,0)],
 'power_grid':[(0,0),(3,0),(6,0),(9,0),(12,-2),(12,2),(15,0),(15,-3.5)],
 'drones_payloads':[(0,-3),(3,-3),(0,2),(3,-.5),(3,2.5),(3,5.5),(0,7)],
 'night_milestones':[(0,0),(3,0),(6,0),(9,0),(12,0)],
 'defense':[(0,0),(0,3),(6,3),(6,-3),(9,-3),(6,6),(3,0),(9,0)],
 'expedition':[(0,0),(3,-4.5),(0,7),(-3,7),(3,-1.5),(3,1.5),(3,4.5),(6,7)],
 'arsenal':[(6,0),(0,0),(0,3),(0,6),(3,6),(3,0),(6,-3),(9,3)],
 'hard_factory':[(0,0),(-3,-3),(-3,0),(-3,3),(9,3),(6,0),(3,-3),(3,3)],
 'hard_siege':[(0,0),(-3,-3),(0,6),(3,6),(9,3),(6,0),(3,-3),(3,3)],
 'home':[(0,-3),(-3,-3),(-3,0),(-3,3),(0,3),(3,0),(6,-3),(9,0)],
 'megaprojects':[(0,0),(0,4),(4,0),(4,4),(7,4)],
}
hub_indices={
 'survival':[0,1], 'workshop':[0,4,5], 'arsenal':[5],
 'defense':[0,3], 'industry':[0,4,6], 'enchanting':[0,4],
 'transport':[0,3], 'expedition':[0], 'home':[0],
 'equipment':[0], 'fuel':[0,4], 'supply':[0], 'recovery':[1],
 'power_grid':[0,3], 'tracked_logistics':[0], 'drones_payloads':[0,2],
}
goal_indices={
 'survival':[6], 'workshop':[7], 'arsenal':[7], 'defense':[6,7],
 'industry':[7], 'enchanting':[7], 'transport':[7], 'expedition':[7],
 'home':[7], 'hard_factory':[7], 'hard_siege':[7], 'hard_endgame':[7],
 'fuel':[3,7], 'supply':[5], 'recovery':[5], 'power_grid':[7],
 'tracked_logistics':[3,4], 'layered_defense':[4],
 'megaprojects':[0,1,2,3,4], 'drones_payloads':[1,6],
 'night_milestones':[4],
}
subtitles={
 'survival':'Укрытие, еда и договор команды перед первой ордой',
 'workshop':'Вода → обработка материалов → нагрев → точная механика',
 'arsenal':'Выберите оружие и наладьте выпуск его боеприпасов',
 'defense':'Шлюзы, обнаружение и огневые позиции работают вместе',
 'industry':'Электропривод, добыча и складские заказы — три ветки завода',
 'enchanting':'Разбор трофеев, запас опыта и производство улучшений',
 'transport':'Поезда, наземные машины и авиация — выбирайте свой маршрут',
 'expedition':'Найдите путь, подготовьте отход и возвращайтесь с добычей',
 'home':'Обжитая крепость: мастерская, комнаты и промышленная архитектура',
 'hard_factory':'Партии материалов и непрерывная смена работающего завода',
 'hard_siege':'Полевые испытания обороны: сектора, резерв и эвакуация',
 'hard_endgame':'Независимые крупные проекты и испытание зрелой базы',
 'equipment':'Один походный комплект — несколько специализаций',
 'fuel':'Две топливные линии: урожай и нефтяная площадка',
 'supply':'Погрузка, питание и ремонт вдали от базы',
 'recovery':'Подготовьте возврат вещей и запас на случай потери',
 'power_grid':'Генерация → передача → обработка → резерв',
 'tracked_logistics':'Соберите ходовую и проверьте её в грузовом рейсе',
 'layered_defense':'Пять ступеней укреплений: от убежища до автономной базы',
 'industrial_expeditions':'Шесть необязательных объектов с полезными находками',
 'megaprojects':'Цитадель, транспортная сеть и автономные поселения',
 'drones_payloads':'Гонки, разведка и снабжение — разные задачи пилота',
 'night_milestones':'10 / 25 / 40 / 70 / 100 ночей: календарь и полевая практика',
}
def dist(a,b):return math.hypot(a[0]-b[0],a[1]-b[1])
def segment_distance(p,a,b):
 dx=b[0]-a[0];dy=b[1]-a[1];d=dx*dx+dy*dy
 t=max(0,min(1,((p[0]-a[0])*dx+(p[1]-a[1])*dy)/d)) if d else 0
 return dist(p,(a[0]+t*dx,a[1]+t*dy))
report=[]
for p in (OUT/'before/chapters').glob('*.snbt'):
 c=read(p);original=copy.deepcopy(c);qs=c['quests']
 if p.stem in layouts:
  assert len(qs)==len(layouts[p.stem])
  for q,(x,y) in zip(qs,layouts[p.stem]):q['x']=float(x);q['y']=float(y)
 for i,q in enumerate(qs):
  if i in goal_indices.get(p.stem,[]):q.update(shape='hexagon',size=1.7)
  elif i in hub_indices.get(p.stem,[]):q.update(shape='gear',size=1.5)
  else:q.update(shape='circle',size=1.0)
 if p.stem=='survival':qs[0].update(shape='square',size=1.7)
 pos={q['id']:(q['x'],q['y']) for q in qs}
 occupied=list(pos.values())
 edges=[(pos[q['id']],pos[d]) for q in qs for d in q.get('dependencies',[]) if d in pos]
 # Existing cross-chapter links retain IDs and targets, but move beside their
 # actual dependants. This eliminates the unrelated 22-unit-tall left column.
 for link in sorted(c.get('quest_links',[]),key=lambda l:-sum(l['linked_quest'] in q.get('dependencies',[]) for q in qs)):
  targets=[pos[q['id']] for q in qs if link['linked_quest'] in q.get('dependencies',[])]
  if not targets:continue
  minx=min(x for x,y in pos.values());maxx=max(x for x,y in pos.values())
  miny=min(y for x,y in pos.values());maxy=max(y for x,y in pos.values())
  candidates=[(x*1.5,y*1.5) for x in range(math.floor((minx-3)/1.5),math.ceil((maxx+3)/1.5)+1) for y in range(math.floor((miny-3)/1.5),math.ceil((maxy+3)/1.5)+1)]
  candidates=[v for v in candidates if all(dist(v,w)>=1.9 for w in occupied)]
  def score(v):
   cost=sum(dist(v,t) for t in targets)
   for a,b in edges:
    if segment_distance(v,a,b)<.85:cost+=50
   for t in targets:
    for node in occupied:
     if node!=t and segment_distance(node,v,t)<.85:cost+=50
   return cost + .025*abs(v[1])+.001*v[0]
  v=min(candidates,key=score);link['x'],link['y']=v;occupied.append(v)
  edges.extend((v,t) for t in targets)
 c['autofocus_id']=qs[0]['id']
 (STAGE/'chapters'/p.name).write_bytes((json.dumps(c,ensure_ascii=False,indent=2)+'\n').encode())
 allnodes=qs+c.get('quest_links',[])
 overlaps=[(a['id'],b['id']) for i,a in enumerate(allnodes) for b in allnodes[i+1:] if dist((a['x'],a['y']),(b['x'],b['y']))<1.5]
 assert not overlaps,(p.stem,overlaps)
 def area(nodes):return (max(n['x'] for n in nodes)-min(n['x'] for n in nodes),max(n['y'] for n in nodes)-min(n['y'] for n in nodes))
 report.append({'chapter':p.stem,'quests':len(qs),'links':len(c.get('quest_links',[])),'before_span':area(original['quests']+original.get('quest_links',[])),'after_span':area(allnodes),'node_overlaps':len(overlaps)})
for locale in ('ru_ru','en_us'):
 p=STAGE/'lang'/f'{locale}.snbt';lang=read(p)
 for f in (STAGE/'chapters').glob('*.snbt'):
  c=read(f);lang[f'chapter.{c["id"]}.subtitle']=subtitles[f.stem]
 p.write_bytes((json.dumps(lang,ensure_ascii=False,indent=2)+'\n').encode())
(OUT/'layout-change-summary.json').write_text(json.dumps(report,ensure_ascii=False,indent=2),encoding='utf8')
print('Staged',len(report),'chapters; zero node overlaps. Native layout validation still pending.')
