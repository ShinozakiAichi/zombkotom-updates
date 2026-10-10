tag @s add lb_tank
attribute @s minecraft:generic.movement_speed base set 0.262
attribute @s minecraft:generic.max_health base set 60
data merge entity @s {Health:60.0f}
attribute @s minecraft:generic.attack_damage base set 9
attribute @s minecraft:generic.armor base set 6
attribute @s minecraft:generic.knockback_resistance base set 0.8
attribute @s minecraft:generic.scale base set 1.15
item replace entity @s armor.chest with minecraft:iron_chestplate
data merge entity @s {ArmorDropChances:[0.0f,0.0f,0.0f,0.0f]}
