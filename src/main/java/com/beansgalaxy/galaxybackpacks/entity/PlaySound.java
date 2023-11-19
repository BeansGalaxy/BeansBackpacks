package com.beansgalaxy.galaxybackpacks.entity;

import net.minecraft.entity.Entity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

public enum PlaySound {
    PLACE(SoundEvents.ENTITY_ITEM_FRAME_PLACE),
    EQUIP(SoundEvents.ITEM_ARMOR_EQUIP_ELYTRA),
    DROP(SoundEvents.ENTITY_ITEM_FRAME_BREAK),
    HIT(SoundEvents.ENTITY_ITEM_FRAME_REMOVE_ITEM),
    INSERT(SoundEvents.ITEM_BUNDLE_INSERT),
    TAKE(SoundEvents.ITEM_BUNDLE_REMOVE_ONE);

    private final SoundEvent soundEvent;

    PlaySound(SoundEvent soundEvent) {
        this.soundEvent = soundEvent;
    }

    public SoundEvent get() {
        return soundEvent;
    }

    public void at(Entity entity) {
        entity.playSound(this.soundEvent, 0.8F, 0.8F);
    }
}
