package com.beansgalaxy.galaxybackpacks.screen;

import com.beansgalaxy.galaxybackpacks.entity.Backpack;
import com.beansgalaxy.galaxybackpacks.entity.Kind;
import com.beansgalaxy.galaxybackpacks.entity.PlaySound;
import com.beansgalaxy.galaxybackpacks.networking.packages.OpenPlayerBackpackPacket;
import com.beansgalaxy.galaxybackpacks.networking.packages.SyncBackSlot;
import com.beansgalaxy.galaxybackpacks.networking.packages.SyncBackpackInventory;
import com.beansgalaxy.galaxybackpacks.networking.packages.SyncBackpackViewersPacket;
import com.mojang.datafixers.util.Pair;
import net.minecraft.advancement.AdvancementManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.OtherClientPlayerEntity;
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

import java.util.List;

import static net.minecraft.screen.PlayerScreenHandler.HOTBAR_END;
import static net.minecraft.screen.PlayerScreenHandler.INVENTORY_START;

public class BackSlot extends Slot implements Viewable {
    private static final Identifier EMPTY_SLOT_SMITHING_TEMPLATE_ARMOR_TRIM_TEXTURE = new Identifier("item/empty_slot_smithing_template_armor_trim");
    private static final Identifier EMPTY_SLOT_SMITHING_TEMPLATE_NETHERITE_UPGRADE_TEXTURE = new Identifier("item/empty_slot_smithing_template_netherite_upgrade");
    public static final List<Identifier> EMPTY_SLOT_TEXTURES = List.of(EMPTY_SLOT_SMITHING_TEMPLATE_ARMOR_TRIM_TEXTURE, EMPTY_SLOT_SMITHING_TEMPLATE_NETHERITE_UPGRADE_TEXTURE);

    private static final Identifier SLOT_BACKPACK = new Identifier("sprites/empty_slot_backpack");
    private static final Identifier SLOT_ELYTRA = new Identifier("sprites/empty_slot_elytra");

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
        ItemStack backpackStack = owner.playerScreenHandler.slots.get(SLOT_INDEX).getStack();
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

            Backpack backpack = new Backpack(viewer.getWorld()) {
                public Entity getOwner() {
                    return owner;
                }

                public void markDirty() {
                    if (owner instanceof ServerPlayerEntity serverPlayer)
                        SyncBackpackInventory.S2C(serverPlayer);
                }

                public void onClose(PlayerEntity player) {
                    BackSlot backSlot = get(owner);
                    backSlot.removeViewer(viewer);
                    if (backSlot.getViewers() < 1)
                        PlaySound.CLOSE.at(owner);
                }

                public void updateViewers() {
                }

                public void playSound(PlaySound sound) {
                    sound.at(owner, 0.3f);
                }
            };

            if (owner instanceof OtherClientPlayerEntity cOwner && viewer instanceof ClientPlayerEntity cViewer)
             OpenPlayerBackpackPacket.C2S(cOwner, cViewer);

            DefaultedList<ItemStack> itemStacks = getInventory(owner).getItemStacks();
            backpack.initDisplay(backpackStack);
            backpack.itemStacks = itemStacks;
            if (viewer.getWorld() instanceof ServerWorld serverWorld)
                serverWorld.tryLoadEntity(backpack);
            viewer.openHandledScreen(backpack);

            // ENABLE THIS LINE OF CODE BELOW TO SHOW WHEN THE BACKPACK IS INTERACTED WITH
            //owner.getWorld().addParticle(ParticleTypes.FIREWORK, newX, viewer.getEyeY() + 0.1, newZ, 0, 0, 0);

            PlaySound.OPEN.at(owner);
            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }

    public static List<Identifier> getTextures() {
        AdvancementManager manager = MinecraftClient.getInstance().getNetworkHandler().getAdvancementHandler().getManager();
        boolean hasEndGoal = manager.get(Identifier.tryParse("end/root")) != null;
        //System.out.println("Has End Goal: " + hasEndGoal + "   Has Elytra Goal: " + hasElytraGoal);

        if (hasEndGoal)
            return List.of(SLOT_ELYTRA, SLOT_BACKPACK);

        return List.of(SLOT_BACKPACK);
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
            SyncBackSlot.S2C(serverPlayer);
            SyncBackpackInventory.S2C(serverPlayer);
        }
    }

    // RETURN FALSE TO CANCEL A PLAYER'S INVENTORY CLICK
    public static boolean continueSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        PlayerScreenHandler playerScreenHandler = player.playerScreenHandler;
        BackpackInventory backpackInventory = BackSlot.getInventory(player);
        PlayerInventory playerInventory = player.getInventory();
        ItemStack cursorStack = playerScreenHandler.getCursorStack();

        BackSlot backSlot = BackSlot.get(player);
        ItemStack backStack = backSlot.getStack();

        Slot slot = playerScreenHandler.slots.get(slotIndex);
        ItemStack stack = slot.getStack();

        if (slotIndex < BackSlot.SLOT_INDEX && backSlot.sprintKeyIsPressed && backStack.isEmpty() && Kind.isWearable(stack)) {
            backSlot.insertStack(stack);
            return false;
        }

        if (actionType == SlotActionType.QUICK_MOVE) {
            if (slotIndex == BackSlot.SLOT_INDEX + 1) {
                if (Kind.POT.isOf(backStack))
                    playerInventory.insertStack(backpackInventory.removeStack(0));
                else if (!Kind.isBackpack(backStack) && Kind.fromStack(backStack) != null) {
                    ItemStack backpackStack = backpackInventory.getItemStacks().get(0);
                    player.getInventory().insertStack(backpackStack);
                    if (backpackStack.isEmpty())
                        backpackInventory.removeStack(0);
                } else
                    playerScreenHandler.quickMove(player, slotIndex);
                return false;
            }
        }
        else if (Kind.isWearable(backStack)) {
            if (backSlot.sprintKeyIsPressed) {
                if (slotIndex == BackSlot.SLOT_INDEX && Kind.isBackpack(stack)) {
                    if (backpackInventory.isEmpty())
                        player.getInventory().insertStack(stack);
                    else {
                        Backpack.drop(player, stack, backpackInventory.getItemStacks());
                        stack.setCount(0);
                    }
                    return false;
                }
                if (slotIndex == BackSlot.SLOT_INDEX + 1) {
                    Item compareItem = backpackInventory.getStack(0).copy().getItem();
                    boolean continueInsert = true;
                    boolean itemRemoved = false;
                    while (!backpackInventory.isEmpty() && backpackInventory.getStack(0).isOf(compareItem) && continueInsert) {
                        continueInsert = playerInventory.insertStack(backpackInventory.removeStackSilent(0));
                        itemRemoved = true;
                    }
                    if (itemRemoved)
                        PlaySound.TAKE.toClient(player);
                    return false;
                }
                if (slotIndex < BackSlot.SLOT_INDEX) {
                    if (actionType == SlotActionType.PICKUP_ALL)
                        moveAll(backpackInventory, playerScreenHandler);
                    else slot.setStack(backpackInventory.insertStack(stack, stack.getCount()));
                } else {
                    playerInventory.insertStack(stack);
                }
                return false;
            }
            else {
                if (slotIndex == BackSlot.SLOT_INDEX + 1) {
                    if (actionType == SlotActionType.PICKUP_ALL)
                        return false;
                    ItemStack backpackSlotStack = playerScreenHandler.slots.get(BackSlot.SLOT_INDEX + 1).getStack();
                    if (button == 1 && !cursorStack.isOf(backpackSlotStack.getItem()) && !cursorStack.isEmpty()) {
                        backpackInventory.insertStack(cursorStack, 1);
                        return false;
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
