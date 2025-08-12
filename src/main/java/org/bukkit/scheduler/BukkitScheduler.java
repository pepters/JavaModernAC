package org.bukkit.scheduler;

public class BukkitScheduler {
    public BukkitTask runTaskLater(Object plugin, Runnable task, long delay) {
        task.run();
        return new DummyBukkitTask();
    }

    public BukkitTask runTaskLaterAsynchronously(Object plugin, Runnable task, long delay) {
        task.run();
        return new DummyBukkitTask();
    }

    public int scheduleSyncRepeatingTask(Object plugin, Runnable task, long delay, long period) {
        task.run();
        return 0;
    }

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
