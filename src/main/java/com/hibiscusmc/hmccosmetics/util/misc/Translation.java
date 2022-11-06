package com.hibiscusmc.hmccosmetics.util.misc;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Map;

public class Translation {

    public static final String TRUE = "true";
    public static final String FALSE = "false";
    public static final String NONE = "none";
    private static final String FILE_NAME = "translations.yml";
    private static final String TRANSLATION_PATH = "translations";

    private static Map<String, String> translations;

    public static String translate(final String key) {
        return translations.getOrDefault(key, null);
    }

    public void load() {
        final File file = new File(HMCCosmeticsPlugin.getInstance().getDataFolder(), FILE_NAME);
        if (!file.exists()) {
            HMCCosmeticsPlugin.getInstance().saveResource(FILE_NAME, false);
        }

        final FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        final ConfigurationSection section = config.getConfigurationSection(TRANSLATION_PATH);

        if (section == null) {
            return;
        }

        for (final String key : section.getKeys(false)) {
            this.translations.put(key, section.getString(key));
        }
    }
}
