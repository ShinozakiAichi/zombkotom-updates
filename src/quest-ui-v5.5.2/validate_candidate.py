from pathlib import Path
import sys,json,math,collections,copy
OUT=Path(__file__).parent
sys.path.insert(0,'C:/Users/allen/AppData/Roaming/ElyPrismLauncher/instances/ZombKotom/tools')
from quest_snbt import read
before=OUT/'before';stage=OUT/'stage/config/ftbquests/quests'
new={p.stem:read(p) for p in (stage/'chapters').glob('*.snbt')}
old={p.stem:read(p) for p in (before/'chapters').glob('*.snbt')}
assert new.keys()==old.keys()
checks=[]
def check(ok,label):
 checks.append({'passed':bool(ok),'label':label})
 if not ok:raise AssertionError(label)
def strip_geometry(c):
 c=copy.deepcopy(c);c.pop('autofocus_id',None)
 for q in c['quests']+c.get('quest_links',[]):
  for field in ('x','y','size','shape'):q.pop(field,None)
 return c
all_ids=[];quests={q['id']:q for c in new.values() for q in c['quests']}
oldquests={q['id']:q for c in old.values() for q in c['quests']}
edge_through=[]
def point(q):return q['x'],q['y']
def distance(p,a,b):
 dx=b[0]-a[0];dy=b[1]-a[1];length=dx*dx+dy*dy
 t=max(0,min(1,((p[0]-a[0])*dx+(p[1]-a[1])*dy)/length)) if length else 0
 return math.hypot(p[0]-a[0]-t*dx,p[1]-a[1]-t*dy)
for name,c in new.items():
 check(strip_geometry(c)==strip_geometry(old[name]),name+': tasks, rewards, dependencies and IDs unchanged')
 check(c['autofocus_id'] in {q['id'] for q in c['quests']},name+': valid initial focus')
 nodes=c['quests']+c.get('quest_links',[])
 check(all(math.dist(point(a),point(b))>=1.5 for i,a in enumerate(nodes) for b in nodes[i+1:]),name+': no coincident or touching node centers')
 byid={q['id']:q for q in c['quests']};byid.update({q['linked_quest']:q for q in c.get('quest_links',[])})
 for q in c['quests']:
  for d in q.get('dependencies',[]):
   check(d in quests,name+': dependency exists '+d)
   check(d in byid,name+': dependency is represented by a node or a chapter link '+d)
   for k,n in byid.items():
    if k not in (d,q['id']) and distance(point(n),point(q),point(byid[d]))<.7:
     edge_through.append({'chapter':name,'from':d,'to':q['id'],'through':k})
 all_ids+=[c['id']]+[q['id'] for q in nodes]+[o['id'] for q in c['quests'] for o in q['tasks']+q['rewards']]
check(len(all_ids)==len(set(all_ids)),'all chapter, quest, link, task and reward IDs unique')
check(set(quests)==set(oldquests),'all 165 existing quest IDs preserved')
seen=set();active=set()
def visit(qid):
 check(qid not in active,'acyclic dependency '+qid)
 if qid in seen:return
 active.add(qid)
 for d in quests[qid].get('dependencies',[]):visit(d)
 active.remove(qid);seen.add(qid)
for qid in quests:visit(qid)
for locale in ('ru_ru','en_us'):
 lang=read(stage/'lang'/f'{locale}.snbt')
 for qid,q in quests.items():
  check(bool(lang.get('quest.'+qid+'.title')) and bool(lang.get('quest.'+qid+'.quest_desc')),locale+': title and nonempty description '+qid)
  desc=' '.join(lang['quest.'+qid+'.quest_desc'])
  check('не проверяет возраст мира' not in desc,locale+': no contradictory calendar claim '+qid)
 for c in new.values():check(bool(lang.get('chapter.'+c['id']+'.subtitle')),locale+': chapter route '+c['id'])
report={'status':'static checks passed; native UI review pending','checks':len(checks),'failed':sum(not c['passed'] for c in checks),'quests':len(quests),'chapters':len(new),'tasks':sum(len(q['tasks']) for q in quests.values()),'reward_ids_preserved':sum(len(q['rewards']) for q in quests.values()),'edge_through_node_candidates':edge_through,'production_modified':False,'native_ui_verified':False}
(OUT/'static-validation.json').write_text(json.dumps(report,ensure_ascii=False,indent=2),encoding='utf8')
print(json.dumps(report,ensure_ascii=False,indent=2))
