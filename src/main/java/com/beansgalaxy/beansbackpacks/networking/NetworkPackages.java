package com.beansgalaxy.beansbackpacks.networking;

import com.beansgalaxy.beansbackpacks.BeansBackpacks;
import com.beansgalaxy.beansbackpacks.client.networking.SyncBackSlot;
import com.beansgalaxy.beansbackpacks.client.networking.SyncBackpackInventory;
import com.beansgalaxy.beansbackpacks.client.networking.SyncBackpackViewersPacket;
import com.beansgalaxy.beansbackpacks.networking.packages.sSyncBackSlot;
import com.beansgalaxy.beansbackpacks.networking.packages.sSyncBackpackInventory;
import com.beansgalaxy.beansbackpacks.networking.packages.sSprintKeyPacket;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.util.Identifier;

public class NetworkPackages {
    public static final Identifier CALL_INVENTORY_2S = new Identifier(BeansBackpacks.MODID, "backpack_inventory_s");
    public static final Identifier BACKPACK_INVENTORY_2C = new Identifier(BeansBackpacks.MODID, "backpack_inventory_c");
    public static final Identifier CALL_BACKSLOT_2S = new Identifier(BeansBackpacks.MODID, "backpack_backslot_s");
    public static final Identifier SYNC_BACKSLOT_2C = new Identifier(BeansBackpacks.MODID, "sync_backslot_c");

    public static final Identifier OPEN_PLAYER_BACKPACK_2S = new Identifier(BeansBackpacks.MODID, "open_player_backpack_s");
    public static final Identifier SPRINT_KEY_2S = new Identifier(BeansBackpacks.MODID, "sprint_key_s");
    public static final Identifier SYNC_VIEWERS_2C = new Identifier(BeansBackpacks.MODID, "interact_c");

    public static void registerC2SPackets() {
        BeansBackpacks.LOGGER.info("Registering C2S Packets for" + BeansBackpacks.MODID);
        ServerPlayNetworking.registerGlobalReceiver(CALL_INVENTORY_2S, sSyncBackpackInventory::callSyncBackpackInventory);
        ServerPlayNetworking.registerGlobalReceiver(CALL_BACKSLOT_2S, sSyncBackSlot::callSyncBackSlot);

        ServerPlayNetworking.registerGlobalReceiver(SPRINT_KEY_2S, sSprintKeyPacket::receiveAtServer);
    }

    public static void registerS2CPackets() {
        BeansBackpacks.LOGGER.info("Registering S2C Packets for" + BeansBackpacks.MODID);
        ClientPlayNetworking.registerGlobalReceiver(BACKPACK_INVENTORY_2C, SyncBackpackInventory::receiveAtClient);
        ClientPlayNetworking.registerGlobalReceiver(SYNC_BACKSLOT_2C, SyncBackSlot::receiveAtClient);

        ClientPlayNetworking.registerGlobalReceiver(SYNC_VIEWERS_2C, SyncBackpackViewersPacket::receiveAtClient);
    }

    public static NbtCompound stringToNbt(String string) {
        try {
            NbtCompound nbt = NbtHelper.fromNbtProviderString(string);
            return nbt;
        } catch (CommandSyntaxException e) {
            throw new RuntimeException(BeansBackpacks.MODID + ": Failed to sync BackpackInventory with networking");
        }
    }

}
