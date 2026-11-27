package com.voxelutopia.ultramarine.compat.rei;

import com.voxelutopia.ultramarine.data.registry.ItemRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * Shared category identifiers for the custom recipe viewers. Kept in a common
 * class so server-side code never loads client-only REI classes.
 */
public final class UltramarineReiCategories {
   public static final CategoryIdentifier<UltramarineReiDisplays.Woodworking> WOODWORKING =
         CategoryIdentifier.of("ultramarine", "woodworking");
   public static final CategoryIdentifier<UltramarineReiDisplays.ChiselTable> CHISEL_TABLE =
         CategoryIdentifier.of("ultramarine", "chisel_table");
   public static final CategoryIdentifier<UltramarineReiDisplays.CompositeSmelting> COMPOSITE_SMELTING =
         CategoryIdentifier.of("ultramarine", "composite_smelting");

   public static final Component WOODWORKING_TITLE = Component.translatable("category.ultramarine.woodworking");
   public static final Component CHISEL_TABLE_TITLE = Component.translatable("category.ultramarine.chisel_table");
   public static final Component COMPOSITE_SMELTING_TITLE = Component.translatable("category.ultramarine.composite_smelting");

   private UltramarineReiCategories() {
   }
}
