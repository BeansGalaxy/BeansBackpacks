package com.beansgalaxy.galaxybackpacks.mixin;

import com.beansgalaxy.galaxybackpacks.entity.BackpackEntity;
import com.beansgalaxy.galaxybackpacks.entity.Kind;
import com.beansgalaxy.galaxybackpacks.entity.PlaySound;
import com.beansgalaxy.galaxybackpacks.item.BackpackItem;
import com.beansgalaxy.galaxybackpacks.networking.packages.SlotClickPacket;
import com.beansgalaxy.galaxybackpacks.networking.packages.SyncBackSlot;
import com.beansgalaxy.galaxybackpacks.networking.packages.syncBackpackInventory;
import com.beansgalaxy.galaxybackpacks.screen.BackpackInventory;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.AbstractRecipeScreenHandler;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerScreenHandler.class)
public abstract class PlayerScreenHandlerMixin extends AbstractRecipeScreenHandler<CraftingInventory> {
    @Shadow @Final public static int OFFHAND_ID;
    @Unique private static final Identifier BACKPACK_ATLAS = new Identifier("textures/atlas/blocks.png");
    @Unique private static final Identifier SLOT = new Identifier("sprites/empty_slot_backpack");
    @Unique private static final Identifier INPUT = new Identifier("sprites/empty_slot_input");

    public PlayerScreenHandlerMixin(ScreenHandlerType<?> screenHandlerType, int i) {
        super(screenHandlerType, i);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onConstructed(PlayerInventory playerInventory, boolean isServerSide, PlayerEntity player, CallbackInfo ci) {
        BackpackItem.SLOT_INDEX = slots.size();
        this.addSlot(new Slot(playerInventory, 41, 59, 62) {
            @Override public int getMaxItemCount() {
                return 1;
            }

            @Override // TODO: NEEDS TO CHECK IF ITEM IS ELYTRA OR A BACKPACK ITEM
            public boolean canInsert(ItemStack stack) {
                return super.canInsert(stack);
            }

            @Override public boolean canTakeItems(PlayerEntity playerEntity) {
                ItemStack itemStack = this.getStack();
                return (itemStack.isEmpty() || playerEntity.isCreative() || !EnchantmentHelper.hasBindingCurse(itemStack)) && BackpackItem.getInventory(playerEntity).getItemStacks().isEmpty();
            }

            @Override public Pair<Identifier, Identifier> getBackgroundSprite() {
                return Pair.of(BACKPACK_ATLAS, SLOT);
            }

            @Override
            public boolean isEnabled() {
                return !player.isCreative();
            }

            public void markDirty() {
                if (player instanceof ServerPlayerEntity serverPlayer)
                    SyncBackSlot.S2C(serverPlayer);
                this.inventory.markDirty();
            }
        });
        BackpackInventory backpackInventory = new BackpackInventory() {
            private final DefaultedList<ItemStack> itemStacks = DefaultedList.of();
            public DefaultedList<ItemStack> getItemStacks() {
                return this.itemStacks;
            }
            public Kind getKind() {
                ItemStack stack = slots.get(BackpackItem.SLOT_INDEX).getStack();
                return Kind.fromItem(stack);
            }
        };
        this.addSlot(new Slot(backpackInventory, 0,59, 45) {

            @Override
            public Pair<Identifier, Identifier> getBackgroundSprite() {
                return Pair.of(BACKPACK_ATLAS, INPUT);
            }

            @Override
            public boolean isEnabled() {
                return Kind.isBackpackItem(BackpackItem.getSlot(player).getStack()) && !player.isCreative();
            }

            public boolean canInsert(ItemStack stack) {
                return backpackInventory.canInsert(stack);
            }

        });
    }

    @Inject(method = "onClosed", at = @At("TAIL")) // TODO: FIND A WAY TO RUN THIS WHEN INVENTORY IS OPENED
    private void onClosed(PlayerEntity player, CallbackInfo ci) {
        if (!player.getWorld().isClient()) {
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
            syncBackpackInventory.S2C(serverPlayer);
        }
    }

    @Inject(method = "quickMove", at = @At("HEAD"))
    private void quickMove(PlayerEntity player, int slot, CallbackInfoReturnable<ItemStack> cir) {
        Slot backpackSlot = this.slots.get(BackpackItem.SLOT_INDEX);
        ItemStack stack = this.slots.get(slot).getStack();
        if (Kind.isBackpackItem(stack) && !backpackSlot.hasStack()) {
            backpackSlot.setStack(stack.copy());
            stack.setCount(0);
        }
    }

    @Override
    public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        if (button < -999) { // interceptSlotClick RETURNS TO HERE THIS BREAKS THE LOOP SINCE I CANNOT REFERENCE METHODS INSIDE A MIXIN
            button = -(button + 1000);
            super.onSlotClick(slotIndex, button, actionType, player);
            return;
        }
        if (actionType == SlotActionType.QUICK_MOVE && slotIndex == BackpackItem.SLOT_INDEX + 1) {
            this.quickMove(player, slotIndex);
            return;
        }
        Slot backpackSlot = this.slots.get(BackpackItem.SLOT_INDEX);
        if (actionType == SlotActionType.QUICK_MOVE || !Kind.isBackpackItem(backpackSlot.getStack())) {
            super.onSlotClick(slotIndex, button, actionType, player);
            return;
        }
        KeyBinding sprintKey = MinecraftClient.getInstance().options.sprintKey;
        boolean sprintKeyPress = InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), sprintKey.getDefaultKey().getCode());
        if (slotIndex == BackpackItem.SLOT_INDEX && !sprintKeyPress) {
            super.onSlotClick(slotIndex, button, actionType, player);
            return;
        }
        if (player.getWorld().isClient) {
            SlotClickPacket.interceptSlotClick(slotIndex, button, actionType, player, sprintKeyPress);
            SlotClickPacket.C2S(slotIndex, button, actionType, sprintKeyPress);
        }
    }

}
