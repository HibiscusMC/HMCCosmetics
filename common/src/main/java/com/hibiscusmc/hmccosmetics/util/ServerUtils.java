package com.hibiscusmc.hmccosmetics.util;

import com.hibiscusmc.hmccosmetics.nms.NMSHandlers;
import org.bukkit.GameMode;

public class ServerUtils {

    /**
     * Converts a bukkit gamemode into an integer for use in packets
     * @param gamemode Bukkit gamemode to convert.
     * @return int of the gamemode
     */
    public static int convertGamemode(final GameMode gamemode) {
        return switch (gamemode) {
            case SURVIVAL -> 0;
            case CREATIVE -> 1;
            case ADVENTURE -> 2;
            case SPECTATOR -> 3;
        };
    }

    public static org.bukkit.entity.Entity getEntity(int entityId) {
        return NMSHandlers.getHandler().getEntity(entityId);
    }
}
