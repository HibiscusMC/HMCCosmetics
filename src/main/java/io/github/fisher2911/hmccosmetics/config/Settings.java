package io.github.fisher2911.hmccosmetics.config;

import io.github.fisher2911.hmccosmetics.HMCCosmetics;
import org.bukkit.Bukkit;

public class Settings {

    private final HMCCosmetics plugin;
    private final CosmeticSettings settings;

    public Settings(final HMCCosmetics plugin) {
        this.plugin = plugin;
        this.settings = new CosmeticSettings();
    }

    public void load() {
        this.plugin.saveDefaultConfig();
        this.plugin.reloadConfig();
        this.settings.load(this.plugin.getConfig());
    }

    public CosmeticSettings getCosmeticSettings() {
        return settings;
    }
}
