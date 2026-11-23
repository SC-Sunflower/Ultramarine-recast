package com.voxelutopia.ultramarine.world.entity;

import com.voxelutopia.ultramarine.data.registry.EntityTypeRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class SeatEntity extends Entity {
   private final int MAX_LIFE = 20;
   private int life = 0;
   private static final EntityDataAccessor<Integer> LIFE = SynchedEntityData.defineId(SeatEntity.class, EntityDataSerializers.INT);

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

         if (this.life > 20) {
            this.discard();
         }
      }
   }

   public double getPassengersRidingOffset() {
      return 0.0;
   }

   public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
      return false;
   }

   protected boolean canRide(Entity entity) {
      return true;
   }

   protected void defineSynchedData(SynchedEntityData.Builder builder) {
      builder.define(LIFE, 0);
   }

   protected void readAdditionalSaveData(CompoundTag pCompound) {
      pCompound.getInt("Life");
   }

   protected void addAdditionalSaveData(CompoundTag pCompound) {
      pCompound.putInt("Life", this.life);
   }

   public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity entity) {
      return new ClientboundAddEntityPacket(this, entity);
   }
}
