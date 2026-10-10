attribute @s minecraft:generic.movement_speed base set 0.293
attribute @s minecraft:generic.attack_damage base set 6
execute if score #roll lb_apoc matches 1..15 run function last_bastion:apocalypse/roles/61_runner
execute if score #roll lb_apoc matches 16..23 run function last_bastion:apocalypse/roles/61_tank
execute if score #roll lb_apoc matches 24..32 run function last_bastion:apocalypse/roles/61_jumper
execute if score #roll lb_apoc matches 33..38 run function last_bastion:apocalypse/roles/61_berserker
execute if score #roll lb_apoc matches 39..43 run function last_bastion:apocalypse/roles/61_plague
execute if score #roll lb_apoc matches 44..45 run function last_bastion:apocalypse/roles/61_leader
