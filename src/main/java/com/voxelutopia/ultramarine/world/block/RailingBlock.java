package com.voxelutopia.ultramarine.world.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.voxelutopia.ultramarine.world.block.state.ModBlockStateProperties;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class RailingBlock extends Block implements BaseBlockPropertyHolder, SimpleWaterloggedBlock {
   protected final BaseBlockProperty property;
   private final Map<BlockState, VoxelShape> shapeByIndex;
   private final Map<Direction, VoxelShape> shapeByPart;
   public static final BooleanProperty UP = BlockStateProperties.UP;
   public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
   public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
   public static final BooleanProperty EAST = BlockStateProperties.EAST;
   public static final BooleanProperty WEST = BlockStateProperties.WEST;
   public static final BooleanProperty SHIFTED = ModBlockStateProperties.SHIFTED;
   public static final BooleanProperty POLE_LOCKED = ModBlockStateProperties.LOCKED;
   public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   private static final VoxelShape MARBLE_POLE = Block.box(6.0, 0.0, 6.0, 10.0, 22.0, 10.0);
   private static final VoxelShape MARBLE_NORTH_SIDE = Block.box(7.0, 0.0, 0.0, 9.0, 15.0, 9.0);
   private static final VoxelShape MARBLE_SOUTH_SIDE = Block.box(7.0, 0.0, 7.0, 9.0, 15.0, 16.0);
   private static final VoxelShape MARBLE_WEST_SIDE = Block.box(0.0, 0.0, 7.0, 9.0, 15.0, 9.0);
   private static final VoxelShape MARBLE_EAST_SIDE = Block.box(7.0, 0.0, 7.0, 16.0, 15.0, 9.0);
   public static final Map<Direction, VoxelShape> MARBLE_SHAPES = Map.of(
      Direction.UP,
      MARBLE_POLE,
      Direction.NORTH,
      MARBLE_NORTH_SIDE,
      Direction.SOUTH,
      MARBLE_SOUTH_SIDE,
      Direction.WEST,
      MARBLE_WEST_SIDE,
      Direction.EAST,
      MARBLE_EAST_SIDE
   );
   private static final VoxelShape WOODEN_POLE = Block.box(7.0, 0.0, 7.0, 9.0, 14.0, 9.0);
   private static final VoxelShape WOODEN_NORTH_SIDE = Block.box(7.5, 2.5, 0.0, 8.5, 12.0, 8.5);
   private static final VoxelShape WOODEN_SOUTH_SIDE = Block.box(7.5, 2.5, 7.5, 8.5, 12.0, 16.0);
   private static final VoxelShape WOODEN_WEST_SIDE = Block.box(0.0, 2.5, 7.5, 8.5, 12.0, 8.5);
   private static final VoxelShape WOODEN_EAST_SIDE = Block.box(7.5, 2.5, 7.5, 16.0, 12.0, 8.5);
   public static final Map<Direction, VoxelShape> WOODEN_SHAPES = Map.of(
      Direction.UP,
      WOODEN_POLE,
      Direction.NORTH,
      WOODEN_NORTH_SIDE,
      Direction.SOUTH,
      WOODEN_SOUTH_SIDE,
      Direction.WEST,
      WOODEN_WEST_SIDE,
      Direction.EAST,
      WOODEN_EAST_SIDE
   );

   public RailingBlock(BaseBlockProperty property, Map<Direction, VoxelShape> partShapes) {
      super(property.properties.noOcclusion());
      this.property = property;
      this.registerDefaultState(
         (BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.getStateDefinition().any())
                                 .setValue(UP, true))
                              .setValue(NORTH, false))
                           .setValue(SOUTH, false))
                        .setValue(EAST, false))
                     .setValue(WEST, false))
                  .setValue(SHIFTED, false))
               .setValue(POLE_LOCKED, false))
            .setValue(WATERLOGGED, false)
      );
      this.shapeByPart = partShapes;
      this.shapeByIndex = this.makeShapes();
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext pContext) {
      LevelReader levelreader = pContext.getLevel();
      BlockPos blockpos = pContext.getClickedPos();
      FluidState fluidstate = pContext.getLevel().getFluidState(pContext.getClickedPos());
      BlockPos northSide = blockpos.north();
      BlockPos eastSide = blockpos.east();
      BlockPos southSide = blockpos.south();
      BlockPos westSide = blockpos.west();
      BlockState northState = levelreader.getBlockState(northSide);
      BlockState eastState = levelreader.getBlockState(eastSide);
      BlockState southState = levelreader.getBlockState(southSide);
      BlockState westState = levelreader.getBlockState(westSide);
      boolean north = this.connectsTo(Direction.NORTH, false, northSide, northState, levelreader);
      boolean east = this.connectsTo(Direction.EAST, false, eastSide, eastState, levelreader);
      boolean south = this.connectsTo(Direction.SOUTH, false, southSide, southState, levelreader);
      boolean west = this.connectsTo(Direction.WEST, false, westSide, westState, levelreader);
      boolean up = (!east || !west || north || south) && (east || west || !north || !south);
      BlockState waterState = (BlockState)this.defaultBlockState().setValue(WATERLOGGED, fluidstate.getType() == Fluids.WATER);
      return (BlockState)((BlockState)((BlockState)((BlockState)((BlockState)waterState.setValue(NORTH, north)).setValue(SOUTH, south))
               .setValue(EAST, east))
            .setValue(WEST, west))
         .setValue(UP, up);
   }

   public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
      boolean north = (Boolean)pState.getValue(NORTH);
      boolean south = (Boolean)pState.getValue(SOUTH);
      boolean east = (Boolean)pState.getValue(EAST);
      boolean west = (Boolean)pState.getValue(WEST);
      boolean shifted = (Boolean)pState.getValue(SHIFTED);
      if ((Boolean)pState.getValue(WATERLOGGED)) {
         pLevel.scheduleTick(pCurrentPos, Fluids.WATER, Fluids.WATER.getTickDelay(pLevel));
      }

      if (pFacing != Direction.DOWN && pFacing != Direction.UP) {
         if (pFacing == Direction.NORTH) {
            north = this.connectsTo(Direction.NORTH, shifted, pFacingPos, pFacingState, pLevel);
         }

         if (pFacing == Direction.SOUTH) {
            south = this.connectsTo(Direction.SOUTH, shifted, pFacingPos, pFacingState, pLevel);
         }

         if (pFacing == Direction.EAST) {
            east = this.connectsTo(Direction.EAST, shifted, pFacingPos, pFacingState, pLevel);
         }

         if (pFacing == Direction.WEST) {
            west = this.connectsTo(Direction.WEST, shifted, pFacingPos, pFacingState, pLevel);
         }

         boolean up = (Boolean)pState.getValue(POLE_LOCKED) || (!east || !west || north || south) && (east || west || !north || !south);
         return (BlockState)((BlockState)((BlockState)((BlockState)((BlockState)pState.setValue(NORTH, north)).setValue(SOUTH, south))
                  .setValue(EAST, east))
               .setValue(WEST, west))
            .setValue(UP, up);
      } else {
         return super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
      }
   }

   public BlockState updatePole(BlockState state) {
      boolean north = (Boolean)state.getValue(NORTH);
      boolean south = (Boolean)state.getValue(SOUTH);
      boolean east = (Boolean)state.getValue(EAST);
      boolean west = (Boolean)state.getValue(WEST);
      boolean up = (Boolean)state.getValue(POLE_LOCKED) || (!east || !west || north || south) && (east || west || !north || !south);
      return (BlockState)state.setValue(UP, up);
   }

   private boolean connectsTo(Direction connectsToDirection, Boolean isSelfShifted, BlockPos connectsToPos, BlockState connectsToState, BlockGetter level) {
      boolean canConnectToSlant = false;
      if (!isSelfShifted && connectsToState.getBlock() instanceof RailingSlant) {
         Direction facing = (Direction)connectsToState.getValue(BaseHorizontalDirectionalBlock.FACING);
         Boolean shifted = (Boolean)connectsToState.getValue(RailingSlant.SHIFTED);
         canConnectToSlant = !shifted && connectsToDirection == facing.getOpposite() || shifted && connectsToDirection == facing;
      } else if (isSelfShifted && connectsToState.getBlock() instanceof RailingSlant) {
         Direction facing = (Direction)connectsToState.getValue(BaseHorizontalDirectionalBlock.FACING);
         Boolean shifted = (Boolean)connectsToState.getValue(RailingSlant.SHIFTED);
         canConnectToSlant = !shifted && connectsToDirection == facing;
      } else if (isSelfShifted && level.getBlockState(connectsToPos.below()).getBlock() instanceof RailingSlant) {
         BlockState blockBelow = level.getBlockState(connectsToPos.below());
         Direction facing = (Direction)blockBelow.getValue(BaseHorizontalDirectionalBlock.FACING);
         Boolean shifted = (Boolean)blockBelow.getValue(RailingSlant.SHIFTED);
         canConnectToSlant = shifted && connectsToDirection == facing.getOpposite();
      }

      boolean pSideSolid = connectsToState.isFaceSturdy(level, connectsToPos, connectsToDirection.getOpposite());
      return connectsToState.getBlock() instanceof RailingBlock || canConnectToSlant || !isExceptionForConnection(connectsToState) && pSideSolid;
   }

   public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      return this.shapeByIndex.get(pState);
   }

   public VoxelShape getCollisionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      return this.getShape(pState, pLevel, pPos, pContext);
   }

   private Map<BlockState, VoxelShape> makeShapes() {
      Builder<BlockState, VoxelShape> builder = ImmutableMap.builder();

      for (Boolean up : UP.getPossibleValues()) {
         for (Boolean east : EAST.getPossibleValues()) {
            for (Boolean north : NORTH.getPossibleValues()) {
               for (Boolean west : WEST.getPossibleValues()) {
                  for (Boolean south : SOUTH.getPossibleValues()) {
                     VoxelShape shape = Shapes.empty();
                     if (north) {
                        shape = Shapes.or(shape, this.shapeByPart.get(Direction.NORTH));
                     }

                     if (south) {
                        shape = Shapes.or(shape, this.shapeByPart.get(Direction.SOUTH));
                     }

                     if (east) {
                        shape = Shapes.or(shape, this.shapeByPart.get(Direction.EAST));
                     }

                     if (west) {
                        shape = Shapes.or(shape, this.shapeByPart.get(Direction.WEST));
                     }

                     if (up) {
                        shape = Shapes.or(shape, this.shapeByPart.get(Direction.UP));
                     }

                     BlockState blockstate = (BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.defaultBlockState().setValue(UP, up))
                                 .setValue(EAST, east))
                              .setValue(WEST, west))
                           .setValue(NORTH, north))
                        .setValue(SOUTH, south);

                     for (Boolean shifted : SHIFTED.getPossibleValues()) {
                        for (Boolean locked : POLE_LOCKED.getPossibleValues()) {
                           for (Boolean waterlogged : WATERLOGGED.getPossibleValues()) {
                              blockstate = (BlockState)((BlockState)((BlockState)blockstate.setValue(SHIFTED, shifted)).setValue(POLE_LOCKED, locked))
                                 .setValue(WATERLOGGED, waterlogged);
                              builder.put(blockstate, shape);
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      return builder.build();
   }

   protected void createBlockStateDefinition(net.minecraft.world.level.block.state.StateDefinition.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(new Property[]{UP, NORTH, SOUTH, EAST, WEST, SHIFTED, POLE_LOCKED, WATERLOGGED});
   }

   public FluidState getFluidState(BlockState pState) {
      return pState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(pState);
   }

   public RenderShape getRenderShape(BlockState pState) {
      return RenderShape.MODEL;
   }

   public boolean propagatesSkylightDown(BlockState pState, BlockGetter pReader, BlockPos pPos) {
      return !(Boolean)pState.getValue(WATERLOGGED);
   }

   @Override
   public BaseBlockProperty getProperty() {
      return this.property;
   }
}
