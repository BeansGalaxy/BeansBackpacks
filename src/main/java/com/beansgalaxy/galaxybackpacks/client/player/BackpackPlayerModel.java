package com.beansgalaxy.galaxybackpacks.client.player;

import net.minecraft.client.model.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;

public class BackpackPlayerModel<T extends Entity>
		extends EntityModel<T> {
	public final ModelPart body;
	public final ModelPart head;
	public final ModelPart mask;

	public BackpackPlayerModel(ModelPart root) {
		this.body = root.getChild("body");
		this.head = root.getChild("body").getChild("head");
		this.mask = root.getChild("body").getChild("mask_r1");
	}

	public static TexturedModelData getTexturedModelData() {
		ModelData modelData = new ModelData();
		ModelPartData modelPartData = modelData.getRoot();
		ModelPartData body = modelPartData.addChild("body", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 12.0F, 0.0F));

		ModelPartData mask_r1 = body.addChild("mask_r1", ModelPartBuilder.create().uv(0, 20).cuboid(-4.0F, -6.0F, 2.0F, 8.0F, 7.0F, 4.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 1.0F, 0.0F, 3.1416F, 3.1416F, 0.0F));

		ModelPartData bottom_r1 = body.addChild("bottom_r1", ModelPartBuilder.create().uv(0, 8).cuboid(-4.0F, 7.0F, -6.0F, 8.0F, 8.0F, 4.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, -6.0F, 0.0F, 0.0F, 3.1416F, 0.0F));

		ModelPartData head = body.addChild("head", ModelPartBuilder.create().uv(0, 1).cuboid(-4.0F, -1.0F, -4.0F, 8.0F, 3.0F, 4.0F, new Dilation(0.0F))
				.uv(26, 0).cuboid(-1.0F, 1.0F, -5.0F, 2.0F, 2.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 1.0F, 2.0F, -1.5708F, 3.1416F, 0.0F));

		ModelPartData tMask1_r1 = head.addChild("tMask1_r1", ModelPartBuilder.create().uv(8, 20).cuboid(-4.0F, 0.0F, -4.0F, 8.0F, 0.0F, 4.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 1.0F, -4.0F, -0.3229F, 3.1416F, 0.0F));

		ModelPartData tMask0_r1 = head.addChild("tMask0_r1", ModelPartBuilder.create().uv(11, 23).cuboid(-4.0F, -1.0F, -4.0F, 8.0F, 0.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 1.0F, -4.0F, 0.0F, 3.1416F, 0.0F));
		return TexturedModelData.of(modelData, 32, 32);
	}
	@Override
	public void setAngles(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
	}
	@Override
	public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha) {
		body.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
	}
}