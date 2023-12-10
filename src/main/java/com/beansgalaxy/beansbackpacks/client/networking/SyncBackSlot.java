package com.beansgalaxy.beansbackpacks.client.networking;

import com.beansgalaxy.beansbackpacks.screen.BackSlot;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;

import java.util.UUID;

public class SyncBackSlot {
    public static void receiveAtClient(MinecraftClient minecraftClient, ClientPlayNetworkHandler clientPlayNetworkHandler, PacketByteBuf buf, PacketSender packetSender) {
        if (minecraftClient.world == null)
            return;

        UUID uuid = buf.readUuid();
        ItemStack stack = buf.readItemStack();
        PlayerEntity player = clientPlayNetworkHandler.getWorld().getPlayerByUuid(uuid) ;

        if (player instanceof OtherClientPlayerEntity otherPlayer) {
            otherPlayer.playerScreenHandler.slots.get(BackSlot.SLOT_INDEX).setStack(stack);
        }
    }
}
