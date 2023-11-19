package com.beansgalaxy.galaxybackpacks.networking.packages;

import com.beansgalaxy.galaxybackpacks.item.BackpackItem;
import com.beansgalaxy.galaxybackpacks.networking.NetworkPackages;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;
import java.util.UUID;

public class SyncBackSlot {
    public static void receiveAtClient(MinecraftClient minecraftClient, ClientPlayNetworkHandler clientPlayNetworkHandler, PacketByteBuf buf, PacketSender packetSender) {
        UUID uuid = buf.readUuid();
        ItemStack stack = buf.readItemStack();
        PlayerEntity player = minecraftClient.world.getPlayerByUuid(uuid);
        if (player instanceof OtherClientPlayerEntity otherPlayer) {
            otherPlayer.playerScreenHandler.slots.get(BackpackItem.SLOT_INDEX).setStack(stack);
        }
    }

    public static void S2C(ServerPlayerEntity player) {
        UUID uuid = player.getUuid();
        ItemStack stack = player.playerScreenHandler.slots.get(BackpackItem.SLOT_INDEX).getStack();
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeUuid(uuid);
        buf.writeItemStack(stack);
        List<ServerPlayerEntity> playerList = player.getServerWorld().getPlayers();
        for (ServerPlayerEntity nextPlayer : playerList) {
            if (player != nextPlayer)
                ServerPlayNetworking.send(nextPlayer, NetworkPackages.SYNC_BACKSLOT_2C, buf);
        }
    }
}
