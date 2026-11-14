attribute @s minecraft:generic.movement_speed base set 0.269
attribute @s minecraft:generic.attack_damage base set 4
execute if score #roll lb_apoc matches 1..11 run function last_bastion:apocalypse/roles/31_runner
execute if score #roll lb_apoc matches 12..16 run function last_bastion:apocalypse/roles/31_tank
execute if score #roll lb_apoc matches 17..21 run function last_bastion:apocalypse/roles/31_jumper
execute if score #roll lb_apoc matches 22..24 run function last_bastion:apocalypse/roles/31_berserker
