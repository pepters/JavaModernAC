package com.modernac.listener;

import com.modernac.ModernACPlugin;
import com.modernac.events.AttackEventData;
import com.modernac.events.TotemPopEventData;
import com.modernac.manager.CheckManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityResurrectEvent;

public class CombatListener implements Listener {
    private final CheckManager manager;

    public CombatListener(ModernACPlugin plugin) {
        this.manager = plugin.getCheckManager();
    }

    @EventHandler
    public void onTotemPop(EntityResurrectEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        manager.handle(player.getUniqueId(), new TotemPopEventData(player.getUniqueId()));
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!(event.getEntity() instanceof Player victim)) return;
        manager.handle(attacker.getUniqueId(), new AttackEventData(attacker.getUniqueId(), victim.getUniqueId()));
    }
}
