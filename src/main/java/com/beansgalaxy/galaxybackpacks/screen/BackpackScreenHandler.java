package com.beansgalaxy.galaxybackpacks.screen;

import com.beansgalaxy.galaxybackpacks.entity.Backpack;
import com.beansgalaxy.galaxybackpacks.entity.BackpackEntity;
import com.beansgalaxy.galaxybackpacks.register.ScreenHandlersRegistry;
import com.mojang.datafixers.util.Pair;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class BackpackScreenHandler extends ScreenHandler {
    private static final Identifier BACKPACK_ATLAS = new Identifier("textures/atlas/blocks.png");
    private static final Identifier INPUT = new Identifier("sprites/empty_slot_input_large");
    private static int BACKPACK_SLOT_INDEX;
    public final Backpack entity;
    protected final Backpack mirror;
    protected final PlayerEntity viewer;
    protected final Entity owner;
    protected final BlockPos ownerPos;
    protected final float ownerYaw;
    public int invOffset = 108;

    public BackpackScreenHandler(int syncId, PlayerInventory inventory, PacketByteBuf buf) {
        this(syncId, inventory, inventory.player.getWorld().getEntityById(buf.readInt()), inventory.player.getWorld().getEntityById(buf.readInt()));
    }

    public BackpackScreenHandler(int syncId, PlayerInventory playerInventory, Entity entity, Entity owner) {
        super(ScreenHandlersRegistry.BACKPACK_SCREEN_HANDLER, syncId);
        this.owner = owner;
        this.ownerPos = owner.getBlockPos();
        this.ownerYaw = owner.getBodyYaw();
        if (entity instanceof BackpackEntity bEntity) {
            this.entity = bEntity;
            this.mirror = bEntity.createMirror();
        }
        else {
            this.entity = (Backpack) entity;
            this.mirror = this.entity;
        }
        this.viewer = playerInventory.player;
        createInventorySlots(playerInventory);
        BACKPACK_SLOT_INDEX = slots.size();
        createBackpackSlots(this.entity);

        //this.entity.onOpen(playerInventory.player);
    }

    private void createBackpackSlots(Inventory inventory) {
        this.addSlot(new Slot(entity, 0, 80, 57) {
            @Override public Pair<Identifier, Identifier> getBackgroundSprite() {
                return Pair.of(BACKPACK_ATLAS, INPUT);
            }

            @Override public void setStackNoCallbacks(ItemStack stack) {
                this.inventory.setStack(0, stack);
                this.markDirty();
            }

            @Override public boolean canInsert(ItemStack stack) {
                return entity.canInsert(stack);
            }
        });

        int columns = 7;
        int rows = 4;
        int spacing = 17;
        int invCenter = 8 + (4 * 18); // = 80
        int bpCenter = (columns / 2) * spacing;        // = -54
        int x = invCenter - bpCenter;
        int y = invOffset - 33;
        if (rows < 4) y += 8;

        for(int r = 0; r < rows; ++r)
            for(int c = 0; c < columns; ++c)
                this.addSlot(new Slot(inventory, c + 1 + r * columns, x + c * spacing, y + r * spacing) {
                    public boolean canInsert(ItemStack p_40231_) {
                        return false;
                    }
                });
    }

    private void createInventorySlots(Inventory playerInventory) {
        for(int l = 0; l < 3; ++l) {
            for(int k = 0; k < 9; ++k) {
                this.addSlot(new Slot(playerInventory, k + l * 9 + 9, 8 + k * 18, l * 18 + 51 + invOffset));
            }
        }
        for(int i1 = 0; i1 < 9; ++i1) {
            this.addSlot(new Slot(playerInventory, i1, 8 + i1 * 18, 109 + invOffset));
        }
    }

    public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        BackpackInventory backpackInventory = entity;
        if (slotIndex == BACKPACK_SLOT_INDEX && actionType != SlotActionType.QUICK_MOVE) {
            ItemStack cursorStack = this.getCursorStack();
            ItemStack slotStack = backpackInventory.getStack(0);
            if (button == 0) {
                this.setPreviousCursorStack(cursorStack);
                ItemStack stack = backpackInventory.returnStack(0, cursorStack);
                this.setCursorStack(stack);
                return;
            }
            if (button == 1 && cursorStack.isOf(slotStack.getItem()) && !cursorStack.isEmpty()) {
                ItemStack stack = backpackInventory.insertStack(cursorStack, 1);
                this.setCursorStack(stack);
                return;
            }
        }
        super.onSlotClick(slotIndex, button, actionType, player);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slotId) {
        Slot clickedSlot = slots.get(slotId);
        ItemStack clickedStack = clickedSlot.getStack();
        if (clickedStack == ItemStack.EMPTY)
            return ItemStack.EMPTY;
        if (slotId < BACKPACK_SLOT_INDEX) { // HANDLES INSERT TO BACKPACK
            if (entity.spaceLeft() < 1)
                return ItemStack.EMPTY;
            entity.insertStack(clickedStack, clickedStack.getCount());
            clickedSlot.setStack(clickedStack);
        } else { // HANDLES INSERT TO INVENTORY
            clickedStack = entity.getItemStacks().get(slotId - BACKPACK_SLOT_INDEX);
            this.insertItem(clickedStack, 0, BACKPACK_SLOT_INDEX, true);
            if (clickedStack.isEmpty()) entity.getItemStacks().remove(slotId - BACKPACK_SLOT_INDEX);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

    public void onClosed(PlayerEntity player) {
        entity.onClose(player);
        mirror.discard();
        super.onClosed(player);
    }
}
