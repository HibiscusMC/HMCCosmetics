package io.github.fisher2911.hmccosmetics.config;

import io.github.fisher2911.hmccosmetics.gui.CosmeticsMenu;
import io.github.fisher2911.hmccosmetics.util.Utils;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.util.Vector;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.Objects;

@ConfigSerializable
public class CosmeticSettings {

    private static final String DEFAULT_MENU = "default-menu";
    private static final String COSMETIC_SETTINGS_PATH = "cosmetic-settings";
    private static final String REQUIRE_EMPTY_HELMET_PATH = "require-empty-helmet";
    private static final String REQUIRE_EMPTY_OFF_HAND_PATH = "require-empty-off-hand";
    private static final String REQUIRE_EMPTY_CHEST_PLATE_PATH = "require-empty-chest-plate";
    private static final String REQUIRE_EMPTY_PANTS_PATH = "require-empty-pants";
    private static final String REQUIRE_EMPTY_BOOTS_PATH = "require-empty-boots";
    private static final String BALLOON_OFFSET = "balloon-offset";

    private static final transient String LOOK_DOWN_PITCH_PATH = "look-down-backpack-remove";
    private static final String VIEW_DISTANCE_PATH = "view-distance";

    private String defaultMenu;
    private boolean requireEmptyHelmet;
    private boolean requireEmptyOffHand;
    private boolean requireEmptyChestPlate;
    private boolean requireEmptyPants;
    private boolean requireEmptyBoots;
    private int lookDownPitch;
    private int viewDistance;
    private Vector balloonOffset;

    public void load(final FileConfiguration config) {
        this.defaultMenu = Utils.replaceIf(config.getString(DEFAULT_MENU), CosmeticsMenu.DEFAULT_MAIN_MENU, null, "");
        this.requireEmptyHelmet = config.getBoolean(COSMETIC_SETTINGS_PATH + "." + REQUIRE_EMPTY_HELMET_PATH);
        this.requireEmptyOffHand = config.getBoolean(COSMETIC_SETTINGS_PATH + "." + REQUIRE_EMPTY_OFF_HAND_PATH);
        this.requireEmptyChestPlate = config.getBoolean(COSMETIC_SETTINGS_PATH + "." + REQUIRE_EMPTY_CHEST_PLATE_PATH);
        this.requireEmptyPants = config.getBoolean(COSMETIC_SETTINGS_PATH + "." + REQUIRE_EMPTY_PANTS_PATH);
        this.requireEmptyBoots = config.getBoolean(COSMETIC_SETTINGS_PATH + "." + REQUIRE_EMPTY_BOOTS_PATH);

        this.lookDownPitch = config.getInt(COSMETIC_SETTINGS_PATH + "." + LOOK_DOWN_PITCH_PATH);
        this.viewDistance = config.getInt(COSMETIC_SETTINGS_PATH + "." + VIEW_DISTANCE_PATH);

        final var balloonSection = config.getConfigurationSection(COSMETIC_SETTINGS_PATH + "." + BALLOON_OFFSET);

        if (balloonSection != null) {
            this.balloonOffset = loadVector(balloonSection);
        }
    }

    private Vector loadVector(final ConfigurationSection section) {
        return new Vector(section.getDouble("x"), section.getDouble("y"), section.getDouble("z"));
    }

    public boolean isRequireEmptyHelmet() {
        return requireEmptyHelmet;
    }

    public void setRequireEmptyHelmet(final boolean requireEmptyHelmet) {
        this.requireEmptyHelmet = requireEmptyHelmet;
    }

    public boolean isRequireEmptyOffHand() {
        return requireEmptyOffHand;
    }

    public void setRequireEmptyOffHand(final boolean requireEmptyOffHand) {
        this.requireEmptyOffHand = requireEmptyOffHand;
    }

    public boolean isRequireEmptyChestPlate() {
        return requireEmptyChestPlate;
    }

    public void setRequireEmptyChestPlate(final boolean requireEmptyChestPlate) {
        this.requireEmptyChestPlate = requireEmptyChestPlate;
    }

    public boolean isRequireEmptyPants() {
        return requireEmptyPants;
    }

    public void setRequireEmptyPants(final boolean requireEmptyPants) {
        this.requireEmptyPants = requireEmptyPants;
    }

    public boolean isRequireEmptyBoots() {
        return requireEmptyBoots;
    }

    public void setRequireEmptyBoots(final boolean requireEmptyBoots) {
        this.requireEmptyBoots = requireEmptyBoots;
    }

    public Vector getBalloonOffset() {
        return balloonOffset;
    }

    public int getLookDownPitch() {
        return lookDownPitch;
    }

    public int getViewDistance() {
        return viewDistance;
    }

    public String getDefaultMenu() {
        return defaultMenu;
    }

    public boolean requireEmpty(final EquipmentSlot slot) {
        return switch (slot) {
            case OFF_HAND -> this.isRequireEmptyOffHand();
            case HEAD -> this.isRequireEmptyHelmet();
            case CHEST -> this.isRequireEmptyChestPlate();
            case LEGS -> this.isRequireEmptyPants();
            case FEET -> this.isRequireEmptyBoots();
            default -> false;
        };
    }

    public boolean isInViewDistance(final Location location, final Location other) {
        if (!Objects.equals(other.getWorld(), location.getWorld())) return false;
        return !(other.distanceSquared(location) > this.getViewDistance() * this.getViewDistance());
    }
}
