package com.beansgalaxy.beansbackpacks.events;

import com.beansgalaxy.beansbackpacks.entity.Kind;
import com.beansgalaxy.beansbackpacks.item.BackpackItem;
import com.beansgalaxy.beansbackpacks.screen.BackSlot;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class PlaceBackpackEvent implements UseBlockCallback
{
    private static DefaultedList<CoyoteClick> coyoteList = DefaultedList.of();

    @Override
    public ActionResult interact(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
        if (player.isSpectator())
            return ActionResult.PASS;

        if (BackSlot.get(player).sprintKeyIsPressed && Kind.isBackpack(BackSlot.get(player).getStack())) {
            Direction direction = hitResult.getSide();
            BlockPos blockPos = hitResult.getBlockPos().offset(direction);
            if (!player.isSprinting() && !player.isSwimming()) {
                return BackpackItem.hotkeyOnBlock(player, direction, blockPos);
            }
            else if (world instanceof ServerWorld serverWorld && player instanceof ServerPlayerEntity serverPlayer) {
                if (coyoteList.stream().noneMatch(clicks -> clicks.player.equals(player)))
                    coyoteList.add(new CoyoteClick(player, direction, blockPos));
            }
        }
        return ActionResult.PASS;
    }

    public static void cancelCoyoteClick(PlayerEntity player, ActionResult actionResult, boolean useItem) {
        if (coyoteList.isEmpty())
            return;

        coyoteList.removeIf(clicks -> {
            if (!clicks.player.equals(player))
                return false;

            if (useItem)
                clicks.successItem = actionResult;
            else
                clicks.successBlock = actionResult;

            if (clicks.hasNull())
                return false;

            if (clicks.success())
                return true;

            if (ActionResult.SUCCESS != BackpackItem.hotkeyOnBlock(player, clicks.direction, clicks.blockPos))
                return false;

            player.swingHand(Hand.MAIN_HAND, true);
            return true;
        });
    }

    static class CoyoteClick {
        private final PlayerEntity player;
        private final Direction direction;
        private final BlockPos blockPos;
        private ActionResult successItem = null;
        private ActionResult successBlock = null;
        public int time = 15;

        CoyoteClick(PlayerEntity player, Direction direction, BlockPos blockPos) {
            this.player = player;
            this.direction = direction;
            this.blockPos = blockPos;
            if (direction.getAxis().isHorizontal())
                time += 5;
        }

        boolean hasNull() {
            return successItem == null || successBlock == null;
        }

        boolean success() {
            return successItem.isAccepted() || successBlock.isAccepted();
        }
    }

    public void onEndTick(ServerWorld world) {
        if (coyoteList.isEmpty())
            return;

        coyoteList.removeIf(clicks -> {
            clicks.time -= 1;
            PlayerEntity player = clicks.player;

            if (clicks.time < 1 || player.currentScreenHandler != player.playerScreenHandler)
                return true;

            if (player.isSwimming() || player.isSprinting() || ActionResult.SUCCESS != BackpackItem.hotkeyOnBlock(player, clicks.direction, clicks.blockPos))
                return false;

            player.swingHand(Hand.MAIN_HAND, true);
            return true;
        });
    }
}
