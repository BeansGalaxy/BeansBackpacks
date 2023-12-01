package com.beansgalaxy.galaxybackpacks.client.player;

import com.beansgalaxy.galaxybackpacks.Client;
import com.beansgalaxy.galaxybackpacks.client.TrimHelper;
import com.beansgalaxy.galaxybackpacks.entity.Kind;
import com.beansgalaxy.galaxybackpacks.item.BackpackItem;
import com.beansgalaxy.galaxybackpacks.screen.BackSlot;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.ArmorStandEntityModel;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.EulerAngle;

import java.awt.*;

import static com.beansgalaxy.galaxybackpacks.client.RendererHelper.*;

public class BackpackFeatureRenderer<T extends LivingEntity, M extends EntityModel<T>>
        extends FeatureRenderer<T, M> {

    private final EntityModel<Entity> model;
    private final SpriteAtlasTexture trimAtlas;
    private float sneakInter = 0;
    private float armorRotation = 0;
    private static ItemStack armorBackpack = ItemStack.EMPTY;

    public BackpackFeatureRenderer(FeatureRendererContext<T, M> context, EntityModelLoader loader, BakedModelManager modelManager) {
        super(context);
        this.model = new BackpackPlayerModel<>(loader.getModelPart(Client.PLAYER_BACKPACK_MODEL));
        this.trimAtlas = modelManager.getAtlas(TexturedRenderLayers.ARMOR_TRIMS_ATLAS_TEXTURE);
    }

    @Override
    public void render(MatrixStack pose, VertexConsumerProvider mbs, int light, T entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
        ItemStack backpackStack = ItemStack.EMPTY;
        BackpackPlayerModel<?> backpackModel = ((BackpackPlayerModel<?>) this.model);
        ModelPart backpackBody = backpackModel.body;

        if (entity instanceof AbstractClientPlayerEntity player) {
            backpackStack = player.playerScreenHandler.slots.get(BackSlot.SLOT_INDEX).getStack();
            if (!Kind.isBackpack(backpackStack))
                return;

            pose.push();
            ModelPart torso = ((PlayerEntityModel<?>) this.getContextModel()).body;
            weld(backpackBody, torso);
            BackSlot backSlot = BackSlot.get(player);
            backSlot.updateOpen();
            backpackModel.head.pitch = backSlot.getHeadPitch();
            sneakInter = sneakInter(player, pose, sneakInter);
        }

        if (entity instanceof ArmorStandEntity armorStand) {
            backpackStack = armorStand.getEquippedStack(EquipmentSlot.OFFHAND);
            boolean isBackpack = Kind.isBackpack(backpackStack);

            ArmorStandEntityModel standModel = (ArmorStandEntityModel) this.getContextModel();
            rotateArmorStand(armorStand, isBackpack, pose, standModel);
            if (!isBackpack)
                return;

            pose.push();
            ModelPart torso = standModel.body;
            weld(backpackBody, torso);
            backpackModel.head.pitch = 0;
            backpackModel.body.yaw = (float) Math.PI * 3;
            float scale = 2f;
            pose.scale(scale, scale, scale);
            pose.translate(0.175, -0.125, 0);
        }

        if (!Kind.isBackpack(backpackStack))
            return;

        Kind b$kind = Kind.fromStack(backpackStack);
        BackpackItem bItem = (BackpackItem) backpackStack.getItem();
        this.model.setAngles(entity, limbAngle, limbDistance, tickDelta, animationProgress, headYaw);
        Color tint = new Color(b$kind == Kind.LEATHER ? bItem.getColor(backpackStack) : 0xFFFFFF);
        Identifier texture = Identifiers.get(b$kind);
        VertexConsumer vc = mbs.getBuffer(this.model.getLayer(texture));
        backpackModel.mask.xScale = 0.999f;
        backpackModel.mask.zScale = 0.93f;
        backpackModel.mask.pivotZ = 0.4f;
        this.model.render(pose, vc, light, OverlayTexture.DEFAULT_UV, tint.getRed() / 255F, tint.getGreen() / 255F, tint.getBlue() / 255F, 1F);
        renderOverlays(pose, light, mbs, entity, backpackStack, b$kind, tint);
        pose.pop();
    }

    private void rotateArmorStand(ArmorStandEntity armorStand, boolean isBackpack, MatrixStack pose, ArmorStandEntityModel standModel) {
        armorStand.setLeftLegRotation(new EulerAngle(-1, 0, isBackpack ? 180 : -1));
        armorStand.setRightLegRotation(new EulerAngle(1, 0, isBackpack ? 180 : 1));
        armorStand.setLeftArmRotation(new EulerAngle(-10, 0, isBackpack ? 90 : -10));
        armorStand.setRightArmRotation(new EulerAngle(-15, 0, isBackpack ? -90 : 10));
        armorStand.setHeadRotation(new EulerAngle(0, 0, isBackpack ? 180 : 0));
    }

    // COULDN'T ATTACH BACKPACK DIRECTLY TO TORSO THUS THIS MATCHES ITS MOVEMENTS WHEN CROUCHING
    public void sneaking(Entity entity, MatrixStack pose) {
        float scale = sneakInter / 3f;
        pose.translate(0, (1 / 16f) * scale, (1 / 32f) * scale);
        if (entity.isInSneakingPose())
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
