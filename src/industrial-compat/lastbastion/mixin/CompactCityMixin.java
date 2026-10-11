package lastbastion.mixin;

import lastbastion.CompactCityGrid;
import mcjty.lostcities.config.LostCityProfile;
import mcjty.lostcities.varia.ChunkCoord;
import mcjty.lostcities.worldgen.IDimensionInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** The negative-chance legacy Perlin mode remains untouched until profile update. */
@Mixin(targets="mcjty.lostcities.worldgen.lost.City",remap=false)
public class CompactCityMixin {
    @Unique
    private static boolean compact(LostCityProfile p) {
        return "last_bastion".equals(p.getName()) && p.CITY_CHANCE >= 0;
    }
    @Inject(method="isCityCenter",at=@At("HEAD"),cancellable=true,require=1)
    private static void lastbastion$separatedCenters(ChunkCoord c,IDimensionInfo info,CallbackInfoReturnable<Boolean> cir) {
        if(compact(info.getProfile())) cir.setReturnValue(CompactCityGrid.isCenter(c.chunkX(),c.chunkZ(),info.getSeed()));
    }
    @Inject(method="getCityRadius",at=@At("RETURN"),cancellable=true,require=1)
    private static void lastbastion$capRadius(ChunkCoord c,IDimensionInfo info,CallbackInfoReturnable<Float> cir) {
        if(compact(info.getProfile())) cir.setReturnValue(Math.min(cir.getReturnValue(),(float)CompactCityGrid.MAX_RADIUS_BLOCKS));
    }
    @Inject(method="getCityFactor",at=@At("RETURN"),cancellable=true,require=1)
    private static void lastbastion$boundedFootprint(ChunkCoord c,IDimensionInfo info,LostCityProfile profile,CallbackInfoReturnable<Float> cir) {
        if(compact(profile) && !CompactCityGrid.withinLimit(c.chunkX(),c.chunkZ(),info.getSeed())) cir.setReturnValue(0F);
    }
}
