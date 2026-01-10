package com.hibiscusmc.hmccosmetics.util;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

/**
 * Folia兼容的调度器工具类
 * 自动检测服务器类型并使用相应的调度器
 */
public class SchedulerUtil {
    
    private static final boolean IS_FOLIA;
    
    static {
        boolean folia = false;
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            folia = true;
        } catch (ClassNotFoundException e) {
            folia = false;
        }
        IS_FOLIA = folia;
    }
    
    /**
     * 检查当前服务器是否为Folia
     */
    public static boolean isFolia() {
        return IS_FOLIA;
    }
    
    /**
     * 在主线程上运行任务
     */
    public static BukkitTask runTask(@NotNull Plugin plugin, @NotNull Runnable task) {
        if (IS_FOLIA) {
            ScheduledTask scheduledTask = Bukkit.getGlobalRegionScheduler().run(plugin, t -> task.run());
            return new FoliaTaskWrapper(scheduledTask);
        } else {
            return Bukkit.getScheduler().runTask(plugin, task);
        }
    }
    
    /**
     * 在主线程上延迟运行任务
     */
    public static BukkitTask runTaskLater(@NotNull Plugin plugin, @NotNull Runnable task, long delay) {
        if (IS_FOLIA) {
            ScheduledTask scheduledTask = Bukkit.getGlobalRegionScheduler().runDelayed(plugin, t -> task.run(), delay);
            return new FoliaTaskWrapper(scheduledTask);
        } else {
            return Bukkit.getScheduler().runTaskLater(plugin, task, delay);
        }
    }
    
    /**
     * 在主线程上定时运行任务
     */
    public static BukkitTask runTaskTimer(@NotNull Plugin plugin, @NotNull Runnable task, long delay, long period) {
        if (IS_FOLIA) {
            // Folia不允许初始延迟小于等于0，需要确保delay至少为1
            long foliaDelay = delay <= 0 ? 1 : delay;
            ScheduledTask scheduledTask = Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, t -> task.run(), foliaDelay, period);
            return new FoliaTaskWrapper(scheduledTask);
        } else {
            return Bukkit.getScheduler().runTaskTimer(plugin, task, delay, period);
        }
    }
    
    /**
     * 在异步线程上运行任务
     */
    public static BukkitTask runTaskAsynchronously(@NotNull Plugin plugin, @NotNull Runnable task) {
        if (IS_FOLIA) {
            ScheduledTask scheduledTask = Bukkit.getAsyncScheduler().runNow(plugin, t -> task.run());
            return new FoliaTaskWrapper(scheduledTask);
        } else {
            return Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
        }
    }
    
    /**
     * 在异步线程上延迟运行任务
     */
    public static BukkitTask runTaskLaterAsynchronously(@NotNull Plugin plugin, @NotNull Runnable task, long delay) {
        if (IS_FOLIA) {
            ScheduledTask scheduledTask = Bukkit.getAsyncScheduler().runDelayed(plugin, t -> task.run(), delay * 50L, TimeUnit.MILLISECONDS);
            return new FoliaTaskWrapper(scheduledTask);
        } else {
            return Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, task, delay);
        }
    }
    
    /**
     * 在异步线程上定时运行任务
     */
    public static BukkitTask runTaskTimerAsynchronously(@NotNull Plugin plugin, @NotNull Runnable task, long delay, long period) {
        if (IS_FOLIA) {
            ScheduledTask scheduledTask = Bukkit.getAsyncScheduler().runAtFixedRate(plugin, t -> task.run(), 
                delay * 50L, period * 50L, TimeUnit.MILLISECONDS);
            return new FoliaTaskWrapper(scheduledTask);
        } else {
            return Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, task, delay, period);
        }
    }
    
    /**
     * 在实体所在区域运行任务（Folia专用）
     */
    public static BukkitTask runTask(@NotNull Plugin plugin, @NotNull Entity entity, @NotNull Runnable task) {
        if (IS_FOLIA) {
            ScheduledTask scheduledTask = entity.getScheduler().run(plugin, t -> task.run(), null);
            return new FoliaTaskWrapper(scheduledTask);
        } else {
            return Bukkit.getScheduler().runTask(plugin, task);
        }
    }
    
    /**
     * 在实体所在区域延迟运行任务（Folia专用）
     */
    public static BukkitTask runTaskLater(@NotNull Plugin plugin, @NotNull Entity entity, @NotNull Runnable task, long delay) {
        if (IS_FOLIA) {
            ScheduledTask scheduledTask = entity.getScheduler().runDelayed(plugin, t -> task.run(), null, delay);
            return new FoliaTaskWrapper(scheduledTask);
        } else {
            return Bukkit.getScheduler().runTaskLater(plugin, task, delay);
        }
    }
    
    /**
     * 在实体所在区域定时运行任务（Folia专用）
     */
    public static BukkitTask runTaskTimer(@NotNull Plugin plugin, @NotNull Entity entity, @NotNull Runnable task, long delay, long period) {
        if (IS_FOLIA) {
            // Folia不允许初始延迟小于等于0，需要确保delay至少为1
            long foliaDelay = delay <= 0 ? 1 : delay;
            ScheduledTask scheduledTask = entity.getScheduler().runAtFixedRate(plugin, t -> task.run(), null, foliaDelay, period);
            return new FoliaTaskWrapper(scheduledTask);
        } else {
            return Bukkit.getScheduler().runTaskTimer(plugin, task, delay, period);
        }
    }
    
    /**
     * 取消任务
     */
    public static void cancelTask(int taskId) {
        if (IS_FOLIA) {
            // Folia中需要不同的取消方式
            // 由于Folia的API不同，我们需要在任务创建时保存引用
            // 这里提供兼容性方法，但建议使用返回的BukkitTask来取消
            Bukkit.getGlobalRegionScheduler().cancelTasks(HMCCosmeticsPlugin.getInstance());
        } else {
            Bukkit.getScheduler().cancelTask(taskId);
        }
    }
    
    /**
     * 取消插件的所有任务
     */
    public static void cancelTasks(@NotNull Plugin plugin) {
        if (IS_FOLIA) {
            Bukkit.getGlobalRegionScheduler().cancelTasks(plugin);
            Bukkit.getAsyncScheduler().cancelTasks(plugin);
        } else {
            Bukkit.getScheduler().cancelTasks(plugin);
        }
    }
    
    /**
     * 兼容旧版scheduleSyncRepeatingTask方法
     */
    public static int scheduleSyncRepeatingTask(@NotNull Plugin plugin, @NotNull Runnable task, long delay, long period) {
        BukkitTask bukkitTask = runTaskTimer(plugin, task, delay, period);
        return bukkitTask.getTaskId();
    }
    
    /**
     * 兼容旧版scheduleSyncDelayedTask方法
     */
    public static int scheduleSyncDelayedTask(@NotNull Plugin plugin, @NotNull Runnable task, long delay) {
        BukkitTask bukkitTask = runTaskLater(plugin, task, delay);
        return bukkitTask.getTaskId();
    }
    
    /**
     * Folia任务包装器，将ScheduledTask包装为BukkitTask
     */
    private static class FoliaTaskWrapper implements BukkitTask {
        private final ScheduledTask scheduledTask;
        
        public FoliaTaskWrapper(ScheduledTask scheduledTask) {
            this.scheduledTask = scheduledTask;
        }
        
        @Override
        public int getTaskId() {
            // Folia没有任务ID的概念，返回一个虚拟值
            return -1;
        }
        
        @Override
        public Plugin getOwner() {
            return scheduledTask.getOwningPlugin();
        }
        
        @Override
        public boolean isSync() {
            // Folia的区域调度器是同步的
            return true;
        }
        
        @Override
        public boolean isCancelled() {
            return scheduledTask.isCancelled();
        }
        
        @Override
        public void cancel() {
            scheduledTask.cancel();
        }
    }
}