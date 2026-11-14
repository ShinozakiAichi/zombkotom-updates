attribute @s minecraft:generic.movement_speed base set 0.289
attribute @s minecraft:generic.attack_damage base set 5
execute if score #roll lb_apoc matches 1..17 run function last_bastion:apocalypse/roles/56_runner
execute if score #roll lb_apoc matches 18..28 run function last_bastion:apocalypse/roles/56_tank
execute if score #roll lb_apoc matches 29..40 run function last_bastion:apocalypse/roles/56_jumper
execute if score #roll lb_apoc matches 41..49 run function last_bastion:apocalypse/roles/56_berserker
execute if score #roll lb_apoc matches 50..56 run function last_bastion:apocalypse/roles/56_plague
execute if score #roll lb_apoc matches 57..60 run function last_bastion:apocalypse/roles/56_leader
