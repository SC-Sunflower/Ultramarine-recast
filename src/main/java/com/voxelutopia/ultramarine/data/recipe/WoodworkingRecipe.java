package com.voxelutopia.ultramarine.data.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.voxelutopia.ultramarine.data.registry.RecipeSerializerRegistry;
import com.voxelutopia.ultramarine.data.registry.RecipeTypeRegistry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleItemRecipe;
import net.minecraft.world.level.Level;

public class WoodworkingRecipe extends SingleItemRecipe {
    public static final Codec<WoodworkingRecipe> CODEC = RecordCodecBuilder.create(instance -> instance.group(
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
        public Codec<WoodworkingRecipe> codec() {
            return CODEC;
        }

        @Override
        public WoodworkingRecipe fromNetwork(FriendlyByteBuf pBuffer) {
            String group = pBuffer.readUtf();
            Ingredient ingredient = Ingredient.fromNetwork(pBuffer);
            ItemStack itemstack = pBuffer.readItem();
            return new WoodworkingRecipe(group, ingredient, itemstack);
        }

        @Override
        public void toNetwork(FriendlyByteBuf pBuffer, WoodworkingRecipe pRecipe) {
            pBuffer.writeUtf(pRecipe.group);
            pRecipe.ingredient.toNetwork(pBuffer);
            pBuffer.writeItem(pRecipe.result);
        }
    }
}
