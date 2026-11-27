package com.voxelutopia.ultramarine.data.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.voxelutopia.ultramarine.data.registry.RecipeSerializerRegistry;
import com.voxelutopia.ultramarine.data.registry.RecipeTypeRegistry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
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

public class ChiselTableRecipe implements Recipe<Container> {
   protected final ResourceLocation id;
   protected final String group;
   protected final Ingredient material;
   protected final Ingredient template;
   protected final List<Ingredient> colors;
   protected final ItemStack result;

   public ChiselTableRecipe(ResourceLocation pId, String pGroup, Ingredient material, Ingredient template, Ingredient[] colors, ItemStack pResult) {
      this.id = pId;
      this.group = pGroup;
      this.material = material;
      this.template = template;
      this.colors = new ArrayList<>();
      this.colors.addAll(Arrays.asList(colors));
      this.result = pResult;
   }

   public boolean matches(Container pContainer, Level pLevel) {
      ItemStack usedMaterial = pContainer.getItem(0);
      ItemStack usedTemplate = pContainer.getItem(1);
      List<ItemStack> usedColors = Arrays.asList(ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY);
      int i = 0;

      for (int j = 2; j < pContainer.getContainerSize(); j++) {
         usedColors.set(i, pContainer.getItem(j));
         i++;
      }

      usedColors = usedColors.stream().filter(item -> !item.isEmpty()).collect(Collectors.toList());
      return this.material.test(usedMaterial) && this.template.test(usedTemplate) && compareColors(this.colors, usedColors);
   }

   public ItemStack assemble(Container pContainer, RegistryAccess registryAccess) {
      return this.result.copy();
   }

   public boolean canCraftInDimensions(int pWidth, int pHeight) {
      return true;
   }

   public ItemStack getResultStack() {
      return this.result.copy();
   }

   public ItemStack getResultItem(RegistryAccess registryAccess) {
      return this.result.copy();
   }

   public Ingredient getMaterial() {
      return this.material;
   }

   public Ingredient getTemplate() {
      return this.template;
   }

   public List<Ingredient> getColors() {
      return this.colors;
   }

   public ResourceLocation getId() {
      return this.id;
   }

   public RecipeSerializer<?> getSerializer() {
      return RecipeSerializerRegistry.CHISEL_TABLE_SERIALIZER;
   }

   public RecipeType<?> getType() {
      return RecipeTypeRegistry.CHISEL_TABLE;
   }

   private static boolean compareColors(List<Ingredient> recipeColors, List<ItemStack> usedColors) {
      if (recipeColors.size() != usedColors.size()) {
         return false;
      }

      List<ItemStack> usedColorsReverse = new ArrayList<>(usedColors);
      Collections.reverse(usedColorsReverse);
      boolean fwd = true;
      boolean rvs = true;

      for (int i = 0; i < recipeColors.size(); i++) {
         fwd = recipeColors.get(i).test(usedColors.get(i)) && fwd;
         rvs = recipeColors.get(i).test(usedColorsReverse.get(i)) && rvs;
      }

      return fwd || rvs;
   }

   public static class Serializer implements RecipeSerializer<ChiselTableRecipe> {
      public static final ChiselTableRecipe.Serializer INSTANCE = new ChiselTableRecipe.Serializer();

      protected Serializer() {
      }

      public ChiselTableRecipe fromJson(ResourceLocation pRecipeId, JsonObject pJson) {
         if (!pJson.has("result")) {
            throw new JsonSyntaxException("Missing result, expected to find a string or object");
         }

         String group = GsonHelper.getAsString(pJson, "group", "");
         Ingredient material = parseIngredient(pJson, "material");
         Ingredient template = parseIngredient(pJson, "template");
         JsonArray colorsJson = GsonHelper.getAsJsonArray(pJson, "colors");
         Ingredient[] colors = new Ingredient[colorsJson.size()];

         for (int i = 0; i < colors.length; i++) {
            colors[i] = Ingredient.fromJson(colorsJson.get(i));
         }

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

         return new ChiselTableRecipe(pRecipeId, group, material, template, colors, result);
      }

      @Nullable
      public ChiselTableRecipe fromNetwork(ResourceLocation pRecipeId, FriendlyByteBuf pBuffer) {
         String group = pBuffer.readUtf();
         Ingredient material = Ingredient.fromNetwork(pBuffer);
         Ingredient template = Ingredient.fromNetwork(pBuffer);
         Ingredient[] colors = new Ingredient[4];

         for (int i = 0; i < 4; i++) {
            colors[i] = Ingredient.fromNetwork(pBuffer);
         }

         colors = Arrays.stream(colors).filter(i -> !i.isEmpty()).toList().toArray(new Ingredient[0]);
         ItemStack result = pBuffer.readItem();
         return new ChiselTableRecipe(pRecipeId, group, material, template, colors, result);
      }

      public void toNetwork(FriendlyByteBuf pBuffer, ChiselTableRecipe pRecipe) {
         pBuffer.writeUtf(pRecipe.group);
         pRecipe.material.toNetwork(pBuffer);
         pRecipe.template.toNetwork(pBuffer);
         Ingredient[] colors = new Ingredient[4];

         for (int i = 0; i < 4; i++) {
            colors[i] = i < pRecipe.colors.size() ? pRecipe.colors.get(i) : Ingredient.EMPTY;
         }

         for (int i = 0; i < 4; i++) {
            colors[i].toNetwork(pBuffer);
         }

         pBuffer.writeItem(pRecipe.result);
      }

      private static Ingredient parseIngredient(JsonObject json, String member) {
         JsonElement ingredientRaw = (JsonElement)(GsonHelper.isArrayNode(json, member)
            ? GsonHelper.getAsJsonArray(json, member)
            : GsonHelper.getAsJsonObject(json, member));
         return Ingredient.fromJson(ingredientRaw);
      }
   }
}
