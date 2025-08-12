package com.modernac;

import com.modernac.config.ConfigManager;
import com.modernac.logging.DebugLogger;
import com.modernac.manager.CheckManager;
import com.modernac.manager.MitigationManager;
import com.modernac.manager.AlertManager;
import com.modernac.manager.PunishmentManager;
import com.modernac.messages.MessageManager;
import com.modernac.listener.PlayerListener;
import com.modernac.listener.PacketListenerImpl;
import io.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.manager.server.ServerVersion;
import org.bukkit.plugin.java.JavaPlugin;

public class ModernACPlugin extends JavaPlugin {

    private ConfigManager configManager;
    private MessageManager messageManager;
    private DebugLogger debugLogger;

    private CheckManager checkManager;
    private AlertManager alertManager;
    private PunishmentManager punishmentManager;
    private MitigationManager mitigationManager;
    private PacketListenerImpl packetListener;

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
        this.alertManager = new AlertManager(this);
        this.punishmentManager = new PunishmentManager(this);
        this.mitigationManager = new MitigationManager(this);

        PacketEvents.get().init();
        this.packetListener = new PacketListenerImpl(this);
        PacketEvents.get().getEventManager().registerListener(packetListener);

        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        getLogger().info("ModernAC enabled.");
    }

    @Override
    public void onDisable() {
        if (checkManager != null) {
            checkManager.shutdown();
        }
        PacketEvents.get().terminate();
        getLogger().info("ModernAC disabled.");
    }

    public ConfigManager getConfigManager() { return configManager; }
    public MessageManager getMessageManager() { return messageManager; }
    public DebugLogger getDebugLogger() { return debugLogger; }
    public CheckManager getCheckManager() { return checkManager; }
    public AlertManager getAlertManager() { return alertManager; }
    public PunishmentManager getPunishmentManager() { return punishmentManager; }
    public MitigationManager getMitigationManager() { return mitigationManager; }
}
