package io.github.fisher2911.hmccosmetics.config;

import io.github.fisher2911.hmccosmetics.HMCCosmetics;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.EquipmentSlot;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@ConfigSerializable
public class CosmeticSettings {

    private static final transient String COSMETIC_SETTINGS_PATH = "cosmetic-settings";
    private static final transient String REQUIRE_EMPTY_HELMET_PATH = "require-empty-helmet";
    private static final transient String REQUIRE_EMPTY_OFF_HAND_PATH = "require-empty-off-hand";

    private boolean requireEmptyHelmet;
    private boolean requireEmptyOffHand;

    public void load(final FileConfiguration config) {
        this.requireEmptyHelmet = config.getBoolean(COSMETIC_SETTINGS_PATH + "." + REQUIRE_EMPTY_HELMET_PATH);
        this.requireEmptyOffHand = config.getBoolean(COSMETIC_SETTINGS_PATH + "." + REQUIRE_EMPTY_OFF_HAND_PATH);
    }

    public boolean isRequireEmptyHelmet() {
        return requireEmptyHelmet;
    }

    public boolean isRequireEmptyOffHand() {
        return requireEmptyOffHand;
    }

    public void setRequireEmptyHelmet(final boolean requireEmptyHelmet) {
        this.requireEmptyHelmet = requireEmptyHelmet;
    }

    public void setRequireEmptyOffHand(final boolean requireEmptyOffHand) {
        this.requireEmptyOffHand = requireEmptyOffHand;
    }

    public boolean requireEmpty(final EquipmentSlot slot) {
        return switch (slot) {
            case OFF_HAND -> this.isRequireEmptyOffHand();
            case HEAD -> this.isRequireEmptyHelmet();
            default -> false;
        };
    }
}
