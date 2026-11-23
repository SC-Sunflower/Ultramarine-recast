package com.voxelutopia.ultramarine.world.block;

import com.voxelutopia.ultramarine.world.block.state.ModBlockStateProperties;
import com.voxelutopia.ultramarine.world.block.state.StackableBlockType;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class StackableHalfBlock extends BaseBlock {
   public static final EnumProperty<StackableBlockType> TYPE = ModBlockStateProperties.STACKABLE_BLOCK_TYPE;
   protected static final VoxelShape HALF = Block.box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0);

   public StackableHalfBlock(BaseBlockProperty property) {
      super(property);
      this.registerDefaultState((BlockState)this.defaultBlockState().setValue(TYPE, StackableBlockType.SINGLE));
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
      pBuilder.add(new Property[]{TYPE});
   }

   public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      StackableBlockType type = (StackableBlockType)pState.getValue(TYPE);

      return switch (type) {
         case DOUBLE -> Shapes.block();
         case SINGLE -> HALF;
      };
   }

   public boolean useShapeForLightOcclusion(BlockState pState) {
      return false;
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext pContext) {
      BlockPos blockpos = pContext.getClickedPos();
      BlockState blockstate = pContext.getLevel().getBlockState(blockpos);
      return blockstate.is(this)
         ? (BlockState)blockstate.setValue(TYPE, StackableBlockType.DOUBLE)
         : (BlockState)this.defaultBlockState().setValue(TYPE, StackableBlockType.SINGLE);
   }

   public boolean canBeReplaced(BlockState pState, BlockPlaceContext pUseContext) {
      ItemStack itemstack = pUseContext.getItemInHand();
      StackableBlockType type = (StackableBlockType)pState.getValue(TYPE);
      return type != StackableBlockType.DOUBLE && itemstack.is(this.asItem());
   }
}
