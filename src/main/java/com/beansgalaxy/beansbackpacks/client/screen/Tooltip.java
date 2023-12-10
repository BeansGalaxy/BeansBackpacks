package com.beansgalaxy.beansbackpacks.client.screen;

import com.beansgalaxy.beansbackpacks.entity.Kind;
import com.beansgalaxy.beansbackpacks.screen.BackSlot;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.BundleTooltipData;
import net.minecraft.client.item.TooltipData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.collection.DefaultedList;

import java.util.Optional;

public class Tooltip {
      public static Optional<TooltipData> get(ItemStack stack) {
            PlayerEntity player = MinecraftClient.getInstance().player;
            DefaultedList<Slot> slots = MinecraftClient.getInstance().player.playerScreenHandler.slots;
            // IS BACKPACK SLOT LOADED AND PLAYER IS LOOKING IN THE PLAYER INVENTORY
            if (slots.size() <= BackSlot.SLOT_INDEX || player.currentScreenHandler != player.playerScreenHandler)
                  return null;
            ItemStack equippedOnBack = slots.get(BackSlot.SLOT_INDEX).getStack();
            // IS BACKPACK EQUIPPED
            if (!stack.equals(equippedOnBack))
                  return null;
            DefaultedList<ItemStack> defaultedList = DefaultedList.of();
            DefaultedList<ItemStack> backpackList = BackSlot.getInventory(player).getItemStacks();
            backpackList.forEach(itemstack -> defaultedList.add(itemstack.copy()));
            if (!defaultedList.isEmpty()) {
                  defaultedList.remove(0);
                  for (int j = 0; j < defaultedList.size(); j++) {
                        ItemStack itemStack = defaultedList.get(j);
                        int count = defaultedList.stream()
                                    .filter(itemStacks -> itemStacks.isOf(itemStack.getItem()) && !itemStack.equals(itemStacks))
                                    .mapToInt(itemStacks -> itemStacks.copyAndEmpty().getCount()).sum();
                        itemStack.setCount(count + itemStack.getCount());
                        defaultedList.removeIf(ItemStack::isEmpty);
                  }

            }
            int totalWeight = getBundleOccupancy(defaultedList) / Kind.getMaxStacks(equippedOnBack);
            return Optional.of(new BundleTooltipData(defaultedList, totalWeight));
      }

      private static int getItemOccupancy(ItemStack stack) {
            NbtCompound nbtCompound;
            if ((stack.isOf(Items.BEEHIVE) || stack.isOf(Items.BEE_NEST)) && stack.hasNbt() && (nbtCompound = BlockItem.getBlockEntityNbt(stack)) != null && !nbtCompound.getList("Bees", NbtElement.COMPOUND_TYPE).isEmpty()) {
                  return 64;
            }
            return 64 / stack.getMaxCount();
      }

      private static int getBundleOccupancy(DefaultedList<ItemStack> defaultedList) {
            return defaultedList.stream().mapToInt(itemStack -> getItemOccupancy(itemStack) * itemStack.getCount()).sum();
      }
}
