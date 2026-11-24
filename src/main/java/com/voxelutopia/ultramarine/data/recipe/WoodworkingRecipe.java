package com.voxelutopia.ultramarine.data.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.voxelutopia.ultramarine.data.registry.RecipeSerializerRegistry;
import com.voxelutopia.ultramarine.data.registry.RecipeTypeRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleItemRecipe;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

public class WoodworkingRecipe extends SingleItemRecipe {
    private final String group;
    private final Ingredient ingredient;
    private final ItemStack result;

    public static final MapCodec<WoodworkingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.optionalFieldOf("group", "").forGetter(recipe -> recipe.group),
            Ingredient.CODEC.fieldOf("ingredient").forGetter(recipe -> recipe.ingredient),
            Codec.INT.optionalFieldOf("count", 1).forGetter(recipe -> recipe.result.getCount()),
      Codec.STRING.fieldOf("result").forGetter(recipe -> BuiltInRegistries.ITEM.getKey(recipe.result.getItem()).toString())
    ).apply(instance, (group, ingredient, count, result) -> new WoodworkingRecipe(group, ingredient, new ItemStack(BuiltInRegistries.ITEM.getValue(ResourceLocation.parse(result)), count))));

    public WoodworkingRecipe(String pGroup, Ingredient pIngredient, ItemStack pResult) {
        super(pGroup, pIngredient, pResult);
        this.group = pGroup;
        this.ingredient = pIngredient;
        this.result = pResult;
    }

    @Override
    public boolean matches(SingleRecipeInput input, Level pLevel) {
        return this.ingredient.test(input.item());
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider provider) {
        return this.result.copy();
    }

    public ItemStack getResultStack() {
        return this.result.copy();
    }

    public Ingredient getIngredient() {
        return this.ingredient;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeType<WoodworkingRecipe> getType() {
        return RecipeTypeRegistry.WOODWORKING;
    }

    @Override
    public RecipeSerializer<WoodworkingRecipe> getSerializer() {
        return RecipeSerializerRegistry.WOODWORKING_SERIALIZER;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return new RecipeBookCategory();
    }

   public PlacementInfo placementInfo() {
      return PlacementInfo.NOT_PLACEABLE;
   }

    public static class Serializer implements RecipeSerializer<WoodworkingRecipe> {
        public static final WoodworkingRecipe.Serializer INSTANCE = new WoodworkingRecipe.Serializer();

        protected Serializer() {
        }

        @Override
        public MapCodec<WoodworkingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, WoodworkingRecipe> streamCodec() {
            return StreamCodec.of(this::toNetwork, this::fromNetwork);
        }

        private WoodworkingRecipe fromNetwork(RegistryFriendlyByteBuf buf) {
            String group = buf.readUtf();
            Ingredient ingredient = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
            ItemStack itemstack = ItemStack.STREAM_CODEC.decode(buf);
            return new WoodworkingRecipe(group, ingredient, itemstack);
        }

        private void toNetwork(RegistryFriendlyByteBuf buf, WoodworkingRecipe recipe) {
            buf.writeUtf(recipe.group);
            Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.ingredient);
            ItemStack.STREAM_CODEC.encode(buf, recipe.result);
        }
    }
}
