package com.modernac.player;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class PlayerData {
    private final UUID uuid;
    private final AtomicInteger vl = new AtomicInteger();

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUuid() { return uuid; }

    public int addVl(int amount) { return vl.addAndGet(amount); }

    public int getVl() { return vl.get(); }

    public void resetVl() { vl.set(0); }
}
