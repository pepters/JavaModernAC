package com.modernac.listener;

import com.modernac.ModernACPlugin;
import com.modernac.checks.combat.AutoTotemDetection.CriticalDamageEvent;
import com.modernac.checks.combat.AutoTotemDetection.TotemPopEvent;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityResurrectEvent;

public class CombatListener implements Listener {
  private final ModernACPlugin plugin;

  public CombatListener(ModernACPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler
  public void onDamage(EntityDamageEvent event) {
    if (!(event.getEntity() instanceof Player)) return;
    Player player = (Player) event.getEntity();
    double finalDamage = event.getFinalDamage();
    if (finalDamage < player.getHealth()) return;
    boolean los = true;
    if (event instanceof EntityDamageByEntityEvent) {
      Entity damager = ((EntityDamageByEntityEvent) event).getDamager();
      if (damager != null) {
        los = player.hasLineOfSight(damager);
      }
    }
    plugin.getCheckManager().handle(player.getUniqueId(), new CriticalDamageEvent(los));
  }

  @EventHandler
  public void onResurrect(EntityResurrectEvent event) {
    if (!(event.getEntity() instanceof Player)) return;
    Player player = (Player) event.getEntity();
    plugin.getCheckManager().handle(player.getUniqueId(), new TotemPopEvent());
  }
}
