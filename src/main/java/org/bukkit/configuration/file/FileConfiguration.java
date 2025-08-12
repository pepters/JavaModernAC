package org.bukkit.configuration.file;

import java.util.Collections;
import java.util.Set;

public class FileConfiguration {
    public String getString(String path) { return null; }
    public String getString(String path, String def) { return def; }
    public int getInt(String path, int def) { return def; }
    public boolean getBoolean(String path, boolean def) { return def; }
    public FileConfiguration getConfigurationSection(String path) { return this; }
    public Set<String> getKeys(boolean deep) { return Collections.emptySet(); }
}
