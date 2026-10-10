attribute @s minecraft:generic.movement_speed base set 0.301
attribute @s minecraft:generic.attack_damage base set 6
execute if score #roll lb_apoc matches 1..16 run function last_bastion:apocalypse/roles/71_runner
execute if score #roll lb_apoc matches 17..25 run function last_bastion:apocalypse/roles/71_tank
execute if score #roll lb_apoc matches 26..35 run function last_bastion:apocalypse/roles/71_jumper
execute if score #roll lb_apoc matches 36..43 run function last_bastion:apocalypse/roles/71_berserker
execute if score #roll lb_apoc matches 44..48 run function last_bastion:apocalypse/roles/71_plague
execute if score #roll lb_apoc matches 49..51 run function last_bastion:apocalypse/roles/71_leader
