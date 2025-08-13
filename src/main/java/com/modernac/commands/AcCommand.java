package com.modernac.commands;

import com.modernac.ModernACPlugin;
import com.modernac.util.TimeUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class AcCommand implements CommandExecutor, TabCompleter {
  private final ModernACPlugin plugin;

  public AcCommand(ModernACPlugin plugin) {
    this.plugin = plugin;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (args.length == 0) {
      sender.sendMessage(plugin.getMessageManager().getMessage("commands.usage"));
      return true;
    }
    String sub = args[0].toLowerCase();
    switch (sub) {
      case "status":
        if (!sender.hasPermission("ac.command.status")) {
          sender.sendMessage(plugin.getMessageManager().getMessage("commands.no_permission"));
          return true;
        }
        if (args.length < 2) {
          sender.sendMessage(plugin.getMessageManager().getMessage("commands.usage"));
          return true;
        }
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        if (target == null || (target.getName() == null && !target.isOnline())) {
          sender.sendMessage(plugin.getMessageManager().getMessage("commands.player_not_found"));
          return true;
        }
        int ping = 0;
        if (target.isOnline()) {
          ping = ((Player) target).getPing();
        }
        double tps = Bukkit.getTPS()[0];
        long ttl = plugin.getExemptManager().getRemaining(target.getUniqueId());
        String ttlStr = ttl > 0 ? TimeUtil.formatDuration(ttl) : "none";
        sender.sendMessage(
            ChatColor.YELLOW
                + "Status for "
                + target.getName()
                + ": ping "
                + ping
                + "ms, tps "
                + String.format("%.1f", tps)
                + ", exempt TTL "
                + ttlStr);
        return true;
      case "debug":
        if (!sender.hasPermission("ac.command.debug")) {
          sender.sendMessage(plugin.getMessageManager().getMessage("commands.no_permission"));
          return true;
        }
        if (!(sender instanceof Player)) {
          sender.sendMessage(plugin.getMessageManager().getMessage("commands.no_permission"));
          return true;
        }
        if (args.length < 2) {
          sender.sendMessage(plugin.getMessageManager().getMessage("commands.usage"));
          return true;
        }
        target = Bukkit.getOfflinePlayer(args[1]);
        if (target == null || (target.getName() == null && !target.isOnline())) {
          sender.sendMessage(plugin.getMessageManager().getMessage("commands.player_not_found"));
          return true;
        }
        boolean enabled =
            plugin
                .getAlertEngine()
                .toggleDebug(((Player) sender).getUniqueId(), target.getUniqueId());
        sender.sendMessage(
            ChatColor.GREEN
                + "Debug "
                + (enabled ? "enabled" : "disabled")
                + " for "
                + target.getName());
        return true;
      case "exempt":
        if (!sender.hasPermission("ac.command.exempt")) {
          sender.sendMessage(plugin.getMessageManager().getMessage("commands.no_permission"));
          return true;
        }
        if (args.length < 3) {
          sender.sendMessage(plugin.getMessageManager().getMessage("commands.usage"));
          return true;
        }
        target = Bukkit.getOfflinePlayer(args[1]);
        if (target == null || (target.getName() == null && !target.isOnline())) {
          sender.sendMessage(plugin.getMessageManager().getMessage("commands.player_not_found"));
          return true;
        }
        long duration = TimeUtil.parseTime(args[2]);
        plugin.exemptPlayer(target.getUniqueId(), duration);
        String msg =
            plugin
                .getMessageManager()
                .getMessage("commands.exempt_set")
                .replace("{player}", target.getName())
                .replace("{time}", args[2]);
        sender.sendMessage(msg);
        return true;
      case "reload":
        if (!sender.hasPermission("ac.command.reload")) {
          sender.sendMessage(plugin.getMessageManager().getMessage("commands.no_permission"));
          return true;
        }
        plugin.reload();
        sender.sendMessage(plugin.getMessageManager().getMessage("commands.reloaded"));
        return true;
      case "inspect":
        if (!sender.hasPermission("ac.alerts")) {
          sender.sendMessage(plugin.getMessageManager().getMessage("commands.no_permission"));
          return true;
        }
        if (args.length < 2) {
          sender.sendMessage(plugin.getMessageManager().getMessage("commands.usage"));
          return true;
        }
        target = Bukkit.getOfflinePlayer(args[1]);
        if (target == null || (target.getName() == null && !target.isOnline())) {
          sender.sendMessage(plugin.getMessageManager().getMessage("commands.player_not_found"));
          return true;
        }
        ping = 0;
        if (target.isOnline()) {
          ping = ((Player) target).getPing();
        }
        tps = Bukkit.getTPS()[0];
        long punish = plugin.getPunishmentManager().getRemaining(target.getUniqueId());
        long mitigate = plugin.getMitigationManager().getRemaining(target.getUniqueId());
        ttl = plugin.getExemptManager().getRemaining(target.getUniqueId());
        com.modernac.engine.DetectionEngine.DetectionSummary sum =
            plugin.getDetectionEngine().getSummary(target.getUniqueId());
        boolean latencyOK = sum != null && sum.latencyOK;
        boolean stabilityOK = sum != null && sum.stabilityOK;
        String msgInspect =
            ChatColor.YELLOW
                + "Inspect "
                + target.getName()
                + ": ping "
                + ping
                + "ms, tps "
                + String.format("%.1f", tps)
                + ", latencyOK="
                + latencyOK
                + ", stabilityOK="
                + stabilityOK
                + ", short="
                + String.format("%.2f", sum != null ? sum.shortWindow : 0.0)
                + ", long="
                + String.format("%.2f", sum != null ? sum.longWindow : 0.0)
                + ", veryLong="
                + String.format("%.2f", sum != null ? sum.veryLongWindow : 0.0)
                + ", punish="
                + TimeUtil.formatDuration(punish)
                + ", mitigation="
                + TimeUtil.formatDuration(mitigate)
                + ", exempt="
                + TimeUtil.formatDuration(ttl);
        sender.sendMessage(msgInspect);
        return true;
      case "devfake":
        if (args.length < 2) {
          sender.sendMessage(plugin.getMessageManager().getMessage("commands.usage"));
          return true;
        }
        target = Bukkit.getOfflinePlayer(args[1]);
        if (target == null || (target.getName() == null && !target.isOnline())) {
          sender.sendMessage(plugin.getMessageManager().getMessage("commands.player_not_found"));
          return true;
        }
        plugin
            .getAlertEngine()
            .queueAlert(
                target.getUniqueId(),
                new com.modernac.engine.AlertEngine.AlertDetail(
                    "DEV", "SHORT", 1.0, 0, Bukkit.getTPS()[0]),
                false);
        sender.sendMessage(ChatColor.GREEN + "Dev alert queued for " + target.getName());
        return true;
      default:
        sender.sendMessage(plugin.getMessageManager().getMessage("commands.usage"));
        return true;
    }
  }

  @Override
  public List<String> onTabComplete(
      CommandSender sender, Command command, String alias, String[] args) {
    if (args.length == 1) {
      return Arrays.asList("status", "debug", "exempt", "reload", "inspect", "devfake");
    }
    if (args.length == 2
        && (args[0].equalsIgnoreCase("status")
            || args[0].equalsIgnoreCase("debug")
            || args[0].equalsIgnoreCase("exempt")
            || args[0].equalsIgnoreCase("inspect")
            || args[0].equalsIgnoreCase("devfake"))) {
      List<String> names = new ArrayList<>();
      for (Player p : Bukkit.getOnlinePlayers()) {
        names.add(p.getName());
      }
      return names;
    }
    if (args.length == 3 && args[0].equalsIgnoreCase("exempt")) {
      return Arrays.asList("10s", "5m", "1h");
    }
    return new ArrayList<>();
  }
}
