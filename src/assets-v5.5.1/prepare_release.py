from pathlib import Path
import json,tomllib,hashlib,re,shutil
OUT=Path(__file__).parent;REPO=Path('D:/LastBastion/auto-update/repo')
sha=lambda b:hashlib.sha256(b).hexdigest()
def write(n,b):
 p=REPO/n;p.parent.mkdir(parents=True,exist_ok=True);p.write_bytes(b)
client=json.loads((OUT/'client-validation.json').read_text());assert client['fail']==0 and client['gl_invalid_texture']==0 and client['process_exited'] and client['shutdown_saved_all_dimensions']
server=json.loads((OUT/'server-validation.json').read_text());assert server['exit']==0 and server['ready'] and any('fail=0' in s for s in server['result'])
assert all(json.loads((OUT/'common-class-validation.json').read_text()).values())
entries={e['file']:e for e in tomllib.loads((REPO/'index.toml').read_text())['files']};installed=json.loads((REPO/'installed-sha256.json').read_text())
old='mods/lastbastion-industrial-compat-1.1.0.jar'
if old in entries:
 entries.pop(old);installed.pop(old);p=REPO/old
 assert p.resolve().is_relative_to((REPO/'mods').resolve());p.unlink()
changed={}
for p in (OUT/'stage').rglob('*'):
 if not p.is_file():continue
 n=p.relative_to(OUT/'stage').as_posix();assert n.startswith('config/openloader/packs/last_bastion_visuals/assets/')
 b=p.read_bytes();json.loads(b);write(n,b);changed[n]=sha(b)
p=OUT/'lastbastion-industrial-compat-1.2.0.jar';n='mods/'+p.name;b=p.read_bytes();assert sha(b)==client['compat_sha256'];write(n,b);changed[n]=sha(b)
for n,h in changed.items():entries[n]={'file':n,'hash':h};installed[n]=h
for n,e in entries.items():
 assert not n.startswith(('saves/','shaderpacks/','xaero/','screenshots/')) and n not in ['options.txt','config/iris.properties','config/fpvcraft-radio.json']
 assert sha((REPO/n).read_bytes())==e['hash'],n
index='hash-format = "sha256"\n\n'
for n,e in sorted(entries.items()):index+='[[files]]\n'+''.join(k+' = '+('true' if v is True else json.dumps(v,ensure_ascii=False))+'\n' for k,v in e.items())+'\n'
write('index.toml',index.encode())
pack=(REPO/'pack.toml').read_text();pack=re.sub(r'(?m)^version = .*','version = "5.5.1"',pack);pack=re.sub(r'(?m)^hash = .*',f'hash = "{sha(index.encode())}"',pack);write('pack.toml',pack.encode())
write('installed-sha256.json',(json.dumps(installed,indent=2)+'\n').encode())
release=json.loads((REPO/'release.json').read_text());release.update(version='5.5.1',pack_sha256=sha(pack.encode()));write('release.json',(json.dumps(release,indent=2)+'\n').encode())
for p in (OUT/'src').rglob('*.java'):write('src/industrial-compat/'+p.relative_to(OUT/'src').as_posix(),p.read_bytes())
for name in ['AnimationRepairTest.java','AssetClientProbe.java','AssetGlDebugMixin.java','build_compat.py','stage_assets.py','prepare_release.py']:
 write('src/assets-v5.5.1/'+name,(OUT/name).read_bytes())
(OUT/'release-delta.json').write_text(json.dumps(changed,indent=2))
print('Prepared asset release 5.5.1:',len(entries),'entries;',len(changed),'new/changed; one obsolete own JAR retired')
