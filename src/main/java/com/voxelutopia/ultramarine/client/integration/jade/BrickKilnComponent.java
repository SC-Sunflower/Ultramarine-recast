package com.voxelutopia.ultramarine.client.integration.jade;

import com.voxelutopia.ultramarine.world.block.entity.BrickKilnBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.JadeUI;

public enum BrickKilnComponent implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
   INSTANCE;

   public static final Identifier BRICK_KILN = Identifier.fromNamespaceAndPath("ultramarine", "brick_kiln");

   @Override
   public void appendTooltip(ITooltip tooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
      CompoundTag data = blockAccessor.getServerData();
      if (data.contains("CookTime")) {
         int progress = data.getIntOr("CookTime", 0);
         int total = data.getIntOr("CookTimeTotal", 0);
         ListTag items = data.getListOrEmpty("Items");

         for (int i = 0; i < items.size() && i < 4; i++) {
            Tag tag = items.get(i);
            ItemStack stack = ItemStack.CODEC.parse(NbtOps.INSTANCE, tag).result().orElse(ItemStack.EMPTY);
            if (stack.isEmpty()) {
               tooltip.append(JadeUI.spacer(18, 18));
            } else {
               tooltip.append(JadeUI.item(stack));
            }
         }

         tooltip.append(JadeUI.progressArrow(total <= 0 ? 0.0F : Math.min(1.0F, (float) progress / total)));
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
         ListTag items = new ListTag();
         for (ItemStack stack : new ItemStack[]{primary, secondary, fuel, result}) {
            ItemStack.CODEC.encodeStart(NbtOps.INSTANCE, stack).result().ifPresent(items::add);
         }
         compoundTag.put("Items", items);
         CompoundTag kilnTag = kiln.saveWithoutMetadata(blockAccessor.getLevel().registryAccess());
         compoundTag.putInt("CookTime", kilnTag.getIntOr("CookTime", 0));
         compoundTag.putInt("CookTimeTotal", kilnTag.getIntOr("CookTimeTotal", 0));
      }
   }

   @Override
   public Identifier getUid() {
      return BRICK_KILN;
   }
}
