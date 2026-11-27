package com.voxelutopia.ultramarine.data.recipe;

import com.google.gson.JsonObject;
import com.voxelutopia.ultramarine.data.registry.RecipeSerializerRegistry;
import com.voxelutopia.ultramarine.data.registry.RecipeTypeRegistry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleItemRecipe;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;

public class WoodworkingRecipe extends SingleItemRecipe {
   protected final Ingredient ingredient;
   protected final ItemStack result;
   protected final ResourceLocation id;
   protected final String group;

   public WoodworkingRecipe(ResourceLocation pId, String pGroup, Ingredient pIngredient, ItemStack pResult) {
      super(RecipeTypeRegistry.WOODWORKING, RecipeSerializerRegistry.WOODWORKING_SERIALIZER, pId, pGroup, pIngredient, pResult);
      this.id = pId;
      this.group = pGroup;
      this.ingredient = pIngredient;
      this.result = pResult;
   }

   public boolean matches(Container pContainer, Level pLevel) {
      return this.ingredient.test(pContainer.getItem(0));
   }

   public ItemStack assemble(Container pContainer, RegistryAccess registryAccess) {
      return this.result.copy();
   }

   public boolean canCraftInDimensions(int pWidth, int pHeight) {
      return true;
   }

   public boolean isSpecial() {
      return true;
   }

   public Ingredient getIngredient() {
      return this.ingredient;
   }

   public ItemStack getResultStack() {
      return this.result.copy();
   }

   public ItemStack getResultItem(RegistryAccess registryAccess) {
      return this.result.copy();
   }

   public ResourceLocation getId() {
      return this.id;
   }

   public RecipeType<?> getType() {
      return RecipeTypeRegistry.WOODWORKING;
   }

   public RecipeSerializer<?> getSerializer() {
      return RecipeSerializerRegistry.WOODWORKING_SERIALIZER;
   }

   public static class Serializer implements RecipeSerializer<WoodworkingRecipe> {
      public static final WoodworkingRecipe.Serializer INSTANCE = new WoodworkingRecipe.Serializer();

      protected Serializer() {
      }

      public WoodworkingRecipe fromJson(ResourceLocation pRecipeId, JsonObject pJson) {
         String s = GsonHelper.getAsString(pJson, "group", "");
         Ingredient ingredient;
         if (GsonHelper.isArrayNode(pJson, "ingredient")) {
            ingredient = Ingredient.fromJson(GsonHelper.getAsJsonArray(pJson, "ingredient"));
         } else {
            ingredient = Ingredient.fromJson(GsonHelper.getAsJsonObject(pJson, "ingredient"));
         }

         String s1 = GsonHelper.getAsString(pJson, "result");
         int i = GsonHelper.getAsInt(pJson, "count");
         ItemStack itemstack = new ItemStack((ItemLike)BuiltInRegistries.ITEM.get(ResourceLocation.tryParse(s1)), i);
         return new WoodworkingRecipe(pRecipeId, s, ingredient, itemstack);
      }

      public WoodworkingRecipe fromNetwork(ResourceLocation pRecipeId, FriendlyByteBuf pBuffer) {
         String s = pBuffer.readUtf();
         Ingredient ingredient = Ingredient.fromNetwork(pBuffer);
         ItemStack itemstack = pBuffer.readItem();
         return new WoodworkingRecipe(pRecipeId, s, ingredient, itemstack);
      }

      public void toNetwork(FriendlyByteBuf pBuffer, WoodworkingRecipe pRecipe) {
         pBuffer.writeUtf(pRecipe.group);
         pRecipe.ingredient.toNetwork(pBuffer);
         pBuffer.writeItem(pRecipe.result);
      }
   }
}
