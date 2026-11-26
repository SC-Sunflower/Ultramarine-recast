package com.voxelutopia.ultramarine.data.registry;

import com.voxelutopia.ultramarine.data.recipe.ChiselTableRecipe;
import com.voxelutopia.ultramarine.data.recipe.CompositeSmeltingRecipe;
import com.voxelutopia.ultramarine.data.recipe.WoodworkingRecipe;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class RecipeSerializerRegistry {
   public static RecipeSerializer<WoodworkingRecipe> WOODWORKING_SERIALIZER;
   public static RecipeSerializer<CompositeSmeltingRecipe> COMPOSITE_SMELTING_SERIALIZER;
   public static RecipeSerializer<ChiselTableRecipe> CHISEL_TABLE_SERIALIZER;

   public static void registerModRecipeSerializers() {
      WOODWORKING_SERIALIZER = Registry.register(
         BuiltInRegistries.RECIPE_SERIALIZER, Identifier.fromNamespaceAndPath("ultramarine", "woodworking"),
         new RecipeSerializer<>(WoodworkingRecipe.CODEC, WoodworkingRecipe.STREAM_CODEC)
      );
      COMPOSITE_SMELTING_SERIALIZER = Registry.register(
         BuiltInRegistries.RECIPE_SERIALIZER, Identifier.fromNamespaceAndPath("ultramarine", "composite_smelting"),
         new RecipeSerializer<>(CompositeSmeltingRecipe.CODEC, CompositeSmeltingRecipe.STREAM_CODEC)
      );
      CHISEL_TABLE_SERIALIZER = Registry.register(
         BuiltInRegistries.RECIPE_SERIALIZER, Identifier.fromNamespaceAndPath("ultramarine", "chisel_table"),
         new RecipeSerializer<>(ChiselTableRecipe.CODEC, ChiselTableRecipe.STREAM_CODEC)
      );
   }
}
