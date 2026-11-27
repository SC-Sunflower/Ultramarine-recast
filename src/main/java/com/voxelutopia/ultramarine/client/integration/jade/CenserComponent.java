package com.voxelutopia.ultramarine.client.integration.jade;

import com.voxelutopia.ultramarine.world.block.entity.CenserBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum CenserComponent implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
   INSTANCE;

   public static final Identifier CENSER_BURN_TIME = Identifier.fromNamespaceAndPath("ultramarine", "censer_burn_time");

   @Override
   public void appendTooltip(ITooltip tooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
      int burnTime = blockAccessor.getServerData().getIntOr("BurnTime", 0);
      if (burnTime > 0) {
         tooltip.add(Component.translatable("gui.jade.plugin_ultramarine.censer_burn_time", burnTime / 20));
      }
   }

   @Override
   public Identifier getUid() {
      return CENSER_BURN_TIME;
   }

   @Override
   public void appendServerData(CompoundTag compoundTag, BlockAccessor blockAccessor) {
      CenserBlockEntity censer = (CenserBlockEntity) blockAccessor.getBlockEntity();
      compoundTag.putInt("BurnTime", censer.getRemainingTime());
   }
}
