attribute @s minecraft:generic.movement_speed base set 0.273
attribute @s minecraft:generic.attack_damage base set 4
execute if score #roll lb_apoc matches 1..11 run function last_bastion:apocalypse/roles/36_runner
execute if score #roll lb_apoc matches 12..16 run function last_bastion:apocalypse/roles/36_tank
execute if score #roll lb_apoc matches 17..22 run function last_bastion:apocalypse/roles/36_jumper
execute if score #roll lb_apoc matches 23..26 run function last_bastion:apocalypse/roles/36_berserker
execute if score #roll lb_apoc matches 27..29 run function last_bastion:apocalypse/roles/36_plague
