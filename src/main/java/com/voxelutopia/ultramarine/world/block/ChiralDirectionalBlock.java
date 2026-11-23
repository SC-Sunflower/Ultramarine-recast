package com.voxelutopia.ultramarine.world.block;

import com.voxelutopia.ultramarine.world.block.state.ChiralBlockType;
import com.voxelutopia.ultramarine.world.block.state.ModBlockStateProperties;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;

public class ChiralDirectionalBlock extends BaseDirectionalBlock implements BaseBlockPropertyHolder {
   public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;
   public static final EnumProperty<ChiralBlockType> TYPE = ModBlockStateProperties.CHIRAL_BLOCK_TYPE;
   private final BaseBlockProperty property;

   public ChiralDirectionalBlock(BaseBlockProperty property) {
      super(property);
      this.property = property;
      this.registerDefaultState(
         (BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.UP))
            .setValue(TYPE, ChiralBlockType.LEFT)
      );
   }

   @Override
   public BlockState getStateForPlacement(BlockPlaceContext pContext) {
      Direction direction = pContext.getNearestLookingDirection().getOpposite();
      return (BlockState)((BlockState)this.defaultBlockState().setValue(FACING, direction)).setValue(TYPE, this.getChiralityFromDirection(direction));
   }

   private ChiralBlockType getChiralityFromDirection(Direction direction) {
      return switch (direction) {
         case NORTH, WEST -> ChiralBlockType.TOP;
         case SOUTH, EAST -> ChiralBlockType.BOTTOM;
         case UP -> ChiralBlockType.LEFT;
         case DOWN -> ChiralBlockType.RIGHT;
         default -> throw new IncompatibleClassChangeError();
      };
   }

   @Override
   protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
      pBuilder.add(new Property[]{FACING});
      pBuilder.add(new Property[]{TYPE});
   }

   @Override
   public BaseBlockProperty getProperty() {
      return this.property;
   }
}
