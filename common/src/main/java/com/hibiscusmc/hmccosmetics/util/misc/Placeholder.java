package com.hibiscusmc.hmccosmetics.util.misc;

import com.hibiscusmc.hmccosmetics.util.TranslationUtil;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

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
     * @return message with placeholders applied
     */

    public static String applyPapiPlaceholders(@Nullable final Player player,
                                               final String message) {
        /*
        if (HookManager.getInstance().isEnabled(PAPIHook.class)) {
            return HookManager.getInstance().getPapiHook().parse(player, message);
        }
         */

        return message;
    }

}
