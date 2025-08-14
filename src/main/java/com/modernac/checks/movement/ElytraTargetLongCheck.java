package com.modernac.checks.movement;

import com.modernac.ModernACPlugin;
import com.modernac.checks.Check;
import com.modernac.engine.DetectionResult;
import com.modernac.engine.Window;
import com.modernac.player.PlayerData;
import com.modernac.player.RotationData;
import com.modernac.util.MathUtil;
import java.util.ArrayDeque;
import java.util.Deque;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/** Experimental Elytra targeting helper detection. */
public class ElytraTargetLongCheck extends Check {
  private static final String FAMILY = "ElytraTarget-LONG";

  private static final int MIN = 100;
  private static final int M = 3;
  private static final long COOLDOWN = 3000L;

  private final Deque<Double> yawWindow = new ArrayDeque<>();
  private Vector prevDir;
  private int streak;
  private long lastFail;

  public ElytraTargetLongCheck(ModernACPlugin plugin, PlayerData data) {
    super(plugin, data, FAMILY, true);
  }

  @Override
  public void handle(Object packet) {
    if (!(packet instanceof RotationData)) {
      return;
    }
    RotationData rot = (RotationData) packet;
    double yaw = rot.getYawChange();
    if (!Double.isFinite(yaw)) {
      return;
    }
    Player player = Bukkit.getPlayer(data.getUuid());
    if (player == null || !player.isGliding()) {
      return;
    }
    int ping = data.getCachedPing();
    double[] tpsArr = Bukkit.getTPS();
    double tps = tpsArr.length > 0 && Double.isFinite(tpsArr[0]) ? tpsArr[0] : 20.0;
    if (ping <= 0 || ping > 180 || tps < 18.0) {
      return;
    }
    if (!data.inRecentPvp(500)) {
      return;
    }
    synchronized (yawWindow) {
      if (yawWindow.size() >= 256) {
        yawWindow.pollFirst();
      }
      yawWindow.addLast(yaw);
    }
    double[] arr = MathUtil.snapshotNonNull(yawWindow);
    if (arr.length < MIN) {
      return;
    }
    Entity tgt = data.getLastTarget();
    Vector dir = null;
    if (tgt instanceof Player) {
      dir = tgt.getLocation().toVector().subtract(player.getLocation().toVector());
      if (dir.lengthSquared() > 0) {
        dir.normalize();
      } else {
        dir = null;
      }
    } else {
      return;
    }
    boolean magnet = false;
    if (dir != null && prevDir != null && prevDir.distanceSquared(dir) > 1e-3 && Math.abs(yaw) < 0.01) {
      magnet = true;
    }
    prevDir = dir;
    double std = stddev(arr);
    boolean autopilot = std < 0.01;
    boolean flag = magnet || autopilot;
    long now = System.currentTimeMillis();
    if (flag) {
      streak++;
      if (streak >= M && now - lastFail >= COOLDOWN) {
        streak = 0;
        lastFail = now;
        fail(new DetectionResult(FAMILY, 1.0, Window.LONG, true, true, true));
      }
    } else {
      streak = 0;
    }
  }

  private static double stddev(double[] a) {
    int n = a.length;
    double sum = 0.0;
    for (double v : a) sum += v;
    double mean = sum / n;
    double var = 0.0;
    for (double v : a) {
      double d = v - mean;
      var += d * d;
    }
    return Math.sqrt(var / n);
  }
}
