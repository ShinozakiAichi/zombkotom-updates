attribute @s minecraft:generic.movement_speed base set 0.257
attribute @s minecraft:generic.attack_damage base set 3
execute if score #roll lb_apoc matches 1..7 run function last_bastion:apocalypse/roles/16_runner
execute if score #roll lb_apoc matches 8..10 run function last_bastion:apocalypse/roles/16_tank
execute if score #roll lb_apoc matches 11..14 run function last_bastion:apocalypse/roles/16_jumper
