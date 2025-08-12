package com.modernac.messages;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.modernac.ModernACPlugin;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class MessageManager {
    private final ModernACPlugin plugin;
    private final Map<String, String> messages = new HashMap<>();

    public MessageManager(ModernACPlugin plugin) {
        this.plugin = plugin;
        plugin.saveResource("messages.yml", false);
        loadMessages();
    }

    private void loadMessages() {
        File file = new File(plugin.getDataFolder(), "messages.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        for (String key : config.getConfigurationSection("messages").getKeys(false)) {
            String msg = ChatColor.translateAlternateColorCodes('&', config.getString("messages." + key));
            messages.put(key, msg);
        }
    }

    public String getMessage(String key) {
        return messages.getOrDefault(key, key);
    }
}
