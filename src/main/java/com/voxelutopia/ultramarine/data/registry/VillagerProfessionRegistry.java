package com.voxelutopia.ultramarine.data.registry;

import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Objects;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.trading.TradeSet;

public class VillagerProfessionRegistry {
   public static VillagerProfession COOK;

   public static void registerModVillagerProfession() {
      // Minecraft 26.1+ made villager trades data-driven: each profession level maps to a
      // TradeSet resource key, whose data lives in data/ultramarine/trade_set/cook/level_<n>.json
      // and data/ultramarine/villager_trade/cook/<n>/*.json. An empty map leaves the cook with
      // no offers, and 26.1+ villagers refuse to open the trading screen when they have none.
      Int2ObjectOpenHashMap<ResourceKey<TradeSet>> tradeSetsByLevel = new Int2ObjectOpenHashMap<>();
      for (int level = 1; level <= 5; level++) {
         tradeSetsByLevel.put(
            level,
            ResourceKey.create(Registries.TRADE_SET, Identifier.fromNamespaceAndPath("ultramarine", "cook/level_" + level))
         );
      }
      COOK = (VillagerProfession)Registry.register(
         BuiltInRegistries.VILLAGER_PROFESSION,
         Identifier.fromNamespaceAndPath("ultramarine", "cook"),
         new VillagerProfession(
            Component.translatable("entity.ultramarine.villager.cook"),
            holder -> holder.is(Objects.requireNonNull(BuiltInRegistries.POINT_OF_INTEREST_TYPE.getKey(PoiTypeRegistry.COOKING_POI))),
            holder -> holder.is(Objects.requireNonNull(BuiltInRegistries.POINT_OF_INTEREST_TYPE.getKey(PoiTypeRegistry.COOKING_POI))),
            ImmutableSet.of(),
            ImmutableSet.of(),
            SoundEvents.VILLAGER_WORK_BUTCHER,
            tradeSetsByLevel
         )
      );
   }
}
