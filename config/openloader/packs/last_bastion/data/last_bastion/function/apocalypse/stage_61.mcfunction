attribute @s minecraft:generic.movement_speed base set 0.293
attribute @s minecraft:generic.attack_damage base set 6
execute if score #roll lb_apoc matches 1..19 run function last_bastion:apocalypse/roles/61_runner
execute if score #roll lb_apoc matches 20..30 run function last_bastion:apocalypse/roles/61_tank
execute if score #roll lb_apoc matches 31..42 run function last_bastion:apocalypse/roles/61_jumper
execute if score #roll lb_apoc matches 43..51 run function last_bastion:apocalypse/roles/61_berserker
execute if score #roll lb_apoc matches 52..59 run function last_bastion:apocalypse/roles/61_plague
execute if score #roll lb_apoc matches 60..64 run function last_bastion:apocalypse/roles/61_leader
