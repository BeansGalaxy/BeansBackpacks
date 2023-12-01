package com.beansgalaxy.galaxybackpacks.events;

import com.beansgalaxy.galaxybackpacks.screen.BackSlot;
import net.fabricmc.fabric.api.entity.event.v1.EntityElytraEvents;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.world.event.GameEvent;

public class EnableElytraEvent implements EntityElytraEvents.Custom {

    @Override
    public boolean useCustomElytra(LivingEntity entity, boolean tickElytra) {
        if (entity instanceof PlayerEntity player) {
            ItemStack backStack = BackSlot.get(player).getStack();
            if (!backStack.isOf(Items.ELYTRA) || !ElytraItem.isUsable(backStack))
                return false;
            if (tickElytra) {
                int i = player.getRoll() + 1;
                if (!player.getWorld().isClient && i % 10 == 0) {
                    int j = i / 10;
                    if (j % 2 == 0) {
                        backStack.damage(1, player, thisPlayer -> thisPlayer.sendEquipmentBreakStatus(EquipmentSlot.CHEST));
                    }
                    player.emitGameEvent(GameEvent.ELYTRA_GLIDE);
                }
            }
            return true;
        }
        return false;
    }
}
