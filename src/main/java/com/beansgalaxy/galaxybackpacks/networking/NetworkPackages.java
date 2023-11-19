package com.beansgalaxy.galaxybackpacks.networking;

import com.beansgalaxy.galaxybackpacks.Main;
import com.beansgalaxy.galaxybackpacks.networking.packages.*;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.util.Identifier;

public class NetworkPackages {
    public static final Identifier BACKPACK_INVENTORY_2C = new Identifier(Main.MODID, "backpack_inventory_c");
    public static final Identifier SYNC_BACKSLOT_2C = new Identifier(Main.MODID, "sync_backslot_c");
    public static final Identifier INTERACT_2C = new Identifier(Main.MODID, "interact_c");
    public static final Identifier INTERACT_2S = new Identifier(Main.MODID, "interact_s");
    public static final Identifier OPEN_PLAYER_BACKPACK_2S = new Identifier(Main.MODID, "open_player_backpack_s");
    public static final Identifier PLACE_2S = new Identifier(Main.MODID, "place_s");
    public static final Identifier SLOT_CLICK_2S = new Identifier(Main.MODID, "slot_click_s");


    public static void registerC2SPackets() {
        ServerPlayNetworking.registerGlobalReceiver(INTERACT_2S, InteractPacket::receiveAtServer);
        ServerPlayNetworking.registerGlobalReceiver(PLACE_2S, PlacePacket::receiveAtServer);
        ServerPlayNetworking.registerGlobalReceiver(SLOT_CLICK_2S, SlotClickPacket::receiveAtServer);
        ServerPlayNetworking.registerGlobalReceiver(OPEN_PLAYER_BACKPACK_2S, OpenPlayerBackpackPacket::receiveAtServer);
    }

    public static void registerS2CPackets() {
        ClientPlayNetworking.registerGlobalReceiver(INTERACT_2C, InteractPacket::receiveAtClient);
        ClientPlayNetworking.registerGlobalReceiver(BACKPACK_INVENTORY_2C, syncBackpackInventory::receiveAtClient);
        ClientPlayNetworking.registerGlobalReceiver(SYNC_BACKSLOT_2C, SyncBackSlot::receiveAtClient);
    }

    public static NbtCompound stringToNbt(String string) {
        try {
            NbtCompound nbt = NbtHelper.fromNbtProviderString(string);
            return nbt;
        } catch (CommandSyntaxException e) {
            throw new RuntimeException(Main.MODID + ": Failed to sync BackpackInventory with client");
        }
    }

}
