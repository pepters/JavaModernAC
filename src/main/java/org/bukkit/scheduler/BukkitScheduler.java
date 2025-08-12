package org.bukkit.scheduler;

public class BukkitScheduler {
    public void runTaskLater(Object plugin, Runnable task, long delay) { task.run(); }
    public void runTaskLaterAsynchronously(Object plugin, Runnable task, long delay) { task.run(); }
}
