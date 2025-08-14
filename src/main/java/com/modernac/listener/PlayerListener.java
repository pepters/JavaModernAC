package com.modernac.listener;

import com.modernac.ModernACPlugin;
import com.modernac.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class PlayerListener implements Listener {
  private final ModernACPlugin plugin;

  public PlayerListener(ModernACPlugin plugin) {
    this.plugin = plugin;
    Bukkit.getScheduler()
        .runTaskTimer(
            plugin,
            () -> {
              for (Player p : Bukkit.getOnlinePlayers()) {
                PlayerData data =
                    plugin.getCheckManager().getPlayerData(p.getUniqueId());
                if (data != null) {
                  data.setCachedPing(p.getPing());
                }
              }
            },
            1L,
            1L);
  }

  @EventHandler
  public void onJoin(PlayerJoinEvent event) {
    plugin.getCheckManager().initPlayer(event.getPlayer().getUniqueId());
  }

  @EventHandler
  public void onQuit(PlayerQuitEvent event) {
    Player player = event.getPlayer();
    plugin.getCheckManager().removePlayer(player.getUniqueId());
    plugin.getDetectionEngine().reset(player.getUniqueId());
    plugin.getAlertEngine().clear(player.getUniqueId());
    plugin.getMitigationManager().reset(player.getUniqueId());
  }

  @EventHandler
  public void onDamage(EntityDamageByEntityEvent event) {
    if (!(event.getDamager() instanceof Player)) return;
    Player damager = (Player) event.getDamager();
    PlayerData data = plugin.getCheckManager().getPlayerData(damager.getUniqueId());
    if (data != null) {
      Entity target = event.getEntity();
      data.markHit(target);
    }
  }

  @EventHandler
  public void onInteract(PlayerInteractEntityEvent event) {
    PlayerData data =
        plugin.getCheckManager().getPlayerData(event.getPlayer().getUniqueId());
    if (data != null) {
      data.markHit(event.getRightClicked());
    }
  }

  @EventHandler
  public void onDeath(PlayerDeathEvent event) {
    Player player = event.getEntity();
    plugin.getDetectionEngine().reset(player.getUniqueId());
    plugin.getAlertEngine().clear(player.getUniqueId());
    plugin.getMitigationManager().reset(player.getUniqueId());
  }

  @EventHandler
  public void onRespawn(PlayerRespawnEvent event) {
    Player player = event.getPlayer();
    plugin.getDetectionEngine().reset(player.getUniqueId());
    plugin.getAlertEngine().clear(player.getUniqueId());
    plugin.getMitigationManager().reset(player.getUniqueId());
  }
}
