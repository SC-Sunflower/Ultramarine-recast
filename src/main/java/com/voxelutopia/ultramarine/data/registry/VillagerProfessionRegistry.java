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
      Int2ObjectOpenHashMap<ResourceKey<TradeSet>> cookTrades = new Int2ObjectOpenHashMap<>();
      for (int level = 1; level <= 5; level++) {
         cookTrades.put(level, ResourceKey.create(Registries.TRADE_SET,
               Identifier.fromNamespaceAndPath("ultramarine", "cook/" + level)));
      }

      COOK = (VillagerProfession)Registry.register(
         BuiltInRegistries.VILLAGER_PROFESSION,
         Identifier.fromNamespaceAndPath("ultramarine", "cook"),
         new VillagerProfession(
            Component.literal("cook"),
            holder -> holder.is(Objects.requireNonNull(BuiltInRegistries.POINT_OF_INTEREST_TYPE.getKey(PoiTypeRegistry.COOKING_POI))),
            holder -> holder.is(Objects.requireNonNull(BuiltInRegistries.POINT_OF_INTEREST_TYPE.getKey(PoiTypeRegistry.COOKING_POI))),
            ImmutableSet.of(),
            ImmutableSet.of(),
            SoundEvents.VILLAGER_WORK_BUTCHER,
            cookTrades
         )
      );
   }
}
