package com.voxelutopia.ultramarine.world.block;

import com.voxelutopia.ultramarine.data.ModBlockTags;
import com.voxelutopia.ultramarine.data.registry.SoundRegistry;
import com.mojang.serialization.MapCodec;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.PushReaction;

public final class BaseBlockProperty {
   public static BaseBlockProperty STONE = new BaseBlockProperty(
      Properties.of().sound(SoundType.STONE).strength(1.5F, 6.0F).requiresCorrectToolForDrops().instrument(NoteBlockInstrument.BASEDRUM),
      BaseBlockProperty.BlockMaterial.STONE
   );
   public static BaseBlockProperty MARBLE = new BaseBlockProperty(
      Properties.of().sound(SoundType.STONE).strength(1.8F, 7.0F).requiresCorrectToolForDrops().instrument(NoteBlockInstrument.BASEDRUM),
      BaseBlockProperty.BlockMaterial.STONE
   );
   public static BaseBlockProperty TERRACOTTA = new BaseBlockProperty(
      Properties.of().sound(SoundType.STONE).strength(1.2F, 4.0F).requiresCorrectToolForDrops().instrument(NoteBlockInstrument.BASEDRUM),
      BaseBlockProperty.BlockMaterial.STONE
   );
   public static BaseBlockProperty IRON = new BaseBlockProperty(
      Properties.of().sound(SoundType.METAL).strength(5.0F, 6.0F).requiresCorrectToolForDrops().instrument(NoteBlockInstrument.IRON_XYLOPHONE),
      BaseBlockProperty.BlockMaterial.METAL
   );
   public static BaseBlockProperty COPPER = new BaseBlockProperty(
      Properties.of().sound(SoundType.METAL).strength(5.0F, 6.0F).requiresCorrectToolForDrops(), BaseBlockProperty.BlockMaterial.METAL
   );
   public static BaseBlockProperty BRONZE = new BaseBlockProperty(
      Properties.of().sound(SoundType.METAL).strength(5.5F, 6.5F).requiresCorrectToolForDrops(), BaseBlockProperty.BlockMaterial.METAL
   );
   public static BaseBlockProperty TILE = new BaseBlockProperty(
      Properties.of().sound(SoundType.BONE_BLOCK).strength(1.5F, 6.0F).requiresCorrectToolForDrops().instrument(NoteBlockInstrument.BASEDRUM),
      BaseBlockProperty.BlockMaterial.STONE
   );
   public static BaseBlockProperty PORCELAIN = new BaseBlockProperty(
      Properties.of().sound(SoundRegistry.PORCELAIN).strength(1.0F, 1.0F).instrument(NoteBlockInstrument.HAT),
      BaseBlockProperty.BlockMaterial.PORCELAIN
   );
   public static BaseBlockProperty WOOD = new BaseBlockProperty(
      Properties.of().sound(SoundType.WOOD).strength(2.0F, 3.0F).ignitedByLava().instrument(NoteBlockInstrument.BASS),
      BaseBlockProperty.BlockMaterial.WOOD
   );
   public static BaseBlockProperty BAMBOO_WOOD = new BaseBlockProperty(
      Properties.of().sound(SoundRegistry.BAMBOO_WOOD).strength(2.0F, 3.0F).ignitedByLava().instrument(NoteBlockInstrument.BASS),
      BaseBlockProperty.BlockMaterial.WOOD
   );
   public static BaseBlockProperty BAMBOO = new BaseBlockProperty(
      Properties.of().sound(SoundType.BAMBOO).ignitedByLava().instrument(NoteBlockInstrument.XYLOPHONE).strength(1.5F, 2.5F),
      BaseBlockProperty.BlockMaterial.BAMBOO
   );
   public static BaseBlockProperty GLAZED = new BaseBlockProperty(
      Properties.of().sound(SoundType.GLASS).strength(1.5F, 6.0F).requiresCorrectToolForDrops().instrument(NoteBlockInstrument.HAT),
      BaseBlockProperty.BlockMaterial.STONE
   );
   public static BaseBlockProperty JADE = new BaseBlockProperty(
      Properties.of().sound(SoundType.GLASS).strength(1.2F, 5.0F).requiresCorrectToolForDrops().instrument(NoteBlockInstrument.BELL),
      BaseBlockProperty.BlockMaterial.STONE
   );
   public static BaseBlockProperty FLAX = new BaseBlockProperty(
      Properties.of().sound(SoundRegistry.FLAX).strength(1.0F, 2.0F).ignitedByLava().instrument(NoteBlockInstrument.BANJO),
      BaseBlockProperty.BlockMaterial.FLAX
   );
   public static BaseBlockProperty CROP = new BaseBlockProperty(
      Properties.of().sound(SoundType.CROP).strength(1.0F, 1.5F).ignitedByLava().pushReaction(PushReaction.DESTROY),
      BaseBlockProperty.BlockMaterial.PLANT
   );
   public static BaseBlockProperty PLANT = new BaseBlockProperty(
      Properties.of().sound(SoundType.CROP).strength(1.0F, 1.5F).ignitedByLava().pushReaction(PushReaction.DESTROY),
      BaseBlockProperty.BlockMaterial.PLANT
   );
   public static BaseBlockProperty LILY = new BaseBlockProperty(
      Properties.of().sound(SoundType.LILY_PAD).strength(1.0F, 1.5F).instabreak(), BaseBlockProperty.BlockMaterial.PLANT
   );
   public static BaseBlockProperty SILK = new BaseBlockProperty(
      Properties.of().sound(SoundType.WOOL).strength(1.0F, 2.0F).ignitedByLava().instrument(NoteBlockInstrument.GUITAR),
      BaseBlockProperty.BlockMaterial.FABRIC
   );
   public static BaseBlockProperty PAPER = new BaseBlockProperty(
      Properties.of().sound(SoundType.WOOL).strength(1.0F, 2.0F).ignitedByLava(), BaseBlockProperty.BlockMaterial.PAPER
   );
   public static BaseBlockProperty DYE = new BaseBlockProperty(
      Properties.of().sound(SoundType.WOOL).strength(1.0F, 1.0F), BaseBlockProperty.BlockMaterial.PAPER
   );
   public static BaseBlockProperty ICE = new BaseBlockProperty(
      Properties.of().sound(SoundType.CROP).strength(0.5F, 1.0F), BaseBlockProperty.BlockMaterial.ICE
   );
   Properties properties;
   final BaseBlockProperty.BlockMaterial material;

   BaseBlockProperty(Properties properties, BaseBlockProperty.BlockMaterial material) {
      this.properties = properties;
      this.material = material;
   }

   public BaseBlockProperty.BlockMaterial getMaterial() {
      return this.material;
   }

   public BaseBlockProperty copy() {
      // 1.20.6 起 Properties.copy 接受 Block（不再接受 BlockBehaviour），且匿名方块需实现 codec()。
      BlockBehaviour dummy = new BlockBehaviour(this.properties) {
         @Override
         protected MapCodec<? extends Block> codec() {
            return BlockBehaviour.simpleCodec(props -> new Block(props));
         }

         @Override
         public Item asItem() {
            return Items.AIR;
         }

         @Override
         protected Block asBlock() {
            return Blocks.AIR;
         }
      };
      Properties properties1 = Properties.ofFullCopy(dummy);
      return new BaseBlockProperty(properties1, this.material);
   }

   public enum BlockMaterial {
      STONE(BlockTags.MINEABLE_WITH_PICKAXE),
      METAL(BlockTags.MINEABLE_WITH_PICKAXE),
      ICE(BlockTags.MINEABLE_WITH_PICKAXE),
      WOOD(BlockTags.MINEABLE_WITH_AXE),
      PORCELAIN(BlockTags.MINEABLE_WITH_PICKAXE),
      BAMBOO(BlockTags.MINEABLE_WITH_AXE),
      FABRIC(ModBlockTags.MINEABLE_WITH_SHEARS),
      PAPER(ModBlockTags.MINEABLE_WITH_SHEARS),
      PLANT(BlockTags.MINEABLE_WITH_HOE),
      FLAX(BlockTags.MINEABLE_WITH_HOE);

      final TagKey<Block> tool;

      public TagKey<Block> getTool() {
         return this.tool;
      }

      BlockMaterial(TagKey<Block> tool) {
         this.tool = tool;
      }
   }
}
