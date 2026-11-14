attribute @s minecraft:generic.movement_speed base set 0.313
attribute @s minecraft:generic.attack_damage base set 7
execute if score #roll lb_apoc matches 1..21 run function last_bastion:apocalypse/roles/86_runner
execute if score #roll lb_apoc matches 22..35 run function last_bastion:apocalypse/roles/86_tank
execute if score #roll lb_apoc matches 36..50 run function last_bastion:apocalypse/roles/86_jumper
execute if score #roll lb_apoc matches 51..62 run function last_bastion:apocalypse/roles/86_berserker
execute if score #roll lb_apoc matches 63..72 run function last_bastion:apocalypse/roles/86_plague
execute if score #roll lb_apoc matches 73..78 run function last_bastion:apocalypse/roles/86_leader
execute if score #roll lb_apoc matches 79..83 run function last_bastion:apocalypse/roles/86_juggernaut
