package com.voxelutopia.ultramarine.data;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class ModBlockTags {
   public static final TagKey<Block> MINEABLE_WITH_SHEARS = create();

   private static TagKey<Block> create() {
      return TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("ultramarine", "mineable_with_shears"));
   }
}
