from pathlib import Path
import json,re,sys,copy
OUT=Path(__file__).parent
HOST=Path('C:/Users/allen/AppData/Roaming/ElyPrismLauncher/instances/ZombKotom')
sys.path.insert(0,str(HOST/'tools'))
from quest_snbt import read
BEFORE=OUT/'before';STAGE=OUT/'stage/config/ftbquests/quests'
STAGE.mkdir(parents=True,exist_ok=True)
chapters={p.stem:read(p) for p in (BEFORE/'chapters').glob('*.snbt')}
quests={q['id']:q for c in chapters.values() for q in c['quests']}
remove={
 'Учёт: предметные условия проверяют ваш инвентарь, не процесс крафта; вещи не расходуются. Обнаружение доступно после предварительных уроков. Общая книга команды позволяет распределить работу между участниками.',
 'Ручную отметку ставьте после указанного опыта. Она не является автоматической проверкой схемы, боя или мультиплеера. Награду можно получить один раз.',
 'Контроль: получите указанный результат. Предметы сохраняются; наличие предмета не доказывает автоматизацию всей линии.',
 'Проверка: получите указанный предмет. Книга не проверяет проводку и работу всей линии.',
 'Награда — расходники для дальнейшей работы; получить её можно один раз.',
 'Достижение Create сохраняется заранее; книга учитывает его после открытия задания. Если механизм построил напарник, пусть он выполнит действие и откроет общую книгу FTB Teams.',
}
stats={'quests':len(quests),'changed_descriptions':0,'characters_before':0,'characters_after':0}
for locale in ('ru_ru','en_us'):
 lang={**read(BEFORE/'lang'/'ru_ru.snbt'),**read(BEFORE/'lang'/f'{locale}.snbt')}
 for k in list(lang):
  if k.startswith('chapter.') and k.endswith('.title'):
   lang[k]=re.sub(r'^\d+\s*•\s*','',lang[k])
 for qid,q in quests.items():
  key=f'quest.{qid}.quest_desc';old=lang[key];types={t['type'] for t in q['tasks']}
  desc=[s for s in old if s.strip() and s not in remove]
  desc=[s.replace('Приёмка: Совместная ручная приёмка:', 'Приёмка команды:')
         .replace('Приёмка: Ручная приёмка:', 'Приёмка команды:')
         .replace('Книга не проверяет возраст мира, факт осады или отсутствие читов автоматически.', 'Календарь проверяется автоматически; бой, ремонт и снабжение принимает команда.')
         for s in desc]
  # Put operational rules next to task widgets, but avoid duplicating a rule
  # already explained in a specific lesson.
  joined=' '.join(desc).lower()
  if 'item' in types and not any(s in joined for s in ('инвентар','вещи не расход','предметы сохраня','без расходования')):
   desc.append('Зачёт: предметы в инвентаре. После проверки они остаются у вас.')
  if 'checkmark' in types and not any(s in joined for s in ('отмет','ручн','приёмк','приемк')):
   desc.append('После испытания поставьте отметку команды в условиях задания.')
  if 'checkmark' in types:
   subtitle='Авто + практика команды' if len(types)>1 else 'Практика • отметка команды'
  elif 'advancement' in types:subtitle='Авто • достижение и результат' if len(types)>1 else 'Авто • достижение'
  elif 'kill' in types:subtitle='Авто • бой'
  elif 'dimension' in types:subtitle='Авто • экспедиция'
  else:subtitle='Авто • предметы в инвентаре'
  lang[f'quest.{qid}.quest_subtitle']=subtitle
  lang[key]=desc
  if locale=='ru_ru':
   stats['changed_descriptions']+=desc!=old
   stats['characters_before']+=sum(map(len,old));stats['characters_after']+=sum(map(len,desc))
 lang['quest.528BB03813D52220.quest_desc']=[
  'Ваша цель — построить базу, которая переживёт сто ночей, и наладить снабжение удалённых постов. Начните с укрытия, еды и пути отхода; промышленность появится следом.',
  'До первых заданий создайте общую команду FTB Teams и пригласите друзей. Прогресс общий: механик, разведчик и защитник могут заниматься разными ветками одновременно.',
  'Выберите направление слева. Стрелки показывают учебные зависимости, но рецепты квестами не заперты. Чары, авиация, реактор и декор — выбор команды.',
  'Автоматические условия отмечаются значками справа: предметы в инвентаре не расходуются; достижения Create подтверждают действие. Уже полученное достижение засчитывается после открытия урока, когда его обладатель в игре и в команде.',
  'Практика — испытание работающей базы. Выполните условия из описания и поставьте отметку команды. Книга не распознаёт произвольную постройку, рейс или победу в осаде.',
  'Награды забирайте в панели задания. Каждый участник получает их один раз. В «Рубежах кампании» календарь отмечается автоматически; бой и восстановление базы остаются вашей практикой.',
 ]
 # Editorial lessons supplement existing objectives; no task or reward changes.
 lessons=json.loads((OUT/'create_lessons.json').read_text(encoding='utf8'))
 for qid,description in lessons.items():
  assert qid in quests, qid
  lang[f'quest.{qid}.quest_desc']=description
 if locale=='ru_ru':
  baseline=read(BEFORE/'lang'/'ru_ru.snbt')
  keys=[f'quest.{qid}.quest_desc' for qid in quests]
  stats['changed_descriptions']=sum(lang[k]!=baseline[k] for k in keys)
  stats['characters_before']=sum(sum(map(len,baseline[k])) for k in keys)
  stats['characters_after']=sum(sum(map(len,lang[k])) for k in keys)
  stats['expanded_create_lessons']=len(lessons)
 # Save translated text; stage_layout.py adds the chapter route subtitles.
 path=STAGE/'lang'/f'{locale}.snbt';path.parent.mkdir(exist_ok=True)
 path.write_bytes((json.dumps(lang,ensure_ascii=False,indent=2)+'\n').encode())
(OUT/'editorial-change-summary.json').write_text(json.dumps(stats,ensure_ascii=False,indent=2),encoding='utf8')
print(json.dumps(stats))
