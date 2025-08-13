package com.modernac.listener;

import com.github.retrooper.packetevents.event.SimplePacketListenerAbstract;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClickWindow;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerPositionAndRotation;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerRotation;
import com.modernac.ModernACPlugin;
import com.modernac.checks.combat.AutoTotemDetection.OffhandSwapEvent;
import com.modernac.manager.CheckManager;
import com.modernac.player.RotationData;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PacketListenerImpl extends SimplePacketListenerAbstract {
  private final ModernACPlugin plugin;
  private final CheckManager manager;
  private final Map<UUID, Float> lastYaw = new ConcurrentHashMap<>();
  private final Map<UUID, Float> lastPitch = new ConcurrentHashMap<>();

  public PacketListenerImpl(ModernACPlugin plugin) {
    this.plugin = plugin;
    this.manager = plugin.getCheckManager();
  }

  @Override
  public void onPacketPlayReceive(PacketPlayReceiveEvent event) {
    UUID uuid = event.getUser().getUUID();
    var type = event.getPacketType();

    if (type == PacketType.Play.Client.PLAYER_ROTATION) {
      var w = new WrapperPlayClientPlayerRotation(event);
      handleRotation(uuid, w.getYaw(), w.getPitch());
      return;
    }
    if (type == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {
      var w = new WrapperPlayClientPlayerPositionAndRotation(event);
      handleRotation(uuid, w.getYaw(), w.getPitch());
      return;
    }
    if (type == PacketType.Play.Client.INTERACT_ENTITY) {
      var w = new WrapperPlayClientInteractEntity(event);
      var action = w.getAction();
      if (action == WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
        manager.handle(uuid, "ATTACK");
      }
      return;
    }
    if (type == PacketType.Play.Client.PLAYER_DIGGING) {
      var w = new WrapperPlayClientPlayerDigging(event);
      if (w.getAction() == DiggingAction.SWAP_ITEM_WITH_OFFHAND) {
        manager.handle(uuid, new OffhandSwapEvent());
      }
      return;
    }
    if (type == PacketType.Play.Client.CLICK_WINDOW) {
      var w = new WrapperPlayClientClickWindow(event);
      if (w.getSlot() == 45) {
        manager.handle(uuid, new OffhandSwapEvent());
      }
      return;
    }
  }

  private void handleRotation(UUID uuid, float yaw, float pitch) {
    float lastYawVal = lastYaw.getOrDefault(uuid, yaw);
    float lastPitchVal = lastPitch.getOrDefault(uuid, pitch);
    double yawChange = Math.abs(yaw - lastYawVal);
    double pitchChange = Math.abs(pitch - lastPitchVal);
    lastYaw.put(uuid, yaw);
    lastPitch.put(uuid, pitch);
    manager.handle(uuid, new RotationData(yawChange, pitchChange));
  }
}
