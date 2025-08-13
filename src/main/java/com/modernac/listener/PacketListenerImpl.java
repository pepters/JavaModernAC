package com.modernac.listener;

import com.modernac.ModernACPlugin;
import com.modernac.manager.CheckManager;
import com.modernac.player.RotationData;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.SimplePacketListenerAbstract;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerRotation;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerPositionAndRotation;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;

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
    public void onPacketReceive(PacketReceiveEvent event) {
        UUID uuid = event.getUser().getUUID();
        var type = event.getPacketType();

        if (type == PacketType.Play.Client.PLAYER_ROTATION) {
            var w = new WrapperPlayClientPlayerRotation(event);
            handleRotation(uuid, w.getYaw(), w.getPitch());
        } else if (type == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {
            var w = new WrapperPlayClientPlayerPositionAndRotation(event);
            handleRotation(uuid, w.getYaw(), w.getPitch());
        } else if (type == PacketType.Play.Client.INTERACT_ENTITY) {
            var w = new WrapperPlayClientInteractEntity(event);
            if (w.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
                manager.handle(uuid, "ATTACK");
            }
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

