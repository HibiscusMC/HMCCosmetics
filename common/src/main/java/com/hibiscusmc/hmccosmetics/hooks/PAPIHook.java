package com.hibiscusmc.hmccosmetics.hooks;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.user.CosmeticUsers;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class PAPIHook extends PlaceholderExpansion {

    // TODO: Finish this

    @Override
    public @NotNull String getIdentifier() {
        return "HMCCosmetics";
    }

    @Override
    public @NotNull String getAuthor() {
        return "HibiscusMC";
    }

    @Override
    public @NotNull String getVersion() {
        return HMCCosmeticsPlugin.getInstance().getDescription().getVersion();
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        final String[] parts = params.split("_");
        if (parts.length == 0) return null;


        CosmeticUser user = CosmeticUsers.getUser(player.getUniqueId());
        if (user == null) return null;
        if (parts[0].equalsIgnoreCase("using")) {
            if (parts.length < 2) return null;
            final String id = this.getId(parts, 1);
        }
        return "";
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
