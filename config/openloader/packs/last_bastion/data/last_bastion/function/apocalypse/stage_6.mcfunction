attribute @s minecraft:generic.movement_speed base set 0.249
attribute @s minecraft:generic.attack_damage base set 3
execute if score #roll lb_apoc matches 1..6 run function last_bastion:apocalypse/roles/6_runner
