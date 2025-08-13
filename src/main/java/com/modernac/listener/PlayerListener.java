package com.modernac.listener;

import com.modernac.ModernACPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {
  private final ModernACPlugin plugin;

  public PlayerListener(ModernACPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler
  public void onJoin(PlayerJoinEvent event) {
    plugin.getCheckManager().initPlayer(event.getPlayer().getUniqueId());
  }

  @EventHandler
  public void onQuit(PlayerQuitEvent event) {
    plugin.getCheckManager().removePlayer(event.getPlayer().getUniqueId());
  }
}
