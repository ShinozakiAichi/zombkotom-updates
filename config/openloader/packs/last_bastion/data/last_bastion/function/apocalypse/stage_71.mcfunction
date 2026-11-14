attribute @s minecraft:generic.movement_speed base set 0.301
attribute @s minecraft:generic.attack_damage base set 6
execute if score #roll lb_apoc matches 1..20 run function last_bastion:apocalypse/roles/71_runner
execute if score #roll lb_apoc matches 21..33 run function last_bastion:apocalypse/roles/71_tank
execute if score #roll lb_apoc matches 34..47 run function last_bastion:apocalypse/roles/71_jumper
execute if score #roll lb_apoc matches 48..59 run function last_bastion:apocalypse/roles/71_berserker
execute if score #roll lb_apoc matches 60..67 run function last_bastion:apocalypse/roles/71_plague
execute if score #roll lb_apoc matches 68..73 run function last_bastion:apocalypse/roles/71_leader
