package com.beansgalaxy.beansbackpacks.networking.server;

import com.beansgalaxy.beansbackpacks.networking.NetworkPackages;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

import java.util.List;

public class sSyncViewers {
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
