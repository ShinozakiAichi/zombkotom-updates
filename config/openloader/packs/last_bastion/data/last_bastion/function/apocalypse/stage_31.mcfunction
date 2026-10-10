attribute @s minecraft:generic.movement_speed base set 0.269
attribute @s minecraft:generic.attack_damage base set 4
execute if score #roll lb_apoc matches 1..10 run function last_bastion:apocalypse/roles/31_runner
execute if score #roll lb_apoc matches 11..15 run function last_bastion:apocalypse/roles/31_tank
execute if score #roll lb_apoc matches 16..20 run function last_bastion:apocalypse/roles/31_jumper
execute if score #roll lb_apoc matches 21..23 run function last_bastion:apocalypse/roles/31_berserker
