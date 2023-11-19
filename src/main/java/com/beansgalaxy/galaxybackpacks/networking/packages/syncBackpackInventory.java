package com.beansgalaxy.galaxybackpacks.networking.packages;

import com.beansgalaxy.galaxybackpacks.Main;
import com.beansgalaxy.galaxybackpacks.item.BackpackItem;
import com.beansgalaxy.galaxybackpacks.networking.NetworkPackages;
import com.beansgalaxy.galaxybackpacks.screen.BackpackInventory;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.collection.DefaultedList;

public class syncBackpackInventory {
    // SYNC SERVER PLAYER'S BACKPACK INVENTORY WITH CLIENT
    public static void receiveAtClient(MinecraftClient client, ClientPlayNetworkHandler networkHandler, PacketByteBuf buf, PacketSender sender) {
        BackpackInventory backpackInventory = BackpackItem.getInventory(client.player);
        backpackInventory.getItemStacks().clear();
        String string = buf.readString();
        NbtCompound nbt = NetworkPackages.stringToNbt(string);
        backpackInventory.readStackNbt(nbt);
    }

    // SYNCS THE SERVER PLAYER'S BACKPACK INVENTORY TO THE CLIENT PLAYER
    public static void S2C(ServerPlayerEntity player) {
        DefaultedList<ItemStack> stack = BackpackItem.getInventory(player).getItemStacks();
        PacketByteBuf buf = PacketByteBufs.create();
        NbtCompound compound = new NbtCompound();
        Inventories.writeNbt(compound, stack);
        String string = compound.asString();
        buf.writeString(string);
        ServerPlayNetworking.send(player, NetworkPackages.BACKPACK_INVENTORY_2C, buf);
    }

}
