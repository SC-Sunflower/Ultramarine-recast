package com.voxelutopia.ultramarine.world.block;

import com.voxelutopia.ultramarine.data.shape.BlockShapes;
import com.voxelutopia.ultramarine.data.shape.ShapeFunction;
import com.voxelutopia.ultramarine.world.block.state.ModBlockStateProperties;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class DecorativeBlock extends HorizontalDirectionalBlock implements BaseBlockPropertyHolder, DiagonallyPlaceable {
   /**
    * 1.20.6 起 BlockBehaviour 要求实现 codec()。DecorativeBlock 的构造参数（形状/朝向等）
    * 无法从 Properties 还原，本模组方块也不参与结构方块序列化，故返回当前实例。
    */
   @Override
   protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
      return BlockBehaviour.simpleCodec(props -> this);
   }
   public static final DirectionProperty HORIZONTAL_FACING_SHIFT = ModBlockStateProperties.HORIZONTAL_FACING_SHIFT;
   public static final BooleanProperty LIT = BlockStateProperties.LIT;
   private final BaseBlockProperty property;
   private final ShapeFunction shapeFunction;
   private final boolean diagonallyPlaceable;
   private final boolean directional;
   private final boolean noCollision;
   private final boolean luminous;
   private final boolean noFenceConnect;
   @Nullable
   private final Direction offsetDirection;
   protected StateDefinition<Block, BlockState> stateDefinition;

   public DecorativeBlock(
      BaseBlockProperty property,
      ShapeFunction shapeFunction,
      boolean directional,
      boolean diagonallyPlaceable,
      boolean luminous,
      boolean noCollision,
      boolean noFenceConnect,
      @Nullable Direction offset
   ) {
      super(luminous ? property.properties.lightLevel(statex -> statex.hasProperty(LIT) && statex.getValue(LIT) ? 15 : 0) : property.properties);
      this.property = property;
      this.shapeFunction = shapeFunction;
      this.directional = directional;
      this.diagonallyPlaceable = diagonallyPlaceable;
      this.luminous = luminous;
      this.noCollision = noCollision;
      this.noFenceConnect = noFenceConnect;
      this.offsetDirection = offset;
      net.minecraft.world.level.block.state.StateDefinition.Builder<Block, BlockState> stateDefinationBuilder = new net.minecraft.world.level.block.state.StateDefinition.Builder(
         this
      );
      this.createBlockStateDefinition(stateDefinationBuilder);
      this.stateDefinition = stateDefinationBuilder.create(Block::defaultBlockState, BlockState::new);
      BlockState state = (BlockState)this.getStateDefinition().any();
      if (this.isDiagonallyPlaceable()) {
         state = (BlockState)state.setValue(DIAGONAL, false);
      }

      if (this.isDirectional()) {
         state = (BlockState)state.setValue(FACING, Direction.NORTH);
      }

      if (this.isDiagonallyPlaceable() && this.isDirectional()) {
         state = (BlockState)state.setValue(HORIZONTAL_FACING_SHIFT, Direction.NORTH);
      }

      if (this.isLuminous()) {
         state = (BlockState)state.setValue(LIT, true);
      }

      this.registerDefaultState(state);
   }

   public DecorativeBlock(DecorativeBlock.Builder builder) {
      this(
         builder.property,
         builder.shapeFunction,
         builder.directional,
         builder.diagonallyPlaceable,
         builder.luminous,
         builder.noCollision,
         builder.noFenceConnect,
         builder.offset
      );
   }

   public static DecorativeBlock.Builder with(BaseBlockProperty property) {
      return new DecorativeBlock.Builder(property);
   }

   public StateDefinition<Block, BlockState> getStateDefinition() {
      return this.stateDefinition;
   }

   @NotNull
   public BlockState getStateForPlacement(BlockPlaceContext pContext) {
      BlockState state = this.setDiagonalStateForPlacement(this.defaultBlockState(), pContext);
      if (this.isDirectional() && this.isDiagonallyPlaceable()) {
         Pair<Direction, Direction> directions = this.getMainAndShiftedDirections(pContext);
         state = (BlockState)((BlockState)((BlockState)state.setValue(FACING, (Direction)directions.getLeft()))
               .setValue(HORIZONTAL_FACING_SHIFT, (Direction)directions.getRight()))
            .setValue(DIAGONAL, this.getDiagonalState(pContext));
      } else if (this.isDirectional() && !this.isDiagonallyPlaceable()) {
         state = (BlockState)state.setValue(FACING, pContext.getHorizontalDirection().getOpposite());
      } else if (!this.isDirectional() && this.isDiagonallyPlaceable()) {
         state = (BlockState)state.setValue(DIAGONAL, this.getDiagonalState(pContext));
      }

      if (this.isLuminous()) {
         state = (BlockState)state.setValue(LIT, true);
      }

      return state;
   }

   protected void createBlockStateDefinition(net.minecraft.world.level.block.state.StateDefinition.Builder<Block, BlockState> pBuilder) {
      super.createBlockStateDefinition(pBuilder);
      if (this.isDirectional()) {
         pBuilder.add(new Property[]{FACING});
      }

      if (this.isDiagonallyPlaceable()) {
         pBuilder.add(new Property[]{DIAGONAL});
      }

      if (this.isDirectional() && this.isDiagonallyPlaceable()) {
         pBuilder.add(new Property[]{HORIZONTAL_FACING_SHIFT});
      }

      if (this.isLuminous()) {
         pBuilder.add(new Property[]{LIT});
      }
   }

   public void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
   }

   public BlockState rotate(BlockState pState, Rotation pRot) {
      BlockState newState = pState;
      if (pState.getBlock() instanceof DecorativeBlock decorativeBlock && decorativeBlock.isDirectional()) {
         newState = (BlockState)pState.setValue(FACING, pRot.rotate((Direction)pState.getValue(FACING)));
         if (decorativeBlock.isDiagonallyPlaceable()) {
            newState = (BlockState)newState.setValue(HORIZONTAL_FACING_SHIFT, pRot.rotate((Direction)pState.getValue(HORIZONTAL_FACING_SHIFT)));
         }
      }

      return newState;
   }

   public BlockState mirror(BlockState pState, Mirror pMirror) {
      BlockState newState = pState;
      if (pState.getBlock() instanceof DecorativeBlock decorativeBlock && decorativeBlock.isDirectional()) {
         newState = pState.rotate(pMirror.getRotation((Direction)pState.getValue(FACING)));
         if (decorativeBlock.isDiagonallyPlaceable()) {
            newState = newState.rotate(pMirror.getRotation((Direction)pState.getValue(HORIZONTAL_FACING_SHIFT)));
         }
      }

      return newState;
   }

   public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      return this.shapeFunction.apply(pState);
   }

   public VoxelShape getCollisionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      return this.noCollision ? Shapes.empty() : this.getShape(pState, pLevel, pPos, pContext);
   }

   public RenderShape getRenderShape(BlockState pState) {
      return RenderShape.MODEL;
   }

   @Override
   public BaseBlockProperty getProperty() {
      return this.property;
   }

   @Override
   public boolean isDiagonallyPlaceable() {
      return this.diagonallyPlaceable;
   }

   public boolean isLuminous() {
      return this.luminous;
   }

   public boolean isDirectional() {
      return this.directional;
   }

   public VoxelShape getBlockSupportShape(BlockState pState, BlockGetter pReader, BlockPos pPos) {
      return this.noFenceConnect ? BlockShapes.S16_H12.apply(pState) : super.getBlockSupportShape(pState, pReader, pPos);
   }

   public float getShadeBrightness(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
      return 1.0F;
   }

   public abstract static class AbstractBuilder<T extends DecorativeBlock.AbstractBuilder<T>> {
      public abstract T self();
   }

   public static class Builder extends DecorativeBlock.AbstractBuilder<DecorativeBlock.Builder> {
      private final BaseBlockProperty property;
      private ShapeFunction shapeFunction = BlockShapes.S16_H16;
      private boolean diagonallyPlaceable;
      private boolean directional;
      private boolean luminous;
      private boolean noCollision;
      private boolean noFenceConnect;
      private Direction offset = null;

      public Builder(BaseBlockProperty property) {
         this.property = property.copy();
      }

      public DecorativeBlock.Builder shaped(VoxelShape shape) {
         return this.shaped(ShapeFunction.simpleShape(shape));
      }

      public DecorativeBlock.Builder shaped(ShapeFunction shapeFunction) {
         this.shapeFunction = shapeFunction;
         return this;
      }

      public DecorativeBlock.Builder diagonallyPlaceable() {
         this.diagonallyPlaceable = true;
         return this;
      }

      public DecorativeBlock.Builder directional() {
         this.directional = true;
         return this;
      }

      public DecorativeBlock.Builder luminous() {
         this.luminous = true;
         this.property.properties = this.property
            .properties
            .lightLevel(state -> state.hasProperty(DecorativeBlock.LIT) && state.getValue(DecorativeBlock.LIT) ? 15 : 0);
         return this;
      }

      public DecorativeBlock.Builder luminous(boolean luminous) {
         this.luminous = luminous;
         if (luminous) {
            this.property.properties = this.property
               .properties
               .lightLevel(state -> state.hasProperty(DecorativeBlock.LIT) && state.getValue(DecorativeBlock.LIT) ? 15 : 0);
         }

         return this;
      }

      public DecorativeBlock.Builder noCollision() {
         this.noCollision = true;
         return this;
      }

      public DecorativeBlock.Builder pushReaction(PushReaction reaction) {
         this.property.properties.pushReaction(reaction);
         return this;
      }

      public DecorativeBlock.Builder noOcclusion() {
         this.property.properties.noOcclusion();
         return this;
      }

      public DecorativeBlock.Builder noFenceConnect() {
         this.noFenceConnect = true;
         return this;
      }

      public DecorativeBlock.Builder placeOffset(Direction direction) {
         this.offset = direction;
         return this;
      }

      public DecorativeBlock build() {
         return new DecorativeBlock(this);
      }

      public DecorativeBlock.Builder self() {
         return this;
      }
   }
}
