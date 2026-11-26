package com.voxelutopia.ultramarine.data.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.voxelutopia.ultramarine.data.registry.RecipeSerializerRegistry;
import com.voxelutopia.ultramarine.data.registry.RecipeTypeRegistry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
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

public class ChiselTableRecipe implements Recipe<CraftingInput> {
   protected final String group;
   protected final Ingredient material;
   protected final Ingredient template;
   protected final List<Ingredient> colors;
   protected final ItemStackTemplate result;

   public static final MapCodec<ChiselTableRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
         Codec.STRING.optionalFieldOf("group", "").forGetter(recipe -> recipe.group),
         Ingredient.CODEC.fieldOf("material").forGetter(recipe -> recipe.material),
         Ingredient.CODEC.fieldOf("template").forGetter(recipe -> recipe.template),
         Ingredient.CODEC.listOf().fieldOf("colors").forGetter(recipe -> recipe.colors),
         Codec.INT.optionalFieldOf("count", 1).forGetter(recipe -> recipe.result.count()),
         Codec.STRING.fieldOf("result").forGetter(recipe -> BuiltInRegistries.ITEM.getKey(recipe.result.item().value()).toString())
   ).apply(instance, (group, material, template, colors, count, result) ->
         new ChiselTableRecipe(group, material, template, colors.toArray(new Ingredient[0]),
               new ItemStackTemplate(BuiltInRegistries.ITEM.getValue(Identifier.parse(result)), Math.max(1, count)))));

   public static final StreamCodec<RegistryFriendlyByteBuf, ChiselTableRecipe> STREAM_CODEC = StreamCodec.of(
         ChiselTableRecipe::toNetwork, ChiselTableRecipe::fromNetwork);

   public ChiselTableRecipe(String group, Ingredient material, Ingredient template, Ingredient[] colors, ItemStackTemplate result) {
      this.group = group;
      this.material = material;
      this.template = template;
      this.colors = new ArrayList<>();
      this.colors.addAll(Arrays.asList(colors));
      this.result = result;
   }

   private static ChiselTableRecipe fromNetwork(RegistryFriendlyByteBuf buf) {
      String group = buf.readUtf();
      Ingredient material = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
      Ingredient template = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
      Ingredient[] colors = new Ingredient[4];
      for (int i = 0; i < 4; i++) {
         colors[i] = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
      }
      colors = Arrays.stream(colors).filter(i -> !i.isEmpty()).toList().toArray(new Ingredient[0]);
      ItemStackTemplate result = ItemStackTemplate.STREAM_CODEC.decode(buf);
      return new ChiselTableRecipe(group, material, template, colors, result);
   }

   private static void toNetwork(RegistryFriendlyByteBuf buf, ChiselTableRecipe recipe) {
      buf.writeUtf(recipe.group);
      Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.material);
      Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.template);
      for (int i = 0; i < 4; i++) {
         Ingredient color = i < recipe.colors.size() ? recipe.colors.get(i) : Ingredient.of();
         Ingredient.CONTENTS_STREAM_CODEC.encode(buf, color);
      }
      ItemStackTemplate.STREAM_CODEC.encode(buf, recipe.result);
   }

   @Override
   public boolean matches(CraftingInput input, Level level) {
      ItemStack usedMaterial = input.getItem(0);
      ItemStack usedTemplate = input.getItem(1);
      List<ItemStack> usedColors = Arrays.asList(ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY);
      int i = 0;

      for (int j = 2; j < input.size(); j++) {
         usedColors.set(i, input.getItem(j));
         i++;
      }

      usedColors = usedColors.stream().filter(item -> !item.isEmpty()).collect(Collectors.toList());
      return this.material.test(usedMaterial) && this.template.test(usedTemplate) && compareColors(this.colors, usedColors);
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
      return PlacementInfo.create(List.of(this.material, this.template));
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

   @Override
   public RecipeSerializer<? extends Recipe<CraftingInput>> getSerializer() {
      return RecipeSerializerRegistry.CHISEL_TABLE_SERIALIZER;
   }

   @Override
   public RecipeType<ChiselTableRecipe> getType() {
      return RecipeTypeRegistry.CHISEL_TABLE;
   }

   @Override
   public RecipeBookCategory recipeBookCategory() {
      return new RecipeBookCategory();
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
}
