tag @s add lb_classified
execute if data entity @s CustomName run return 0
execute store result score #roll lb_apoc run random value 1..100
function last_bastion:industrial/bias_roll
execute if score #night lb_apoc matches 1..5 run function last_bastion:apocalypse/stage_1
execute if score #night lb_apoc matches 6..10 run function last_bastion:apocalypse/stage_6
execute if score #night lb_apoc matches 11..15 run function last_bastion:apocalypse/stage_11
execute if score #night lb_apoc matches 16..20 run function last_bastion:apocalypse/stage_16
execute if score #night lb_apoc matches 21..25 run function last_bastion:apocalypse/stage_21
execute if score #night lb_apoc matches 26..30 run function last_bastion:apocalypse/stage_26
execute if score #night lb_apoc matches 31..35 run function last_bastion:apocalypse/stage_31
execute if score #night lb_apoc matches 36..40 run function last_bastion:apocalypse/stage_36
execute if score #night lb_apoc matches 41..45 run function last_bastion:apocalypse/stage_41
execute if score #night lb_apoc matches 46..50 run function last_bastion:apocalypse/stage_46
execute if score #night lb_apoc matches 51..55 run function last_bastion:apocalypse/stage_51
execute if score #night lb_apoc matches 56..60 run function last_bastion:apocalypse/stage_56
execute if score #night lb_apoc matches 61..65 run function last_bastion:apocalypse/stage_61
execute if score #night lb_apoc matches 66..70 run function last_bastion:apocalypse/stage_66
execute if score #night lb_apoc matches 71..75 run function last_bastion:apocalypse/stage_71
execute if score #night lb_apoc matches 76..80 run function last_bastion:apocalypse/stage_76
execute if score #night lb_apoc matches 81..85 run function last_bastion:apocalypse/stage_81
execute if score #night lb_apoc matches 86..90 run function last_bastion:apocalypse/stage_86
execute if score #night lb_apoc matches 91..95 run function last_bastion:apocalypse/stage_91
execute if score #night lb_apoc matches 96..99 run function last_bastion:apocalypse/stage_96
execute if score #night lb_apoc matches 100.. run function last_bastion:apocalypse/stage_100
