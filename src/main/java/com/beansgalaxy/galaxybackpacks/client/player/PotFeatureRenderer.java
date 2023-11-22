package com.beansgalaxy.galaxybackpacks.client.player;

import com.beansgalaxy.galaxybackpacks.Client;
import com.beansgalaxy.galaxybackpacks.Main;
import com.beansgalaxy.galaxybackpacks.item.BackpackItem;
import net.minecraft.block.entity.DecoratedPotBlockEntity;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

public class PotFeatureRenderer<T extends LivingEntity, M extends EntityModel<T>>
        extends FeatureRenderer<T, M> {

    public static final Identifier TEXTURE = new Identifier(Main.MODID, "textures/entity/backpack/clay_detail.png");
    private final EntityModel<Entity> model;
    private float sneakInter = 0;

    public PotFeatureRenderer(FeatureRendererContext<T, M> context, EntityModelLoader loader) {
        super(context);
        this.model = new PlayerPotModel<>(loader.getModelPart(Client.PLAYER_POT_MODEL));
    }

    @Override
    public void render(MatrixStack pose, VertexConsumerProvider mbs, int light, T entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
        ItemStack backpackStack;
        if (entity instanceof AbstractClientPlayerEntity player)
            backpackStack = player.playerScreenHandler.slots.get(BackpackItem.SLOT_INDEX).getStack();
        else
            backpackStack = ItemStack.EMPTY;
        if (!backpackStack.isOf(Items.DECORATED_POT))
            return;


        NbtCompound nbt = new NbtCompound();
        if (backpackStack.getNbt() != null)
            nbt = backpackStack.getNbt().getCompound("BlockEntityTag");

        DecoratedPotBlockEntity.Sherds sherds = DecoratedPotBlockEntity.Sherds.fromNbt(nbt);

        pose.push();

        PlayerPotModel<?> potModel = (PlayerPotModel<?>) this.model;
        ModelPart torso = ((PlayerEntityModel<?>) this.getContextModel()).body;
        potModel.weld(torso);
        sneaking(entity, pose);

        this.model.setAngles(entity, limbAngle, limbDistance, tickDelta, animationProgress, headYaw);
        VertexConsumer vc = mbs.getBuffer(this.model.getLayer(TEXTURE));
        potModel.renderBody(pose, vc, light, OverlayTexture.DEFAULT_UV, true);
        potModel.renderDetail(pose, mbs, light, OverlayTexture.DEFAULT_UV, sherds);

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
}
