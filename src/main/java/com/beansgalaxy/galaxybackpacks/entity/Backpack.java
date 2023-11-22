package com.beansgalaxy.galaxybackpacks.entity;

import com.beansgalaxy.galaxybackpacks.Main;
import com.beansgalaxy.galaxybackpacks.item.BackpackItem;
import com.beansgalaxy.galaxybackpacks.networking.packages.syncBackpackInventory;
import com.beansgalaxy.galaxybackpacks.screen.BackpackInventory;
import com.beansgalaxy.galaxybackpacks.screen.BackpackScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class Backpack extends Entity implements ExtendedScreenHandlerFactory, BackpackInventory  {
    public static final TrackedData<String> BACKPACK_KIND = DataTracker.registerData(Backpack.class, TrackedDataHandlerRegistry.STRING);
    public static final TrackedData<Integer> BACKPACK_COLOR = DataTracker.registerData(Backpack.class, TrackedDataHandlerRegistry.INTEGER);
    public static final TrackedData<String> BACKPACK_MATERIAL = DataTracker.registerData(Backpack.class, TrackedDataHandlerRegistry.STRING);
    public static final TrackedData<String> BACKPACK_PATTERN = DataTracker.registerData(Backpack.class, TrackedDataHandlerRegistry.STRING);
    protected static int DEFAULT_BACKPACK_COLOR = 9062433;
    public float headX = 0;

    public Backpack(World world) {
        this(Main.ENTITY_BACKPACK, world);
    }

    public Backpack(EntityType<?> type, World world) {
        super(type, world);
    }

    public boolean isMirror() {
        boolean notMirror = this instanceof BackpackEntity;
        return !notMirror;
    }

    // COMMUNICATES WITH "BackpackInventory"
    public DefaultedList<ItemStack> itemStacks = DefaultedList.of();
    public DefaultedList<ItemStack> getItemStacks() {
        return this.itemStacks;
    }

    public Kind getKind() {
        return Kind.fromString(this.dataTracker.get(BACKPACK_KIND));
    }

    public int getColor() {
        return this.dataTracker.get(BACKPACK_COLOR);
    }

    public NbtCompound getTrim() {
        String material = this.dataTracker.get(BACKPACK_MATERIAL);
        String pattern = this.dataTracker.get(BACKPACK_PATTERN);
        if (!pattern.isEmpty() && !material.isEmpty()) {
            NbtCompound tag = new NbtCompound();
            tag.putString("material", material);
            tag.putString("pattern", pattern);
            return tag;
        } else return null;
    }

    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        buf.writeInt(this.getId());
    }

    protected void initDataTracker() {
        this.dataTracker.startTracking(BACKPACK_KIND, "");
        this.dataTracker.startTracking(BACKPACK_COLOR, DEFAULT_BACKPACK_COLOR);
        this.dataTracker.startTracking(BACKPACK_MATERIAL, "");
        this.dataTracker.startTracking(BACKPACK_PATTERN, "");
    }

    public void setDisplay(NbtCompound display) {
        this.dataTracker.set(BACKPACK_KIND, display.getString("Kind"));
        this.dataTracker.set(BACKPACK_COLOR, display.getInt("Color"));
        this.dataTracker.set(BACKPACK_MATERIAL, display.getString("Material"));
        this.dataTracker.set(BACKPACK_PATTERN, display.getString("Pattern"));
    }

    public void initDisplay(String kind, ItemStack item) {
        this.dataTracker.set(BACKPACK_KIND, kind);
        if (item.getSubNbt("display") != null)
            this.dataTracker.set(BACKPACK_COLOR, item.getSubNbt("display").getInt("color"));
        if (item.getSubNbt("Trim") != null) {
            this.dataTracker.set(BACKPACK_MATERIAL, item.getSubNbt("Trim").getString("material"));
            this.dataTracker.set(BACKPACK_PATTERN, item.getSubNbt("Trim").getString("pattern"));
        }
    }

    protected void readCustomDataFromNbt(NbtCompound tag) {
    }

    protected void writeCustomDataToNbt(NbtCompound tag) {
    }

    public int viewers = 0;
    public boolean isOpen() {
        return viewers > 0 || isMirror();
    }

    public void onOpen(PlayerEntity player) {
        getItemStacks().remove(ItemStack.EMPTY);
    }

    public ScreenHandler createMenu(int id, PlayerInventory inventory, PlayerEntity player) {
        if (player.isSpectator()) {
            return null;
        } else {
            return new BackpackScreenHandler(id, player.getInventory(), this);
        }
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
            ItemStack backpackStack = otherPlayer.playerScreenHandler.slots.get(BackpackItem.SLOT_INDEX).getStack();
            if (!Kind.isBackpackItem(backpackStack))
                return ActionResult.PASS;

            Backpack backpack = new Backpack(thisPlayer.getWorld()) {
                public void markDirty() {
                    if (thisPlayer instanceof ServerPlayerEntity serverPlayer)
                        syncBackpackInventory.S2C(serverPlayer);
                }
            };

            DefaultedList<ItemStack> itemStacks = BackpackItem.getInventory(otherPlayer).getItemStacks();
            backpack.initDisplay(((BackpackItem) backpackStack.getItem()).getKind().getString(), backpackStack);
            backpack.itemStacks = itemStacks;
            if (thisPlayer.getWorld() instanceof ServerWorld serverWorld)
                serverWorld.tryLoadEntity(backpack);
            thisPlayer.openHandledScreen(backpack);

            // ENABLE THIS LINE OF CODE BELOW TO SHOW WHEN THE BACKPACK IS INTERACTED WITH
            //thisPlayer.getWorld().addParticle(ParticleTypes.FIREWORK, newX, otherPlayer.getEyeY() + 0.1, newZ, 0, 0, 0);
            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }
}
