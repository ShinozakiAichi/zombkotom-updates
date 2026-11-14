attribute @s minecraft:generic.movement_speed base set 0.309
attribute @s minecraft:generic.attack_damage base set 7
execute if score #roll lb_apoc matches 1..21 run function last_bastion:apocalypse/roles/81_runner
execute if score #roll lb_apoc matches 22..34 run function last_bastion:apocalypse/roles/81_tank
execute if score #roll lb_apoc matches 35..49 run function last_bastion:apocalypse/roles/81_jumper
execute if score #roll lb_apoc matches 50..61 run function last_bastion:apocalypse/roles/81_berserker
execute if score #roll lb_apoc matches 62..70 run function last_bastion:apocalypse/roles/81_plague
execute if score #roll lb_apoc matches 71..76 run function last_bastion:apocalypse/roles/81_leader
execute if score #roll lb_apoc matches 77..80 run function last_bastion:apocalypse/roles/81_juggernaut
