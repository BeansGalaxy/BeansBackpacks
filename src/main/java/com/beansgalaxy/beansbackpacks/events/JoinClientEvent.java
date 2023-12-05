package com.beansgalaxy.beansbackpacks.events;

import com.beansgalaxy.beansbackpacks.networking.NetworkPackages;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;

public class JoinClientEvent implements ClientEntityEvents.Load {

      @Override
      public void onLoad(Entity entity, ClientWorld world) {
            if (entity instanceof ClientPlayerEntity) {
                  PacketByteBuf buf = PacketByteBufs.create();
                  ClientPlayNetworking.send(NetworkPackages.CALL_INVENTORY_2S, buf);
            }

            if (entity instanceof OtherClientPlayerEntity otherClientPlayer) {
                  PacketByteBuf buf = PacketByteBufs.create();
                  buf.writeUuid(otherClientPlayer.getUuid());
                  ClientPlayNetworking.send(NetworkPackages.CALL_BACKSLOT_2S, buf);
            }

      }
}
