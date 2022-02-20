package io.github.fisher2911.hmccosmetics.message;

import io.github.fisher2911.hmccosmetics.hook.HookManager;
import io.github.fisher2911.hmccosmetics.hook.PAPIHook;
import java.util.Map;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class Placeholder {

    public static final String PREFIX = "%prefix%";
    public static final String TYPE = "%type%";
    public static final String ITEM = "%item%";
    public static final String FILE = "%file%";

    public static final String PLAYER = "%player%";
    public static final String ENABLED = "%enabled%";
    public static final String ALLOWED = "%allowed%";
    public static final String ID = "%id%";

    /**
     * @param message message being translated
     * @param placeholders placeholders applied
     * @return message with placeholders applied
     */

    public static String applyPlaceholders(String message, final Map<String, String> placeholders) {
        for (final Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace(entry.getKey(), Translation.translate(entry.getValue()));
        }
        return message;
    }

    public static String applyPapiPlaceholders(@Nullable final Player player,
            final String message) {
        if (HookManager.getInstance().isEnabled(PAPIHook.class)) {
            return HookManager.getInstance().getPapiHook().parse(player, message);
        }

        return message;
    }

}
