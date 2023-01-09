package com.hibiscusmc.hmccosmetics.cosmetic.types;

import com.hibiscusmc.hmccosmetics.config.Settings;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.entities.BalloonEntity;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.util.PlayerUtils;
import com.hibiscusmc.hmccosmetics.util.packets.PacketManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.List;

public class CosmeticBalloonType extends Cosmetic {

    private String modelName;
    public CosmeticBalloonType(String id, ConfigurationNode config) {
        super(id, config);

        String modelId = config.node("model").getString();

        this.modelName = modelId;

        setDyable(false);
    }

    @Override
    public void update(CosmeticUser user) {
        Player player = Bukkit.getPlayer(user.getUniqueId());
        Location currentLocation = user.getBalloonEntity().getLocation().clone();
        Location newLocation = player.getLocation().clone().add(Settings.getBalloonOffset()).clone();
        if (player == null) return;
        if (user.isInWardrobe()) return;

        List<Player> viewer = PlayerUtils.getNearbyPlayers(player);
        viewer.add(player);

        BalloonEntity balloonEntity = user.getBalloonEntity();

        if (player.getLocation().getWorld() != balloonEntity.getLocation().getWorld()) {
            balloonEntity.getModelEntity().teleport(newLocation);
            PacketManager.sendTeleportPacket(balloonEntity.getPufferfishBalloonId(), newLocation, false, viewer);
            return;
        }

        //newLocation.add(player.getVelocity().clone().multiply(-1));
        Vector velocity = newLocation.clone().toVector().subtract(currentLocation.clone().toVector());
        balloonEntity.getModelEntity().setVelocity(velocity);
        balloonEntity.setLocation(newLocation);
        //balloonEntity.setVelocity(newLocation.clone().subtract(currentLocation.clone()).toVector());

        PacketManager.sendTeleportPacket(balloonEntity.getPufferfishBalloonId(), newLocation, false, viewer);
        if (!user.getHidden()) PacketManager.sendLeashPacket(balloonEntity.getPufferfishBalloonId(), player.getEntityId(), viewer);
    }

    public String getModelName() {
        return this.modelName;
    }
}
