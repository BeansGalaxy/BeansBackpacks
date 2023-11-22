package com.beansgalaxy.galaxybackpacks.client.player;

import com.beansgalaxy.galaxybackpacks.Client;
import com.beansgalaxy.galaxybackpacks.client.TrimHelper;
import com.beansgalaxy.galaxybackpacks.entity.Kind;
import com.beansgalaxy.galaxybackpacks.item.BackpackItem;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

import java.awt.*;

import static com.beansgalaxy.galaxybackpacks.client.RendererHelper.*;

public class BackpackFeatureRenderer<T extends LivingEntity, M extends EntityModel<T>>
        extends FeatureRenderer<T, M> {

    private final EntityModel<Entity> model;
    private final SpriteAtlasTexture trimAtlas;
    private float sneakInter = 0;

    public BackpackFeatureRenderer(FeatureRendererContext<T, M> context, EntityModelLoader loader, BakedModelManager modelManager) {
        super(context);
        this.model = new BackpackPlayerModel<>(loader.getModelPart(Client.PLAYER_BACKPACK_MODEL));
        this.trimAtlas = modelManager.getAtlas(TexturedRenderLayers.ARMOR_TRIMS_ATLAS_TEXTURE);
    }

    @Override
    public void render(MatrixStack pose, VertexConsumerProvider mbs, int light, T entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
        ItemStack backpackStack;
        if (entity instanceof AbstractClientPlayerEntity player)
            backpackStack = player.playerScreenHandler.slots.get(BackpackItem.SLOT_INDEX).getStack();
        else
            backpackStack = ItemStack.EMPTY; //player.playerScreenHandler.slots.get(BackpackItem.SLOT_INDEX).getStack()
        if (!Kind.isBackpackItem(backpackStack) || backpackStack.isOf(Items.DECORATED_POT))
            return;
        pose.push();

        ModelPart backpack = ((BackpackPlayerModel<?>) this.model).body;
        ModelPart torso = ((PlayerEntityModel<?>) this.getContextModel()).body;
        backpack.pitch = torso.pitch;
        backpack.yaw = torso.yaw;
        backpack.roll = torso.roll;
        backpack.pivotX = torso.pivotX;
        backpack.pivotY = torso.pivotY;
        backpack.pivotZ = torso.pivotZ;
        sneaking(entity, pose);
        Kind b$kind = Kind.fromItem(backpackStack);
        BackpackItem bItem = (BackpackItem) backpackStack.getItem();
        this.model.setAngles(entity, limbAngle, limbDistance, tickDelta, animationProgress, headYaw);
        Color tint = new Color(b$kind == Kind.LEATHER ? bItem.getColor(backpackStack) : 0xFFFFFF);
        Identifier texture = Identifiers.get(b$kind);
        VertexConsumer vc = mbs.getBuffer(this.model.getLayer(texture));
        this.model.render(pose, vc, light, OverlayTexture.DEFAULT_UV, tint.getRed() / 255F, tint.getGreen() / 255F, tint.getBlue() / 255F, 1F);
        renderOverlays(pose, light, mbs, entity, backpackStack, b$kind, tint);
        pose.pop();

    }

    // COULDN'T ATTACH BACKPACK DIRECTLY TO TORSO THUS THIS MATCHES ITS MOVEMENTS WHEN CROUCHING
    public void sneaking(Entity entity, MatrixStack pose) {
        float scale = sneakInter / 3f;
        pose.translate(0, (1 / 16f) * scale, (1 / 32f) * scale);
        if (entity.isSneaking())
            sneakInter += sneakInter < 3 ? 1 : 0;
        else {
            sneakInter -= sneakInter > 1 ? 1 : 0;
            sneakInter -= sneakInter > 0 ? 1 : 0;
        }
    }

    private void renderOverlays(MatrixStack pose, int light, VertexConsumerProvider mbs, Entity entity, ItemStack backpackStack, Kind b$kind, Color tint) {
        NbtCompound nbt = backpackStack.getNbt();
        NbtCompound trim = nbt != null ? backpackStack.getNbt().getCompound("Trim") : null;
        if (b$kind.isTrimmable() && trim != null)
            TrimHelper.getBackpackTrim(entity.getEntityWorld().getRegistryManager(), trim).ifPresent((trim1) ->
                    renderTrim(this.model, pose, light, mbs, this.trimAtlas.getSprite(trim1.backpackTexture(b$kind.getMaterial()))));
        else renderButton(b$kind, tint, model, pose, light, mbs);
    }
}
