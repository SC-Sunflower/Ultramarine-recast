package com.voxelutopia.ultramarine.world.entity;

import com.voxelutopia.ultramarine.data.registry.EntityTypeRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class SeatEntity extends Entity {
   private final int MAX_LIFE = 20;
   private int life = 0;

   public SeatEntity(EntityType<? extends SeatEntity> pEntityType, Level pLevel) {
      super(pEntityType, pLevel);
   }

   public SeatEntity(Level level, Vec3 pos) {
      this(EntityTypeRegistry.SEAT, level);
      this.moveTo(pos);
   }

   public void tick() {
      super.tick();
      if (!this.level().isClientSide) {
         if (this.getPassengers().isEmpty() || this.level().isEmptyBlock(this.blockPosition())) {
            this.life++;
         }

         if (this.life > MAX_LIFE) {
            this.discard();
         }
      }
   }

   public double getPassengersRidingOffset() {
      return 0.0;
   }

   protected boolean canRide(Entity entity) {
      return true;
   }

   protected void defineSynchedData(SynchedEntityData.Builder builder) {
   }

   protected void readAdditionalSaveData(CompoundTag pCompound) {
      this.life = pCompound.getInt("Life");
   }

   protected void addAdditionalSaveData(CompoundTag pCompound) {
      pCompound.putInt("Life", this.life);
   }

   public Packet<ClientGamePacketListener> getAddEntityPacket() {
      return new ClientboundAddEntityPacket(this);
   }
}
