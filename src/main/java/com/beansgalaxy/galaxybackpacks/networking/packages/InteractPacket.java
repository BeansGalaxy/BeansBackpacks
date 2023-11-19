package com.beansgalaxy.galaxybackpacks.networking.packages;

import com.beansgalaxy.galaxybackpacks.entity.BackpackEntity;
import com.beansgalaxy.galaxybackpacks.networking.NetworkPackages;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;


public class InteractPacket {
    /** UPDATES SERVER PLAYER'S BACKPACK INVENTORIES ACCORDING TO CLIENT **/
    public static void receiveAtServer(MinecraftServer server, ServerPlayerEntity serverPlayer, ServerPlayNetworkHandler handler,
                                       PacketByteBuf buf, PacketSender responseSender) {
        // Everything here happens ONLY on the Server!
        int id = buf.readInt();
        Entity entity = serverPlayer.getWorld().getEntityById(id);
        if (entity instanceof BackpackEntity backpackEntity) {
            boolean sprintKeyPressed = buf.readBoolean();
            BackpackEntity.attemptEquip(serverPlayer, backpackEntity, sprintKeyPressed);
        }
    }

    /** SENDS CLIENT PLAYER'S BACKPACK INVENTORIES TO SERVER **/
    public static void C2S(AbstractClientPlayerEntity player, BackpackEntity backpackEntity, boolean sprintKeyPressed) {
        PacketByteBuf buf = PacketByteBufs.create();
        int id = backpackEntity.getId();
        buf.writeInt(id);
        buf.writeBoolean(sprintKeyPressed);
        ClientPlayNetworking.send(NetworkPackages.INTERACT_2S, buf);
    }

    public static void receiveAtClient(MinecraftClient client, ClientPlayNetworkHandler networkHandler, PacketByteBuf buf, PacketSender sender) {
        int id = buf.readInt();
        Entity entity = client.player.getWorld().getEntityById(id);
        int viewers = buf.readInt();
        if (entity instanceof BackpackEntity backpackEntity)
            backpackEntity.viewers = viewers;
    }

    public static void S2C(ServerPlayerEntity player, BackpackEntity backpackEntity, int viewers) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(backpackEntity.getId());
        buf.writeInt(viewers);
        ServerPlayNetworking.send(player, NetworkPackages.INTERACT_2C, buf);
    }
}
