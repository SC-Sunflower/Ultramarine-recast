package com.voxelutopia.ultramarine.data.registry;

import com.google.common.collect.ImmutableSet;
import java.util.Objects;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.npc.villager.VillagerProfession;

public class VillagerProfessionRegistry {
   public static VillagerProfession COOK;

   public static void registerModVillagerProfession() {
      COOK = (VillagerProfession)Registry.register(
         BuiltInRegistries.VILLAGER_PROFESSION,
         "cook",
         new VillagerProfession(
            Component.literal("cook"),
            holder -> holder.is(Objects.requireNonNull(BuiltInRegistries.POINT_OF_INTEREST_TYPE.getKey(PoiTypeRegistry.COOKING_POI))),
            holder -> holder.is(Objects.requireNonNull(BuiltInRegistries.POINT_OF_INTEREST_TYPE.getKey(PoiTypeRegistry.COOKING_POI))),
            ImmutableSet.of(),
            ImmutableSet.of(),
            SoundEvents.VILLAGER_WORK_BUTCHER
         )
      );
   }
}
