package com.modernac;

import com.github.retrooper.packetevents.PacketEvents;
import com.modernac.commands.AcCommand;
import com.modernac.config.ConfigManager;
import com.modernac.engine.AlertEngine;
import com.modernac.engine.DetectionEngine;
import com.modernac.listener.PacketListenerImpl;
import com.modernac.listener.PlayerListener;
import com.modernac.logging.DebugLogger;
import com.modernac.manager.CheckManager;
import com.modernac.manager.ExemptManager;
import com.modernac.manager.MitigationManager;
import com.modernac.manager.PunishmentManager;
import com.modernac.messages.MessageManager;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import java.util.UUID;
import org.bukkit.plugin.java.JavaPlugin;

public class ModernACPlugin extends JavaPlugin {

  private ConfigManager configManager;
  private MessageManager messageManager;
  private DebugLogger debugLogger;

  private CheckManager checkManager;
  private AlertEngine alertEngine;
  private PunishmentManager punishmentManager;
  private MitigationManager mitigationManager;
  private DetectionEngine detectionEngine;
  private ExemptManager exemptManager;

  @Override
  public void onLoad() {
    // PacketEvents 2.9.x bootstrap
    PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
    PacketEvents.getAPI().getSettings().checkForUpdates(false);
    PacketEvents.getAPI().load();
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
    this.exemptManager = new ExemptManager(this);

    PacketEvents.getAPI().init();
    // Регистрируем 2.9.x слушатель
    PacketEvents.getAPI().getEventManager().registerListener(new PacketListenerImpl(this));

    getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

    AcCommand ac = new AcCommand(this);
    if (getCommand("ac") != null) {
      getCommand("ac").setExecutor(ac);
      getCommand("ac").setTabCompleter(ac);
    } else {
      getLogger().severe("/ac command not defined in plugin.yml");
    }

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
    if (punishmentManager != null) {
      punishmentManager.reload();
    }
    if (mitigationManager != null) {
      mitigationManager.reload();
    }
    if (exemptManager != null) {
      exemptManager.save();
    }
    PacketEvents.getAPI().terminate();
    getLogger().info("ModernAC disabled.");
  }

  public ConfigManager getConfigManager() {
    return configManager;
  }

  public MessageManager getMessageManager() {
    return messageManager;
  }

  public DebugLogger getDebugLogger() {
    return debugLogger;
  }

  public CheckManager getCheckManager() {
    return checkManager;
  }

  public AlertEngine getAlertEngine() {
    return alertEngine;
  }

  public PunishmentManager getPunishmentManager() {
    return punishmentManager;
  }

  public MitigationManager getMitigationManager() {
    return mitigationManager;
  }

  public DetectionEngine getDetectionEngine() {
    return detectionEngine;
  }

  public ExemptManager getExemptManager() {
    return exemptManager;
  }

  public void reload() {
    reloadConfig();
    this.configManager = new ConfigManager(this);
    this.messageManager.reload();
    this.alertEngine.reload();
    this.punishmentManager.reload();
    this.mitigationManager.reload();
    this.detectionEngine.reload();
    this.exemptManager.load();
  }

  public void exemptPlayer(UUID uuid, long durationMs) {
    exemptManager.exemptPlayer(uuid, durationMs);
  }

  public boolean isExempt(UUID uuid) {
    return exemptManager.isExempt(uuid);
  }
}
