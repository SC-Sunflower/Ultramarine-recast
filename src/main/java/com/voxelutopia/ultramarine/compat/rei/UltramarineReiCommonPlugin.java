package com.voxelutopia.ultramarine.compat.rei;

import com.voxelutopia.ultramarine.Ultramarine;
import com.voxelutopia.ultramarine.data.recipe.ChiselTableRecipe;
import com.voxelutopia.ultramarine.data.recipe.CompositeSmeltingRecipe;
import com.voxelutopia.ultramarine.data.recipe.WoodworkingRecipe;
import com.voxelutopia.ultramarine.data.registry.RecipeTypeRegistry;
import java.util.List;
import me.shedaniel.rei.api.common.display.DisplaySerializerRegistry;
import me.shedaniel.rei.api.common.plugins.REICommonPlugin;
import me.shedaniel.rei.api.common.registry.display.ServerDisplayRegistry;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.resources.ResourceLocation;

/**
 * Common (server + client) REI plugin for Ultramarine Recast.
 *
 * The 1.21.2 client no longer holds custom recipes, so displays are filled on the
 * server from the server-side recipe manager and synced to clients through REI's
 * display sync protocol.
 */
public class UltramarineReiCommonPlugin implements REICommonPlugin {
    @Override
    public void registerDisplays(ServerDisplayRegistry registry) {
        registry.beginRecipeFiller(WoodworkingRecipe.class)
                .filterType(RecipeTypeRegistry.WOODWORKING)
                .fill(holder -> new UltramarineDisplay(
                        UltramarineReiPlugin.WOODWORKING, UltramarineReiPlugin.WOODWORKING.getNamespace(), UltramarineReiPlugin.WOODWORKING.getPath(),
                        List.of(EntryIngredients.ofIngredient(holder.value().getIngredient())),
                        List.of(EntryIngredients.of(holder.value().getResultStack())),
                        java.util.Optional.of(holder.id().location())));

        registry.beginRecipeFiller(ChiselTableRecipe.class)
                .filterType(RecipeTypeRegistry.CHISEL_TABLE)
                .fill(holder -> new UltramarineDisplay(
                        UltramarineReiPlugin.CHISEL_TABLE, UltramarineReiPlugin.CHISEL_TABLE.getNamespace(), UltramarineReiPlugin.CHISEL_TABLE.getPath(),
                        List.of(EntryIngredients.ofIngredient(holder.value().getMaterial()), EntryIngredients.ofIngredient(holder.value().getTemplate())),
                        List.of(EntryIngredients.of(holder.value().getResultStack())),
                        java.util.Optional.of(holder.id().location())));

        registry.beginRecipeFiller(CompositeSmeltingRecipe.class)
                .filterType(RecipeTypeRegistry.COMPOSITE_SMELTING)
                .fill(holder -> new UltramarineDisplay(
                        UltramarineReiPlugin.COMPOSITE_SMELTING, UltramarineReiPlugin.COMPOSITE_SMELTING.getNamespace(), UltramarineReiPlugin.COMPOSITE_SMELTING.getPath(),
                        List.of(EntryIngredients.ofIngredient(holder.value().getPrimaryIngredient()), EntryIngredients.ofIngredient(holder.value().getSecondaryIngredient())),
                        List.of(EntryIngredients.of(holder.value().getResultStack())),
                        java.util.Optional.of(holder.id().location())));
    }

    @Override
    public void registerDisplaySerializer(DisplaySerializerRegistry registry) {
        registry.register(ResourceLocation.fromNamespaceAndPath(Ultramarine.MOD_ID, "custom_recipe"), UltramarineDisplay.SERIALIZER);
    }
}
