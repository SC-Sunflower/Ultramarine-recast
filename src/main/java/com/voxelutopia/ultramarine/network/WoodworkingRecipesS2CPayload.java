package com.voxelutopia.ultramarine.network;

import com.voxelutopia.ultramarine.Ultramarine;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * Server-to-client sync of the woodworking workbench recipe candidates.
 * The server computes the matching recipes whenever the input slot changes and
 * pushes their results/ids here, so the client never depends on its own recipe
 * manager (which may be empty on a dedicated-server connection in some setups).
 *
 * @param results    result stacks shown on the recipe buttons
 * @param recipeIds  matching recipe ids
 */
public record WoodworkingRecipesS2CPayload(List<ItemStack> results, List<ResourceLocation> recipeIds)
      implements CustomPacketPayload {
   public static final Type<WoodworkingRecipesS2CPayload> TYPE =
         new Type<>(ResourceLocation.fromNamespaceAndPath(Ultramarine.MOD_ID, "woodworking_recipes"));

   public static final StreamCodec<RegistryFriendlyByteBuf, WoodworkingRecipesS2CPayload> STREAM_CODEC = StreamCodec.composite(
         ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list()), WoodworkingRecipesS2CPayload::results,
         ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.list()), WoodworkingRecipesS2CPayload::recipeIds,
         WoodworkingRecipesS2CPayload::new
   );

   @Override
   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }
}
