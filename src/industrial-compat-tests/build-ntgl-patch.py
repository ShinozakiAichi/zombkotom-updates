"""Rebuild the 1.3.4 delta against the pinned 1.3.3 artifact with JDK 21.

--classpath-file contains one semicolon-separated (Windows) or colon-separated
classpath of the mapped Minecraft 1.21.1, NeoForge 21.1.257 and Mixin libraries.
No downloading, launching Minecraft, or installation is performed.
"""
from pathlib import Path
import argparse,subprocess,zipfile,json,hashlib,copy,tempfile

p=argparse.ArgumentParser()
p.add_argument('--base-jar',type=Path,required=True)
p.add_argument('--classpath-file',type=Path,required=True)
p.add_argument('--javac',required=True)
p.add_argument('--output',type=Path,required=True)
a=p.parse_args()
assert hashlib.sha256(a.base_jar.read_bytes()).hexdigest()=='b3aa3db546a3ebb0df0e182270bea427f6b6e6ac5ada546714a05ceb5694f5df','Wrong 1.3.3 base'
assert a.output.resolve()!=a.base_jar.resolve(),'Do not overwrite the base'
src=Path(__file__).resolve().parent.parent/'industrial-compat'
files=[src/'lastbastion/resources/ConfigScanResources.java',src/'lastbastion/mixin/NtglConfigScanMixin.java']
with tempfile.TemporaryDirectory(prefix='lastbastion-ntgl-') as tmp:
    tmp=Path(tmp);classes=tmp/'classes';classes.mkdir()
    args=['-proc:none','-encoding','UTF-8','-classpath',a.classpath_file.read_text().strip(),'-d',classes.as_posix()]+[f.as_posix() for f in files]
    argfile=tmp/'javac.args';argfile.write_text('\n'.join('"'+s.replace('\\','/')+'"' for s in args),encoding='utf8')
    subprocess.run([a.javac,'@'+str(argfile)],check=True)
    replacements={f.relative_to(classes).as_posix():f.read_bytes() for f in classes.rglob('*.class')}
    with zipfile.ZipFile(a.base_jar) as base:
        replacements['META-INF/neoforge.mods.toml']=base.read('META-INF/neoforge.mods.toml').replace(b'version="1.3.3"',b'version="1.3.4"')
        mixins=json.loads(base.read('lastbastion.mixins.json'));mixins['mixins'].append('NtglConfigScanMixin')
        replacements['lastbastion.mixins.json']=json.dumps(mixins,indent=2).encode()
        with zipfile.ZipFile(a.output,'w',zipfile.ZIP_DEFLATED) as target:
            for entry in base.infolist():
                if entry.filename not in replacements:target.writestr(copy.copy(entry),base.read(entry.filename))
            for name,data in sorted(replacements.items()):
                entry=zipfile.ZipInfo(name,(1980,1,1,0,0,0));entry.compress_type=zipfile.ZIP_DEFLATED;target.writestr(entry,data)
        with zipfile.ZipFile(a.output) as target:
            assert all(base.read(n)==target.read(n) for n in base.namelist() if n not in replacements)
print(hashlib.sha256(a.output.read_bytes()).hexdigest(),a.output)
