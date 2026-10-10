attribute @s minecraft:generic.movement_speed base set 0.277
attribute @s minecraft:generic.attack_damage base set 5
execute if score #roll lb_apoc matches 1..11 run function last_bastion:apocalypse/roles/41_runner
execute if score #roll lb_apoc matches 12..17 run function last_bastion:apocalypse/roles/41_tank
execute if score #roll lb_apoc matches 18..24 run function last_bastion:apocalypse/roles/41_jumper
execute if score #roll lb_apoc matches 25..28 run function last_bastion:apocalypse/roles/41_berserker
execute if score #roll lb_apoc matches 29..31 run function last_bastion:apocalypse/roles/41_plague
