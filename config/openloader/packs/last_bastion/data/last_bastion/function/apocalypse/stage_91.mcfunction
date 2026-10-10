attribute @s minecraft:generic.movement_speed base set 0.317
attribute @s minecraft:generic.attack_damage base set 7
execute if score #roll lb_apoc matches 1..18 run function last_bastion:apocalypse/roles/91_runner
execute if score #roll lb_apoc matches 19..29 run function last_bastion:apocalypse/roles/91_tank
execute if score #roll lb_apoc matches 30..42 run function last_bastion:apocalypse/roles/91_jumper
execute if score #roll lb_apoc matches 43..52 run function last_bastion:apocalypse/roles/91_berserker
execute if score #roll lb_apoc matches 53..59 run function last_bastion:apocalypse/roles/91_plague
execute if score #roll lb_apoc matches 60..63 run function last_bastion:apocalypse/roles/91_leader
execute if score #roll lb_apoc matches 64..66 run function last_bastion:apocalypse/roles/91_juggernaut
