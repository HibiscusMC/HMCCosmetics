package com.hibiscusmc.hmccosmetics.util.misc;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public class StringUtils {

    /**
     * @param parsed message to be parsed
     * @return MiniMessage parsed string
     */
    @NotNull
    public static Component parse(final String parsed) {
        return Adventure.MINI_MESSAGE.deserialize(parsed);
    }

    @NotNull
    public static String parseStringToString(final String parsed) {
        return Adventure.SERIALIZER.serialize(Adventure.MINI_MESSAGE.deserialize(parsed));
    }
}
