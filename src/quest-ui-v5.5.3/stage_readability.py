"""Paginate existing lessons without changing content or quest progression."""
from pathlib import Path
import sys,json,shutil,re
OUT=Path(__file__).parent
HOST=Path('C:/Users/allen/AppData/Roaming/ElyPrismLauncher/instances/ZombKotom')
sys.path.insert(0,str(HOST/'tools'))
from quest_snbt import read
BASE=Path('C:/LastBastion/auto-update/repo/config/ftbquests/quests')
DEST=OUT/'readability-stage/config/ftbquests/quests'
shutil.copytree(BASE,DEST,dirs_exist_ok=True)
report={}
for locale in ('ru_ru','en_us'):
    lang=read(BASE/'lang'/f'{locale}.snbt'); changed=[]
    for key,original in list(lang.items()):
        if not key.endswith('.quest_desc') or not isinstance(original,list):continue
        paragraphs=[s for s in original if s and s!='{@pagebreak}']
        if len(paragraphs)<2:continue
        pages=[];page=[];size=0
        for paragraph in paragraphs:
            if page and size+len(paragraph)>520:
                pages.append(page);page=[];size=0
            page.append(paragraph);size+=len(paragraph)
        if page:pages.append(page)
        result=[]
        for pi,page in enumerate(pages):
            if pi:result.append('{@pagebreak}')
            for i,paragraph in enumerate(page):
                if i:result.append('')
                result.append(paragraph)
        assert [s for s in result if s and s!='{@pagebreak}']==paragraphs
        lang[key]=result
        if result!=original:changed.append({'key':key,'pages':len(pages),'longest_page_chars':max(sum(map(len,p)) for p in pages)})
    (DEST/'lang'/f'{locale}.snbt').write_text(json.dumps(lang,ensure_ascii=False,indent=2)+'\n',encoding='utf8')
    assert read(DEST/'lang'/f'{locale}.snbt')==lang
    report[locale]=changed
for p in BASE.rglob('*'):
    if p.is_file() and p.parent.name!='lang':assert p.read_bytes()==(DEST/p.relative_to(BASE)).read_bytes()
(OUT/'readability-validation.json').write_text(json.dumps(report,ensure_ascii=False,indent=2),encoding='utf8')
print(json.dumps({k:{'descriptions':len(v),'paginated':sum(r['pages']>1 for r in v),'max_pages':max(r['pages'] for r in v)} for k,v in report.items()}))
