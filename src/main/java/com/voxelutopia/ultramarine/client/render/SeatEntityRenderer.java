package com.voxelutopia.ultramarine.client.render;

import com.voxelutopia.ultramarine.world.entity.SeatEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

/**
 * Seat entities are invisible; only the block underneath is rendered. Minecraft 1.21.2
 * moved entity rendering to a render-state model, so this renderer only creates an
 * empty state and draws nothing.
 */
public class SeatEntityRenderer extends EntityRenderer<SeatEntity, EntityRenderState> {
    public SeatEntityRenderer(Context context) {
        super(context);
    }

    @Override
    public EntityRenderState createRenderState() {
        return new EntityRenderState();
    }
}