attribute @s minecraft:generic.movement_speed base set 0.305
attribute @s minecraft:generic.attack_damage base set 6
execute if score #roll lb_apoc matches 1..17 run function last_bastion:apocalypse/roles/76_runner
execute if score #roll lb_apoc matches 18..27 run function last_bastion:apocalypse/roles/76_tank
execute if score #roll lb_apoc matches 28..38 run function last_bastion:apocalypse/roles/76_jumper
execute if score #roll lb_apoc matches 39..46 run function last_bastion:apocalypse/roles/76_berserker
execute if score #roll lb_apoc matches 47..52 run function last_bastion:apocalypse/roles/76_plague
execute if score #roll lb_apoc matches 53..55 run function last_bastion:apocalypse/roles/76_leader
execute if score #roll lb_apoc matches 56..57 run function last_bastion:apocalypse/roles/76_juggernaut
