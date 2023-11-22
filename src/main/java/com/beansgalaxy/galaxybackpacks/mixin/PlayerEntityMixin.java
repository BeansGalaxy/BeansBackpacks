package com.beansgalaxy.galaxybackpacks.mixin;

import com.beansgalaxy.galaxybackpacks.entity.Backpack;
import com.beansgalaxy.galaxybackpacks.entity.Kind;
import com.beansgalaxy.galaxybackpacks.item.BackpackItem;
import com.beansgalaxy.galaxybackpacks.networking.packages.SlotClickPacket;
import com.beansgalaxy.galaxybackpacks.screen.BackpackInventory;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
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
        return Backpack.openBackpackMenu(player, (PlayerEntity) (Object) this);
    }

    @Inject(method = "dropInventory", at = @At("HEAD"))
    protected void dropInventory(CallbackInfo ci) {
        if (!this.getWorld().getGameRules().getBoolean(GameRules.KEEP_INVENTORY)) {
            PlayerEntity player = (PlayerEntity) (Object) this;
            BackpackInventory backpackInventory = BackpackItem.getInventory(player);
            ItemStack stack = BackpackItem.getSlot(player).getStack();
            SlotClickPacket.dropBackpack(player, stack, backpackInventory.getItemStacks());
            stack.setCount(0);
        }
    }

    }
