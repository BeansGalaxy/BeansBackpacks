package com.beansgalaxy.beansbackpacks.client.networking;

import com.beansgalaxy.beansbackpacks.networking.NetworkPackages;
import com.beansgalaxy.beansbackpacks.screen.BackSlot;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.PacketByteBuf;

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

}
