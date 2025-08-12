package com.modernac.listener;

import com.modernac.ModernACPlugin;
import com.modernac.manager.CheckManager;
import com.modernac.player.RotationData;
import io.github.retrooper.packetevents.event.PacketListener;
import io.github.retrooper.packetevents.event.PacketPlayReceiveEvent;
import io.github.retrooper.packetevents.protocol.packettype.PacketType;
import io.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientLook;
import io.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPositionLook;
import io.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientUseEntity;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PacketListenerImpl implements PacketListener {
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
        UUID uuid = event.getPlayer().getUniqueId();
        PacketType type = event.getPacketType();
        if (type == PacketType.Play.Client.LOOK) {
            WrapperPlayClientLook wrapper = new WrapperPlayClientLook(event);
            handleRotation(uuid, wrapper.getYaw(), wrapper.getPitch());
        } else if (type == PacketType.Play.Client.POSITION_LOOK) {
            WrapperPlayClientPositionLook wrapper = new WrapperPlayClientPositionLook(event);
            handleRotation(uuid, wrapper.getYaw(), wrapper.getPitch());
        } else if (type == PacketType.Play.Client.USE_ENTITY) {
            WrapperPlayClientUseEntity wrapper = new WrapperPlayClientUseEntity(event);
            if (wrapper.getAction() == WrapperPlayClientUseEntity.Action.ATTACK) {
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
