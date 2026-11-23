package com.voxelutopia.ultramarine.client.integration.jei;

import com.voxelutopia.ultramarine.data.recipe.CompositeSmeltingRecipe;
import com.voxelutopia.ultramarine.data.registry.BlockRegistry;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.placement.VerticalAlignment;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.gui.widgets.ITextWidget;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.library.util.RecipeUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class CompositeSmeltingRecipeCategory implements IRecipeCategory<CompositeSmeltingRecipe> {
   public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath("ultramarine", "composite_smelting");
   public static final RecipeType<CompositeSmeltingRecipe> COMPOSITE_SMELTING_RECIPE_TYPE = new RecipeType(UID, CompositeSmeltingRecipe.class);
   public static final ResourceLocation TEXTURE_GUI = ResourceLocation.fromNamespaceAndPath("ultramarine", "textures/gui/brick_kiln.png");
   private final IDrawable background;
   private final int regularCookTime;
   private final IDrawable icon;
   private final Component localizedName;

   public CompositeSmeltingRecipeCategory(IGuiHelper guiHelper) {
      this.background = guiHelper.createDrawable(TEXTURE_GUI, 45, 16, 92, 54);
      this.regularCookTime = 200;
      this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(BlockRegistry.BRICK_KILN));
      this.localizedName = Component.translatable("gui.jei.category.composite_smelting");
   }

   public IDrawable getBackground() {
      return this.background;
   }

   public IDrawable getIcon() {
      return this.icon;
   }

   public void createRecipeExtras(IRecipeExtrasBuilder builder, CompositeSmeltingRecipe recipe, IFocusGroup focuses) {
      int cookTime = recipe.getCookingTime();
      if (cookTime <= 0) {
         cookTime = this.regularCookTime;
      }

      builder.addAnimatedRecipeArrow(cookTime).setPosition(34, 17);
      builder.addAnimatedRecipeFlame(300).setPosition(11, 20);
      this.addExperience(builder, recipe);
      this.addCookTime(builder, recipe);
   }

   protected void addExperience(IRecipeExtrasBuilder builder, CompositeSmeltingRecipe recipe) {
      float experience = recipe.getExp();
      if (experience > 0.0F) {
         Component experienceString = Component.translatable("gui.jei.category.smelting.experience", new Object[]{experience});
         ((ITextWidget)builder.addText(experienceString, this.getWidth() - 20, 10)
               .setPosition(0, 0, this.getWidth(), this.getHeight(), HorizontalAlignment.RIGHT, VerticalAlignment.TOP))
            .setTextAlignment(HorizontalAlignment.RIGHT)
            .setColor(-8355712);
      }
   }

   protected void addCookTime(IRecipeExtrasBuilder builder, CompositeSmeltingRecipe recipe) {
      int cookTime = recipe.getCookingTime();
      if (cookTime <= 0) {
         cookTime = this.regularCookTime;
      }

      if (cookTime > 0) {
         int cookTimeSeconds = cookTime / 20;
         Component timeString = Component.translatable("gui.jei.category.smelting.time.seconds", new Object[]{cookTimeSeconds});
         ((ITextWidget)builder.addText(timeString, this.getWidth() - 20, 10)
               .setPosition(0, 0, this.getWidth(), this.getHeight(), HorizontalAlignment.RIGHT, VerticalAlignment.BOTTOM))
            .setTextAlignment(HorizontalAlignment.RIGHT)
            .setTextAlignment(VerticalAlignment.BOTTOM)
            .setColor(-8355712);
      }
   }

   public Component getTitle() {
      return this.localizedName;
   }

   public void setRecipe(IRecipeLayoutBuilder builder, CompositeSmeltingRecipe recipe, IFocusGroup focuses) {
      builder.addSlot(RecipeIngredientRole.INPUT, 1, 1).addIngredients(recipe.getPrimaryIngredient());
      builder.addSlot(RecipeIngredientRole.INPUT, 21, 1).addIngredients(recipe.getSecondaryIngredient());
      builder.addSlot(RecipeIngredientRole.OUTPUT, 71, 19).addItemStack(RecipeUtil.getResultItem(recipe));
   }

   public RecipeType<CompositeSmeltingRecipe> getRecipeType() {
      return COMPOSITE_SMELTING_RECIPE_TYPE;
   }
}
