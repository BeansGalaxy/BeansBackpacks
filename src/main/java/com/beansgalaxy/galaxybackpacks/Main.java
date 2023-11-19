package com.beansgalaxy.galaxybackpacks;

import com.beansgalaxy.galaxybackpacks.entity.BackpackEntity;
import com.beansgalaxy.galaxybackpacks.events.UseBlockEvent;
import com.beansgalaxy.galaxybackpacks.networking.NetworkPackages;
import com.beansgalaxy.galaxybackpacks.register.ItemRegistry;
import com.beansgalaxy.galaxybackpacks.register.ScreenHandlersRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main implements ModInitializer {
	public static final String MODID = "galaxybackpacks";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

	@Override
	public void onInitialize() {
		ItemRegistry.registerItems();
		ScreenHandlersRegistry.registerScreenHandlers();
		NetworkPackages.registerC2SPackets();
		UseBlockCallback.EVENT.register(new UseBlockEvent());

		LOGGER.info("Hello Fabric world!");
	}

	public static final EntityType<Entity> ENTITY_BACKPACK = Registry.register(
			Registries.ENTITY_TYPE, new Identifier(Main.MODID, "backpack"),
			FabricEntityTypeBuilder.create(SpawnGroup.MISC, BackpackEntity::new).build()
	);
}