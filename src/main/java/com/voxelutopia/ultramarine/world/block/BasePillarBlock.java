package com.voxelutopia.ultramarine.world.block;

import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;

public class BasePillarBlock extends RotatedPillarBlock implements BaseBlockPropertyHolder {
   protected final BaseBlockProperty property;

   public BasePillarBlock(BaseBlock block) {
      this(block.getProperty());
   }

   public BasePillarBlock(BaseBlockProperty property) {
      super(property.properties);
      this.property = property;
      this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(AXIS, Axis.Y));
   }

   @Override
   public BaseBlockProperty getProperty() {
      return this.property;
   }
}
