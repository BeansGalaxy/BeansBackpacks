package com.beansgalaxy.galaxybackpacks.networking.packages;

import com.beansgalaxy.galaxybackpacks.item.BackpackItem;
import com.beansgalaxy.galaxybackpacks.networking.NetworkPackages;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class PlacePacket {
    public static void receiveAtServer(MinecraftServer server, ServerPlayerEntity serverPlayer, ServerPlayNetworkHandler handler,
                                       PacketByteBuf buf, PacketSender responseSender) {
        Direction direction = Direction.byId(buf.readInt());
        int x = buf.readInt();
        int y = buf.readInt();
        int z = buf.readInt();
        BlockPos blockPos = new BlockPos(x, y, z);
        BackpackItem.hotkeyUseOnBlock(serverPlayer, serverPlayer.getWorld(), direction, blockPos);
        SyncBackSlot.S2C(serverPlayer);
    }

    public static void C2S(PlayerEntity player, World world, BlockHitResult hitResult) {
        Direction direction = hitResult.getSide();
        BlockPos blockPos = hitResult.getBlockPos().offset(direction);
        BackpackItem.hotkeyUseOnBlock(player, world, direction, blockPos);
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(direction.getId());
        buf.writeInt(blockPos.getX());
        buf.writeInt(blockPos.getY());
        buf.writeInt(blockPos.getZ());
        ClientPlayNetworking.send(NetworkPackages.PLACE_2S, buf);
    }
}
