package com.hibiscusmc.hmccosmetics.util;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitTask;

/**
 * Dispatches work to the correct scheduler on both Paper and Folia.
 */
public final class SchedulerUtil {

    private static final boolean FOLIA = detectFolia();

    private SchedulerUtil() {
    }

    public static boolean isFolia() {
        return FOLIA;
    }

    public static void run(Entity entity, Runnable task) {
        run(entity, task, () -> { });
    }

    public static void run(Entity entity, Runnable task, Runnable retired) {
        if (FOLIA) {
            entity.getScheduler().run(plugin(), ignored -> task.run(), retired);
        } else {
            Bukkit.getScheduler().runTask(plugin(), task);
        }
    }

    public static void runLater(Entity entity, Runnable task, long delayTicks) {
        runLater(entity, task, () -> { }, delayTicks);
    }

    public static void runLater(Entity entity, Runnable task, Runnable retired, long delayTicks) {
        if (FOLIA) {
            entity.getScheduler().runDelayed(plugin(), ignored -> task.run(), retired, Math.max(1L, delayTicks));
        } else {
            Bukkit.getScheduler().runTaskLater(plugin(), task, delayTicks);
        }
    }

    public static TaskHandle runTimer(Entity entity, Runnable task, long delayTicks, long periodTicks) {
        return runTimer(entity, task, () -> { }, delayTicks, periodTicks);
    }

    public static TaskHandle runTimer(Entity entity, Runnable task, Runnable retired, long delayTicks, long periodTicks) {
        if (FOLIA) {
            ScheduledTask scheduledTask = entity.getScheduler().runAtFixedRate(
                plugin(), ignored -> task.run(), retired, Math.max(1L, delayTicks), Math.max(1L, periodTicks)
            );
            return scheduledTask == null ? TaskHandle.NO_OP : scheduledTask::cancel;
        }
        BukkitTask bukkitTask = Bukkit.getScheduler().runTaskTimer(plugin(), task, delayTicks, periodTicks);
        return bukkitTask::cancel;
    }

    public static void runGlobal(Runnable task) {
        if (FOLIA) {
            Bukkit.getGlobalRegionScheduler().execute(plugin(), task);
        } else {
            Bukkit.getScheduler().runTask(plugin(), task);
        }
    }

    public static void runAsync(Runnable task) {
        if (FOLIA) {
            Bukkit.getAsyncScheduler().runNow(plugin(), ignored -> task.run());
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(plugin(), task);
        }
    }

    private static HMCCosmeticsPlugin plugin() {
        return HMCCosmeticsPlugin.getInstance();
    }

    private static boolean detectFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }

    @FunctionalInterface
    public interface TaskHandle {
        TaskHandle NO_OP = () -> { };

        void cancel();
    }
}
