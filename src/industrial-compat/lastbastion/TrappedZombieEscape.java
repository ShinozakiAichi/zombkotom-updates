package lastbastion;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.vehicle.*;
import net.minecraft.world.level.GameRules;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/** A trapped zombie attacks only its own immediate boat/cart; never scans for parked vehicles. */
public final class TrappedZombieEscape {
    private static final TagKey<EntityType<?>> ZOMBIES=TagKey.create(Registries.ENTITY_TYPE,ResourceLocation.fromNamespaceAndPath("last_bastion","zombies"));
    private static final TagKey<EntityType<?>> ESCAPERS=TagKey.create(Registries.ENTITY_TYPE,ResourceLocation.fromNamespaceAndPath("lastbastion_compat","vehicle_escapers"));
    public static void tick(EntityTickEvent.Post event) {
        var vehicle=event.getEntity();
        if(!(vehicle instanceof Boat || vehicle instanceof AbstractMinecart) || vehicle.level().isClientSide || vehicle.isRemoved() || vehicle.tickCount%20!=0)return;
        if(!vehicle.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING))return;
        // NeoForge fires tick events for the root vehicle, not rideTick passengers.
        // Inspect only this vehicle's short passenger list; do not scan the level.
        for(var passenger:vehicle.getPassengers()) {
            if(!(passenger instanceof Mob mob) || !mob.isAlive() || mob instanceof ZombifiedPiglin)continue;
            if(!(mob instanceof Zombie || mob.getType().is(ZOMBIES) || mob.getType().is(ESCAPERS)))continue;
            mob.swing(InteractionHand.MAIN_HAND);
            // Native hurt/destroy handles drops, chest inventories and passenger release.
            vehicle.hurt(mob.damageSources().mobAttack(mob),3.0F);
            return;
        }
    }
}
