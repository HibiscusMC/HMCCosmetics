package io.github.fisher2911.hmccosmetics.message;

import io.github.fisher2911.hmccosmetics.HMCCosmetics;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class Translation {

    private static final Translation INSTANCE;

    static {
        INSTANCE = new Translation(HMCCosmetics.getPlugin(HMCCosmetics.class));
    }

    public static Translation getInstance() {
        return INSTANCE;
    }

    private final HMCCosmetics plugin;
    private final Map<String, String> translations;

    public Translation(final HMCCosmetics plugin) {
        this.translations = new HashMap<>();
        this.plugin = plugin;
    }

    private static final String FILE_NAME = "translations.yml";
    private static final String TRANSLATION_PATH = "translations";

    public void load() {
        final File file = new File(this.plugin.getDataFolder(), FILE_NAME);
        if (!file.exists()) {
            this.plugin.saveResource(FILE_NAME, false);
        }

        final FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        final ConfigurationSection section = config.getConfigurationSection(TRANSLATION_PATH);

        if (section == null) return;

        for (final String key : section.getKeys(false)) {
            this.translations.put(key, section.getString(key));
        }
    }

    public static String translate(final String key) {
        return INSTANCE.translations.getOrDefault(key, key);
    }

}
