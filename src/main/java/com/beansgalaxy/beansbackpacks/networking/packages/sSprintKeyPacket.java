package com.beansgalaxy.beansbackpacks.networking.packages;

import com.beansgalaxy.beansbackpacks.screen.BackSlot;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

public class sSprintKeyPacket {
      public static void receiveAtServer(MinecraftServer server, ServerPlayerEntity serverPlayer, ServerPlayNetworkHandler handler,
                                         PacketByteBuf buf, PacketSender responseSender) {
          BackSlot.get(serverPlayer).sprintKeyIsPressed = buf.readBoolean();
      }
}
