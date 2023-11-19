package com.beansgalaxy.galaxybackpacks.networking.packages;

import com.beansgalaxy.galaxybackpacks.entity.BackpackEntity;
import com.beansgalaxy.galaxybackpacks.item.BackpackItem;
import com.beansgalaxy.galaxybackpacks.networking.NetworkPackages;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.OpenHorseScreenS2CPacket;
import net.minecraft.screen.HorseScreenHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Direction;

import java.util.UUID;

public class OpenPlayerBackpackPacket {
    public static void receiveAtServer(MinecraftServer server, ServerPlayerEntity serverPlayer, ServerPlayNetworkHandler handler,
                                       PacketByteBuf buf, PacketSender responseSender) {
        UUID uuid = buf.readUuid();
        int id = buf.readInt();
        ServerPlayerEntity otherPlayer = server.getPlayerManager().getPlayer(uuid);

        ItemStack backpackStack = otherPlayer.playerScreenHandler.slots.get(BackpackItem.SLOT_INDEX).getStack();
        DefaultedList<ItemStack> itemStacks = BackpackItem.getInventory(otherPlayer).getItemStacks();

        BackpackEntity backpackEntity = new BackpackEntity(otherPlayer.getWorld(), otherPlayer.getBlockPos(), Direction.UP);
        backpackEntity.initDisplay(((BackpackItem) backpackStack.getItem()).getKind().getString(), backpackStack);
        backpackEntity.itemStacks = itemStacks;
        backpackEntity.setId(id);

        serverPlayer.openHandledScreen(backpackEntity);
    }

    public static void C2S(ClientPlayerEntity thisPlayer, OtherClientPlayerEntity otherPlayer) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeUuid(otherPlayer.getUuid());

        ItemStack backpackStack = otherPlayer.playerScreenHandler.slots.get(BackpackItem.SLOT_INDEX).getStack();
        DefaultedList<ItemStack> itemStacks = BackpackItem.getInventory(otherPlayer).getItemStacks();

        BackpackEntity backpackEntity = new BackpackEntity(thisPlayer.getWorld(), thisPlayer.getBlockPos(), Direction.UP);
        backpackEntity.initDisplay(((BackpackItem) backpackStack.getItem()).getKind().getString(), backpackStack);
        backpackEntity.itemStacks = itemStacks;


        thisPlayer.openHandledScreen(backpackEntity);

        int id = backpackEntity.getId();
        buf.writeInt(id);
        ClientPlayNetworking.send(NetworkPackages.OPEN_PLAYER_BACKPACK_2S, buf);
    }

}
