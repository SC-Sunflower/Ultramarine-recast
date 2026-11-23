package com.voxelutopia.ultramarine.data.registry;

import net.fabricmc.fabric.api.object.builder.v1.world.poi.PointOfInterestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.block.Block;

public class PoiTypeRegistry {
   public static PoiType COOKING_POI;
   public static PoiType TRADE_POI;

   public static void registerModPOI() {
      COOKING_POI = PointOfInterestHelper.register(new ResourceLocation("ultramarine", "cooking_poi"), 1, 1, new Block[]{BlockRegistry.FOOD_HAMPER});
      TRADE_POI = PointOfInterestHelper.register(new ResourceLocation("ultramarine", "trade_poi"), 1, 5, new Block[]{BlockRegistry.TEAHOUSE_FLAG});
   }
}
