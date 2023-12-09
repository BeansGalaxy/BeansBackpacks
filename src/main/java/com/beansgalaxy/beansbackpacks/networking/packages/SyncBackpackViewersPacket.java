package com.beansgalaxy.beansbackpacks.networking.packages;

import com.beansgalaxy.beansbackpacks.entity.BackpackEntity;
import com.beansgalaxy.beansbackpacks.screen.BackSlot;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;


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

}
