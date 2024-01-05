package com.beansgalaxy.beansbackpacks;

import com.beansgalaxy.beansbackpacks.entity.BackpackEntity;
import com.beansgalaxy.beansbackpacks.events.EnableElytraEvent;
import com.beansgalaxy.beansbackpacks.events.PlaceBackpackEvent;
import com.beansgalaxy.beansbackpacks.events.PlayerRespawnEvent;
import com.beansgalaxy.beansbackpacks.networking.NetworkPackages;
import com.beansgalaxy.beansbackpacks.register.ItemRegistry;
import com.beansgalaxy.beansbackpacks.register.ScreenHandlersRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.EntityElytraEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BeansBackpacks implements ModInitializer {
	public static final String MODID = "beansbackpacks";
	public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

	@Override
	public void onInitialize() {
		LOGGER.info("Begun Galaxy's Backpacks Init");
		NetworkPackages.registerC2SPackets();

		ItemRegistry.registerItems();
		ScreenHandlersRegistry.registerScreenHandlers();

		ServerPlayerEvents.AFTER_RESPAWN.register(new PlayerRespawnEvent());
		UseBlockCallback.EVENT.register(new PlaceBackpackEvent());
		EntityElytraEvents.CUSTOM.register(new EnableElytraEvent());
		LOGGER.info("Finished Galaxy's Backpacks Init");
	}

	public static final EntityType<Entity> ENTITY_BACKPACK = Registry.register(
			Registries.ENTITY_TYPE, new Identifier(BeansBackpacks.MODID, "backpack"),
			FabricEntityTypeBuilder.create(SpawnGroup.MISC, BackpackEntity::new).build()
	);
}