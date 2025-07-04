package com.hibiscusmc.hmccosmetics.util;

import com.hibiscusmc.hmccosmetics.config.Settings;
import me.lojosho.hibiscuscommons.util.packets.PacketManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class HMCCPlayerUtils {

    /**
     * Get nearby players. {@link com.hibiscusmc.hmccosmetics.util.packets.HMCCPacketManager#getViewers(Location)}
     * @param player
     * @return
     */
    @NotNull
    @Deprecated(since = "2.7.5", forRemoval = true)
    public static List<Player> getNearbyPlayers(@NotNull Player player) {
        return getNearbyPlayers(player.getLocation());
    }

    /**
     * Get nearby players. {@link com.hibiscusmc.hmccosmetics.util.packets.HMCCPacketManager#getViewers(Location)}
     * @param location
     * @return
     */
    @NotNull
    @Deprecated(since = "2.7.5", forRemoval = true)
    public static List<Player> getNearbyPlayers(@NotNull Location location) {
        return PacketManager.getViewers(location, Settings.getViewDistance());
    }
}
