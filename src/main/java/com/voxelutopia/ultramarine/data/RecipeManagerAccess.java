package com.voxelutopia.ultramarine.data;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * Minecraft 1.21.2 removed {@code Level#getRecipeManager}; recipes are fetched from the
 * server-side {@link RecipeManager} (or the client's synced copy on the integrated client).
 */
public final class RecipeManagerAccess {
    private RecipeManagerAccess() {
    }

    @Nullable
    public static RecipeManager get(Level level) {
        if (!level.isClientSide()) {
            return level.getServer() != null ? level.getServer().getRecipeManager() : null;
        }
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.getSingleplayerServer() != null ? minecraft.getSingleplayerServer().getRecipeManager() : null;
    }
}
