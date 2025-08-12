package org.bukkit.configuration;

import java.util.Collections;
import java.util.Set;

public interface ConfigurationSection {
    default String getString(String path) { return null; }
    default ConfigurationSection getConfigurationSection(String path) { return this; }
    default Set<String> getKeys(boolean deep) { return Collections.emptySet(); }
}
