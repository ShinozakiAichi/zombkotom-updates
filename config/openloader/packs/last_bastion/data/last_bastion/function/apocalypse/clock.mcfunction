execute store result score #day lb_apoc run time query day
scoreboard players operation #night lb_apoc = #day lb_apoc
scoreboard players add #night lb_apoc 1
execute unless score #synced lb_apoc = #day lb_apoc run function last_bastion:apocalypse/sync_day
function last_bastion:apocalypse/sync_incontrol with storage last_bastion:clock
execute store result score #time lb_apoc run time query daytime
execute if entity @a if score #time lb_apoc matches 13000..22999 unless score #announced lb_apoc = #night lb_apoc run function last_bastion:apocalypse/announce
