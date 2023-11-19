package com.beansgalaxy.galaxybackpacks.register;

import com.beansgalaxy.galaxybackpacks.Main;
import com.beansgalaxy.galaxybackpacks.screen.BackpackScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

public class ScreenHandlersRegistry {
    public static final ScreenHandlerType<BackpackScreenHandler> BACKPACK_SCREEN_HANDLER =
            Registry.register(Registries.SCREEN_HANDLER, new Identifier(Main.MODID, "backpack"),
                    new ExtendedScreenHandlerType<>(BackpackScreenHandler::new));

    public static void registerScreenHandlers() {
        Main.LOGGER.info("Registering Screen Handlers for " + Main.MODID);
    }
}
