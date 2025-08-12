package io.github.retrooper.packetevents.event;

import io.github.retrooper.packetevents.protocol.packettype.PacketType;
import org.bukkit.entity.Player;

public class PacketPlayReceiveEvent {
    private final Player player;
    private final PacketType packetType;

    public PacketPlayReceiveEvent(Player player, PacketType packetType) {
        this.player = player;
        this.packetType = packetType;
    }

    public Player getPlayer() { return player; }
    public PacketType getPacketType() { return packetType; }
}
