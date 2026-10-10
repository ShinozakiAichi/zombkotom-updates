attribute @s minecraft:generic.movement_speed base set 0.324
attribute @s minecraft:generic.attack_damage base set 7
execute if score #roll lb_apoc matches 1..18 run function last_bastion:apocalypse/roles/100_runner
execute if score #roll lb_apoc matches 19..30 run function last_bastion:apocalypse/roles/100_tank
execute if score #roll lb_apoc matches 31..44 run function last_bastion:apocalypse/roles/100_jumper
execute if score #roll lb_apoc matches 45..54 run function last_bastion:apocalypse/roles/100_berserker
execute if score #roll lb_apoc matches 55..62 run function last_bastion:apocalypse/roles/100_plague
execute if score #roll lb_apoc matches 63..66 run function last_bastion:apocalypse/roles/100_leader
execute if score #roll lb_apoc matches 67..70 run function last_bastion:apocalypse/roles/100_juggernaut
