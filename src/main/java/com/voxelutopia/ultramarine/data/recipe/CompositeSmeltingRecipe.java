package com.voxelutopia.ultramarine.data.recipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.voxelutopia.ultramarine.data.registry.RecipeSerializerRegistry;
import com.voxelutopia.ultramarine.data.registry.RecipeTypeRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class CompositeSmeltingRecipe implements Recipe<Container> {
   protected final ResourceLocation id;
   protected final String group;
   protected final Ingredient primaryIngredient;
   protected final Ingredient secondaryIngredient;
   protected final ItemStack result;
   protected final float experience;
   protected final int cookingTime;

   public CompositeSmeltingRecipe(
      ResourceLocation pId, String pGroup, Ingredient primaryIngredient, Ingredient secondaryIngredient, ItemStack pResult, float pExperience, int pCookingTime
   ) {
      this.id = pId;
      this.group = pGroup;
      this.primaryIngredient = primaryIngredient;
      this.secondaryIngredient = secondaryIngredient;
      this.result = pResult;
      this.experience = pExperience;
      this.cookingTime = pCookingTime;
   }

   public boolean matches(Container pContainer, Level pLevel) {
      return this.primaryIngredient.test(pContainer.getItem(0)) && this.secondaryIngredient.test(pContainer.getItem(1));
   }

   public boolean partialMatch(Container pContainer, Level pLevel) {
      return this.primaryIngredient.or(this.secondaryIngredient).test(pContainer.getItem(0));
   }

   public ItemStack assemble(Container pContainer, RegistryAccess registryAccess) {
      return this.result.copy();
   }

   public boolean canCraftInDimensions(int pWidth, int pHeight) {
      return true;
   }

   public Ingredient getPrimaryIngredient() {
      return this.primaryIngredient;
   }

   public Ingredient getSecondaryIngredient() {
      return this.secondaryIngredient;
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

   public RecipeSerializer<?> getSerializer() {
      return RecipeSerializerRegistry.COMPOSITE_SMELTING_SERIALIZER;
   }

   public RecipeType<?> getType() {
      return RecipeTypeRegistry.COMPOSITE_SMELTING;
   }

   public int getCookingTime() {
      return this.cookingTime;
   }

   public float getExp() {
      return this.experience;
   }

   public static class Serializer implements RecipeSerializer<CompositeSmeltingRecipe> {
      public static final CompositeSmeltingRecipe.Serializer INSTANCE = new CompositeSmeltingRecipe.Serializer();
      private static final int defaultCookingTime = 200;

      protected Serializer() {
      }

      public CompositeSmeltingRecipe fromJson(ResourceLocation pRecipeId, JsonObject pJson) {
         if (!pJson.has("result")) {
            throw new JsonSyntaxException("Missing result, expected to find a string or object");
         }

         String group = GsonHelper.getAsString(pJson, "group", "");
         Ingredient primaryIngredient = parseIngredient(pJson, "primary_ingredient");
         Ingredient secondaryIngredient = parseIngredient(pJson, "secondary_ingredient");
         ItemStack result;
         if (pJson.get("result").isJsonObject()) {
            result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(pJson, "result"));
         } else {
            String s1 = GsonHelper.getAsString(pJson, "result");
            ResourceLocation resourcelocation = ResourceLocation.tryParse(s1);
            ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, resourcelocation);
            result = new ItemStack(
               (Holder)BuiltInRegistries.ITEM.getHolder(itemKey).orElseThrow(() -> new IllegalStateException("Item: " + s1 + " does not exist"))
            );
         }

         float exp = GsonHelper.getAsFloat(pJson, "experience", 0.0F);
         int cookingTime = GsonHelper.getAsInt(pJson, "cookingtime", 200);
         return new CompositeSmeltingRecipe(pRecipeId, group, primaryIngredient, secondaryIngredient, result, exp, cookingTime);
      }

      @Nullable
      public CompositeSmeltingRecipe fromNetwork(ResourceLocation pRecipeId, FriendlyByteBuf pBuffer) {
         String group = pBuffer.readUtf();
         Ingredient primary = Ingredient.fromNetwork(pBuffer);
         Ingredient secondary = Ingredient.fromNetwork(pBuffer);
         ItemStack result = pBuffer.readItem();
         float exp = pBuffer.readFloat();
         int cookingTime = pBuffer.readVarInt();
         return new CompositeSmeltingRecipe(pRecipeId, group, primary, secondary, result, exp, cookingTime);
      }

      public void toNetwork(FriendlyByteBuf pBuffer, CompositeSmeltingRecipe pRecipe) {
         pBuffer.writeUtf(pRecipe.group);
         pRecipe.primaryIngredient.toNetwork(pBuffer);
         pRecipe.secondaryIngredient.toNetwork(pBuffer);
         pBuffer.writeItem(pRecipe.result);
         pBuffer.writeFloat(pRecipe.experience);
         pBuffer.writeVarInt(pRecipe.cookingTime);
      }

      private static Ingredient parseIngredient(JsonObject json, String member) {
         JsonElement ingredientRaw = (JsonElement)(GsonHelper.isArrayNode(json, member)
            ? GsonHelper.getAsJsonArray(json, member)
            : GsonHelper.getAsJsonObject(json, member));
         return Ingredient.fromJson(ingredientRaw);
      }
   }
}
