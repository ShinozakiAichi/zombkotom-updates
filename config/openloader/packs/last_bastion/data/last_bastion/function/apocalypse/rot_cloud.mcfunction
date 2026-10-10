effect clear @s minecraft:speed
particle minecraft:spore_blossom_air ~ ~1.5 ~ 1.4 1.2 1.4 0 10 normal
effect give @a[distance=..3,gamemode=!creative,gamemode=!spectator] minecraft:poison 4 0 true
execute if entity @s[tag=lb_patriarch] run effect give @a[distance=..4,gamemode=!creative,gamemode=!spectator] minecraft:weakness 4 0 true
