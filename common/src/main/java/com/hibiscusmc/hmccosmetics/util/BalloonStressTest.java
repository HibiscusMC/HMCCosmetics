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
import org.bukkit.util.EulerAngle;
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
 * ({@code balloon-lerp-period}). Each balloon is stepped through {@link BalloonSmoothingTask#step} - the
 * exact production math (position lerp, idle bob/sway, rope-drag dip, movement yaw and tilt) - and pays
 * the same teleport dedup and item-balloon head-pose tilt broadcast, so the measured cost is the real
 * path rather than a divergent copy. The target keeps moving so all balloons are always lagging, i.e.
 * the worst case where every balloon teleports every update.
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

    private static final List<Balloon> BALLOONS = new ArrayList<>();
    private static BukkitTask task;
    private static Location anchor;
    private static long tick;
    private static boolean usedModelEngine;

    // Dedup thresholds mirrored from BalloonSmoothingTask / UserBalloonManager so the benchmark skips the
    // same sub-visible teleports and tilt packets production does.
    private static final double RENDER_EPSILON_SQUARED = 0.001 * 0.001;
    private static final double SETTLED_YAW = 0.05;
    private static final double TILT_EPSILON = 0.05;

    // Per-balloon smoothing state, mirroring what UserBalloonManager holds for a real balloon, so the
    // benchmark can drive BalloonSmoothingTask.step() exactly like production: the follow-lerp base is
    // tracked apart from the rendered (bob + dip) location so the display offsets never feed back into it,
    // and the sent tilt is tracked apart from the logical tilt so the head-pose packet only goes out on a
    // visible change.
    private static final class Balloon {
        final ArmorStand stand;
        final double phase;
        Location base;
        Location lastRendered;
        double tiltPitch;
        double tiltRoll;
        double sentTiltPitch;
        double sentTiltRoll;

        Balloon(ArmorStand stand, double phase, Location base) {
            this.stand = stand;
            this.phase = phase;
            this.base = base;
        }
    }

    // Self-timing accumulators, reset each reporting window.
    private static double windowNanos;
    private static long windowSamples;
    private static CommandSender reportTo;

    public static synchronized boolean isRunning() {
        return task != null;
    }

    public static synchronized int count() {
        return BALLOONS.size();
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
            // Golden-angle phase spread so the idle bob/sway of adjacent balloons is out of lockstep,
            // the same de-synchronisation UserBalloonManager gets from the per-user UUID hash.
            final double phase = 2.399963229728653 * i;
            BALLOONS.add(new Balloon(stand, phase, spawn.clone()));
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
        for (Balloon b : BALLOONS) {
            final ArmorStand stand = b.stand;
            if (stand == null || !stand.isValid()) continue;
            if (usedModelEngine) {
                ModeledEntity me = ModelEngineAPI.getModeledEntity(stand);
                if (me != null) me.destroy();
            }
            stand.remove();
        }
        BALLOONS.clear();
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

        // The exact tuning production resolves once per run - real lerp factors, dip, bob, sway, tilt.
        final BalloonSmoothingTask.Tuning tuning = BalloonSmoothingTask.Tuning.snapshot(period);

        final long start = System.nanoTime();
        for (Balloon b : BALLOONS) {
            if (!b.stand.isValid()) continue;

            // The exact per-balloon computation production runs, from this balloon's own follow-lerp base.
            final BalloonSmoothingTask.Step step = BalloonSmoothingTask.step(b.base, target, tick, b.phase, tuning);
            b.base = step.base();

            // Teleport dedup, mirroring BalloonSmoothingTask: skip a sub-visible step so the benchmark
            // does not overcount teleports the real path would have elided.
            final Location render = step.render();
            if (b.lastRendered == null
                    || b.lastRendered.getWorld() != render.getWorld()
                    || b.lastRendered.distanceSquared(render) > RENDER_EPSILON_SQUARED
                    || Math.abs(render.getYaw() - b.lastRendered.getYaw()) > SETTLED_YAW) {
                b.stand.teleport(render);
                b.lastRendered = render.clone();
            }

            // Movement tilt. An item balloon pays a head-pose metadata broadcast on a visible change - the
            // real cost production incurs and the old benchmark ignored entirely. A ModelEngine balloon
            // reads its own transform data and never the stand's head pose, so it is not posed (matching
            // UserBalloonManager.setTilt), which is why this is gated on !usedModelEngine.
            b.tiltPitch = MathUtil.lerp(b.tiltPitch, step.desiredPitch(), tuning.tiltFactor());
            b.tiltRoll = MathUtil.lerp(b.tiltRoll, step.desiredRoll(), tuning.tiltFactor());
            if (!usedModelEngine
                    && (Math.abs(b.tiltPitch - b.sentTiltPitch) >= TILT_EPSILON
                        || Math.abs(b.tiltRoll - b.sentTiltRoll) >= TILT_EPSILON)) {
                b.sentTiltPitch = b.tiltPitch;
                b.sentTiltRoll = b.tiltRoll;
                b.stand.setHeadPose(new EulerAngle(Math.toRadians(b.tiltPitch), 0, Math.toRadians(b.tiltRoll)));
            }
        }
        final long elapsed = System.nanoTime() - start;

        windowNanos += elapsed;
        windowSamples++;

        // Report roughly every 5 seconds of wall time.
        if (windowSamples * period >= 100) {
            final double avgMs = (windowNanos / windowSamples) / 1_000_000.0;
            final String line = String.format(
                    "[BalloonStressTest] %d balloons, period=%d: avg %.3f ms/update (%.2f%% of a 50ms tick budget)",
                    BALLOONS.size(), period, avgMs, avgMs / 50.0 * 100.0);
            Bukkit.getLogger().info(line);
            if (reportTo != null) reportTo.sendMessage(line);
            windowNanos = 0;
            windowSamples = 0;
        }
    }
}
