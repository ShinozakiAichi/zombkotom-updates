attribute @s minecraft:generic.movement_speed base set 0.281
attribute @s minecraft:generic.attack_damage base set 5
execute if score #roll lb_apoc matches 1..12 run function last_bastion:apocalypse/roles/46_runner
execute if score #roll lb_apoc matches 13..18 run function last_bastion:apocalypse/roles/46_tank
execute if score #roll lb_apoc matches 19..25 run function last_bastion:apocalypse/roles/46_jumper
execute if score #roll lb_apoc matches 26..30 run function last_bastion:apocalypse/roles/46_berserker
execute if score #roll lb_apoc matches 31..33 run function last_bastion:apocalypse/roles/46_plague
