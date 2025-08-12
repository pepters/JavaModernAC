package org.bukkit.configuration.file;

import java.util.Collections;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;

public class FileConfiguration implements ConfigurationSection {
    @Override
    public String getString(String path) { return null; }
    public String getString(String path, String def) { return def; }
    public int getInt(String path, int def) { return def; }
    public boolean getBoolean(String path, boolean def) { return def; }
    @Override
    public ConfigurationSection getConfigurationSection(String path) { return this; }
    @Override
    public Set<String> getKeys(boolean deep) { return Collections.emptySet(); }
}
