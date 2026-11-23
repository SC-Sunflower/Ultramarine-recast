package com.voxelutopia.ultramarine.world.item;

import com.voxelutopia.ultramarine.data.registry.SoundRegistry;
import com.voxelutopia.ultramarine.world.block.RailingBlock;
import com.voxelutopia.ultramarine.world.block.state.ChiralBlockType;
import com.voxelutopia.ultramarine.world.block.state.ModBlockStateProperties;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class WoodenMallet extends Item {
   public WoodenMallet() {
      super(new Properties().durability(100));
   }

   @NotNull
   public InteractionResult useOn(UseOnContext pContext) {
      Level level = pContext.getLevel();
      ItemStack item = pContext.getItemInHand();
      Optional<Player> player = Optional.ofNullable(pContext.getPlayer());
      BlockPos blockpos = pContext.getClickedPos();
      BlockState blockstate = level.getBlockState(blockpos);
      boolean success = false;
      if (blockstate.hasProperty(ModBlockStateProperties.SHIFTED)) {
         BlockState pNewState = (BlockState)blockstate.setValue(
            ModBlockStateProperties.SHIFTED, !(Boolean)blockstate.getValue(ModBlockStateProperties.SHIFTED)
         );
         level.setBlock(blockpos, pNewState, 3);
         success = true;
      }

      if (blockstate.hasProperty(ModBlockStateProperties.CHIRAL_BLOCK_TYPE)) {
         level.setBlock(
            blockpos,
            (BlockState)blockstate.setValue(
               ModBlockStateProperties.CHIRAL_BLOCK_TYPE, ((ChiralBlockType)blockstate.getValue(ModBlockStateProperties.CHIRAL_BLOCK_TYPE)).getOpposite()
            ),
            3
         );
         success = true;
      }

      if (blockstate.hasProperty(ModBlockStateProperties.LOCKED) && player.isPresent() && player.get().isCrouching()) {
         blockstate = (BlockState)blockstate.setValue(ModBlockStateProperties.LOCKED, !(Boolean)blockstate.getValue(ModBlockStateProperties.LOCKED));
         if (blockstate.getBlock() instanceof RailingBlock railingBlock) {
            blockstate = railingBlock.updatePole(blockstate);
         }

         level.setBlock(blockpos, blockstate, 3);
         success = true;
      }

      if (success) {
         player.ifPresent(player1 -> {
            item.hurtAndBreak(1, player1, EquipmentSlot.MAINHAND);
            player1.awardStat(Stats.ITEM_USED.get(item.getItem()));
            level.playSound(player.get(), blockpos, SoundRegistry.WOOD_HAMMER, SoundSource.BLOCKS, 1.0F, 0.75F);
         });
         return InteractionResult.SUCCESS;
      } else {
         return super.useOn(pContext);
      }
   }
}
