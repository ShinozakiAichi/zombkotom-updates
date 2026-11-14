attribute @s minecraft:generic.movement_speed base set 0.281
attribute @s minecraft:generic.attack_damage base set 5
execute if score #roll lb_apoc matches 1..15 run function last_bastion:apocalypse/roles/46_runner
execute if score #roll lb_apoc matches 16..24 run function last_bastion:apocalypse/roles/46_tank
execute if score #roll lb_apoc matches 25..34 run function last_bastion:apocalypse/roles/46_jumper
execute if score #roll lb_apoc matches 35..41 run function last_bastion:apocalypse/roles/46_berserker
execute if score #roll lb_apoc matches 42..46 run function last_bastion:apocalypse/roles/46_plague
