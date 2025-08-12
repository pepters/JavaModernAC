package io.github.retrooper.packetevents;

import io.github.retrooper.packetevents.manager.event.EventManager;
import io.github.retrooper.packetevents.manager.server.ServerVersion;

public class PacketEvents {
    private static final PacketEvents INSTANCE = new PacketEvents();
    private final Settings settings = new Settings();
    private final EventManager eventManager = new EventManager();

    public static PacketEvents get() { return INSTANCE; }
    public Settings getSettings() { return settings; }
    public EventManager getEventManager() { return eventManager; }
    public void init() {}
    public void terminate() {}

    public static class Settings {
        public Settings fallbackServerVersion(ServerVersion version) { return this; }
        public Settings checkForUpdates(boolean check) { return this; }
    }
}
