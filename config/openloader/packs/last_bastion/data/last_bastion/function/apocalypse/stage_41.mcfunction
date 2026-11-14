attribute @s minecraft:generic.movement_speed base set 0.277
attribute @s minecraft:generic.attack_damage base set 5
execute if score #roll lb_apoc matches 1..13 run function last_bastion:apocalypse/roles/41_runner
execute if score #roll lb_apoc matches 14..21 run function last_bastion:apocalypse/roles/41_tank
execute if score #roll lb_apoc matches 22..30 run function last_bastion:apocalypse/roles/41_jumper
execute if score #roll lb_apoc matches 31..36 run function last_bastion:apocalypse/roles/41_berserker
execute if score #roll lb_apoc matches 37..40 run function last_bastion:apocalypse/roles/41_plague
