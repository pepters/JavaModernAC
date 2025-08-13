package com.modernac.engine;

import com.modernac.ModernACPlugin;
import com.modernac.config.ConfigManager;
import com.modernac.messages.MessageManager;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class AlertEngine {
  private final ModernACPlugin plugin;
  private final Map<UUID, List<AlertDetail>> queues = new ConcurrentHashMap<>();
  private final Map<UUID, Long> lastSent = new ConcurrentHashMap<>();
  private final Map<UUID, Set<UUID>> debugSubs = new ConcurrentHashMap<>();
  private int delayMin, delayMax, rateLimit, batchWindow;
  private int taskId = -1;
  private final Random random = new Random();

  public AlertEngine(ModernACPlugin plugin) {
    this.plugin = plugin;
    reload();
  }

  public void reload() {
    ConfigManager cfg = plugin.getConfigManager();
    delayMin = cfg.getAlertDelayMin();
    delayMax = cfg.getAlertDelayMax();
    rateLimit = cfg.getAlertRateLimit();
    batchWindow = cfg.getAlertBatchWindow();
    if (taskId != -1) {
      Bukkit.getScheduler().cancelTask(taskId);
    }
    taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::flush, 10L, 10L);
  }

  public void shutdown() {
    if (taskId != -1) {
      Bukkit.getScheduler().cancelTask(taskId);
    }
    queues.clear();
    lastSent.clear();
    debugSubs.clear();
  }

  public void queueAlert(UUID uuid, AlertDetail detail, boolean soft) {
    long now = System.currentTimeMillis();
    detail.created = now;
    detail.sendAfter =
        now + (delayMin + random.nextInt(Math.max(1, delayMax - delayMin + 1))) * 1000L;
    detail.soft = soft;
    queues.computeIfAbsent(uuid, k -> Collections.synchronizedList(new ArrayList<>())).add(detail);
    sendDebug(uuid, detail);
  }

  private void sendDebug(UUID target, AlertDetail detail) {
    Set<UUID> subs = debugSubs.get(target);
    if (subs == null) return;
    MessageManager mm = plugin.getMessageManager();
    String msg =
        mm.getMessage("alerts.debug_format")
            .replace("{player}", getName(target))
            .replace("{families}", detail.family)
            .replace("{confidence}", Integer.toString((int) Math.round(detail.confidence * 100)))
            .replace("{ping}", Integer.toString(detail.ping))
            .replace("{tps}", String.format(Locale.US, "%.1f", detail.tps));
    msg = ChatColor.translateAlternateColorCodes('&', msg);
    for (UUID sub : subs) {
      Player p = Bukkit.getPlayer(sub);
      if (p != null && p.isOnline()) {
        p.sendMessage(msg);
      }
    }
  }

  public boolean toggleDebug(UUID sender, UUID target) {
    Set<UUID> set = debugSubs.computeIfAbsent(target, k -> ConcurrentHashMap.newKeySet());
    if (set.contains(sender)) {
      set.remove(sender);
      if (set.isEmpty()) debugSubs.remove(target);
      return false;
    } else {
      set.add(sender);
      return true;
    }
  }

  private void flush() {
    long now = System.currentTimeMillis();
    String perm = plugin.getConfigManager().getAlertPermission();
    MessageManager mm = plugin.getMessageManager();
    for (Iterator<Map.Entry<UUID, List<AlertDetail>>> it = queues.entrySet().iterator();
        it.hasNext(); ) {
      Map.Entry<UUID, List<AlertDetail>> entry = it.next();
      UUID playerId = entry.getKey();
      List<AlertDetail> list = entry.getValue();
      synchronized (list) {
        if (list.isEmpty()) {
          it.remove();
          continue;
        }
        AlertDetail first = list.get(0);
        if (now < first.sendAfter) {
          continue;
        }
        long last = lastSent.getOrDefault(playerId, 0L);
        if (now - last < rateLimit * 1000L) {
          continue;
        }
        List<AlertDetail> batch = new ArrayList<>();
        Iterator<AlertDetail> iter = list.iterator();
        while (iter.hasNext()) {
          AlertDetail d = iter.next();
          if (now >= d.sendAfter && now - d.sendAfter <= batchWindow * 1000L) {
            batch.add(d);
            iter.remove();
          }
        }
        if (batch.isEmpty()) {
          continue;
        }
        lastSent.put(playerId, now);
        String families =
            batch.stream().map(a -> a.family).distinct().collect(Collectors.joining(", "));
        String windows =
            batch.stream().map(a -> a.window).distinct().collect(Collectors.joining(", "));
        double conf = batch.stream().mapToDouble(a -> a.confidence).max().orElse(0.0) * 100.0;
        AlertDetail sample = batch.get(batch.size() - 1);
        String msg =
            mm.getMessage("alerts.staff_format")
                .replace("{player}", getName(playerId))
                .replace("{families}", families)
                .replace("{windows}", windows)
                .replace("{confidence}", Integer.toString((int) Math.round(conf)))
                .replace("{ping}", Integer.toString(sample.ping))
                .replace("{tps}", String.format(Locale.US, "%.1f", sample.tps));
        msg = ChatColor.translateAlternateColorCodes('&', msg);
        if (sample.soft) {
          msg = "[soft] " + msg;
        }
        plugin
            .getDetectionLogger()
            .trace(
                playerId,
                families,
                "windows="
                    + windows
                    + ", ping="
                    + sample.ping
                    + ", tps="
                    + String.format(Locale.US, "%.1f", sample.tps));
        for (Player staff : Bukkit.getOnlinePlayers()) {
          if (staff.hasPermission(perm)) {
            staff.sendMessage(msg);
          }
        }
      }
    }
  }

  private String getName(UUID uuid) {
    OfflinePlayer op = Bukkit.getOfflinePlayer(uuid);
    return op.getName() != null ? op.getName() : uuid.toString();
  }

  public static class AlertDetail {
    public final String family;
    public final String window;
    public final double confidence;
    public final int ping;
    public final double tps;
    private long created;
    private long sendAfter;
    private boolean soft;

    public AlertDetail(String family, String window, double confidence, int ping, double tps) {
      this.family = family;
      this.window = window;
      this.confidence = confidence;
      this.ping = ping;
      this.tps = tps;
    }
  }
}
