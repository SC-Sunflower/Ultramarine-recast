package com.voxelutopia.ultramarine.compat.rei;

import com.voxelutopia.ultramarine.data.recipe.ChiselTableRecipe;
import com.voxelutopia.ultramarine.data.recipe.CompositeSmeltingRecipe;
import com.voxelutopia.ultramarine.data.recipe.WoodworkingRecipe;
import com.voxelutopia.ultramarine.data.RecipeManagerAccess;
import com.voxelutopia.ultramarine.data.registry.ItemRegistry;
import net.minecraft.client.Minecraft;
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
import net.minecraft.world.item.crafting.RecipeManager;
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
        // Filler predicates on the holder wrapper did not fire; register displays
        // directly from the client recipe manager instead.
        if (Minecraft.getInstance().level == null) {
            return;
        }
        RecipeManager recipeManager = RecipeManagerAccess.get(Minecraft.getInstance().level);
        if (recipeManager == null) {
            return;
        }
        recipeManager.getRecipes().forEach(holder -> {
            if (holder.value() instanceof WoodworkingRecipe r) {
                registry.add(new UltramarineDisplay(WOODWORKING.getNamespace(), WOODWORKING.getPath(),
                        List.of(EntryIngredients.ofIngredient(r.getIngredient())),
                        List.of(EntryIngredients.of(r.getResultStack()))));
            } else if (holder.value() instanceof ChiselTableRecipe r) {
                registry.add(new UltramarineDisplay(CHISEL_TABLE.getNamespace(), CHISEL_TABLE.getPath(),
                        List.of(EntryIngredients.ofIngredient(r.getMaterial()), EntryIngredients.ofIngredient(r.getTemplate())),
                        List.of(EntryIngredients.of(r.getResultItem(BasicDisplay.registryAccess())))));
            } else if (holder.value() instanceof CompositeSmeltingRecipe r) {
                registry.add(new UltramarineDisplay(COMPOSITE_SMELTING.getNamespace(), COMPOSITE_SMELTING.getPath(),
                        List.of(EntryIngredients.ofIngredient(r.getPrimaryIngredient()), EntryIngredients.ofIngredient(r.getSecondaryIngredient())),
                        List.of(EntryIngredients.of(r.getResultItem(BasicDisplay.registryAccess())))));
            }
        });
    }
}
