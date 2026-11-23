package com.voxelutopia.ultramarine.data;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ToolMaterial;

/**
 * Tool materials. Minecraft 1.21.2 replaced the {@code Tier} interface with the
 * {@link ToolMaterial} record; repair ingredients are now item tags.
 */
public final class ModTiers {
    public static final ToolMaterial BLUE_AND_WHITE_PORCELAIN = new ToolMaterial(
            BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 16, 14.0F, 5.0F, 24, ModItemTags.BLUE_AND_WHITE_PORCELAIN_REPAIR);

    private ModTiers() {
    }
}
