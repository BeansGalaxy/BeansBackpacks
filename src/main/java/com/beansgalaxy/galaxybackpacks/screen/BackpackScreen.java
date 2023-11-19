package com.beansgalaxy.galaxybackpacks.screen;

import com.beansgalaxy.galaxybackpacks.Main;
import com.beansgalaxy.galaxybackpacks.entity.BackpackEntity;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import org.joml.Vector3f;

public class BackpackScreen extends HandledScreen<BackpackScreenHandler> {
    private static final Identifier TEXTURE = new Identifier(Main.MODID, "textures/gui/backpack.png");
    private final BackpackScreenHandler handler;
    private final BackpackEntity mirror;

    public BackpackScreen(BackpackScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.handler = handler;
        this.backgroundHeight = 256;
        this.mirror = this.handler.entity.createMirror();
    }

    @Override
    protected void init() {
        super.init();
        titleY = 1000;
        playerInventoryTitleY = backgroundHeight - 216 + handler.invOffset;
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        if (handler.entity.isRemoved()) this.close();
        renderBackground(ctx, mouseX, mouseY, delta);
        super.render(ctx, mouseX, mouseY, delta);
        drawMouseoverTooltip(ctx, mouseX, mouseY);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int j = this.handler.invOffset + y;
        context.drawTexture(TEXTURE, x, j - 123, 0, 0, 0, backgroundWidth, backgroundHeight, 256, 256);
        drawBackpack(context, width / 2, j, 192, this.mirror, mouseX, mouseY);
    }

    private void drawBackpack(DrawContext context, int x, int y, int scale, BackpackEntity entity, int mouseX, int mouseY) {
        context.getMatrices().push();
        context.enableScissor(x - 80, y - 220, x + 80, y + 36);
        float relX = -((width / 2f) - mouseX);
        float relY = (height / 2f) - mouseY - (height / 2f);
        float h = (float) (Math.atan(relX) * Math.atan(Math.pow(relX, 4) / (width * 100000)));
        float g = Math.max(Math.abs(h), Math.abs(relX / 314));
        int i = relX > 0 ? 1 : -1;
        context.getMatrices().translate(x + 3, y + 49 - mouseY / 12f, 60);
        context.getMatrices().multiply(RotationAxis.POSITIVE_X.rotationDegrees(relY / 14 - 10));
        context.getMatrices().multiply(RotationAxis.POSITIVE_Y.rotation(i * g));
        context.getMatrices().scale(scale, -scale, scale);
        EntityRenderDispatcher entRD = MinecraftClient.getInstance().getEntityRenderDispatcher();
        DiffuseLighting.enableGuiDepthLighting();
        RenderSystem.setShaderLights(new Vector3f(0f, 10f, 0.4f), new Vector3f(0f, -10f, 0.4f));
        RenderSystem.runAsFancy(() ->
            entRD.render(entity, 0D, 0D, 0D, 20, 1F, context.getMatrices(), context.getVertexConsumers(), 0xFF00FF));
        context.draw();
        context.getMatrices().pop();
        context.disableScissor();
        DiffuseLighting.enableGuiDepthLighting();
    }

    @Override
    protected boolean isClickOutsideBounds(double mouseX, double mouseY, int left, int top, int button) {
        boolean inventory = // BOUNDS OF THE INVENTORY
               mouseX < (double)left
            || mouseY < (double)(top + 100)
            || mouseX >= (double)(left + this.backgroundWidth)
            || mouseY >= (double)(top + this.backgroundHeight);
        boolean backpackSlots = // BOUNDS OF BACKPACK SLOTS
               mouseX < (double)(left - 40)
            || mouseY < (double)(top + handler.invOffset - 35)
            || mouseX >= (double)(left + this.backgroundWidth + 40)
            || mouseY >= (double)(top + handler.invOffset + 37);
        boolean backpackRender = // BOUNDS OF BACKPACK RENDER --- INSET 5 FOR EASIER DROPPING
               mouseX < (double)(left + 45)
            || mouseY < (double)(top + 15)
            || mouseX >= (double)(left + this.backgroundWidth - 45)
            || mouseY >= (double)(top + this.backgroundHeight);

        return inventory && backpackSlots && backpackRender;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(mouseX, mouseY, button);
    }

}
