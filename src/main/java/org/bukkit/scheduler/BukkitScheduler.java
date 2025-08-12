package org.bukkit.scheduler;

/**
 * Minimal scheduler interface used for testing without a real Bukkit runtime.
 *
 * <p>This mirrors the Bukkit API's {@code BukkitScheduler} which is an
 * interface on modern servers. The previous stub incorrectly declared it as a
 * class which caused code compiled against it to expect a concrete class at
 * runtime. When running on a real server the scheduler is provided as an
 * interface, leading to {@link java.lang.IncompatibleClassChangeError}. By
 * modelling it as an interface here we ensure the compiled bytecode matches the
 * actual API.</p>
 */
public interface BukkitScheduler {

    BukkitTask runTaskLater(Object plugin, Runnable task, long delay);

    BukkitTask runTaskLaterAsynchronously(Object plugin, Runnable task, long delay);

    int scheduleSyncRepeatingTask(Object plugin, Runnable task, long delay, long period);

    void cancelTask(int id);
}

