attribute @s minecraft:generic.movement_speed base set 0.253
attribute @s minecraft:generic.attack_damage base set 3
execute if score #roll lb_apoc matches 1..6 run function last_bastion:apocalypse/roles/11_runner
execute if score #roll lb_apoc matches 7..9 run function last_bastion:apocalypse/roles/11_tank
