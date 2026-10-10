attribute @s minecraft:generic.movement_speed base set 0.309
attribute @s minecraft:generic.attack_damage base set 7
execute if score #roll lb_apoc matches 1..18 run function last_bastion:apocalypse/roles/81_runner
execute if score #roll lb_apoc matches 19..28 run function last_bastion:apocalypse/roles/81_tank
execute if score #roll lb_apoc matches 29..40 run function last_bastion:apocalypse/roles/81_jumper
execute if score #roll lb_apoc matches 41..49 run function last_bastion:apocalypse/roles/81_berserker
execute if score #roll lb_apoc matches 50..55 run function last_bastion:apocalypse/roles/81_plague
execute if score #roll lb_apoc matches 56..58 run function last_bastion:apocalypse/roles/81_leader
execute if score #roll lb_apoc matches 59..60 run function last_bastion:apocalypse/roles/81_juggernaut
