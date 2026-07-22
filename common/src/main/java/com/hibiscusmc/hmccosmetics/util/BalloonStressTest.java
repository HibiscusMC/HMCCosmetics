package com.hibiscusmc.hmccosmetics.util;

import com.hibiscusmc.hmccosmetics.config.Settings;
import com.hibiscusmc.hmccosmetics.cosmetic.types.CosmeticBalloonType;
import com.hibiscusmc.hmccosmetics.user.manager.BalloonSmoothingTask;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.entity.data.BukkitEntityData;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import me.lojosho.hibiscuscommons.hooks.Hooks;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Synthetic load generator for the balloon system. Spawns {@code count} real, invisible marker armor
 * stands - the exact entity {@link com.hibiscusmc.hmccosmetics.user.manager.UserBalloonManager} uses -
 * each carrying the caller's equipped balloon cosmetic (ModelEngine model or item helmet), and drives
 * every one of them toward a wandering target on the same cadence as {@link BalloonSmoothingTask}
 * ({@code balloon-lerp-period}). The target keeps moving so all balloons are always lagging, i.e. the
 * worst case where every balloon teleports every update.
 * <p>
 * This reproduces the dominant per-balloon cost - a real {@link ArmorStand#teleport} plus the vanilla
 * entity-tracker broadcast to every nearby viewer, and the ModelEngine render for a real model - at
 * whatever count you ask for, without needing that many players. It measures its own main-thread
 * self-time and reports avg ms/update to the console, so a profiler is optional. It does NOT spawn the
 * lead pufferfish; the armor-stand teleport and model render are the costs that scale, and the lead is a
 * comparatively cheap packet guarded by having viewers.
 * <p>
 * Debug/benchmark tool only. Spawned stands are non-persistent (gone on restart) and are also cleared by
 * {@link #stop()} and on plugin disable.
 */
public final class BalloonStressTest {

    private BalloonStressTest() {}

    private static final List<ArmorStand> STANDS = new ArrayList<>();
    private static BukkitTask task;
    private static Location anchor;
    private static long tick;
    private static boolean usedModelEngine;

    // Self-timing accumulators, reset each reporting window.
    private static double windowNanos;
    private static long windowSamples;
    private static CommandSender reportTo;

    public static synchronized boolean isRunning() {
        return task != null;
    }

    public static synchronized int count() {
        return STANDS.size();
    }

    /**
     * Spawns {@code count} stress balloons around {@code origin} and starts driving them.
     *
     * @param balloon  the cosmetic to render on every stand (its model/item). May be null.
     * @param fallback helmet item worn when {@code balloon} is null or has no ModelEngine model, so the
     *                 swarm is still visible. May be null for a bare marker stand.
     * @param report   where to echo the periodic timing summary; may be null.
     */
    public static synchronized void start(@NotNull Plugin plugin, @NotNull Location origin, int count,
                                          @Nullable CosmeticBalloonType balloon, @Nullable ItemStack fallback,
                                          @Nullable CommandSender report) {
        stop();
        anchor = origin.clone();
        reportTo = report;
        final World world = origin.getWorld();
        if (world == null) return;

        final boolean modelEngine = balloon != null && balloon.getModelName() != null
                && Hooks.isActiveHook("ModelEngine")
                && ModelEngineAPI.getBlueprint(balloon.getModelName()) != null;
        usedModelEngine = modelEngine;
        final ItemStack helmet = balloon != null && balloon.getItem() != null ? balloon.getItem() : fallback;

        for (int i = 0; i < count; i++) {
            // Small spread so the caller (and any nearby players) tracks them all, exercising the broadcast.
            final Location spawn = origin.clone().add((Math.random() - 0.5) * 6, 0, (Math.random() - 0.5) * 6);
            final ArmorStand stand = world.spawn(spawn, ArmorStand.class, e -> {
                e.setInvisible(true);
                e.setGravity(false);
                e.setSilent(true);
                e.setInvulnerable(true);
                e.setSmall(true);
                e.setMarker(true);
                e.setPersistent(false);
                e.setAI(false);
                if (!modelEngine && helmet != null) e.getEquipment().setHelmet(helmet);
                e.getPersistentDataContainer().set(HMCCServerUtils.getCosmemeticMobKey(), PersistentDataType.BOOLEAN, true);
            });
            if (modelEngine) applyModel(stand, balloon.getModelName());
            STANDS.add(stand);
        }

        final int period = Math.max(1, Settings.getBalloonLerpPeriod());
        tick = 0;
        windowNanos = 0;
        windowSamples = 0;
        task = Bukkit.getScheduler().runTaskTimer(plugin, BalloonStressTest::run, period, period);
    }

    // Attaches a real ModelEngine model to the stand, mirroring UserBalloonManager.spawnModel so the
    // render cost measured is the genuine one.
    private static void applyModel(@NotNull ArmorStand stand, @NotNull String modelId) {
        ModeledEntity modeledEntity = ModelEngineAPI.getOrCreateModeledEntity(stand);
        ActiveModel model = ModelEngineAPI.createActiveModel(ModelEngineAPI.getBlueprint(modelId));
        model.setCanHurt(false);
        modeledEntity.addModel(model, false);
        BukkitEntityData data = (BukkitEntityData) modeledEntity.getBase().getData();
        data.setBlockedCullIgnoreRadius((double) Settings.getViewDistance());
    }

    public static synchronized void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        for (ArmorStand stand : STANDS) {
            if (stand == null || !stand.isValid()) continue;
            if (usedModelEngine) {
                ModeledEntity me = ModelEngineAPI.getModeledEntity(stand);
                if (me != null) me.destroy();
            }
            stand.remove();
        }
        STANDS.clear();
        anchor = null;
        reportTo = null;
        usedModelEngine = false;
    }

    private static synchronized void run() {
        final int period = Math.max(1, Settings.getBalloonLerpPeriod());
        tick += period;

        // Wander the shared target so every balloon keeps a follow-lag and thus keeps teleporting.
        final double t = tick * 0.02;
        final Location target = anchor.clone().add(Math.sin(t) * 8.0, Math.sin(t * 0.5) * 2.0, Math.cos(t) * 8.0);

        final double posFactor = 1 - Math.pow(1 - 0.35, period);
        final double vertFactor = 1 - Math.pow(1 - 0.15, period);
        final double yawFactor = 1 - Math.pow(1 - 0.15, period);

        final long start = System.nanoTime();
        for (ArmorStand stand : STANDS) {
            if (!stand.isValid()) continue;
            final Location cur = stand.getLocation();
            final double dx = target.getX() - cur.getX();
            final double dz = target.getZ() - cur.getZ();
            final double desiredYaw = Math.toDegrees(Math.atan2(-dx, dz));
            final Location render = MathUtil.lerpLocation(cur, target, posFactor, vertFactor);
            render.setYaw((float) MathUtil.lerpAngle(cur.getYaw(), desiredYaw, yawFactor));
            stand.teleport(render);
        }
        final long elapsed = System.nanoTime() - start;

        windowNanos += elapsed;
        windowSamples++;

        // Report roughly every 5 seconds of wall time.
        if (windowSamples * period >= 100) {
            final double avgMs = (windowNanos / windowSamples) / 1_000_000.0;
            final String line = String.format(
                    "[BalloonStressTest] %d balloons, period=%d: avg %.3f ms/update (%.2f%% of a 50ms tick budget)",
                    STANDS.size(), period, avgMs, avgMs / 50.0 * 100.0);
            Bukkit.getLogger().info(line);
            if (reportTo != null) reportTo.sendMessage(line);
            windowNanos = 0;
            windowSamples = 0;
        }
    }
}
