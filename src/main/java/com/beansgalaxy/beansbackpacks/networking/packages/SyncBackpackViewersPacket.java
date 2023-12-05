package com.beansgalaxy.beansbackpacks.networking.packages;

import com.beansgalaxy.beansbackpacks.entity.BackpackEntity;
import com.beansgalaxy.beansbackpacks.networking.NetworkPackages;
import com.beansgalaxy.beansbackpacks.screen.BackSlot;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

import java.util.List;


public class SyncBackpackViewersPacket {
    public static void receiveAtClient(MinecraftClient client, ClientPlayNetworkHandler networkHandler, PacketByteBuf buf, PacketSender sender) {
        int id = buf.readInt();
        Entity entity = client.player.getWorld().getEntityById(id);
        byte viewers = buf.readByte();
        if (entity instanceof BackpackEntity backpackEntity)
            backpackEntity.viewers = viewers;
        else if (entity instanceof PlayerEntity player) {
            BackSlot.get(player).setViewers(viewers);
        }
    }

    public static void S2C(Entity entity, byte viewers) {
        World world = entity.getWorld();
        List<ServerPlayerEntity> playerList = world.getServer().getPlayerManager().getPlayerList();
        for (ServerPlayerEntity serverPlayer : playerList)
            if (serverPlayer.getWorld().getRegistryKey() == world.getRegistryKey()) {
                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeInt(entity.getId());
                buf.writeByte(viewers);

                ServerPlayNetworking.send(serverPlayer, NetworkPackages.SYNC_VIEWERS_2C, buf);
            }
    }
}
