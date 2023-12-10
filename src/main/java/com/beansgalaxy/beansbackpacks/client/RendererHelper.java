package com.beansgalaxy.beansbackpacks.client;

import com.beansgalaxy.beansbackpacks.BeansBackpacks;
import com.beansgalaxy.beansbackpacks.entity.Kind;
import com.beansgalaxy.beansbackpacks.client.screen.BackpackScreen;
import com.google.common.collect.ImmutableMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.SmithingScreen;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;

import java.awt.*;
import java.util.Map;

public interface RendererHelper {
    Identifier TEXTURE = new Identifier(BeansBackpacks.MODID, "textures/entity/backpack/null.png");
    Identifier OVERLAY_LEATHER = new Identifier(BeansBackpacks.MODID, "textures/entity/backpack/leather_overlay.png");
    Map<Kind, Identifier> Identifiers = ImmutableMap.of(
            Kind.NULL, new Identifier(BeansBackpacks.MODID, "textures/entity/backpack/null.png"),
            Kind.LEATHER, new Identifier(BeansBackpacks.MODID, "textures/entity/backpack/leather.png"),
            Kind.IRON, new Identifier(BeansBackpacks.MODID, "textures/entity/backpack/iron.png"),
            Kind.GOLD, new Identifier(BeansBackpacks.MODID, "textures/entity/backpack/gold.png"),
            Kind.NETHERITE, new Identifier(BeansBackpacks.MODID, "textures/entity/backpack/netherite.png"));
    Map<String, Identifier> ButtonIdentifiers = ImmutableMap.of(
            "gold", new Identifier(BeansBackpacks.MODID, "textures/entity/backpack/overlay/gold.png"),
            "amethyst", new Identifier(BeansBackpacks.MODID, "textures/entity/backpack/overlay/amethyst.png"),
            "diamond", new Identifier(BeansBackpacks.MODID, "textures/entity/backpack/overlay/diamond.png"),
            "netherite", new Identifier(BeansBackpacks.MODID, "textures/entity/backpack/overlay/netherite.png"));


    static void weld(ModelPart welded, ModelPart weldTo) {
        welded.pitch = weldTo.pitch;
        welded.yaw = weldTo.yaw;
        welded.roll = weldTo.roll;
        welded.pivotX = weldTo.pivotX;
        welded.pivotY = weldTo.pivotY;
        welded.pivotZ = weldTo.pivotZ;
    }

    static float sneakInter(Entity entity, MatrixStack pose, float sneakInter) {
        float scale = sneakInter / 3f;
        pose.translate(0, (1 / 16f) * scale, (1 / 32f) * scale);
        if (entity.isInSneakingPose())
            sneakInter += sneakInter < 3 ? 1 : 0;
        else {
            sneakInter -= sneakInter > 1 ? 1 : 0;
            sneakInter -= sneakInter > 0 ? 1 : 0;
        }
        return sneakInter;
    }

    static void renderTrim(EntityModel<Entity> model, MatrixStack pose, int light, VertexConsumerProvider mbs, Sprite sprite) {
        VertexConsumer vc = sprite.getTextureSpecificVertexConsumer(mbs.getBuffer(TexturedRenderLayers.getArmorTrims(false)));
        if (inBackpackScreen()) {
            for (int j = 1; j < 4; j++) {
                VertexConsumer vc1 = sprite.getTextureSpecificVertexConsumer(mbs.getBuffer(TexturedRenderLayers.getArmorTrims(false)));
                float scale = (0.0015F * j);
                pose.scale(1 + scale, 1 + scale / 2, 1 + scale);
                pose.translate(0, -scale / 1.1, 0);
                model.render(pose, vc1, light, OverlayTexture.DEFAULT_UV, 1F, 1F, 1F, 1F);
            }
        } else
            model.render(pose, vc, light, OverlayTexture.DEFAULT_UV, 1F, 1F, 1F, 1F);
    }

    static void renderButton(Kind b$kind, Color tint, EntityModel<Entity> model, MatrixStack pose, int light, VertexConsumerProvider mbs) {
        if (b$kind == Kind.LEATHER) {
            VertexConsumer overlayTexture = mbs.getBuffer(RenderLayer.getEntityTranslucent(OVERLAY_LEATHER));
            model.render(pose, overlayTexture, light, OverlayTexture.DEFAULT_UV, 1F, 1F, 1F, 1F);
        }
        Identifier identifier = null;
        switch (b$kind) {
            case IRON, GOLD -> identifier = ButtonIdentifiers.get("diamond");
            case NETHERITE -> identifier = ButtonIdentifiers.get("netherite");
            case LEATHER -> {
                if (isYellow(tint)) identifier = ButtonIdentifiers.get("amethyst");
                else identifier = ButtonIdentifiers.get("gold");
            }
        }
        if (identifier != null) {
            VertexConsumer buttonVc = mbs.getBuffer(RenderLayer.getEntityCutout(identifier));
            model.render(pose, buttonVc, light, OverlayTexture.DEFAULT_UV, 1F, 1F, 1F, 1F);
        }
    }

    private static boolean inBackpackScreen() {
        Screen currentScreen = MinecraftClient.getInstance().currentScreen;
        return currentScreen instanceof BackpackScreen || currentScreen instanceof SmithingScreen;
    }

    private static boolean isYellow(Color tint) {
        int red = tint.getRed();
        int blue = tint.getBlue();
        int green = tint.getGreen();

        // BRIGHTNESS
        if (red + green + blue > 600) return false;
        //DARKNESS
        if (red + green <333) return false;

        float min = Math.min(Math.min(red, green), blue);
        float max = Math.max(Math.max(red, green), blue);

        if (min == max) return false;

        float hue;

        if (max == red)
            hue = (green - blue) / (max - min);
        else if (max == green)
            hue = 2f + (blue - red) / (max - min);
        else
            hue = 4f + (red - green) / (max - min);

        hue = hue * 60;
        if (hue < 0) hue = hue + 360;

        // LOWER TOWARDS RED, HIGHER TOWARDS GREEN
        return 40 < Math.round(hue) && 60 > Math.round(hue);
    }

}
