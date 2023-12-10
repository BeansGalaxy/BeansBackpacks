package com.beansgalaxy.beansbackpacks.client.networking;

import com.beansgalaxy.beansbackpacks.networking.NetworkPackages;
import com.beansgalaxy.beansbackpacks.screen.BackSlot;
import com.beansgalaxy.beansbackpacks.screen.BackpackInventory;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;

public class SyncBackpackInventory {
    // SYNC SERVER PLAYER'S BACKPACK INVENTORY WITH CLIENT
    public static void receiveAtClient(MinecraftClient client, ClientPlayNetworkHandler networkHandler, PacketByteBuf buf, PacketSender sender) {
        BackpackInventory backpackInventory = BackSlot.getInventory(client.player);
        backpackInventory.getItemStacks().clear();
        String string = buf.readString();
        NbtCompound nbt = NetworkPackages.stringToNbt(string);
        backpackInventory.readStackNbt(nbt);
    }

}
