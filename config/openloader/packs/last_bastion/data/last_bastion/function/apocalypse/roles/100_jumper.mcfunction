tag @s add lb_jumper
attribute @s minecraft:generic.movement_speed base set 0.344
attribute @s minecraft:generic.max_health base set 20
data merge entity @s {Health:20.0f}
attribute @s minecraft:generic.attack_damage base set 7
scoreboard players set @s lb_cd 3
item replace entity @s armor.feet with minecraft:chainmail_boots
data merge entity @s {ArmorDropChances:[0.0f,0.0f,0.0f,0.0f]}
