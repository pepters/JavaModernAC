package com.modernac.checks.combat;

import com.modernac.ModernACPlugin;
import com.modernac.checks.aim.AimCheck;
import com.modernac.engine.DetectionResult;
import com.modernac.engine.Window;
import com.modernac.net.LagCompensator;
import com.modernac.player.PlayerData;
import com.modernac.player.RotationData;
import java.util.ArrayDeque;
import java.util.Deque;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

/** Correlates attack clicks with minor yaw adjustments over long window. */
public class ClickAimCorrelationLongCheck extends AimCheck {
  private static final String FAMILY = "AIM/ClickCoupling";

  private static final int MIN = 40;
  private static final double YAW_MAX_BASE = 0.02;
  private static final double RATE_HIGH = 0.70;
  private static final double RATE_CRIT = 0.85;
  private static final int HITS_REQ = 2;
  private static final long COOLDOWN_MS = 2000L;

  private long lastRotTime;
  private double lastYaw;
  private final Deque<Boolean> samples = new ArrayDeque<>();
  private int coupledCount;
  private int hits;
  private long lastFail;

  public ClickAimCorrelationLongCheck(ModernACPlugin plugin, PlayerData data) {
    super(plugin, data, "ClickAimCorrelationLong", false);
  }

  @Override
  public void handle(Object packet) {
    if (packet instanceof RotationData) {
      RotationData rot = (RotationData) packet;
      lastRotTime = System.currentTimeMillis();
      lastYaw = Math.abs(rot.getYawChange());
      return;
    }
    if (packet instanceof String && "ATTACK".equals(packet)) {
      Player player = Bukkit.getPlayer(data.getUuid());
      Entity target = data.getLastTarget();
      if (!(data.inRecentPvp(500)
          && player != null
          && target instanceof Player
          && player.hasLineOfSight(target)
          && player.getLocation().distanceSquared(target.getLocation()) <= 10.24
          && !player.isGliding()
          && !player.isRiptiding()
          && !player.isHandRaised()
          && player.getOpenInventory().getType()
              == org.bukkit.event.inventory.InventoryType.CRAFTING)) {
        return;
      }
      long now = System.currentTimeMillis();
      LagCompensator.LagContext ctx = plugin.getLagCompensator().estimate(data.getUuid());
      boolean coupled =
          (now - lastRotTime) <= ctx.dtRotAimMs && lastYaw <= (YAW_MAX_BASE + ctx.yawRelax);
      synchronized (samples) {
        if (samples.size() >= 100) {
          boolean old = samples.pollFirst();
          if (old) coupledCount--;
        }
        samples.addLast(coupled);
        if (coupled) coupledCount++;
      }
      evaluate(ctx);
    }
  }

  private void evaluate(LagCompensator.LagContext ctx) {
    int size = samples.size();
    int req = ctx.jitterMs > 30.0 ? 60 : MIN;
    if (size < req) {
      return;
    }
    double rate = coupledCount / (double) size;
    long now = System.currentTimeMillis();
    if (now - lastFail < COOLDOWN_MS) {
      return;
    }
    double env = clamp(1.0 - ctx.jitterMs / 120.0, 0.7, 1.0);
    if (rate >= RATE_CRIT) {
      hits++;
      if (hits >= HITS_REQ) {
        hits = 0;
        lastFail = now;
        fail(new DetectionResult(FAMILY, 1.0 * env, Window.LONG, true, true, true));
      }
    } else if (rate >= RATE_HIGH) {
      hits++;
      if (hits >= HITS_REQ) {
        hits = 0;
        lastFail = now;
        fail(new DetectionResult(FAMILY, 0.9 * env, Window.LONG, true, true, true));
      }
    } else {
      hits = 0;
    }
  }

  private static double clamp(double v, double lo, double hi) {
    return Math.max(lo, Math.min(hi, v));
  }
}
