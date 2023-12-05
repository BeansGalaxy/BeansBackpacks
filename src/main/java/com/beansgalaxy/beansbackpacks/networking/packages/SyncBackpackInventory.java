package com.beansgalaxy.beansbackpacks.networking.packages;

import com.beansgalaxy.beansbackpacks.networking.NetworkPackages;
import com.beansgalaxy.beansbackpacks.screen.BackSlot;
import com.beansgalaxy.beansbackpacks.screen.BackpackInventory;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

public class SyncBackpackInventory {
    // SYNC SERVER PLAYER'S BACKPACK INVENTORY WITH CLIENT
    public static void receiveAtClient(MinecraftClient client, ClientPlayNetworkHandler networkHandler, PacketByteBuf buf, PacketSender sender) {
        BackpackInventory backpackInventory = BackSlot.getInventory(client.player);
        backpackInventory.getItemStacks().clear();
        String string = buf.readString();
        NbtCompound nbt = NetworkPackages.stringToNbt(string);
        backpackInventory.readStackNbt(nbt);
    }

    // SYNCS THE SERVER PLAYER'S BACKPACK INVENTORY TO THE CLIENT PLAYER
    public static void S2C(ServerPlayerEntity player) {
        BackpackInventory backpackInventory = BackSlot.getInventory(player);
        PacketByteBuf buf = PacketByteBufs.create();
        NbtCompound compound = new NbtCompound();
        backpackInventory.writeNbt(compound, backpackInventory.isEmpty());
        String string = compound.asString();
        buf.writeString(string);
        ServerPlayNetworking.send(player, NetworkPackages.BACKPACK_INVENTORY_2C, buf);
    }

    public static void callSyncBackpackInventory(MinecraftServer server, ServerPlayerEntity serverPlayer, ServerPlayNetworkHandler handler,
                                                 PacketByteBuf buf, PacketSender responseSender) {
        S2C(serverPlayer);
    }
}
