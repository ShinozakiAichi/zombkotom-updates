tag @s add lb_leader
attribute @s minecraft:generic.movement_speed base set 0.273
attribute @s minecraft:generic.max_health base set 36
data merge entity @s {Health:36.0f}
attribute @s minecraft:generic.attack_damage base set 6
attribute @s minecraft:generic.armor base set 4
item replace entity @s armor.head with minecraft:golden_helmet
data merge entity @s {ArmorDropChances:[0.0f,0.0f,0.0f,0.0f]}
