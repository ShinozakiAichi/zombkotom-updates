# World calendar only; does not assert presence during a night or a survived horde.
execute if score #day lb_apoc matches 10.. run advancement grant @a[gamemode=!spectator,advancements={last_bastion:campaign/night_10=false}] only last_bastion:campaign/night_10
execute if score #day lb_apoc matches 25.. run advancement grant @a[gamemode=!spectator,advancements={last_bastion:campaign/night_25=false}] only last_bastion:campaign/night_25
execute if score #day lb_apoc matches 40.. run advancement grant @a[gamemode=!spectator,advancements={last_bastion:campaign/night_40=false}] only last_bastion:campaign/night_40
execute if score #day lb_apoc matches 70.. run advancement grant @a[gamemode=!spectator,advancements={last_bastion:campaign/night_70=false}] only last_bastion:campaign/night_70
execute if score #day lb_apoc matches 100.. run advancement grant @a[gamemode=!spectator,advancements={last_bastion:campaign/night_100=false}] only last_bastion:campaign/night_100
