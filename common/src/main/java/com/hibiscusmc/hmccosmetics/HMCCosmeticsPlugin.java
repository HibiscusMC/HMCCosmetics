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
import com.hibiscusmc.hmccosmetics.gui.Menus;
import com.hibiscusmc.hmccosmetics.hooks.Hooks;
import com.hibiscusmc.hmccosmetics.hooks.worldguard.WGHook;
import com.hibiscusmc.hmccosmetics.hooks.worldguard.WGListener;
import com.hibiscusmc.hmccosmetics.listener.PlayerConnectionListener;
import com.hibiscusmc.hmccosmetics.listener.PlayerGameListener;
import com.hibiscusmc.hmccosmetics.nms.NMSHandlers;
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;
import com.hibiscusmc.hmccosmetics.util.TranslationUtil;
import com.jeff_media.updatechecker.UpdateCheckSource;
import com.jeff_media.updatechecker.UpdateChecker;
import com.ticxo.playeranimator.PlayerAnimatorImpl;
import com.ticxo.playeranimator.api.PlayerAnimator;
import com.ticxo.playeranimator.api.animation.pack.AnimationPack;
import org.apache.commons.io.FilenameUtils;
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
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class HMCCosmeticsPlugin extends JavaPlugin {

    private static HMCCosmeticsPlugin instance;
    private static boolean disable = false;
    private static YamlConfigurationLoader configLoader;
    private static final int pluginId = 13873;
    private static boolean hasModelEngine = false;
    private static boolean onLatestVersion = true;
    private static String latestVersion = "";

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
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
        if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null) {
            getServer().getPluginManager().registerEvents(new WGListener(), this);
        }
    }

    @Override
    public void onLoad() {
        // WorldGuard
        if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null) {
            new WGHook();
        }
    }

    @Override
    public void onDisable() {
        disable = true;
        for (Player player : Bukkit.getOnlinePlayers()) {
            Database.save(player);
        }
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
        final File messagesFile = Path.of(getInstance().getDataFolder().getPath(), "messages.yml").toFile();
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
        final File translationFile = Path.of(getInstance().getDataFolder().getPath(), "translations.yml").toFile();
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

        // For reloads
        /*
        for (Player player : Bukkit.getOnlinePlayers()) {
            CosmeticUser user = CosmeticUsers.getUser(player.getUniqueId());
            if (user == null) continue;
            for (Cosmetic cosmetic : user.getCosmetic()) {
                Color color = user.getCosmeticColor(cosmetic.getSlot());
                Cosmetic newCosmetic = Cosmetics.getCosmetic(cosmetic.getId());
                user.removeCosmeticSlot(cosmetic);

                if (newCosmetic == null) continue;
                user.addPlayerCosmetic(newCosmetic, color);
            }
            user.updateCosmetic();
        }
         */
        for (Cosmetic cosmetic : Cosmetics.values()) {
            if (cosmetic.getPermission() != null) {
                if (getInstance().getServer().getPluginManager().getPermission(cosmetic.getPermission()) != null) continue;
                getInstance().getServer().getPluginManager().addPermission(new Permission(cosmetic.getPermission()));
            }
        }

        File emoteFolder = new File(getInstance().getDataFolder().getPath() + "/emotes/");
        if (emoteFolder.exists()) {
            PlayerAnimator.api.getAnimationManager().clearRegistry();
            File[] emotesFiles = emoteFolder.listFiles();
            for (File emoteFile : emotesFiles) {
                if (!emoteFile.getName().contains("bbmodel")) continue;
                String animationName = emoteFile.getName().replaceAll(".bbmodel", "");
                PlayerAnimator.api.getAnimationManager().importAnimations(FilenameUtils.removeExtension(emoteFile.getName()), emoteFile);
                MessagesUtil.sendDebugMessages("Added '" + animationName + "' to Player Animator ");
            }

            for (Map.Entry<String, AnimationPack> packEntry : PlayerAnimator.api.getAnimationManager().getRegistry().entrySet()) {
                //Set<String> animationNames = packEntry.getValue().getAnimations().keySet().stream().map(animation -> packEntry.getKey().replace(":", ".") + "." + animation).collect(Collectors.toSet());
            }
        }

        getInstance().getLogger().info("Successfully Enabled HMCCosmetics");
        getInstance().getLogger().info(Cosmetics.values().size() + " Cosmetics Successfully Setup");
        getInstance().getLogger().info(Menus.getMenuNames().size() + " Menus Successfully Setup");
        getInstance().getLogger().info("Data storage is set to " + DatabaseSettings.getDatabaseType());

        Bukkit.getPluginManager().callEvent(new HMCCosmeticSetupEvent());
    }

    public static boolean isDisable() {
        return disable;
    }

    public static YamlConfigurationLoader getConfigLoader() {
        return configLoader;
    }

    public static void saveConfig(ConfigurationNode node) {
        try {
            HMCCosmeticsPlugin.getConfigLoader().save(node);
            HMCCosmeticsPlugin.getInstance().getLogger().info("Set new location " + node.path());
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
