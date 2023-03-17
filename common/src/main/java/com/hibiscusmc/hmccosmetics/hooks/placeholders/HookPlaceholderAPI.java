package com.hibiscusmc.hmccosmetics.hooks.placeholders;

import com.hibiscusmc.hmccosmetics.hooks.Hook;

/**
 * A hook that integrates the plugin {@link me.clip.placeholderapi.PlaceholderAPI PlaceholderAPI}
 */
public class HookPlaceholderAPI extends Hook {
    public HookPlaceholderAPI() {
        super("PlaceholderAPI");
    }

    /**
     * @throws RuntimeException If PlaceholderAPI fails to register
     */
    @Override
    public void load() throws RuntimeException {
        if (!new HMCPlaceholderExpansion().register())
            throw new RuntimeException("Failed to register PlaceholderExpansion");
    }
}
