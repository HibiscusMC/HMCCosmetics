package com.hibiscusmc.hmccosmetics.config;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
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

    private static String defaultMenu;
    private static String dyeMenuName;
    private static int configVersion;
    private static boolean requireEmptyHelmet;
    private static boolean requireEmptyOffHand;
    private static boolean requireEmptyChestPlate;
    private static boolean requireEmptyPants;
    private static boolean requireEmptyBoots;
    private static int lookDownPitch;
    private static int viewDistance;
    private static Vector balloonOffset;

    public static void load(ConfigurationNode source) {

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

        lookDownPitch = cosmeticSettings.node(LOOK_DOWN_PITCH_PATH).getInt();
        viewDistance = cosmeticSettings.node(VIEW_DISTANCE_PATH).getInt();

        final var balloonSection = cosmeticSettings.node(BALLOON_OFFSET);

        balloonOffset = loadVector(balloonSection);

        ConfigurationNode dyeMenuSettings = source.node(DYE_MENU_PATH);

        dyeMenuName = dyeMenuSettings.node(DYE_MENU_NAME).getString("Dye Menu");
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
}
