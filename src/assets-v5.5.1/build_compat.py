from pathlib import Path
import subprocess,zipfile,json,hashlib,shutil
OUT=Path(__file__).parent;ROOT=Path('C:/Users/allen/AppData/Roaming/ElyPrismLauncher/instances/ZombKotom');base=Path('D:/LastBastion/fpv/qa');classes=OUT/'compat-classes';classes.mkdir(exist_ok=True)
for p in (ROOT/'tools/industrial-compat/src').rglob('*.java'):
 dest=OUT/'src'/p.relative_to(ROOT/'tools/industrial-compat/src');dest.parent.mkdir(parents=True,exist_ok=True);shutil.copy2(p,dest)
jars=[p for p in (base/'libraries').rglob('*.jar') if '21.1.238' not in str(p)];jars.sort(key=lambda p:0 if p.name=='neoforge-21.1.257-server.jar' else 1 if p.name.endswith('-srg.jar') else 2);jars+=list((base/'mods').glob('*.jar'));jars+=list((ROOT.parent.parent/'libraries').rglob('*.jar'))
jars.insert(0,OUT/'mixinextras-compile.jar')
args=['-proc:none','-encoding','UTF-8','-classpath',';'.join(map(str,jars)),'-d',str(classes)]+[str(p) for p in (OUT/'src').rglob('*.java')]
a=classes/'javac.args';a.write_text('\n'.join('"'+s.replace('\\','/')+'"' for s in args));subprocess.run(['C:/Program Files/Eclipse Adoptium/jdk-21.0.8.9-hotspot/bin/javac.exe','@'+str(a)],check=True)
jar=OUT/'lastbastion-industrial-compat-1.2.0.jar'
with zipfile.ZipFile(jar,'w',zipfile.ZIP_DEFLATED) as z:
 for p in (classes/'lastbastion').rglob('*.class'):z.write(p,p.relative_to(classes))
 for p in (OUT/'src').rglob('*.java'):z.write(p,'src/'+p.relative_to(OUT/'src').as_posix())
 z.write(ROOT/'tools/industrial-compat/LICENSE.txt','LICENSE.txt')
 z.writestr('META-INF/neoforge.mods.toml','modLoader="javafml"\nloaderVersion="[4,)"\nlicense="MIT"\n[[mods]]\nmodId="lastbastion_compat"\nversion="1.2.0"\ndisplayName="LAST BASTION Industrial Compatibility"\n[[mixins]]\nconfig="lastbastion.mixins.json"\n[[dependencies.lastbastion_compat]]\nmodId="lostcities"\ntype="required"\nversionRange="[1.21-8.4.6]"\nside="BOTH"\n[[dependencies.lastbastion_compat]]\nmodId="superbwarfare"\ntype="required"\nversionRange="[0.8.9.2]"\nside="BOTH"\n[[dependencies.lastbastion_compat]]\nmodId="geckolib"\ntype="required"\nversionRange="[4.9.3]"\nside="CLIENT"\n[[dependencies.lastbastion_compat]]\nmodId="railways"\ntype="required"\nversionRange="[0.2.1+neoforge-mc1.21.1]"\nside="CLIENT"\n')
 z.writestr('lastbastion.mixins.json',json.dumps({'required':True,'minVersion':'0.8','package':'lastbastion.mixin','compatibilityLevel':'JAVA_21','mixins':['ChunkDriverMixin','PendingBlockEntitiesAccessor','DroneSaveMixin'],'client':['AnimationJsonMixin','TextureAtlasContentsAccessor','PhantomSpriteUploadMixin'],'injectors':{'defaultRequire':1}}))
m={'slug':'lastbastion-industrial-compat','filename':jar.name,'sha512':hashlib.sha512(jar.read_bytes()).hexdigest(),'url':None,'server':True};(OUT/'updated-compat.json').write_text(json.dumps(m,indent=2));print(jar.name,m['sha512'])
