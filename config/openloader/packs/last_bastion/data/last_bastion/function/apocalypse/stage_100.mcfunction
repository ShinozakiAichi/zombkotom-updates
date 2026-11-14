attribute @s minecraft:generic.movement_speed base set 0.324
attribute @s minecraft:generic.attack_damage base set 7
execute if score #roll lb_apoc matches 1..21 run function last_bastion:apocalypse/roles/100_runner
execute if score #roll lb_apoc matches 22..36 run function last_bastion:apocalypse/roles/100_tank
execute if score #roll lb_apoc matches 37..53 run function last_bastion:apocalypse/roles/100_jumper
execute if score #roll lb_apoc matches 54..66 run function last_bastion:apocalypse/roles/100_berserker
execute if score #roll lb_apoc matches 67..76 run function last_bastion:apocalypse/roles/100_plague
execute if score #roll lb_apoc matches 77..82 run function last_bastion:apocalypse/roles/100_leader
execute if score #roll lb_apoc matches 83..88 run function last_bastion:apocalypse/roles/100_juggernaut
