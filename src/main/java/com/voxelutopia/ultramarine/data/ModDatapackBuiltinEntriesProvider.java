package com.voxelutopia.ultramarine.data;

import com.voxelutopia.ultramarine.world.worldgen.ModConfiguredFeatures;
import com.voxelutopia.ultramarine.world.worldgen.ModPlacedFeatures;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;

public class ModDatapackBuiltinEntriesProvider extends FabricDynamicRegistryProvider {
    public static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.CONFIGURED_FEATURE, ModConfiguredFeatures::bootstrap)
            .add(Registries.PLACED_FEATURE, ModPlacedFeatures::bootstrap);

    public ModDatapackBuiltinEntriesProvider(FabricDataOutput output, CompletableFuture<Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(Provider registries, Entries entries) {
        entries.addAll(registries.lookupOrThrow(Registries.CONFIGURED_FEATURE));
        entries.addAll(registries.lookupOrThrow(Registries.PLACED_FEATURE));
    }

    @Override
    public String getName() {
        return "Ultramarine Built-in Datapack Registries";
    }
}
