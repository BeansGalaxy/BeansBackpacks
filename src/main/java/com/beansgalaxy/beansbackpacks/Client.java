package com.beansgalaxy.beansbackpacks;

import com.beansgalaxy.beansbackpacks.client.entity.BackpackEntityModel;
import com.beansgalaxy.beansbackpacks.client.entity.BackpackEntityRenderer;
import com.beansgalaxy.beansbackpacks.client.player.BackpackPlayerModel;
import com.beansgalaxy.beansbackpacks.client.player.PlayerPotModel;
import com.beansgalaxy.beansbackpacks.item.BackpackItem;
import com.beansgalaxy.beansbackpacks.networking.NetworkPackages;
import com.beansgalaxy.beansbackpacks.register.ItemRegistry;
import com.beansgalaxy.beansbackpacks.register.ScreenHandlersRegistry;
import com.beansgalaxy.beansbackpacks.screen.BackpackScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;

public class Client implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        NetworkPackages.registerS2CPackets();

        ColorProviderRegistry.ITEM.register((stack, layer) ->
                (layer != 1 ? ((BackpackItem) stack.getItem()).getColor(stack) : 16777215), ItemRegistry.LEATHER_BACKPACK.asItem());

        EntityModelLayerRegistry.registerModelLayer(BACKPACK_ENTITY_MODEL, BackpackEntityModel::getTexturedModelData);
        EntityModelLayerRegistry.registerModelLayer(PLAYER_BACKPACK_MODEL, BackpackPlayerModel::getTexturedModelData);
        EntityModelLayerRegistry.registerModelLayer(PLAYER_POT_MODEL, PlayerPotModel::getTexturedModelData);
        EntityRendererRegistry.register(BeansBackpacks.ENTITY_BACKPACK, BackpackEntityRenderer::new);

        HandledScreens.register(ScreenHandlersRegistry.BACKPACK_SCREEN_HANDLER, BackpackScreen::new);
    }

    public static final EntityModelLayer BACKPACK_ENTITY_MODEL =
            new EntityModelLayer(new Identifier(BeansBackpacks.MODID, "backpack_entity"), "main");

    public static final EntityModelLayer PLAYER_BACKPACK_MODEL =
            new EntityModelLayer(new Identifier(BeansBackpacks.MODID, "backpack_player"), "main");

    public static final EntityModelLayer PLAYER_POT_MODEL =
            new EntityModelLayer(new Identifier(BeansBackpacks.MODID, "pot_player"), "main");

}
