package com.beansgalaxy.galaxybackpacks.events;

import com.beansgalaxy.galaxybackpacks.entity.Kind;
import com.beansgalaxy.galaxybackpacks.screen.BackSlot;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRenderEvents;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.item.ItemStack;

public class DisableCapeEvent implements LivingEntityFeatureRenderEvents.AllowCapeRender {
      @Override
      public boolean allowCapeRender(AbstractClientPlayerEntity player) {
            ItemStack backStack = BackSlot.get(player).getStack();
            return !Kind.isWearable(backStack);
      }
}
