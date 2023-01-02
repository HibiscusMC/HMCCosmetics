package com.hibiscusmc.hmccosmetics.util;

import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.WrappedSignedProperty;
import com.hibiscusmc.hmccosmetics.config.Settings;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class PlayerUtils {

    public static WrappedSignedProperty getSkin(Player player) {
        WrappedSignedProperty skinData = WrappedGameProfile.fromPlayer(player).getProperties()
                .get("textures").stream().findAny().orElse(null);

        if (skinData == null) {
            return null;
        }
        return new WrappedSignedProperty("textures", skinData.getValue(), skinData.getSignature());
    }

    public static List<Player> getNearbyPlayers(Player player) {
        return getNearbyPlayers(player.getLocation());
    }

    public static List<Player> getNearbyPlayers(Location location) {
        List<Player> players = new ArrayList<>();
        int viewDistance = Settings.getViewDistance();
        for (Entity entity : location.getWorld().getNearbyEntities(location, viewDistance, viewDistance, viewDistance)) {
            if (entity instanceof Player) {
                players.add((Player) entity);
            }
        }
        return players;
    }
}
