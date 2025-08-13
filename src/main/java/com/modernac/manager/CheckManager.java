package com.modernac.manager;

import com.modernac.ModernACPlugin;
import com.modernac.checks.Check;
import com.modernac.checks.aim.AimCheckFactory;
import com.modernac.checks.combat.CombatCheckFactory;
import com.modernac.checks.latency.LatencyCheckFactory;
import com.modernac.checks.misc.MiscCheckFactory;
import com.modernac.checks.signatures.SignatureCheckFactory;
import com.modernac.logging.DetectionLogger;
import com.modernac.player.PlayerData;
import com.modernac.player.RotationData;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class CheckManager {
  private static final int QUEUE_LIMIT = 256;
  private static final long OVERLOAD_LOG_INTERVAL_MS = 10_000L;

  private final ModernACPlugin plugin;
  private final Map<UUID, List<Check>> checks = new ConcurrentHashMap<>();
  private final Map<UUID, PlayerData> players = new ConcurrentHashMap<>();
  private final ThreadPoolExecutor executor;
  private final AtomicLong lastOverloadLog = new AtomicLong();

  public CheckManager(ModernACPlugin plugin) {
    this.plugin = plugin;
    DetectionLogger logger = plugin.getDetectionLogger();
    ThreadFactory factory =
        new ThreadFactory() {
          private final AtomicInteger idx = new AtomicInteger();

          @Override
          public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "ModernAC-detector-" + idx.incrementAndGet());
            t.setUncaughtExceptionHandler((th, ex) -> logger.error("Detector thread failure", ex));
            t.setDaemon(true);
            return t;
          }
        };
    this.executor =
        new ThreadPoolExecutor(
            2,
            2,
            0L,
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(QUEUE_LIMIT),
            factory,
            (r, e) -> {
              long now = System.currentTimeMillis();
              long last = lastOverloadLog.get();
              if (now - last > OVERLOAD_LOG_INTERVAL_MS
                  && lastOverloadLog.compareAndSet(last, now)) {
                logger.error("Detector queue overloaded, dropping tasks");
              }
            });
  }

  public void initPlayer(UUID uuid) {
    PlayerData data = new PlayerData(uuid);
    players.put(uuid, data);
    List<Check> list = new ArrayList<>(AimCheckFactory.build(plugin, data));
    list.addAll(SignatureCheckFactory.build(plugin, data));
    list.addAll(LatencyCheckFactory.build(plugin, data));
    list.addAll(CombatCheckFactory.build(plugin, data));
    list.addAll(MiscCheckFactory.build(plugin, data));
    checks.put(uuid, list);
  }

  public void removePlayer(UUID uuid) {
    checks.remove(uuid);
    players.remove(uuid);
    plugin.getDetectionEngine().reset(uuid);
  }

  public void handle(UUID uuid, Object packet) {
    PlayerData data = players.get(uuid);
    List<Check> list = checks.get(uuid);
    if (data == null || list == null) {
      return;
    }
    if (packet instanceof RotationData) {
      data.recordRotation((RotationData) packet);
    }
    executor.execute(
        () -> {
          for (Check check : list) {
            try {
              check.handle(packet);
            } catch (Throwable t) {
              plugin.getDetectionLogger().error("Exception in " + check.getName() + ".handle", t);
            }
          }
        });
  }

  public void shutdown() {
    executor.shutdownNow();
  }
}
