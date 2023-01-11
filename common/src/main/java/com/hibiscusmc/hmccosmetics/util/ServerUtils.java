package com.hibiscusmc.hmccosmetics.util;

import com.hibiscusmc.hmccosmetics.nms.NMSHandlers;
import org.bukkit.Color;
import org.bukkit.GameMode;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServerUtils {

    private static String COLOR_CHAR = "&";

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

    public static Color hex2Rgb(String colorStr) {
        try {
            return Color.fromRGB(
                    Integer.valueOf(colorStr.substring(1, 3), 16),
                    Integer.valueOf(colorStr.substring(3, 5), 16),
                    Integer.valueOf(colorStr.substring(5, 7), 16));
        } catch (StringIndexOutOfBoundsException e) {
            return null;
        }
    }
}
