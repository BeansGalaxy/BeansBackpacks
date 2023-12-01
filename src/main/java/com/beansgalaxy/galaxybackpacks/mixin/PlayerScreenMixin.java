package com.beansgalaxy.galaxybackpacks.mixin;

import com.beansgalaxy.galaxybackpacks.screen.BackSlot;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.CyclingSlotIcon;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public abstract class PlayerScreenMixin extends AbstractInventoryScreen<PlayerScreenHandler> {

      @Unique
      private final CyclingSlotIcon templateSlotIcon = new CyclingSlotIcon(BackSlot.SLOT_INDEX);

      public PlayerScreenMixin(PlayerScreenHandler screenHandler, PlayerInventory playerInventory, Text text) {
            super(screenHandler, playerInventory, text);
      }

      @Inject(method = "handledScreenTick", at = @At("HEAD"))
      public void handledScreenTick(CallbackInfo ci) {
            this.templateSlotIcon.updateTexture(BackSlot.getTextures());
      }

      @Inject(method = "drawBackground", at = @At("TAIL"))
      protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY, CallbackInfo ci) {
            this.templateSlotIcon.render(this.handler, context, delta, this.x, this.y);
      }
}
