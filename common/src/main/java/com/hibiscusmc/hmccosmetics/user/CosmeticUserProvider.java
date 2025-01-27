package com.hibiscusmc.hmccosmetics.user;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Allow custom implementations of a {@link CosmeticUser}.
 */
public abstract class CosmeticUserProvider {
    /**
     * Construct the custom {@link CosmeticUser}.
     * @param playerId the player uuid
     * @return the {@link CosmeticUser}
     * @apiNote This method is called during the {@link PlayerJoinEvent}.
     */
    public abstract @NotNull CosmeticUser createCosmeticUser(@NotNull UUID playerId);

    /**
     * Represents the plugin that is providing this {@link CosmeticUserProvider}
     * @return the plugin
     */
    public abstract Plugin getProviderPlugin();

    /**
     * Default implementation.
     */
    static class Default extends CosmeticUserProvider {
        public static CosmeticUserProvider INSTANCE = new Default();

        @Override
        public @NotNull CosmeticUser createCosmeticUser(@NotNull UUID playerId) {
            return new CosmeticUser(playerId);
        }

        @Override
        public Plugin getProviderPlugin() {
            return HMCCosmeticsPlugin.getInstance();
        }
    }
}