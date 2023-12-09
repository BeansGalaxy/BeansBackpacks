package com.beansgalaxy.beansbackpacks.entity;

import com.beansgalaxy.beansbackpacks.BeansBackpacks;
import com.beansgalaxy.beansbackpacks.screen.BackpackInventory;
import com.beansgalaxy.beansbackpacks.screen.BackpackScreenHandler;
import com.beansgalaxy.beansbackpacks.screen.Viewable;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class Backpack extends Entity implements ExtendedScreenHandlerFactory, BackpackInventory, Viewable {
    public static final TrackedData<String> BACKPACK_KIND = DataTracker.registerData(Backpack.class, TrackedDataHandlerRegistry.STRING);
    public static final TrackedData<Integer> BACKPACK_COLOR = DataTracker.registerData(Backpack.class, TrackedDataHandlerRegistry.INTEGER);
    public static final TrackedData<String> BACKPACK_MATERIAL = DataTracker.registerData(Backpack.class, TrackedDataHandlerRegistry.STRING);
    public static final TrackedData<String> BACKPACK_PATTERN = DataTracker.registerData(Backpack.class, TrackedDataHandlerRegistry.STRING);
    protected static int DEFAULT_BACKPACK_COLOR = 9062433;

    public Backpack(World world) {
        this(BeansBackpacks.ENTITY_BACKPACK, world);
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
        buf.writeInt(getOwner().getId());
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

    public void initDisplay(ItemStack backpackStack) {
        String kind = Kind.fromStack(backpackStack).getString();
        this.dataTracker.set(BACKPACK_KIND, kind);
        if (backpackStack.getSubNbt("display") != null)
            this.dataTracker.set(BACKPACK_COLOR, backpackStack.getSubNbt("display").getInt("color"));
        if (backpackStack.getSubNbt("Trim") != null) {
            this.dataTracker.set(BACKPACK_MATERIAL, backpackStack.getSubNbt("Trim").getString("material"));
            this.dataTracker.set(BACKPACK_PATTERN, backpackStack.getSubNbt("Trim").getString("pattern"));
        }
    }

    protected void readCustomDataFromNbt(NbtCompound tag) {
    }

    protected void writeCustomDataToNbt(NbtCompound tag) {
    }

    public void onClose(PlayerEntity player) {
        this.removeViewer(player);
        if (getViewers() < 1)
            PlaySound.CLOSE.at(this);
    }

    public void playSound(PlaySound sound) {
        sound.at(getOwner(), 0.3f);
    }

    public ScreenHandler createMenu(int id, PlayerInventory inventory, PlayerEntity player) {
        if (player.isSpectator()) {
            return null;
        } else {
            this.addViewer(player);
            return new BackpackScreenHandler(id, player.getInventory(), this, getOwner());
        }
    }

    // TODO: CONFIG FILE TO DISABLE "maxIterations"

    /**
     * Ideally player.dropStack(stack, 0.5f) would handle dropping all items from the Decorated Pot's inventory without the if statement,
     * however, picking up from an ItemEntity is only capped at {@link Inventory#getMaxCountPerStack()} and not {@link ItemStack#getMaxCount()}.
     * For most items it works as expected, so I chose to use both rather than dropping each stack separate every time.
     */
    public static void drop(PlayerEntity player, ItemStack backpackStack, DefaultedList<ItemStack> itemStacks) {
        Kind kind = Kind.fromStack(backpackStack);
        if (!Kind.isBackpack(backpackStack)) {
            player.dropStack(backpackStack.copy(), 0.5f);
            if (kind.isOf(Kind.POT)) {
                int iteration = 0;
                int maxIterations = BeansBackpacks.CONFIG.limitDroppedStacks();
                while (!itemStacks.isEmpty() && iteration < maxIterations) {
                    ItemStack stack = itemStacks.remove(iteration);
                    if (stack.getMaxCount() == 64) {
                        player.dropStack(stack, 0.5f);
                    } else while (stack.getCount() > 0) {
                        int removedCount = Math.min(stack.getCount(), stack.getMaxCount());
                        player.dropStack(stack.copyWithCount(removedCount));
                        stack.decrement(removedCount);
                    }
                    iteration++;
                }
                SoundEvent soundEvent = iteration >= maxIterations ? SoundEvents.BLOCK_DECORATED_POT_BREAK : SoundEvents.BLOCK_DECORATED_POT_SHATTER;
                player.playSound(soundEvent, 0.4f, 0.8f);
            }
            return;
        }

        BlockPos blockPos = player.getBlockPos();
        int x = blockPos.getX();
        double y = blockPos.getY() + 2D / 16;
        int z = blockPos.getZ();

        new BackpackEntity(player, player.getWorld(), x, y, z, Direction.UP,
                    itemStacks, backpackStack, player.getYaw());

        PlaySound.DROP.at(player);

    }

    @Override
    public Entity getOwner() {
        return this;
    }

    public boolean isOpen() {
        return getViewers() > 0 || isMirror();
    }

    public final DefaultedList<PlayerEntity> playersViewing = DefaultedList.of();

    public DefaultedList<PlayerEntity> getPlayersViewing() {
        return playersViewing;
    }

    public float headPitch = 0;

    @Override
    public float getHeadPitch() {
        return headPitch;
    }

    @Override
    public void setHeadPitch(float headPitch) {
        this.headPitch = headPitch;
    }

    public byte viewers = 0;

    public byte getViewers() {
        return viewers;
    }

    @Override
    public void setViewers(byte viewers) {
        this.viewers = viewers;
    }
}
