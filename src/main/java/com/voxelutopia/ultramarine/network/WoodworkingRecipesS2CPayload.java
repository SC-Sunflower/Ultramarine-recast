package com.voxelutopia.ultramarine.network;

import com.voxelutopia.ultramarine.Ultramarine;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

/**
 * Server-to-client sync of the woodworking workbench recipe candidates.
 * 1.21.2 no longer gives the client a full {@code RecipeManager} (only the synced
 * stonecutter set and item properties), so the server sends the matching recipe
 * results and ids whenever the input slot changes.
 *
 * @param results    result stacks shown on the recipe buttons
 * @param recipeIds  matching recipe ids, used by REI to link displays
 */
public record WoodworkingRecipesS2CPayload(List<ItemStack> results, List<Identifier> recipeIds)
      implements CustomPacketPayload {
   public static final Type<WoodworkingRecipesS2CPayload> TYPE =
         CustomPacketPayload.createType("ultramarine:woodworking_recipes");

   public static final StreamCodec<RegistryFriendlyByteBuf, WoodworkingRecipesS2CPayload> STREAM_CODEC = StreamCodec.composite(
         ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list()), WoodworkingRecipesS2CPayload::results,
         Identifier.STREAM_CODEC.apply(ByteBufCodecs.list()), WoodworkingRecipesS2CPayload::recipeIds,
         WoodworkingRecipesS2CPayload::new
   );

   @Override
   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }
}
