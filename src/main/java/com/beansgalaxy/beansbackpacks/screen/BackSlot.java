package com.beansgalaxy.beansbackpacks.screen;

import com.beansgalaxy.beansbackpacks.client.ClientPlaySound;
import com.beansgalaxy.beansbackpacks.entity.Backpack;
import com.beansgalaxy.beansbackpacks.entity.Kind;
import com.beansgalaxy.beansbackpacks.entity.PlaySound;
import com.beansgalaxy.beansbackpacks.networking.packages.sSyncBackSlot;
import com.beansgalaxy.beansbackpacks.networking.packages.sSyncBackpackInventory;
import com.mojang.datafixers.util.Pair;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static net.minecraft.screen.PlayerScreenHandler.HOTBAR_END;
import static net.minecraft.screen.PlayerScreenHandler.INVENTORY_START;

public class BackSlot extends Slot implements Viewable {
    private static final Identifier EMPTY_SLOT_SMITHING_TEMPLATE_ARMOR_TRIM_TEXTURE = new Identifier("item/empty_slot_smithing_template_armor_trim");
    private static final Identifier EMPTY_SLOT_SMITHING_TEMPLATE_NETHERITE_UPGRADE_TEXTURE = new Identifier("item/empty_slot_smithing_template_netherite_upgrade");
    public static final List<Identifier> EMPTY_SLOT_TEXTURES = List.of(EMPTY_SLOT_SMITHING_TEMPLATE_ARMOR_TRIM_TEXTURE, EMPTY_SLOT_SMITHING_TEMPLATE_NETHERITE_UPGRADE_TEXTURE);

    public static final Identifier BACKPACK_ATLAS = new Identifier("textures/atlas/blocks.png");
    public static int SLOT_INDEX;
    private final PlayerEntity owner;
    public boolean sprintKeyIsPressed = false;

    public BackSlot(Inventory inventory, int index, int x, int y, PlayerEntity player) {
        super(inventory, index, x, y);
        this.owner = player;
    }

    public static BackpackInventory getInventory(PlayerEntity player) {
        return (BackpackInventory) player.playerScreenHandler.slots.get(SLOT_INDEX + 1).inventory;
    }

    public static BackSlot get(PlayerEntity player) {
        return (BackSlot) player.playerScreenHandler.slots.get(SLOT_INDEX);
    }

    public static ActionResult openPlayerBackpackMenu(PlayerEntity viewer, PlayerEntity owner) {
        BackSlot backSlot = BackSlot.get(owner);
        ItemStack backpackStack = backSlot.getStack();
        if (!Kind.isBackpack(backpackStack))
            return ActionResult.PASS;

        // CHECKS ROTATION OF BOTH PLAYERS
        boolean yawMatches = Viewable.yawMatches(viewer.headYaw, owner.bodyYaw, 90d);

        // OFFSETS OTHER PLAYER'S POSITION
        double angleRadians = Math.toRadians(owner.bodyYaw);
        double offset = -0.3;
        double x = owner.getX();
        double z = owner.getZ();
        double offsetX = Math.cos(angleRadians) * offset;
        double offsetZ = Math.sin(angleRadians) * offset;
        double newX = x - offsetZ;
        double newY = owner.getEyeY() - .45;
        double newZ = z + offsetX;

        // CHECKS IF PLAYER IS LOOKING
        Vec3d vec3d = viewer.getRotationVec(1.0f).normalize();
        Vec3d vec3d2 = new Vec3d(newX - viewer.getX(), newY - viewer.getEyeY(), newZ - viewer.getZ());
        double d = -vec3d2.length() + 5.65;
        double e = vec3d.dotProduct(vec3d2.normalize());
        double maxRadius = 0.05;
        double radius = (d * d * d * d) / 625;
        boolean looking = e > 1.0 - radius * maxRadius && viewer.canSee(owner);

        if (yawMatches && looking) { // INTERACT WITH BACKPACK CODE GOES HERE
            Backpack backpack = getBackpack(viewer, owner);
            if (viewer.getWorld() instanceof ServerWorld serverWorld)
                serverWorld.tryLoadEntity(backpack);

            // ENABLE THIS LINE OF CODE BELOW TO SHOW WHEN THE BACKPACK IS INTERACTED WITH
            //owner.getWorld().addParticle(ParticleTypes.FIREWORK, newX, viewer.getEyeY() + 0.1, newZ, 0, 0, 0);

            PlaySound.OPEN.at(owner);
            if (!viewer.getWorld().isClient())
                viewer.openHandledScreen(backpack);

            if (!viewer.getWorld().isClient() && viewer.currentScreenHandler != viewer.playerScreenHandler) {
                    backSlot.addViewer(viewer);
            }

            return ActionResult.success(!viewer.getWorld().isClient);
        }

        return ActionResult.PASS;
    }

    @NotNull
    public static Backpack getBackpack(PlayerEntity viewer, PlayerEntity owner) {
        Backpack backpack = new Backpack(viewer.getWorld()) {

            public Entity getOwner() {
                return owner;
            }

            public void markDirty() {
                if (owner instanceof ServerPlayerEntity serverPlayer)
                    sSyncBackpackInventory.S2C(serverPlayer);
            }

            public void onClose(PlayerEntity player) {
                BackSlot backSlot = get(owner);
                backSlot.removeViewer(viewer);
                if (backSlot.getViewers() < 1)
                    PlaySound.CLOSE.at(owner);
            }

            public void tick() {
            }

            public void updateViewers() {
            }

            public void playSound(PlaySound sound) {
                sound.at(owner, 0.3f);
            }
        };
        ItemStack backpackStack = BackSlot.get(owner).getStack();

        DefaultedList<ItemStack> itemStacks = getInventory(owner).getItemStacks();
        backpack.initDisplay(backpackStack);
        backpack.itemStacks = itemStacks;
        return backpack;
    }

    public int getMaxItemCount() {
        return 1;
    }

    public boolean canInsert(ItemStack stack) {
        return Kind.isWearable(stack);
    }

    public boolean canTakeItems(PlayerEntity playerEntity) {
        ItemStack itemStack = this.getStack();
        return (itemStack.isEmpty() || playerEntity.isCreative() || !EnchantmentHelper.hasBindingCurse(itemStack)) && (getInventory(playerEntity).getItemStacks().isEmpty() || Kind.ELYTRA.isOf(itemStack));
    }

    public Pair<Identifier, Identifier> getBackgroundSprite() {
        return null;
    }

    public boolean isEnabled() {
        return !owner.isCreative();
    }

    public void markDirty() {
        if (this.getStack().isEmpty())
            clearViewers();
        if (owner instanceof ServerPlayerEntity serverPlayer) {
            sSyncBackSlot.S2C(serverPlayer);
            sSyncBackpackInventory.S2C(serverPlayer);
        }
    }

    // RETURN FALSE TO CANCEL A PLAYER'S INVENTORY CLICK
    public static boolean continueSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        if (slotIndex < 0)
            return true;

        PlayerScreenHandler playerScreenHandler = player.playerScreenHandler;
        BackpackInventory backpackInventory = BackSlot.getInventory(player);
        PlayerInventory playerInventory = player.getInventory();
        ItemStack cursorStack = playerScreenHandler.getCursorStack();

        BackSlot backSlot = BackSlot.get(player);
        ItemStack backStack = backSlot.getStack();

        ItemStack backpackStack = backpackInventory.getStack(0);
        int maxStack = backpackStack.getMaxCount();

        Slot slot = playerScreenHandler.slots.get(slotIndex);
        ItemStack stack = slot.getStack();


        if (slotIndex == SLOT_INDEX + 1 && Kind.POT.isOf(backStack)) {
            if (actionType == SlotActionType.THROW && cursorStack.isEmpty()) {
                int count = button == 0 ? 1 : Math.min(stack.getCount(), maxStack);
                ItemStack itemStack = backpackInventory.removeStack(0, count);
                player.dropItem(itemStack, true);
                return false;
            }
            if (actionType == SlotActionType.SWAP) {
                ItemStack itemStack = playerInventory.getStack(button);
                if (itemStack.isEmpty()) {
                    if (backpackStack.getCount() > maxStack) {
                        playerInventory.setStack(button, backpackStack.copyWithCount(maxStack));
                        backpackStack.decrement(maxStack);
                        return false;
                    }
                    playerInventory.setStack(button, backpackInventory.removeStack(0));
                }
                else {
                    if (backpackStack.isEmpty())
                        return true;
                    if (backpackStack.getCount() > maxStack)
                        if (playerInventory.insertStack(-1, itemStack)) {
                            playerInventory.setStack(button, backpackStack.copyWithCount(maxStack));
                            backpackStack.decrement(maxStack);
                            return false;
                        }
                    playerInventory.setStack(button, backpackInventory.removeStack(0));
                    backpackInventory.insertStack(itemStack, itemStack.getCount());
                }
                return false;
            }
            if (button == 1 && cursorStack.isEmpty() && backpackStack.getCount() > maxStack) {
                int count = Math.max(1, maxStack / 2);
                ItemStack splitStack = backpackInventory.removeStack(0, count);
                playerScreenHandler.setCursorStack(splitStack);
                return false;
            }
        }

        if (actionType == SlotActionType.THROW)
            return true;

        if (slotIndex < SLOT_INDEX && backSlot.sprintKeyIsPressed && backStack.isEmpty() && Kind.isWearable(stack)) {
            backSlot.insertStack(stack);
            return false;
        }

        if (actionType == SlotActionType.QUICK_MOVE) {
            if (slotIndex == SLOT_INDEX + 1) {
                if (Kind.POT.isOf(backStack))
                    playerInventory.insertStack(-1, backpackInventory.removeStack(0));
                else if (!Kind.isBackpack(backStack) && Kind.fromStack(backStack) != null) {
                    player.getInventory().insertStack(-1, backpackStack);
                    if (backpackStack.isEmpty())
                        backpackInventory.removeStack(0);
                } else
                    playerScreenHandler.quickMove(player, slotIndex);
                return false;
            }
        }
        else if (Kind.isWearable(backStack)) {
            if (backSlot.sprintKeyIsPressed) {
                if (slotIndex == SLOT_INDEX && Kind.isStorage(stack)) {
                    if (Kind.POT.isOf(stack))
                        return false;
                    if (backpackInventory.isEmpty())
                        player.getInventory().insertStack(-1, stack);
                    else {
                        Backpack.drop(player, stack, backpackInventory.getItemStacks());
                        stack.setCount(0);
                    }
                    return false;
                }
                if (slotIndex == SLOT_INDEX + 1) {
                    Item compareItem = backpackInventory.getStack(0).copy().getItem();
                    boolean continueInsert = true;
                    boolean itemRemoved = false;
                    while (!backpackInventory.isEmpty() && backpackInventory.getStack(0).isOf(compareItem) && continueInsert) {
                        continueInsert = playerInventory.insertStack(-1, backpackInventory.removeStackSilent(0));
                        itemRemoved = true;
                    }
                    if (itemRemoved)
                        PlaySound.TAKE.toClient(player);
                    return false;
                }
                if (slotIndex < SLOT_INDEX) {
                    if (actionType == SlotActionType.PICKUP_ALL)
                        moveAll(backpackInventory, playerScreenHandler);
                    else slot.setStack(backpackInventory.insertStack(stack, stack.getCount()));
                } else {
                    playerInventory.insertStack(-1, stack);
                }
                return false;
            }
            else {
                if (slotIndex == SLOT_INDEX + 1) {
                    if (actionType == SlotActionType.PICKUP_ALL)
                        return false;

                    if (button == 1) {
                        if (!cursorStack.isOf(backpackStack.getItem()) && !cursorStack.isEmpty()) {
                            backpackInventory.insertStack(cursorStack, 1);
                            return false;
                        }
                    }
                    if (button == 0) {
                        playerScreenHandler.setPreviousCursorStack(cursorStack);
                        ItemStack returnStack = backpackInventory.returnStack(0, cursorStack);
                        playerScreenHandler.setCursorStack(returnStack);
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private static void moveAll(BackpackInventory backpackInventory, PlayerScreenHandler playerScreenHandler) {
        DefaultedList<Slot> slots = playerScreenHandler.slots;
        ItemStack cursorStack = playerScreenHandler.getCursorStack();
        int matchingItemsTotalCount = 0;
        for (int j = INVENTORY_START; j < HOTBAR_END; j++) {
            Slot thisSlot = slots.get(j);
            ItemStack thisStack = thisSlot.getStack();
            if (thisStack.isOf(cursorStack.getItem())) {
                matchingItemsTotalCount += thisStack.getCount();
                thisSlot.setStack(ItemStack.EMPTY);
            }
        }
        if (matchingItemsTotalCount > 0)
            backpackInventory.playSound(PlaySound.INSERT);
        while (matchingItemsTotalCount > 0) {
            int itemsMaxCount = cursorStack.getMaxCount();
            if (matchingItemsTotalCount > itemsMaxCount) {
                backpackInventory.insertStackSilent(cursorStack.copy(), itemsMaxCount);
                matchingItemsTotalCount -= itemsMaxCount;
            } else {
                backpackInventory.insertStackSilent(cursorStack.copy(), matchingItemsTotalCount);
                matchingItemsTotalCount = 0;
            }
        }
    }

    public boolean pickupItemEntity(PlayerInventory instance, ItemStack stack) {
        if (stack.isEmpty())
            return false;

        PlayerEntity player = instance.player;
        BackSlot backSlot = BackSlot.get(player);
        BackpackInventory backpackInventory = BackSlot.getInventory(player);

        if (backpackInventory.getKind() == null || !Kind.isStorage(backSlot.getStack()))
            return instance.insertStack(-1, stack);

        if (backpackInventory.canInsert(stack)) {
            instance.main.forEach(stacks -> {
                if (stacks.isOf(stack.getItem())) {
                    int present = stacks.getCount();
                    int inserted = stack.getCount();
                    int count = present + inserted;
                    int remainder = Math.max(0, count - stack.getMaxCount());
                    count -= remainder;

                    stacks.setCount(count);
                    stack.setCount(remainder);
                }
            });

            backpackInventory.getItemStacks().forEach(stacks -> {
                if (stacks.isOf(stack.getItem())) {
                    backpackInventory.insertStackSilent(stack, stack.getCount());
                    backpackInventory.markDirty();
                }
            });
        }

        if (stack.isEmpty())
            return true;

        if (instance.insertStack(-1, stack))
            return true;

        if (backpackInventory.canInsert(stack))
            return backpackInventory.insertStackSilent(stack, stack.getCount()).isEmpty();

        return false;
    }

    /** OPENING ANIMATIONS */

    @Override
    public Entity getOwner() {
        return owner;
    }

    DefaultedList<PlayerEntity> playersViewing = DefaultedList.of();

    @Override
    public DefaultedList<PlayerEntity> getPlayersViewing() {
        return playersViewing;
    }

    private float headPitch = 0;

    @Override
    public float getHeadPitch() {
        return headPitch;
    }

    @Override
    public void setHeadPitch(float headPitch) {
        this.headPitch = headPitch;
    }

    private byte viewers = 0;

    @Override
    public byte getViewers() {
        return viewers;
    }

    @Override
    public void setViewers(byte viewers) {
        this.viewers = viewers;
    }
}
