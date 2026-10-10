tag @s add lb_juggernaut
attribute @s minecraft:generic.movement_speed base set 0.210
attribute @s minecraft:generic.max_health base set 80
data merge entity @s {Health:80.0f}
attribute @s minecraft:generic.attack_damage base set 8
attribute @s minecraft:generic.armor base set 10
attribute @s minecraft:generic.knockback_resistance base set 1.0
attribute @s minecraft:generic.scale base set 1.3
item replace entity @s armor.chest with minecraft:iron_chestplate
data merge entity @s {ArmorDropChances:[0.0f,0.0f,0.0f,0.0f]}
attribute @s minecraft:generic.attack_knockback base set 1.2
