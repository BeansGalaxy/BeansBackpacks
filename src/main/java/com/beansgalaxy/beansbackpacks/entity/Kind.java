package com.beansgalaxy.beansbackpacks.entity;

import com.beansgalaxy.beansbackpacks.BeansBackpacks;
import com.beansgalaxy.beansbackpacks.register.ItemRegistry;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;

import java.util.Objects;

public enum Kind {
    ELYTRA("elytra", null, 0),
    POT("pot", null, 999),
    NETHERITE("netherite", ArmorMaterials.NETHERITE, 12),
    GOLD("gold", ArmorMaterials.GOLD, 9),
    IRON("iron", ArmorMaterials.IRON, 9),
    LEATHER("leather", null, 4),
    NULL("", null, 999);

    private final String kind;
    private final int maxStacks;
    private final ArmorMaterials material;

    Kind(String kind, ArmorMaterials material, int maxStacks) {
        this.kind = kind;
        this.maxStacks = maxStacks;
        this.material = material;
    }

    public Item getItem() {
        Item item = ItemRegistry.NULL_BACKPACK.asItem();
        switch (this) {
            case ELYTRA -> item = Items.ELYTRA.asItem();
            case POT -> item = Items.DECORATED_POT.asItem();
            case NETHERITE -> item = ItemRegistry.NETHERITE_BACKPACK.asItem();
            case GOLD -> item = ItemRegistry.GOLD_BACKPACK.asItem();
            case IRON -> item = ItemRegistry.IRON_BACKPACK.asItem();
            case LEATHER -> item = ItemRegistry.LEATHER_BACKPACK.asItem();
        }
        return item;
    }

    public String getString() {
        return kind;
    }

    public boolean isTrimmable() {
        return getMaterial() != null;
    }

    public ArmorMaterials getMaterial() {
        return material;
    }

    public static boolean isWearable(ItemStack stack) {
        return fromStack(stack) != null;
    }

    public static boolean isStorage(ItemStack stack) {
        return getMaxStacks(stack) > 1;
    }

    public static boolean isBackpack(ItemStack stack) {
        Kind kind = fromStack(stack);
        return kind != null && kind != POT && kind != ELYTRA;
    }

    public boolean isOf(Kind kind) {
        return this == kind;
    }

    public boolean isOf(ItemStack stack) {
        Kind kind = Kind.fromStack(stack);
        return this == kind;
    }

    public static Kind fromStack(ItemStack stack) {
        for (Kind kind : Kind.values())
            if (Objects.equals(stack.getItem(), kind.getItem()))
                return kind;
        return null;
    }

    public static ItemStack toStack(BackpackEntity backpack) {
        ItemStack stack = NULL.getItem().getDefaultStack();
        for(Kind kind : Kind.values())
            if (Objects.equals(backpack.getKind(), kind))
                stack = kind.getItem().getDefaultStack();
        int color = backpack.getColor();
        if (color != BackpackEntity.DEFAULT_BACKPACK_COLOR)
            stack.getOrCreateSubNbt("display").putInt("color", color);
        NbtCompound trim = backpack.getTrim();
        if (trim != null)
            stack.setSubNbt("Trim", trim);
        return stack;
    }



    public static Kind fromString(String string) {
        for(Kind kind : Kind.values())
            if (Objects.equals(string, kind.kind))
                return kind;
        return NULL;
    }

    public int getMaxStacks() {
        return maxStacks;
    }

    public static int getMaxStacks(ItemStack stack) {
        Kind kind = Kind.fromStack(stack);
        if (kind != null)
            return kind.getMaxStacks();
        return 0;
    }
}
