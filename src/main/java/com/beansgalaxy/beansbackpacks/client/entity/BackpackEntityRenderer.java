package com.beansgalaxy.beansbackpacks.client.entity;

import com.beansgalaxy.beansbackpacks.Client;
import com.beansgalaxy.beansbackpacks.client.TrimHelper;
import com.beansgalaxy.beansbackpacks.entity.Backpack;
import com.beansgalaxy.beansbackpacks.entity.BackpackEntity;
import com.beansgalaxy.beansbackpacks.entity.Kind;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.RotationAxis;

import java.awt.*;

import static com.beansgalaxy.beansbackpacks.client.RendererHelper.*;

public class BackpackEntityRenderer<T extends Entity> extends EntityRenderer<T> {

    private final BackpackEntityModel model;
    private final SpriteAtlasTexture trimAtlas;

    public BackpackEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
        this.model = new BackpackEntityModel(ctx.getPart(Client.BACKPACK_ENTITY_MODEL));
        this.trimAtlas = ctx.getModelManager().getAtlas(TexturedRenderLayers.ARMOR_TRIMS_ATLAS_TEXTURE);
    }

    private float renderWobble(Entity entity, float yaw) {
        if (entity instanceof BackpackEntity backpack) {
            double breakTime = backpack.wobble;
            return (float) (0.5 * breakTime * Math.sin(breakTime / Math.PI * 4));
        }
        return 0;
    }

    public void render(T entity, float yaw, float tickDelta, MatrixStack pose, VertexConsumerProvider mbs, int light) {
        super.render(entity, yaw += renderWobble(entity, yaw), tickDelta, pose, mbs, light);
        pose.push();
        Backpack bEntity = (Backpack) entity;

        bEntity.updateOpen();
        model.head.pitch = bEntity.getHeadPitch();

        if (!bEntity.isMirror())
            renderHitbox(pose, mbs.getBuffer(RenderLayer.getLines()), entity, yaw, light);
        pose.translate(0, -21 / 16f, 0);
        this.model.setAngles(entity, 0F, 0F, 0F, 50F, 0F);
        Kind b$kind = bEntity.getKind();
        Color tint = b$kind == Kind.LEATHER ? new Color(bEntity.getColor()) : new Color(0xFFFFFF);
        Identifier texture = Identifiers.get(b$kind);
        VertexConsumer vc = mbs.getBuffer(this.model.getLayer(texture));
        ModelPart mask = this.model.mask;
        mask.xScale = 0.99f;
        mask.yScale = 1.0005f;
        mask.zScale = 0.94f;
        mask.pivotZ = -0.1f;
        this.model.render(pose, vc, light, OverlayTexture.DEFAULT_UV, tint.getRed() / 255F, tint.getGreen() / 255F, tint.getBlue() / 255F, 1F);
        renderOverlays(pose, light, mbs, tint, bEntity, b$kind);
        pose.pop();
    }

    private static void renderHitbox(MatrixStack pose, VertexConsumer vertices, Entity entity, float yaw, int light) {
        MinecraftClient minecraft = MinecraftClient.getInstance();
        HitResult crosshairTarget = minecraft.crosshairTarget;
        if (crosshairTarget.getType() == HitResult.Type.ENTITY && ((EntityHitResult) crosshairTarget).getEntity() == entity && !minecraft.options.hudHidden) {
            Box box = entity.getBoundingBox().offset(-entity.getX(), -entity.getY(), -entity.getZ());
            float brightness = Math.min(light, 300) / 300f / 2;
            float value = 0.2f * brightness;
            float alpha = 0.8f;
            if (!entity.getHorizontalFacing().getAxis().isHorizontal()) {
                double h = 9D / 16;
                double w = 8D / 32;
                double d = 4D / 32;
                box = new Box(w, 0, d, -w, h, -d);
                box.offset(-entity.getX(), -entity.getY(), -entity.getZ());
                pose.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees(yaw));
                WorldRenderer.drawBox(pose, vertices, box, value, value, value, alpha);
            } else {
                WorldRenderer.drawBox(pose, vertices, box, value, value, value, alpha);
                pose.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees(yaw));
            }
        } else pose.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees(yaw));
    }

    private void renderOverlays(MatrixStack pose, int light, VertexConsumerProvider mbs, Color tint, Backpack bEntity, Kind b$kind) {
        NbtCompound trim = bEntity.getTrim();
        if (b$kind.isTrimmable() && trim != null)
            TrimHelper.getBackpackTrim(bEntity.getEntityWorld().getRegistryManager(), trim).ifPresent((trim1) ->
                renderTrim(this.model, pose, light, mbs, this.trimAtlas.getSprite(trim1.backpackTexture(b$kind.getMaterial()))));
        else renderButton(b$kind, tint, model, pose, light, mbs);
    }

    @Override
    public Identifier getTexture(Entity entity) {
        return TEXTURE;
    }
}
