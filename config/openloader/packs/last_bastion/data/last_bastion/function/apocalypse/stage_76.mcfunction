attribute @s minecraft:generic.movement_speed base set 0.305
attribute @s minecraft:generic.attack_damage base set 6
execute if score #roll lb_apoc matches 1..20 run function last_bastion:apocalypse/roles/76_runner
execute if score #roll lb_apoc matches 21..33 run function last_bastion:apocalypse/roles/76_tank
execute if score #roll lb_apoc matches 34..47 run function last_bastion:apocalypse/roles/76_jumper
execute if score #roll lb_apoc matches 48..58 run function last_bastion:apocalypse/roles/76_berserker
execute if score #roll lb_apoc matches 59..67 run function last_bastion:apocalypse/roles/76_plague
execute if score #roll lb_apoc matches 68..73 run function last_bastion:apocalypse/roles/76_leader
execute if score #roll lb_apoc matches 74..77 run function last_bastion:apocalypse/roles/76_juggernaut
