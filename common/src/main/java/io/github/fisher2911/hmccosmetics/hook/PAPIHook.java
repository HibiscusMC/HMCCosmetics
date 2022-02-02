package io.github.fisher2911.hmccosmetics.hook;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

public class PAPIHook implements Hook {

    private static final String ID = "PAPI";

    @Override
    public String getId() {
        return ID;
    }

    public String parse(final Player player, final String string) {
        return PlaceholderAPI.setPlaceholders(player, string);
    }

}
