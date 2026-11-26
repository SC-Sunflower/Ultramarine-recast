package com.voxelutopia.ultramarine.data.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.voxelutopia.ultramarine.data.registry.RecipeSerializerRegistry;
import com.voxelutopia.ultramarine.data.registry.RecipeTypeRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleItemRecipe;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

public class WoodworkingRecipe extends SingleItemRecipe {
   private final String group;
   private final Ingredient ingredient;
   private final ItemStackTemplate result;

   public static final MapCodec<WoodworkingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
         Codec.STRING.optionalFieldOf("group", "").forGetter(recipe -> recipe.group),
         Ingredient.CODEC.fieldOf("ingredient").forGetter(recipe -> recipe.ingredient),
         Codec.INT.optionalFieldOf("count", 1).forGetter(recipe -> recipe.result.count()),
         Codec.STRING.fieldOf("result").forGetter(recipe -> BuiltInRegistries.ITEM.getKey(recipe.result.item().value()).toString())
   ).apply(instance, (group, ingredient, count, result) ->
         new WoodworkingRecipe(group, ingredient,
               new ItemStackTemplate(BuiltInRegistries.ITEM.getValue(Identifier.parse(result)), Math.max(1, count)))));

   public static final StreamCodec<RegistryFriendlyByteBuf, WoodworkingRecipe> STREAM_CODEC = StreamCodec.of(
         WoodworkingRecipe::toNetwork, WoodworkingRecipe::fromNetwork);

   public WoodworkingRecipe(String group, Ingredient ingredient, ItemStackTemplate result) {
      super(new Recipe.CommonInfo(true), ingredient, result);
      this.group = group;
      this.ingredient = ingredient;
      this.result = result;
   }

   private static WoodworkingRecipe fromNetwork(RegistryFriendlyByteBuf buf) {
      String group = buf.readUtf();
      Ingredient ingredient = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
      ItemStackTemplate result = ItemStackTemplate.STREAM_CODEC.decode(buf);
      return new WoodworkingRecipe(group, ingredient, result);
   }

   private static void toNetwork(RegistryFriendlyByteBuf buf, WoodworkingRecipe recipe) {
      buf.writeUtf(recipe.group);
      Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.ingredient);
      ItemStackTemplate.STREAM_CODEC.encode(buf, recipe.result);
   }

   @Override
   public boolean matches(SingleRecipeInput input, Level level) {
      return this.ingredient.test(input.item());
   }

   @Override
   public ItemStack assemble(SingleRecipeInput input) {
      return this.result.create();
   }

   public ItemStack getResultStack() {
      return this.result.create();
   }

   @Override
   public boolean isSpecial() {
      return false;
   }

   @Override
   public String group() {
      return this.group;
   }

   @Override
   public RecipeType<WoodworkingRecipe> getType() {
      return RecipeTypeRegistry.WOODWORKING;
   }

   @Override
   public RecipeSerializer<? extends SingleItemRecipe> getSerializer() {
      return RecipeSerializerRegistry.WOODWORKING_SERIALIZER;
   }

   @Override
   public RecipeBookCategory recipeBookCategory() {
      return new RecipeBookCategory();
   }

   @Override
   public PlacementInfo placementInfo() {
      return PlacementInfo.create(this.ingredient);
   }
}
