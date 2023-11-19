package com.beansgalaxy.galaxybackpacks;

import com.beansgalaxy.galaxybackpacks.client.entity.BackpackEntityModel;
import com.beansgalaxy.galaxybackpacks.client.player.BackpackPlayerModel;
import com.beansgalaxy.galaxybackpacks.client.entity.BackpackEntityRenderer;
import com.beansgalaxy.galaxybackpacks.networking.NetworkPackages;
import com.beansgalaxy.galaxybackpacks.register.ItemRegistry;
import com.beansgalaxy.galaxybackpacks.item.BackpackItem;
import com.beansgalaxy.galaxybackpacks.screen.BackpackScreen;
import com.beansgalaxy.galaxybackpacks.register.ScreenHandlersRegistry;
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
        EntityModelLayerRegistry.registerModelLayer(BACKPACK_PLAYER_MODEL, BackpackPlayerModel::getTexturedModelData);
        EntityRendererRegistry.register(Main.ENTITY_BACKPACK, BackpackEntityRenderer::new);

        HandledScreens.register(ScreenHandlersRegistry.BACKPACK_SCREEN_HANDLER, BackpackScreen::new);
    }

    public static final EntityModelLayer BACKPACK_ENTITY_MODEL =
            new EntityModelLayer(new Identifier(Main.MODID, "backpack_entity"), "main");

    public static final EntityModelLayer BACKPACK_PLAYER_MODEL =
            new EntityModelLayer(new Identifier(Main.MODID, "backpack_player"), "main");

}
