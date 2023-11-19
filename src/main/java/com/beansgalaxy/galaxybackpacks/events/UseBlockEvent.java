package com.beansgalaxy.galaxybackpacks.events;

import com.beansgalaxy.galaxybackpacks.entity.Kind;
import com.beansgalaxy.galaxybackpacks.item.BackpackItem;
import com.beansgalaxy.galaxybackpacks.networking.packages.PlacePacket;
import com.beansgalaxy.galaxybackpacks.register.ItemRegistry;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.client.MinecraftClient;
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
        if (MinecraftClient.getInstance().options.sprintKey.isPressed()) {
            ItemStack backpackStack = BackpackItem.getSlot(player).getStack();
            if (Kind.isBackpackItem(backpackStack)) {
                PlacePacket.C2S(player, world, hitResult);
                return ActionResult.SUCCESS;
            }
        }
        return ActionResult.PASS;
    }
}
