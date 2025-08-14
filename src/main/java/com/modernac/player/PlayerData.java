package com.modernac.player;

import com.modernac.model.BaselineProfile;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class PlayerData {
  private final UUID uuid;
  private final AtomicInteger vl = new AtomicInteger();
  private final BaselineProfile baseline = new BaselineProfile();
  private volatile long lastPvpHitAt;
  private volatile boolean lastTargetIsPlayer;
  private volatile UUID lastTargetId;
  private volatile int cachedPing = -1;
  private volatile Vector lastTargetUnit;

  public PlayerData(UUID uuid) {
    this.uuid = uuid;
  }

  public UUID getUuid() {
    return uuid;
  }

  public int addVl(int amount) {
    return vl.addAndGet(amount);
  }

  public int getVl() {
    return vl.get();
  }

  public void resetVl() {
    vl.set(0);
  }

  public BaselineProfile getBaseline() {
    return baseline;
  }

  public void recordRotation(RotationData rot) {
    baseline.update(rot.getYawChange(), rot.getPitchChange());
  }

  public void markHit(boolean targetIsPlayer, UUID targetId, Vector unitToTarget) {
    lastPvpHitAt = System.currentTimeMillis();
    lastTargetIsPlayer = targetIsPlayer;
    lastTargetId = targetId;
    lastTargetUnit = unitToTarget;
  }

  public boolean inRecentPvp(long ms) {
    return lastTargetIsPlayer && lastTargetId != null && System.currentTimeMillis() - lastPvpHitAt <= ms;
  }

  public Entity getLastTarget() {
    return lastTargetId != null ? Bukkit.getEntity(lastTargetId) : null;
  }

  public void setCachedPing(int ping) {
    this.cachedPing = ping;
  }

  public int getCachedPing() {
    return cachedPing;
  }

  public Vector getLastTargetUnit() {
    return lastTargetUnit;
  }
}
