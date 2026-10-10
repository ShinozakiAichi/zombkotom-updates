scoreboard players add #ray lb_apoc 1
execute unless block ~ ~ ~ #last_bastion:leap_clear run return 0
execute if entity @p[distance=..1.8,gamemode=!creative,gamemode=!spectator] run scoreboard players set #los lb_apoc 1
execute if score #los lb_apoc matches 0 if score #ray lb_apoc matches ..18 positioned ^ ^ ^0.5 run function last_bastion:apocalypse/ray
