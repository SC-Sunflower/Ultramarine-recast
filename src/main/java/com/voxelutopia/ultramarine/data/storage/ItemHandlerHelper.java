package com.voxelutopia.ultramarine.data.storage;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Item helper utilities used by the original authors' Porting Lib based code.
 */
public final class ItemHandlerHelper {
    private ItemHandlerHelper() {
    }

    public static void giveItemToPlayer(Player player, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        ItemStack remainder = stack.copy();
        if (!player.getInventory().add(remainder) && !remainder.isEmpty()) {
            player.drop(remainder, false);
        }
    }
}
