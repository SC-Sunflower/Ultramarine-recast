package com.voxelutopia.ultramarine.world.worldgen;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class ModBiomeModifiers {
   public static void register() {
      addOreFeature(ModPlacedFeatures.OVERWORLD_JADE_ORE_PLACED_KEY, BiomeTags.IS_OVERWORLD, Decoration.UNDERGROUND_ORES);
      addOreFeature(ModPlacedFeatures.OVERWORLD_MAGNESITE_ORE_PLACED_KEY, BiomeTags.IS_OVERWORLD, Decoration.UNDERGROUND_ORES);
      addOreFeature(ModPlacedFeatures.OVERWORLD_HEMATITE_ORE_PLACED_KEY, BiomeTags.IS_OVERWORLD, Decoration.UNDERGROUND_ORES);
      addOreFeature(ModPlacedFeatures.NETHER_COBALT_ORE_PLACED_KEY, BiomeTags.IS_NETHER, Decoration.UNDERGROUND_ORES);
   }

   private static void addOreFeature(ResourceKey<PlacedFeature> featureKey, TagKey<Biome> biomeTag, Decoration step) {
      BiomeModifications.addFeature(context -> context.hasTag(biomeTag), step, featureKey);
   }

   public static ResourceKey<PlacedFeature> key(String name) {
      return ResourceKey.create(Registries.PLACED_FEATURE, ResourceLocation.fromNamespaceAndPath("ultramarine", name));
   }
}
