scoreboard players set #los lb_apoc 0
scoreboard players set #ray lb_apoc 0
execute anchored eyes facing entity @p[distance=..8,gamemode=!creative,gamemode=!spectator] eyes positioned ^ ^ ^ run function last_bastion:apocalypse/ray
execute if score #los lb_apoc matches 1 run function last_bastion:apocalypse/jump
