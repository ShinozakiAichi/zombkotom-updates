from pathlib import Path
import sys,json,collections,hashlib
OUT=Path(__file__).parent;ROOT=Path('C:/Users/allen/AppData/Roaming/ElyPrismLauncher/instances/ZombKotom')
sys.path.insert(0,str(ROOT/'tools'));from quest_snbt import read
base=OUT/'before';stage=OUT/'stage/config/ftbquests/quests'
files={p.name:p for p in (base/'chapters').glob('*.snbt')};files.update({p.name:p for p in (stage/'chapters').glob('*.snbt')})
chapters=[read(p) for p in files.values()];quests=[q for c in chapters for q in c['quests']];lookup={q['id']:q for q in quests};lang=read(stage/'lang/ru_ru.snbt')
ids=[o['id'] for c in chapters for o in [c]+c['quests']+c.get('quest_links',[])]+[o['id'] for q in quests for o in q['tasks']+q['rewards']]
assert len(ids)==len(set(ids)),[k for k,v in collections.Counter(ids).items() if v>1]
old=[q for p in (base/'chapters').glob('*.snbt') for q in read(p)['quests']]
assert all(q['id'] in lookup for q in old)
old_rewards={r['id'] for q in old for r in q['rewards']};assert old_rewards<={r['id'] for q in quests for r in q['rewards']}
seen=set();active=set()
def visit(qid):
 assert qid in lookup,qid
 assert qid not in active,('cycle',qid)
 if qid in seen:return
 active.add(qid)
 for dep in lookup[qid].get('dependencies',[]):visit(dep)
 active.remove(qid);seen.add(qid)
for q in quests:
 visit(q['id']);k='quest.'+q['id']
 assert lang.get(k+'.title') and lang.get(k+'.quest_desc'),k
 assert all(not t.get('consume_items',False) for t in q['tasks'])
 assert not q.get('repeatable',False)
for c in chapters:
 assert lang.get('chapter.'+c['id']+'.title'),c['id']
 for link in c.get('quest_links',[]):assert link['linked_quest'] in lookup
for p in (OUT/'stage').rglob('*'):
 if p.is_file() and p.suffix=='.json':json.loads(p.read_bytes())
report={'quests':len(quests),'chapters':len(chapters),'tasks':sum(len(q['tasks']) for q in quests),'types':dict(collections.Counter(t['type'] for q in quests for t in q['tasks'])),'old_quest_ids_preserved':len(old),'old_reward_ids_preserved':len(old_rewards),'unique_ids':len(ids),'acyclic':True,'all_translated':True,'files':{p.relative_to(OUT/'stage').as_posix():hashlib.sha256(p.read_bytes()).hexdigest() for p in (OUT/'stage').rglob('*') if p.is_file()}}
(OUT/'static-validation.json').write_text(json.dumps(report,indent=2))
print(json.dumps({k:v for k,v in report.items() if k!='files'}))
