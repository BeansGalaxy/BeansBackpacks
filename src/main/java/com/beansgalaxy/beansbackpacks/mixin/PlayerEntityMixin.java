package com.beansgalaxy.beansbackpacks.mixin;

import com.beansgalaxy.beansbackpacks.entity.Backpack;
import com.beansgalaxy.beansbackpacks.screen.BackSlot;
import com.beansgalaxy.beansbackpacks.screen.BackpackInventory;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    public ActionResult interact(PlayerEntity player, Hand hand) {
        return BackSlot.openPlayerBackpackMenu(player, (PlayerEntity) (Object) this);
    }

    @Inject(method = "dropInventory", at = @At("HEAD"))
    protected void dropInventory(CallbackInfo ci) {
        if (!this.getWorld().getGameRules().getBoolean(GameRules.KEEP_INVENTORY)) {
            PlayerEntity player = (PlayerEntity) (Object) this;
            BackpackInventory backpackInventory = BackSlot.getInventory(player);
            ItemStack stack = BackSlot.get(player).getStack();
            Backpack.drop(player, stack, backpackInventory.getItemStacks());
            stack.setCount(0);
        }
    }
}
