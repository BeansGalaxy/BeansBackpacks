package com.beansgalaxy.galaxybackpacks.events;

import com.beansgalaxy.galaxybackpacks.entity.Kind;
import com.beansgalaxy.galaxybackpacks.item.BackpackItem;
import com.beansgalaxy.galaxybackpacks.screen.BackSlot;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class UseBlockEvent implements UseBlockCallback {

    @Override
    public ActionResult interact(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
        if (player.isSpectator())
            return ActionResult.PASS;
        if (BackSlot.get(player).sprintKeyIsPressed) {
            ItemStack backpackStack = BackSlot.get(player).getStack();
            if (Kind.isBackpack(backpackStack)) {
                Direction direction = hitResult.getSide();
                BlockPos blockPos = hitResult.getBlockPos().offset(direction);
                return BackpackItem.hotkeyOnBlock(player, direction, blockPos);
            }
        }
        return ActionResult.PASS;
    }
}
