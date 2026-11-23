package com.voxelutopia.ultramarine.world.block;

import com.voxelutopia.ultramarine.data.shape.ShapeFunction;
import com.voxelutopia.ultramarine.world.block.state.ChiralBlockType;
import com.voxelutopia.ultramarine.world.block.state.ModBlockStateProperties;
import java.util.Arrays;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluids;

/**
 * Chiral orientable six-side block ported from Ultramarine 0.7 (NeoForge 1.21.1) to Fabric 1.20.1.
 */
public class ChiralOrientableSixSideBlock extends OrientableSixSideBlock {
    public static final EnumProperty<ChiralBlockType> TYPE = ModBlockStateProperties.CHIRAL_BLOCK_TYPE;

    public ChiralOrientableSixSideBlock(BaseBlockProperty property, ShapeFunction shapeFunction) {
        super(property, shapeFunction);
        this.registerDefaultState(
                this.getStateDefinition().any()
                        .setValue(FACING, Direction.NORTH)
                        .setValue(DIRECTION, Direction.EAST)
                        .setValue(TYPE, ChiralBlockType.LEFT)
                        .setValue(WATERLOGGED, false)
        );
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING, DIRECTION, TYPE, WATERLOGGED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        Direction face = pContext.getClickedFace();
        Direction direction;
        if (face != Direction.UP && face != Direction.DOWN) {
            direction = Arrays.stream(pContext.getNearestLookingDirections())
                    .filter(dir -> dir != face && dir != face.getOpposite() && !Axis.Y.test(dir))
                    .findFirst()
                    .orElse(Direction.EAST);
        } else {
            direction = Arrays.stream(pContext.getNearestLookingDirections())
                    .filter(dir -> dir != face && dir != face.getOpposite())
                    .findFirst()
                    .orElse(Direction.EAST);
        }

        return this.defaultBlockState()
                .setValue(WATERLOGGED, pContext.getLevel().getFluidState(pContext.getClickedPos()).getType() == Fluids.WATER)
                .setValue(FACING, face)
                .setValue(DIRECTION, direction);
    }
}
