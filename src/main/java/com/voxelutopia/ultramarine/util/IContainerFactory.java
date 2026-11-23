package com.voxelutopia.ultramarine.util;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType.MenuSupplier;

public interface IContainerFactory<T extends AbstractContainerMenu> extends MenuSupplier<T> {
   T create(int var1, Inventory var2, FriendlyByteBuf var3);

   default T create(int containerId, Inventory playerInventory) {
      return this.create(containerId, playerInventory, null);
   }
}
