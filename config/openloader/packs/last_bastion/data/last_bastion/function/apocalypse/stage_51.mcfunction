attribute @s minecraft:generic.movement_speed base set 0.285
attribute @s minecraft:generic.attack_damage base set 5
execute if score #roll lb_apoc matches 1..16 run function last_bastion:apocalypse/roles/51_runner
execute if score #roll lb_apoc matches 17..26 run function last_bastion:apocalypse/roles/51_tank
execute if score #roll lb_apoc matches 27..37 run function last_bastion:apocalypse/roles/51_jumper
execute if score #roll lb_apoc matches 38..44 run function last_bastion:apocalypse/roles/51_berserker
execute if score #roll lb_apoc matches 45..50 run function last_bastion:apocalypse/roles/51_plague
execute if score #roll lb_apoc matches 51..54 run function last_bastion:apocalypse/roles/51_leader
