package com.voxelutopia.ultramarine.world.block;

import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;

public class BaseHorizontalDirectionalBlock extends HorizontalDirectionalBlock implements BaseBlockPropertyHolder {
   /**
    * 1.20.6 起 BlockBehaviour 要求实现 codec()。DecorativeBlock 的构造参数（形状/朝向等）
    * 无法从 Properties 还原，本模组方块也不参与结构方块序列化，故返回当前实例。
    */
   @Override
   protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
      return BlockBehaviour.simpleCodec(props -> this);
   }
   protected final BaseBlockProperty property;
   public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

   public BaseHorizontalDirectionalBlock(BaseBlock block) {
      this(block.getProperty());
   }

   public BaseHorizontalDirectionalBlock(BaseBlockProperty property) {
      super(property.properties);
      this.property = property;
      this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.NORTH));
   }

   public BlockState getStateForPlacement(BlockPlaceContext pContext) {
      return (BlockState)this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection());
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
      pBuilder.add(new Property[]{FACING});
   }

   @Override
   public BaseBlockProperty getProperty() {
      return this.property;
   }
}
