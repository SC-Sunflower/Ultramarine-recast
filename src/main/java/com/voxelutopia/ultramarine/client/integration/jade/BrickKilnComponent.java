package com.voxelutopia.ultramarine.client.integration.jade;

import com.voxelutopia.ultramarine.world.block.entity.BrickKilnBlockEntity;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElementHelper;
import snownee.jade.impl.ui.ProgressArrowElement;

public enum BrickKilnComponent implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
   INSTANCE;

   public static final ResourceLocation BRICK_KILN = ResourceLocation.fromNamespaceAndPath("ultramarine", "brick_kiln");

   public void appendTooltip(ITooltip tooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
      CompoundTag data = blockAccessor.getServerData();
      if (data.contains("CookTime")) {
         int progress = data.getInt("CookTime");
         ListTag items = data.getList("Items", 10);
         NonNullList<ItemStack> inventory = NonNullList.withSize(4, ItemStack.EMPTY);

         for (int i = 0; i < items.size(); i++) {
            inventory.set(i, ItemStack.of(items.getCompound(i)));
         }

         IElementHelper helper = IElementHelper.get();
         int total = data.getInt("CookTimeTotal");
         tooltip.add(helper.item((ItemStack)inventory.get(0)));
         tooltip.append(helper.item((ItemStack)inventory.get(1)));
         tooltip.append(helper.item((ItemStack)inventory.get(2)));
         tooltip.append(new ProgressArrowElement((float)progress / total));
         tooltip.append(helper.item((ItemStack)inventory.get(3)));
      }
   }

   public void appendServerData(CompoundTag compoundTag, BlockAccessor blockAccessor) {
      BrickKilnBlockEntity kiln = (BrickKilnBlockEntity)blockAccessor.getBlockEntity();
      ItemStack primary = kiln.getStackInSlot(0);
      ItemStack secondary = kiln.getStackInSlot(1);
      ItemStack fuel = kiln.getStackInSlot(2);
      ItemStack result = kiln.getStackInSlot(3);
      if (!primary.isEmpty() || !secondary.isEmpty() || !fuel.isEmpty() || !result.isEmpty()) {
         ListTag items = new ListTag();
         items.add(primary.save(new CompoundTag()));
         items.add(secondary.save(new CompoundTag()));
         items.add(fuel.save(new CompoundTag()));
         items.add(result.save(new CompoundTag()));
         compoundTag.put("Items", items);
         CompoundTag kilnTag = kiln.saveWithoutMetadata();
         compoundTag.putInt("CookTime", kilnTag.getInt("CookTime"));
         compoundTag.putInt("CookTimeTotal", kilnTag.getInt("CookTimeTotal"));
      }
   }

   public ResourceLocation getUid() {
      return BRICK_KILN;
   }
}
