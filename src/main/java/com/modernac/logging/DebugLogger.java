package com.modernac.logging;

import com.modernac.ModernACPlugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DebugLogger {
    private final File logFile;
    private final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final boolean enabled;

    public DebugLogger(ModernACPlugin plugin) {
        File folder = new File(plugin.getDataFolder(), "logs");
        folder.mkdirs();
        logFile = new File(folder, "detections.log");
        this.enabled = plugin.getConfigManager().isDetectionsDebug();
    }

    public synchronized void log(String message) {
        if (!enabled) return;
        try (FileWriter writer = new FileWriter(logFile, true)) {
            writer.write(format.format(new Date()) + " " + message + System.lineSeparator());
        } catch (IOException ignored) {
        }
    }
}
