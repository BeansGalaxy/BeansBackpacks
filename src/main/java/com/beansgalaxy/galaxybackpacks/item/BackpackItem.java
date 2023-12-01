package com.beansgalaxy.galaxybackpacks.item;

import com.beansgalaxy.galaxybackpacks.entity.BackpackEntity;
import com.beansgalaxy.galaxybackpacks.entity.Kind;
import com.beansgalaxy.galaxybackpacks.entity.PlaySound;
import com.beansgalaxy.galaxybackpacks.networking.packages.SyncBackSlot;
import com.beansgalaxy.galaxybackpacks.screen.BackSlot;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.BundleTooltipData;
import net.minecraft.client.item.TooltipData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

import java.util.Optional;

public class BackpackItem extends Item implements DyeableItem {
    private final static int BACKPACK_DEFAULT_COLOR = 10511680;
    private final Kind kind;

    public BackpackItem(Kind kind, Item.Settings settings) {
        super(settings.maxCount(1));
        this.kind = kind;
    }

    public Kind getKind() {
        return this.kind;
    }

    public int getColor(ItemStack stack) {
        NbtCompound nbtCompound = stack.getSubNbt(DISPLAY_KEY);
        if (nbtCompound != null && nbtCompound.contains(COLOR_KEY, NbtElement.NUMBER_TYPE)) {
            return nbtCompound.getInt(COLOR_KEY);
        }
        return BACKPACK_DEFAULT_COLOR;
    }

    /** PLACE BACKPACK FROM ITEMS */
    public static ActionResult hotkeyOnBlock(PlayerEntity player, Direction direction, BlockPos blockPos) {
        ItemStack backpackStack = BackSlot.get(player).getStack();

        int x = blockPos.getX();
        double y = blockPos.getY() + 2d / 16;
        int z = blockPos.getZ();

        Box box = BackpackEntity.newBox(blockPos, y, 10 / 16d, direction);
        if (player.getWorld().isSpaceEmpty(box) && BackpackItem.doesPlace(player, x, y, z, direction, backpackStack, true)) {
            if (player instanceof ServerPlayerEntity serverPlayer)
                SyncBackSlot.S2C(serverPlayer);
            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }

    public static ActionResult useOnBackpack(PlayerEntity player, BackpackEntity backpackEntity, ItemStack backpackStack, boolean copyBackpackInventory) {

        Vec3d pos = backpackEntity.position();
        Direction direction = backpackEntity.direction;

        int invert = player.isSneaking() ? -1 : 1;
        int x = MathHelper.floor(pos.x);
        double y = pos.y + 11d / 16 * invert;
        int z = MathHelper.floor(pos.z);

        Box box = backpackEntity.getBoundingBox().offset(0, 10d / 16 * invert, 0);
        boolean spaceEmpty = player.getWorld().isSpaceEmpty(box);
        if (spaceEmpty && doesPlace(player, x, y, z, direction, backpackStack, copyBackpackInventory)) {
            if (player instanceof ServerPlayerEntity serverPlayer)
                SyncBackSlot.S2C(serverPlayer);
            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }

    public ActionResult useOnBlock(ItemUsageContext ctx) {
        PlayerEntity player = ctx.getPlayer();
        Direction direction = ctx.getSide();
        BlockPos blockPos = ctx.getBlockPos().offset(direction);
        ItemStack backpackStack = ctx.getStack();

        int x = blockPos.getX();
        double y = blockPos.getY() + 2d / 16;
        int z = blockPos.getZ();

        Box box = BackpackEntity.newBox(blockPos, y, 10 / 16d, direction);
        boolean spaceEmpty = player.getWorld().isSpaceEmpty(box);
        if (spaceEmpty && doesPlace(player, x, y, z, direction, backpackStack, false)) {
            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }

    public static boolean doesPlace(PlayerEntity player, int x, double y, int z, Direction direction, ItemStack backpackStack, boolean copyBackpackInventory) {
        World world = player.getWorld();
        BlockPos blockPos = BlockPos.ofFloored(x, y, z);

        DefaultedList<ItemStack> stacks = copyBackpackInventory ?
                    BackSlot.getInventory(player).getItemStacks() :
                    DefaultedList.of();

        BackpackEntity entBackpack = new BackpackEntity(world, x, y, z, direction, stacks);
        entBackpack.initDisplay(backpackStack);
        if (!direction.getAxis().isHorizontal() && player != null)
            entBackpack.setYaw(rotFromBlock(blockPos, player) + 90);
        PlaySound.PLACE.at(entBackpack);
        if (!world.isClient()) {
            world.emitGameEvent(player, GameEvent.ENTITY_PLACE, entBackpack.position());
            world.spawnEntity(entBackpack);
        }
        backpackStack.decrement(1);

        return true;
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
        if (slots.size() <= BackSlot.SLOT_INDEX || player.currentScreenHandler != player.playerScreenHandler)
            return super.getTooltipData(stack);
        ItemStack equippedOnBack = slots.get(BackSlot.SLOT_INDEX).getStack();
        // IS BACKPACK EQUIPPED
        if (!stack.equals(equippedOnBack))
            return super.getTooltipData(stack);
        DefaultedList<ItemStack> defaultedList = DefaultedList.of();
        DefaultedList<ItemStack> backpackList = BackSlot.getInventory(player).getItemStacks();
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
        int totalWeight = BackpackItem.getBundleOccupancy(defaultedList) / Kind.getMaxStacks(equippedOnBack);
        return Optional.of(new BundleTooltipData(defaultedList, totalWeight));
    }

}
