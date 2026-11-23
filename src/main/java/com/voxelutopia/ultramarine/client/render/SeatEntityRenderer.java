package com.voxelutopia.ultramarine.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.voxelutopia.ultramarine.world.entity.SeatEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;

public class SeatEntityRenderer<T extends SeatEntity> extends EntityRenderer<T> {
   public SeatEntityRenderer(Context context) {
      super(context);
   }

   public ResourceLocation getTextureLocation(T pEntity) {
      return null;
   }

   public void render(T pEntity, float pEntityYaw, float pPartialTicks, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight) {
   }
}
