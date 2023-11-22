package com.beansgalaxy.galaxybackpacks.networking.packages;

import com.beansgalaxy.galaxybackpacks.entity.BackpackEntity;
import com.beansgalaxy.galaxybackpacks.entity.Kind;
import com.beansgalaxy.galaxybackpacks.entity.PlaySound;
import com.beansgalaxy.galaxybackpacks.item.BackpackItem;
import com.beansgalaxy.galaxybackpacks.networking.NetworkPackages;
import com.beansgalaxy.galaxybackpacks.screen.BackpackInventory;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.spongepowered.asm.mixin.Unique;

import static net.minecraft.screen.PlayerScreenHandler.OFFHAND_ID;

public class SlotClickPacket {
    public static void receiveAtServer(MinecraftServer server, ServerPlayerEntity serverPlayer, ServerPlayNetworkHandler handler,
                                       PacketByteBuf buf, PacketSender responseSender) {
        int slotIndex = buf.readInt();
        int button = buf.readInt();
        SlotActionType actionType = SlotActionType.valueOf(buf.readString());
        boolean sprintKeyPress = buf.readBoolean();
        interceptSlotClick(slotIndex, button, actionType, serverPlayer, sprintKeyPress);
    }

    public static void C2S(int slotIndex, int button, SlotActionType actionType, boolean sprintKeyPress) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(slotIndex);
        buf.writeInt(button);
        buf.writeString(actionType.toString());
        buf.writeBoolean(sprintKeyPress);
        ClientPlayNetworking.send(NetworkPackages.SLOT_CLICK_2S, buf);
    }

    @Unique
    public static void interceptSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player, boolean sprintKeyPress) {
        ScreenHandler p = player.playerScreenHandler;
        BackpackInventory backpackInventory = BackpackItem.getInventory(player);
        ItemStack cursorStack = p.getCursorStack();
        if (sprintKeyPress) {
            Slot slot = p.slots.get(slotIndex);
            ItemStack stack = slot.getStack();
            if (slotIndex == BackpackItem.SLOT_INDEX && Kind.isBackpackItem(stack) && !backpackInventory.isEmpty()) {
                dropBackpack(player, stack, backpackInventory.getItemStacks());
                stack.setCount(0);
                return;
            }
            if (slotIndex < BackpackItem.SLOT_INDEX) {
                if (actionType == SlotActionType.PICKUP_ALL)
                    moveAll(backpackInventory, cursorStack, p);
                else slot.setStack(backpackInventory.insertStack(stack, stack.getCount()));
                return;
            } else
                actionType = SlotActionType.QUICK_MOVE;
        } else {
            if (slotIndex == BackpackItem.SLOT_INDEX + 1) {
                if (actionType == SlotActionType.PICKUP_ALL) return;
                ItemStack backpackSlotStack = p.slots.get(BackpackItem.SLOT_INDEX + 1).getStack();
                if (!cursorStack.isEmpty() || !backpackSlotStack.isEmpty())
                    player.playSound(SoundEvents.ITEM_BUNDLE_INSERT, 0.6F, 0.8f + player.getWorld().getRandom().nextFloat() * 0.4f);
                if (button == 1 && !cursorStack.isOf(backpackSlotStack.getItem()) && !cursorStack.isEmpty()) {
                    backpackInventory.insertStack(cursorStack, 1);
                    return;
                }
                if (button == 0) {
                    p.setPreviousCursorStack(cursorStack);
                    ItemStack stack = backpackInventory.returnStack(0, cursorStack);
                    p.setCursorStack(stack);
                    return;
                }
            }
        }
        button = -1000 - button;
        p.onSlotClick(slotIndex, button, actionType, player);
    }

    private static void moveAll(BackpackInventory backpackInventory, ItemStack cursorStack, ScreenHandler p) {
        int count = 0;
        for (int j = 9; j < OFFHAND_ID; j++) {
            Slot thisSlot = p.slots.get(j);
            ItemStack thisStack = thisSlot.getStack();
            if (thisStack.isOf(cursorStack.getItem())) {
                count += thisStack.getCount();
                thisSlot.setStack(ItemStack.EMPTY);
            }
        }
        while (count > 0) {
            if (count > 64) {
                backpackInventory.insertStack(cursorStack.copy(), 64);
                count -= 64;
            } else {
                backpackInventory.insertStack(cursorStack.copy(), count);
                count = 0;
            }
        }
    }

    public static void dropBackpack(PlayerEntity player, ItemStack backpackStack, DefaultedList<ItemStack> itemStacks) {
        World world = player.getWorld();
        BackpackEntity backpackEntity = new BackpackEntity(player.getWorld(), player.getBlockPos(), Direction.UP, itemStacks);
        backpackEntity.initDisplay(((BackpackItem) backpackStack.getItem()).getKind().getString(), backpackStack);
        backpackEntity.setYaw(player.getYaw());
        if (!world.isClient()) {
            PlaySound.PLACE.at(backpackEntity);
            world.emitGameEvent(player, GameEvent.ENTITY_PLACE, backpackEntity.position());
            world.spawnEntity(backpackEntity);
        }
    }
}
