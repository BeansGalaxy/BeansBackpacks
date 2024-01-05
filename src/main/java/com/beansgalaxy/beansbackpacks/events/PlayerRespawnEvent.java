package com.beansgalaxy.beansbackpacks.events;

import com.beansgalaxy.beansbackpacks.screen.BackSlot;
import com.beansgalaxy.beansbackpacks.screen.BackpackInventory;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.server.network.ServerPlayerEntity;

public class PlayerRespawnEvent implements ServerPlayerEvents.AfterRespawn {
      @Override
      public void afterRespawn(ServerPlayerEntity oldPlayer, ServerPlayerEntity newPlayer, boolean alive) {
            if (alive)
            {
                  BackSlot oldSlot = BackSlot.get(oldPlayer);
                  BackSlot newSlot = BackSlot.get(newPlayer);

                  newSlot.setStack(oldSlot.getStack());

                  BackpackInventory oldInv = BackSlot.getInventory(oldPlayer);
                  BackpackInventory newInv = BackSlot.getInventory(newPlayer);

                  newInv.getItemStacks().clear();
                  newInv.getItemStacks().addAll(oldInv.getItemStacks());
            }
      }
}
