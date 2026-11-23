package com.voxelutopia.ultramarine.mixin;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 1.21.11 uses Identifier instead of ResourceLocation; see the generic note
 * in the 1.21.2-1.21.10 variant of this mixin.
 */
@Mixin(BlockBehaviour.class)
public abstract class BlockBehaviourMixin {
    @Inject(method = "<init>(Lnet/minecraft/world/level/block/state/BlockBehaviour$Properties;)V", at = @At("HEAD"))
    private void ultramarine$ensureBlockId(BlockBehaviour.Properties properties, CallbackInfo ci) {
        properties.setId(ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("ultramarine", "dummy")));
    }
}
