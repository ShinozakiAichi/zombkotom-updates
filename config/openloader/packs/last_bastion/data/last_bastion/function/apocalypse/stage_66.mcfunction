attribute @s minecraft:generic.movement_speed base set 0.297
attribute @s minecraft:generic.attack_damage base set 6
execute if score #roll lb_apoc matches 1..20 run function last_bastion:apocalypse/roles/66_runner
execute if score #roll lb_apoc matches 21..33 run function last_bastion:apocalypse/roles/66_tank
execute if score #roll lb_apoc matches 34..46 run function last_bastion:apocalypse/roles/66_jumper
execute if score #roll lb_apoc matches 47..56 run function last_bastion:apocalypse/roles/66_berserker
execute if score #roll lb_apoc matches 57..64 run function last_bastion:apocalypse/roles/66_plague
execute if score #roll lb_apoc matches 65..69 run function last_bastion:apocalypse/roles/66_leader
