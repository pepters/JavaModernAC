package com.modernac.checks.misc;

import com.modernac.ModernACPlugin;
import com.modernac.checks.Check;
import com.modernac.player.PlayerData;

import java.util.ArrayList;
import java.util.List;

public class MiscCheckFactory {
    public static List<Check> build(ModernACPlugin plugin, PlayerData data) {
        List<Check> list = new ArrayList<>();
        list.add(new AutoTotemCheck(plugin, data));
        list.add(new TriggerBotCheck(plugin, data));
        return list;
    }
}
