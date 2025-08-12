package com.modernac;

import com.modernac.config.ConfigManager;
import com.modernac.logging.DebugLogger;
import com.modernac.manager.AlertManager;
import com.modernac.manager.MitigationManager;
import com.modernac.manager.PunishmentManager;

import java.util.UUID;

public class TestPlugin extends ModernACPlugin {
    private final ConfigManager configManager;
    private final DebugLogger debugLogger;
    private final AlertManager alertManager;
    private final MitigationManager mitigationManager;
    private final PunishmentManager punishmentManager;

    public TestPlugin() {
        this.configManager = new ConfigManager(this);
        this.debugLogger = new DebugLogger(this) {
            @Override
            public void log(String message) {
                // no-op for tests
            }
        };
        this.alertManager = new AlertManager(this) {
            @Override
            public void alert(UUID uuid, String check, int vl) {
                // no-op for tests
            }
        };
        this.mitigationManager = new MitigationManager(this) {
            @Override
            public void mitigate(UUID uuid, int level) {
                // no-op for tests
            }
        };
        this.punishmentManager = new PunishmentManager(this) {
            @Override
            public void schedule(UUID uuid) {
                // no-op for tests
            }

            @Override
            public void cancel(UUID uuid) {
                // no-op for tests
            }
        };
    }

    @Override public ConfigManager getConfigManager() { return configManager; }
    @Override public DebugLogger getDebugLogger() { return debugLogger; }
    @Override public AlertManager getAlertManager() { return alertManager; }
    @Override public MitigationManager getMitigationManager() { return mitigationManager; }
    @Override public PunishmentManager getPunishmentManager() { return punishmentManager; }
}
