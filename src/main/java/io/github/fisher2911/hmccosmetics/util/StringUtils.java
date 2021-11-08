package io.github.fisher2911.hmccosmetics.util;

import io.github.fisher2911.hmccosmetics.message.Adventure;
import net.kyori.adventure.text.Component;

import java.util.Map;

public class StringUtils {

    /**
     *
     * @param message message being translated
     * @param placeholders placeholders applied
     * @return message with placeholders applied
     */

    public static String applyPlaceholders(String message, final Map<String, String> placeholders) {
        for (final Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace(entry.getKey(), entry.getValue());
        }
        return message;
    }


    /**
     *
     * @param parsed message to be parsed
     * @return MiniMessage parsed string
     */

    public static Component parse(final String parsed) {
        return Adventure.MINI_MESSAGE.parse(parsed);
    }

    public static String parseStringToString(final String parsed) {
        return Adventure.SERIALIZER.serialize(Adventure.MINI_MESSAGE.parse(parsed));
    }
}
