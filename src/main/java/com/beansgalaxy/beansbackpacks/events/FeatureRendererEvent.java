package com.beansgalaxy.beansbackpacks.events;

import com.beansgalaxy.beansbackpacks.client.player.BackpackFeatureRenderer;
import com.beansgalaxy.beansbackpacks.client.player.PotFeatureRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.minecraft.client.render.entity.ArmorStandEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;

public class FeatureRendererEvent implements LivingEntityFeatureRendererRegistrationCallback {
      @Override
      public void registerRenderers(EntityType<? extends LivingEntity> entityType, LivingEntityRenderer<?, ?> entityRenderer, RegistrationHelper registrationHelper, EntityRendererFactory.Context context) {
            if (entityRenderer instanceof ArmorStandEntityRenderer armorStandEntityRenderer) {
                  registrationHelper.register(new BackpackFeatureRenderer<>(armorStandEntityRenderer, context.getModelLoader(), context.getModelManager()));
            }
            if (entityRenderer instanceof PlayerEntityRenderer playerEntityRenderer) {
                  registrationHelper.register(new BackpackFeatureRenderer<>(playerEntityRenderer, context.getModelLoader(), context.getModelManager()));
                  registrationHelper.register(new PotFeatureRenderer<>(playerEntityRenderer, context.getModelLoader()));
            }
      }
}
