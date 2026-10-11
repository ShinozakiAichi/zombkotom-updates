"""Apply the tested, bounded journal delta to the curated Packwiz release."""
from pathlib import Path
import json,tomllib,hashlib,re,shutil
OUT=Path(__file__).parent;REPO=Path('D:/LastBastion/auto-update/repo')
def sha(b):return hashlib.sha256(b).hexdigest()
def enc(x):return json.dumps(x,ensure_ascii=False)
def write(n,b):p=REPO/n;p.parent.mkdir(parents=True,exist_ok=True);p.write_bytes(b)
version=tomllib.loads((REPO/'pack.toml').read_text())['version'];assert version in ['5.4.0','5.5.0'],version
runs=sorted(OUT.glob('runtime-*/result.json'));assert runs
result=json.loads(runs[-1].read_text());assert result['exit']==0 and result['ready'] and any('fail=0' in r for r in result['result']),result['result']
static=json.loads((OUT/'static-validation.json').read_text())
entries={e['file']:e for e in tomllib.loads((REPO/'index.toml').read_text())['files']};installed=json.loads((REPO/'installed-sha256.json').read_text())
for n,h in static['files'].items():
 p=OUT/'stage'/n;b=p.read_bytes();assert sha(b)==h,n
 assert n.startswith(('config/ftbquests/','config/openloader/')),n
 write(n,b);entries[n]={'file':n,'hash':h};installed[n]=h
for n,e in entries.items():
 assert not n.startswith(('saves/','shaderpacks/','xaero/','screenshots/')) and n not in ['options.txt','config/iris.properties','config/fpvcraft-radio.json'],n
 assert sha((REPO/n).read_bytes())==e['hash'],n
index='hash-format = "sha256"\n\n'
for n,e in sorted(entries.items()):index+='[[files]]\n'+''.join(k+' = '+('true' if v is True else enc(v))+'\n' for k,v in e.items())+'\n'
write('index.toml',index.encode())
pack=(REPO/'pack.toml').read_text();pack=re.sub(r'(?m)^version = .*','version = "5.5.0"',pack);pack=re.sub(r'(?m)^hash = .*',f'hash = "{sha(index.encode())}"',pack);write('pack.toml',pack.encode())
write('installed-sha256.json',(json.dumps(installed,indent=2)+'\n').encode())
release=json.loads((REPO/'release.json').read_text());release.update(version='5.5.0',quests=165,chapters=23,pack_sha256=sha(pack.encode()));write('release.json',(json.dumps(release,indent=2)+'\n').encode())
shutil.copy2(OUT/'QUESTS-5.5-RU.md',REPO/'QUESTS-5.5-RU.md')
for name in ['stage_journal.py','check_static.py','prepare_qa.py','prepare_release.py','QuestJournalProbe.java','CalendarProbeMethods.java.txt']:
 write('src/journal-v5.5/'+name,(OUT/name).read_bytes())
print('Prepared 5.5.0:',len(entries),'entries;',len(static['files']),'changed/added files; mods unchanged')
