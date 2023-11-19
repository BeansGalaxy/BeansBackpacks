package com.beansgalaxy.galaxybackpacks.screen;

import com.beansgalaxy.galaxybackpacks.entity.Kind;
import com.beansgalaxy.galaxybackpacks.item.BackpackItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.collection.DefaultedList;

import java.util.List;

public interface BackpackInventory extends Inventory {
    DefaultedList<ItemStack> getItemStacks();
    Kind getKind();

    default int size() {
        return getItemStacks().size();
    }

    default boolean isEmpty() {
        return getItemStacks().isEmpty();
    }

    default void clear() {
        this.getItemStacks().clear();
    }

    default ItemStack getStack(int slot) {
        return size() > slot ? this.getItemStacks().get(slot) : ItemStack.EMPTY;
    }

    default ItemStack removeStack(int slot, int amount) {
        List<ItemStack> stacks = getItemStacks();
        ItemStack stack = stacks.get(slot).copyWithCount(amount);
        stacks.get(slot).split(amount);
        return stack;
    }

    default ItemStack removeStack(int slot) {
        return size() > slot ? this.getItemStacks().remove(slot) : ItemStack.EMPTY;
    }

    default void setStack(int slot, ItemStack stack) {
        if (!stack.isEmpty())
            if (size() > slot)
                getItemStacks().set(slot, stack);
            else getItemStacks().add(slot, stack);
        else if (size() > slot) getItemStacks().remove(slot);
    }

    default ItemStack returnStack(int slot, ItemStack stack) {
        if (!stack.isEmpty()) {
            return insertStack(stack, stack.getCount());
        } else return removeStack(slot);
    }

    default ItemStack insertStack(ItemStack stack, int amount) {
        int count = Math.min(amount, spaceLeft());
        if (!stack.isEmpty() && count > 0 && canInsert(stack)) {
            this.getItemStacks().add(0, mergeStack(stack.copyWithCount(count)));
            stack.setCount(stack.getCount() - count);
        } // CLAMP AMOUNT TO STOP ITEMS BEING INSERTED TO BACKPACK
        return stack;
    }

    default int spaceLeft() {
        int totalWeight = this.getItemStacks().stream().mapToInt(itemstacks -> weightByStack(itemstacks) * itemstacks.getCount()).sum();
        return (getKind().getMaxStacks() * 64) - totalWeight;
    }

    default int weightByStack(ItemStack stack) {
        return 64 / stack.getMaxCount();
    };


    private ItemStack mergeStack(ItemStack stack) {
        for (int i = 0; i <= getItemStacks().size(); i++) {
            ItemStack lookSlot = getStack(i);
            if (stack.isOf(lookSlot.getItem()) && !stack.isEmpty()) {
                int count = stack.getCount() + lookSlot.getCount();
                int maxCount = stack.getMaxCount();
                if (count > maxCount) {
                    lookSlot.setCount(maxCount);
                    count -= maxCount;
                } else getItemStacks().remove(i);
                stack.setCount(count);
            }
        }
        return stack;
    }

    default void readStackNbt(NbtCompound nbt) {
        NbtList nbtList = nbt.getList("Items", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < nbtList.size(); ++i) {
            NbtCompound nbtCompound = nbtList.getCompound(i);
            this.getItemStacks().add(ItemStack.fromNbt(nbtCompound));
        }
    }

    default boolean canInsert(ItemStack stack) {
        boolean isEmpty = getItemStacks().isEmpty();
        ItemStack stack1 = isEmpty ? ItemStack.EMPTY : getItemStacks().get(0);
        boolean sameStack = !stack.isOf(stack1.getItem());
        boolean isWooden = getKind() == Kind.WOODEN;
        boolean isSpace = spaceLeft() < 1;
        if (!isEmpty && isWooden && sameStack || isSpace)
            return false;
        return true;
    }

    default boolean canPlayerUse(PlayerEntity player) {
        return true;
    }

    default void markDirty() {
    }
}
