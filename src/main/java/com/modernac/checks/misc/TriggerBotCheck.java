package com.modernac.checks.misc;

import com.modernac.ModernACPlugin;
import com.modernac.checks.Check;
import com.modernac.player.PlayerData;

public class TriggerBotCheck extends Check {
    public TriggerBotCheck(ModernACPlugin plugin, PlayerData data) {
        super(plugin, data, "TriggerBot", true);
    }

    @Override
    public void handle(Object packet) {
        // TODO: Trigger-bot detection
    }
}
