attribute @s minecraft:generic.movement_speed base set 0.313
attribute @s minecraft:generic.attack_damage base set 7
execute if score #roll lb_apoc matches 1..18 run function last_bastion:apocalypse/roles/86_runner
execute if score #roll lb_apoc matches 19..29 run function last_bastion:apocalypse/roles/86_tank
execute if score #roll lb_apoc matches 30..41 run function last_bastion:apocalypse/roles/86_jumper
execute if score #roll lb_apoc matches 42..50 run function last_bastion:apocalypse/roles/86_berserker
execute if score #roll lb_apoc matches 51..57 run function last_bastion:apocalypse/roles/86_plague
execute if score #roll lb_apoc matches 58..60 run function last_bastion:apocalypse/roles/86_leader
execute if score #roll lb_apoc matches 61..62 run function last_bastion:apocalypse/roles/86_juggernaut
