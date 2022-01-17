package io.github.fisher2911.hmccosmetics.util;

import io.github.fisher2911.hmccosmetics.HMCCosmetics;
import io.github.fisher2911.hmccosmetics.hook.HookManager;
import io.github.fisher2911.hmccosmetics.message.Adventure;
import io.github.fisher2911.hmccosmetics.hook.PAPIHook;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class StringUtils {

    private static final HMCCosmetics plugin;

    static {
        plugin = HMCCosmetics.getPlugin(HMCCosmetics.class);
    }

    /**
     * @param message      message being translated
     * @param placeholders placeholders applied
     * @return message with placeholders applied
     */

    public static String applyPlaceholders(String message, final Map<String, String> placeholders) {
        for (final Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace(entry.getKey(), entry.getValue());
        }
        return message;
    }

    public static String applyPapiPlaceholders(@Nullable final Player player, final String message) {
        if (HookManager.getInstance().isEnabled(PAPIHook.class)) {
            return HookManager.getInstance().getPapiHook().parse(player, message);
        }

        return message;
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
}
