package com.voxelutopia.ultramarine.world.block.entity;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.voxelutopia.ultramarine.data.recipe.CompositeSmeltingRecipe;
import com.voxelutopia.ultramarine.data.registry.BlockEntityRegistry;
import com.voxelutopia.ultramarine.data.registry.RecipeTypeRegistry;
import com.voxelutopia.ultramarine.world.block.menu.BrickKilnMenu;
import com.voxelutopia.ultramarine.data.storage.ItemStackHandler;
import com.voxelutopia.ultramarine.data.storage.RecipeWrapper;
import com.voxelutopia.ultramarine.data.storage.SlottedStackStorage;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import java.util.List;
import javax.annotation.ParametersAreNonnullByDefault;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BrickKilnBlockEntity extends BlockEntity implements MenuProvider, SlottedStackStorage {
   public static final Codec<BrickKilnBlockEntity> CODEC = RecordCodecBuilder.create(
      instance -> instance.group(
            BlockPos.CODEC.fieldOf("pos").forGetter(entity -> entity.worldPosition),
            BlockState.CODEC.fieldOf("blockState").forGetter(entity -> entity.getBlockState()),
            Codec.INT.fieldOf("litTime").forGetter(entity -> entity.litTime),
            Codec.INT.fieldOf("litDuration").forGetter(entity -> entity.litDuration),
            Codec.INT.fieldOf("cookingProgress").forGetter(entity -> entity.cookingProgress),
            Codec.INT.fieldOf("cookingTotalTime").forGetter(entity -> entity.cookingTotalTime)
         )
         .apply(instance, (pos, state, litTime, litDuration, cookingProgress, cookingTotalTime) -> {
            BrickKilnBlockEntity entity = new BrickKilnBlockEntity(pos, state);
            entity.litTime = litTime;
            entity.litDuration = litDuration;
            entity.cookingProgress = cookingProgress;
            entity.cookingTotalTime = cookingTotalTime;
            return entity;
         })
   );
   public static final int SLOT_INPUT_PRIMARY = 0;
   public static final int SLOT_INPUT_SECONDARY = 1;
   public static final int SLOT_FUEL = 2;
   public static final int SLOT_RESULT = 3;
   public static final int DATA_LIT_TIME = 0;
   public static final int DATA_LIT_DURATION = 1;
   public static final int DATA_COOKING_PROGRESS = 2;
   public static final int DATA_COOKING_TOTAL_TIME = 3;
   public static final int NUM_DATA_VALUES = 4;
   public static final int NUM_SLOTS = 4;
   public static final int BURN_TIME_STANDARD = 200;
   public static final int BURN_COOL_SPEED = 2;
   private static final Component CONTAINER_TITLE = Component.translatable("container.brick_kiln");
   int litTime;
   int litDuration;
   int cookingProgress;
   int cookingTotalTime;
   private final ItemStackHandler ingredientsHandler = new ItemStackHandler(2) {
      protected void onContentsChanged(int slot) {
         BrickKilnBlockEntity.this.setChanged();
      }
   };
   private final ItemStackHandler fuelHandler = new ItemStackHandler(1) {
      protected void onContentsChanged(int slot) {
         BrickKilnBlockEntity.this.setChanged();
      }

      public boolean isItemValid(int slot, ItemVariant resource, int amount) {
         ItemStack stack = resource.toStack(amount);
         return FuelRegistry.INSTANCE.get(stack.getItem()) != null;
      }
   };
   private final ItemStackHandler resultHandler = new ItemStackHandler(1) {
      protected void onContentsChanged(int slot) {
         BrickKilnBlockEntity.this.setChanged();
      }

      public boolean isItemValid(int slot, ItemVariant resource, int count) {
         return false;
      }
   };
   protected final ContainerData dataAccess = new ContainerData() {
      public int get(int key) {
         return switch (key) {
            case 0 -> BrickKilnBlockEntity.this.litTime;
            case 1 -> BrickKilnBlockEntity.this.litDuration;
            case 2 -> BrickKilnBlockEntity.this.cookingProgress;
            case 3 -> BrickKilnBlockEntity.this.cookingTotalTime;
            default -> 0;
         };
      }

      public void set(int key, int value) {
         switch (key) {
            case 0:
               BrickKilnBlockEntity.this.litTime = value;
               break;
            case 1:
               BrickKilnBlockEntity.this.litDuration = value;
               break;
            case 2:
               BrickKilnBlockEntity.this.cookingProgress = value;
               break;
            case 3:
               BrickKilnBlockEntity.this.cookingTotalTime = value;
         }
      }

      public int getCount() {
         return 4;
      }
   };
   private final Object2IntOpenHashMap<ResourceLocation> recipesUsed = new Object2IntOpenHashMap();

   public BrickKilnBlockEntity(BlockPos blockPos, BlockState blockState) {
      super(BlockEntityRegistry.BRICK_KILN, blockPos, blockState);
   }

   public static void serverTick(Level pLevel, BlockPos pPos, BlockState pState, BrickKilnBlockEntity pBlockEntity) {
      boolean lit = pBlockEntity.isLit();
      boolean changed = false;
      ItemStack fuelItem = pBlockEntity.fuelHandler.getStackInSlot(0);
      ItemStack primaryItem = pBlockEntity.ingredientsHandler.getStackInSlot(0);
      ItemStack secondaryItem = pBlockEntity.ingredientsHandler.getStackInSlot(1);
      ItemStack resultItem = pBlockEntity.resultHandler.getStackInSlot(0);
      RecipeHolder<CompositeSmeltingRecipe> holder = pLevel.getRecipeManager()
         .getRecipeFor(RecipeTypeRegistry.COMPOSITE_SMELTING, wrapRecipe(pBlockEntity), pLevel)
         .orElse(null);
      CompositeSmeltingRecipe recipe = holder == null ? null : (CompositeSmeltingRecipe)holder.value();
      if (pBlockEntity.isLit()) {
         pBlockEntity.litTime--;
      }

      if (recipe != null) {
         pBlockEntity.cookingTotalTime = recipe.getCookingTime();
      }

      if (pBlockEntity.isLit() || !fuelItem.isEmpty() && !primaryItem.isEmpty() && !secondaryItem.isEmpty()) {
         int maxStack = 64;
         if (!pBlockEntity.isLit() && recipe != null && pBlockEntity.canBurn(recipe, fuelItem, primaryItem, secondaryItem, resultItem, maxStack)) {
            pBlockEntity.litTime = (Integer)FuelRegistry.INSTANCE.get(fuelItem.getItem());
            pBlockEntity.litDuration = pBlockEntity.litTime;
            if (pBlockEntity.isLit()) {
               changed = true;
               fuelItem.shrink(1);
               pBlockEntity.fuelHandler.setStackInSlot(0, fuelItem);
            }
         }

         if (pBlockEntity.isLit() && pBlockEntity.canBurn(recipe, fuelItem, primaryItem, secondaryItem, resultItem, maxStack)) {
            pBlockEntity.cookingProgress++;
            if (pBlockEntity.cookingProgress == pBlockEntity.cookingTotalTime) {
               pBlockEntity.cookingProgress = 0;
               pBlockEntity.cookingTotalTime = getTotalCookTime(pLevel, pBlockEntity);
               if (pBlockEntity.burn(recipe, pBlockEntity, fuelItem, primaryItem, secondaryItem, resultItem, maxStack)) {
                  pBlockEntity.setRecipeUsed(holder);
               }

               changed = true;
            }
         } else {
            pBlockEntity.cookingProgress = 0;
         }
      } else if (!pBlockEntity.isLit() && pBlockEntity.cookingProgress > 0) {
         pBlockEntity.cookingProgress = Mth.clamp(pBlockEntity.cookingProgress - 2, 0, pBlockEntity.cookingTotalTime);
      }

      if (lit != pBlockEntity.isLit()) {
         changed = true;
         pState = (BlockState)pState.setValue(AbstractFurnaceBlock.LIT, pBlockEntity.isLit());
         pLevel.setBlock(pPos, pState, 3);
      }

      if (changed) {
         setChanged(pLevel, pPos, pState);
      }
   }

   private boolean canBurn(@Nullable Recipe<Container> pRecipe, ItemStack fuel, ItemStack primary, ItemStack secondary, ItemStack resultPrev, int maxStackSize) {
      if (!primary.isEmpty() && !secondary.isEmpty() && pRecipe != null) {
         ItemStack result = pRecipe.assemble(new SimpleContainer(new ItemStack[]{primary, secondary}), this.level.registryAccess());
         if (result.isEmpty()) {
            return false;
         } else if (resultPrev.isEmpty()) {
            return true;
         } else if (!ItemStack.isSameItem(resultPrev, result)) {
            return false;
         } else {
            return resultPrev.getCount() + result.getCount() <= maxStackSize
                  && resultPrev.getCount() + result.getCount() <= resultPrev.getMaxStackSize()
               ? true
               : resultPrev.getCount() + result.getCount() <= result.getMaxStackSize();
         }
      } else {
         return false;
      }
   }

   private boolean burn(
      Recipe<Container> pRecipe, BrickKilnBlockEntity entity, ItemStack fuel, ItemStack primary, ItemStack secondary, ItemStack resultPrev, int maxStackSize
   ) {
      if (this.canBurn(pRecipe, fuel, primary, secondary, resultPrev, maxStackSize)) {
         ItemStack newResult = pRecipe.assemble(new SimpleContainer(new ItemStack[]{primary, secondary}), this.level.registryAccess());
         if (resultPrev.isEmpty()) {
            entity.resultHandler.setStackInSlot(0, newResult.copy());
         } else if (resultPrev.is(newResult.getItem())) {
            resultPrev.grow(newResult.getCount());
         }

         primary.shrink(1);
         secondary.shrink(1);
         entity.ingredientsHandler.setStackInSlot(0, primary);
         entity.ingredientsHandler.setStackInSlot(1, secondary);
         return true;
      } else {
         return false;
      }
   }

   private static int getTotalCookTime(Level pLevel, BrickKilnBlockEntity entity) {
      return pLevel.getRecipeManager()
         .getRecipeFor(RecipeTypeRegistry.COMPOSITE_SMELTING, wrapRecipe(entity), pLevel)
         .map(holder -> ((CompositeSmeltingRecipe)holder.value()).getCookingTime())
         .orElse(200);
   }

   public void setRecipeUsed(@Nullable RecipeHolder<?> pRecipe) {
      if (pRecipe != null) {
         this.recipesUsed.addTo(pRecipe.id(), 1);
      }
   }

   public void awardUsedRecipes(Player pPlayer) {
   }

   public void awardUsedRecipesAndPopExperience(ServerPlayer pPlayer) {
      if (!pPlayer.level().isClientSide()) {
         List<RecipeHolder<?>> list = this.getRecipesToAwardAndPopExperience((ServerLevel)pPlayer.level(), pPlayer.position());
         pPlayer.awardRecipes(list);
         this.recipesUsed.clear();
      }
   }

   public List<RecipeHolder<?>> getRecipesToAwardAndPopExperience(ServerLevel pLevel, Vec3 pos) {
      List<RecipeHolder<?>> list = Lists.newArrayList();
      ObjectIterator var4 = this.recipesUsed.object2IntEntrySet().iterator();

      while (var4.hasNext()) {
         Entry<ResourceLocation> entry = (Entry<ResourceLocation>)var4.next();
         pLevel.getRecipeManager().byKey((ResourceLocation)entry.getKey()).ifPresent(holder -> {
            list.add((RecipeHolder<?>)holder);
            if (holder.value() instanceof CompositeSmeltingRecipe smeltingRecipe) {
               createExperience(pLevel, pos, entry.getIntValue(), smeltingRecipe.getExp());
            } else if (holder.value() instanceof AbstractCookingRecipe cookingRecipe) {
               createExperience(pLevel, pos, entry.getIntValue(), cookingRecipe.getExperience());
            }
         });
      }

      return list;
   }

   private static void createExperience(ServerLevel pLevel, Vec3 p_155000_, int p_155001_, float p_155002_) {
      int i = Mth.floor(p_155001_ * p_155002_);
      float f = Mth.frac(p_155001_ * p_155002_);
      if (f != 0.0F && Math.random() < f) {
         i++;
      }

      ExperienceOrb.award(pLevel, p_155000_, i);
   }

   private boolean isLit() {
      return this.litTime > 0;
   }

   public Component getDisplayName() {
      return CONTAINER_TITLE;
   }

   @Nullable
   public AbstractContainerMenu createMenu(int pContainerId, Inventory pInventory, Player pPlayer) {
      return new BrickKilnMenu(pContainerId, this.worldPosition, pInventory, this, this.dataAccess);
   }

   private static RecipeWrapper wrapRecipe(BrickKilnBlockEntity entity) {
      return new RecipeWrapper(entity.ingredientsHandler);
   }

   protected void loadAdditional(CompoundTag pTag, HolderLookup.Provider provider) {
      super.loadAdditional(pTag, provider);
      this.litTime = pTag.getInt("BurnTime");
      this.cookingProgress = pTag.getInt("CookTime");
      this.cookingTotalTime = pTag.getInt("CookTimeTotal");
      ListTag itemListTag = pTag.getList("Items", 10);

      for (int i = 0; i < itemListTag.size(); i++) {
         CompoundTag itemTag = itemListTag.getCompound(i);
         int j = itemTag.getByte("Slot") & 255;
         switch (j) {
            case 0:
               this.ingredientsHandler.setStackInSlot(0, ItemStack.parseOptional(provider, itemTag));
               break;
            case 1:
               this.ingredientsHandler.setStackInSlot(1, ItemStack.parseOptional(provider, itemTag));
               break;
            case 2:
               this.fuelHandler.setStackInSlot(0, ItemStack.parseOptional(provider, itemTag));
               break;
            case 3:
               this.resultHandler.setStackInSlot(0, ItemStack.parseOptional(provider, itemTag));
         }
      }

      this.litDuration = this.getBurnTime(this.fuelHandler.getStackInSlot(0));
      CompoundTag recipesTag = pTag.getCompound("RecipesUsed");

      for (String s : recipesTag.getAllKeys()) {
         this.recipesUsed.put(new ResourceLocation(s), recipesTag.getInt(s));
      }
   }

   protected void saveAdditional(CompoundTag pTag, HolderLookup.Provider provider) {
      super.saveAdditional(pTag, provider);
      pTag.putInt("BurnTime", this.litTime);
      pTag.putInt("CookTime", this.cookingProgress);
      pTag.putInt("CookTimeTotal", this.cookingTotalTime);
      ListTag itemListTag = new ListTag();
      ItemStackHandler items = this.wrapHandlers();

      for (int i = 0; i < items.getSlots().size(); i++) {
         ItemStack item = items.getStackInSlot(i);
         if (!item.isEmpty()) {
            CompoundTag itemTag = new CompoundTag();
            itemTag.putByte("Slot", (byte)i);
            item.save(provider, itemTag);
            itemListTag.add(itemTag);
         }
      }

      if (!itemListTag.isEmpty()) {
         pTag.put("Items", itemListTag);
      }

      CompoundTag recipesTag = new CompoundTag();
      this.recipesUsed.forEach((resourceLocation, count) -> recipesTag.putInt(resourceLocation.toString(), count));
      pTag.put("RecipesUsed", recipesTag);
   }


   private int getBurnTime(ItemStack stack) {
      return stack.isEmpty() ? 0 : (Integer)FuelRegistry.INSTANCE.get(stack.getItem());
   }



   public ItemStackHandler getIngredientsHandler() {
      return this.ingredientsHandler;
   }

   public ItemStackHandler getFuelHandler() {
      return this.fuelHandler;
   }

   public ItemStackHandler getResultHandler() {
      return this.resultHandler;
   }

   public int getSlotCount() {
      return 4;
   }

   public SingleSlotStorage<ItemVariant> getSlot(int slot) {
      return switch (slot) {
         case 0 -> this.ingredientsHandler.getSlot(0);
         case 1 -> this.ingredientsHandler.getSlot(1);
         case 2 -> this.fuelHandler.getSlot(0);
         case 3 -> this.resultHandler.getSlot(0);
         default -> null;
      };
   }

   public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
      long inserted = 0L;
      if (inserted < maxAmount) {
         inserted += this.ingredientsHandler.insert(resource, maxAmount - inserted, transaction);
      }

      if (inserted < maxAmount && FuelRegistry.INSTANCE.get(resource.getItem()) != null) {
         inserted += this.fuelHandler.insert(resource, maxAmount - inserted, transaction);
      }

      return inserted;
   }

   public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
      long extracted = 0L;
      extracted += this.resultHandler.extract(resource, maxAmount - extracted, transaction);
      if (extracted < maxAmount) {
         extracted += this.ingredientsHandler.extract(resource, maxAmount - extracted, transaction);
      }

      if (extracted < maxAmount) {
         extracted += this.fuelHandler.extract(resource, maxAmount - extracted, transaction);
      }

      return extracted;
   }

   public ItemStack getStackInSlot(int slot) {
      return switch (slot) {
         case 0 -> this.ingredientsHandler.getStackInSlot(0);
         case 1 -> this.ingredientsHandler.getStackInSlot(1);
         case 2 -> this.fuelHandler.getStackInSlot(0);
         case 3 -> this.resultHandler.getStackInSlot(0);
         default -> ItemStack.EMPTY;
      };
   }

   public void setStackInSlot(int slot, ItemStack stack) {
      switch (slot) {
         case 0:
            this.ingredientsHandler.setStackInSlot(0, stack);
            break;
         case 1:
            this.ingredientsHandler.setStackInSlot(1, stack);
            break;
         case 2:
            this.fuelHandler.setStackInSlot(0, stack);
            break;
         case 3:
            this.resultHandler.setStackInSlot(0, stack);
      }
   }

   public int getSlotLimit(int slot) {
      return 64;
   }

   public boolean isItemValid(int slot, ItemVariant resource, int count) {
      return switch (slot) {
         case 0, 1 -> true;
         case 2 -> FuelRegistry.INSTANCE.get(resource.getItem()) != null;
         case 3 -> false;
         default -> false;
      };
   }

   public ItemStackHandler wrapHandlers() {
      ItemStackHandler combined = new ItemStackHandler(4);
      combined.setStackInSlot(0, this.ingredientsHandler.getStackInSlot(0));
      combined.setStackInSlot(1, this.ingredientsHandler.getStackInSlot(1));
      combined.setStackInSlot(2, this.fuelHandler.getStackInSlot(0));
      combined.setStackInSlot(3, this.resultHandler.getStackInSlot(0));
      return combined;
   }
}
