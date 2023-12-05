package com.beansgalaxy.beansbackpacks.register;

import com.beansgalaxy.beansbackpacks.BeansBackpacks;
import com.beansgalaxy.beansbackpacks.screen.BackpackScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

public class ScreenHandlersRegistry {
    public static final ScreenHandlerType<BackpackScreenHandler> BACKPACK_SCREEN_HANDLER =
            Registry.register(Registries.SCREEN_HANDLER, new Identifier(BeansBackpacks.MODID, "backpack"),
                    new ExtendedScreenHandlerType<>(BackpackScreenHandler::new));

    public static void registerScreenHandlers() {
        BeansBackpacks.LOGGER.info("Registering Screen Handlers for " + BeansBackpacks.MODID);
    }
}
