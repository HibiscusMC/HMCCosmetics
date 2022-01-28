package io.github.fisher2911.hmccosmetics.util;

import io.github.fisher2911.hmccosmetics.HMCCosmetics;
import io.github.fisher2911.hmccosmetics.hook.HookManager;
import io.github.fisher2911.hmccosmetics.message.Adventure;
import io.github.fisher2911.hmccosmetics.hook.PAPIHook;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Map;

public class StringUtils {

    private static final HMCCosmetics plugin;

    static {
        plugin = HMCCosmetics.getPlugin(HMCCosmetics.class);
    }

    /**
     * @param parsed message to be parsed
     * @return MiniMessage parsed string
     */

    public static Component parse(final String parsed) {
        return Adventure.MINI_MESSAGE.parse(parsed);
    }

    public static String parseStringToString(final String parsed) {
        return Adventure.SERIALIZER.serialize(Adventure.MINI_MESSAGE.parse(parsed));
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
