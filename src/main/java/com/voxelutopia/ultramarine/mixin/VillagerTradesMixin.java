package com.voxelutopia.ultramarine.mixin;

import org.apache.commons.lang3.tuple.Pair;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.npc.VillagerTrades.ItemListing;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

/**
 * 1.21.5+ stores villager/wandering-trader trades in immutable collections
 * (List.of / Map.copyOf), so a mod cannot add trades via put/add anymore.
 * This mixin exposes a static hook to replace both collections with mutable
 * copies built by the mod.
 */
@Mixin(VillagerTrades.class)
public abstract class VillagerTradesMixin {
    @Shadow
    @Final
    @Mutable
    private static Map<ResourceKey<VillagerProfession>, Int2ObjectMap<ItemListing[]>> TRADES;

    @Shadow
    @Final
    @Mutable
    private static List<Pair<ItemListing[], Integer>> WANDERING_TRADER_TRADES;

    public static void ultramarine$replaceVillagerTrades(Map<ResourceKey<VillagerProfession>, Int2ObjectMap<ItemListing[]>> trades) {
        TRADES = trades;
    }

    public static void ultramarine$replaceWanderingTraderTrades(List<Pair<ItemListing[], Integer>> trades) {
        WANDERING_TRADER_TRADES = trades;
    }
}