package org.bukkit.scheduler;

/**
 * Simple no-op scheduler implementation used for testing. Tasks are executed
 * immediately on the calling thread and no scheduling actually occurs.
 */
public class DummyBukkitScheduler implements BukkitScheduler {
    @Override
    public BukkitTask runTaskLater(Object plugin, Runnable task, long delay) {
        task.run();
        return new DummyBukkitTask();
    }

    @Override
    public BukkitTask runTaskLaterAsynchronously(Object plugin, Runnable task, long delay) {
        task.run();
        return new DummyBukkitTask();
    }

    @Override
    public int scheduleSyncRepeatingTask(Object plugin, Runnable task, long delay, long period) {
        task.run();
        return 0;
    }

    @Override
    public void cancelTask(int id) {
        // no-op
    }

    private static class DummyBukkitTask implements BukkitTask {
        @Override
        public void cancel() {
            // no-op
        }
    }
}
