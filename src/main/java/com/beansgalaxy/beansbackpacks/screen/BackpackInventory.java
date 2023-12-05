package com.beansgalaxy.beansbackpacks.screen;

import com.beansgalaxy.beansbackpacks.entity.Kind;
import com.beansgalaxy.beansbackpacks.entity.PlaySound;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

import java.util.List;

public interface BackpackInventory extends Inventory {
    DefaultedList<ItemStack> getItemStacks();
    Kind getKind();
    void playSound(PlaySound sound);

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
        if (!stack.isEmpty())
            playSound(PlaySound.TAKE);
        return stack;
    }

    default ItemStack removeStack(int slot) {
        ItemStack stack = removeStackSilent(slot);
        if (!stack.isEmpty())
            playSound(PlaySound.TAKE);
        return stack;
    }

    default ItemStack removeStackSilent(int slot) {
        if (size() > slot) {
            ItemStack stack = getItemStacks().get(slot);
            int maxCount = stack.getMaxCount();
            if (stack.getCount() > maxCount) {
                stack.decrement(maxCount);
                return stack.copyWithCount(maxCount);
            }
            return this.getItemStacks().remove(slot);
        }
        return ItemStack.EMPTY;
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
        ItemStack insertedStack = stack.copy();
        if (insertStackSilent(stack, amount) != insertedStack)
            playSound(stack.isEmpty() ? PlaySound.INSERT : PlaySound.TAKE);
        return stack.isEmpty() ? ItemStack.EMPTY : stack;
    }

    default ItemStack insertStackSilent(ItemStack stack, int amount) {
        int count = Math.min(amount, spaceLeft());
        if (!stack.isEmpty() && count > 0 && canInsert(stack)) {
            this.getItemStacks().add(0, mergeStack(stack.copyWithCount(count)));
            stack.setCount(stack.getCount() - count);
        }
        return stack;
    }

    default int spaceLeft() {
        int totalWeight = this.getItemStacks().stream().mapToInt(
                itemStacks -> weightByStack(itemStacks) * itemStacks.getCount()).sum();
        return (getKind().getMaxStacks() * 64) - totalWeight;
    }

    default int weightByStack(ItemStack stack) {
        return 64 / stack.getMaxCount();
    };


    private ItemStack mergeStack(ItemStack stack) {
        for (int i = 0; i <= getItemStacks().size(); i++) {
            ItemStack lookSlot = getStack(i);
            if (!stack.isEmpty() && ItemStack.canCombine(stack, lookSlot)) {
                int count = stack.getCount() + lookSlot.getCount();
                int maxCount = getKind() == Kind.POT ? Integer.MAX_VALUE : stack.getMaxCount();
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

            ItemStack stack = new ItemStack(Registries.ITEM.get(new Identifier(nbtCompound.getString("id"))), nbtCompound.getInt("Count"));
            if (nbtCompound.contains("tag", NbtElement.COMPOUND_TYPE)) {
                stack.setNbt(nbtCompound.getCompound("tag"));
                stack.getItem().postProcessNbt(stack.getNbt());
            }
            if (stack.getItem().isDamageable()) {
                stack.setDamage(stack.getDamage());
            }

            this.getItemStacks().add(stack);
        }
    }

    default NbtCompound writeNbt(NbtCompound nbt, boolean setIfEmpty) {
        NbtList nbtList = new NbtList();
        DefaultedList<ItemStack> stacks = getItemStacks();
        for (int i = 0; i < stacks.size(); ++i) {
            ItemStack itemStack = stacks.get(i);
            if (itemStack.isEmpty()) continue;
            NbtCompound nbtCompound = new NbtCompound();
            nbtCompound.putByte("Slot", (byte)i);

            Identifier identifier = Registries.ITEM.getId(itemStack.getItem());
            nbtCompound.putString("id", identifier == null ? "minecraft:air" : identifier.toString());
            nbtCompound.putInt("Count", itemStack.getCount());
            if (itemStack.getNbt() != null) {
                nbtCompound.put("tag", itemStack.getNbt().copy());
            }

            nbtList.add(nbtCompound);
        }
        if (!nbtList.isEmpty() || setIfEmpty) {
            nbt.put("Items", nbtList);
        }
        return nbt;
    }


    default boolean canInsert(ItemStack stack) {
        boolean isEmpty = getItemStacks().isEmpty();
        ItemStack stack1 = isEmpty ? ItemStack.EMPTY : getItemStacks().get(0);
        boolean sameStack = !stack.isOf(stack1.getItem());
        boolean isPot = getKind() == Kind.POT;
        boolean isFull = spaceLeft() < 1;
        if (!isEmpty && isPot && sameStack || isFull)
            return false;
        return true;
    }

    default void markDirty() {
    }


    @Override
    default boolean canPlayerUse(PlayerEntity player) {
        return false;
    }
}
