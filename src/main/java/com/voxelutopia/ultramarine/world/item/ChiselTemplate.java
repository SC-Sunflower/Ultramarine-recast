package com.voxelutopia.ultramarine.world.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import org.jetbrains.annotations.Nullable;

public class ChiselTemplate extends Item {
   public ChiselTemplate() {
      super(new Properties());
   }

   public boolean hasCraftingRemainingItem() {
      return true;
   }

   @Nullable
   public Item getCraftingRemainingItem() {
      return this;
   }
}
