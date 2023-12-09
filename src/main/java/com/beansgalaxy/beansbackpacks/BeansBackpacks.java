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
import net.minecraft.client.gui.tooltip.TooltipComponent;
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
	public static final com.beansgalaxy.beansbackpacks.BeansGalaxyConfig CONFIG = com.beansgalaxy.beansbackpacks.BeansGalaxyConfig.createAndLoad();

	@Override
	public void onInitialize() {
		LOGGER.info("Begun Galaxy's Backpacks Init");
		NetworkPackages.registerC2SPackets();

		ItemRegistry.registerItems();
		ScreenHandlersRegistry.registerScreenHandlers();

		UseBlockCallback.EVENT.register(new PlaceBackpackEvent());
		EntityElytraEvents.CUSTOM.register(new EnableElytraEvent());
		LOGGER.info("Finished Galaxy's Backpacks Init");
	}

	public static final EntityType<Entity> ENTITY_BACKPACK = Registry.register(
			Registries.ENTITY_TYPE, new Identifier(BeansBackpacks.MODID, "backpack"),
			FabricEntityTypeBuilder.create(SpawnGroup.MISC, BackpackEntity::new).build()
	);
}