package com.voxelutopia.ultramarine.client.integration.jade;

import com.voxelutopia.ultramarine.world.block.entity.BrickKilnBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum BrickKilnComponent implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
   INSTANCE;

   public static final ResourceLocation BRICK_KILN = new ResourceLocation("ultramarine", "brick_kiln");

   @Override
   public void appendTooltip(ITooltip tooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
      CompoundTag data = blockAccessor.getServerData();
      if (data.contains("CookTime")) {
         int progress = data.getInt("CookTime");
         int total = data.getInt("CookTimeTotal");
         int percent = total <= 0 ? 0 : Math.min(100, progress * 100 / total);
         tooltip.add(Component.translatable("gui.jade.plugin_ultramarine.brick_kiln_progress", percent));
      }
   }

   @Override
   public void appendServerData(CompoundTag compoundTag, BlockAccessor blockAccessor) {
      BrickKilnBlockEntity kiln = (BrickKilnBlockEntity) blockAccessor.getBlockEntity();
      CompoundTag kilnTag = kiln.saveWithoutMetadata();
      compoundTag.putInt("CookTime", kilnTag.getInt("CookTime"));
      compoundTag.putInt("CookTimeTotal", kilnTag.getInt("CookTimeTotal"));
   }

   @Override
   public ResourceLocation getUid() {
      return BRICK_KILN;
   }
}
