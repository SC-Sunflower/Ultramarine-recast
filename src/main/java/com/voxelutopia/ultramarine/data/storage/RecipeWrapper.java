package com.voxelutopia.ultramarine.data.storage;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Container view over an {@link ItemStackHandler}, used for recipe matching.
 * Ported from the original authors' Porting Lib based code (Ultramarine 0.5.1).
 */
public class RecipeWrapper implements Container {
    protected final ItemStackHandler handler;

    public RecipeWrapper(ItemStackHandler handler) {
        this.handler = handler;
    }

    @Override
    public int getContainerSize() {
        return this.handler.getSlotCount();
    }

    @Override
    public boolean isEmpty() {
        return this.handler.empty();
    }

    @Override
    public ItemStack getItem(int index) {
        return this.handler.getStackInSlot(index);
    }

    @Override
    public ItemStack removeItem(int index, int count) {
        if (index >= 0 && index < this.handler.getSlotCount()) {
            ItemStack current = this.handler.getStackInSlot(index);
            if (current.isEmpty()) {
                return ItemStack.EMPTY;
            }
            ItemStack copy = current.copy();
            ItemStack extracted = copy.split(count);
            this.handler.setStackInSlot(index, copy);
            return extracted;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        return this.removeItem(index, Integer.MAX_VALUE);
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        this.handler.setStackInSlot(index, stack);
    }

    @Override
    public void setChanged() {
    }

    @Override
    public boolean stillValid(Player player) {
        return false;
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < this.handler.getSlotCount(); i++) {
            this.handler.setStackInSlot(i, ItemStack.EMPTY);
        }
    }

    @Override
    public int getMaxStackSize() {
        return 64;
    }
}
