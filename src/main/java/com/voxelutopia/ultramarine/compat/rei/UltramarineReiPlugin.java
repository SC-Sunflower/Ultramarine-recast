package com.voxelutopia.ultramarine.compat.rei;

import com.voxelutopia.ultramarine.data.recipe.ChiselTableRecipe;
import com.voxelutopia.ultramarine.data.recipe.CompositeSmeltingRecipe;
import com.voxelutopia.ultramarine.data.recipe.WoodworkingRecipe;
import com.voxelutopia.ultramarine.data.registry.ItemRegistry;
import com.voxelutopia.ultramarine.data.registry.RecipeTypeRegistry;
import java.util.List;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;

/**
 * REI 12 integration: displays are generated on the client from the synced recipe
 * manager (1.20.1 still gives the client a full RecipeManager).
 */
public class UltramarineReiPlugin implements REIClientPlugin {

   @Override
   public void registerCategories(CategoryRegistry registry) {
      registry.add(
            new UltramarineCategory<>(UltramarineReiCategories.WOODWORKING, UltramarineReiCategories.WOODWORKING_TITLE,
                  new ItemStack(ItemRegistry.WOODWORKING_WORKBENCH)),
            new UltramarineCategory<>(UltramarineReiCategories.CHISEL_TABLE, UltramarineReiCategories.CHISEL_TABLE_TITLE,
                  new ItemStack(ItemRegistry.CHISEL_TABLE)),
            new UltramarineCategory<>(UltramarineReiCategories.COMPOSITE_SMELTING, UltramarineReiCategories.COMPOSITE_SMELTING_TITLE,
                  new ItemStack(ItemRegistry.BRICK_KILN)));
      registry.addWorkstations(UltramarineReiCategories.WOODWORKING, EntryStacks.of(ItemRegistry.WOODWORKING_WORKBENCH));
      registry.addWorkstations(UltramarineReiCategories.CHISEL_TABLE, EntryStacks.of(ItemRegistry.CHISEL_TABLE));
      registry.addWorkstations(UltramarineReiCategories.COMPOSITE_SMELTING, EntryStacks.of(ItemRegistry.BRICK_KILN));
   }

   @Override
   public void registerDisplays(DisplayRegistry registry) {
      Minecraft minecraft = Minecraft.getInstance();
      if (minecraft.level == null) {
         return;
      }
      RecipeManager recipeManager = minecraft.level.getRecipeManager();

      for (Recipe<?> recipe : recipeManager.getAllRecipesFor(RecipeTypeRegistry.WOODWORKING)) {
         WoodworkingRecipe woodworking = (WoodworkingRecipe) recipe;
         registry.add(new UltramarineReiDisplays.Woodworking(
               List.of(EntryIngredients.ofIngredient(woodworking.getIngredient())),
               List.of(EntryIngredients.of(woodworking.getResultStack()))));
      }
      for (Recipe<?> recipe : recipeManager.getAllRecipesFor(RecipeTypeRegistry.CHISEL_TABLE)) {
         ChiselTableRecipe chisel = (ChiselTableRecipe) recipe;
         registry.add(new UltramarineReiDisplays.ChiselTable(
               List.of(EntryIngredients.ofIngredient(chisel.getMaterial()), EntryIngredients.ofIngredient(chisel.getTemplate())),
               List.of(EntryIngredients.of(chisel.getResultStack()))));
      }
      for (Recipe<?> recipe : recipeManager.getAllRecipesFor(RecipeTypeRegistry.COMPOSITE_SMELTING)) {
         CompositeSmeltingRecipe smelting = (CompositeSmeltingRecipe) recipe;
         registry.add(new UltramarineReiDisplays.CompositeSmelting(
               List.of(EntryIngredients.ofIngredient(smelting.getPrimaryIngredient()),
                     EntryIngredients.ofIngredient(smelting.getSecondaryIngredient())),
               List.of(EntryIngredients.of(smelting.getResultStack()))));
      }
   }
}
