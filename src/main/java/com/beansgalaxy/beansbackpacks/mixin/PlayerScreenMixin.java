package com.beansgalaxy.beansbackpacks.mixin;

import com.beansgalaxy.beansbackpacks.screen.BackSlot;
import net.minecraft.advancement.AdvancementManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.CyclingSlotIcon;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(InventoryScreen.class)
public abstract class PlayerScreenMixin extends AbstractInventoryScreen<PlayerScreenHandler> {
      @Unique
      private static final Identifier SLOT_BACKPACK = new Identifier("sprites/empty_slot_backpack");
      @Unique
      private static final Identifier SLOT_ELYTRA = new Identifier("sprites/empty_slot_elytra");

      @Unique
      private final CyclingSlotIcon templateSlotIcon = new CyclingSlotIcon(BackSlot.SLOT_INDEX);

      public PlayerScreenMixin(PlayerScreenHandler screenHandler, PlayerInventory playerInventory, Text text) {
            super(screenHandler, playerInventory, text);
      }

      @Inject(method = "handledScreenTick", at = @At("HEAD"))
      public void handledScreenTick(CallbackInfo ci) {
            this.templateSlotIcon.updateTexture(getTextures());
      }

      @Inject(method = "drawBackground", at = @At("TAIL"))
      protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY, CallbackInfo ci) {
            this.templateSlotIcon.render(this.handler, context, delta, this.x, this.y);
      }

      @Unique
      private static List<Identifier> getTextures() {
            AdvancementManager manager = MinecraftClient.getInstance().getNetworkHandler().getAdvancementHandler().getManager();
            boolean hasEndGoal = manager.get(Identifier.tryParse("end/root")) != null;
            if (hasEndGoal)
                  return List.of(SLOT_ELYTRA, SLOT_BACKPACK);

            return List.of(SLOT_BACKPACK);
      }

}
