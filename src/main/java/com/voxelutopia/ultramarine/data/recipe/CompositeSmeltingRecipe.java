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
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public class CompositeSmeltingRecipe implements Recipe<Container> {
    protected final String group;
    protected final Ingredient primaryIngredient;
    protected final Ingredient secondaryIngredient;
    protected final ItemStack result;
    protected final float experience;
    protected final int cookingTime;

    public static final MapCodec<CompositeSmeltingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.optionalFieldOf("group", "").forGetter(recipe -> recipe.group),
            Ingredient.CODEC.fieldOf("primary_ingredient").forGetter(recipe -> recipe.primaryIngredient),
            Ingredient.CODEC.fieldOf("secondary_ingredient").forGetter(recipe -> recipe.secondaryIngredient),
            Codec.INT.optionalFieldOf("count", 1).forGetter(recipe -> recipe.result.getCount()),
            Codec.STRING.fieldOf("result").forGetter(recipe -> BuiltInRegistries.ITEM.getKey(recipe.result.getItem()).toString()),
            Codec.FLOAT.optionalFieldOf("experience", 0.0F).forGetter(recipe -> recipe.experience),
            Codec.INT.optionalFieldOf("cookingtime", 200).forGetter(recipe -> recipe.cookingTime)
    ).apply(instance, (group, primary, secondary, count, resultId, experience, cookingTime) ->
            new CompositeSmeltingRecipe(group, primary, secondary,
                    new ItemStack(BuiltInRegistries.ITEM.get(new ResourceLocation(resultId)), count), experience, cookingTime)));

    public CompositeSmeltingRecipe(
            String pGroup, Ingredient primaryIngredient, Ingredient secondaryIngredient, ItemStack pResult, float pExperience, int pCookingTime
    ) {
        this.group = pGroup;
        this.primaryIngredient = primaryIngredient;
        this.secondaryIngredient = secondaryIngredient;
        this.result = pResult;
        this.experience = pExperience;
        this.cookingTime = pCookingTime;
    }

    public boolean matches(Container pContainer, Level pLevel) {
        // Order-insensitive: players may drop materials into either ingredient
        // slot (e.g. via shift-click), so accept both arrangements.
        return (this.primaryIngredient.test(pContainer.getItem(0)) && this.secondaryIngredient.test(pContainer.getItem(1)))
            || (this.primaryIngredient.test(pContainer.getItem(1)) && this.secondaryIngredient.test(pContainer.getItem(0)));
    }

    public boolean partialMatch(Container pContainer, Level pLevel) {
        return this.primaryIngredient.or(this.secondaryIngredient).test(pContainer.getItem(0));
    }

    public ItemStack assemble(Container pContainer, HolderLookup.Provider provider) {
        return this.result.copy();
    }

    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return true;
    }

    public ItemStack getResultStack() {
        return this.result.copy();
    }

public Ingredient getPrimaryIngredient() {
        return this.primaryIngredient;
    }

    public Ingredient getSecondaryIngredient() {
        return this.secondaryIngredient;
    }

    public ItemStack getResultItem(HolderLookup.Provider provider) {
        return this.result.copy();
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

        protected Serializer() {
        }

        @Override
        public MapCodec<CompositeSmeltingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, CompositeSmeltingRecipe> streamCodec() {
            return StreamCodec.of(this::toNetwork, this::fromNetwork);
        }

        private CompositeSmeltingRecipe fromNetwork(RegistryFriendlyByteBuf buf) {
            String group = buf.readUtf();
            Ingredient primary = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
            Ingredient secondary = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
            ItemStack result = ItemStack.STREAM_CODEC.decode(buf);
            float exp = buf.readFloat();
            int cookingTime = buf.readVarInt();
            return new CompositeSmeltingRecipe(group, primary, secondary, result, exp, cookingTime);
        }

        private void toNetwork(RegistryFriendlyByteBuf buf, CompositeSmeltingRecipe recipe) {
            buf.writeUtf(recipe.group);
            Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.primaryIngredient);
            Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.secondaryIngredient);
            ItemStack.STREAM_CODEC.encode(buf, recipe.result);
            buf.writeFloat(recipe.experience);
            buf.writeVarInt(recipe.cookingTime);
        }
    }
}
