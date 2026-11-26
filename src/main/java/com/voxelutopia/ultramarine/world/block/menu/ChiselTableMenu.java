package com.voxelutopia.ultramarine.world.block.menu;

import com.voxelutopia.ultramarine.data.RecipeManagerAccess;

import com.voxelutopia.ultramarine.Ultramarine;
import com.voxelutopia.ultramarine.data.ModItemTags;
import com.voxelutopia.ultramarine.data.recipe.ChiselTableRecipe;
import com.voxelutopia.ultramarine.data.registry.BlockRegistry;
import com.voxelutopia.ultramarine.data.registry.MenuTypeRegistry;
import com.voxelutopia.ultramarine.data.registry.RecipeTypeRegistry;
import com.voxelutopia.ultramarine.data.storage.ItemStackHandler;
import com.voxelutopia.ultramarine.data.storage.SlotItemHandler;
import com.voxelutopia.ultramarine.data.storage.SlottedStackStorage;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ChiselTableMenu extends AbstractContainerMenu {
   public static final int SLOT_MATERIAL = 0;
   public static final int SLOT_TEMPLATE = 1;
   public static final int SLOT_COLOR_START = 2;
   public static final int SLOT_COLOR_END = 6;
   public static final int SLOT_RESULT = 6;
   private static final int INV_SLOT_START = 7;
   private static final int INV_SLOT_END = 34;
   private static final int USE_ROW_SLOT_START = 34;
   private static final int USE_ROW_SLOT_END = 43;
   private static final Predicate<ItemStack> IS_WOOD = i -> i.is(ItemTags.LOGS) || i.is(ModItemTags.POLISHED_PLANKS);
   private static final Predicate<ItemStack> IS_TEMPLATE = i -> i.is(ModItemTags.CHISEL_TEMPLATES);
   private static final Predicate<ItemStack> IS_COLOR = i -> i.is(ModItemTags.DYES) || i.is(ModItemTags.DYE_POWDER);
   private final ContainerLevelAccess access;
   private final Player player;
   private final ItemStackHandler crafting = new ItemStackHandler(6);
   private final ItemStackHandler result = new ItemStackHandler(1);
   private final SlottedStackStorage inventory;

   public ChiselTableMenu(int id, Inventory inventory) {
      this(id, inventory, ContainerLevelAccess.NULL);
   }

   public ChiselTableMenu(int id, Inventory inventory, ContainerLevelAccess access) {
      super(MenuTypeRegistry.CHISEL_TABLE, id);
      this.inventory = new ChiselTableMenu.InventoryWrapper(inventory);
      this.access = access;
      this.player = inventory.player;
      this.addSlot(new ChiselTableMenu.MaterialSlot(this.crafting, 0, 26, 25));
      this.addSlot(new ChiselTableMenu.TemplateSlot(this.crafting, 1, 53, 25));
      int i = 2;

      for (int j = 0; i < 6; j++) {
         this.addSlot(new ChiselTableMenu.DyeSlot(this.crafting, i, 26 + j * 18, 52));
         i++;
      }

      this.addSlot(new ChiselTableMenu.OutputSlot(this.result, 0, 130, 34));

      for (int r = 0; r < 3; r++) {
         for (int c = 0; c < 9; c++) {
            this.addSlot(new SlotItemHandler(this.inventory, c + r * 9 + 9, 8 + c * 18, 84 + r * 18));
         }
      }

      for (int k = 0; k < 9; k++) {
         this.addSlot(new SlotItemHandler(this.inventory, k, 8 + k * 18, 142));
      }
   }

   public void slotsChanged(SlotItemHandler slot) {
      this.broadcastChanges();
      if (slot.getSlotIndex() <= 6) {
         this.createResult();
      }
   }

   public void createResult() {
      Level level = this.player.level();
      if (level.isClientSide()) {
         // 1.21.2: the client has no recipe manager; the server computes the result
         // and the slot change is synced to the client automatically.
         return;
      }
      RecipeManager recipeManager = RecipeManagerAccess.get(level);
      if (recipeManager == null) {
         return;
      }
      Container ingredients = this.wrapIngredients();
      CraftingInput input = CraftingInput.of(2, 3, java.util.stream.IntStream.range(0, 6).mapToObj(ingredients::getItem).toList());
      List<RecipeHolder<ChiselTableRecipe>> list = recipeManager.getRecipes().stream()
            .filter(holder -> holder.value() instanceof ChiselTableRecipe)
            .map(holder -> (RecipeHolder<ChiselTableRecipe>)holder)
            .filter(holder -> ((ChiselTableRecipe)holder.value()).matches(input, level))
            .toList();
      if (list.size() > 1) {
         Ultramarine.LOGGER.warn("Duplicate chisel table recipe: ");
         list.forEach(recipe -> Ultramarine.LOGGER.warn(recipe.id().identifier().getPath()));
      }

      if (list.isEmpty()) {
         this.result.setStackInSlot(0, ItemStack.EMPTY);
      } else {
         ChiselTableRecipe recipe = (ChiselTableRecipe)list.get(0).value();
         ItemStack resultItemStack = recipe.assemble(input);
         this.result.setStackInSlot(0, resultItemStack);
      }

      this.broadcastChanges();
   }

   protected void onTake(Player player, ItemStack itemStack, SlotItemHandler slot) {
      for (int i = 0; i < 6; i++) {
         ItemStack item = this.crafting.getStackInSlot(i);
         this.crafting.setStackInSlot(i, item);
         if (i != 1) {
            item.shrink(1);
         }
      }

      this.slotsChanged(slot);
   }

   public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
      ItemStack itemstack = ItemStack.EMPTY;
      Slot slot = (Slot)this.slots.get(pIndex);
      if (slot.hasItem()) {
         ItemStack slotItem = slot.getItem();
         itemstack = slotItem.copy();
         if (pIndex == 6) {
            slotItem.getItem().onCraftedBy(slotItem, pPlayer);
            if (!this.moveItemStackTo(slotItem, 7, 43, true)) {
               return ItemStack.EMPTY;
            }

            slot.onQuickCraft(slotItem, itemstack);
         } else if (pIndex > 6) {
            if (IS_WOOD.test(slotItem)) {
               if (!this.moveItemStackTo(slotItem, 0, 1, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (IS_TEMPLATE.test(slotItem)) {
               if (!this.moveItemStackTo(slotItem, 1, 2, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (IS_COLOR.test(slotItem)) {
               if (!this.moveItemStackTo(slotItem, 2, 6, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (pIndex < 34) {
               if (!this.moveItemStackTo(slotItem, 34, 43, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (pIndex < 43 && !this.moveItemStackTo(slotItem, 7, 34, false)) {
               return ItemStack.EMPTY;
            }
         } else if (!this.moveItemStackTo(slotItem, 7, 43, false)) {
            return ItemStack.EMPTY;
         }

         if (slotItem.isEmpty()) {
            slot.set(ItemStack.EMPTY);
         }

         slot.setChanged();
         if (slotItem.getCount() == itemstack.getCount()) {
            return ItemStack.EMPTY;
         }

         slot.onTake(pPlayer, slotItem);
         this.broadcastChanges();
      }

      return itemstack;
   }

   public boolean canTakeItemForPickAll(ItemStack pStack, Slot pSlot) {
      return ((SlotItemHandler)pSlot).getItemHandler() != this.result && super.canTakeItemForPickAll(pStack, pSlot);
   }

   public void removed(Player pPlayer) {
      super.removed(pPlayer);
      this.access.execute((level, blockPos) -> this.clearContainer(pPlayer, this.wrapIngredients()));
   }

   private Container wrapIngredients() {
      SimpleContainer container = new SimpleContainer(6);

      for (int i = 0; i < 6; i++) {
         container.setItem(i, this.crafting.getStackInSlot(i));
      }

      return container;
   }

   public boolean stillValid(Player pPlayer) {
      return stillValid(this.access, pPlayer, BlockRegistry.CHISEL_TABLE);
   }

   class DyeSlot extends ChiselTableMenu.IngredientSlot {
      public DyeSlot(SlottedStackStorage itemHandler, int index, int xPosition, int yPosition) {
         super(itemHandler, index, xPosition, yPosition);
      }

      public boolean mayPlace(@Nonnull ItemStack stack) {
         return ChiselTableMenu.IS_COLOR.test(stack);
      }
   }

   class IngredientSlot extends SlotItemHandler {
      public IngredientSlot(SlottedStackStorage itemHandler, int index, int xPosition, int yPosition) {
         super(itemHandler, index, xPosition, yPosition);
      }

      public void setChanged() {
         super.setChanged();
         ChiselTableMenu.this.slotsChanged(this);
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
                  InventoryWrapper.this.inventory.setItem(slot, resource.toStack(toInsert));
                  return toInsert;
               }

               if (ItemVariant.of(stack).equals(resource)) {
                  int canInsert = Math.min((int)maxAmount, InventoryWrapper.this.inventory.getMaxStackSize() - stack.getCount());
                  if (canInsert > 0) {
                     stack.grow(canInsert);
                     return canInsert;
                  }
               }

               return 0L;
            }

            public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
               ItemStack stack = InventoryWrapper.this.inventory.getItem(slot);
               if (!stack.isEmpty() && ItemVariant.of(stack).equals(resource)) {
                  int toExtract = (int)Math.min(maxAmount, stack.getCount());
                  stack.shrink(toExtract);
                  if (stack.isEmpty()) {
                     InventoryWrapper.this.inventory.setItem(slot, ItemStack.EMPTY);
                  }

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

   class MaterialSlot extends ChiselTableMenu.IngredientSlot {
      public MaterialSlot(SlottedStackStorage itemHandler, int index, int xPosition, int yPosition) {
         super(itemHandler, index, xPosition, yPosition);
      }

      public boolean mayPlace(@Nonnull ItemStack stack) {
         return ChiselTableMenu.IS_WOOD.test(stack);
      }
   }

   class OutputSlot extends SlotItemHandler {
      public OutputSlot(SlottedStackStorage itemHandler, int index, int xPosition, int yPosition) {
         super(itemHandler, index, xPosition, yPosition);
      }

      public boolean mayPlace(@Nonnull ItemStack stack) {
         return false;
      }

      public void onTake(Player pPlayer, ItemStack pStack) {
         super.onTake(pPlayer, pStack);
         ChiselTableMenu.this.onTake(pPlayer, pStack, this);
      }

      public void setChanged() {
         super.setChanged();
         ChiselTableMenu.this.slotsChanged(this);
      }

      public void onQuickCraft(@NotNull ItemStack oldStackIn, @NotNull ItemStack newStackIn) {
         int i = newStackIn.getCount() - oldStackIn.getCount();
         if (i > 0) {
            this.onQuickCraft(newStackIn, i);
         }
      }
   }

   class TemplateSlot extends ChiselTableMenu.IngredientSlot {
      public TemplateSlot(SlottedStackStorage itemHandler, int index, int xPosition, int yPosition) {
         super(itemHandler, index, xPosition, yPosition);
      }

      public boolean mayPlace(@Nonnull ItemStack stack) {
         return ChiselTableMenu.IS_TEMPLATE.test(stack);
      }
   }
}
