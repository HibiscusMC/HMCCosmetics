package com.hibiscusmc.hmccosmetics.util.misc;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Provides useful String operations
 */
public final class StringUtils {
    public static @NotNull Component parse(@NotNull String parsed) {
        return Adventure.MINI_MESSAGE.deserialize(parsed);
    }

    public static @NotNull String parseToString(@NotNull String parsed) {
        return Adventure.SERIALIZER.serialize(parse(parsed));
    }

    @Deprecated
    public static @NotNull String parseStringToString(final String parsed) {
        return Adventure.SERIALIZER.serialize(Adventure.MINI_MESSAGE.deserialize(parsed));
    }

    public static @NotNull String formatArmorItemType(@NotNull String type) {
        type = type.toLowerCase();
        final String[] parts = type.split(" ");

        final String firstPart = parts[0].substring(0, 1).toUpperCase() + parts[0].substring(1);

        if (parts.length == 1) {
            return firstPart;
        }

        return firstPart + parts[1].substring(0, 1).toUpperCase() + parts[1].substring(1);
    }
}
