package com.beansgalaxy.galaxybackpacks.networking.packages;

import com.beansgalaxy.galaxybackpacks.networking.NetworkPackages;
import com.beansgalaxy.galaxybackpacks.screen.BackSlot;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

public class SprintKeyPacket {
    public static void updateSprintKeyIsPressed(ClientPlayerEntity clientPlayer) {
        boolean sprintKeyPressed = sprintBoundKeyPressed();
        boolean sprintKeyPrevious = BackSlot.get(clientPlayer).sprintKeyIsPressed;
        if (sprintKeyPressed != sprintKeyPrevious) {
            BackSlot.get(clientPlayer).sprintKeyIsPressed = sprintKeyPressed;
            C2S(sprintKeyPressed);
        }
    }

    private static boolean sprintBoundKeyPressed() {
        long clientWindowHandle = MinecraftClient.getInstance().getWindow().getHandle();
        int sprintKeyCode = MinecraftClient.getInstance().options.sprintKey.getDefaultKey().getCode();
        return InputUtil.isKeyPressed(clientWindowHandle, sprintKeyCode);
    }

    private static void C2S(boolean sprintKeyIsPressed) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBoolean(sprintKeyIsPressed);
        ClientPlayNetworking.send(NetworkPackages.SPRINT_KEY_2S, buf);
    }

    public static void receiveAtServer(MinecraftServer server, ServerPlayerEntity serverPlayer, ServerPlayNetworkHandler handler,
                                       PacketByteBuf buf, PacketSender responseSender) {
        BackSlot.get(serverPlayer).sprintKeyIsPressed = buf.readBoolean();
    }
}
