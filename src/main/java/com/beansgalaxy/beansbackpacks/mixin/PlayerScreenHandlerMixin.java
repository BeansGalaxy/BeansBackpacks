package com.beansgalaxy.beansbackpacks.mixin;

import com.beansgalaxy.beansbackpacks.client.ClientPlaySound;
import com.beansgalaxy.beansbackpacks.entity.Kind;
import com.beansgalaxy.beansbackpacks.entity.PlaySound;
import com.beansgalaxy.beansbackpacks.networking.packages.sSyncBackpackInventory;
import com.beansgalaxy.beansbackpacks.screen.BackSlot;
import com.beansgalaxy.beansbackpacks.screen.BackpackInventory;
import com.mojang.datafixers.util.Pair;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.AbstractRecipeScreenHandler;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerScreenHandler.class)
public abstract class PlayerScreenHandlerMixin extends AbstractRecipeScreenHandler<CraftingInventory> {
    @Shadow @Final private PlayerEntity owner;
    @Shadow @Final private static EquipmentSlot[] EQUIPMENT_SLOT_ORDER;
    @Unique private static final Identifier INPUT = new Identifier("sprites/empty_slot_input");

    public PlayerScreenHandlerMixin(ScreenHandlerType<?> screenHandlerType, int i) {
        super(screenHandlerType, i);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onConstructed(PlayerInventory playerInventory, boolean isServerSide, PlayerEntity player, CallbackInfo ci) {
        BackSlot.SLOT_INDEX = slots.size();
        this.addSlot(new BackSlot(playerInventory, 41, 59, 62, player) {
        });
        BackpackInventory backpackInventory = new BackpackInventory() {
            private final DefaultedList<ItemStack> itemStacks = DefaultedList.of();
            public DefaultedList<ItemStack> getItemStacks() {
                return this.itemStacks;
            }
            public Kind getKind() {
                ItemStack stack = slots.get(BackSlot.SLOT_INDEX).getStack();
                return Kind.fromStack(stack);
            }

            public void playSound(PlaySound sound) {
                if (player.getWorld().isClient)
                    sound.toClient(player);
            }

            public void markDirty() {
                if (player instanceof ServerPlayerEntity serverPlayer)
                    sSyncBackpackInventory.S2C(serverPlayer);
            }

        };
        this.addSlot(new Slot(backpackInventory, 0,59, 45) {

            @Override
            public Pair<Identifier, Identifier> getBackgroundSprite() {
                return Pair.of(BackSlot.BACKPACK_ATLAS, INPUT);
            }

            @Override
            public boolean isEnabled() {
                ItemStack stack = BackSlot.get(player).getStack();
                return Kind.isStorage(stack) && !player.isCreative();
            }

            public boolean canInsert(ItemStack stack) {
                return backpackInventory.canInsert(stack);
            }

        });
    }

    @ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/PlayerScreenHandler;addSlot(Lnet/minecraft/screen/slot/Slot;)Lnet/minecraft/screen/slot/Slot;", ordinal = 2))
    private Slot overrideConstructed(Slot par1) {
        Identifier[] EMPTY_ARMOR_SLOT_TEXTURES = new Identifier[]{ PlayerScreenHandler.EMPTY_BOOTS_SLOT_TEXTURE, PlayerScreenHandler.EMPTY_LEGGINGS_SLOT_TEXTURE, PlayerScreenHandler.EMPTY_CHESTPLATE_SLOT_TEXTURE, PlayerScreenHandler.EMPTY_HELMET_SLOT_TEXTURE};
        int index = par1.getIndex();
        final EquipmentSlot equipmentSlot = EQUIPMENT_SLOT_ORDER[39 - index];

        return new Slot(par1.inventory, index, par1.x, par1.y) {

            @Override
            public void setStack(ItemStack stack) {
                ItemStack previousStack = this.getStack();
                owner.onEquipStack(equipmentSlot, previousStack, stack);
                super.setStack(stack);
            }

            @Override
            public int getMaxItemCount() {
                return 1;
            }

            @Override
            public boolean canInsert(ItemStack stack) {
                return equipmentSlot == MobEntity.getPreferredEquipmentSlot(stack) && !stack.isOf(Items.ELYTRA);
            }

            @Override
            public boolean canTakeItems(PlayerEntity playerEntity) {
                ItemStack itemStack = this.getStack();
                if (!itemStack.isEmpty() && !playerEntity.isCreative() && EnchantmentHelper.hasBindingCurse(itemStack)) {
                    return false;
                }
                return super.canTakeItems(playerEntity);
            }

            @Override
            public Pair<Identifier, Identifier> getBackgroundSprite() {
                return Pair.of(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, EMPTY_ARMOR_SLOT_TEXTURES[equipmentSlot.getEntitySlotId()]);
            }
        };
    }

    @Inject(method = "quickMove", at = @At("HEAD"))
    private void quickMove(PlayerEntity player, int slot, CallbackInfoReturnable<ItemStack> cir) {
        Slot backSlot = this.slots.get(BackSlot.SLOT_INDEX);
        ItemStack stack = this.slots.get(slot).getStack();
        if ((Kind.isWearable(stack)) && !backSlot.hasStack()) {
            backSlot.setStack(stack.copy());
            stack.setCount(0);
        }
    }

    @Override
    public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        if (BackSlot.continueSlotClick(slotIndex, button, actionType, player))
            super.onSlotClick(slotIndex, button, actionType, player);
    }

}
