attribute @s minecraft:generic.movement_speed base set 0.289
attribute @s minecraft:generic.attack_damage base set 5
execute if score #roll lb_apoc matches 1..14 run function last_bastion:apocalypse/roles/56_runner
execute if score #roll lb_apoc matches 15..22 run function last_bastion:apocalypse/roles/56_tank
execute if score #roll lb_apoc matches 23..31 run function last_bastion:apocalypse/roles/56_jumper
execute if score #roll lb_apoc matches 32..37 run function last_bastion:apocalypse/roles/56_berserker
execute if score #roll lb_apoc matches 38..41 run function last_bastion:apocalypse/roles/56_plague
execute if score #roll lb_apoc matches 42..43 run function last_bastion:apocalypse/roles/56_leader
