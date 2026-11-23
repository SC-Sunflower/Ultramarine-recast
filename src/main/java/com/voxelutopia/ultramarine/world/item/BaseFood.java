package com.voxelutopia.ultramarine.world.item;

import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;

public class BaseFood extends Item {
   public BaseFood(FoodProperties food, Item.Properties properties) {
      super(properties.food(food));
   }
}
