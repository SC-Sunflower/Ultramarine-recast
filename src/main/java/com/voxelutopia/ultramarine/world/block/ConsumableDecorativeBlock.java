package com.voxelutopia.ultramarine.world.block;

import com.mojang.datafixers.util.Pair;
import com.voxelutopia.ultramarine.world.block.state.ModBlockStateProperties;
import com.voxelutopia.ultramarine.data.storage.ItemHandlerHelper;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

public class ConsumableDecorativeBlock extends DecorativeBlock {
   public static final IntegerProperty BITES = ModBlockStateProperties.BITES;
   private final int bites;
   private final ConsumableDecorativeBlock.ConsumeAction finishedAction;
   private final ConsumableDecorativeBlock.ConsumeAction eatAction;
   private final Supplier<ItemStack> plate;
   private final FoodProperties food;
   public static ConsumableDecorativeBlock.ConsumeAction DEFAULT_FINISH_ACTION = (pState, pLevel, pPos, pPlayer) -> {
      pLevel.setBlock(pPos, Blocks.AIR.defaultBlockState(), 3);
      ItemHandlerHelper.giveItemToPlayer(
         pPlayer, pState.getBlock() instanceof ConsumableDecorativeBlock consumable ? consumable.getPlate() : ItemStack.EMPTY
      );
      pLevel.gameEvent(pPlayer, GameEvent.BLOCK_DESTROY, pPos);
   };
   public static ConsumableDecorativeBlock.ConsumeAction DEFAULT_EAT_ACTION = (pState, pLevel, pPos, pPlayer) -> {
      if (pState.getBlock() instanceof ConsumableDecorativeBlock consumable) {
         FoodProperties food = consumable.getFood();
         pPlayer.getFoodData().eat(food.nutrition(), food.saturation());


         pLevel.playSound(pPlayer, pPos, SoundEvents.GENERIC_EAT.value(), SoundSource.PLAYERS, 1.0F, 0.75F);
         pLevel.gameEvent(pPlayer, GameEvent.EAT, pPos);
      }
   };

   public ConsumableDecorativeBlock(ConsumableDecorativeBlock.Builder builder) {
      super(builder);
      this.bites = builder.bites;
      this.finishedAction = builder.finishedAction;
      this.eatAction = builder.eatAction;
      this.plate = builder.plate;
      this.food = builder.food;
      this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(BITES, this.bites));
   }

   public static ConsumableDecorativeBlock.Builder with(BaseBlockProperty property) {
      return new ConsumableDecorativeBlock.Builder(property);
   }

   @NotNull
   @Override
   public BlockState getStateForPlacement(BlockPlaceContext pContext) {
      BlockState state = super.getStateForPlacement(pContext);
      return state.hasProperty(BITES) ? (BlockState)state.setValue(BITES, this.getMaxBites()) : state;
   }

   public InteractionResult useItemOn(ItemStack stack, BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
      int bitesRemaining = pState.hasProperty(BITES) ? (Integer)pState.getValue(BITES) : 0;
      if (!pPlayer.canEat(false)) {
         return InteractionResult.PASS;
      }

      if (bitesRemaining > 0) {
         this.eatAction.consume(pState, pLevel, pPos, pPlayer);
         bitesRemaining--;
      }

      if (bitesRemaining <= 0) {
         this.finishedAction.consume(pState, pLevel, pPos, pPlayer);
      } else {
         pLevel.setBlock(pPos, (BlockState)pState.setValue(BITES, bitesRemaining), 3);
      }

      return InteractionResult.SUCCESS;
   }

   public int getMaxBites() {
      return this.bites;
   }

   public ItemStack getPlate() {
      return this.plate.get();
   }

   public FoodProperties getFood() {
      return this.food;
   }

   @Override
   protected void createBlockStateDefinition(net.minecraft.world.level.block.state.StateDefinition.Builder<Block, BlockState> pBuilder) {
      super.createBlockStateDefinition(pBuilder);
      pBuilder.add(new Property[]{BITES});
   }

   public static class Builder extends DecorativeBlock.Builder {
      private static final FoodProperties DEFAULT_FOOD = new net.minecraft.world.food.FoodProperties.Builder()
         .nutrition(2)
         .saturationModifier(0.2F)
         .build();
      private int bites = 4;
      private Supplier<ItemStack> plate = () -> new ItemStack(Blocks.STONE_SLAB);
      private ConsumableDecorativeBlock.ConsumeAction eatAction = ConsumableDecorativeBlock.DEFAULT_EAT_ACTION;
      private ConsumableDecorativeBlock.ConsumeAction finishedAction = ConsumableDecorativeBlock.DEFAULT_FINISH_ACTION;
      private FoodProperties food = DEFAULT_FOOD;

      public Builder(BaseBlockProperty property) {
         super(property);
      }

      public ConsumableDecorativeBlock.Builder bites(int bites) {
         this.bites = bites;
         return this;
      }

      public ConsumableDecorativeBlock.Builder whenFinished(ConsumableDecorativeBlock.ConsumeAction action) {
         this.finishedAction = action;
         return this;
      }

      public ConsumableDecorativeBlock.Builder onEat(ConsumableDecorativeBlock.ConsumeAction action) {
         this.eatAction = action;
         return this;
      }

      public ConsumableDecorativeBlock.Builder platedWith(ItemStack plate) {
         this.plate = () -> plate;
         return this;
      }

      public ConsumableDecorativeBlock.Builder platedWith(Block plate) {
         this.plate = () -> new ItemStack(plate);
         return this;
      }

      public ConsumableDecorativeBlock.Builder platedWith(Supplier<Item> plate) {
         this.plate = () -> new ItemStack((ItemLike)plate.get());
         return this;
      }

      public ConsumableDecorativeBlock.Builder food(FoodProperties food) {
         this.food = food;
         return this;
      }

      public ConsumableDecorativeBlock build() {
         return new ConsumableDecorativeBlock(this);
      }
   }

   @FunctionalInterface
   public interface ConsumeAction {
      void consume(BlockState var1, Level var2, BlockPos var3, Player var4);
   }
}
