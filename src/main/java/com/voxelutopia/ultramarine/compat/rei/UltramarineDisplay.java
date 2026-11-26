package com.voxelutopia.ultramarine.compat.rei;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

/**
 * REI display for the three custom recipe types (woodworking / chisel table /
 * composite smelting). Category + inputs/outputs are serialized so displays can
 * be favorites or travel over the network like vanilla displays.
 */
public class UltramarineDisplay extends BasicDisplay {
    private final CategoryIdentifier<?> category;
    private final String categoryNs;
    private final String categoryPath;

    public UltramarineDisplay(CategoryIdentifier<?> category, String categoryNs, String categoryPath,
                              List<EntryIngredient> inputs, List<EntryIngredient> outputs,
                              Optional<Identifier> location) {
        super(inputs, outputs, location);
        this.category = category;
        this.categoryNs = categoryNs;
        this.categoryPath = categoryPath;
    }

    public UltramarineDisplay(String categoryNs, String categoryPath,
                              List<EntryIngredient> inputs, List<EntryIngredient> outputs) {
        this(CategoryIdentifier.of(categoryNs, categoryPath), categoryNs, categoryPath, inputs, outputs, Optional.empty());
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return this.category;
    }

    @Override
    public DisplaySerializer<UltramarineDisplay> getSerializer() {
        return SERIALIZER;
    }

    public static final MapCodec<UltramarineDisplay> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("category_ns").forGetter(d -> d.categoryNs),
            Codec.STRING.fieldOf("category_path").forGetter(d -> d.categoryPath),
            Codec.list(EntryIngredient.codec()).fieldOf("inputs").forGetter(d -> d.getInputEntries()),
            Codec.list(EntryIngredient.codec()).fieldOf("outputs").forGetter(d -> d.getOutputEntries())
    ).apply(instance, (ns, path, inputs, outputs) -> new UltramarineDisplay(ns, path, inputs, outputs)));

    public static final StreamCodec<RegistryFriendlyByteBuf, UltramarineDisplay> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, d -> d.categoryNs,
            ByteBufCodecs.STRING_UTF8, d -> d.categoryPath,
            EntryIngredient.streamCodec().apply(ByteBufCodecs.list()), d -> d.getInputEntries(),
            EntryIngredient.streamCodec().apply(ByteBufCodecs.list()), d -> d.getOutputEntries(),
            UltramarineDisplay::new
    );

    public static final DisplaySerializer<UltramarineDisplay> SERIALIZER = DisplaySerializer.of(CODEC, STREAM_CODEC, false);
}