package com.beansgalaxy.beansbackpacks.entity;

import com.beansgalaxy.beansbackpacks.BeansBackpacks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public enum PlaySound {
    PLACE(SoundEvents.ENTITY_ITEM_FRAME_PLACE),
    EQUIP(SoundEvents.ITEM_ARMOR_EQUIP_ELYTRA),
    DROP(SoundEvents.ENTITY_ITEM_FRAME_BREAK),
    HIT(SoundEvents.ENTITY_PLAYER_ATTACK_WEAK),
    BREAK(SoundEvents.ENTITY_PLAYER_ATTACK_CRIT),
    INSERT(SoundEvents.ITEM_BUNDLE_INSERT),
    TAKE(SoundEvents.ITEM_BUNDLE_REMOVE_ONE),
    OPEN(SoundEvents.BLOCK_CHEST_OPEN),
    CLOSE(SoundEvents.BLOCK_CHEST_CLOSE);

    private final SoundEvent soundEvent;

    PlaySound(SoundEvent soundEvent) {
        this.soundEvent = soundEvent;
    }

    public SoundEvent get() {
        return soundEvent;
    }

    public void at(Entity entity) {
        this.at(entity, 0.4f);
    }

    public void at(Entity entity, float volume) {
        World world = entity.getWorld();
        if (!world.isClient) {
            world.playSound(null, entity.getBlockPos(), soundEvent, SoundCategory.BLOCKS, volume, 1f);
        }
    }

    public void toClient(PlayerEntity player) {
        if (!player.getWorld().isClient) {
            MinecraftClient.getInstance().getSoundManager().play(
                new PositionedSoundInstance(this.soundEvent.getId(), SoundCategory.PLAYERS, 0.7f, player.getWorld().random.nextFloat() * 0.1f + 0.8f,
                            SoundInstance.createRandom(), false, 0, SoundInstance.AttenuationType.LINEAR, 0.0, 0.0, 0.0, true)
                );
        }
    }

    public static void registerAll() {
        for (PlaySound sound : PlaySound.values()) {
            PlaySound.register(sound.toString().toLowerCase());
        }
    }

    private static SoundEvent register(String name) {
        Identifier id = new Identifier(BeansBackpacks.MODID, name);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }
}
