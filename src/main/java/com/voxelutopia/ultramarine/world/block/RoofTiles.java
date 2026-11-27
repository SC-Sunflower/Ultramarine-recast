package com.voxelutopia.ultramarine.world.block;

import com.voxelutopia.ultramarine.data.shape.RawVoxelShape;
import com.voxelutopia.ultramarine.world.block.state.ModBlockStateProperties;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome.Precipitation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.tuple.Pair;

public class RoofTiles extends ShiftableBlock {
   public static final Properties PROPERTIES = Properties.of().requiresCorrectToolForDrops().strength(1.5F, 4.0F).sound(SoundType.DEEPSLATE_TILES);
   public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
   public static final IntegerProperty SNOW_LAYERS = ModBlockStateProperties.SNOW_LAYERS;
   public static final EnumProperty<RoofTiles.SnowSide> SNOW_SIDE = ModBlockStateProperties.SNOW_SIDE;
   private static final VoxelShape BOTTOM_AABB = Block.box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0);
   private static final VoxelShape BOTTOM_AABB_SHIFTED = Block.box(0.0, -8.0, 0.0, 16.0, 0.0, 16.0);
   private static final RawVoxelShape NORTH_UPPER_RAW = new RawVoxelShape(0.0, 8.0, 0.0, 16.0, 16.0, 8.0);
   private static final VoxelShape NORTH_SHAPE = Shapes.or(BOTTOM_AABB, NORTH_UPPER_RAW.copy().toVoxelShape());
   private static final VoxelShape WEST_SHAPE = Shapes.or(BOTTOM_AABB, NORTH_UPPER_RAW.copy().rotateY(90.0F).toVoxelShape());
   private static final VoxelShape SOUTH_SHAPE = Shapes.or(BOTTOM_AABB, NORTH_UPPER_RAW.copy().rotateY(180.0F).toVoxelShape());
   private static final VoxelShape EAST_SHAPE = Shapes.or(BOTTOM_AABB, NORTH_UPPER_RAW.copy().rotateY(270.0F).toVoxelShape());
   private static final VoxelShape EAST_SHAPE_SHIFTED = Shapes.or(BOTTOM_AABB_SHIFTED, Block.box(8.0, 0.0, 0.0, 16.0, 8.0, 16.0));
   private static final VoxelShape NORTH_SHAPE_SHIFTED = Shapes.or(BOTTOM_AABB_SHIFTED, Block.box(0.0, 0.0, 0.0, 16.0, 8.0, 8.0));
   private static final VoxelShape SOUTH_SHAPE_SHIFTED = Shapes.or(BOTTOM_AABB_SHIFTED, Block.box(0.0, 0.0, 8.0, 16.0, 8.0, 16.0));
   private static final VoxelShape WEST_SHAPE_SHIFTED = Shapes.or(BOTTOM_AABB_SHIFTED, Block.box(0.0, 0.0, 0.0, 8.0, 8.0, 16.0));
   private final DyeColor color;
   private final RoofTiles.RoofTileType type;

   public RoofTiles(DyeColor color, RoofTiles.RoofTileType type) {
      super(PROPERTIES);
      this.color = color;
      this.type = type;
      this.registerDefaultState(
         (BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any())
                        .setValue(FACING, Direction.NORTH))
                     .setValue(SHIFTED, Boolean.FALSE))
                  .setValue(WATERLOGGED, Boolean.FALSE))
               .setValue(SNOW_LAYERS, 0))
            .setValue(SNOW_SIDE, RoofTiles.SnowSide.BOTH)
      );
   }

   public void handlePrecipitation(BlockState pState, Level pLevel, BlockPos pPos, Precipitation pPrecipitation) {
      if (pPrecipitation == Precipitation.SNOW) {
         this.tryPushSnow(pState, pLevel, pPos);
      }
   }

   private void tryPushSnow(BlockState pState, Level pLevel, BlockPos pPos) {
      RandomSource random = pLevel.getRandom();
      RoofTiles.SlopeAngle forwardAngle = this.checkForwardSlopeAngle(pLevel, pState, pPos);
      if (forwardAngle == RoofTiles.SlopeAngle.HIGHER) {
         this.handleSnow(pState, pLevel, pPos);
      } else {
         BlockState blockForward = pLevel.getBlockState(pPos.relative(((Direction)pState.getValue(FACING)).getOpposite()));
         BlockState blockForwardBelow = pLevel.getBlockState(pPos.relative(((Direction)pState.getValue(FACING)).getOpposite()).below());
         if (forwardAngle == RoofTiles.SlopeAngle.LEVEL
            && random.nextFloat() < 0.5F
            && blockForward.getBlock() instanceof RoofTiles
            && (Integer)blockForward.getValue(SNOW_LAYERS) == 15
            && random.nextFloat() > 0.9F) {
            this.tryPushSnow(blockForward, pLevel, pPos.relative(((Direction)pState.getValue(FACING)).getOpposite()));
         } else {
            if (forwardAngle == RoofTiles.SlopeAngle.LOWER && random.nextFloat() < 0.8F) {
               if (blockForward.getBlock() instanceof RoofTiles) {
                  if ((Integer)blockForward.getValue(SNOW_LAYERS) == 15 && random.nextFloat() > 0.9F) {
                     this.tryPushSnow(blockForward, pLevel, pPos.relative(((Direction)pState.getValue(FACING)).getOpposite()));
                     return;
                  }
               } else if (blockForwardBelow.getBlock() instanceof RoofTiles
                  && (Integer)blockForwardBelow.getValue(SNOW_LAYERS) == 15
                  && random.nextFloat() > 0.9F) {
                  this.tryPushSnow(blockForwardBelow, pLevel, pPos.relative(((Direction)pState.getValue(FACING)).getOpposite()));
                  return;
               }
            }

            this.handleSnow(pState, pLevel, pPos);
         }
      }
   }

   private void handleSnow(BlockState pState, Level pLevel, BlockPos pPos) {
      if (!pLevel.isClientSide()) {
         int snow = (Integer)pState.getValue(SNOW_LAYERS);
         BlockState newState = (BlockState)pState.setValue(SNOW_LAYERS, Math.min(15, ++snow));
         pLevel.setBlockAndUpdate(pPos, newState);
         this.updateSideSnow(newState, pLevel, pPos);
      }
   }

   private void removeSnow(BlockState pState, Level pLevel, BlockPos pPos) {
      if (!pLevel.isClientSide()) {
         int snow = (Integer)pState.getValue(SNOW_LAYERS);
         BlockState newState = (BlockState)pState.setValue(SNOW_LAYERS, Math.max(0, --snow));
         pLevel.setBlockAndUpdate(pPos, newState);
         this.updateSideSnow(newState, pLevel, pPos);
      }
   }

   public InteractionResult useItemOn(ItemStack stack, BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
      ItemStack item = pPlayer.getItemInHand(pHand);
      if (item.is(Items.SNOWBALL)) {
         this.handleSnow(pState, pLevel, pPos);
         if (!pPlayer.isCreative()) {
            item.shrink(1);
         }

         return InteractionResult.SUCCESS;
      } else if (item.is(ItemTags.SHOVELS)) {
         this.removeSnow(pState, pLevel, pPos);
         if (!pPlayer.isCreative()) {
            item.hurtAndBreak(1, pPlayer, EquipmentSlot.MAINHAND);
         }

         return InteractionResult.SUCCESS;
      } else {
         return InteractionResult.PASS;
      }
   }

   public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pBlock, Orientation pFromPos, boolean pIsMoving) {
      this.updateSideSnow(pState, pLevel, pPos);
      super.neighborChanged(pState, pLevel, pPos, pBlock, pFromPos, pIsMoving);
   }

   public void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
      this.updateSideSnow(pState, pLevel, pPos);
      this.updateNeighborSideSnow(pState, pLevel, pPos);
      super.onPlace(pState, pLevel, pPos, pOldState, pIsMoving);
   }

   protected void affectNeighborsAfterRemoval(BlockState pState, ServerLevel pLevel, BlockPos pPos, boolean pIsMoving) {
      this.updateNeighborSideSnow(pState, pLevel, pPos);
      super.affectNeighborsAfterRemoval(pState, pLevel, pPos, pIsMoving);
   }

   private void updateNeighborSideSnow(BlockState pState, Level pLevel, BlockPos pPos) {
      Direction direction = (Direction)pState.getValue(FACING);
      BlockPos[] posToUpdate = this.getSideUpdatePosArray(pPos, direction);

      for (BlockPos pos : posToUpdate) {
         Block block = pLevel.getBlockState(pos).getBlock();
         if (block instanceof RoofTiles) {
            this.updateSideSnow(pLevel.getBlockState(pos), pLevel, pos);
         }
      }
   }

   private BlockPos[] getSideUpdatePosArray(BlockPos pPos, Direction direction) {
      return new BlockPos[]{
         pPos.relative(direction.getClockWise()),
         pPos.relative(direction.getCounterClockWise()),
         pPos.relative(direction.getClockWise()).above(),
         pPos.relative(direction.getCounterClockWise()).above(),
         pPos.relative(direction.getClockWise()).below(),
         pPos.relative(direction.getCounterClockWise()).below()
      };
   }

   private void updateSideSnow(BlockState pState, Level pLevel, BlockPos pPos) {
      RoofTiles.SnowSide currentSnowSide = (RoofTiles.SnowSide)pState.getValue(SNOW_SIDE);
      Direction direction = (Direction)pState.getValue(FACING);
      BlockPos blockPosLeft = pPos.relative(direction.getCounterClockWise());
      BlockPos blockPosRight = pPos.relative(direction.getClockWise());
      RoofTiles.SlopeAngle slopeLeft = this.checkSideSlopeAngle(pLevel, pState, pPos, pLevel.getBlockState(blockPosLeft), blockPosLeft);
      RoofTiles.SlopeAngle slopeRight = this.checkSideSlopeAngle(pLevel, pState, pPos, pLevel.getBlockState(blockPosRight), blockPosRight);
      if (slopeLeft == RoofTiles.SlopeAngle.LOWER) {
         currentSnowSide = RoofTiles.SnowSide.add(currentSnowSide, RoofTiles.SnowSide.LEFT);
      } else {
         currentSnowSide = RoofTiles.SnowSide.remove(currentSnowSide, RoofTiles.SnowSide.LEFT);
      }

      if (slopeRight == RoofTiles.SlopeAngle.LOWER) {
         currentSnowSide = RoofTiles.SnowSide.add(currentSnowSide, RoofTiles.SnowSide.RIGHT);
      } else {
         currentSnowSide = RoofTiles.SnowSide.remove(currentSnowSide, RoofTiles.SnowSide.RIGHT);
      }

      BlockState newState = (BlockState)pState.setValue(SNOW_SIDE, currentSnowSide);
      pLevel.setBlockAndUpdate(pPos, newState);
   }

   private RoofTiles.SlopeAngle checkSideSlopeAngle(Level level, BlockState self, BlockPos selfPos, BlockState other, BlockPos otherPos) {
      Boolean selfShifted = (Boolean)self.getValue(SHIFTED);
      RoofTiles.RoofTileType type = ((RoofTiles)self.getBlock()).getType();
      if (type != RoofTiles.RoofTileType.STAIRS) {
         if (selfShifted) {
            Block blockAbove = level.getBlockState(otherPos.above()).getBlock();
            if (blockAbove instanceof RoofTiles) {
               return RoofTiles.SlopeAngle.HIGHER;
            }

            if (other.getBlock() instanceof RoofTiles tileLevel) {
               if (!(Boolean)other.getValue(SHIFTED)) {
                  return RoofTiles.SlopeAngle.HIGHER;
               }

               return tileLevel.getType() == RoofTiles.RoofTileType.STAIRS ? RoofTiles.SlopeAngle.HIGHER : RoofTiles.SlopeAngle.LEVEL;
            }

            if (!other.isAir()) {
               return RoofTiles.SlopeAngle.LEVEL;
            }
         } else {
            BlockState blockAbove = level.getBlockState(otherPos.above());
            if (blockAbove.getBlock() instanceof RoofTiles && (Boolean)blockAbove.getValue(SHIFTED)) {
               return RoofTiles.SlopeAngle.HIGHER;
            }

            if (other.getBlock() instanceof RoofTiles tileLevel) {
               if ((Boolean)other.getValue(SHIFTED)) {
                  return RoofTiles.SlopeAngle.LOWER;
               }

               return tileLevel.getType() == RoofTiles.RoofTileType.STAIRS ? RoofTiles.SlopeAngle.HIGHER : RoofTiles.SlopeAngle.LEVEL;
            }

            if (!other.isAir()) {
               return RoofTiles.SlopeAngle.HIGHER;
            }
         }

         return RoofTiles.SlopeAngle.LOWER;
      } else if (selfShifted) {
         BlockState blockAbove = level.getBlockState(otherPos.above());
         if (!blockAbove.isAir()) {
            return RoofTiles.SlopeAngle.HIGHER;
         }

         if (other.getBlock() instanceof RoofTiles tileLevel) {
            if (tileLevel.getType() != RoofTiles.RoofTileType.STAIRS) {
               return RoofTiles.SlopeAngle.LOWER;
            } else {
               return other.getValue(SHIFTED) ? RoofTiles.SlopeAngle.LOWER : RoofTiles.SlopeAngle.LEVEL;
            }
         } else {
            return !other.isAir() ? RoofTiles.SlopeAngle.LEVEL : RoofTiles.SlopeAngle.LOWER;
         }
      } else if (other.getBlock() instanceof RoofTiles tileLevel) {
         if (!(Boolean)other.getValue(SHIFTED)) {
            return RoofTiles.SlopeAngle.HIGHER;
         } else {
            return tileLevel.getType() == RoofTiles.RoofTileType.STAIRS ? RoofTiles.SlopeAngle.LEVEL : RoofTiles.SlopeAngle.LOWER;
         }
      } else {
         return !other.isAir() ? RoofTiles.SlopeAngle.HIGHER : RoofTiles.SlopeAngle.LEVEL;
      }
   }

   private RoofTiles.SlopeAngle checkForwardSlopeAngle(Level level, BlockState self, BlockPos selfPos) {
      Boolean selfShifted = (Boolean)self.getValue(SHIFTED);
      Direction direction = (Direction)self.getValue(FACING);
      RoofTiles.RoofTileType type = ((RoofTiles)self.getBlock()).getType();
      BlockState blockForward = level.getBlockState(selfPos.relative(direction.getOpposite()));
      if (type != RoofTiles.RoofTileType.STAIRS) {
         if (selfShifted) {
            if (blockForward.getBlock() instanceof RoofTiles tileForward) {
               if (!(Boolean)blockForward.getValue(SHIFTED)) {
                  return RoofTiles.SlopeAngle.HIGHER;
               }

               return tileForward.getType() == RoofTiles.RoofTileType.STAIRS ? RoofTiles.SlopeAngle.HIGHER : RoofTiles.SlopeAngle.LEVEL;
            }

            BlockState blockForwardBelow = level.getBlockState(selfPos.relative(direction.getOpposite()).above());
            if (blockForwardBelow.getBlock() instanceof RoofTiles && !(Boolean)blockForwardBelow.getValue(SHIFTED)) {
               return RoofTiles.SlopeAngle.LOWER;
            }
         } else {
            BlockState blockForwardAbove = level.getBlockState(selfPos.relative(direction.getOpposite()).above());
            if (blockForwardAbove.getBlock() instanceof RoofTiles && (Boolean)blockForwardAbove.getValue(SHIFTED)) {
               return RoofTiles.SlopeAngle.HIGHER;
            }

            if (blockForward.getBlock() instanceof RoofTiles tileForward) {
               if (tileForward.getType() == RoofTiles.RoofTileType.STAIRS) {
                  return blockForward.getValue(SHIFTED) ? RoofTiles.SlopeAngle.LOWER : RoofTiles.SlopeAngle.HIGHER;
               }

               return blockForward.getValue(SHIFTED) ? RoofTiles.SlopeAngle.LOWER : RoofTiles.SlopeAngle.LEVEL;
            }
         }
      } else if (selfShifted) {
         if (blockForward.getBlock() instanceof RoofTiles tileForward && tileForward.getType() != RoofTiles.RoofTileType.STAIRS) {
            return RoofTiles.SlopeAngle.LOWER;
         }

         if (!blockForward.isAir()) {
            return RoofTiles.SlopeAngle.HIGHER;
         }

         BlockState blockForwardBelow = level.getBlockState(selfPos.relative(direction.getOpposite()).above());
         if (blockForwardBelow.getBlock() instanceof RoofTiles tilesForwardBelow) {
            if ((Boolean)blockForwardBelow.getValue(SHIFTED)) {
               return tilesForwardBelow.getType() == RoofTiles.RoofTileType.STAIRS ? RoofTiles.SlopeAngle.LOWER : RoofTiles.SlopeAngle.HIGHER;
            }

            return RoofTiles.SlopeAngle.LOWER;
         }
      } else {
         BlockState blockForwardAbove = level.getBlockState(selfPos.relative(direction.getOpposite()).above());
         if (blockForwardAbove.getBlock() instanceof RoofTiles && (Boolean)blockForwardAbove.getValue(SHIFTED)) {
            return RoofTiles.SlopeAngle.HIGHER;
         }

         if (blockForward.getBlock() instanceof RoofTiles tileForward) {
            if ((Boolean)blockForward.getValue(SHIFTED)) {
               return tileForward.getType() == RoofTiles.RoofTileType.STAIRS ? RoofTiles.SlopeAngle.HIGHER : RoofTiles.SlopeAngle.LOWER;
            }

            return RoofTiles.SlopeAngle.LOWER;
         }
      }

      return RoofTiles.SlopeAngle.HIGHER;
   }

   public DyeColor getColor() {
      return this.color;
   }

   public RoofTiles.RoofTileType getType() {
      return this.type;
   }

   @Override
   protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
      super.createBlockStateDefinition(pBuilder);
      pBuilder.add(new Property[]{FACING});
      pBuilder.add(new Property[]{SNOW_LAYERS});
      pBuilder.add(new Property[]{SNOW_SIDE});
   }

   @Override
   public BlockState getStateForPlacement(BlockPlaceContext pContext) {
      return (BlockState)super.getStateForPlacement(pContext).setValue(FACING, pContext.getHorizontalDirection());
   }

   public BlockState rotate(BlockState pState, Rotation pRot) {
      return (BlockState)pState.setValue(FACING, pRot.rotate((Direction)pState.getValue(FACING)));
   }

   public BlockState mirror(BlockState pState, Mirror pMirror) {
      return pState.rotate(pMirror.getRotation((Direction)pState.getValue(FACING)));
   }

   public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      Boolean shifted = (Boolean)pState.getValue(SHIFTED);
      if (this.type != RoofTiles.RoofTileType.STAIRS) {
         return shifted ? Block.box(0.0, -8.0, 0.0, 16.0, 0.0, 16.0) : Block.box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0);
      }

      return switch ((Direction)pState.getValue(FACING)) {
         case NORTH -> shifted ? NORTH_SHAPE_SHIFTED : NORTH_SHAPE;
         case SOUTH -> shifted ? SOUTH_SHAPE_SHIFTED : SOUTH_SHAPE;
         case WEST -> shifted ? WEST_SHAPE_SHIFTED : WEST_SHAPE;
         case EAST -> shifted ? EAST_SHAPE_SHIFTED : EAST_SHAPE;
         default -> Shapes.empty();
      };
   }

   public enum RoofTileType implements ShiftedTileType {
      NORMAL(
         "roof_tiles",
         Map.ofEntries(
            Map.entry(0, Pair.of(0, false)),
            Map.entry(1, Pair.of(1, false)),
            Map.entry(2, Pair.of(2, false)),
            Map.entry(3, Pair.of(2, false)),
            Map.entry(4, Pair.of(3, true)),
            Map.entry(5, Pair.of(4, true)),
            Map.entry(6, Pair.of(4, true)),
            Map.entry(7, Pair.of(5, true)),
            Map.entry(8, Pair.of(6, true)),
            Map.entry(9, Pair.of(7, true)),
            Map.entry(10, Pair.of(7, true)),
            Map.entry(11, Pair.of(8, true)),
            Map.entry(12, Pair.of(9, true)),
            Map.entry(13, Pair.of(10, true)),
            Map.entry(14, Pair.of(11, true)),
            Map.entry(15, Pair.of(11, true))
         ),
         11
      ),
      STAIRS(
         "roof_tile_stairs",
         Map.ofEntries(
            Map.entry(0, Pair.of(0, false)),
            Map.entry(1, Pair.of(1, false)),
            Map.entry(2, Pair.of(1, false)),
            Map.entry(3, Pair.of(1, false)),
            Map.entry(4, Pair.of(1, false)),
            Map.entry(5, Pair.of(1, false)),
            Map.entry(6, Pair.of(2, false)),
            Map.entry(7, Pair.of(2, false)),
            Map.entry(8, Pair.of(2, false)),
            Map.entry(9, Pair.of(3, false)),
            Map.entry(10, Pair.of(3, false)),
            Map.entry(11, Pair.of(3, false)),
            Map.entry(12, Pair.of(4, false)),
            Map.entry(13, Pair.of(4, false)),
            Map.entry(14, Pair.of(4, false)),
            Map.entry(15, Pair.of(5, false))
         ),
         5
      ),
      EDGE(
         "roof_tile_edge",
         Map.ofEntries(
            Map.entry(0, Pair.of(0, false)),
            Map.entry(1, Pair.of(1, true)),
            Map.entry(2, Pair.of(1, true)),
            Map.entry(3, Pair.of(1, true)),
            Map.entry(4, Pair.of(1, true)),
            Map.entry(5, Pair.of(1, true)),
            Map.entry(6, Pair.of(1, true)),
            Map.entry(7, Pair.of(2, true)),
            Map.entry(8, Pair.of(2, true)),
            Map.entry(9, Pair.of(2, true)),
            Map.entry(10, Pair.of(2, true)),
            Map.entry(11, Pair.of(2, true)),
            Map.entry(12, Pair.of(2, true)),
            Map.entry(13, Pair.of(3, true)),
            Map.entry(14, Pair.of(3, true)),
            Map.entry(15, Pair.of(3, true))
         ),
         3
      );

      final String blockName;
      final Map<Integer, Pair<Integer, Boolean>> snowStages;
      final Integer maxSnowStages;

      RoofTileType(String blockName, Map<Integer, Pair<Integer, Boolean>> snowStages, int maxSnowStages) {
         this.blockName = blockName;
         this.snowStages = snowStages;
         this.maxSnowStages = maxSnowStages;
      }

      public Map<Integer, Pair<Integer, Boolean>> getSnowStages() {
         return this.snowStages;
      }

      public Integer getMaxSnowStages() {
         return this.maxSnowStages;
      }

      @Override
      public String toString() {
         return this.blockName;
      }
   }

   private enum SlopeAngle {
      HIGHER,
      LOWER,
      LEVEL;
   }

   public enum SnowSide implements StringRepresentable {
      LEFT("left"),
      RIGHT("right"),
      NONE("none"),
      BOTH("both");

      final String name;

      SnowSide(String name) {
         this.name = name;
      }

      public static RoofTiles.SnowSide add(RoofTiles.SnowSide current, RoofTiles.SnowSide toAdd) {
         if (toAdd == NONE) {
            return current;
         } else if (current != LEFT && current != RIGHT) {
            return current == NONE ? toAdd : current;
         } else {
            return current == toAdd ? current : BOTH;
         }
      }

      public static RoofTiles.SnowSide remove(RoofTiles.SnowSide current, RoofTiles.SnowSide toRemove) {
         if (toRemove == NONE) {
            return current;
         }

         if (current != LEFT && current != RIGHT) {
            if (current != BOTH) {
               return current;
            } else if (toRemove == BOTH) {
               return NONE;
            } else {
               return toRemove == LEFT ? RIGHT : LEFT;
            }
         } else {
            return toRemove != BOTH && current != toRemove ? current : NONE;
         }
      }

      public String getSerializedName() {
         return this.name;
      }
   }
}
