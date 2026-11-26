package com.voxelutopia.ultramarine.compat.rei;

import com.voxelutopia.ultramarine.Ultramarine;
import com.voxelutopia.ultramarine.data.recipe.ChiselTableRecipe;
import com.voxelutopia.ultramarine.data.recipe.CompositeSmeltingRecipe;
import com.voxelutopia.ultramarine.data.recipe.WoodworkingRecipe;
import com.voxelutopia.ultramarine.data.registry.RecipeTypeRegistry;
import java.util.List;
import java.util.Optional;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.DisplaySerializerRegistry;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.plugins.REICommonPlugin;
import me.shedaniel.rei.api.common.registry.display.ServerDisplayRegistry;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeHolder;

/**
 * Common (server + client) REI plugin for Ultramarine Recast.
 *
 * The 1.21.2+ client no longer holds custom recipes, so displays are filled on the
 * server from the server-side recipe manager and synced to clients through REI's
 * display sync protocol.
 */
public class UltramarineReiCommonPlugin implements REICommonPlugin {
    @Override
    public void registerDisplays(ServerDisplayRegistry registry) {
        registry.beginRecipeFiller(WoodworkingRecipe.class)
                .filterType(RecipeTypeRegistry.WOODWORKING)
                .fill(UltramarineReiCommonPlugin::woodworkingDisplay);

        registry.beginRecipeFiller(ChiselTableRecipe.class)
                .filterType(RecipeTypeRegistry.CHISEL_TABLE)
                .fill(UltramarineReiCommonPlugin::chiselTableDisplay);

        registry.beginRecipeFiller(CompositeSmeltingRecipe.class)
                .filterType(RecipeTypeRegistry.COMPOSITE_SMELTING)
                .fill(UltramarineReiCommonPlugin::compositeSmeltingDisplay);
    }

    private static UltramarineDisplay woodworkingDisplay(RecipeHolder<WoodworkingRecipe> holder) {
        try {
            return newDisplay(UltramarineReiCategories.WOODWORKING,
                    List.of(EntryIngredients.ofIngredient(holder.value().getIngredient())),
                    List.of(EntryIngredients.of(holder.value().getResultStack())),
                    holder);
        } catch (Throwable t) {
            Ultramarine.LOGGER.error("REI woodworking filler failed for recipe {}", holder.id(), t);
            throw t;
        }
    }

    private static UltramarineDisplay chiselTableDisplay(RecipeHolder<ChiselTableRecipe> holder) {
        try {
            return newDisplay(UltramarineReiCategories.CHISEL_TABLE,
                    List.of(EntryIngredients.ofIngredient(holder.value().getMaterial()), EntryIngredients.ofIngredient(holder.value().getTemplate())),
                    List.of(EntryIngredients.of(holder.value().getResultStack())),
                    holder);
        } catch (Throwable t) {
            Ultramarine.LOGGER.error("REI chisel table filler failed for recipe {}", holder.id(), t);
            throw t;
        }
    }

    private static UltramarineDisplay compositeSmeltingDisplay(RecipeHolder<CompositeSmeltingRecipe> holder) {
        try {
            return newDisplay(UltramarineReiCategories.COMPOSITE_SMELTING,
                    List.of(EntryIngredients.ofIngredient(holder.value().getPrimaryIngredient()), EntryIngredients.ofIngredient(holder.value().getSecondaryIngredient())),
                    List.of(EntryIngredients.of(holder.value().getResultStack())),
                    holder);
        } catch (Throwable t) {
            Ultramarine.LOGGER.error("REI composite smelting filler failed for recipe {}", holder.id(), t);
            throw t;
        }
    }

    private static UltramarineDisplay newDisplay(CategoryIdentifier<UltramarineDisplay> category,
                                                 List<EntryIngredient> inputs,
                                                 List<EntryIngredient> outputs,
                                                 RecipeHolder<?> holder) {
        return new UltramarineDisplay(category, category.getNamespace(), category.getPath(),
                inputs, outputs, Optional.of(holder.id().identifier()));
    }

    @Override
    public void registerDisplaySerializer(DisplaySerializerRegistry registry) {
        registry.register(Identifier.fromNamespaceAndPath(Ultramarine.MOD_ID, "custom_recipe"), UltramarineDisplay.SERIALIZER);
    }
}