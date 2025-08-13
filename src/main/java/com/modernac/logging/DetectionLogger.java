package com.modernac.logging;

import com.modernac.ModernACPlugin;
import com.modernac.config.ConfigManager;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.bukkit.Bukkit;

/** Centralized logging for detections and alerts. */
public class DetectionLogger {
  private final ModernACPlugin plugin;
  private final File traceFile;
  private final File alertFile;
  private final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  private final ExecutorService executor;
  private final Map<UUID, RateLimiter> limits = new ConcurrentHashMap<>();
  private final Set<UUID> debugPlayers = ConcurrentHashMap.newKeySet();

  private boolean traceEnabled;
  private boolean traceToFile;
  private int traceSample;
  private Set<String> traceOnly;
  private boolean alertsToConsole;
  private boolean alertsToFile;

  public DetectionLogger(ModernACPlugin plugin) {
    this.plugin = plugin;
    File folder = new File(plugin.getDataFolder(), "logs");
    folder.mkdirs();
    this.traceFile = new File(folder, "detections.log");
    this.alertFile = new File(folder, "alerts.log");
    this.executor =
        new ThreadPoolExecutor(
            1,
            1,
            0L,
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(1024),
            r -> {
              Thread t = new Thread(r, "ModernAC-Logger");
              t.setDaemon(true);
              return t;
            },
            new ThreadPoolExecutor.DiscardPolicy());
    reload();
  }

  public void reload() {
    ConfigManager cfg = plugin.getConfigManager();
    traceEnabled = cfg.isTraceEnabled();
    traceToFile = cfg.isTraceToFile();
    traceSample = cfg.getTraceSamplePerSecond();
    traceOnly = new HashSet<>();
    for (String s : cfg.getTraceOnlyPlayers()) {
      traceOnly.add(s.toLowerCase(Locale.ROOT));
    }
    alertsToConsole = cfg.isAlertLogToConsole();
    alertsToFile = cfg.isAlertLogToFile();
  }

  public void shutdown() {
    executor.shutdownNow();
  }

  public void setDebug(UUID uuid, boolean enabled) {
    if (enabled) {
      debugPlayers.add(uuid);
    } else {
      debugPlayers.remove(uuid);
    }
  }

  public boolean isTracing(UUID uuid) {
    if (debugPlayers.contains(uuid)) return true;
    if (!traceEnabled) return false;
    if (traceOnly.isEmpty()) return true;
    String u = uuid.toString().toLowerCase(Locale.ROOT);
    if (traceOnly.contains(u)) return true;
    String name = getName(uuid).toLowerCase(Locale.ROOT);
    return traceOnly.contains(name);
  }

  public void trace(UUID uuid, String detector, String message) {
    boolean debug = debugPlayers.contains(uuid);
    boolean enabled = debug || traceEnabled;
    if (!enabled) return;

    if (!debug && !traceOnly.isEmpty()) {
      String u = uuid.toString().toLowerCase(Locale.ROOT);
      String name = getName(uuid).toLowerCase(Locale.ROOT);
      if (!traceOnly.contains(u) && !traceOnly.contains(name)) {
        return;
      }
    }

    int sample = debug ? 20 : traceSample;
    if (sample > 0) {
      RateLimiter rl = limits.computeIfAbsent(uuid, k -> new RateLimiter());
      if (!rl.tryAcquire(sample)) {
        return;
      }
    }

    String line = format.format(new Date()) + " [" + getName(uuid) + "] " + detector + " - " + message;

    if (debug || traceToFile) {
      writeAsync(traceFile, line);
    }
  }

  public void alert(UUID uuid, String detector, String message) {
    String line = format.format(new Date()) + " [" + getName(uuid) + "] " + detector + " - " + message;
    if (alertsToFile) {
      writeAsync(alertFile, line);
    }
    if (alertsToConsole) {
      logConsole(line);
    }
  }

  public void error(String message) {
    error(message, null);
  }

  public void error(String message, Throwable t) {
    plugin.getLogger().log(Level.SEVERE, message, t);
    String line = format.format(new Date()) + " [ERROR] " + message;
    if (alertsToFile) {
      writeAsync(alertFile, line);
    }
  }

  public void logStartup() {
    String players = traceOnly.isEmpty() ? "all" : String.join(",", traceOnly);
    plugin.getLogger().info(("Logging: trace[enabled=" + traceEnabled + ", file=" + traceToFile + ", sample=" + traceSample + ", players=" + players + "] alerts[console=" + alertsToConsole + ", file=" + alertsToFile + "]"));
  }

  private void writeAsync(File file, String line) {
    try {
      executor.execute(
          () -> {
            try (FileWriter fw = new FileWriter(file, true)) {
              fw.write(line + System.lineSeparator());
            } catch (IOException ignored) {
            }
          });
    } catch (RejectedExecutionException ignored) {
    }
  }

  private void logConsole(String line) {
    if (Bukkit.isPrimaryThread()) {
      plugin.getLogger().info(line);
    } else {
      Bukkit.getScheduler().runTask(plugin, () -> plugin.getLogger().info(line));
    }
  }

  private String getName(UUID uuid) {
    try {
      String n = Bukkit.getOfflinePlayer(uuid).getName();
      return n != null ? n : uuid.toString();
    } catch (Exception e) {
      return uuid.toString();
    }
  }

  private static class RateLimiter {
    private long sec;
    private int count;

    synchronized boolean tryAcquire(int maxPerSec) {
      long now = System.currentTimeMillis() / 1000L;
      if (now != sec) {
        sec = now;
        count = 0;
      }
      if (count >= maxPerSec) {
        return false;
      }
      count++;
      return true;
    }
  }
}
