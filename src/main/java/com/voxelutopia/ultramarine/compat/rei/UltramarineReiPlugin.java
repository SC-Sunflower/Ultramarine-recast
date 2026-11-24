package com.voxelutopia.ultramarine.compat.rei;

import com.voxelutopia.ultramarine.data.recipe.ChiselTableRecipe;
import com.voxelutopia.ultramarine.data.recipe.CompositeSmeltingRecipe;
import com.voxelutopia.ultramarine.data.recipe.WoodworkingRecipe;
import com.voxelutopia.ultramarine.data.registry.ItemRegistry;
import java.util.List;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

/**
 * REI integration for Ultramarine Recast: shows the woodworking table,
 * chisel table and composite smelting recipes in the recipe viewer.
 */
public class UltramarineReiPlugin implements REIClientPlugin {
    public static final CategoryIdentifier<UltramarineDisplay> WOODWORKING = CategoryIdentifier.of("ultramarine", "woodworking");
    public static final CategoryIdentifier<UltramarineDisplay> CHISEL_TABLE = CategoryIdentifier.of("ultramarine", "chisel_table");
    public static final CategoryIdentifier<UltramarineDisplay> COMPOSITE_SMELTING = CategoryIdentifier.of("ultramarine", "composite_smelting");

    @Override
    public void registerCategories(CategoryRegistry registry) {
        registry.add(new UltramarineCategory<>(WOODWORKING, Component.translatable("container.woodworking_workbench"), new ItemStack(ItemRegistry.WOODWORKING_WORKBENCH)));
        registry.add(new UltramarineCategory<>(CHISEL_TABLE, Component.translatable("container.chisel_table"), new ItemStack(ItemRegistry.CHISEL_TABLE)));
        registry.add(new UltramarineCategory<>(COMPOSITE_SMELTING, Component.translatable("container.brick_kiln"), new ItemStack(ItemRegistry.BRICK_KILN)));
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        registry.registerDisplaysFillerWithReason(
                (recipe, reasons) -> recipe instanceof RecipeHolder<?> holder && holder.value() instanceof WoodworkingRecipe,
                (recipe, reasons) -> {
                    RecipeHolder<?> holder = (RecipeHolder<?>) recipe;
                    WoodworkingRecipe r = (WoodworkingRecipe) holder.value();
                    return List.of(new UltramarineDisplay(WOODWORKING.getNamespace(), WOODWORKING.getPath(),
                            List.of(EntryIngredients.ofIngredient(r.getIngredient())),
                            List.of(EntryIngredients.of(r.getResultStack()))));
                }
        );
        registry.registerDisplaysFillerWithReason(
                (recipe, reasons) -> recipe instanceof RecipeHolder<?> holder && holder.value() instanceof ChiselTableRecipe,
                (recipe, reasons) -> {
                    RecipeHolder<?> holder = (RecipeHolder<?>) recipe;
                    ChiselTableRecipe r = (ChiselTableRecipe) holder.value();
                    return List.of(new UltramarineDisplay(CHISEL_TABLE.getNamespace(), CHISEL_TABLE.getPath(),
                            List.of(EntryIngredients.ofIngredient(r.getMaterial()), EntryIngredients.ofIngredient(r.getTemplate())),
                            List.of(EntryIngredients.of(r.getResultItem(BasicDisplay.registryAccess())))));
                }
        );
        registry.registerDisplaysFillerWithReason(
                (recipe, reasons) -> recipe instanceof RecipeHolder<?> holder && holder.value() instanceof CompositeSmeltingRecipe,
                (recipe, reasons) -> {
                    RecipeHolder<?> holder = (RecipeHolder<?>) recipe;
                    CompositeSmeltingRecipe r = (CompositeSmeltingRecipe) holder.value();
                    return List.of(new UltramarineDisplay(COMPOSITE_SMELTING.getNamespace(), COMPOSITE_SMELTING.getPath(),
                            List.of(EntryIngredients.ofIngredient(r.getPrimaryIngredient()), EntryIngredients.ofIngredient(r.getSecondaryIngredient())),
                            List.of(EntryIngredients.of(r.getResultItem(BasicDisplay.registryAccess())))));
                }
        );
    }
}
