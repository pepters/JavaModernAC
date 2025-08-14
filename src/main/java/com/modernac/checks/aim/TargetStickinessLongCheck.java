package com.modernac.checks.aim;

import com.modernac.ModernACPlugin;
import com.modernac.engine.DetectionResult;
import com.modernac.engine.Window;
import com.modernac.player.PlayerData;
import com.modernac.player.RotationData;
import java.util.ArrayDeque;
import java.util.Deque;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/** Detects aim stickiness to a moving target over long window. */
public class TargetStickinessLongCheck extends AimCheck {
  private static final String FAMILY = "AIM/Stickiness";

  private static final int MIN = 80;
  private static final double YAW_IDLE_MAX = 0.01;
  private static final double RATE_HIGH = 0.60;
  private static final double RATE_CRIT = 0.75;
  private static final int HITS_REQ = 2;
  private static final long COOLDOWN_MS = 1500L;

  private final Deque<Boolean> samples = new ArrayDeque<>();
  private int idleCount;
  private Vector prevDir;
  private int hits;
  private long lastFail;

  public TargetStickinessLongCheck(ModernACPlugin plugin, PlayerData data) {
    super(plugin, data, "TargetStickiness", false);
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
    int ping = data.getCachedPing();
    double[] tpsArr = Bukkit.getTPS();
    double tps = tpsArr.length > 0 && Double.isFinite(tpsArr[0]) ? tpsArr[0] : 20.0;
    if (ping <= 0 || ping > 180 || tps < 18.0) {
      return;
    }
    Player player = Bukkit.getPlayer(data.getUuid());
    Entity tgt = data.getLastTarget();
    if (player == null || tgt == null) {
      return;
    }
    Vector dir = tgt.getLocation().toVector().subtract(player.getLocation().toVector());
    if (dir.lengthSquared() == 0) {
      return;
    }
    dir.normalize();
    boolean moved = prevDir != null && prevDir.distanceSquared(dir) > 1e-4;
    prevDir = dir;
    if (!moved) {
      return;
    }
    boolean idle = Math.abs(yaw) <= YAW_IDLE_MAX;
    synchronized (samples) {
      if (samples.size() >= 200) {
        boolean old = samples.pollFirst();
        if (old) idleCount--;
      }
      samples.addLast(idle);
      if (idle) idleCount++;
    }
    evaluate();
  }

  private void evaluate() {
    int size = samples.size();
    if (size < MIN) {
      return;
    }
    double rate = idleCount / (double) size;
    long now = System.currentTimeMillis();
    if (now - lastFail < COOLDOWN_MS) {
      return;
    }
    if (rate >= RATE_CRIT) {
      hits++;
      if (hits >= HITS_REQ) {
        hits = 0;
        lastFail = now;
        fail(new DetectionResult(FAMILY, 1.0, Window.LONG, true, true, true));
      }
    } else if (rate >= RATE_HIGH) {
      hits++;
      if (hits >= HITS_REQ) {
        hits = 0;
        lastFail = now;
        fail(new DetectionResult(FAMILY, 0.9, Window.LONG, true, true, true));
      }
    } else {
      hits = 0;
    }
  }
}
