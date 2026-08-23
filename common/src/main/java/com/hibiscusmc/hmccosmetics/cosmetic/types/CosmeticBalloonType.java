package com.hibiscusmc.hmccosmetics.cosmetic.types;

import com.hibiscusmc.hmccosmetics.config.Settings;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.cosmetic.behavior.CosmeticUpdateBehavior;
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

// Balloons deliberately do not implement CosmeticMovementBehavior: BalloonSmoothingTask owns their
// position and runs far more often than move events fire, so a move handler has nothing left to do.
public class CosmeticBalloonType extends Cosmetic implements CosmeticUpdateBehavior {

    @Getter
    private final String modelName;
    @Getter
    private List<String> dyeableParts;
    @Getter
    private final boolean showLead;
    @Getter
    private Vector balloonOffset;

    public CosmeticBalloonType(String id, ConfigurationNode config) {
        super(id, config);

        String modelId = config.node("model").getString();
        showLead = config.node("show-lead").getBoolean(Settings.isBalloonDefaultShowLead());

        ConfigurationNode balloonOffsetNode = config.node("balloon-offset");
        if (balloonOffsetNode.virtual())
            balloonOffset = Settings.getBalloonOffset();
        else
            balloonOffset = Settings.loadVector(balloonOffsetNode);

        try {
            if (!config.node("dyeable-parts").virtual()) {
                dyeableParts = config.node("dyeable-parts").getList(String.class);
            }
        } catch (SerializationException e) {
            // Seriously?
            throw new RuntimeException(e);
        }
        if (modelId != null) modelId = modelId.toLowerCase(); // ME only accepts lowercase
        this.modelName = modelId;
    }

    @Override
    public void dispatchUpdate(@NotNull CosmeticUser user) {
        Entity entity = Bukkit.getEntity(user.getUniqueId());
        UserBalloonManager userBalloonManager = user.getBalloonManager();

        if (entity == null || userBalloonManager == null) return;
        if (user.isInWardrobe()) return;

        if (!userBalloonManager.getModelEntity().isValid()) {
            user.respawnBalloon();
            return;
        }

        Location newLocation = entity.getLocation();
        newLocation = newLocation.clone().add(getBalloonOffset());
        if (Settings.isBalloonHeadForward()) newLocation.setPitch(0);

        // Smoothing task is off (balloon-lerp-period <= 0): it is the only thing that moves the balloon
        // AND its lead, so drive both from this low-frequency tick instead of leaving them frozen at spawn.
        // Gated on <= 0 so it never fights the smoothing task while that is running.
        if (Settings.getBalloonLerpPeriod() <= 0) {
            userBalloonManager.snapTo(newLocation);
            // snapTo only moves the model entity; the lead is anchored to the pufferfish, which the
            // smoothing task would normally teleport. Move it here for existing viewers so the lead follows.
            if (!userBalloonManager.getPufferfish().getViewers().isEmpty()) {
                userBalloonManager.getPufferfish().teleport(newLocation);
            }
        }

        if (!user.isHidden() && showLead) {
            List<Player> sendTo = userBalloonManager.getPufferfish().refreshViewers(newLocation);
            if (sendTo.isEmpty()) return;
            user.getBalloonManager().getPufferfish().spawnPufferfish(newLocation, sendTo);
            HMCCPacketManager.sendLeashPacket(userBalloonManager.getPufferfishBalloonId(), entity.getEntityId(), sendTo);
        }
    }

    public boolean isDyeablePart(String name) {
        // If player does not define parts, dye whole model
        if (dyeableParts == null) return true;
        if (dyeableParts.isEmpty()) return true;
        return dyeableParts.contains(name);
    }
}
