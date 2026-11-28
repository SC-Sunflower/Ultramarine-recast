package com.voxelutopia.ultramarine.world.block.menu;

import com.voxelutopia.ultramarine.data.recipe.CompositeSmeltingRecipe;

import com.voxelutopia.ultramarine.data.registry.BlockRegistry;
import com.voxelutopia.ultramarine.data.registry.MenuTypeRegistry;
import com.voxelutopia.ultramarine.data.registry.RecipeTypeRegistry;
import com.voxelutopia.ultramarine.data.storage.ItemStackHandler;
import com.voxelutopia.ultramarine.data.storage.SlotItemHandler;
import com.voxelutopia.ultramarine.data.storage.SlottedStackStorage;
import javax.annotation.Nonnull;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

public class BrickKilnMenu extends AbstractContainerMenu {
   public static final int SLOT_INPUT_PRIMARY = 0;
   public static final int SLOT_INPUT_SECONDARY = 1;
   public static final int SLOT_FUEL = 2;
   public static final int SLOT_RESULT = 3;
   private static final int INV_SLOT_START = 4;
   private static final int INV_SLOT_END = 31;
   private static final int USE_ROW_SLOT_START = 31;
   private static final int USE_ROW_SLOT_END = 40;
   private final BlockEntity blockEntity;
   private final Player playerEntity;
   private final ContainerData data;

   public BrickKilnMenu(int pId, BlockPos pos, Inventory inventory) {
      this(pId, pos, inventory, new ItemStackHandler(4), new SimpleContainerData(4));
   }

   public BrickKilnMenu(int id, BlockPos pos, Inventory inventory, SlottedStackStorage container, ContainerData containerData) {
      super(MenuTypeRegistry.BRICK_KILN, id);
      this.playerEntity = inventory.player;
      this.blockEntity = this.playerEntity.getCommandSenderWorld().getBlockEntity(pos);
      SlottedStackStorage inventory1 = new BrickKilnMenu.InventoryWrapper(inventory);
      this.data = containerData;
      this.addSlot(new BrickKilnMenu.IngredientSlot(container, 0, 46, 17));
      this.addSlot(new BrickKilnMenu.IngredientSlot(container, 1, 66, 17));
      this.addSlot(new BrickKilnMenu.FuelSlot(container, 2, 56, 53));
      this.addSlot(new BrickKilnMenu.OutputSlot(container, 3, 116, 35));

      for (int r = 0; r < 3; r++) {
         for (int c = 0; c < 9; c++) {
            this.addSlot(new SlotItemHandler(inventory1, c + r * 9 + 9, 8 + c * 18, 84 + r * 18));
         }
      }

      for (int k = 0; k < 9; k++) {
         this.addSlot(new SlotItemHandler(inventory1, k, 8 + k * 18, 142));
      }

      this.addDataSlots(this.data);
   }

   public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
      ItemStack itemstack = ItemStack.EMPTY;
      Slot slot = (Slot)this.slots.get(pIndex);
      if (slot.hasItem()) {
         ItemStack slotItem = slot.getItem();
         itemstack = slotItem.copy();
         if (pIndex == 3) {
            if (!this.moveItemStackTo(slotItem, 4, 40, true)) {
               return ItemStack.EMPTY;
            }

            slot.onQuickCraft(slotItem, itemstack);
         } else if (pIndex != 2 && pIndex != 0 && pIndex != 1) {
            if (this.canProcess(slotItem)) {
               if (!this.moveItemStackTo(slotItem, 0, 2, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (isFuel(slotItem)) {
               if (!this.moveItemStackTo(slotItem, 2, 3, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (pIndex >= 4 && pIndex < 31) {
               if (!this.moveItemStackTo(slotItem, 31, 40, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (pIndex >= 31 && pIndex < 40 && !this.moveItemStackTo(slotItem, 4, 31, false)) {
               return ItemStack.EMPTY;
            }
         } else if (!this.moveItemStackTo(slotItem, 4, 40, false)) {
            return ItemStack.EMPTY;
         }

         if (slotItem.isEmpty()) {
            slot.set(ItemStack.EMPTY);
         } else {
            slot.setChanged();
         }

         if (slotItem.getCount() == itemstack.getCount()) {
            return ItemStack.EMPTY;
         }

         slot.onTake(pPlayer, slotItem);
      }

      return itemstack;
   }

   protected boolean canProcess(ItemStack item) {
      return this.blockEntity != null && this.blockEntity.getLevel() != null
         ? this.blockEntity
            .getLevel()
            .getRecipeManager()
            .getAllRecipesFor(RecipeTypeRegistry.COMPOSITE_SMELTING)
            .stream()
            .anyMatch(holder -> ((CompositeSmeltingRecipe)holder.value()).partialMatch(new SimpleContainer(new ItemStack[]{item}), this.blockEntity.getLevel()))
         : false;
   }

   public boolean stillValid(Player pPlayer) {
      return this.blockEntity != null && this.blockEntity.getLevel() != null
         ? stillValid(
            ContainerLevelAccess.create(this.blockEntity.getLevel(), this.blockEntity.getBlockPos()), this.playerEntity, BlockRegistry.BRICK_KILN
         )
         : false;
   }

   public boolean isLit() {
      return this.data.get(0) > 0;
   }

   public int getBurnProgress() {
      int i = this.data.get(2);
      int j = this.data.get(3);
      return j != 0 && i != 0 ? i * 24 / j : 0;
   }

   public int getLitProgress() {
      int i = this.data.get(1);
      if (i == 0) {
         i = 200;
      }

      return this.data.get(0) * 13 / i;
   }

   private static boolean isFuel(@NotNull ItemStack stack) {
      return FuelRegistry.INSTANCE.get(stack.getItem()) != null;
   }

   static class FuelSlot extends SlotItemHandler {
      public FuelSlot(SlottedStackStorage itemHandler, int index, int xPosition, int yPosition) {
         super(itemHandler, index, xPosition, yPosition);
      }

      public boolean mayPlace(@Nonnull ItemStack stack) {
         return BrickKilnMenu.isFuel(stack);
      }
   }

   static class IngredientSlot extends SlotItemHandler {
      public IngredientSlot(SlottedStackStorage itemHandler, int index, int xPosition, int yPosition) {
         super(itemHandler, index, xPosition, yPosition);
      }
   }

   public static class InventoryWrapper implements SlottedStackStorage {
      private final Inventory inventory;

      public InventoryWrapper(Inventory inventory) {
         this.inventory = inventory;
      }

      public int getSlotCount() {
         return this.inventory.getContainerSize();
      }

      public SingleSlotStorage<ItemVariant> getSlot(int slot) {
         return slot >= 0 && slot < this.inventory.getContainerSize() ? new SingleSlotStorage<ItemVariant>() {
            public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
               ItemStack stack = InventoryWrapper.this.inventory.getItem(slot);
               if (stack.isEmpty()) {
                  int toInsert = (int)Math.min(maxAmount, InventoryWrapper.this.inventory.getMaxStackSize());
                  ItemStack original = stack.copy();
                  InventoryWrapper.this.inventory.setItem(slot, resource.toStack(toInsert));
                  transaction.addCloseCallback((context, result) -> {
                     if (result.wasAborted()) {
                        InventoryWrapper.this.inventory.setItem(slot, original);
                     }
                  });
                  return toInsert;
               }

               if (ItemVariant.of(stack).equals(resource)) {
                  int canInsert = Math.min((int)maxAmount, InventoryWrapper.this.inventory.getMaxStackSize() - stack.getCount());
                  if (canInsert > 0) {
                     ItemStack original = stack.copy();
                     stack.grow(canInsert);
                     transaction.addCloseCallback((context, result) -> {
                        if (result.wasAborted()) {
                           InventoryWrapper.this.inventory.setItem(slot, original);
                        }
                     });
                     return canInsert;
                  }
               }

               return 0L;
            }


            public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
               ItemStack stack = InventoryWrapper.this.inventory.getItem(slot);
               if (!stack.isEmpty() && ItemVariant.of(stack).equals(resource)) {
                  int toExtract = (int)Math.min(maxAmount, stack.getCount());
                  ItemStack original = stack.copy();
                  stack.shrink(toExtract);
                  if (stack.isEmpty()) {
                     InventoryWrapper.this.inventory.setItem(slot, ItemStack.EMPTY);
                  }

                  transaction.addCloseCallback((context, result) -> {
                     if (result.wasAborted()) {
                        InventoryWrapper.this.inventory.setItem(slot, original);
                     }
                  });
                  return toExtract;
               } else {
                  return 0L;
               }
            }


            public boolean isResourceBlank() {
               return InventoryWrapper.this.inventory.getItem(slot).isEmpty();
            }

            public ItemVariant getResource() {
               return ItemVariant.of(InventoryWrapper.this.inventory.getItem(slot));
            }

            public long getAmount() {
               return InventoryWrapper.this.inventory.getItem(slot).getCount();
            }

            public long getCapacity() {
               return InventoryWrapper.this.inventory.getMaxStackSize();
            }
         } : null;
      }

      public ItemStack getStackInSlot(int slot) {
         return this.inventory.getItem(slot);
      }

      public void setStackInSlot(int slot, ItemStack stack) {
         this.inventory.setItem(slot, stack);
      }

      public int getSlotLimit(int slot) {
         return this.inventory.getMaxStackSize();
      }

      public boolean isItemValid(int slot, ItemVariant resource, int count) {
         return true;
      }

      public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
         long inserted = 0L;

         for (int i = 0; i < this.inventory.getContainerSize() && inserted < maxAmount; i++) {
            SingleSlotStorage<ItemVariant> slot = this.getSlot(i);
            if (slot != null) {
               inserted += slot.insert(resource, maxAmount - inserted, transaction);
            }
         }

         return inserted;
      }

      public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
         long extracted = 0L;

         for (int i = 0; i < this.inventory.getContainerSize() && extracted < maxAmount; i++) {
            SingleSlotStorage<ItemVariant> slot = this.getSlot(i);
            if (slot != null) {
               extracted += slot.extract(resource, maxAmount - extracted, transaction);
            }
         }

         return extracted;
      }
   }

   static class OutputSlot extends SlotItemHandler {
      public OutputSlot(SlottedStackStorage itemHandler, int index, int xPosition, int yPosition) {
         super(itemHandler, index, xPosition, yPosition);
      }

      public boolean mayPlace(@Nonnull ItemStack stack) {
         return false;
      }
   }
}
