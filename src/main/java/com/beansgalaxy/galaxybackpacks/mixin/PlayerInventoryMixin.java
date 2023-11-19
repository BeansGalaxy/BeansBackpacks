package com.beansgalaxy.galaxybackpacks.mixin;

import com.beansgalaxy.galaxybackpacks.Main;
import com.beansgalaxy.galaxybackpacks.entity.Kind;
import com.beansgalaxy.galaxybackpacks.item.BackpackItem;
import com.beansgalaxy.galaxybackpacks.register.ItemRegistry;
import com.beansgalaxy.galaxybackpacks.screen.BackpackInventory;
import com.google.common.collect.ImmutableList;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(PlayerInventory.class)
public abstract class PlayerInventoryMixin implements Inventory {

    @Unique
    public DefaultedList<ItemStack> back = DefaultedList.ofSize(1, ItemStack.EMPTY);

    @Mutable
    @Final
    @Shadow
    private List<DefaultedList<ItemStack>> combinedInventory;

    @Shadow @Final public PlayerEntity player;

    @Unique private DefaultedList<ItemStack> backpackStack;

    @Inject(method = "<init>*", at = @At("RETURN"))
    private void onConstructed(PlayerEntity playerEntity, CallbackInfo info) {
        this.back = DefaultedList.ofSize(1, ItemStack.EMPTY);
        this.combinedInventory = new ArrayList<>(combinedInventory);
        this.combinedInventory.add(back);
    }

    @Inject(method = "writeNbt", at = @At("TAIL"))
    public void writeMixin(NbtList tag, CallbackInfoReturnable<NbtList> info) {
        if (!this.back.get(0).isEmpty()) {
            NbtCompound compoundTag = new NbtCompound();
            compoundTag.putByte("Slot", (byte) (110));
            ItemStack backItem = this.back.get(0);
            backItem.writeNbt(compoundTag);
            if (Kind.isBackpackItem(backItem)) {
                NbtCompound backTag1 = new NbtCompound();
                Inventories.writeNbt(backTag1, BackpackItem.getInventory(player).getItemStacks());
                compoundTag.put("Contents", backTag1);
            }
            tag.add(compoundTag);
        }
    }

    @Inject(method = "readNbt", at = @At("TAIL"))
    public void readMixin(NbtList tag, CallbackInfo info) {
        this.back.clear();
        for (int i = 0; i < tag.size(); ++i) {
            NbtCompound compoundTag = tag.getCompound(i);
            int slot = compoundTag.getByte("Slot") & 255;
            ItemStack itemStack = ItemStack.fromNbt(compoundTag);
            if (!itemStack.isEmpty()) {
                if (slot == 110) {
                    this.back.set(0, itemStack);
                    if (Kind.isBackpackItem(itemStack))
                        BackpackItem.getInventory(player).readStackNbt(compoundTag.getCompound("Contents"));
                }
            }
        }
    }

    @Inject(method = "size", at = @At("HEAD"), cancellable = true)
    public void sizeMixin(CallbackInfoReturnable<Integer> info) {
        int size = 0;
        for (DefaultedList<ItemStack> list : combinedInventory) {
            size += list.size();
        }
        info.setReturnValue(size);
    }

    @Inject(method = "isEmpty", at = @At("HEAD"), cancellable = true)
    public void isEmptyMixin(CallbackInfoReturnable<Boolean> info) {
        if (!this.back.isEmpty()) {
            info.setReturnValue(false);
        }
    }

    public ItemStack getBackStack(int slot) {
        return this.back.get(slot);
    }
}
