package com.hibiscusmc.hmccosmetics.cosmetic.types;

import com.hibiscusmc.hmccosmetics.config.Settings;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.user.manager.UserBalloonManager;
import com.hibiscusmc.hmccosmetics.util.packets.HMCCPacketManager;
import lombok.Getter;
import me.lojosho.shaded.configurate.ConfigurationNode;
import me.lojosho.shaded.configurate.serialize.SerializationException;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CosmeticBalloonType extends Cosmetic {
    private static final float STRING_ELASTICITY_FACTOR = 10f;
    private static final Vector BALLOON_BUOYANCY_FORCE = new Vector(0, 6.5f, 0);
    private static final float BALLOON_MASS = 1f;
    private static final float AIR_DAMPING = 0.85f; // 15% damping
    private static final float BALLOON_RADIUS = 1f;
    private static final float BALLOON_MOMENT_OF_INERTIA = 0.4f * BALLOON_MASS * BALLOON_RADIUS * BALLOON_RADIUS;
    private static final float RESTORE_CONSTANT = 40f;

    // The offset, in blocks, from the player's feet (player location) to the player's hand
    private static final Vector HAND_OFFSET = new Vector(0, 0.35, 0);

    @Getter
    private final String modelName;
    @Getter
    private List<String> dyableParts;
    @Getter
    private final boolean showLead;
    @Getter
    private Vector balloonOffset;

    private volatile long lastCall = -1;

    public CosmeticBalloonType(String id, ConfigurationNode config) {
        super(id, config);

        String modelId = config.node("model").getString();
        showLead = config.node("show-lead").getBoolean(true);

        ConfigurationNode balloonOffsetNode = config.node("balloon-offset");
        if (balloonOffsetNode.virtual())
            balloonOffset = Settings.getBalloonOffset();
        else
            balloonOffset = Settings.loadVector(balloonOffsetNode);

        try {
            if (!config.node("dyable-parts").virtual()) {
                dyableParts = config.node("dyable-parts").getList(String.class);
            }
        } catch (SerializationException e) {
            // Seriously?
            throw new RuntimeException(e);
        }
        if (modelId != null) modelId = modelId.toLowerCase(); // ME only accepts lowercase
        this.modelName = modelId;
    }

    @Override
    public void update(@NotNull CosmeticUser user) {
        // called every 1 second, or every tick (during player move)

        final long now = System.currentTimeMillis();
        final long elapsedMillis = lastCall == -1 ? 0 : now - lastCall;
        lastCall = now;

        Entity entity = Bukkit.getEntity(user.getUniqueId());
        UserBalloonManager userBalloonManager = user.getBalloonManager();

        if (entity == null || userBalloonManager == null) return;
        if (user.isInWardrobe()) return;

        if (!userBalloonManager.getModelEntity().isValid()) {
            user.respawnBalloon();
            return;
        }

        float deltaTime = elapsedMillis / 1000.0f;

        Location playerLocation = entity.getLocation();
        Location balloonLocation = userBalloonManager.getLocation();
        Vector equilibriumPosition = playerLocation.toVector().add(HAND_OFFSET);

        Vector displacement = balloonLocation.toVector().subtract(equilibriumPosition);

        // tension force (Hooke's Law: F = -k * x), with a little difference,
        // there is no tension if the balloon is closer to the player than the
        // equilibrium position
        Vector tensionForce;
        if (displacement.length() > balloonOffset.length()) {
            tensionForce = displacement.clone().multiply(-STRING_ELASTICITY_FACTOR);
        } else {
            tensionForce = new Vector(0, 0, 0);
        }

        Vector netForce = BALLOON_BUOYANCY_FORCE.clone().add(tensionForce);

        // acceleration (Newton’s Second Law: F = ma => a = F / m)
        Vector acceleration = netForce.multiply(1 / BALLOON_MASS);

        Vector velocity = userBalloonManager.getVelocity()
                .clone()
                .multiply(AIR_DAMPING) // damping
                .add(acceleration.multiply(deltaTime)); // apply acceleration

        Vector newBalloonPosition = balloonLocation.toVector().add(velocity.clone().multiply(deltaTime));

        Location newLocation = newBalloonPosition.toLocation(entity.getWorld());

        // rotate the balloon to face the player
        Vector direction = playerLocation.toVector().subtract(newLocation.toVector());
        double dx = direction.getX();
        double dz = direction.getZ();
        float newYaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90F;
        if (newYaw < -180F) newYaw += 360F;
        if (newYaw >= 180F) newYaw -= 360F;
        newLocation.setYaw(newYaw);

        //if (Settings.isBalloonHeadForward()) newLocation.setPitch(0);

        List<Player> viewer = HMCCPacketManager.getViewers(entity.getLocation());

        if (entity.getLocation().getWorld() != userBalloonManager.getLocation().getWorld()) {
            userBalloonManager.getModelEntity().teleport(newLocation);
            HMCCPacketManager.sendTeleportPacket(userBalloonManager.getPufferfishBalloonId(), newLocation, false, viewer);
            return;
        }

        userBalloonManager.setLocation(newLocation);
        userBalloonManager.setVelocity(velocity);

        // balloon rotation physics
        double angleBetweenTensionAndHorizontal = Math.atan2(tensionForce.getY(), tensionForce.length());

        // torque = r * sin(angle) * F
        double torqueMagnitude = BALLOON_RADIUS * tensionForce.length() * Math.sin(angleBetweenTensionAndHorizontal);
        double restoringTorque = -RESTORE_CONSTANT * userBalloonManager.getXRotation();
        double totalTorque = torqueMagnitude + restoringTorque;

        // torque = moment of inertia * angular acceleration
        double angularAcceleration = totalTorque / BALLOON_MOMENT_OF_INERTIA;

        double angularVelocity = userBalloonManager.getAngularPitchVelocity();
        angularVelocity += angularAcceleration * deltaTime;
        angularVelocity *= 0.8; // damping

        //double maxTiltAngle = Math.toRadians(30);
        double newRotation = userBalloonManager.getXRotation() + angularVelocity * deltaTime;
        //newRotation = Math.max(-maxTiltAngle, Math.min(maxTiltAngle, newRotation));

        userBalloonManager.setAngularPitchVelocity(angularVelocity);
        userBalloonManager.setXRotation(newRotation);

        HMCCPacketManager.sendTeleportPacket(userBalloonManager.getPufferfishBalloonId(), newLocation, false, viewer);
        HMCCPacketManager.sendLeashPacket(userBalloonManager.getPufferfishBalloonId(), entity.getEntityId(), viewer);
        if (user.isHidden()) {
            userBalloonManager.getPufferfish().hidePufferfish();
            return;
        }
        if (!user.isHidden() && showLead) {
            List<Player> sendTo = userBalloonManager.getPufferfish().refreshViewers(newLocation);
            if (sendTo.isEmpty()) return;
            user.getBalloonManager().getPufferfish().spawnPufferfish(newLocation, sendTo);
        }
    }

    public boolean isDyablePart(String name) {
        // If player does not define parts, dye whole model
        if (dyableParts == null) return true;
        if (dyableParts.isEmpty()) return true;
        return dyableParts.contains(name);
    }
}
