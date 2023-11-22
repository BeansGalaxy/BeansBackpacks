package com.beansgalaxy.galaxybackpacks.item;

import com.beansgalaxy.galaxybackpacks.entity.BackpackEntity;
import com.beansgalaxy.galaxybackpacks.entity.Kind;
import com.beansgalaxy.galaxybackpacks.entity.PlaySound;
import com.beansgalaxy.galaxybackpacks.networking.NetworkPackages;
import com.beansgalaxy.galaxybackpacks.screen.BackpackInventory;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.BundleTooltipData;
import net.minecraft.client.item.TooltipData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

import java.util.Optional;

public class BackpackItem extends Item implements DyeableItem {
    private final static int BACKPACK_DEFAULT_COLOR = 10511680;
    public static int SLOT_INDEX;
    private final Kind kind;

    public BackpackItem(Kind kind, Item.Settings settings) {
        super(settings.maxCount(1));
        this.kind = kind;
    }

    public Kind getKind() {
        return this.kind;
    }

    public static BackpackInventory getInventory(PlayerEntity player) {
        return (BackpackInventory) player.playerScreenHandler.slots.get(SLOT_INDEX + 1).inventory;
    }

    public static Slot getSlot(PlayerEntity player) {
        return player.playerScreenHandler.slots.get(SLOT_INDEX);
    }

    public int getColor(ItemStack stack) {
        NbtCompound nbtCompound = stack.getSubNbt(DISPLAY_KEY);
        if (nbtCompound != null && nbtCompound.contains(COLOR_KEY, NbtElement.NUMBER_TYPE)) {
            return nbtCompound.getInt(COLOR_KEY);
        }
        return BACKPACK_DEFAULT_COLOR;
    }

    // PLACES BACKPACK FROM HOTKEYS
    public static void hotkeyUseOnBlock(PlayerEntity player, World world, Direction direction, BlockPos blockPos) {
        ItemStack backpackStack = BackpackItem.getSlot(player).getStack();
        BackpackInventory backpackInventory = (BackpackInventory) player.playerScreenHandler.slots.get(SLOT_INDEX + 1).inventory;
        BackpackEntity entBackpack = new BackpackEntity(world, blockPos, direction, backpackInventory.getItemStacks());
        Kind kind = ((BackpackItem) backpackStack.getItem()).getKind();
        entBackpack.initDisplay(kind.getString(), backpackStack);
        if (!direction.getAxis().isHorizontal() && player != null)
            entBackpack.setYaw(rotFromBlock(blockPos, player) + 90);
        if (!world.isClient()) {
            PlaySound.PLACE.at(entBackpack);
            world.emitGameEvent(player, GameEvent.ENTITY_PLACE, entBackpack.position());
            world.spawnEntity(entBackpack);
        }
        backpackStack.decrement(1);
    }

    public ActionResult useOnBlock(ItemUsageContext ctx) {
        Direction direction = ctx.getSide();
        BlockPos blockPos = ctx.getBlockPos().offset(direction);
        PlayerEntity player = ctx.getPlayer();
        ItemStack backpackStack = ctx.getStack();
        World world = ctx.getWorld();
        BackpackEntity entBackpack = new BackpackEntity(world, blockPos, direction);
        entBackpack.initDisplay(kind.getString(), backpackStack);
        if (!direction.getAxis().isHorizontal() && player != null)
            entBackpack.setYaw(rotFromBlock(blockPos, player) + 90);
        if (!world.isClient()) {
            PlaySound.PLACE.at(entBackpack);
            world.emitGameEvent(player, GameEvent.ENTITY_PLACE, entBackpack.position());
            world.spawnEntity(entBackpack);
        }
        backpackStack.decrement(1);
        return ActionResult.success(world.isClient());
    }

    private static float rotFromBlock(BlockPos blockPos, PlayerEntity player) {
        Vec3d CPos = blockPos.toCenterPos();
        float YRot = (float) Math.toDegrees(Math.atan2
                (CPos.z - player.getZ(), CPos.x - player.getX()));
        if (YRot < -180) YRot += 360;
        else if (YRot > 180) YRot -= 360;
        return YRot;
    }

    private static int getItemOccupancy(ItemStack stack) {
        NbtCompound nbtCompound;
        if ((stack.isOf(Items.BEEHIVE) || stack.isOf(Items.BEE_NEST)) && stack.hasNbt() && (nbtCompound = BlockItem.getBlockEntityNbt(stack)) != null && !nbtCompound.getList("Bees", NbtElement.COMPOUND_TYPE).isEmpty()) {
            return 64;
        }
        return 64 / stack.getMaxCount();
    }

    private static int getBundleOccupancy(DefaultedList<ItemStack> defaultedList) {
        return defaultedList.stream().mapToInt(itemStack -> BackpackItem.getItemOccupancy(itemStack) * itemStack.getCount()).sum();
    }

    // GIVES AN EQUIPPED BACKPACK A CUSTOM TOOLTIP.
    public Optional<TooltipData> getTooltipData(ItemStack stack) {
        PlayerEntity player = MinecraftClient.getInstance().player;
        DefaultedList<Slot> slots = MinecraftClient.getInstance().player.playerScreenHandler.slots;
        // IS BACKPACK SLOT LOADED AND PLAYER IS LOOKING IN THE PLAYER INVENTORY
        if (slots.size() <= SLOT_INDEX || player.currentScreenHandler != player.playerScreenHandler)
            return super.getTooltipData(stack);
        ItemStack equippedOnBack = slots.get(SLOT_INDEX).getStack();
        // IS BACKPACK EQUIPPED
        if (!stack.equals(equippedOnBack))
            return super.getTooltipData(stack);
        DefaultedList<ItemStack> defaultedList = DefaultedList.of();
        DefaultedList<ItemStack> backpackList = BackpackItem.getInventory(player).getItemStacks();
        backpackList.forEach(itemstack -> defaultedList.add(itemstack.copy()));
        if (!defaultedList.isEmpty()) {
            defaultedList.remove(0);
            for (int j = 0; j < defaultedList.size(); j++) {
                ItemStack itemStack = defaultedList.get(j);
                int count = defaultedList.stream()
                        .filter(itemStacks -> itemStacks.isOf(itemStack.getItem()) && !itemStack.equals(itemStacks))
                        .mapToInt(itemStacks -> itemStacks.copyAndEmpty().getCount()).sum();
                itemStack.setCount(count + itemStack.getCount());
                defaultedList.removeIf(ItemStack::isEmpty);
            }

        }
        BackpackItem backpackItem = (BackpackItem) equippedOnBack.getItem();
        int totalWeight = BackpackItem.getBundleOccupancy(defaultedList) / Kind.getMaxStacks(backpackItem.getKind());
        return Optional.of(new BundleTooltipData(defaultedList, totalWeight));
    }

}
