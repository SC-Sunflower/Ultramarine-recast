package com.voxelutopia.ultramarine.compat.rei;

import com.voxelutopia.ultramarine.data.registry.ItemRegistry;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * Client-side REI plugin for Ultramarine Recast: registers the recipe viewer
 * categories and their workstations. The displays themselves are filled on the
 * server ({@link UltramarineReiCommonPlugin}) and synced to the client.
 */
public class UltramarineReiPlugin implements REIClientPlugin {
    public static final me.shedaniel.rei.api.common.category.CategoryIdentifier<UltramarineDisplay> WOODWORKING = UltramarineReiCategories.WOODWORKING;
    public static final me.shedaniel.rei.api.common.category.CategoryIdentifier<UltramarineDisplay> CHISEL_TABLE = UltramarineReiCategories.CHISEL_TABLE;
    public static final me.shedaniel.rei.api.common.category.CategoryIdentifier<UltramarineDisplay> COMPOSITE_SMELTING = UltramarineReiCategories.COMPOSITE_SMELTING;

    @Override
    public void registerCategories(CategoryRegistry registry) {
        registry.add(new UltramarineCategory<>(WOODWORKING, Component.translatable("container.woodworking_workbench"), new ItemStack(ItemRegistry.WOODWORKING_WORKBENCH)));
        registry.add(new UltramarineCategory<>(CHISEL_TABLE, Component.translatable("container.chisel_table"), new ItemStack(ItemRegistry.CHISEL_TABLE)));
        registry.add(new UltramarineCategory<>(COMPOSITE_SMELTING, Component.translatable("container.brick_kiln"), new ItemStack(ItemRegistry.BRICK_KILN)));

        registry.addWorkstations(WOODWORKING, EntryIngredients.of(new ItemStack(ItemRegistry.WOODWORKING_WORKBENCH)));
        registry.addWorkstations(CHISEL_TABLE, EntryIngredients.of(new ItemStack(ItemRegistry.CHISEL_TABLE)));
        registry.addWorkstations(COMPOSITE_SMELTING, EntryIngredients.of(new ItemStack(ItemRegistry.BRICK_KILN)));
    }
}