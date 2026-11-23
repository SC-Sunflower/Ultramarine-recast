package com.voxelutopia.ultramarine.world.item;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;

public class DyePowder extends Item {
   private final DyeColor color;

   public DyePowder(DyeColor color, Item.Properties properties) {
      super(properties);
      this.color = color;
   }

   public DyeColor getColor() {
      return this.color;
   }
}
