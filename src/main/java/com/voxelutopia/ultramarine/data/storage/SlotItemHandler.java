package com.voxelutopia.ultramarine.data.storage;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Menu slot backed by a {@link SlottedStorage}.
 * Ported from the original authors' Porting Lib based code (Ultramarine 0.5.1).
 */
public class SlotItemHandler extends Slot {
    private static final Container EMPTY_INVENTORY = new Container() {
        @Override
        public int getContainerSize() {
            return 0;
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @NotNull
        @Override
        public ItemStack getItem(int index) {
            return ItemStack.EMPTY;
        }

        @NotNull
        @Override
        public ItemStack removeItem(int index, int count) {
            return ItemStack.EMPTY;
        }

        @NotNull
        @Override
        public ItemStack removeItemNoUpdate(int index) {
            return ItemStack.EMPTY;
        }

        @Override
        public void setItem(int index, @NotNull ItemStack stack) {
        }

        @Override
        public void setChanged() {
        }

        @Override
        public boolean stillValid(@NotNull Player player) {
            return false;
        }

        @Override
        public void clearContent() {
        }
    };
    private final SlottedStorage<ItemVariant> storage;
    private final int index;

    public SlotItemHandler(SlottedStackStorage storage, int index, int xPosition, int yPosition) {
        super(EMPTY_INVENTORY, index, xPosition, yPosition);
        this.storage = storage;
        this.index = index;
    }

    public SlotItemHandler(SlottedStorage<ItemVariant> storage, int index, int xPosition, int yPosition) {
        super(EMPTY_INVENTORY, index, xPosition, yPosition);
        this.storage = storage;
        this.index = index;
    }

    @Override
    public boolean mayPlace(@NotNull ItemStack stack) {
        return !stack.isEmpty()
                && (!(this.storage instanceof SlottedStackStorage slottedStorage)
                || slottedStorage.isItemValid(this.index, ItemVariant.of(stack), stack.getCount()));
    }

    @NotNull
    @Override
    public ItemStack getItem() {
        if (this.storage instanceof SlottedStackStorage slottedStorage) {
            return slottedStorage.getStackInSlot(this.index);
        }
        SingleSlotStorage<ItemVariant> slot = this.storage.getSlot(this.index);
        return slot.getResource().toStack((int) slot.getAmount());
    }

    @Override
    public void set(@NotNull ItemStack stack) {
        if (this.storage instanceof SlottedStackStorage slottedStorage) {
            slottedStorage.setStackInSlot(this.index, stack);
        }
        this.setChanged();
    }

    @Override
    public void setChanged() {
    }

    @Override
    public int getMaxStackSize() {
        return this.storage instanceof SlottedStackStorage slottedStorage
                ? slottedStorage.getSlotLimit(this.index)
                : (int) this.storage.getSlot(this.index).getCapacity();
    }

    @Override
    public int getMaxStackSize(@NotNull ItemStack stack) {
        return this.getMaxStackSize();
    }

    @Override
    public boolean mayPickup(@NotNull Player player) {
        return this.storage instanceof SlottedStackStorage slottedStorage
                ? !slottedStorage.getStackInSlot(this.index).isEmpty()
                : !this.storage.getSlot(this.index).isResourceBlank();
    }

    @NotNull
    @Override
    public ItemStack remove(int amount) {
        if (this.storage instanceof SlottedStackStorage slottedStorage) {
            ItemStack held = slottedStorage.getStackInSlot(this.index).copy();
            ItemStack removed = held.split(amount);
            slottedStorage.setStackInSlot(this.index, held);
            return removed;
        }
        SingleSlotStorage<ItemVariant> slot = this.storage.getSlot(this.index);
        ItemStack lastResource = slot.getResource().toStack();
        long extracted = slot.extract(slot.getResource(), amount, null);
        return lastResource.copyWithCount((int) extracted);
    }

    public SlottedStorage<ItemVariant> getItemHandler() {
        return this.storage;
    }

    public int getSlotIndex() {
        return this.index;
    }
}
