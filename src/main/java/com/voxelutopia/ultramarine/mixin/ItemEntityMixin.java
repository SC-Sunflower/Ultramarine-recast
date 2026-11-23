package com.voxelutopia.ultramarine.mixin;

import com.voxelutopia.ultramarine.data.registry.ItemRegistry;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Port of the Forge {@code ItemExpireEvent} handler from Ultramarine 0.5.1: a fired
 * brick about to despawn underwater converts into a cyan brick and lingers 6000 ticks
 * longer (matching {@code event.setExtraLife(6000)}).
 */
@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {
    @Shadow
    private int age;

    @Inject(method = "tick", at = @At("HEAD"))
    private void ultramarine$convertFiredBrickInWater(CallbackInfo ci) {
        ItemEntity self = (ItemEntity) (Object) this;
        if (self.level().isClientSide()) {
            return;
        }
        ItemStack stack = self.getItem();
        if (self.isInWater() && stack.is(ItemRegistry.FIRED_BRICK) && this.age + 1 >= 6000) {
            self.setItem(new ItemStack(ItemRegistry.CYAN_BRICK, stack.getCount()));
            self.level().playSound(null, self, SoundEvents.LAVA_EXTINGUISH, SoundSource.NEUTRAL, 0.5F, 1.0F);
            this.age -= 6000;
        }
    }
}
