package com.hibiscusmc.hmccosmetics;

import com.hibiscusmc.hmccosmetics.api.events.HMCCosmeticSetupEvent;
import com.hibiscusmc.hmccosmetics.command.CosmeticCommand;
import com.hibiscusmc.hmccosmetics.command.CosmeticCommandTabComplete;
import com.hibiscusmc.hmccosmetics.config.migration.WardrobeMigration;
import com.hibiscusmc.hmccosmetics.config.section.DatabaseSettings;
import com.hibiscusmc.hmccosmetics.config.Settings;
import com.hibiscusmc.hmccosmetics.config.WardrobeSettings;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetics;
import com.hibiscusmc.hmccosmetics.database.Database;
import com.hibiscusmc.hmccosmetics.gui.Menu;
import com.hibiscusmc.hmccosmetics.gui.Menus;
import com.hibiscusmc.hmccosmetics.gui.special.DyeMenuProvider;
import com.hibiscusmc.hmccosmetics.gui.special.impl.HMCColorDyeMenu;
import com.hibiscusmc.hmccosmetics.gui.special.impl.InternalDyeMenu;
import com.hibiscusmc.hmccosmetics.hooks.items.HookHMCCosmetics;
import com.hibiscusmc.hmccosmetics.hooks.misc.HookBetterHud;
import com.hibiscusmc.hmccosmetics.hooks.misc.HookVulcan;
import com.hibiscusmc.hmccosmetics.hooks.placeholders.HMCPlaceholderExpansion;
import com.hibiscusmc.hmccosmetics.hooks.resourcepack.HookNexo;
import com.hibiscusmc.hmccosmetics.hooks.worldguard.WGHook;
import com.hibiscusmc.hmccosmetics.hooks.worldguard.WGListener;
import com.hibiscusmc.hmccosmetics.listener.*;
import com.hibiscusmc.hmccosmetics.packets.CosmeticPacketInterface;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.user.CosmeticUsers;
import com.hibiscusmc.hmccosmetics.util.search.PlayerSearchManager;
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;
import com.hibiscusmc.hmccosmetics.util.TranslationUtil;
import lombok.Getter;
import me.lojosho.hibiscuscommons.HibiscusCommonsPlugin;
import me.lojosho.hibiscuscommons.HibiscusPlugin;
import me.lojosho.hibiscuscommons.config.serializer.ItemSerializer;
import me.lojosho.hibiscuscommons.config.serializer.LocationSerializer;
import me.lojosho.hibiscuscommons.hooks.Hooks;
import me.lojosho.shaded.configupdater.common.config.CommentedConfiguration;
import me.lojosho.shaded.configurate.CommentedConfigurationNode;
import me.lojosho.shaded.configurate.ConfigurateException;
import me.lojosho.shaded.configurate.ConfigurationOptions;
import me.lojosho.shaded.configurate.yaml.NodeStyle;
import me.lojosho.shaded.configurate.yaml.YamlConfigurationLoader;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;

import java.io.File;
import java.nio.file.Path;

public final class HMCCosmeticsPlugin extends HibiscusPlugin {

    private static HMCCosmeticsPlugin instance;
    private static YamlConfigurationLoader configLoader;

    @Getter
    private PlayerSearchManager playerSearchManager;

    public HMCCosmeticsPlugin() {
        super(13873, 1879);
        new HookHMCCosmetics();
        new HookBetterHud();
        new HookVulcan();
        new HookNexo();
    }

    @Override
    public void onStart() {
        // Plugin startup logic
        instance = this;

        // File setup
        saveDefaultConfig();
        if (!Path.of(getDataFolder().getPath(), "messages.yml").toFile().exists()) saveResource("messages.yml", false);
        if (!Path.of(getDataFolder().getPath(), "translations.yml").toFile().exists()) saveResource("translations.yml", false);
        if (!Path.of(getDataFolder().getPath() + "/cosmetics/").toFile().exists()) saveResource("cosmetics/defaultcosmetics.yml", false);
        if (!Path.of(getDataFolder().getPath() + "/menus/").toFile().exists()) {
            saveResource("menus/defaultmenu_hats.yml", false);
            saveResource("menus/defaultmenu_balloons.yml", false);
            saveResource("menus/defaultmenu_hands.yml", false);
            saveResource("menus/defaultmenu_backpacks.yml", false);
        }
        if (!Path.of(getDataFolder().getPath() + "/menus/functional/internal_dye_menu.yml").toFile().exists()) {
            saveResource("menus/functional/internal_dye_menu.yml", false);
        }
        if (!Path.of(getDataFolder().getPath() + "/wardrobes/").toFile().exists()) {
            saveResource("wardrobes/defaultwardrobe.yml", false);
            WardrobeMigration.migrate(this);
        }

        // Configuration Sync
        final File configFile = Path.of(getInstance().getDataFolder().getPath(), "config.yml").toFile();
        final File messageFile = Path.of(getInstance().getDataFolder().getPath(), "messages.yml").toFile();
        final File translationFile = Path.of(getInstance().getDataFolder().getPath(), "translations.yml").toFile();
        try {
            CommentedConfiguration.loadConfiguration(configFile).syncWithConfig(configFile, getInstance().getResource("config.yml"),
                    "database-settings", "wardrobe.wardrobes", "debug-mode", "wardrobe.viewer-location", "wardrobe.npc-location", "wardrobe.wardrobe-location", "wardrobe.leave-location");
            CommentedConfiguration.loadConfiguration(messageFile).syncWithConfig(messageFile, getInstance().getResource("messages.yml"));
            CommentedConfiguration.loadConfiguration(translationFile).syncWithConfig(translationFile, getInstance().getResource("translations.yml"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Move this over to Hibiscus Commons later
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) new HMCPlaceholderExpansion().register();

        // Setup
        setup();
        setPacketInterface(new CosmeticPacketInterface());

        // Search Service
        this.playerSearchManager = new PlayerSearchManager(Settings.getEngine(), this);

        // Commands
        PluginCommand cosmeticCommand = getServer().getPluginCommand("hmccosmetics");
        if (cosmeticCommand != null) {
            cosmeticCommand.setExecutor(new CosmeticCommand());
            cosmeticCommand.setTabCompleter(new CosmeticCommandTabComplete());
        } else {
            getLogger().severe("Unable to register commands! (Is another plugin interfering with HMCCosmetics commands?)");
        }

        // Listener
        getServer().getPluginManager().registerEvents(new PlayerConnectionListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerGameListener(), this);
        getServer().getPluginManager().registerEvents(new ServerListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerMovementListener(), this);
        getServer().getPluginManager().registerEvents(this.playerSearchManager.getEngine(), this);

        if (HibiscusCommonsPlugin.isOnPaper()) {
            getServer().getPluginManager().registerEvents(new PaperPlayerGameListener(), this);
        }
        // Database
        new Database();

        // HMCColor
        try {
            if (Settings.isPreferHMCColorDyeMenu() && Hooks.isActiveHook("HMCColor")) {
                DyeMenuProvider.setDyeMenuProvider(new HMCColorDyeMenu());
            } else {
                DyeMenuProvider.setDyeMenuProvider(new InternalDyeMenu());
            }
            // Reload method called in setup, do not need to call it here as all we do is set the provider.
        } catch (IllegalStateException e) {
            getLogger().warning("Unable to set a dye menu. There is likely another plugin registering another dye menu.");
        }

        // WorldGuard
        if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null && Settings.isWorldGuardMoveCheck()) {
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
    public void onEnd() {
        // Plugin shutdown logic
        for (Player player : Bukkit.getOnlinePlayers()) {
            CosmeticUser user = CosmeticUsers.getUser(player);
            if (user == null) continue;
            if (user.isInWardrobe()) {
                user.leaveWardrobe(true);
            }
            Database.save(user);
        }
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
        } catch (Exception e) {
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

        // Cosmetics setup
        Cosmetics.setup();

        // Menus setup
        Menus.setup();

        // Dye Menu Reload
        DyeMenuProvider.reload();

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

        getInstance().getLogger().info("Successfully Enabled HMCCosmetics");
        getInstance().getLogger().info(Cosmetics.values().size() + " Cosmetics Successfully Setup");
        getInstance().getLogger().info(Menus.getMenuNames().size() + " Menus Successfully Setup");
        getInstance().getLogger().info(WardrobeSettings.getWardrobes().size() + " Wardrobes Successfully Setup");
        getInstance().getLogger().info("Data storage is set to " + DatabaseSettings.getDatabaseType());

        Bukkit.getPluginManager().callEvent(new HMCCosmeticSetupEvent());
    }
}
