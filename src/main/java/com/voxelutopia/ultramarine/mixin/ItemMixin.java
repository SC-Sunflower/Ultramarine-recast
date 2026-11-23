package com.voxelutopia.ultramarine.mixin;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Item.class)
public abstract class ItemMixin {
    @Inject(method = "<init>(Lnet/minecraft/world/item/Item$Properties;)V", at = @At("HEAD"))
    private static void ultramarine$ensureItemId(Item.Properties properties, CallbackInfo ci) {
        properties.setId(ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("ultramarine", "dummy")));
    }
}