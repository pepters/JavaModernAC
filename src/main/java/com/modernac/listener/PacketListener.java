package com.modernac.listener;

import com.modernac.ModernACPlugin;
import com.modernac.manager.CheckManager;
import org.bukkit.entity.Player;

// Placeholder PacketEvents listener
public class PacketListener {
    private final ModernACPlugin plugin;
    private final CheckManager manager;

    public PacketListener(ModernACPlugin plugin) {
        this.plugin = plugin;
        this.manager = plugin.getCheckManager();
    }

    // Example method representing packet receive
    public void onRotationPacket(Player player, Object packet) {
        manager.handle(player.getUniqueId(), packet);
    }
}
