package com.voxelutopia.ultramarine.world.block.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.voxelutopia.ultramarine.data.registry.BlockEntityRegistry;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BottleGourdBlockEntity extends BlockEntity {
   public static final Codec<BottleGourdBlockEntity> CODEC = RecordCodecBuilder.create(
      instance -> instance.group(
            BlockPos.CODEC.fieldOf("pos").forGetter(entity -> entity.worldPosition),
            BlockState.CODEC.fieldOf("blockState").forGetter(entity -> entity.getBlockState()),
            BuiltInRegistries.POTION.byNameCodec().fieldOf("potion").forGetter(entity -> entity.potion),
            Codec.INT.fieldOf("charges").forGetter(entity -> entity.charges),
            Codec.BOOL.fieldOf("filled").forGetter(entity -> entity.filled)
         )
         .apply(instance, (pos, state, potion, charges, filled) -> {
            BottleGourdBlockEntity entity = new BottleGourdBlockEntity(pos, state);
            entity.potion = potion;
            entity.charges = charges;
            entity.filled = filled;
            return entity;
         })
   );
   public static final int MAX_CHARGE = 6;
   private Potion potion;
   private int charges;
   private boolean filled;

   public BottleGourdBlockEntity(BlockPos pos, BlockState state) {
      super(BlockEntityRegistry.BOTTLE_GOURD, pos, state);
   }

   public boolean addPotionCharge(Potion potion) {
      if (!this.filled) {
         this.potion = potion;
         this.charges = 1;
         this.filled = true;
         this.setChanged();
         return true;
      } else if (potion.equals(this.potion) && this.charges < MAX_CHARGE) {
         this.charges++;
         this.setChanged();
         return true;
      } else {
         return false;
      }
   }

   public Optional<Potion> takePotionCharge() {
      if (this.filled && this.charges > 0 && this.potion != null) {
         Potion charge = this.potion;
         this.charges--;
         if (this.charges <= 0) {
            this.filled = false;
            this.potion = null;
         }

         this.setChanged();
         return Optional.of(charge);
      } else {
         return Optional.empty();
      }
   }

   public boolean hasCharges() {
      return this.filled && this.charges > 0 && this.potion != null;
   }

   public int getCharges() {
      return this.charges;
   }

   public Potion getPotion() {
      return this.potion;
   }

   protected void loadAdditional(ValueInput pTag) {
      super.loadAdditional(pTag);
      this.potion = BuiltInRegistries.POTION.get(Identifier.tryParse(pTag.getStringOr("Potion", ""))).map(Holder.Reference::value).orElse(null);
      this.charges = pTag.getIntOr("Charges", 0);
      this.filled = pTag.getBooleanOr("Filled", false);
   }

   protected void saveAdditional(ValueOutput pTag) {
      super.saveAdditional(pTag);
      if (this.potion != null) {
         pTag.putString("Potion", BuiltInRegistries.POTION.getKey(this.potion).toString());
      }
      pTag.putInt("Charges", this.charges);
      pTag.putBoolean("Filled", this.filled);
   }
}
