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
     * Registers HMCCosmetics Placeholder Expansion
     */
    @Override
    public void load() {
        new HMCPlaceholderExpansion().register();
    }
}
