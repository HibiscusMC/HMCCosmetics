package com.hibiscusmc.hmccosmetics;

import com.hibiscusmc.hmccosmetics.api.HMCCosmeticSetupEvent;
import com.hibiscusmc.hmccosmetics.command.CosmeticCommand;
import com.hibiscusmc.hmccosmetics.command.CosmeticCommandTabComplete;
import com.hibiscusmc.hmccosmetics.config.DatabaseSettings;
import com.hibiscusmc.hmccosmetics.config.Settings;
import com.hibiscusmc.hmccosmetics.config.WardrobeSettings;
import com.hibiscusmc.hmccosmetics.config.serializer.ItemSerializer;
import com.hibiscusmc.hmccosmetics.config.serializer.LocationSerializer;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetics;
import com.hibiscusmc.hmccosmetics.database.Database;
import com.hibiscusmc.hmccosmetics.emotes.EmoteManager;
import com.hibiscusmc.hmccosmetics.gui.Menus;
import com.hibiscusmc.hmccosmetics.hooks.Hooks;
import com.hibiscusmc.hmccosmetics.hooks.worldguard.WGHook;
import com.hibiscusmc.hmccosmetics.hooks.worldguard.WGListener;
import com.hibiscusmc.hmccosmetics.listener.PlayerConnectionListener;
import com.hibiscusmc.hmccosmetics.listener.PlayerGameListener;
import com.hibiscusmc.hmccosmetics.nms.NMSHandlers;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.user.CosmeticUsers;
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;
import com.hibiscusmc.hmccosmetics.util.TranslationUtil;
import com.jeff_media.updatechecker.UpdateCheckSource;
import com.jeff_media.updatechecker.UpdateChecker;
import com.ticxo.playeranimator.PlayerAnimatorImpl;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.java.JavaPlugin;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.nio.file.Path;

@SuppressWarnings("SpellCheckingInspection")
public final class HMCCosmeticsPlugin extends JavaPlugin {

    private static HMCCosmeticsPlugin instance;
    private static boolean disable = false;
    private static YamlConfigurationLoader configLoader;
    private static final int pluginId = 13873;
    private static boolean hasModelEngine = false;
    private static boolean onLatestVersion = true;
    private static String latestVersion = "";

    public HMCCosmeticsPlugin() {
        instance = this;
    }

    @Override
    public void onLoad() {
        // WorldGuard
        if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null) {
            new WGHook();
        }
    }

    @Override
    public void onEnable() {
        // bstats https://bstats.org/plugin/bukkit/HMCCosmetics/13873
        Metrics metrics = new Metrics(this, pluginId);

        // NMS version check
        if (!NMSHandlers.getHandler().getSupported()) {
            getLogger().severe("This version is not supported! Consider switching versions?");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Update Checker
        UpdateChecker checker = new UpdateChecker(this, UpdateCheckSource.POLYMART, "1879")
                .onSuccess((commandSenders, latestVersion) -> {
                    this.latestVersion = (String) latestVersion;
                    if (!this.latestVersion.equalsIgnoreCase(getDescription().getVersion())) {
                        getLogger().info("+++++++++++++++++++++++++++++++++++");
                        getLogger().info("There is a new update for HMCCosmetics!");
                        getLogger().info("Please download it as soon as possible for possible fixes and new features.");
                        getLogger().info("Current Version " + getDescription().getVersion() + " | Latest Version " + latestVersion);
                        getLogger().info("Spigot: https://www.spigotmc.org/resources/100107/");
                        getLogger().info("Polymart: https://polymart.org/resource/1879");
                        getLogger().info("+++++++++++++++++++++++++++++++++++");
                    }
                })
                .setNotifyRequesters(false)
                .setNotifyOpsOnJoin(false)
                .checkEveryXHours(24)
                .checkNow();
        onLatestVersion = checker.isUsingLatestVersion();
        // File setup
        if (!getDataFolder().exists()) {
            saveDefaultConfig();
            saveResource("translations.yml", false);
            saveResource("messages.yml", false);
            saveResource("cosmetics/defaultcosmetics.yml", false);
            saveResource("menus/defaultmenu.yml", false);
        }
        // Emote folder setup
        File emoteFile = new File(getDataFolder().getPath() + "/emotes");
        if (!emoteFile.exists()) emoteFile.mkdir();

        // Player Animator
        PlayerAnimatorImpl.initialize(this);

        setup();

        // Commands
        getServer().getPluginCommand("cosmetic").setExecutor(new CosmeticCommand());
        getServer().getPluginCommand("cosmetic").setTabCompleter(new CosmeticCommandTabComplete());

        // Listener
        getServer().getPluginManager().registerEvents(new PlayerConnectionListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerGameListener(), this);

        // Database
        new Database();

        // ModelEngine
        if (Bukkit.getPluginManager().getPlugin("ModelEngine") != null) {
            hasModelEngine = true;
        }

        // WorldGuard
        if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null && Settings.isWorldGuardMoveCheckEnabled()) {
            getServer().getPluginManager().registerEvents(new WGListener(), this);
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        disable = true;
        for (Player player : Bukkit.getOnlinePlayers()) {
            CosmeticUser user = CosmeticUsers.getUser(player);
            if (user == null) continue;
            if (user.getUserEmoteManager().isPlayingEmote()) {
                player.setInvisible(false);
            }
            Database.save(user);
        }
    }

    public static HMCCosmeticsPlugin get() {
        return instance;
    }

    @Deprecated
    public static HMCCosmeticsPlugin getInstance() {
        return instance;
    }

    @Deprecated
    public static boolean isDisable() {
        return disable;
    }

    public static void setup() {
        get().reloadConfig();

        // Configuration setup
        final File file = Path.of(get().getDataFolder().getPath(), "config.yml").toFile();
        final YamlConfigurationLoader loader = YamlConfigurationLoader.
                builder().
                path(file.toPath()).
                defaultOptions(opts ->
                        opts.serializers(build -> {
                            build.register(Location.class, LocationSerializer.INSTANCE);
                            build.register(ItemStack.class, ItemSerializer.INSTANCE);
                        }))
                .nodeStyle(NodeStyle.BLOCK)
                .build();
        try {
            Settings.load(loader.load(ConfigurationOptions.defaults()));
            WardrobeSettings.load(loader.load().node("wardrobe"));
            DatabaseSettings.load(loader.load().node("database-settings"));
            configLoader = loader;
        } catch (ConfigurateException e) {
            throw new RuntimeException(e);
        }

        // Messages setup
        final File messagesFile = Path.of(get().getDataFolder().getPath(), "messages.yml").toFile();
        final YamlConfigurationLoader messagesLoader = YamlConfigurationLoader.
                builder().
                path(messagesFile.toPath()).
                defaultOptions(opts ->
                        opts.serializers(build -> {
                            build.register(Location.class, LocationSerializer.INSTANCE);
                            build.register(ItemStack.class, ItemSerializer.INSTANCE);
                        }))
                .nodeStyle(NodeStyle.BLOCK)
                .build();
        try {
            MessagesUtil.setup(messagesLoader.load());
        } catch (ConfigurateException e) {
            throw new RuntimeException(e);
        }

        // Translation setup
        final File translationFile = Path.of(get().getDataFolder().getPath(), "translations.yml").toFile();
        final YamlConfigurationLoader translationLoader = YamlConfigurationLoader.
                builder().
                path(translationFile.toPath())
                .nodeStyle(NodeStyle.BLOCK)
                .build();
        try {
            TranslationUtil.setup(translationLoader.load());
        } catch (ConfigurateException e) {
            throw new RuntimeException(e);
        }

        // Misc Hooks setup (like items)
        Hooks.setup();

        // Cosmetics setup
        Cosmetics.setup();

        // Menus setup
        Menus.setup();

        for (Cosmetic cosmetic : Cosmetics.values()) {
            if (cosmetic.getPermission() != null) {
                if (get().getServer().getPluginManager().getPermission(cosmetic.getPermission()) != null) continue;
                get().getServer().getPluginManager().addPermission(new Permission(cosmetic.getPermission()));
            }
        }

        EmoteManager.loadEmotes();

        get().getLogger().info("Successfully Enabled HMCCosmetics");
        get().getLogger().info(Cosmetics.values().size() + " Cosmetics Successfully Setup");
        get().getLogger().info(Menus.getMenuNames().size() + " Menus Successfully Setup");
        get().getLogger().info("Data storage is set to " + DatabaseSettings.getDatabaseType());

        Bukkit.getPluginManager().callEvent(new HMCCosmeticSetupEvent());
    }

    public static YamlConfigurationLoader getConfigLoader() {
        return configLoader;
    }

    public static void saveConfig(ConfigurationNode node) {
        try {
            HMCCosmeticsPlugin.getConfigLoader().save(node);
            HMCCosmeticsPlugin.get().getLogger().info("Set new location " + node.path());
        } catch (ConfigurateException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean hasModelEngine() {
        return hasModelEngine;
    }
    public static boolean isOnLatestVersion() {
        return onLatestVersion;
    }
    public static String getLatestVersion() {
        return latestVersion;
    }
}
