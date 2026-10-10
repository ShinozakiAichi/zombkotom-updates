attribute @s minecraft:generic.movement_speed base set 0.265
attribute @s minecraft:generic.attack_damage base set 4
execute if score #roll lb_apoc matches 1..9 run function last_bastion:apocalypse/roles/26_runner
execute if score #roll lb_apoc matches 10..13 run function last_bastion:apocalypse/roles/26_tank
execute if score #roll lb_apoc matches 14..18 run function last_bastion:apocalypse/roles/26_jumper
execute if score #roll lb_apoc matches 19..21 run function last_bastion:apocalypse/roles/26_berserker
