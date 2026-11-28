package com.voxelutopia.ultramarine.data.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.voxelutopia.ultramarine.data.registry.RecipeSerializerRegistry;
import com.voxelutopia.ultramarine.data.registry.RecipeTypeRegistry;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public class CompositeSmeltingRecipe implements Recipe<CraftingInput> {
   protected final String group;
   protected final Ingredient primaryIngredient;
   protected final Ingredient secondaryIngredient;
   protected final ItemStackTemplate result;
   protected final float experience;
   protected final int cookingTime;

   public static final MapCodec<CompositeSmeltingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
         Codec.STRING.optionalFieldOf("group", "").forGetter(recipe -> recipe.group),
         Ingredient.CODEC.fieldOf("primary_ingredient").forGetter(recipe -> recipe.primaryIngredient),
         Ingredient.CODEC.fieldOf("secondary_ingredient").forGetter(recipe -> recipe.secondaryIngredient),
         Codec.INT.optionalFieldOf("count", 1).forGetter(recipe -> recipe.result.count()),
         Codec.STRING.fieldOf("result").forGetter(recipe -> BuiltInRegistries.ITEM.getKey(recipe.result.item().value()).toString()),
         Codec.FLOAT.optionalFieldOf("experience", 0.0F).forGetter(recipe -> recipe.experience),
         Codec.INT.optionalFieldOf("cookingtime", 200).forGetter(recipe -> recipe.cookingTime)
   ).apply(instance, (group, primary, secondary, count, result, experience, cookingTime) ->
         new CompositeSmeltingRecipe(group, primary, secondary,
               new ItemStackTemplate(BuiltInRegistries.ITEM.getValue(Identifier.parse(result)), Math.max(1, count)),
               experience, cookingTime)));

   public static final StreamCodec<RegistryFriendlyByteBuf, CompositeSmeltingRecipe> STREAM_CODEC = StreamCodec.of(
         CompositeSmeltingRecipe::toNetwork, CompositeSmeltingRecipe::fromNetwork);

   public CompositeSmeltingRecipe(String group, Ingredient primaryIngredient, Ingredient secondaryIngredient,
                                  ItemStackTemplate result, float experience, int cookingTime) {
      this.group = group;
      this.primaryIngredient = primaryIngredient;
      this.secondaryIngredient = secondaryIngredient;
      this.result = result;
      this.experience = experience;
      this.cookingTime = cookingTime;
   }

   private static CompositeSmeltingRecipe fromNetwork(RegistryFriendlyByteBuf buf) {
      String group = buf.readUtf();
      Ingredient primary = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
      Ingredient secondary = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
      ItemStackTemplate result = ItemStackTemplate.STREAM_CODEC.decode(buf);
      float exp = buf.readFloat();
      int cookingTime = buf.readVarInt();
      return new CompositeSmeltingRecipe(group, primary, secondary, result, exp, cookingTime);
   }

   private static void toNetwork(RegistryFriendlyByteBuf buf, CompositeSmeltingRecipe recipe) {
      buf.writeUtf(recipe.group);
      Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.primaryIngredient);
      Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.secondaryIngredient);
      ItemStackTemplate.STREAM_CODEC.encode(buf, recipe.result);
      buf.writeFloat(recipe.experience);
      buf.writeVarInt(recipe.cookingTime);
   }

   @Override
   public boolean matches(CraftingInput input, Level level) {
      // Order-insensitive: players may drop materials into either ingredient
      // slot (e.g. via shift-click), so accept both arrangements.
      return (this.primaryIngredient.test(input.getItem(0)) && this.secondaryIngredient.test(input.getItem(1)))
      || (this.primaryIngredient.test(input.getItem(1)) && this.secondaryIngredient.test(input.getItem(0)));
   }

   public boolean partialMatch(CraftingInput input, Level level) {
      return this.primaryIngredient.or(this.secondaryIngredient).test(input.getItem(0));
   }

   @Override
   public ItemStack assemble(CraftingInput input) {
      return this.result.create();
   }

   public ItemStack getResultStack() {
      return this.result.create();
   }

   @Override
   public String group() {
      return this.group;
   }

   @Override
   public boolean showNotification() {
      return true;
   }

   @Override
   public PlacementInfo placementInfo() {
      return PlacementInfo.create(List.of(this.primaryIngredient, this.secondaryIngredient));
   }

   public Ingredient getPrimaryIngredient() {
      return this.primaryIngredient;
   }

   public Ingredient getSecondaryIngredient() {
      return this.secondaryIngredient;
   }

   public int getCookingTime() {
      return this.cookingTime;
   }

   public float getExp() {
      return this.experience;
   }

   @Override
   public RecipeSerializer<? extends Recipe<CraftingInput>> getSerializer() {
      return RecipeSerializerRegistry.COMPOSITE_SMELTING_SERIALIZER;
   }

   @Override
   public RecipeType<CompositeSmeltingRecipe> getType() {
      return RecipeTypeRegistry.COMPOSITE_SMELTING;
   }

   @Override
   public RecipeBookCategory recipeBookCategory() {
      return new RecipeBookCategory();
   }
}
