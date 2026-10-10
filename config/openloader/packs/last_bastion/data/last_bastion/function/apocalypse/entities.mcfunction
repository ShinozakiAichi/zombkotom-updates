execute as @e[type=undead_revamp2:theheavy,tag=!lb_classified,limit=4] at @s run function last_bastion:apocalypse/miniboss
execute as @e[tag=lb_miniboss] at @s run function last_bastion:apocalypse/rot_cloud
execute as @e[type=#last_bastion:zombies,tag=!lb_classified,limit=64] at @s run function last_bastion:apocalypse/classify
scoreboard players remove @e[tag=lb_jumper,scores={lb_cd=1..}] lb_cd 1
execute as @e[tag=lb_jumper,scores={lb_cd=..0},nbt={OnGround:1b}] at @s if entity @p[distance=2..8,gamemode=!creative,gamemode=!spectator] run function last_bastion:apocalypse/try_jump
execute as @e[tag=lb_berserker,tag=!lb_enraged] store result score @s lb_apoc run data get entity @s Health 1
execute as @e[tag=lb_berserker,tag=!lb_enraged,scores={lb_apoc=..14}] at @s run function last_bastion:apocalypse/enrage
execute as @e[tag=lb_plague] at @s run function last_bastion:apocalypse/plague
execute as @e[tag=lb_leader] at @s run function last_bastion:apocalypse/command
