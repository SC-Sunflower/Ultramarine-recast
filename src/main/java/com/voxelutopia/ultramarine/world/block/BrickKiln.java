package com.voxelutopia.ultramarine.world.block;

import com.voxelutopia.ultramarine.data.registry.BlockEntityRegistry;
import com.voxelutopia.ultramarine.world.block.entity.BlockEntityHelper;
import com.voxelutopia.ultramarine.world.block.entity.BrickKilnBlockEntity;
import io.github.fabricators_of_create.porting_lib.transfer.item.RecipeWrapper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class BrickKiln extends DecorativeBlock implements EntityBlock, BaseBlockPropertyHolder {
   public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
   public static final BooleanProperty LIT = BlockStateProperties.LIT;

   public BrickKiln() {
      super(DecorativeBlock.with(BaseBlockProperty.STONE).shaped(Block.box(0.0, 0.0, 0.0, 16.0, 15.0, 16.0)).directional().luminous().noOcclusion());
      this.registerDefaultState(
         (BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.NORTH))
            .setValue(LIT, Boolean.FALSE)
      );
   }

   @Nullable
   public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
      return new BrickKilnBlockEntity(pPos, pState);
   }

   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
      return pLevel.isClientSide
         ? null
         : BlockEntityHelper.createTickerHelper(pBlockEntityType, BlockEntityRegistry.BRICK_KILN, BrickKilnBlockEntity::serverTick);
   }

   public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
      if (pLevel.isClientSide) {
         return InteractionResult.SUCCESS;
      } else if (pLevel.getBlockEntity(pPos) instanceof BrickKilnBlockEntity furnaceBlockEntity) {
         pPlayer.openMenu(furnaceBlockEntity);
         return InteractionResult.CONSUME;
      } else {
         return InteractionResult.PASS;
      }
   }

   public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
      if (!pState.is(pNewState.getBlock())) {
         if (pLevel.getBlockEntity(pPos) instanceof BrickKilnBlockEntity furnace) {
            if (pLevel instanceof ServerLevel) {
               Containers.dropContents(pLevel, pPos, new RecipeWrapper(furnace.wrapHandlers()));
               furnace.getRecipesToAwardAndPopExperience((ServerLevel)pLevel, Vec3.atCenterOf(pPos));
            }

            pLevel.updateNeighbourForOutputSignal(pPos, this);
         }

         super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
      }
   }

   public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, RandomSource pRand) {
      if ((Boolean)pState.getValue(LIT)) {
         double d0 = pPos.getX() + 0.5;
         double d1 = pPos.getY();
         double d2 = pPos.getZ() + 0.5;
         if (pRand.nextDouble() < 0.1) {
            pLevel.playLocalSound(d0, d1, d2, SoundEvents.FURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, 1.0F, 1.0F, false);
         }

         Direction direction = (Direction)pState.getValue(FACING);
         Axis direction$axis = direction.getAxis();
         double d3 = 0.52;
         double d4 = pRand.nextDouble() * 0.6 - 0.3;
         double d5 = direction$axis == Axis.X ? direction.getStepX() * d3 : d4;
         double d6 = pRand.nextDouble() * 6.0 / 16.0;
         double d7 = direction$axis == Axis.Z ? direction.getStepZ() * d3 : d4;
         pLevel.addParticle(ParticleTypes.SMOKE, d0 + d5, d1 + d6, d2 + d7, 0.0, 0.0, 0.0);
         pLevel.addParticle(ParticleTypes.FLAME, d0 + d5, d1 + d6, d2 + d7, 0.0, 0.0, 0.0);
      }
   }

   @Override
   protected void createBlockStateDefinition(net.minecraft.world.level.block.state.StateDefinition.Builder<Block, BlockState> pBuilder) {
      super.createBlockStateDefinition(pBuilder);
   }

   @Override
   public BlockState getStateForPlacement(BlockPlaceContext pContext) {
      return (BlockState)this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection().getOpposite());
   }

   public boolean useShapeForLightOcclusion(BlockState pState) {
      return true;
   }

   public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
      return state.getValue(LIT) ? 15 : 0;
   }

   @Override
   public RenderShape getRenderShape(BlockState pState) {
      return RenderShape.MODEL;
   }

   @Override
   public BaseBlockProperty getProperty() {
      return BaseBlockProperty.STONE;
   }
}
