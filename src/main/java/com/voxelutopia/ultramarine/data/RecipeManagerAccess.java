package com.voxelutopia.ultramarine.data;

import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * Server-side recipe manager access. The client menu does not rely on this:
 * the client may hold a synced recipe manager (integrated server / dedicated
 * connection sync), but the woodworking menu receives its candidates from the
 * server via {@code WoodworkingRecipesS2CPayload} instead.
 */
public final class RecipeManagerAccess {
    private RecipeManagerAccess() {
    }

    @Nullable
    public static RecipeManager get(Level level) {
        if (level.isClientSide()) {
            return null;
        }
        return level.getServer() != null ? level.getServer().getRecipeManager() : null;
    }
}
