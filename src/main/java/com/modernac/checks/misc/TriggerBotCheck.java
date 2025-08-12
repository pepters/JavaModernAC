package com.modernac.checks.misc;

import com.modernac.ModernACPlugin;
import com.modernac.checks.Check;
import com.modernac.events.AttackEventData;
import com.modernac.player.PlayerData;

public class TriggerBotCheck extends Check {
    public TriggerBotCheck(ModernACPlugin plugin, PlayerData data) {
        super(plugin, data, "TriggerBot", true);
    }

    @Override
    public void handle(Object data) {
        if (data instanceof AttackEventData) {
            // TODO: Trigger-bot detection
        }
    }
}
