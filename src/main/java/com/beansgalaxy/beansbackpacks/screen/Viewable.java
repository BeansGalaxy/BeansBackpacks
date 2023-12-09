package com.beansgalaxy.beansbackpacks.screen;

import com.beansgalaxy.beansbackpacks.networking.server.sSyncViewers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.collection.DefaultedList;

public interface Viewable {

      static boolean yawMatches(float viewerYaw, float ownerYaw, double acceptableYaw) {
            double yaw = Math.abs(viewerYaw - ownerYaw) % 360 - 180;
            return Math.abs(yaw) > 180 - acceptableYaw;
      }

      Entity getOwner();

      DefaultedList<PlayerEntity> getPlayersViewing();

      float getHeadPitch();

      void setHeadPitch(float headPitch);

      byte getViewers();

      void setViewers(byte viewers);

      default void clearViewers() {
            getPlayersViewing().clear();
            setViewers((byte) 0);
      }

      default void addViewer(PlayerEntity viewer) {
            if (getPlayersViewing().stream().noneMatch(viewing -> viewing.equals(viewer))) {}
            getPlayersViewing().add(viewer);
            updateViewers();
      }

      default void removeViewer(PlayerEntity viewer) {
            getPlayersViewing().remove(viewer);
            updateViewers();
      }

      default void updateViewers() {
            DefaultedList<PlayerEntity> playersViewing = getPlayersViewing();
            byte newViewers = (byte) Math.min(playersViewing.size(), Byte.MAX_VALUE);
            setViewers(newViewers);
            if (!getOwner().getWorld().isClient)
                  sSyncViewers.S2C(getOwner(), newViewers);
      }

      default boolean isOpen() {
            return getViewers() > 0;
      }

      default void updateOpen() {
            float newPitch = getHeadPitch();
            boolean isOpen = isOpen();

            float speed = Math.max((-Math.abs(newPitch + .4F) + .6F) / 5, isOpen ? 0 : 0.1F);
            if (isOpen) speed /= -2;
            newPitch += speed;
            if (newPitch > 0) newPitch = 0;
            if (newPitch < -1) newPitch = -1;

            //newPitch = -1f; // HOLDS TOP OPEN FOR TEXTURING
            this.setHeadPitch(newPitch);
      }



}
