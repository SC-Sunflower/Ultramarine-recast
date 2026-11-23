package com.voxelutopia.ultramarine.data.storage;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;

/**
 * Simple item stack storage backed by an {@link ItemStack} array.
 * Ported from the original authors' Porting Lib based code (Ultramarine 0.5.1) so the mod
 * does not need Porting Lib on Minecraft 1.20.2+.
 */
public class ItemStackHandler implements SlottedStackStorage {
    private final List<ItemStackHandlerSlot> slots;
    private final List<SingleSlotStorage<ItemVariant>> slotViews;

    public ItemStackHandler(int size) {
        this(createStacks(size));
    }

    public ItemStackHandler(ItemStack[] stacks) {
        this.slots = new ArrayList<>(stacks.length);
        for (int i = 0; i < stacks.length; i++) {
            this.slots.add(new ItemStackHandlerSlot(this, i, stacks[i]));
        }
        this.slotViews = new ArrayList<>(this.slots);
    }

    public int getSlotCount() {
        return this.slots.size();
    }

    public ItemStackHandlerSlot getSlot(int slot) {
        return this.slots.get(slot);
    }

    @Override
    public List<SingleSlotStorage<ItemVariant>> getSlots() {
        return this.slotViews;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return this.slots.get(slot).getStack();
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        this.slots.get(slot).setStack(stack);
    }

    public ItemVariant getVariantInSlot(int slot) {
        return ItemVariant.of(this.getStackInSlot(slot));
    }

    @Override
    public int getSlotLimit(int slot) {
        return this.getStackInSlot(slot).getMaxStackSize();
    }

    public int getStackLimit(int slot, ItemVariant resource) {
        return Math.min(this.getSlotLimit(slot), (int) resource.getItem().getMaxStackSize());
    }

    @Override
    public boolean isItemValid(int slot, ItemVariant resource, int count) {
        return true;
    }

    protected void onContentsChanged(int slot) {
    }

    void onStackChanged(ItemStackHandlerSlot slot) {
        this.onContentsChanged(slot.getIndex());
    }

    protected void onLoad() {
    }

    public boolean empty() {
        for (ItemStackHandlerSlot slot : this.slots) {
            if (!slot.getStack().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public void setSize(int size) {
        this.slots.clear();
        for (int i = 0; i < size; i++) {
            this.slots.add(new ItemStackHandlerSlot(this, i, ItemStack.EMPTY));
        }
        this.slotViews.clear();
        this.slotViews.addAll(this.slots);
    }

    @Override
    public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        long inserted = 0;
        for (ItemStackHandlerSlot slot : this.slots) {
            inserted += slot.insert(resource, maxAmount - inserted, transaction);
            if (inserted >= maxAmount) {
                break;
            }
        }
        return inserted;
    }

    @Override
    public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        long extracted = 0;
        for (ItemStackHandlerSlot slot : this.slots) {
            extracted += slot.extract(resource, maxAmount - extracted, transaction);
            if (extracted >= maxAmount) {
                break;
            }
        }
        return extracted;
    }

    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("Size", this.slots.size());
        ListTag items = new ListTag();
        for (ItemStackHandlerSlot slot : this.slots) {
            if (!slot.getStack().isEmpty()) {
                CompoundTag tag = new CompoundTag();
                tag.putInt("Slot", slot.getIndex());
                slot.getStack().save(tag);
                items.add(tag);
            }
        }
        nbt.put("Items", items);
        return nbt;
    }

    public void deserializeNBT(CompoundTag nbt) {
        this.setSize(nbt.contains("Size") ? nbt.getInt("Size") : this.slots.size());
        ListTag items = nbt.getList("Items", 10);
        for (int i = 0; i < items.size(); i++) {
            CompoundTag tag = items.getCompound(i);
            int index = tag.getInt("Slot");
            if (index >= 0 && index < this.slots.size()) {
                this.slots.get(index).setStack(ItemStack.of(tag));
            }
        }
        this.onLoad();
    }

    private static ItemStack[] createStacks(int size) {
        ItemStack[] stacks = new ItemStack[size];
        for (int i = 0; i < size; i++) {
            stacks[i] = ItemStack.EMPTY;
        }
        return stacks;
    }
}
