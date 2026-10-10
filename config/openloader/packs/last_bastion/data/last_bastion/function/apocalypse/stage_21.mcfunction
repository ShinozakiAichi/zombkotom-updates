attribute @s minecraft:generic.movement_speed base set 0.261
attribute @s minecraft:generic.attack_damage base set 4
execute if score #roll lb_apoc matches 1..8 run function last_bastion:apocalypse/roles/21_runner
execute if score #roll lb_apoc matches 9..12 run function last_bastion:apocalypse/roles/21_tank
execute if score #roll lb_apoc matches 13..16 run function last_bastion:apocalypse/roles/21_jumper
