package com.beansgalaxy.beansbackpacks;

import com.beansgalaxy.beansbackpacks.entity.BackpackEntity;
import com.beansgalaxy.beansbackpacks.events.*;
import com.beansgalaxy.beansbackpacks.networking.NetworkPackages;
import com.beansgalaxy.beansbackpacks.register.ItemRegistry;
import com.beansgalaxy.beansbackpacks.register.ScreenHandlersRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRenderEvents;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.EntityElytraEvents;
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
	public static final PlaceBackpackEvent placeEvent = new PlaceBackpackEvent();

	@Override
	public void onInitialize() {
		LOGGER.info("Begun Galaxy's Backpacks Init");
		ItemRegistry.registerItems();
		ScreenHandlersRegistry.registerScreenHandlers();
		NetworkPackages.registerC2SPackets();
		UseBlockCallback.EVENT.register(placeEvent);
		//ServerTickEvents.END_WORLD_TICK.register(placeEvent);
		EntityElytraEvents.CUSTOM.register(new EnableElytraEvent());
		ClientEntityEvents.ENTITY_LOAD.register(new JoinClientEvent());
		LivingEntityFeatureRenderEvents.ALLOW_CAPE_RENDER.register(new DisableCapeEvent());
		LivingEntityFeatureRendererRegistrationCallback.EVENT.register(new FeatureRendererEvent());
		LOGGER.info("Finished Galaxy's Backpacks Init");
	}

	public static final EntityType<Entity> ENTITY_BACKPACK = Registry.register(
			Registries.ENTITY_TYPE, new Identifier(BeansBackpacks.MODID, "backpack"),
			FabricEntityTypeBuilder.create(SpawnGroup.MISC, BackpackEntity::new).build()
	);
}