package com.beansgalaxy.galaxybackpacks.entity;

import com.beansgalaxy.galaxybackpacks.item.BackpackItem;
import com.beansgalaxy.galaxybackpacks.networking.packages.OpenPlayerBackpackPacket;
import com.beansgalaxy.galaxybackpacks.register.ItemRegistry;
import com.beansgalaxy.galaxybackpacks.screen.BackpackInventory;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.OpenHorseScreenS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.screen.HorseScreenHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

public enum Kind {
    NETHERITE("netherite", 12, ArmorMaterials.NETHERITE),
    GOLD("gold", 9, ArmorMaterials.GOLD),
    IRON("iron", 9, ArmorMaterials.IRON),
    WOODEN("wooden", 999, null),
    LEATHER("leather", 4, null),
    NULL("", 999, null);

    private final String kind;
    private final int maxStacks;
    private final ArmorMaterials material;

    Kind(String kind, int maxStacks, ArmorMaterials material) {
        this.kind = kind;
        this.maxStacks = maxStacks;
        this.material = material;
    }

    public Item getItem() {
        Item item = ItemRegistry.NULL_BACKPACK.asItem();
        switch (this) {
            case NETHERITE -> item = ItemRegistry.NETHERITE_BACKPACK.asItem();
            case GOLD -> item = ItemRegistry.GOLD_BACKPACK.asItem();
            case IRON -> item = ItemRegistry.IRON_BACKPACK.asItem();
            case WOODEN -> item = ItemRegistry.WOODEN_BACKPACK.asItem();
            case LEATHER -> item = ItemRegistry.LEATHER_BACKPACK.asItem();
        }
        return item;
    }

    public String getString() {
        return kind;
    }

    public int getMaxStacks() {
        return maxStacks;
    }

    public boolean isTrimmable() {
        return getMaterial() != null;
    }

    public ArmorMaterials getMaterial() {
        return material;
    }

    public static boolean isBackpackItem(ItemStack stack) {
        for(Kind kind : Kind.values())
            if (Objects.equals(stack.getItem(), kind.getItem()))
                return true;
        return false;
    }

    public static ItemStack toStack(BackpackEntity backpack) {
        ItemStack stack = NULL.getItem().getDefaultStack();
        for(Kind kind : Kind.values())
            if (Objects.equals(backpack.getKind(), kind))
                stack = kind.getItem().getDefaultStack();
        int color = backpack.getColor();
        if (color != BackpackEntity.DEFAULT_BACKPACK_COLOR)
            stack.getOrCreateSubNbt("display").putInt("color", color);
        NbtCompound trim = backpack.getTrim();
        if (trim != null)
            stack.setSubNbt("Trim", trim);
        return stack;
    }

    public static Kind fromItem(ItemStack stack) {
        if (isBackpackItem(stack)) {
            BackpackItem backpackItem = (BackpackItem) stack.getItem();
            for (Kind kind : Kind.values())
                if (Objects.equals(backpackItem.getKind(), kind))
                    return kind;
        }
        return NULL;
    }

    public static Kind fromString(String string) {
        for(Kind kind : Kind.values())
            if (Objects.equals(string, kind.kind))
                return kind;
        return NULL;
    }

    public static int getMaxStacks(Kind pKind) {
        for(Kind kind : Kind.values())
            if (Objects.equals(pKind, kind))
                return kind.getMaxStacks();
        return 0;
    }

    public static int getMaxStacks(ItemStack stack) {
        if (isBackpackItem(stack)) {
            BackpackItem backpackItem = (BackpackItem) stack.getItem();
            return getMaxStacks(backpackItem.getKind());
        }
        return 0;
    }

    public static ActionResult openBackpackMenu(PlayerEntity thisPlayer, PlayerEntity otherPlayer) {

        // CHECKS ROTATION OF BOTH PLAYERS
        double yaw = Math.abs(thisPlayer.headYaw - otherPlayer.bodyYaw) % 360 - 180;
        boolean yawMatches = Math.abs(yaw) > 90;

        // OFFSETS OTHER PLAYER'S POSITION
        double angleRadians = Math.toRadians(otherPlayer.bodyYaw);
        double offset = -0.3;
        double x = otherPlayer.getX();
        double z = otherPlayer.getZ();
        double offsetX = Math.cos(angleRadians) * offset;
        double offsetZ = Math.sin(angleRadians) * offset;
        double newX = x - offsetZ;
        double newY = otherPlayer.getEyeY() - .45;
        double newZ = z + offsetX;

        // CHECKS IF PLAYER IS LOOKING
        Vec3d vec3d = thisPlayer.getRotationVec(1.0f).normalize();
        Vec3d vec3d2 = new Vec3d(newX - thisPlayer.getX(), newY - thisPlayer.getEyeY(), newZ - thisPlayer.getZ());
        double d = -vec3d2.length() + 5.65;
        double e = vec3d.dotProduct(vec3d2.normalize());
        double maxRadius = 0.05;
        double radius = (d * d * d * d) / 625;
        boolean looking = e > 1.0 - radius * maxRadius && thisPlayer.canSee(otherPlayer);

        if (yawMatches && looking) { // INTERACT WITH BACKPACK CODE GOES HERE
            // ENABLE THIS LINE OF CODE BELOW TO SHOW WHEN THE BACKPACK IS INTERACTED WITH
            //thisPlayer.getWorld().addParticle(ParticleTypes.FIREWORK, newX, otherPlayer.getEyeY() + 0.1, newZ, 0, 0, 0);

            ItemStack backpackStack = otherPlayer.playerScreenHandler.slots.get(BackpackItem.SLOT_INDEX).getStack();
            DefaultedList<ItemStack> itemStacks = BackpackItem.getInventory(otherPlayer).getItemStacks();

            BackpackEntity backpackEntity = new BackpackEntity(thisPlayer.getWorld(), new BlockPos(0, Integer.MAX_VALUE, 0), Direction.UP);
            backpackEntity.initDisplay(((BackpackItem) backpackStack.getItem()).getKind().getString(), backpackStack);
            backpackEntity.setNoGravity(true);
            backpackEntity.itemStacks = itemStacks;

            World world = thisPlayer.getWorld();
            if (world instanceof ServerWorld serverWorld) {
                serverWorld.tryLoadEntity(backpackEntity);
            }

            thisPlayer.openHandledScreen(backpackEntity);

            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }
}
