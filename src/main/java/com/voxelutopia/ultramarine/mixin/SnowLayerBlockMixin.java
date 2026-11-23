package com.voxelutopia.ultramarine.mixin;

import com.voxelutopia.ultramarine.world.block.SnowRoofRidge;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Port of the Forge {@code NeighborNotifyEvent} handler from Ultramarine 0.5.1: snow falling
 * onto a snow roof ridge is absorbed as an extra snow layer. The event-based listener used on
 * the 1.20.1 build relies on Porting Lib's block events, which do not exist in the 1.20.2
 * Porting Lib line, so the check is attached to the snow layer's own random tick instead.
 */
@Mixin(SnowLayerBlock.class)
public abstract class SnowLayerBlockMixin {
    @Inject(method = "randomTick", at = @At("HEAD"))
    private void ultramarine$absorbSnowOnRoofRidge(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {
        BlockState below = level.getBlockState(pos.below());
        if (below.getBlock() instanceof SnowRoofRidge ridge && below.getValue(SnowRoofRidge.SNOW_LAYERS) < 15) {
            ridge.handleSnow(below, level, pos.below());
            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        }
    }
}
