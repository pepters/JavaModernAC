package com.modernac;

import com.modernac.config.ConfigManager;
import com.modernac.logging.DebugLogger;
import com.modernac.manager.CheckManager;
import com.modernac.manager.MitigationManager;
import com.modernac.manager.PunishmentManager;
import com.modernac.messages.MessageManager;
import com.modernac.listener.PlayerListener;
import com.modernac.listener.PacketListenerImpl;
import com.modernac.engine.DetectionEngine;
import com.modernac.engine.AlertEngine;
import com.modernac.commands.AcCommand;
import io.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.manager.server.ServerVersion;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ModernACPlugin extends JavaPlugin {

    private ConfigManager configManager;
    private MessageManager messageManager;
    private DebugLogger debugLogger;

    private CheckManager checkManager;
    private AlertEngine alertEngine;
    private PunishmentManager punishmentManager;
    private MitigationManager mitigationManager;
    private PacketListenerImpl packetListener;
    private DetectionEngine detectionEngine;
    private final Map<UUID, Long> exemptions = new ConcurrentHashMap<>();

    @Override
    public void onLoad() {
        PacketEvents.get().getSettings()
                .fallbackServerVersion(ServerVersion.v1_16_5)
                .checkForUpdates(false);
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.configManager = new ConfigManager(this);
        this.messageManager = new MessageManager(this);
        this.debugLogger = new DebugLogger(this);

        this.checkManager = new CheckManager(this);
        this.alertEngine = new AlertEngine(this);
        this.punishmentManager = new PunishmentManager(this);
        this.mitigationManager = new MitigationManager(this);
        this.detectionEngine = new DetectionEngine(this);

        PacketEvents.get().init();
        this.packetListener = new PacketListenerImpl(this);
        PacketEvents.get().getEventManager().registerListener(packetListener);

        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        AcCommand ac = new AcCommand(this);
        getCommand("ac").setExecutor(ac);
        getCommand("ac").setTabCompleter(ac);

        getLogger().info("ModernAC enabled.");
    }

    @Override
    public void onDisable() {
        if (checkManager != null) {
            checkManager.shutdown();
        }
        if (detectionEngine != null) {
            detectionEngine.shutdown();
        }
        if (alertEngine != null) {
            alertEngine.shutdown();
        }
        PacketEvents.get().terminate();
        getLogger().info("ModernAC disabled.");
    }

    public ConfigManager getConfigManager() { return configManager; }
    public MessageManager getMessageManager() { return messageManager; }
    public DebugLogger getDebugLogger() { return debugLogger; }
    public CheckManager getCheckManager() { return checkManager; }
    public AlertEngine getAlertEngine() { return alertEngine; }
    public PunishmentManager getPunishmentManager() { return punishmentManager; }
    public MitigationManager getMitigationManager() { return mitigationManager; }
    public DetectionEngine getDetectionEngine() { return detectionEngine; }

    public void reload() {
        reloadConfig();
        this.configManager = new ConfigManager(this);
        this.messageManager.reload();
        this.alertEngine.reload();
    }

    public void exemptPlayer(UUID uuid, long durationMs) {
        exemptions.put(uuid, System.currentTimeMillis() + durationMs);
    }

    public boolean isExempt(UUID uuid) {
        Long expire = exemptions.get(uuid);
        if (expire == null) return false;
        if (expire < System.currentTimeMillis()) {
            exemptions.remove(uuid);
            return false;
        }
        return true;
    }
}
