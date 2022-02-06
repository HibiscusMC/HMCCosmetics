package io.github.fisher2911.hmccosmetics.config;

import io.github.fisher2911.hmccosmetics.HMCCosmetics;

public class Settings {

    private final HMCCosmetics plugin;
    private final CosmeticSettings cosmeticSettings;
    private final WardrobeSettings wardrobeSettings;

    public Settings(final HMCCosmetics plugin) {
        this.plugin = plugin;
        this.cosmeticSettings = new CosmeticSettings();
        this.wardrobeSettings = new WardrobeSettings(this.plugin);
    }

    public void load() {
        this.plugin.saveDefaultConfig();
        this.plugin.reloadConfig();
        this.cosmeticSettings.load(this.plugin.getConfig());
        this.wardrobeSettings.load();
    }

    public CosmeticSettings getCosmeticSettings() {
        return cosmeticSettings;
    }

    public WardrobeSettings getWardrobeSettings() {
        return wardrobeSettings;
    }
}
