package com.voxelutopia.ultramarine.world.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.util.Map;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.VoxelShape;

public interface AxialBlock {
   BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

   default Map<Axis, VoxelShape> makeAxialShapes(int thickness, int bottom, int top) {
      VoxelShape XAABB = Block.box(0.0, bottom, 8 - (int)(thickness / 2.0), 16.0, top, 8 + (int)(thickness / 2.0));
      VoxelShape ZAABB = Block.box(8 - (int)(thickness / 2.0), bottom, 0.0, 8 + (int)(thickness / 2.0), top, 16.0);
      Builder<Axis, VoxelShape> builder = ImmutableMap.builder();
      builder.put(Axis.X, XAABB).put(Axis.Z, ZAABB);
      return builder.build();
   }

   default Map<Axis, VoxelShape> makeAxialShapes(int thickness, int height) {
      return this.makeAxialShapes(thickness, 0, height);
   }
}
