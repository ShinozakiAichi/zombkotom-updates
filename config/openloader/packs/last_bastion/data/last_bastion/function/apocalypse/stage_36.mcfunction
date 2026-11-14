attribute @s minecraft:generic.movement_speed base set 0.273
attribute @s minecraft:generic.attack_damage base set 4
execute if score #roll lb_apoc matches 1..12 run function last_bastion:apocalypse/roles/36_runner
execute if score #roll lb_apoc matches 13..18 run function last_bastion:apocalypse/roles/36_tank
execute if score #roll lb_apoc matches 19..25 run function last_bastion:apocalypse/roles/36_jumper
execute if score #roll lb_apoc matches 26..30 run function last_bastion:apocalypse/roles/36_berserker
execute if score #roll lb_apoc matches 31..34 run function last_bastion:apocalypse/roles/36_plague
