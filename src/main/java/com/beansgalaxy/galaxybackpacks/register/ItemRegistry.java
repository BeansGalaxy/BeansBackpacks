package com.beansgalaxy.galaxybackpacks.register;

import com.beansgalaxy.galaxybackpacks.Main;
import com.beansgalaxy.galaxybackpacks.entity.Kind;
import com.beansgalaxy.galaxybackpacks.item.BackpackItem;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ItemRegistry {

    public static void registerItems() {
        Main.LOGGER.info("Registering Mod Items for" + Main.MODID);
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(ItemRegistry::addItemsToTab);
    }

    private static void addItemsToTab(FabricItemGroupEntries entries) {
        entries.add(NULL_BACKPACK);
        entries.add(LEATHER_BACKPACK);
        entries.add(WOODEN_BACKPACK);
        entries.add(IRON_BACKPACK);
        entries.add(GOLD_BACKPACK);
        entries.add(NETHERITE_BACKPACK);
    }

    public static final Item NULL_BACKPACK = registerItem("null_backpack", new BackpackItem(Kind.NULL));
    public static final Item LEATHER_BACKPACK = registerItem("backpack", new BackpackItem(Kind.LEATHER));
    public static final Item WOODEN_BACKPACK = registerItem("wood_backpack", new BackpackItem(Kind.WOODEN));
    public static final Item IRON_BACKPACK = registerItem("iron_backpack", new BackpackItem(Kind.IRON));
    public static final Item GOLD_BACKPACK = registerItem("gold_backpack", new BackpackItem(Kind.GOLD));
    public static final Item NETHERITE_BACKPACK = registerItem("netherite_backpack", new BackpackItem(Kind.NETHERITE));

    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, new Identifier(Main.MODID, name), item);
    }

}
