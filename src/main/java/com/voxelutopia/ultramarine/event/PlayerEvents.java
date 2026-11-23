package com.voxelutopia.ultramarine.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

public class PlayerEvents {
   public static final Event<PlayerEvents.BreakSpeed> BREAK_SPEED = EventFactory.createArrayBacked(
      PlayerEvents.BreakSpeed.class, callbacks -> (player, state, pos, speed) -> {
         for (PlayerEvents.BreakSpeed callback : callbacks) {
            speed = callback.modifyBreakSpeed(player, state, pos, speed);
         }

         return speed;
      }
   );

   @FunctionalInterface
   public interface BreakSpeed {
      float modifyBreakSpeed(Player var1, BlockState var2, BlockPos var3, float var4);
   }
}
