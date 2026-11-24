package com.voxelutopia.ultramarine.world.block.menu;

import com.voxelutopia.ultramarine.data.RecipeManagerAccess;

import com.google.common.collect.Lists;
import com.voxelutopia.ultramarine.data.recipe.WoodworkingRecipe;
import com.voxelutopia.ultramarine.data.registry.BlockRegistry;
import com.voxelutopia.ultramarine.data.registry.MenuTypeRegistry;
import com.voxelutopia.ultramarine.data.registry.SoundRegistry;
import com.voxelutopia.ultramarine.network.WoodworkingRecipesS2CPayload;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

public class WoodworkingWorkbenchMenu extends AbstractContainerMenu {
   public static final int INPUT_SLOT = 0;
   public static final int RESULT_SLOT = 1;
   private static final int INV_SLOT_START = 2;
   private static final int INV_SLOT_END = 29;
   private static final int USE_ROW_SLOT_START = 29;
   private static final int USE_ROW_SLOT_END = 38;
   private final ContainerLevelAccess access;
   private final DataSlot selectedRecipeIndex = DataSlot.standalone();
   private final Level level;
   private final Player player;
   // Server side: full recipe holders for the current input.
   private List<RecipeHolder<WoodworkingRecipe>> recipes = Lists.newArrayList();
   // Client side: results/ids synced by the server (the client has no recipe manager in 1.21.2+).
   private List<ItemStack> clientResults = List.of();
   private List<ResourceLocation> clientRecipeIds = List.of();
   private ItemStack input = ItemStack.EMPTY;
   final Slot inputSlot;
   final Slot resultSlot;
   Runnable slotUpdateListener = () -> {};
   public final Container container = new SimpleContainer(1) {
      public void setChanged() {
         super.setChanged();
         WoodworkingWorkbenchMenu.this.slotsChanged(this);
         WoodworkingWorkbenchMenu.this.slotUpdateListener.run();
      }
   };
   final ResultContainer resultContainer = new ResultContainer();
   private long lastSoundTime;

   public WoodworkingWorkbenchMenu(int pId, Inventory inventory) {
      this(pId, inventory, ContainerLevelAccess.NULL);
   }

   public WoodworkingWorkbenchMenu(int id, Inventory inventory, ContainerLevelAccess levelAccess) {
      super(MenuTypeRegistry.WOODWORKING_WORKBENCH, id);
      this.access = levelAccess;
      this.level = inventory.player.level();
      this.player = inventory.player;
      this.inputSlot = this.addSlot(new Slot(this.container, 0, 20, 33));
      this.resultSlot = this.addSlot(new Slot(this.resultContainer, 1, 143, 33) {
         public boolean mayPlace(ItemStack itemStack) {
            return false;
         }

         public void onTake(Player player, ItemStack itemStack) {
            itemStack.onCraftedBy(player, itemStack.getCount());
            WoodworkingWorkbenchMenu.this.resultContainer.awardUsedRecipes(player, List.of(itemStack));
            ItemStack itemstack = WoodworkingWorkbenchMenu.this.inputSlot.remove(1);
            if (!itemstack.isEmpty()) {
               WoodworkingWorkbenchMenu.this.setupResultSlot();
            }

            super.onTake(player, itemStack);
            levelAccess.execute((level, pos) -> {
               long l = level.getGameTime();
               if (WoodworkingWorkbenchMenu.this.lastSoundTime != l) {
                  level.playSound(null, pos, SoundRegistry.WOODWORK, SoundSource.BLOCKS, 1.0F, 1.0F);
                  WoodworkingWorkbenchMenu.this.lastSoundTime = l;
               }
            });
         }
      });

      for (int i = 0; i < 3; i++) {
         for (int j = 0; j < 9; j++) {
            this.addSlot(new Slot(inventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
         }
      }

      for (int k = 0; k < 9; k++) {
         this.addSlot(new Slot(inventory, k, 8 + k * 18, 142));
      }

      this.addDataSlot(this.selectedRecipeIndex);
   }

   public int getSelectedRecipeIndex() {
      return this.selectedRecipeIndex.get();
   }

   public List<RecipeHolder<WoodworkingRecipe>> getRecipes() {
      return this.recipes;
   }

   public int getNumRecipes() {
      return this.level.isClientSide ? this.clientResults.size() : this.recipes.size();
   }

   public ItemStack getRecipeResult(int index) {
      if (this.level.isClientSide) {
         return index >= 0 && index < this.clientResults.size() ? this.clientResults.get(index).copy() : ItemStack.EMPTY;
      }
      return index >= 0 && index < this.recipes.size() ? this.recipes.get(index).value().getResultStack() : ItemStack.EMPTY;
   }

   public boolean hasInputItem() {
      return this.inputSlot.hasItem() && this.getNumRecipes() > 0;
   }

   public boolean stillValid(Player pPlayer) {
      return stillValid(this.access, pPlayer, BlockRegistry.WOODWORKING_WORKBENCH);
   }

   public boolean clickMenuButton(Player pPlayer, int pId) {
      if (this.isValidRecipeIndex(pId)) {
         this.selectedRecipeIndex.set(pId);
         this.setupResultSlot();
      }

      return true;
   }

   private boolean isValidRecipeIndex(int p_40335_) {
      return p_40335_ >= 0 && p_40335_ < this.getNumRecipes();
   }

   public void slotsChanged(Container pInventory) {
      ItemStack itemstack = this.inputSlot.getItem();
      if (!itemstack.is(this.input.getItem())) {
         this.input = itemstack.copy();
         this.setupRecipeList(pInventory, itemstack);
      }
   }

   private void setupRecipeList(Container pInventory, ItemStack pStack) {
      this.recipes.clear();
      this.clientResults = List.of();
      this.clientRecipeIds = List.of();
      this.selectedRecipeIndex.set(-1);
      this.resultSlot.set(ItemStack.EMPTY);
      if (this.level.isClientSide) {
         // The client cannot look up recipes itself; the server sends them back
         // via WoodworkingRecipesS2CPayload after processing the same slot change.
         return;
      }

      if (!pStack.isEmpty()) {
         RecipeManager recipeManager = RecipeManagerAccess.get(this.level);
         if (recipeManager == null) {
            return;
         }
         SingleRecipeInput input = new SingleRecipeInput(pStack);
         this.recipes = recipeManager.getRecipes().stream()
               .filter(holder -> holder.value() instanceof WoodworkingRecipe)
               .map(holder -> (RecipeHolder<WoodworkingRecipe>)holder)
               .filter(holder -> holder.value().matches(input, this.level))
               .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
      }

      if (this.player instanceof ServerPlayer serverPlayer) {
         List<ItemStack> results = this.recipes.stream().map(holder -> holder.value().getResultStack()).toList();
         List<ResourceLocation> ids = this.recipes.stream().map(holder -> holder.id().location()).toList();
         ServerPlayNetworking.send(serverPlayer, new WoodworkingRecipesS2CPayload(results, ids));
      }
   }

   /**
    * Called on the client when the server pushes the matching recipe list.
    */
   public void receiveRecipes(List<ItemStack> results, List<ResourceLocation> ids) {
      if (!this.level.isClientSide) {
         return;
      }
      this.clientResults = results;
      this.clientRecipeIds = ids;
      if (this.isValidRecipeIndex(this.selectedRecipeIndex.get())) {
         this.setupResultSlot();
      }
      this.slotUpdateListener.run();
   }

   void setupResultSlot() {
      if (this.getNumRecipes() > 0 && this.isValidRecipeIndex(this.selectedRecipeIndex.get())) {
         if (this.level.isClientSide) {
            this.resultSlot.set(this.clientResults.get(this.selectedRecipeIndex.get()).copy());
         } else {
            RecipeHolder<WoodworkingRecipe> holder = this.recipes.get(this.selectedRecipeIndex.get());
            this.resultContainer.setRecipeUsed(holder);
            this.resultSlot.set(holder.value().assemble(new SingleRecipeInput(this.container.getItem(0)), this.level.registryAccess()));
         }
      } else {
         this.resultSlot.set(ItemStack.EMPTY);
      }

      this.broadcastChanges();
   }

   public MenuType<?> getType() {
      return MenuTypeRegistry.WOODWORKING_WORKBENCH;
   }

   public void registerUpdateListener(Runnable pListener) {
      this.slotUpdateListener = pListener;
   }

   public boolean canTakeItemForPickAll(ItemStack pStack, Slot pSlot) {
      return pSlot.container != this.resultContainer && super.canTakeItemForPickAll(pStack, pSlot);
   }

   public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
      ItemStack itemstack = ItemStack.EMPTY;
      Slot slot = (Slot)this.slots.get(pIndex);
      if (slot.hasItem()) {
         ItemStack slotItem = slot.getItem();
         Item item = slotItem.getItem();
         itemstack = slotItem.copy();
         if (pIndex == 1) {
            item.onCraftedBy(slotItem, pPlayer);
            if (!this.moveItemStackTo(slotItem, 2, 38, true)) {
               return ItemStack.EMPTY;
            }

            slot.onQuickCraft(slotItem, itemstack);
         } else if (pIndex == 0) {
            if (!this.moveItemStackTo(slotItem, 2, 38, false)) {
               return ItemStack.EMPTY;
            }
         } else if (!this.level.isClientSide && this.acceptsInput(slotItem)) {
            if (!this.moveItemStackTo(slotItem, 0, 1, false)) {
               return ItemStack.EMPTY;
            }
         } else if (pIndex >= 2 && pIndex < 29) {
            if (!this.moveItemStackTo(slotItem, 29, 38, false)) {
               return ItemStack.EMPTY;
            }
         } else if (pIndex >= 29 && pIndex < 38 && !this.moveItemStackTo(slotItem, 2, 29, false)) {
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

   private boolean acceptsInput(ItemStack stack) {
      RecipeManager recipeManager = RecipeManagerAccess.get(this.level);
      if (recipeManager == null) {
         return false;
      }
      return recipeManager.getRecipes().stream()
            .filter(holder -> holder.value() instanceof WoodworkingRecipe)
            .anyMatch(holder -> ((WoodworkingRecipe)holder.value()).matches(new SingleRecipeInput(stack), this.level));
   }

   public void removed(Player pPlayer) {
      super.removed(pPlayer);
      this.resultContainer.removeItemNoUpdate(1);
      this.access.execute((p_40313_, p_40314_) -> this.clearContainer(pPlayer, this.container));
   }
}
