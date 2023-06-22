package com.hibiscusmc.hmccosmetics.cosmetic.types;

import com.hibiscusmc.hmccosmetics.config.Settings;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.user.manager.UserBalloonManager;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.util.packets.PacketManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.List;

public class CosmeticBalloonType extends Cosmetic {

    private final String modelName;
    private List<String> dyableParts;
    private boolean showLead;

    public CosmeticBalloonType(String id, ConfigurationNode config) {
        super(id, config);

        String modelId = config.node("model").getString();

        showLead = config.node("show-lead").getBoolean(true);

        try {
            if (!config.node("dyable-parts").virtual()) {
                dyableParts = config.node("dyable-parts").getList(String.class);
            }
        } catch (SerializationException e) {
            // Seriously?
            throw new RuntimeException(e);
        }

        this.modelName = modelId;
    }

    @Override
    public void update(@NotNull CosmeticUser user) {
        Entity entity = Bukkit.getEntity(user.getUniqueId());
        UserBalloonManager userBalloonManager = user.getBalloonManager();

        if (entity == null || userBalloonManager == null) return;
        if (user.isInWardrobe()) return;

        if (!userBalloonManager.getModelEntity().isValid()) {
            user.respawnBalloon();
            return;
        }

        Location newLocation = entity.getLocation();
        Location currentLocation = user.getBalloonManager().getLocation();
        newLocation = newLocation.clone().add(Settings.getBalloonOffset());

        List<Player> viewer = PacketManager.getViewers(entity.getLocation());

        if (entity.getLocation().getWorld() != userBalloonManager.getLocation().getWorld()) {
            userBalloonManager.getModelEntity().teleport(newLocation);
            PacketManager.sendTeleportPacket(userBalloonManager.getPufferfishBalloonId(), newLocation, false, viewer);
            return;
        }

        Vector velocity = newLocation.toVector().subtract(currentLocation.toVector());
        userBalloonManager.setVelocity(velocity.multiply(1.1));
        userBalloonManager.setLocation(newLocation);

        PacketManager.sendTeleportPacket(userBalloonManager.getPufferfishBalloonId(), newLocation, false, viewer);
        if (!user.getHidden() && showLead) {
            List<Player> sendTo = userBalloonManager.getPufferfish().refreshViewers(newLocation);
            PacketManager.sendEntitySpawnPacket(newLocation, userBalloonManager.getPufferfishBalloonId(), EntityType.PUFFERFISH, userBalloonManager.getPufferfishBalloonUniqueId(), sendTo);
            PacketManager.sendInvisibilityPacket(userBalloonManager.getPufferfishBalloonId(), sendTo);
            PacketManager.sendLeashPacket(userBalloonManager.getPufferfishBalloonId(), entity.getEntityId(), viewer);
        }
    }

    public String getModelName() {
        return this.modelName;
    }

    public List<String> getDyableParts() {
        return dyableParts;
    }

    public boolean isDyablePart(String name) {
        // If player does not define parts, dye whole model
        if (dyableParts == null) return true;
        if (dyableParts.isEmpty()) return true;
        return dyableParts.contains(name);
    }

    public boolean isShowLead() {
        return showLead;
    }
}
