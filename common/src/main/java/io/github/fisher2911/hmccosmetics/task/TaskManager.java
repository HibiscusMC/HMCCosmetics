package io.github.fisher2911.hmccosmetics.task;

import io.github.fisher2911.hmccosmetics.HMCCosmetics;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.LinkedList;
import java.util.Queue;

public class TaskManager {

    private final HMCCosmetics plugin;
    private BukkitTask timer;
    private final Queue<Task> tasks = new LinkedList<>();

    public TaskManager(final HMCCosmetics plugin) {
        this.plugin = plugin;
    }

    public void start() {
        this.timer = Bukkit.getScheduler().runTaskTimerAsynchronously(
                this.plugin,
                () -> tasks.removeIf(task -> {
                    task.run();
                    return task.isComplete();
                }),
                1,
                1
        );
    }

    public void submit(final Task task) {
        this.tasks.add(task);
    }

    public void end() {
        this.timer.cancel();
        for (final Task task : this.tasks) {
            task.run();
        }
    }
}
