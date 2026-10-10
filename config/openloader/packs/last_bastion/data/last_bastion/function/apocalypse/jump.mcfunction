scoreboard players set @s lb_cd 6
kill @e[type=minecraft:marker,tag=lb_vector]
execute facing entity @p[distance=..8,gamemode=!creative,gamemode=!spectator] feet rotated ~ 0 positioned ^ ^ ^0.75 run summon minecraft:marker ~ ~ ~ {Tags:["lb_vector"]}
execute store result score #x lb_apoc run data get entity @e[type=minecraft:marker,tag=lb_vector,limit=1] Pos[0] 1000
execute store result score #z lb_apoc run data get entity @e[type=minecraft:marker,tag=lb_vector,limit=1] Pos[2] 1000
execute store result score #sx lb_apoc run data get entity @s Pos[0] 1000
execute store result score #sz lb_apoc run data get entity @s Pos[2] 1000
scoreboard players operation #x lb_apoc -= #sx lb_apoc
scoreboard players operation #z lb_apoc -= #sz lb_apoc
execute store result entity @s Motion[0] double 0.001 run scoreboard players get #x lb_apoc
execute store result entity @s Motion[2] double 0.001 run scoreboard players get #z lb_apoc
data modify entity @s Motion[1] set value 0.65d
kill @e[type=minecraft:marker,tag=lb_vector]
particle minecraft:cloud ~ ~0.2 ~ 0.2 0.1 0.2 0.01 5 normal
