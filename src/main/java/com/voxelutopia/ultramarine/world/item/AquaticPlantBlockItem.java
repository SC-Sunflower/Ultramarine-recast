package com.voxelutopia.ultramarine.world.item;

import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.PlaceOnWaterBlockItem;
import net.minecraft.world.level.block.Block;

/**
 * 1.21.2+ (and earlier in vanilla): the water-lily item uses PlaceOnWaterBlockItem,
 * which ray-traces against source water and places on the surface above it.
 */
public class AquaticPlantBlockItem extends PlaceOnWaterBlockItem {
   public AquaticPlantBlockItem(Block pBlock, Properties properties) {
      super(pBlock, properties);
   }
}