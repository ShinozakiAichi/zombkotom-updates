tag @s add lb_patriarch
attribute @s minecraft:generic.max_health base set 600
data merge entity @s {Health:600.0f,DeathLootTable:"last_bastion:entities/plague_patriarch"}
attribute @s minecraft:generic.movement_speed base set 0.055
attribute @s minecraft:generic.armor base set 22
attribute @s minecraft:generic.armor_toughness base set 8
attribute @s minecraft:generic.attack_damage base set 16
attribute @s minecraft:generic.scale base set 1.7
tellraw @a[distance=..64] {"text": "Замечен Чумной патриарх! 600 HP и тяжёлая броня. Готовьте пушки; вблизи отравляет и ослабляет.", "color": "dark_red"}
