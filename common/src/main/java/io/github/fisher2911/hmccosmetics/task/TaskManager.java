package io.github.fisher2911.hmccosmetics.task;

import io.github.fisher2911.hmccosmetics.HMCCosmetics;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

public class TaskManager {

    private final HMCCosmetics plugin;
    private BukkitTask timer;
    private final Queue<Task> tasks = new ConcurrentLinkedDeque<>();

    public TaskManager(final HMCCosmetics plugin) {
        this.plugin = plugin;
    }

    public void start() {
        this.timer = Bukkit.getScheduler().runTaskTimerAsynchronously(
                this.plugin,
                () -> {
                    int currentTasks = this.tasks.size();
                    Task task;
                    while ((task = this.tasks.peek()) != null && currentTasks > 0) {
                        // if an exception is thrown it will never end up removing the task
                        try {
                            task.run();
                        } catch (final Exception e) {
                            e.printStackTrace();
                        }
                        this.tasks.poll();
                        if (!task.isComplete()) {
                            this.tasks.add(task);
                        }
                        currentTasks--;
                    }
                },
                1,
                1
        );
    }

    public void submit(final Task task) {
        this.tasks.add(task);
    }

    public void submit(final Runnable runnable) {
        this.submit(new ImmediateTask(runnable));
    }

    public void end() {
        this.timer.cancel();
        for (final Task task : this.tasks) {
            task.run();
        }
    }
}
