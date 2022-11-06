package com.hibiscusmc.hmccosmetics;

import com.hibiscusmc.hmccosmetics.command.CosmeticCommand;
import com.hibiscusmc.hmccosmetics.command.CosmeticCommandTabComplete;
import com.hibiscusmc.hmccosmetics.config.Settings;
import com.hibiscusmc.hmccosmetics.config.WardrobeSettings;
import com.hibiscusmc.hmccosmetics.config.serializer.ItemSerializer;
import com.hibiscusmc.hmccosmetics.config.serializer.LocationSerializer;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetics;
import com.hibiscusmc.hmccosmetics.database.Database;
import com.hibiscusmc.hmccosmetics.gui.Menus;
import com.hibiscusmc.hmccosmetics.listener.PlayerConnectionListener;
import com.hibiscusmc.hmccosmetics.listener.PlayerGameListener;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.nio.file.Path;

public final class HMCCosmeticsPlugin extends JavaPlugin {

    private static HMCCosmeticsPlugin instance;

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;

        // File setup
        if (!getDataFolder().exists()) {
            saveDefaultConfig();
            saveResource("translations.yml", false);
            saveResource("messages.yml", false);
            saveResource("cosmetics/examplecosmetics.yml", false);
            saveResource("menus/examplemenu.yml", false);
        }

        setup();

        // Commands
        getServer().getPluginCommand("cosmetic").setExecutor(new CosmeticCommand());
        getServer().getPluginCommand("cosmetic").setTabCompleter(new CosmeticCommandTabComplete());

        // Listener
        getServer().getPluginManager().registerEvents(new PlayerConnectionListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerGameListener(), this);

        // Database
        new Database();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static HMCCosmeticsPlugin getInstance() {
        return instance;
    }

    public static void setup() {
        getInstance().reloadConfig();

        // Configuration setup
        final File file = Path.of(getInstance().getDataFolder().getPath(), "config.yml").toFile();
        final YamlConfigurationLoader loader = YamlConfigurationLoader.
                builder().
                path(file.toPath()).
                defaultOptions(opts ->
                        opts.serializers(build -> {
                            build.register(Location.class, LocationSerializer.INSTANCE);
                            build.register(ItemStack.class, ItemSerializer.INSTANCE);
                        }))
                .build();
        try {
            Settings.load(loader.load().node(""));
            WardrobeSettings.load(loader.load().node("wardrobe"));
        } catch (ConfigurateException e) {
            throw new RuntimeException(e);
        }

        // Cosmetics setup
        Cosmetics.setup();

        // Menus setup
        Menus.setup();
    }
}
