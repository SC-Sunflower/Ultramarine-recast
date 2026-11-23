package com.voxelutopia.ultramarine.data.storage;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.world.item.ItemStack;

/**
 * Single slot view over an {@link ItemStackHandler}.
 * Ported from the original authors' Porting Lib based code (Ultramarine 0.5.1).
 */
public class ItemStackHandlerSlot implements SingleSlotStorage<ItemVariant> {
    private final ItemStackHandler handler;
    private final int index;
    private ItemStack stack;

    ItemStackHandlerSlot(ItemStackHandler handler, int index, ItemStack stack) {
        this.handler = handler;
        this.index = index;
        this.stack = stack;
    }

    public ItemStack getStack() {
        return this.stack;
    }

    public int getIndex() {
        return this.index;
    }

    public void setStack(ItemStack stack) {
        ItemStack old = this.stack;
        this.stack = stack;
        if (!ItemStack.matches(old, stack)) {
            this.handler.onStackChanged(this);
        }
    }

    @Override
    public boolean isResourceBlank() {
        return this.stack.isEmpty();
    }

    @Override
    public ItemVariant getResource() {
        return ItemVariant.of(this.stack);
    }

    @Override
    public long getAmount() {
        return this.stack.getCount();
    }

    @Override
    public long getCapacity() {
        return this.handler.getSlotLimit(this.index);
    }

    @Override
    public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        if (resource.isBlank() || maxAmount <= 0) {
            return 0;
        }
        int slotLimit = this.handler.getSlotLimit(this.index);
        if (!this.stack.isEmpty()) {
            if (!ItemVariant.of(this.stack.getItem()).equals(resource)) {
                return 0;
            }
            slotLimit -= this.stack.getCount();
        }
        long toInsert = Math.min(maxAmount, Math.min(slotLimit, resource.toStack(1).getMaxStackSize()));
        if (toInsert <= 0) {
            return 0;
        }
        ItemStack old = this.stack;
        ItemStack newStack = old.isEmpty() ? resource.toStack((int) toInsert) : old.copyWithCount(old.getCount() + (int) toInsert);
        this.setStack(newStack);
        if (transaction != null) {
            transaction.addCloseCallback((tx, result) -> {
                if (result != Transaction.Result.COMMITTED) {
                    this.setStack(old);
                }
            });
        }
        return toInsert;
    }

    @Override
    public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        if (resource.isBlank() || maxAmount <= 0 || this.stack.isEmpty() || !ItemVariant.of(this.stack.getItem()).equals(resource)) {
            return 0;
        }
        long toExtract = Math.min(maxAmount, this.stack.getCount());
        if (toExtract <= 0) {
            return 0;
        }
        ItemStack old = this.stack;
        ItemStack newStack = old.copyWithCount(old.getCount() - (int) toExtract);
        this.setStack(newStack);
        if (transaction != null) {
            transaction.addCloseCallback((tx, result) -> {
                if (result != Transaction.Result.COMMITTED) {
                    this.setStack(old);
                }
            });
        }
        return toExtract;
    }
}
