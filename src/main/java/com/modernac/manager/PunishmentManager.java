package com.modernac.manager;

import com.modernac.ModernACPlugin;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.scheduler.BukkitTask;

/** Handles delayed punishments with tier escalation. */
public class PunishmentManager {
  private final ModernACPlugin plugin;
  private final Map<UUID, BukkitTask> tasks = new ConcurrentHashMap<>();
  private final Map<UUID, PunishmentTier> activeTier = new ConcurrentHashMap<>();
  private final Map<UUID, Long> expires = new ConcurrentHashMap<>();

  public PunishmentManager(ModernACPlugin plugin) {
    this.plugin = plugin;
  }

  public void schedule(UUID uuid, PunishmentTier tier) {
    PunishmentTier current = activeTier.get(uuid);
    if (current != null) {
      if (tier.ordinal() >= current.ordinal()) {
        return; // already scheduled with same or stricter delay
      }
      cancel(uuid); // escalate
    }
    int[] range = plugin.getConfigManager().getTierDelaySeconds(tier);
    int min = range[0];
    int max = range[1];
    int delta = Math.max(1, max - min + 1);
    long delaySeconds = min + ThreadLocalRandom.current().nextInt(delta);
    long runAt = System.currentTimeMillis() + delaySeconds * 1000L;
    long delayTicks = delaySeconds * 20L;
    BukkitTask task =
        Bukkit.getScheduler()
            .runTaskLater(
                plugin,
                () -> {
                  tasks.remove(uuid);
                  activeTier.remove(uuid);
                  expires.remove(uuid);
                  if (plugin.getDetectionEngine().isPunishable(uuid, tier)) {
                    OfflinePlayer op = Bukkit.getOfflinePlayer(uuid);
                    String name = Optional.ofNullable(op.getName()).orElse(uuid.toString());
                    String command =
                        plugin.getConfigManager().getBanCommand().replace("{player}", name);
                    plugin.getDetectionEngine().reset(uuid);
                    plugin.getAlertEngine().clear(uuid);
                    plugin.getMitigationManager().reset(uuid);
                    plugin.exemptPlayer(uuid, 60_000L);
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                  }
                },
                delayTicks);
    tasks.put(uuid, task);
    activeTier.put(uuid, tier);
    expires.put(uuid, runAt);
  }

  public void cancel(UUID uuid) {
    BukkitTask task = tasks.remove(uuid);
    if (task != null) {
      task.cancel();
    }
    activeTier.remove(uuid);
    expires.remove(uuid);
  }

  public void reload() {
    for (BukkitTask task : tasks.values()) {
      task.cancel();
    }
    tasks.clear();
    activeTier.clear();
    expires.clear();
  }

  public long getRemaining(UUID uuid) {
    Long exp = expires.get(uuid);
    if (exp == null) return 0L;
    long diff = exp - System.currentTimeMillis();
    return Math.max(0L, diff);
  }
}
