package com.hibiscusmc.hmccosmetics.cosmetic.types;

import com.hibiscusmc.hmccosmetics.config.Settings;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.util.PlayerUtils;
import com.hibiscusmc.hmccosmetics.util.packets.PacketManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.List;

public class CosmeticBalloonType extends Cosmetic {

    private String modelName;
    public CosmeticBalloonType(String id, ConfigurationNode config) {
        super(id, config);

        String modelId = config.node("model").getString();

        this.modelName = modelId;
    }

    @Override
    public void update(CosmeticUser user) {
        Player player = Bukkit.getPlayer(user.getUniqueId());

        if (user.isInWardrobe()) return;

        final Location actual = player.getLocation().clone().add(Settings.getBalloonOffset());

        if (player.getLocation().getWorld() != user.getBalloonEntity().getLocation().getWorld()) {
            user.getBalloonEntity().getModelEntity().getBukkitLivingEntity().teleport(actual);
        }

        user.getBalloonEntity().getModelEntity().moveTo(actual.getX(), actual.getY(), actual.getZ());

        List<Player> viewer = PlayerUtils.getNearbyPlayers(player);
        viewer.add(player);

        PacketManager.sendTeleportPacket(user.getBalloonEntity().getPufferfishBalloonId(), actual, false, viewer);
        PacketManager.sendLeashPacket(user.getBalloonEntity().getPufferfishBalloonId(), player.getEntityId(), viewer);
    }

    public String getModelName() {
        return this.modelName;
    }
}
