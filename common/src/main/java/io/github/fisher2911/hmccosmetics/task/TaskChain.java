package io.github.fisher2911.hmccosmetics.task;

import com.google.common.collect.HashBiMap;
import io.github.fisher2911.hmccosmetics.HMCCosmetics;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.io.BufferedReader;
import java.util.LinkedList;
import java.util.Queue;

public class TaskChain {

    private final HMCCosmetics plugin;
    private final Queue<SyncedTask> tasks = new LinkedList<>();

    public TaskChain(final HMCCosmetics plugin) {
        this.plugin = plugin;
    }

    public TaskChain chain(final Runnable runnable) {
        return this.chain(runnable, false);
    }

    public TaskChain chain(final Runnable runnable, final boolean async) {
        this.tasks.add(new SyncedTask(runnable, async));
        return this;
    }

    public void execute() {
        final SyncedTask first = tasks.poll();
        if (first == null) return;
        this.run(first, this.tasks.poll());
    }

    private void run(final SyncedTask task, final SyncedTask next) {
        if (task.isAsync()) {
            Bukkit.getScheduler().runTaskAsynchronously(
                    this.plugin,
                    () -> {
                        task.getRunnable().run();
                        if (next == null) return;
                        run(next, this.tasks.poll());
                    }
            );
            return;
        }
        Bukkit.getScheduler().runTask(
                this.plugin,
                () -> {
                    task.getRunnable().run();
                    if (next == null) return;
                    run(next, this.tasks.poll());
                }
        );
    }
}
