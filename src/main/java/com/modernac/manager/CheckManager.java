package com.modernac.manager;

import com.modernac.ModernACPlugin;
import com.modernac.checks.Check;
import com.modernac.checks.aim.AimCheckFactory;
import com.modernac.checks.latency.LatencyCheckFactory;
import com.modernac.checks.misc.MiscCheckFactory;
import com.modernac.checks.signatures.SignatureCheckFactory;
import com.modernac.player.PlayerData;
import com.modernac.player.RotationData;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CheckManager {
  private final ModernACPlugin plugin;
  private final Map<UUID, List<Check>> checks = new ConcurrentHashMap<>();
  private final Map<UUID, PlayerData> players = new ConcurrentHashMap<>();
  private final ExecutorService executor = Executors.newFixedThreadPool(2);

  public CheckManager(ModernACPlugin plugin) {
    this.plugin = plugin;
  }

  public void initPlayer(UUID uuid) {
    PlayerData data = new PlayerData(uuid);
    players.put(uuid, data);
    List<Check> list = new ArrayList<>(AimCheckFactory.build(plugin, data));
    list.addAll(SignatureCheckFactory.build(plugin, data));
    list.addAll(LatencyCheckFactory.build(plugin, data));
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
            check.handle(packet);
          }
        });
  }

  public void shutdown() {
    executor.shutdownNow();
  }
}
