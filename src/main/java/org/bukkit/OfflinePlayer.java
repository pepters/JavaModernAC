package org.bukkit;

import java.util.UUID;

public class OfflinePlayer {
    private final UUID uuid;
    public OfflinePlayer(UUID uuid) { this.uuid = uuid; }
    public String getName() { return uuid.toString(); }
}
