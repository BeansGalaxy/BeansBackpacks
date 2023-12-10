package com.beansgalaxy.beansbackpacks.client;

import com.beansgalaxy.beansbackpacks.entity.PlaySound;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;

public class ClientPlaySound {
      public static void toClient(PlayerEntity player, SoundEvent soundEvent) {
            MinecraftClient.getInstance().getSoundManager().play(
                        new PositionedSoundInstance(soundEvent.getId(), SoundCategory.PLAYERS, 0.7f, player.getWorld().random.nextFloat() * 0.1f + 0.8f,
                                    SoundInstance.createRandom(), false, 0, SoundInstance.AttenuationType.LINEAR, 0.0, 0.0, 0.0, true)
            );
      }

}
