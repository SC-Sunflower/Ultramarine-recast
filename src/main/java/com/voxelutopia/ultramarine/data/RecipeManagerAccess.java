package com.voxelutopia.ultramarine.data;

import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * Minecraft 1.21.2 removed {@code Level#getRecipeManager}; recipes are fetched from the
 * server-side {@link RecipeManager}. The client no longer holds a full recipe manager
 * (it only receives the synced stonecutter set and item properties), so this returns
 * {@code null} on the client — callers must either run server-side or rely on menu
 * slot synchronization / dedicated sync payloads.
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
