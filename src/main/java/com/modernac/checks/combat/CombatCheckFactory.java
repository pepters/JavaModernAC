package com.modernac.checks.combat;

import com.modernac.ModernACPlugin;
import com.modernac.checks.Check;
import com.modernac.player.PlayerData;
import java.util.ArrayList;
import java.util.List;

// import new combat check


public class CombatCheckFactory {
  public static List<Check> build(ModernACPlugin plugin, PlayerData data) {
    List<Check> list = new ArrayList<>();
    list.add(new AutoTotemDetection(plugin, data));
    list.add(new ClickAimCorrelationLongCheck(plugin, data));
    return list;
  }
}
