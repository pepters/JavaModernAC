package com.modernac.listener;

import com.modernac.ModernACPlugin;
import com.modernac.manager.CheckManager;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import org.bukkit.entity.Player;

/**
 * PacketEvents listener that forwards rotation and attack packets to the check manager.
 */
public class PacketListener extends PacketListenerAbstract {
    private final CheckManager manager;

    public PacketListener(ModernACPlugin plugin) {
        this.manager = plugin.getCheckManager();
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        Object obj = event.getPlayer();
        if (!(obj instanceof Player)) {
            return;
        }
        Player player = (Player) obj;
        PacketTypeCommon type = event.getPacketType();
        if (type == PacketType.Play.Client.LOOK
                || type == PacketType.Play.Client.POSITION
                || type == PacketType.Play.Client.POSITION_LOOK
                || type == PacketType.Play.Client.USE_ENTITY
                || type == PacketType.Play.Client.ANIMATION) {
            manager.handle(player.getUniqueId(), event.getPacket());
        }
    }
}
