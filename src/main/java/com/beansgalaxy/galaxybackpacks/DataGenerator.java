package com.beansgalaxy.galaxybackpacks;

import com.beansgalaxy.galaxybackpacks.register.ItemRegistry;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.ItemTags;

import java.util.concurrent.CompletableFuture;

public class DataGenerator implements DataGeneratorEntrypoint {
	@Override public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		fabricDataGenerator.createPack().addProvider(myTagGenerator::new);
    }

	private static class myTagGenerator extends FabricTagProvider.ItemTagProvider {
		public myTagGenerator(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> completableFuture) {
			super(output, completableFuture);
		}

		@Override protected void configure(RegistryWrapper.WrapperLookup arg) {
			this.getOrCreateTagBuilder(ItemTags.TRIMMABLE_ARMOR).add(ItemRegistry.IRON_BACKPACK.asItem()).add(ItemRegistry.GOLD_BACKPACK.asItem());
		}
	}
}
