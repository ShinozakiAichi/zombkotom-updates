scoreboard objectives add lb_apoc dummy
scoreboard objectives add lb_cd dummy
execute unless score #announced lb_apoc matches -2147483648..2147483647 run scoreboard players set #announced lb_apoc -1
scoreboard players set #pulse lb_apoc 0
scoreboard players set #four lb_apoc 4
execute in minecraft:overworld run function last_bastion:apocalypse/clock
