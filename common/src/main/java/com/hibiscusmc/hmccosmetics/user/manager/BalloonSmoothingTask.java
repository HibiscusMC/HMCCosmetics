package com.hibiscusmc.hmccosmetics.user.manager;

import com.hibiscusmc.hmccosmetics.config.Settings;
import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticSlot;
import com.hibiscusmc.hmccosmetics.cosmetic.types.CosmeticBalloonType;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.user.CosmeticUsers;
import com.hibiscusmc.hmccosmetics.util.MathUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Drives smooth, high-frequency position lerping and movement-based tilt for balloon cosmetics,
 * independent of the much slower general {@link CosmeticUser} tick heartbeat.
 * <p>
 * Balloons that have caught up with their owner and have no idle animation configured short-circuit
 * before doing any work, so a server full of standing-still players costs a walk over the user map and
 * nothing else.
 */
public class BalloonSmoothingTask implements Runnable {

    private static final double TWO_PI = Math.PI * 2;
    /** Follow lag below this is under the position quantisation of a teleport, so nothing would move. */
    private static final double SETTLED_DISTANCE_SQUARED = 1.0E-6;
    /** Residual yaw step below this is not worth a position update. Degrees. */
    private static final double SETTLED_YAW = 0.05;
    /** Residual tilt below this is invisible, so the tilt lerp is allowed to stop here. Degrees. */
    private static final double SETTLED_TILT = 0.05;
    /** Squared follow lag past which the balloon is teleported outright instead of lerped. */
    private static final double SNAP_DISTANCE_SQUARED = 8 * 8;
    /** Squared follow lag by which the idle yaw wander has fully faded out. */
    private static final double IDLE_YAW_FADE_DISTANCE_SQUARED = 0.05 * 0.05;
    /** Squared follow lag above which the balloon is considered to be travelling, not drifting. */
    private static final double MOVING_DISTANCE_SQUARED = 0.02 * 0.02;
    /** Render step below this (0.001 blocks) is invisible, so the entity teleport can be skipped. */
    private static final double RENDER_EPSILON_SQUARED = 0.001 * 0.001;

    private Plugin plugin;
    private BukkitTask task;
    private long tick;
    private int period = 1;
    // Users whose smoothing has already thrown once. Keeps a persistently broken balloon from dumping a
    // stack trace twenty times a second.
    private final Set<UUID> failed = new HashSet<>();

    public void start(Plugin plugin) {
        stop();
        this.plugin = plugin;
        // Read the period once, here: run() has to agree with the rate the scheduler was actually given,
        // and a reload that changes it goes back through start().
        this.period = Settings.getBalloonLerpPeriod();
        if (this.period <= 0) return;

        task = Bukkit.getScheduler().runTaskTimer(plugin, this, 0L, this.period);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        failed.clear();
    }

    @Override
    public void run() {
        // Advance the oscillator time base by the task period so idle animations run at the same
        // real-time speed regardless of balloon-lerp-period.
        tick += period;

        final Tuning tuning = Tuning.snapshot(period);

        for (CosmeticUser user : CosmeticUsers.balloonView()) {
            try {
                smooth(user, tuning);
            } catch (Exception e) {
                // Without this, one broken balloon starves every user after it in iteration order - the
                // same victims, every tick.
                if (failed.add(user.getUniqueId())) {
                    plugin.getLogger().log(Level.WARNING, "Balloon smoothing failed for " + user.getUniqueId()
                            + ", suppressing further reports for this player", e);
                }
            }
        }
    }

    private void smooth(CosmeticUser user, Tuning tuning) {
        if (!user.isBalloonSpawned() || user.isHidden() || user.isInWardrobe()) return;

        UserBalloonManager balloonManager = user.getBalloonManager();
        Entity entity = user.getEntity();
        if (entity == null || balloonManager == null) return;
        if (!balloonManager.getModelEntity().isValid()) return;

        if (!(user.getCosmetic(CosmeticSlot.BALLOON) instanceof CosmeticBalloonType cosmeticBalloonType)) return;

        Location target = entity.getLocation().add(cosmeticBalloonType.getBalloonOffset());
        if (tuning.headForward) target.setPitch(0);

        Location current = balloonManager.getSmoothedBase();
        if (current.getWorld() != target.getWorld()) {
            // Lerping across a dimension change is meaningless - the balloon has to arrive with the player.
            balloonManager.snapTo(target);
            return;
        }

        double dx = target.getX() - current.getX();
        double dy = target.getY() - current.getY();
        double dz = target.getZ() - current.getZ();
        double horizontalLagSq = dx * dx + dz * dz;

        if (horizontalLagSq + dy * dy > SNAP_DISTANCE_SQUARED) {
            // A long teleport would otherwise walk a real entity through a handful of intermediate
            // positions, force-loading chunks nobody is near, before the respawn in PlayerGameListener
            // catches up four ticks later.
            balloonManager.snapTo(target);
            return;
        }

        double currentYaw = current.getYaw();
        double desiredYaw = horizontalLagSq > MOVING_DISTANCE_SQUARED
                ? Math.toDegrees(Math.atan2(-dx, dz))
                : currentYaw;
        double newYaw = MathUtil.lerpAngle(currentYaw, desiredYaw, tuning.yawFactor);

        // Nothing to send once the balloon has caught up and no oscillator is running.
        if (!tuning.animated
                && horizontalLagSq + dy * dy < SETTLED_DISTANCE_SQUARED
                && Math.abs(newYaw - currentYaw) < SETTLED_YAW
                && Math.abs(balloonManager.getTiltPitch()) < SETTLED_TILT
                && Math.abs(balloonManager.getTiltRoll()) < SETTLED_TILT) {
            return;
        }

        Location base = MathUtil.lerpLocation(current, target, tuning.positionFactor, tuning.verticalFactor);
        base.setYaw((float) newYaw);
        balloonManager.setSmoothedBase(base);

        // Idle life: per-user phase offset so nearby balloons don't animate in lockstep.
        double phase = (user.getUniqueId().hashCode() & 0xFFFF) * TWO_PI / 0x10000;
        double bob = tuning.bobAmplitude * Math.sin(TWO_PI * tick / tuning.bobPeriod + phase);
        double swayTheta = TWO_PI * tick / tuning.swayPeriod + phase;
        double swayPitch = tuning.swayAngle * Math.sin(swayTheta);
        double swayRoll = tuning.swayAngle * Math.cos(swayTheta); // cos: circular pendulum swing, not a diagonal line

        // The wander is an idle behaviour, and it also rotates the frame the follow lag is decomposed in
        // below - left alone it would bleed forward lean into the roll channel at speed. Fade it out as
        // soon as the balloon is actually travelling.
        double idleYaw = tuning.idleYawAngle == 0 ? 0
                : tuning.idleYawAngle
                    * (1 - Math.min(1, horizontalLagSq / IDLE_YAW_FADE_DISTANCE_SQUARED))
                    * Math.sin(TWO_PI * tick / tuning.idleYawPeriod + phase);

        double renderYaw = newYaw + idleYaw;

        // Display-only offsets, kept off `base` so the bob and the wander never feed back into the
        // follow-lerp and compound tick over tick.
        // Rope drag: the balloon rides lower the further it is lagging behind its owner horizontally, and
        // eases back up as that lag decays on stop. Display-only, kept off `base` like the bob so it never
        // feeds back into the follow-lerp. Clamped so a sprint or teleport catch-up can't drag it into the floor.
        double dip = Math.min(tuning.sagFactor * Math.sqrt(horizontalLagSq), tuning.maxSag);
        Location render = base.clone();
        render.setY(render.getY() + bob - dip);
        render.setYaw((float) renderYaw);

        // The head pose is applied in the armor stand's own frame, whose yaw is renderYaw (movement-driven),
        // not the player's. Decomposing the follow-lag in any other frame rotates the lean by the difference.
        double yawRad = Math.toRadians(renderYaw);
        double fwdX = -Math.sin(yawRad);
        double fwdZ = Math.cos(yawRad);
        // Right of forward in Minecraft's frame: at yaw 0 the balloon faces +Z (south), so right is -X (west).
        double rightX = -fwdZ;
        double rightZ = fwdX;

        double forwardLag = dx * fwdX + dz * fwdZ;
        double sideLag = dx * rightX + dz * rightZ;

        double maxTilt = tuning.maxTilt;
        // Sway is added after the clamp so the idle swing is never eaten by the movement-tilt cap.
        double desiredPitch = Math.clamp(-forwardLag * tuning.tiltForwardFactor, -maxTilt, maxTilt) + swayPitch;
        double desiredRoll = Math.clamp(-sideLag * tuning.tiltSideFactor, -maxTilt, maxTilt) + swayRoll;

        // Only teleport the entity when the rendered position actually changed by a visible amount. The
        // tail of a lerp (and an idle balloon whose only motion is a settling tilt) otherwise pays a full
        // entity teleport + tracker broadcast every tick for a sub-millimetre step. Tilt still updates via
        // setTilt below, which self-dedups its own metadata packet.
        Location lastRendered = balloonManager.getLastRendered();
        boolean rendered = lastRendered == null
                || lastRendered.getWorld() != render.getWorld()
                || lastRendered.distanceSquared(render) > RENDER_EPSILON_SQUARED
                || Math.abs(render.getYaw() - lastRendered.getYaw()) > SETTLED_YAW;

        if (rendered) {
            balloonManager.setLocation(render);
            balloonManager.setLastRendered(render.clone());

            // The armor stand is broadcast by the vanilla entity tracker; the only packet this task owes is
            // for the pufferfish the lead is tied to. Reusing the entity's own viewer list avoids re-deriving
            // it, and leaves balloons with show-lead off - whose pufferfish is never spawned client-side - free.
            UserBalloonPufferfish pufferfish = balloonManager.getPufferfish();
            if (!pufferfish.getViewers().isEmpty()) pufferfish.teleport(render);
        }

        balloonManager.setTilt(
                MathUtil.lerp(balloonManager.getTiltPitch(), desiredPitch, tuning.tiltFactor),
                MathUtil.lerp(balloonManager.getTiltRoll(), desiredRoll, tuning.tiltFactor)
        );
    }

    /**
     * The configuration this run is using, resolved once instead of per balloon.
     */
    private record Tuning(
            boolean headForward,
            double positionFactor,
            double verticalFactor,
            double yawFactor,
            double tiltFactor,
            double maxTilt,
            double tiltForwardFactor,
            double tiltSideFactor,
            double bobAmplitude,
            int bobPeriod,
            double swayAngle,
            int swayPeriod,
            double idleYawAngle,
            int idleYawPeriod,
            double sagFactor,
            double maxSag,
            boolean animated
    ) {

        static Tuning snapshot(int period) {
            double bobAmplitude = Settings.getBalloonBobAmplitude();
            double swayAngle = Settings.getBalloonSwayAngle();
            double idleYawAngle = Settings.getBalloonIdleYawAngle();

            return new Tuning(
                    Settings.isBalloonHeadForward(),
                    perPeriod(Settings.getBalloonPositionLerpFactor(), period),
                    perPeriod(Settings.getBalloonVerticalLerpFactor(), period),
                    perPeriod(Settings.getBalloonYawLerpFactor(), period),
                    perPeriod(Settings.getBalloonTiltLerpFactor(), period),
                    Settings.getBalloonMaxTiltAngle(),
                    Settings.getBalloonTiltForwardFactor(),
                    Settings.getBalloonTiltSideFactor(),
                    bobAmplitude,
                    Settings.getBalloonBobPeriod(),
                    swayAngle,
                    Settings.getBalloonSwayPeriod(),
                    idleYawAngle,
                    Settings.getBalloonIdleYawPeriod(),
                    Settings.getBalloonSagFactor(),
                    Settings.getBalloonMaxSag(),
                    bobAmplitude != 0 || swayAngle != 0 || idleYawAngle != 0
            );
        }

        /**
         * Converts a factor expressed as "fraction of the remaining gap closed per tick" into the
         * equivalent for a run that only happens every {@code period} ticks, so raising the period trades
         * smoothness for cost without also changing how closely the balloon follows.
         */
        private static double perPeriod(double factor, int period) {
            if (period <= 1) return factor;
            return 1 - Math.pow(1 - factor, period);
        }
    }
}
