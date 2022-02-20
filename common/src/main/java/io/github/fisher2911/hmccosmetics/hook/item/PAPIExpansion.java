package io.github.fisher2911.hmccosmetics.hook.item;

import io.github.fisher2911.hmccosmetics.HMCCosmetics;
import io.github.fisher2911.hmccosmetics.api.CosmeticItem;
import io.github.fisher2911.hmccosmetics.gui.ArmorItem;
import io.github.fisher2911.hmccosmetics.message.Placeholder;
import io.github.fisher2911.hmccosmetics.message.Translation;
import io.github.fisher2911.hmccosmetics.user.User;
import io.github.fisher2911.hmccosmetics.user.UserManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class PAPIExpansion extends PlaceholderExpansion {

    private final HMCCosmetics plugin;
    private final UserManager userManager;
    private static final String IDENTIFIER = "hmccosmetics";
    private static final String AUTHOR = "MasterOfTheFish";

    public PAPIExpansion(final HMCCosmetics plugin) {
        this.plugin = plugin;
        this.userManager = this.plugin.getUserManager();
    }

    @Override
    public @NotNull String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public @NotNull String getAuthor() {
        return AUTHOR;
    }

    @Override
    public @NotNull String getVersion() {
        return this.plugin.getDescription().getVersion();
    }

    @Override
    public @Nullable String onPlaceholderRequest(final Player player, @NotNull final String params) {
        final String[] parts = params.split("_");
        if (parts.length == 0) {
            return null;
        }

        final Optional<User> optionalUser = this.userManager.get(player.getUniqueId());

        if (optionalUser.isEmpty()) return null;
        final User user = optionalUser.get();

        // %hmccosmetics_using_id%
        if (parts[0].equalsIgnoreCase("using")) {
            if (parts.length < 2) return null;
            final String id = this.getId(parts, 1);
            for (final ArmorItem item : user.getPlayerArmor().getArmorItems()) {
                if (item.getId().equals(id)) return Translation.translate(Translation.TRUE);
            }
            return Translation.translate(Translation.FALSE);
        }

        // %hmccosmetics_current_type%
        if (parts[0].startsWith("current")) {
            final boolean formatted = parts[0].contains("formatted");
            if (parts.length >= 2) {
                final String typeStr = getId(parts, 1);
                try {
                    final ArmorItem.Type type = ArmorItem.Type.valueOf(typeStr.toUpperCase());
                    for (final ArmorItem item : user.getPlayerArmor().getArmorItems()) {
                        if (item.getType().equals(type)) {
                            if (formatted) {
                                final String name = item.getItemName();
                                if (name.isBlank()) return item.getId().replace("_", "");
                                return name;
                            }
                            return item.getId();
                        }
                    }
                    return Translation.translate(Translation.NONE);
                } catch (final IllegalArgumentException exception) {
                    return null;
                }
            }
            return null;
        }

        if (parts[0].equals("wardrobe-enabled")) {
            return Translation.translate(String.valueOf(user.getWardrobe().isActive()));
        }

        return null;
    }

    private String getId(final String[] parts, final int fromIndex) {
        final StringBuilder builder = new StringBuilder();
        for (int i = fromIndex; i < parts.length; i++) {
            builder.append(parts[i]);
            if (i < parts.length - 1) builder.append("_");
        }

        return builder.toString();
    }
}
