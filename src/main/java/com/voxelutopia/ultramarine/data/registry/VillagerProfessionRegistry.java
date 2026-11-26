package com.voxelutopia.ultramarine.data.registry;

import com.google.common.collect.ImmutableSet;
import java.util.Objects;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.npc.VillagerProfession;

public class VillagerProfessionRegistry {
   public static VillagerProfession COOK;

   public static void registerModVillagerProfession() {
      COOK = (VillagerProfession)Registry.register(
         BuiltInRegistries.VILLAGER_PROFESSION,
         ResourceLocation.fromNamespaceAndPath("ultramarine", "cook"),
         new VillagerProfession(
            "cook",
            holder -> holder.is(Objects.requireNonNull(BuiltInRegistries.POINT_OF_INTEREST_TYPE.getKey(PoiTypeRegistry.COOKING_POI))),
            holder -> holder.is(Objects.requireNonNull(BuiltInRegistries.POINT_OF_INTEREST_TYPE.getKey(PoiTypeRegistry.COOKING_POI))),
            ImmutableSet.of(),
            ImmutableSet.of(),
            SoundEvents.VILLAGER_WORK_BUTCHER
         )
      );
   }
}
