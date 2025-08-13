package com.modernac.manager;

import com.modernac.ModernACPlugin;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.configuration.file.YamlConfiguration;

public class ExemptManager {
  private final ModernACPlugin plugin;
  private final Map<UUID, Long> exemptions = new ConcurrentHashMap<>();
  private final File file;

  public ExemptManager(ModernACPlugin plugin) {
    this.plugin = plugin;
    this.file = new File(plugin.getDataFolder(), "exempt.yml");
    load();
  }

  public void exemptPlayer(UUID uuid, long durationMs) {
    exemptions.put(uuid, System.currentTimeMillis() + durationMs);
    save();
  }

  public boolean isExempt(UUID uuid) {
    Long expire = exemptions.get(uuid);
    if (expire == null) return false;
    if (expire < System.currentTimeMillis()) {
      exemptions.remove(uuid);
      save();
      return false;
    }
    return true;
  }

  public long getRemaining(UUID uuid) {
    Long expire = exemptions.get(uuid);
    if (expire == null) return 0L;
    long diff = expire - System.currentTimeMillis();
    if (diff <= 0) {
      exemptions.remove(uuid);
      save();
      return 0L;
    }
    return diff;
  }

  public void load() {
    exemptions.clear();
    if (!file.exists()) return;
    YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
    for (String key : cfg.getKeys(false)) {
      try {
        UUID uuid = UUID.fromString(key);
        long expire = cfg.getLong(key);
        if (expire > System.currentTimeMillis()) {
          exemptions.put(uuid, expire);
        }
      } catch (IllegalArgumentException ignored) {
      }
    }
  }

  public void save() {
    YamlConfiguration cfg = new YamlConfiguration();
    for (Map.Entry<UUID, Long> e : exemptions.entrySet()) {
      if (e.getValue() > System.currentTimeMillis()) {
        cfg.set(e.getKey().toString(), e.getValue());
      }
    }
    try {
      cfg.save(file);
    } catch (IOException ignored) {
    }
  }
}
