package com.voxelutopia.ultramarine.client.integration.jei;

import com.voxelutopia.ultramarine.client.screen.BrickKilnScreen;
import com.voxelutopia.ultramarine.client.screen.ChiselTableScreen;
import com.voxelutopia.ultramarine.data.recipe.ChiselTableRecipe;
import com.voxelutopia.ultramarine.data.recipe.CompositeSmeltingRecipe;
import com.voxelutopia.ultramarine.data.recipe.WoodworkingRecipe;
import com.voxelutopia.ultramarine.data.registry.BlockRegistry;
import com.voxelutopia.ultramarine.data.registry.RecipeTypeRegistry;
import com.voxelutopia.ultramarine.world.entity.TravellingMerchant;
import java.util.List;
import java.util.Objects;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JeiPlugin
public class UltramarineJEIPlugin implements IModPlugin {
   public static final ResourceLocation JEI_GUI_VANILLA = new ResourceLocation("jei", "textures/gui/gui_vanilla.png");
   private static final Logger LOGGER = LoggerFactory.getLogger(UltramarineJEIPlugin.class);

   public UltramarineJEIPlugin() {
      LOGGER.info("JEI Integration Loaded");
   }

   public ResourceLocation getPluginUid() {
      return new ResourceLocation("ultramarine", "jei_plugin");
   }

   public void registerGuiHandlers(IGuiHandlerRegistration registration) {
      registration.addRecipeClickArea(BrickKilnScreen.class, 78, 32, 28, 23, new RecipeType[]{CompositeSmeltingRecipeCategory.COMPOSITE_SMELTING_RECIPE_TYPE});
      registration.addRecipeClickArea(ChiselTableScreen.class, 79, 24, 18, 30, new RecipeType[]{ChiselTableRecipeCategory.CHISEL_TABLE_RECIPE_TYPE});
   }

   public void registerCategories(IRecipeCategoryRegistration registration) {
      IJeiHelpers jeiHelpers = registration.getJeiHelpers();
      IGuiHelper guiHelper = jeiHelpers.getGuiHelper();
      registration.addRecipeCategories(new IRecipeCategory[]{new WoodworkingRecipeCategory(guiHelper)});
      registration.addRecipeCategories(new IRecipeCategory[]{new CompositeSmeltingRecipeCategory(guiHelper)});
      registration.addRecipeCategories(new IRecipeCategory[]{new ChiselTableRecipeCategory(guiHelper)});
      registration.addRecipeCategories(new IRecipeCategory[]{new TravellingMerchantCategory(guiHelper)});
   }

   public void registerRecipes(IRecipeRegistration registration) {
      Objects.requireNonNull(Minecraft.getInstance().level);
      RecipeManager recipeManager = Minecraft.getInstance().level.getRecipeManager();
      List<RecipeHolder<WoodworkingRecipe>> woodworkingHolders = recipeManager.getAllRecipesFor(RecipeTypeRegistry.WOODWORKING);
      registration.addRecipes(WoodworkingRecipeCategory.WOODWORKING_RECIPE_TYPE, woodworkingHolders.stream().map(holder -> (WoodworkingRecipe)holder.value()).toList());
      List<RecipeHolder<CompositeSmeltingRecipe>> compositeSmeltingHolders = recipeManager.getAllRecipesFor(RecipeTypeRegistry.COMPOSITE_SMELTING);
      registration.addRecipes(CompositeSmeltingRecipeCategory.COMPOSITE_SMELTING_RECIPE_TYPE, compositeSmeltingHolders.stream().map(holder -> (CompositeSmeltingRecipe)holder.value()).toList());
      List<RecipeHolder<ChiselTableRecipe>> chiselTableHolders = recipeManager.getAllRecipesFor(RecipeTypeRegistry.CHISEL_TABLE);
      registration.addRecipes(ChiselTableRecipeCategory.CHISEL_TABLE_RECIPE_TYPE, chiselTableHolders.stream().map(holder -> (ChiselTableRecipe)holder.value()).toList());
      List<TravellingMerchantWrapper> customWanderingTraderTrades = TravellingMerchant.getTradeOptions().stream().map(TravellingMerchantWrapper::new).toList();
      registration.addRecipes(TravellingMerchantCategory.CUSTOM_WANDERING_TRADER_WRAPPER_RECIPE_TYPE, customWanderingTraderTrades);
   }

   public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
      registration.addRecipeCatalyst(new ItemStack(BlockRegistry.WOODWORKING_WORKBENCH), new RecipeType[]{WoodworkingRecipeCategory.WOODWORKING_RECIPE_TYPE});
      registration.addRecipeCatalyst(new ItemStack(BlockRegistry.BRICK_KILN), new RecipeType[]{CompositeSmeltingRecipeCategory.COMPOSITE_SMELTING_RECIPE_TYPE});
      registration.addRecipeCatalyst(new ItemStack(BlockRegistry.CHISEL_TABLE), new RecipeType[]{ChiselTableRecipeCategory.CHISEL_TABLE_RECIPE_TYPE});
      registration.addRecipeCatalyst(
         new ItemStack(BlockRegistry.TEAHOUSE_FLAG), new RecipeType[]{TravellingMerchantCategory.CUSTOM_WANDERING_TRADER_WRAPPER_RECIPE_TYPE}
      );
   }
}
