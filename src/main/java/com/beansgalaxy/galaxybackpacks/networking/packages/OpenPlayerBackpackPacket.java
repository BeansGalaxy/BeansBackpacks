package com.beansgalaxy.galaxybackpacks.networking.packages;

import com.beansgalaxy.galaxybackpacks.entity.BackpackEntity;
import com.beansgalaxy.galaxybackpacks.entity.PlaySound;
import com.beansgalaxy.galaxybackpacks.item.BackpackItem;
import com.beansgalaxy.galaxybackpacks.networking.NetworkPackages;
import com.beansgalaxy.galaxybackpacks.screen.BackSlot;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Direction;

import java.util.UUID;

public class OpenPlayerBackpackPacket {
    public static void receiveAtServer(MinecraftServer server, ServerPlayerEntity serverPlayer, ServerPlayNetworkHandler handler,
                                       PacketByteBuf buf, PacketSender responseSender) {
        UUID uuid = buf.readUuid();
        ServerPlayerEntity owner = server.getPlayerManager().getPlayer(uuid);

        if (owner == null)
            return;

        BackSlot backSlot = BackSlot.get(owner);
        backSlot.addViewer(serverPlayer);
    }

    public static void C2S(OtherClientPlayerEntity owner, ClientPlayerEntity viewer) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeUuid(owner.getUuid());

        ClientPlayNetworking.send(NetworkPackages.OPEN_PLAYER_BACKPACK_2S, buf);
    }



}
