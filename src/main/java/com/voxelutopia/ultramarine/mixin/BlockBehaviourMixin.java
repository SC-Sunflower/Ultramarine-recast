package com.voxelutopia.ultramarine.mixin;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 1.21.2+ requires every Block's Properties to carry a non-null id
 * (BlockBehaviour construction reads Properties.getId() and throws
 * "Block id not set"). The mod constructs ~200 blocks through many
 * direct/inherited constructors, so instead of patching every constructor,
 * inject once at the root: assign a placeholder id before the base
 * constructor body runs. The real registry id is assigned separately
 * by Registry.register and is unaffected by this placeholder.
 */
@Mixin(BlockBehaviour.class)
public abstract class BlockBehaviourMixin {
    @Inject(method = "<init>(Lnet/minecraft/world/level/block/state/BlockBehaviour$Properties;)V", at = @At("HEAD"))
    private void ultramarine$ensureBlockId(BlockBehaviour.Properties properties, CallbackInfo ci) {
        properties.setId(ResourceKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("ultramarine", "dummy")));
    }
}
