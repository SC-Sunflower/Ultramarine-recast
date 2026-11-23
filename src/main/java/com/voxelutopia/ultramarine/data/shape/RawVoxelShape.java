package com.voxelutopia.ultramarine.data.shape;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector3f;

public class RawVoxelShape {
   private final Vector3f minP;
   private final Vector3f maxP;
   private final Vector3f XZcorner1;
   private final Vector3f XZcorner2;

   public RawVoxelShape(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
      this.minP = new Vector3f((float)minX, (float)minY, (float)minZ);
      this.maxP = new Vector3f((float)maxX, (float)maxY, (float)maxZ);
      this.XZcorner1 = new Vector3f((float)minX, (float)minY, (float)maxZ);
      this.XZcorner2 = new Vector3f((float)maxX, (float)minY, (float)minZ);
   }

   public RawVoxelShape() {
      this(0.0, 0.0, 0.0, 16.0, 16.0, 16.0);
   }

   public RawVoxelShape rotateY(float degrees) {
      float radians = (float)Math.toRadians(degrees);
      Vector3f t = new Vector3f(8.0F);
      this.minP.sub(t).rotateY(radians).add(t);
      this.maxP.sub(t).rotateY(radians).add(t);
      this.XZcorner1.sub(t).rotateY(radians).add(t);
      this.XZcorner2.sub(t).rotateY(radians).add(t);
      return this;
   }

   public RawVoxelShape rotateZ(float degrees) {
      float radians = (float)Math.toRadians(degrees);
      Vector3f t = new Vector3f(8.0F);
      this.minP.sub(t).rotateZ(radians).add(t);
      this.maxP.sub(t).rotateZ(radians).add(t);
      this.XZcorner1.sub(t).rotateZ(radians).add(t);
      this.XZcorner2.sub(t).rotateZ(radians).add(t);
      return this;
   }

   public RawVoxelShape mirrorZ() {
      this.minP.mul(-1.0F, 1.0F, 1.0F).add(16.0F, 0.0F, 0.0F);
      this.maxP.mul(-1.0F, 1.0F, 1.0F).add(16.0F, 0.0F, 0.0F);
      this.XZcorner1.mul(-1.0F, 1.0F, 1.0F).add(16.0F, 0.0F, 0.0F);
      this.XZcorner2.mul(-1.0F, 1.0F, 1.0F).add(16.0F, 0.0F, 0.0F);
      return this;
   }

   public RawVoxelShape mirrorY() {
      this.minP.mul(1.0F, -1.0F, 1.0F).add(0.0F, 16.0F, 0.0F);
      this.maxP.mul(1.0F, -1.0F, 1.0F).add(0.0F, 16.0F, 0.0F);
      this.XZcorner1.mul(1.0F, -1.0F, 1.0F).add(0.0F, 16.0F, 0.0F);
      this.XZcorner2.mul(1.0F, -1.0F, 1.0F).add(0.0F, 16.0F, 0.0F);
      return this;
   }

   public RawVoxelShape copy() {
      return new RawVoxelShape(this.minP.x(), this.minP.y(), this.minP.z(), this.maxP.x(), this.maxP.y(), this.maxP.z());
   }

   public VoxelShape toVoxelShape() {
      Vector3f newMinP = new Vector3f(
         Math.min(Math.min(this.minP.x(), this.maxP.x()), Math.min(this.XZcorner1.x(), this.XZcorner2.x())),
         Math.min(Math.min(this.minP.y(), this.maxP.y()), Math.min(this.XZcorner1.y(), this.XZcorner2.y())),
         Math.min(Math.min(this.minP.z(), this.maxP.z()), Math.min(this.XZcorner1.z(), this.XZcorner2.z()))
      );
      Vector3f newMaxP = new Vector3f(
         Math.max(Math.max(this.minP.x(), this.maxP.x()), Math.max(this.XZcorner1.x(), this.XZcorner2.x())),
         Math.max(Math.max(this.minP.y(), this.maxP.y()), Math.max(this.XZcorner1.y(), this.XZcorner2.y())),
         Math.max(Math.max(this.minP.z(), this.maxP.z()), Math.max(this.XZcorner1.z(), this.XZcorner2.z()))
      );
      return Block.box(newMinP.x(), newMinP.y(), newMinP.z(), newMaxP.x(), newMaxP.y(), newMaxP.z());
   }
}
