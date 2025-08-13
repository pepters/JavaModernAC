package com.modernac.messages;

import com.modernac.ModernACPlugin;
import java.io.File;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class MessageManager {
  private final ModernACPlugin plugin;
  private FileConfiguration config;

  public MessageManager(ModernACPlugin plugin) {
    this.plugin = plugin;
    reload();
  }

  public void reload() {
    File file = new File(plugin.getDataFolder(), "messages.yml");
    if (!file.exists()) {
      plugin.saveResource("messages.yml", false);
    }
    this.config = YamlConfiguration.loadConfiguration(file);
  }

  public String getMessage(String path) {
    String raw = config.getString(path, path);
    return ChatColor.translateAlternateColorCodes('&', raw);
  }
}
