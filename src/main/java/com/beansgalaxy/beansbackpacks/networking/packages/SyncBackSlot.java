package com.beansgalaxy.beansbackpacks.networking.packages;

import com.beansgalaxy.beansbackpacks.networking.NetworkPackages;
import com.beansgalaxy.beansbackpacks.screen.BackSlot;
import com.beansgalaxy.beansbackpacks.screen.BackpackScreenHandler;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;
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

    public static void S2C(ServerPlayerEntity player) {
        UUID uuid = player.getUuid();
        ItemStack stack = player.playerScreenHandler.slots.get(BackSlot.SLOT_INDEX).getStack();

        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeUuid(uuid);
        buf.writeItemStack(stack);

        List<ServerPlayerEntity> playerList = player.getServerWorld().getPlayers();
        for (ServerPlayerEntity nextPlayer : playerList) {
            if (player != nextPlayer) {
                if (nextPlayer.currentScreenHandler instanceof BackpackScreenHandler backpackScreenHandler && backpackScreenHandler.entity.itemStacks == BackSlot.getInventory(player).getItemStacks())
                    nextPlayer.closeHandledScreen();
                ServerPlayNetworking.send(nextPlayer, NetworkPackages.SYNC_BACKSLOT_2C, buf);
            }
        }
    }

    public static void callSyncBackSlot(MinecraftServer server, ServerPlayerEntity serverPlayer, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        UUID uuid = buf.readUuid();
        PlayerEntity otherPlayer = serverPlayer.getWorld().getPlayerByUuid(uuid);
        ItemStack backStack = BackSlot.get(otherPlayer).getStack();

        PacketByteBuf bufSlot = PacketByteBufs.create();
        bufSlot.writeUuid(uuid);
        bufSlot.writeItemStack(backStack);

        ServerPlayNetworking.send(serverPlayer, NetworkPackages.SYNC_BACKSLOT_2C, bufSlot);

    }
}
