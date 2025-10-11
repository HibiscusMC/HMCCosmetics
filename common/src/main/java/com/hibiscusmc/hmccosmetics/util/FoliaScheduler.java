package com.hibiscusmc.hmccosmetics.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Minimal Folia-aware scheduler for Java.
 * - On Folia: uses Global/Region/Entity/Async schedulers via reflection.
 * - Else: falls back to BukkitScheduler (sync/async).
 *
 * No compile-time dependency on Folia API required.
 */
public final class FoliaScheduler {
    private final Plugin plugin;
    private final boolean isFolia;

    public FoliaScheduler(Plugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.isFolia = detectFolia();
    }

    /* ------------------------ Public API ------------------------ */

    /** Run on global region (sync). */
    public void runGlobal(Runnable task) {
        if (isFolia) foliaGlobalRun(task);
        else Bukkit.getScheduler().runTask(plugin, task);
    }

    /** Run on global region after delay (ticks). */
    public void runGlobalLater(long delayTicks, Runnable task) {
        if (isFolia) foliaGlobalRunDelayed(delayTicks, task);
        else Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks);
    }

    /** Run on global region at fixed rate (ticks). */
    public void runGlobalTimer(long delayTicks, long periodTicks, Runnable task) {
        if (isFolia) foliaGlobalRunAtFixedRate(delayTicks, periodTicks, task);
        else Bukkit.getScheduler().runTaskTimer(plugin, task, delayTicks, periodTicks);
    }

    /** Run in the region at a specific location. */
    public void runAt(Location location, Runnable task) {
        if (!isFolia || location == null) {
            Bukkit.getScheduler().runTask(plugin, task);
            return;
        }
        foliaRegionRun(location, task);
    }

    /** Run in the region of a specific entity (entity-locked). */
    public void runFor(Entity entity, Runnable task) {
        if (isFolia && entity != null) foliaEntityRun(entity, task);
        else Bukkit.getScheduler().runTask(plugin, task);
    }

    /** Run in the region of a specific entity later (ticks). */
    public void runForLater(Entity entity, long delayTicks, Runnable task) {
        if (isFolia && entity != null) foliaEntityRunDelayed(entity, delayTicks, task);
        else Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks);
    }

    /** Run async. */
    public void runAsync(Runnable task) {
        if (isFolia) foliaAsyncRunNow(task);
        else Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
    }

    /** Run async after a delay in time units. */
    public void runAsyncLater(long delay, TimeUnit unit, Runnable task) {
        if (isFolia) {
            foliaAsyncRunDelayed(delay, unit, task);
        } else {
            long ticks = Math.max(1L, millis(unit.toMillis(delay)) / 50L);
            Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, task, ticks);
        }
    }

    /* ------------------------ Detection ------------------------ */

    private static boolean detectFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }

    /* ------------------------ Folia via reflection ------------------------ */

    private void foliaGlobalRun(Runnable task) {
        try {
            Object global = Bukkit.class.getMethod("getGlobalRegionScheduler").invoke(null);
            Method run = global.getClass().getMethod("run", Plugin.class, Consumer.class);
            run.invoke(global, plugin, (Consumer<Object>) ignored -> task.run());
        } catch (Throwable t) {
            // Fallback just in case
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }

    private void foliaGlobalRunDelayed(long delayTicks, Runnable task) {
        try {
            Object global = Bukkit.class.getMethod("getGlobalRegionScheduler").invoke(null);
            Method run = global.getClass().getMethod("runDelayed", Plugin.class, Consumer.class, long.class);
            run.invoke(global, plugin, (Consumer<Object>) ignored -> task.run(), delayTicks);
        } catch (Throwable t) {
            Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks);
        }
    }

    private void foliaGlobalRunAtFixedRate(long delayTicks, long periodTicks, Runnable task) {
        try {
            Object global = Bukkit.class.getMethod("getGlobalRegionScheduler").invoke(null);
            Method run = global.getClass().getMethod("runAtFixedRate", Plugin.class, Consumer.class, long.class, long.class);
            run.invoke(global, plugin, (Consumer<Object>) ignored -> task.run(), delayTicks, periodTicks);
        } catch (Throwable t) {
            Bukkit.getScheduler().runTaskTimer(plugin, task, delayTicks, periodTicks);
        }
    }

    private void foliaRegionRun(Location loc, Runnable task) {
        try {
            Object region = Bukkit.class.getMethod("getRegionScheduler").invoke(null);
            World world = loc.getWorld();
            Method run = region.getClass().getMethod("run", Plugin.class, World.class, Location.class, Consumer.class);
            run.invoke(region, plugin, world, loc, (Consumer<Object>) ignored -> task.run());
        } catch (Throwable t) {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }

    private void foliaEntityRun(Entity entity, Runnable task) {
        try {
            Method getScheduler = entity.getClass().getMethod("getScheduler");
            Object entityScheduler = getScheduler.invoke(entity);
            Method run = entityScheduler.getClass().getMethod("run", Plugin.class, Runnable.class, Object.class);
            run.invoke(entityScheduler, plugin, (Runnable) task, null);
        } catch (Throwable t) {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }

    private void foliaEntityRunDelayed(Entity entity, long delayTicks, Runnable task) {
        try {
            Method getScheduler = entity.getClass().getMethod("getScheduler");
            Object entityScheduler = getScheduler.invoke(entity);
            Method runDelayed = entityScheduler.getClass().getMethod("runDelayed", Plugin.class, Runnable.class, Object.class, long.class);
            runDelayed.invoke(entityScheduler, plugin, (Runnable) task, null, delayTicks);
        } catch (Throwable t) {
            Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks);
        }
    }

    private void foliaAsyncRunNow(Runnable task) {
        try {
            Object async = Bukkit.class.getMethod("getAsyncScheduler").invoke(null);
            Method runNow = async.getClass().getMethod("runNow", Plugin.class, Consumer.class);
            runNow.invoke(async, plugin, (Consumer<Object>) ignored -> task.run());
        } catch (Throwable t) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
        }
    }

    private void foliaAsyncRunDelayed(long delay, TimeUnit unit, Runnable task) {
        try {
            Object async = Bukkit.class.getMethod("getAsyncScheduler").invoke(null);
            Method runDelayed = async.getClass().getMethod("runDelayed", Plugin.class, Consumer.class, long.class, TimeUnit.class);
            runDelayed.invoke(async, plugin, (Consumer<Object>) ignored -> task.run(), delay, unit);
        } catch (Throwable t) {
            long ticks = Math.max(1L, millis(unit.toMillis(delay)) / 50L);
            Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, task, ticks);
        }
    }

    private static long millis(long ms) { return ms; }
}
