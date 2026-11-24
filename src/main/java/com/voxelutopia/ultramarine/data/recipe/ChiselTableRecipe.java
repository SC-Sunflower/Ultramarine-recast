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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public class ChiselTableRecipe implements Recipe<Container> {
    protected final String group;
    protected final Ingredient material;
    protected final Ingredient template;
    protected final List<Ingredient> colors;
    protected final ItemStack result;

    public static final MapCodec<ChiselTableRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.optionalFieldOf("group", "").forGetter(recipe -> recipe.group),
            Ingredient.CODEC.fieldOf("material").forGetter(recipe -> recipe.material),
            Ingredient.CODEC.fieldOf("template").forGetter(recipe -> recipe.template),
            Ingredient.CODEC.listOf().fieldOf("colors").forGetter(recipe -> recipe.colors),
            Codec.INT.optionalFieldOf("count", 1).forGetter(recipe -> recipe.result.getCount()),
            Codec.STRING.fieldOf("result").forGetter(recipe -> BuiltInRegistries.ITEM.getKey(recipe.result.getItem()).toString())
    ).apply(instance, (group, material, template, colors, count, resultId) ->
            new ChiselTableRecipe(group, material, template, colors.toArray(new Ingredient[0]),
                    new ItemStack(BuiltInRegistries.ITEM.get(new ResourceLocation(resultId)), count))));

    public ChiselTableRecipe(String pGroup, Ingredient material, Ingredient template, Ingredient[] colors, ItemStack pResult) {
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

    public ItemStack assemble(Container pContainer, HolderLookup.Provider provider) {
        return this.result.copy();
    }

    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return true;
    }

    public ItemStack getResultItem(HolderLookup.Provider provider) {
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

        @Override
        public MapCodec<ChiselTableRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ChiselTableRecipe> streamCodec() {
            return StreamCodec.of(this::toNetwork, this::fromNetwork);
        }

        private ChiselTableRecipe fromNetwork(RegistryFriendlyByteBuf buf) {
            String group = buf.readUtf();
            Ingredient material = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
            Ingredient template = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
            Ingredient[] colors = new Ingredient[4];
            for (int i = 0; i < 4; i++) {
                colors[i] = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
            }
            colors = Arrays.stream(colors).filter(i -> !i.isEmpty()).toList().toArray(new Ingredient[0]);
            ItemStack result = ItemStack.STREAM_CODEC.decode(buf);
            return new ChiselTableRecipe(group, material, template, colors, result);
        }

        private void toNetwork(RegistryFriendlyByteBuf buf, ChiselTableRecipe recipe) {
            buf.writeUtf(recipe.group);
            Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.material);
            Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.template);
            Ingredient[] colors = new Ingredient[4];
            for (int i = 0; i < 4; i++) {
                colors[i] = i < recipe.colors.size() ? recipe.colors.get(i) : Ingredient.EMPTY;
            }
            for (int i = 0; i < 4; i++) {
                Ingredient.CONTENTS_STREAM_CODEC.encode(buf, colors[i]);
            }
            ItemStack.STREAM_CODEC.encode(buf, recipe.result);
        }
    }
}
