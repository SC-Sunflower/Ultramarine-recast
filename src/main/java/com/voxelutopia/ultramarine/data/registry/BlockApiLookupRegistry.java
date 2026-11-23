package com.voxelutopia.ultramarine.data.registry;

import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;

public class BlockApiLookupRegistry {
   public static void init() {
      ItemStorage.SIDED.registerForBlockEntity((blockEntity, direction) -> {
         if (!blockEntity.remove && direction != null) {
            return switch (direction) {
               case UP -> blockEntity.getIngredientsHandler();
               case DOWN -> blockEntity.getResultHandler();
               default -> blockEntity.getFuelHandler();
            };
         } else {
            return null;
         }
      }, BlockEntityRegistry.BRICK_KILN);
   }
}
