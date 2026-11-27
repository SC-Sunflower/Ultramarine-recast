package com.voxelutopia.ultramarine.client.integration.jade;

import com.voxelutopia.ultramarine.world.block.entity.BottleGourdBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum BottleGourdComponent implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
   INSTANCE;

   public static final ResourceLocation BOTTLE_GOURD = new ResourceLocation("ultramarine", "bottle_gourd");

   @Override
   public void appendTooltip(ITooltip tooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
      CompoundTag data = blockAccessor.getServerData();
      if (data.contains("Charges")) {
         tooltip.add(Component.translatable("gui.jade.plugin_ultramarine.bottle_gourd", data.getInt("Charges")));
      }
   }

   @Override
   public void appendServerData(CompoundTag compoundTag, BlockAccessor blockAccessor) {
      BottleGourdBlockEntity gourd = (BottleGourdBlockEntity) blockAccessor.getBlockEntity();
      if (gourd.hasCharges()) {
         compoundTag.putInt("Charges", gourd.getCharges());
      }
   }

   @Override
   public ResourceLocation getUid() {
      return BOTTLE_GOURD;
   }
}
