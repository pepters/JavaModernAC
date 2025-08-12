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

    private static class DummyBukkitTask implements BukkitTask {
        @Override
        public void cancel() {
            // no-op
        }
    }
}
