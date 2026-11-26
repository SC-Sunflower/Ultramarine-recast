package com.voxelutopia.ultramarine.compat.rei;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;

/**
 * Category identifiers shared by the common (server) and client REI plugins.
 * Kept in a separate class so the server-side plugin never loads client-only classes.
 */
public final class UltramarineReiCategories {
    public static final CategoryIdentifier<UltramarineDisplay> WOODWORKING = CategoryIdentifier.of("ultramarine", "woodworking");
    public static final CategoryIdentifier<UltramarineDisplay> CHISEL_TABLE = CategoryIdentifier.of("ultramarine", "chisel_table");
    public static final CategoryIdentifier<UltramarineDisplay> COMPOSITE_SMELTING = CategoryIdentifier.of("ultramarine", "composite_smelting");

    private UltramarineReiCategories() {
    }
}