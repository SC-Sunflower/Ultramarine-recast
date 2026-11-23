package com.voxelutopia.ultramarine.mixin;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockBehaviour.class)
public abstract class BlockBehaviourMixin {    @Inject(method = "<init>(Lnet/minecraft/world/level/block/state/BlockBehaviour$Properties;)V", at = @At("HEAD"))
    private static void ultramarine$ensureBlockId(BlockBehaviour.Properties properties, CallbackInfo ci) {
        if (properties.getId() == null) {

           properties.setId(ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("ultramarine", "dummy")));

        }
    }
}