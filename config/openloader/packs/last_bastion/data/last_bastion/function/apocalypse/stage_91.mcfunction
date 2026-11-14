attribute @s minecraft:generic.movement_speed base set 0.317
attribute @s minecraft:generic.attack_damage base set 7
execute if score #roll lb_apoc matches 1..21 run function last_bastion:apocalypse/roles/91_runner
execute if score #roll lb_apoc matches 22..35 run function last_bastion:apocalypse/roles/91_tank
execute if score #roll lb_apoc matches 36..51 run function last_bastion:apocalypse/roles/91_jumper
execute if score #roll lb_apoc matches 52..64 run function last_bastion:apocalypse/roles/91_berserker
execute if score #roll lb_apoc matches 65..74 run function last_bastion:apocalypse/roles/91_plague
execute if score #roll lb_apoc matches 75..80 run function last_bastion:apocalypse/roles/91_leader
execute if score #roll lb_apoc matches 81..85 run function last_bastion:apocalypse/roles/91_juggernaut
