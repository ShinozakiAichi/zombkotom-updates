from pathlib import Path
import json,zipfile,hashlib
ROOT=Path('C:/Users/allen/AppData/Roaming/ElyPrismLauncher/instances/ZombKotom');OUT=Path(__file__).parent
STAGE=OUT/'stage/config/openloader/packs/last_bastion_visuals/assets'
def put(name,data):p=STAGE/name;p.parent.mkdir(parents=True,exist_ok=True);p.write_bytes((json.dumps(data,indent=2)+'\n').encode())
for material in ['andesite','brass']:
 put(f'create_power_loader/models/item/{material}_chunk_loader.json',{'parent':f'create_power_loader:block/{material}_chunk_loader/item','textures':{'7':'create:block/axis','8':'create:block/axis_top'}})
for variant in ['stone','diamond']:
 # These are attachment heads, not weapons with a GeoItemRenderer. Original small
 # block models keep the two materials recognizable without copying NTGL artwork.
 def box(a,b,texture):return {'from':a,'to':b,'faces':{side:{'texture':texture} for side in ['north','south','east','west','up','down']}}
 put(f'ntgl/models/item/hammer_{variant}.json',{'parent':'minecraft:block/block','textures':{'head':'minecraft:block/'+('cobblestone' if variant=='stone' else 'diamond_block'),'socket':'minecraft:block/iron_block','particle':'#head'},'elements':[box([2,6,4],[14,12,12],'#head'),box([6,3,6],[10,6,10],'#socket')]})
# The distributed DnD JAR contains no sound files. Use the actual vanilla metal-chain events.
sounds={}
for action,subtitle in [('break','break'),('step','footsteps'),('place','place'),('hit','hit'),('fall','fall')]:
 sounds['large_chain_'+action]={'replace':True,'subtitle':'subtitles.block.generic.'+subtitle,'sounds':[{'name':'minecraft:block.chain.'+action,'type':'event','volume':0.9}]}
put('dndecor/sounds.json',sounds)
# Extract only local test inputs, never publish upstream animations as our own pack assets.
sources=[('superbwarfare-*','assets/superbwarfare/animations/qbz_95.animation.json'),('superbwarfare-*','assets/superbwarfare/animations/vector.animation.json'),('THEUNDEAD*','assets/undead_revamp2/animations/clogger.animation.json')]
for pattern,name in sources:
 p=next((ROOT/'minecraft/mods').glob(pattern))
 with zipfile.ZipFile(p) as z:
  target=OUT/'test-inputs'/Path(name).name;target.parent.mkdir(exist_ok=True);target.write_bytes(z.read(name))
print('Staged five resource files and three local animation test inputs')
