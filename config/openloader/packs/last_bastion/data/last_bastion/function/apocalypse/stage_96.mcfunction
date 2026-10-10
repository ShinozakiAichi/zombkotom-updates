attribute @s minecraft:generic.movement_speed base set 0.321
attribute @s minecraft:generic.attack_damage base set 7
execute if score #roll lb_apoc matches 1..18 run function last_bastion:apocalypse/roles/96_runner
execute if score #roll lb_apoc matches 19..30 run function last_bastion:apocalypse/roles/96_tank
execute if score #roll lb_apoc matches 31..44 run function last_bastion:apocalypse/roles/96_jumper
execute if score #roll lb_apoc matches 45..54 run function last_bastion:apocalypse/roles/96_berserker
execute if score #roll lb_apoc matches 55..62 run function last_bastion:apocalypse/roles/96_plague
execute if score #roll lb_apoc matches 63..66 run function last_bastion:apocalypse/roles/96_leader
execute if score #roll lb_apoc matches 67..69 run function last_bastion:apocalypse/roles/96_juggernaut
