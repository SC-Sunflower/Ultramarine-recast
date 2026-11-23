package com.voxelutopia.ultramarine.world.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import org.jetbrains.annotations.Nullable;

public class ChiselTemplate extends Item {
   public ChiselTemplate(Item.Properties properties) {
      super(properties);
   }

   public boolean hasCraftingRemainingItem() {
      return true;
   }

   @Nullable
   public Item getCraftingRemainingItem() {
      return this;
   }
}
