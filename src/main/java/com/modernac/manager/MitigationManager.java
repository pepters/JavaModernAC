package com.modernac.manager;

import com.modernac.ModernACPlugin;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MitigationManager {
    private final ModernACPlugin plugin;
    private final Map<UUID, Integer> mitigated = new ConcurrentHashMap<>();

    public MitigationManager(ModernACPlugin plugin) {
        this.plugin = plugin;
    }

    public void mitigate(UUID uuid, int level) {
        mitigated.put(uuid, level);
        long duration = 20L * plugin.getConfigManager().getMitigationDurationSeconds();
        Bukkit.getScheduler().runTaskLater(plugin, () -> mitigated.remove(uuid), duration);
        apply(uuid);
    }

    private void apply(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return;
        AttributeInstance attr = player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
        if (attr == null) return;
        double base = attr.getBaseValue();
        double modifier = base * (1 - mitigated.getOrDefault(uuid, 0) / 100.0);
        attr.setBaseValue(modifier);
    }
}
