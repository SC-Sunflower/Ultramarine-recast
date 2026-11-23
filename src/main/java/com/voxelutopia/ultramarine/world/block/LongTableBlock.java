package com.voxelutopia.ultramarine.world.block;

import com.voxelutopia.ultramarine.data.shape.RawVoxelShape;
import com.voxelutopia.ultramarine.world.block.state.ModBlockStateProperties;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class LongTableBlock extends Block implements BaseBlockPropertyHolder {
   public static final EnumProperty<Axis> AXIS = EnumProperty.create("axis", Axis.class, new Axis[]{Axis.X, Axis.Z});
   public static final BooleanProperty LEFT = ModBlockStateProperties.LEFT;
   public static final BooleanProperty RIGHT = ModBlockStateProperties.RIGHT;
   protected final BaseBlockProperty property;
   private static final RawVoxelShape SURFACE = new RawVoxelShape(0.0, 14.0, 0.0, 16.0, 16.0, 16.0);
   private static final RawVoxelShape SURFACE_SINGLE_X = new RawVoxelShape(-4.0, 14.0, 0.0, 20.0, 16.0, 16.0);
   private static final RawVoxelShape LEGS_SINGLE_X_LEFT = new RawVoxelShape(17.5, 0.0, 0.5, 19.5, 16.0, 15.5);
   private static final RawVoxelShape LEGS_LONG_X_LEFT = new RawVoxelShape(13.5, 0.0, 0.5, 15.5, 16.0, 15.5);
   private static final Function<BlockState, VoxelShape> SHAPE_FUNCTION = Util.memoize(
      blockstate -> {
         boolean left = (Boolean)blockstate.getValue(LEFT);
         boolean right = (Boolean)blockstate.getValue(RIGHT);
         boolean x = blockstate.getValue(AXIS) == Axis.X;
         if (left && right) {
            return SURFACE.copy().toVoxelShape();
         } else if (!left && !right) {
            return x
               ? Shapes.or(
                  SURFACE_SINGLE_X.copy().toVoxelShape(),
                  new VoxelShape[]{LEGS_SINGLE_X_LEFT.copy().toVoxelShape(), LEGS_SINGLE_X_LEFT.copy().mirrorZ().toVoxelShape()}
               )
               : Shapes.or(
                  SURFACE_SINGLE_X.copy().rotateY(90.0F).toVoxelShape(),
                  new VoxelShape[]{LEGS_SINGLE_X_LEFT.copy().rotateY(90.0F).toVoxelShape(), LEGS_SINGLE_X_LEFT.copy().mirrorZ().rotateY(90.0F).toVoxelShape()}
               );
         } else if (right) {
            return x
               ? Shapes.or(SURFACE.copy().toVoxelShape(), LEGS_LONG_X_LEFT.copy().toVoxelShape())
               : Shapes.or(SURFACE.copy().toVoxelShape(), LEGS_LONG_X_LEFT.copy().rotateY(90.0F).toVoxelShape());
         } else {
            return x
               ? Shapes.or(SURFACE.copy().toVoxelShape(), LEGS_LONG_X_LEFT.copy().mirrorZ().toVoxelShape())
               : Shapes.or(SURFACE.copy().toVoxelShape(), LEGS_LONG_X_LEFT.copy().mirrorZ().rotateY(90.0F).toVoxelShape());
         }
      }
   );

   public LongTableBlock(BaseBlockProperty property) {
      super(property.properties);
      this.property = property;
      this.registerDefaultState(
         (BlockState)((BlockState)((BlockState)((BlockState)this.getStateDefinition().any()).setValue(AXIS, Axis.X)).setValue(LEFT, false))
            .setValue(RIGHT, false)
      );
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
      super.createBlockStateDefinition(pBuilder);
      pBuilder.add(new Property[]{AXIS, LEFT, RIGHT});
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext pContext) {
      Axis axis = pContext.getHorizontalDirection().getClockWise().getAxis();
      return (BlockState)((BlockState)((BlockState)Objects.requireNonNull(super.getStateForPlacement(pContext)).setValue(AXIS, axis)).setValue(LEFT, false))
         .setValue(RIGHT, false);
   }

   public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pBlock, Orientation pFromPos, boolean pIsMoving) {
      super.neighborChanged(pState, pLevel, pPos, pBlock, pFromPos, pIsMoving);
      this.update(pLevel, pPos, new HashSet<>());
   }

   private void update(Level level, BlockPos pos, Set<BlockPos> visited) {
      ArrayList<BlockPos> toVisit = new ArrayList<>();

      for (int x = -1; x <= 1; x++) {
         for (int z = -1; z <= 1; z++) {
            BlockPos neighbor = pos.offset(x, 0, z);
            toVisit.add(neighbor);
         }
      }

      for (BlockPos p : toVisit) {
         if (!visited.contains(p)) {
            visited.add(p);
            if (this.isTableBlock(level, p)) {
               this.updateState(level, p);
               this.update(level, p, visited);
            }
         }
      }
   }

   private void updateState(Level level, BlockPos pos) {
      if (level.isLoaded(pos)) {
         BlockState state = level.getBlockState(pos);
         BlockPos east = pos.east();
         BlockPos west = pos.west();
         boolean isTableAtEast = this.isTableBlock(level, east);
         boolean isTableAtWest = this.isTableBlock(level, west);
         if (!isTableAtEast && !isTableAtWest) {
            BlockPos north = pos.north();
            BlockPos south = pos.south();
            boolean isTableAtNorth = this.isTableBlock(level, north);
            boolean isTableAtSouth = this.isTableBlock(level, south);
            state = (BlockState)((BlockState)state.setValue(LEFT, isTableAtNorth)).setValue(RIGHT, isTableAtSouth);
            if (isTableAtNorth || isTableAtSouth) {
               state = (BlockState)state.setValue(AXIS, Axis.Z);
            }
         } else {
            state = (BlockState)((BlockState)((BlockState)state.setValue(AXIS, Axis.X)).setValue(LEFT, isTableAtEast))
               .setValue(RIGHT, isTableAtWest);
         }

         level.setBlock(pos, state, 3);
      }
   }

   private boolean isTableBlock(Level level, BlockPos pos) {
      return !level.isLoaded(pos) ? false : level.getBlockState(pos).getBlock() == this;
   }

   public BlockState rotate(BlockState pState, Rotation pRot) {
      BlockState newState = pState;
      if (pRot == Rotation.CLOCKWISE_90 || pRot == Rotation.COUNTERCLOCKWISE_90) {
         newState = switch ((Axis)pState.getValue(AXIS)) {
            case X -> (BlockState)pState.setValue(AXIS, Axis.Z);
            case Z -> (BlockState)pState.setValue(AXIS, Axis.X);
            default -> newState;
         };
      } else if (pRot == Rotation.CLOCKWISE_180) {
         newState = (BlockState)((BlockState)pState.setValue(LEFT, !(Boolean)pState.getValue(LEFT)))
            .setValue(RIGHT, !(Boolean)pState.getValue(RIGHT));
      }

      return newState;
   }

   public BlockState mirror(BlockState pState, Mirror pMirror) {
      return (BlockState)((BlockState)pState.setValue(LEFT, !(Boolean)pState.getValue(LEFT))).setValue(RIGHT, !(Boolean)pState.getValue(RIGHT));
   }

   @Override
   public BaseBlockProperty getProperty() {
      return this.property;
   }

   public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      return SHAPE_FUNCTION.apply(pState);
   }
}
