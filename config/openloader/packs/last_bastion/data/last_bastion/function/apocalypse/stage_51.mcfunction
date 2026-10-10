attribute @s minecraft:generic.movement_speed base set 0.285
attribute @s minecraft:generic.attack_damage base set 5
execute if score #roll lb_apoc matches 1..13 run function last_bastion:apocalypse/roles/51_runner
execute if score #roll lb_apoc matches 14..20 run function last_bastion:apocalypse/roles/51_tank
execute if score #roll lb_apoc matches 21..28 run function last_bastion:apocalypse/roles/51_jumper
execute if score #roll lb_apoc matches 29..33 run function last_bastion:apocalypse/roles/51_berserker
execute if score #roll lb_apoc matches 34..37 run function last_bastion:apocalypse/roles/51_plague
execute if score #roll lb_apoc matches 38..39 run function last_bastion:apocalypse/roles/51_leader
