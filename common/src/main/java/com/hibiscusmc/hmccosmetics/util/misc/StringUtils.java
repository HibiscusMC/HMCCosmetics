package com.hibiscusmc.hmccosmetics.util.misc;

import net.kyori.adventure.text.Component;

public class StringUtils {

    /**
     * @param parsed message to be parsed
     * @return MiniMessage parsed string
     */

    public static Component parse(final String parsed) {
        return Adventure.MINI_MESSAGE.deserialize(parsed);
    }

    public static String parseStringToString(final String parsed) {
        return Adventure.SERIALIZER.serialize(Adventure.MINI_MESSAGE.deserialize(parsed));
    }

    public static String formatArmorItemType(String type) {
        type = type.toLowerCase();
        final String[] parts = type.split(" ");

        final String firstPart = parts[0].substring(0, 1).toUpperCase() + parts[0].substring(1);

        if (parts.length == 1) {
            return firstPart;
        }

        return firstPart + parts[1].substring(0, 1).toUpperCase() + parts[1].substring(1);
    }

}
