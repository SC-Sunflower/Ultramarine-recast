package com.voxelutopia.ultramarine.client.integration.jade;

import com.voxelutopia.ultramarine.world.block.entity.BrickKilnBlockEntity;
import net.minecraft.core.HolderLookup;
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

public enum BrickKilnComponent implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
   INSTANCE;

   public static final ResourceLocation BRICK_KILN = ResourceLocation.fromNamespaceAndPath("ultramarine", "brick_kiln");

   @Override
   public void appendTooltip(ITooltip tooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
      CompoundTag data = blockAccessor.getServerData();
      if (data.contains("CookTime")) {
         int progress = data.getInt("CookTime");
         int total = data.getInt("CookTimeTotal");
         ListTag items = data.getList("Items", 10);
         NonNullList<ItemStack> inventory = NonNullList.withSize(4, ItemStack.EMPTY);
         HolderLookup.Provider provider = blockAccessor.getLevel().registryAccess();

         for (int i = 0; i < items.size() && i < inventory.size(); i++) {
            inventory.set(i, ItemStack.parseOptional(provider, items.getCompound(i)));
         }

         IElementHelper helper = IElementHelper.get();
         tooltip.add(helper.item(inventory.get(0)));
         tooltip.append(helper.item(inventory.get(1)));
         tooltip.append(helper.item(inventory.get(2)));
         tooltip.append(helper.progress(total <= 0 ? 0.0F : Math.min(1.0F, (float) progress / total)));
         tooltip.append(helper.item(inventory.get(3)));
      }
   }

   @Override
   public void appendServerData(CompoundTag compoundTag, BlockAccessor blockAccessor) {
      BrickKilnBlockEntity kiln = (BrickKilnBlockEntity) blockAccessor.getBlockEntity();
      ItemStack primary = kiln.getStackInSlot(0);
      ItemStack secondary = kiln.getStackInSlot(1);
      ItemStack fuel = kiln.getStackInSlot(2);
      ItemStack result = kiln.getStackInSlot(3);
      if (!primary.isEmpty() || !secondary.isEmpty() || !fuel.isEmpty() || !result.isEmpty()) {
         HolderLookup.Provider provider = blockAccessor.getLevel().registryAccess();
         ListTag items = new ListTag();
         items.add(primary.save(provider));
         items.add(secondary.save(provider));
         items.add(fuel.save(provider));
         items.add(result.save(provider));
         compoundTag.put("Items", items);
         CompoundTag kilnTag = kiln.saveWithoutMetadata(provider);
         compoundTag.putInt("CookTime", kilnTag.getInt("CookTime"));
         compoundTag.putInt("CookTimeTotal", kilnTag.getInt("CookTimeTotal"));
      }
   }

   @Override
   public ResourceLocation getUid() {
      return BRICK_KILN;
   }
}
