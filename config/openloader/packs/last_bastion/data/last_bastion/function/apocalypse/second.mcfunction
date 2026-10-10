scoreboard players set #pulse lb_apoc 0
function last_bastion:apocalypse/clock
execute in minecraft:overworld run function last_bastion:apocalypse/entities
execute in minecraft:the_nether run function last_bastion:apocalypse/entities
execute in minecraft:the_end run function last_bastion:apocalypse/entities
execute in lostcities:lostcity run function last_bastion:apocalypse/entities
