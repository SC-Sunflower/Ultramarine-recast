package com.voxelutopia.ultramarine.data.storage;

import java.util.Iterator;
import java.util.List;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.world.item.ItemStack;

/**
 * Slotted item storage view used by menus and block entities.
 * Ported from the original authors' Porting Lib based code (Ultramarine 0.5.1) so the mod
 * does not need Porting Lib on Minecraft 1.20.2+.
 */
public interface SlottedStackStorage extends SlottedStorage<ItemVariant> {
    ItemStack getStackInSlot(int slot);

    void setStackInSlot(int slot, ItemStack stack);

    int getSlotLimit(int slot);

    default boolean isItemValid(int slot, ItemVariant resource, int count) {
        return true;
    }

    default long insertSlot(int slot, ItemVariant resource, long maxAmount, TransactionContext transaction) {
        return this.getSlot(slot).insert(resource, maxAmount, transaction);
    }

    default long extractSlot(int slot, ItemVariant resource, long maxAmount, TransactionContext transaction) {
        return this.getSlot(slot).extract(resource, maxAmount, transaction);
    }

    @Override
    default Iterator<StorageView<ItemVariant>> iterator() {
        return (Iterator<StorageView<ItemVariant>>) (Iterator<?>) this.getSlots().iterator();
    }
}
