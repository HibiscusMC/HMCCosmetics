package io.github.fisher2911.hmccosmetics.papi;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

public class PAPIHook {

    public static String parse(final Player player, final String string) {
        return PlaceholderAPI.setPlaceholders(player, string);
    }

}
