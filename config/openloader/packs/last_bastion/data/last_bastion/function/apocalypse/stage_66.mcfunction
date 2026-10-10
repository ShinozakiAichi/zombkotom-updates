attribute @s minecraft:generic.movement_speed base set 0.297
attribute @s minecraft:generic.attack_damage base set 6
execute if score #roll lb_apoc matches 1..16 run function last_bastion:apocalypse/roles/66_runner
execute if score #roll lb_apoc matches 17..25 run function last_bastion:apocalypse/roles/66_tank
execute if score #roll lb_apoc matches 26..35 run function last_bastion:apocalypse/roles/66_jumper
execute if score #roll lb_apoc matches 36..42 run function last_bastion:apocalypse/roles/66_berserker
execute if score #roll lb_apoc matches 43..47 run function last_bastion:apocalypse/roles/66_plague
execute if score #roll lb_apoc matches 48..49 run function last_bastion:apocalypse/roles/66_leader
