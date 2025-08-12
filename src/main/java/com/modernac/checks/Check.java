package com.modernac.checks;

import com.modernac.ModernACPlugin;
import com.modernac.player.PlayerData;

import java.util.UUID;

public abstract class Check {
    protected final PlayerData data;
    protected final ModernACPlugin plugin;
    private final String name;
    private final boolean experimental;

    public Check(ModernACPlugin plugin, PlayerData data, String name, boolean experimental) {
        this.plugin = plugin;
        this.data = data;
        this.name = name;
        this.experimental = experimental;
    }

    public String getName() { return name; }
    public boolean isExperimental() { return experimental; }
    public UUID getUuid() { return data.getUuid(); }

    public int addVl(int amount) { return data.addVl(amount); }

    protected void fail(int vl, boolean punishable) {
        plugin.getDetectionEngine().record(this, vl, punishable, experimental);
    }

    public abstract void handle(Object packet);
}
