package com.beansgalaxy.galaxybackpacks.client.player;

import com.beansgalaxy.galaxybackpacks.Main;
import com.beansgalaxy.galaxybackpacks.entity.Kind;
import com.google.common.collect.ImmutableMap;
import net.minecraft.block.entity.DecoratedPotBlockEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

import java.util.Map;
import java.util.Objects;

public class PlayerPotModel<T extends Entity>
		extends EntityModel<T> {
	public final ModelPart body;
	public final ModelPart head;
	public final ModelPart front;
	public final ModelPart back;
	public final ModelPart left;
	public final ModelPart right;

	public PlayerPotModel(ModelPart root) {
		this.body = root.getChild("bottom");
		this.head = root.getChild("head");
		this.front = root.getChild("north");
		this.back = root.getChild("south");
		this.left = root.getChild("east");
		this.right = root.getChild("west");
	}

	public DefaultedList<ModelPart> getModelParts() {
		DefaultedList<ModelPart> modelParts = DefaultedList.of();

		modelParts.add(body);
		modelParts.add(head);
		modelParts.add(front);
		modelParts.add(back);
		modelParts.add(left);
		modelParts.add(right);

		return modelParts;
	}

	public void weld(ModelPart weldTo) {
		for (int j = 0; j < this.getModelParts().size(); j++) {
			ModelPart modelPart = getModelParts().get(j);

			modelPart.pitch = weldTo.pitch;
			modelPart.yaw = weldTo.yaw;
			modelPart.roll = weldTo.roll;
			modelPart.pivotX = weldTo.pivotX;
			modelPart.pivotY = weldTo.pivotY;
			modelPart.pivotZ = weldTo.pivotZ;
		}
	}

	public static TexturedModelData getTexturedModelData() {
		ModelData modelData = new ModelData();
		ModelPartData modelPartData = modelData.getRoot();
		ModelPartData north = modelPartData.addChild("north", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 12.0F, -5.0F));

		ModelPartData wall_r1 = north.addChild("wall_r1", ModelPartBuilder.create().uv(0, 0).cuboid(-3.0F, -11.0F, -3.0F, 6.0F, 7.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 12.0F, 5.0F, 0.0F, 3.1416F, 0.0F));

		ModelPartData south = modelPartData.addChild("south", ModelPartBuilder.create().uv(0, 0).cuboid(-3.0F, 1.0F, 2.0F, 6.0F, 7.0F, 1.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 12.0F, -5.0F));

		ModelPartData east = modelPartData.addChild("east", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 12.0F, -5.0F));

		ModelPartData wall_r2 = east.addChild("wall_r2", ModelPartBuilder.create().uv(0, 0).cuboid(-3.0F, -11.0F, -3.0F, 6.0F, 7.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 12.0F, 5.0F, 0.0F, -1.5708F, 0.0F));

		ModelPartData west = modelPartData.addChild("west", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 12.0F, -5.0F));

		ModelPartData wall_r3 = west.addChild("wall_r3", ModelPartBuilder.create().uv(0, 0).cuboid(-3.0F, -11.0F, -3.0F, 6.0F, 7.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 12.0F, 5.0F, 0.0F, 1.5708F, 0.0F));

		ModelPartData bottom = modelPartData.addChild("bottom", ModelPartBuilder.create().uv(0, 19).cuboid(-3.0F, 1.0F, 2.0F, 6.0F, 7.0F, 6.0F, new Dilation(0.0F))
				.uv(16, 0).cuboid(-2.0F, 1.0F, 3.0F, 4.0F, 4.0F, 4.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 12.0F, -5.0F));

		ModelPartData head = modelPartData.addChild("head", ModelPartBuilder.create().uv(0, 0).cuboid(-2.0F, 0.0F, 3.0F, 4.0F, 2.0F, 4.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 12.0F, -5.0F));
		return TexturedModelData.of(modelData, 32, 32);
	}
	@Override
	public void setAngles(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
	}
	@Override
	public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha) {
		body.render(matrices, vertexConsumer, light, overlay, 1, 1, 1, 1);
		head.render(matrices, vertexConsumer, light, overlay, 1, 1, 1, 1);
	}


	public void renderBody(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, boolean renderHead) {
		body.render(matrices, vertexConsumer, light, overlay, 1, 1, 1, 1);
		if (renderHead)
			head.render(matrices, vertexConsumer, light, overlay, 1, 1, 1, 1);
	}


	public void renderDetail(MatrixStack matrices, VertexConsumerProvider mbs, int light, int overlay, DecoratedPotBlockEntity.Sherds sherds) {
		VertexConsumer vFront = mbs.getBuffer(getLayer(SherdTexture.toTexture(sherds.front())));
		front.render(matrices, vFront, light, overlay, 1, 1, 1, 1);
		VertexConsumer vLeft = mbs.getBuffer(getLayer(SherdTexture.toTexture(sherds.left())));
		left.render(matrices, vLeft, light, overlay, 1, 1, 1, 1);
		VertexConsumer vRight = mbs.getBuffer(getLayer(SherdTexture.toTexture(sherds.right())));
		right.render(matrices, vRight, light, overlay, 1, 1, 1, 1);
//		VertexConsumer vBack = mbs.getBuffer(getLayer(SherdTexture.toTexture(sherds.back())));
//		back.render(matrices, vBack, light, overlay, 1, 1, 1, 1);
	}

	public enum SherdTexture {
		ANGLER(			Items.ANGLER_POTTERY_SHERD, 		new Identifier(Main.MODID, "textures/entity/backpack/clay/angler.png")),
		ARCHER(			Items.ARCHER_POTTERY_SHERD, 		new Identifier(Main.MODID, "textures/entity/backpack/clay/archer.png")),
		ARMS_UP(		Items.ARMS_UP_POTTERY_SHERD, 		new Identifier(Main.MODID, "textures/entity/backpack/clay/arms_up.png")),
		BLADE(			Items.BLADE_POTTERY_SHERD, 			new Identifier(Main.MODID, "textures/entity/backpack/clay/blade.png")),
		BREWER(			Items.BREWER_POTTERY_SHERD, 		new Identifier(Main.MODID, "textures/entity/backpack/clay/brewer.png")),
		BURN(			Items.BURN_POTTERY_SHERD, 			new Identifier(Main.MODID, "textures/entity/backpack/clay/burn.png")),
		DANGER(			Items.DANGER_POTTERY_SHERD, 		new Identifier(Main.MODID, "textures/entity/backpack/clay/danger.png")),
		EXPLORER(		Items.EXPLORER_POTTERY_SHERD,		new Identifier(Main.MODID, "textures/entity/backpack/clay/explorer.png")),
		FRIEND(			Items.FRIEND_POTTERY_SHERD, 		new Identifier(Main.MODID, "textures/entity/backpack/clay/friend.png")),
		HEART(			Items.HEART_POTTERY_SHERD,			new Identifier(Main.MODID, "textures/entity/backpack/clay/heart.png")),
		HEARTBREAK(		Items.HEARTBREAK_POTTERY_SHERD,		new Identifier(Main.MODID, "textures/entity/backpack/clay/heartbreak.png")),
		HOWL(			Items.HOWL_POTTERY_SHERD, 			new Identifier(Main.MODID, "textures/entity/backpack/clay/howl.png")),
		MINER(			Items.MINER_POTTERY_SHERD, 			new Identifier(Main.MODID, "textures/entity/backpack/clay/miner.png")),
		MOURNER(		Items.MOURNER_POTTERY_SHERD, 		new Identifier(Main.MODID, "textures/entity/backpack/clay/mourner.png")),
		PLENTY(			Items.PLENTY_POTTERY_SHERD, 		new Identifier(Main.MODID, "textures/entity/backpack/clay/plenty.png")),
		PRIZE(			Items.PRIZE_POTTERY_SHERD, 			new Identifier(Main.MODID, "textures/entity/backpack/clay/prize.png")),
		SHEAF(			Items.SHEAF_POTTERY_SHERD, 			new Identifier(Main.MODID, "textures/entity/backpack/clay/sheaf.png")),
		SHELTER(		Items.SHELTER_POTTERY_SHERD, 		new Identifier(Main.MODID, "textures/entity/backpack/clay/shelter.png")),
		SKULL(			Items.SKULL_POTTERY_SHERD, 			new Identifier(Main.MODID, "textures/entity/backpack/clay/skull.png")),
		SNORT(			Items.SNORT_POTTERY_SHERD, 			new Identifier(Main.MODID, "textures/entity/backpack/clay/snort.png"));

		public static final Identifier NONE = new Identifier(Main.MODID, "textures/entity/backpack/clay_none.png");


		private final Item sherdItem;
		private final Identifier texture;

		SherdTexture(Item item, Identifier identifier) {
			sherdItem = item;
			texture = identifier;
		}

		private Item getItem() {
			return this.sherdItem;
		}

		private Identifier getTexture() {
			return this.texture;
		}

		public static Identifier toTexture(Item item) {
			for (SherdTexture sherd : SherdTexture.values())
				if (Objects.equals(item.asItem(), sherd.getItem()))
					return sherd.getTexture();
			return NONE;
		}
	}

}