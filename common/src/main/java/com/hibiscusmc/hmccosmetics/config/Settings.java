package com.hibiscusmc.hmccosmetics.config;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticSlot;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.util.Vector;
import org.spongepowered.configurate.ConfigurationNode;

public class Settings {

    // General Settings
    private static final String DEFAULT_MENU = "default-menu";
    private static final String CONFIG_VERSION = "config-version";
    private static final String COSMETIC_SETTINGS_PATH = "cosmetic-settings";
    private static final String REQUIRE_EMPTY_HELMET_PATH = "require-empty-helmet";
    private static final String REQUIRE_EMPTY_OFF_HAND_PATH = "require-empty-off-hand";
    private static final String REQUIRE_EMPTY_CHEST_PLATE_PATH = "require-empty-chest-plate";
    private static final String REQUIRE_EMPTY_PANTS_PATH = "require-empty-pants";
    private static final String REQUIRE_EMPTY_BOOTS_PATH = "require-empty-boots";
    private static final String BALLOON_OFFSET = "balloon-offset";
    private static final String FIRST_PERSON_BACKPACK_MODE = "first-person-backpack-mode";

    private static final transient String LOOK_DOWN_PITCH_PATH = "look-down-backpack-remove";
    private static final String VIEW_DISTANCE_PATH = "view-distance";
    private static final String PARTICLE_COUNT = "particle-count";
    private static final String DYE_MENU_PATH = "dye-menu";
    private static final String DYE_MENU_NAME = "title";
    private static final String DYE_MENU_INPUT_SLOT = "input-slot";
    private static final String DYE_MENU_OUTPUT_SLOT = "output-slot";
    private static final String DEBUG_ENABLE_PETH = "debug-mode";
    private static final String TICK_PERIOD_PATH = "tick-period";
    private static final String UNAPPLY_DEATH_PATH = "unapply-on-death";
    private static final String FORCE_PERMISSION_JOIN_PATH = "force-permission-join";
    private static final String EMOTE_DISTANCE_PATH = "emote-distance";

    private static String defaultMenu;
    private static String dyeMenuName;
    private static int dyeMenuInputSlot;
    private static int dyeMenuOutputSlot;
    private static int configVersion;
    private static boolean requireEmptyHelmet;
    private static boolean requireEmptyOffHand;
    private static boolean requireEmptyChestPlate;
    private static boolean requireEmptyPants;
    private static boolean requireEmptyBoots;
    private static boolean debugMode;
    private static boolean unapplyOnDeath;
    private static boolean forcePermissionJoin;
    private static int lookDownPitch;
    private static int viewDistance;
    private static int tickPeriod;
    private static double emoteDistance;
    private static Vector balloonOffset;

    public static void load(ConfigurationNode source) {

        debugMode = source.node(DEBUG_ENABLE_PETH).getBoolean(false);
        defaultMenu = source.node(DEFAULT_MENU).getString();
        configVersion = source.node(CONFIG_VERSION).getInt(0);
        if (configVersion == 0) {
            HMCCosmeticsPlugin plugin = HMCCosmeticsPlugin.getInstance();
            plugin.getLogger().severe("");
            plugin.getLogger().severe("");
            plugin.getLogger().severe("Improper Configuration Found (Config Version Does Not Exist!)");
            plugin.getLogger().severe("Problems will happen with the plugin! Delete and regenerate a new one!");
            plugin.getLogger().severe("");
            plugin.getLogger().severe("");
        }

        ConfigurationNode cosmeticSettings = source.node(COSMETIC_SETTINGS_PATH);

        requireEmptyHelmet = cosmeticSettings.node(REQUIRE_EMPTY_HELMET_PATH).getBoolean();
        requireEmptyOffHand = cosmeticSettings.node(REQUIRE_EMPTY_OFF_HAND_PATH).getBoolean();
        requireEmptyChestPlate = cosmeticSettings.node(REQUIRE_EMPTY_CHEST_PLATE_PATH).getBoolean();
        requireEmptyPants = cosmeticSettings.node(REQUIRE_EMPTY_PANTS_PATH).getBoolean();
        requireEmptyBoots = cosmeticSettings.node(REQUIRE_EMPTY_BOOTS_PATH).getBoolean();
        unapplyOnDeath = cosmeticSettings.node(UNAPPLY_DEATH_PATH).getBoolean(false);
        forcePermissionJoin = cosmeticSettings.node(FORCE_PERMISSION_JOIN_PATH).getBoolean(false);
        emoteDistance = cosmeticSettings.node(EMOTE_DISTANCE_PATH).getDouble(-3);

        tickPeriod = cosmeticSettings.node(TICK_PERIOD_PATH).getInt(-1);
        lookDownPitch = cosmeticSettings.node(LOOK_DOWN_PITCH_PATH).getInt();
        viewDistance = cosmeticSettings.node(VIEW_DISTANCE_PATH).getInt();

        final var balloonSection = cosmeticSettings.node(BALLOON_OFFSET);

        balloonOffset = loadVector(balloonSection);

        ConfigurationNode dyeMenuSettings = source.node(DYE_MENU_PATH);

        dyeMenuName = dyeMenuSettings.node(DYE_MENU_NAME).getString("Dye Menu");
        dyeMenuInputSlot = dyeMenuSettings.node(DYE_MENU_INPUT_SLOT).getInt(19);
        dyeMenuOutputSlot = dyeMenuSettings.node(DYE_MENU_OUTPUT_SLOT).getInt(25);
    }

    private static Vector loadVector(final ConfigurationNode config) {
        return new Vector(config.node("x").getDouble(), config.node("y").getDouble(), config.node("z").getDouble());
    }

    public static boolean isRequireEmptyHelmet() {
        return requireEmptyHelmet;
    }

    public static boolean isRequireEmptyOffHand() {
        return requireEmptyOffHand;
    }


    public static boolean isRequireEmptyChestPlate() {
        return requireEmptyChestPlate;
    }

    public static boolean isRequireEmptyPants() {
        return requireEmptyPants;
    }

    public static boolean isRequireEmptyBoots() {
        return requireEmptyBoots;
    }

    public static boolean getRequireEmpty(CosmeticSlot slot) {
        switch (slot) {
            case HELMET -> {
                return requireEmptyHelmet;
            }
            case CHESTPLATE -> {
                return requireEmptyChestPlate;
            }
            case LEGGINGS -> {
                return requireEmptyPants;
            }
            case BOOTS -> {
                return requireEmptyBoots;
            }
            case OFFHAND -> {
                return requireEmptyOffHand;
            }
        }
        return false;
    }

    public static boolean getRequireEmpty(EquipmentSlot slot) {
        switch (slot) {
            case HEAD -> {
                return requireEmptyHelmet;
            }
            case CHEST -> {
                return requireEmptyChestPlate;
            }
            case LEGS -> {
                return requireEmptyPants;
            }
            case FEET -> {
                return requireEmptyBoots;
            }
            case OFF_HAND -> {
                return requireEmptyOffHand;
            }
        }
        return false;
    }

    public static Vector getBalloonOffset() {
        if (balloonOffset == null) HMCCosmeticsPlugin.getInstance().getLogger().info("Shits null");
        return balloonOffset;
    }

    public static int getLookDownPitch() {
        return lookDownPitch;
    }

    public static int getViewDistance() {
        return viewDistance;
    }

    public static String getDefaultMenu() {
        return defaultMenu;
    }

    public static int getConfigVersion() {
        return configVersion;
    }

    public static String getDyeMenuName() {
        return dyeMenuName;
    }
    public static int getDyeMenuInputSlot() { return dyeMenuInputSlot; }
    public static int getDyeMenuOutputSlot() { return dyeMenuOutputSlot; }

    public static boolean isDebugEnabled() {
        return debugMode;
    }

    public static int getTickPeriod() {
        return tickPeriod;
    }
    public static boolean getUnapplyOnDeath() {
        return unapplyOnDeath;
    }
    public static boolean getForcePermissionJoin() {
        return forcePermissionJoin;
    }

    public static boolean getDebugMode() {
        return debugMode;
    }

    public static double getEmoteDistance() {
        return emoteDistance;
    }
    public static void setDebugMode(boolean newSetting) {
        debugMode = newSetting;

        HMCCosmeticsPlugin plugin = HMCCosmeticsPlugin.getInstance();

        plugin.getConfig().set("debug-mode", newSetting);

        plugin.saveConfig();
    }
}
