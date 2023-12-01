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
    public static final Identifier SYNC_VIEWERS_2C = new Identifier(Main.MODID, "interact_c");
    public static final Identifier OPEN_PLAYER_BACKPACK_2S = new Identifier(Main.MODID, "open_player_backpack_s");
    public static final Identifier SPRINT_KEY_2S = new Identifier(Main.MODID, "sprint_key_s");
    public static final Identifier CALL_INVENTORY_2S = new Identifier(Main.MODID, "backpack_inventory_s");
    public static final Identifier CALL_BACKSLOT_2S = new Identifier(Main.MODID, "backpack_backslot_s");


    public static void registerC2SPackets() {
        ServerPlayNetworking.registerGlobalReceiver(OPEN_PLAYER_BACKPACK_2S, OpenPlayerBackpackPacket::receiveAtServer);
        ServerPlayNetworking.registerGlobalReceiver(SPRINT_KEY_2S, SprintKeyPacket::receiveAtServer);
        ServerPlayNetworking.registerGlobalReceiver(CALL_INVENTORY_2S, SyncBackpackInventory::callSyncBackpackInventory);
        ServerPlayNetworking.registerGlobalReceiver(CALL_BACKSLOT_2S, SyncBackSlot::callSyncBackSlot);
    }

    public static void registerS2CPackets() {
        ClientPlayNetworking.registerGlobalReceiver(SYNC_VIEWERS_2C, SyncBackpackViewersPacket::receiveAtClient);
        ClientPlayNetworking.registerGlobalReceiver(BACKPACK_INVENTORY_2C, SyncBackpackInventory::receiveAtClient);
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
