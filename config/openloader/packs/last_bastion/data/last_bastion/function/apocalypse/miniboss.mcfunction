tag @s add lb_classified
tag @s add lb_miniboss
attribute @s minecraft:generic.max_health base set 360
data merge entity @s {Health:360.0f,DeathLootTable:"last_bastion:entities/rot_titan"}
attribute @s minecraft:generic.movement_speed base set 0.07
attribute @s minecraft:generic.armor base set 18
attribute @s minecraft:generic.armor_toughness base set 6
attribute @s minecraft:generic.knockback_resistance base set 1
attribute @s minecraft:generic.attack_damage base set 12
attribute @s minecraft:generic.scale base set 1.5
execute if score #night lb_apoc matches 91.. run function last_bastion:apocalypse/patriarch
execute unless entity @s[tag=lb_patriarch] run tellraw @a[distance=..64] {"text": "Замечен Гнилотитан! 360 HP, броня, снаряды и гнилое облако. Крайне медленный — разойдитесь и держите дистанцию.", "color": "dark_green"}
