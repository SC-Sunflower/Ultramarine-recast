package com.voxelutopia.ultramarine.data.recipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import com.voxelutopia.ultramarine.data.registry.RecipeSerializerRegistry;
import com.voxelutopia.ultramarine.data.registry.RecipeTypeRegistry;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleItemRecipe;
import net.minecraft.world.level.Level;

public class WoodworkingRecipe extends SingleItemRecipe {
    public static final MapCodec<WoodworkingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.optionalFieldOf("group", "").forGetter(recipe -> recipe.group),
            Ingredient.CODEC.fieldOf("ingredient").forGetter(recipe -> recipe.ingredient),
            Codec.INT.optionalFieldOf("count", 1).forGetter(recipe -> recipe.result.getCount()),
            Codec.STRING.fieldOf("result").forGetter(recipe -> BuiltInRegistries.ITEM.getKey(recipe.result.getItem()).toString())
    ).apply(instance, (group, ingredient, count, resultId) -> new WoodworkingRecipe(group, ingredient,
            new ItemStack(BuiltInRegistries.ITEM.get(new ResourceLocation(resultId)), count))));

    public WoodworkingRecipe(String pGroup, Ingredient pIngredient, ItemStack pResult) {
        super(RecipeTypeRegistry.WOODWORKING, RecipeSerializerRegistry.WOODWORKING_SERIALIZER, pGroup, pIngredient, pResult);
    }

    public boolean matches(Container pContainer, Level pLevel) {
        return this.ingredient.test(pContainer.getItem(0));
    }

    public ItemStack assemble(Container pContainer, HolderLookup.Provider provider) {
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

public ItemStack getResultItem(HolderLookup.Provider provider) {
        return this.result.copy();
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
