package com.beansgalaxy.beansbackpacks.mixin;

import com.beansgalaxy.beansbackpacks.events.PlaceBackpackEvent;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayMixin {

      @Shadow public ServerPlayerEntity player;

      @Redirect(method = "onPlayerInteractItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerInteractionManager;interactItem(Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/ActionResult;"))
      public ActionResult interactItem(ServerPlayerInteractionManager instance, ServerPlayerEntity player, World world, ItemStack stack, Hand hand) {
            ActionResult actionResult = this.player.interactionManager.interactItem(this.player, world, stack, hand);
            PlaceBackpackEvent.cancelCoyoteClick(player, actionResult, true);
            return actionResult;
      }

      @Redirect(method = "onPlayerInteractBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerInteractionManager;interactBlock(Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/Hand;Lnet/minecraft/util/hit/BlockHitResult;)Lnet/minecraft/util/ActionResult;"))
      public ActionResult interactBlock(ServerPlayerInteractionManager instance, ServerPlayerEntity player, World world, ItemStack stack, Hand hand, BlockHitResult hitResult) {
            ActionResult actionResult = this.player.interactionManager.interactBlock(this.player, world, stack, hand, hitResult);
            PlaceBackpackEvent.cancelCoyoteClick(player, actionResult, false);
            return actionResult;
      }
}
