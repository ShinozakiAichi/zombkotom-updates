scoreboard players set #event lb_apoc 0
execute if score #night lb_apoc matches 11.. store result score #event lb_apoc run random value 0..4
execute if score #event lb_apoc matches 3 if score #night lb_apoc matches ..15 run scoreboard players set #event lb_apoc 0
execute if score #event lb_apoc matches 4 if score #night lb_apoc matches ..35 run scoreboard players set #event lb_apoc 0
execute if score #event lb_apoc matches 0 run tellraw @a {"text": "[Сводка] Обычная ночь. Проверьте запасы и отход. Расписание и лимиты орд сохранены.", "color": "yellow"}
execute if score #event lb_apoc matches 1 run tellraw @a {"text": "[Сводка] Бегущая стая: больше бегунов среди новых зомби. Закройте фланги и внешние ворота.", "color": "yellow"}
execute if score #event lb_apoc matches 2 run tellraw @a {"text": "[Сводка] Бронированный натиск: больше танков. Готовьте перекрёстный огонь и ремонт второго рубежа.", "color": "yellow"}
execute if score #event lb_apoc matches 3 run tellraw @a {"text": "[Сводка] Прыгающая стая: больше прыгунов. Низкой стены мало; перекройте подходы к стрелковым позициям.", "color": "yellow"}
execute if score #event lb_apoc matches 4 run tellraw @a {"text": "[Сводка] Чумная ночь: больше носителей яда. Приготовьте молоко и держите дистанцию от проломов.", "color": "yellow"}
