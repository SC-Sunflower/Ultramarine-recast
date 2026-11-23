package com.voxelutopia.ultramarine.world.block.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.voxelutopia.ultramarine.data.registry.BlockEntityRegistry;
import com.voxelutopia.ultramarine.world.block.ContainerDecorativeBlock;
import com.voxelutopia.ultramarine.world.block.menu.ContainerDecorativeBlockMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ContainerDecorativeBlockEntity extends RandomizableContainerBlockEntity {
   public static final Codec<ContainerDecorativeBlockEntity> CODEC = RecordCodecBuilder.create(
      instance -> instance.group(
            BlockPos.CODEC.fieldOf("pos").forGetter(entity -> entity.worldPosition),
            BlockState.CODEC.fieldOf("blockState").forGetter(BlockEntity::getBlockState),
            Codec.INT.fieldOf("rows").forGetter(entity -> entity.rows),
            BuiltInRegistries.BLOCK.byNameCodec().fieldOf("block").forGetter(entity -> entity.block)
         )
         .apply(instance, (pos, state, rows, block) -> {
            ContainerDecorativeBlockEntity entity = new ContainerDecorativeBlockEntity(pos, state, rows);
            entity.block = block;
            return entity;
         })
   );
   private NonNullList<ItemStack> items;
   private int rows = 3;
   private Block block;

   public ContainerDecorativeBlockEntity(BlockPos pos, BlockState state) {
      super(BlockEntityRegistry.CONTAINER_DECORATIVE_BLOCK, pos, state);
   }

   public ContainerDecorativeBlockEntity(BlockPos pos, BlockState state, int rows) {
      this(pos, state);
      this.block = state.getBlock();
      this.rows = rows;
      this.items = NonNullList.withSize(rows * 9, ItemStack.EMPTY);
   }

   protected NonNullList<ItemStack> getItems() {
      return this.items;
   }

   protected void setItems(NonNullList<ItemStack> pItemStacks) {
      this.items = pItemStacks;
   }

   protected Component getDefaultName() {
      return Component.translatable("container." + BuiltInRegistries.BLOCK.getKey(this.block).getPath());
   }

   protected AbstractContainerMenu createMenu(int pContainerId, Inventory pInventory) {
      return this.block instanceof ContainerDecorativeBlock container
         ? container.getContainerType().createMenu(pContainerId, pInventory, this)
         : ContainerDecorativeBlockMenu.genericThreeRows(pContainerId, pInventory, this);
   }

   public int getContainerSize() {
      return this.rows * 9;
   }

   protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider provider) {
      super.saveAdditional(nbt, provider);
      nbt.putString("Block", BuiltInRegistries.BLOCK.getKey(this.block).toString());
      nbt.putByte("Rows", (byte)this.rows);
      if (!this.trySaveLootTable(nbt)) {
         ContainerHelper.saveAllItems(nbt, this.items, provider);
      }
   }

   protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider provider) {
      super.loadAdditional(nbt, provider);
      this.block = (Block)BuiltInRegistries.BLOCK.get(ResourceLocation.tryParse(nbt.getString("Block")));
      this.rows = nbt.getByte("Rows");
      this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
      if (!this.tryLoadLootTable(nbt)) {
         ContainerHelper.loadAllItems(nbt, this.items, provider);
      }
   }
}
