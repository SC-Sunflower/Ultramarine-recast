package com.voxelutopia.ultramarine.world.worldgen;

import com.voxelutopia.ultramarine.data.registry.BlockRegistry;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.BlockReplacement;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.OreFeature;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;

/**
 * Minecraft 26.3 removed ConfiguredFeature: a Feature now carries its own configuration
 * and is registered directly in {@code Registries.FEATURE}.
 */
public class ModConfiguredFeatures {
   public static final ResourceKey<Feature> OVERWORLD_JADE_ORE_KEY = key("overworld_jade_ore");
   public static final ResourceKey<Feature> OVERWORLD_MAGNESITE_ORE_KEY = key("overworld_magnesite_ore");
   public static final ResourceKey<Feature> OVERWORLD_HEMATITE_ORE_KEY = key("overworld_hematite_ore");
   public static final ResourceKey<Feature> NETHER_COBALT_ORE_KEY = key("nether_cobalt_ore");
   private static final RuleTest STONE_ORE_REPLACEABLES = new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES);
   private static final RuleTest DEEPSLATE_ORE_REPLACEABLES = new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES);
   private static final RuleTest NETHERRACK = new BlockMatchTest(Blocks.NETHERRACK);

   public static void bootstrap(BootstrapContext<Feature> context) {
      register(context, OVERWORLD_JADE_ORE_KEY, new OreFeature(List.of(
         new BlockReplacement(STONE_ORE_REPLACEABLES, BlockRegistry.JADE_ORE.defaultBlockState()),
         new BlockReplacement(DEEPSLATE_ORE_REPLACEABLES, BlockRegistry.DEEPSLATE_JADE_ORE.defaultBlockState())
      ), 4));
      register(context, OVERWORLD_MAGNESITE_ORE_KEY, new OreFeature(List.of(
         new BlockReplacement(STONE_ORE_REPLACEABLES, BlockRegistry.MAGNESITE_ORE.defaultBlockState()),
         new BlockReplacement(DEEPSLATE_ORE_REPLACEABLES, BlockRegistry.DEEPSLATE_MAGNESITE_ORE.defaultBlockState())
      ), 7));
      register(context, OVERWORLD_HEMATITE_ORE_KEY, new OreFeature(List.of(
         new BlockReplacement(STONE_ORE_REPLACEABLES, BlockRegistry.HEMATITE_ORE.defaultBlockState()),
         new BlockReplacement(DEEPSLATE_ORE_REPLACEABLES, BlockRegistry.DEEPSLATE_HEMATITE_ORE.defaultBlockState())
      ), 9));
      register(context, NETHER_COBALT_ORE_KEY, new OreFeature(List.of(
         new BlockReplacement(NETHERRACK, BlockRegistry.NETHER_COBALT_ORE.defaultBlockState())
      ), 6));
   }

   public static ResourceKey<Feature> key(String name) {
      return ResourceKey.create(Registries.FEATURE, Identifier.fromNamespaceAndPath("ultramarine", name));
   }

   private static void register(BootstrapContext<Feature> context, ResourceKey<Feature> key, Feature feature) {
      context.register(key, feature);
   }
}
