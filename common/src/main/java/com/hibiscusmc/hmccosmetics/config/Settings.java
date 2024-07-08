package com.hibiscusmc.hmccosmetics.config;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;
import lombok.Getter;
import lombok.Setter;
import me.lojosho.shaded.configurate.ConfigurationNode;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

public class Settings {

    // General Settings
    private static final String DEFAULT_MENU = "default-menu";
    private static final String CONFIG_VERSION = "config-version";
    private static final String COSMETIC_SETTINGS_PATH = "cosmetic-settings";
    private static final String BALLOON_OFFSET = "balloon-offset";
    private static final String VIEW_DISTANCE_PATH = "view-distance";
    private static final String DYE_MENU_PATH = "dye-menu";
    private static final String DYE_MENU_NAME = "title";
    private static final String DYE_MENU_INPUT_SLOT = "input-slot";
    private static final String DYE_MENU_OUTPUT_SLOT = "output-slot";
    private static final String DEBUG_ENABLE_PETH = "debug-mode";
    private static final String TICK_PERIOD_PATH = "tick-period";
    private static final String UNAPPLY_DEATH_PATH = "unapply-on-death";
    private static final String FORCE_PERMISSION_JOIN_PATH = "force-permission-join";
    private static final String FORCE_SHOW_COSMETICS_PATH = "force-show-join";
    private static final String DISABLED_GAMEMODE_PATH = "disabled-gamemode";
    private static final String DISABLED_GAMEMODE_GAMEMODES_PATH = "gamemodes";
    private static final String EMOTE_DISTANCE_PATH = "emote-distance";
    private static final String HOOK_SETTING_PATH = "hook-settings";
    private static final String HOOK_ITEMADDER_PATH = "itemsadder";
    private static final String HOOK_RELOAD_CHANGE_PATH = "reload-on-change";
    private static final String HOOK_WORLDGUARD_PATH = "worldguard";
    private static final String HOOK_WG_MOVE_CHECK_PATH = "player-move-check";
    private static final String HOOK_WG_MOVE_CHECK_PATH_LEGACY = "player_move_check";
    private static final String COSMETIC_EMOTE_ENABLE = "emote-enable";
    private static final String COSMETIC_EMOTE_CHECK_PATH = "emote-block-check";
    private static final String COSMETIC_EMOTE_AIR_CHECK_PATH = "emote-air-check";
    private static final String COSMETIC_EMOTE_DAMAGE_PATH = "emote-damage-leave";
    private static final String COSMETIC_EMOTE_INVINCIBLE_PATH = "emote-invincible";
    private static final String COSMETIC_EMOTE_CAMERA_PATH = "emote-camera";
    private static final String COSMETIC_EMOTE_MOVE_CHECK_PATH = "emote-move";
    private static final String COSMETIC_DISABLED_WORLDS_PATH = "disabled-worlds";
    private static final String COSMETIC_PACKET_ENTITY_TELEPORT_COOLDOWN_PATH = "entity-cooldown-teleport-packet";
    private static final String COSMETIC_BACKPACK_FORCE_RIDING_PACKET_PATH = "backpack-force-riding-packet";
    private static final String COSMETIC_BACKPACK_LIGHT_EMINATION_PATH = "backpack-light-emination";
    private static final String COSMETIC_BACKPACK_LIGHT_BLOCK_DETECTION = "backpack-block-detection";
    private static final String COSMETIC_DESTROY_LOOSE_COSMETIC_PATH = "destroy-loose-cosmetics";
    private static final String COSMETIC_BALLOON_HEAD_FORWARD_PATH = "balloon-head-forward";
    private static final String MENU_SETTINGS_PATH = "menu-settings";
    private static final String MENU_CLICK_COOLDOWN_PATH = "click-cooldown";
    private static final String MENU_CLICK_COOLDOWN_TIME_PATH = "time";
    private static final String COSMETIC_TYPE_SETTINGS_PATH = "cosmetic-type";
    private static final String EQUIP_CLICK_TYPE = "equip-click";
    private static final String UNEQUIP_CLICK_TYPE = "unequip-click";
    private static final String SHADING_PATH = "shading";
    private static final String FIRST_ROW_SHIFT_PATH = "first-row-shift";
    private static final String SEQUENT_ROW_SHIFT_PATH = "sequent-row-shift";
    private static final String INDIVIDUAL_COLUMN_SHIFT_PATH = "individual-column-shift";
    private static final String BACKGROUND_PATH = "background";
    private static final String CLEAR_BACKGROUND_PATH = "clear-background";
    private static final String EQUIPPED_COSMETIC_COLOR_PATH = "equipped-cosmetic-color";
    private static final String EQUIPABLE_COSMETIC_COLOR_PATH = "equipable-cosmetic-color";
    private static final String LOCKED_COSMETIC_COLOR_PATH = "locked-cosmetic-color";
    private static final String ENABLED_PATH = "enabled";
    private static final String SLOT_OPTIONS_PATH = "slot-options";

    @Getter
    private static String defaultMenu;
    @Getter
    private static String dyeMenuName;
    @Getter
    private static int dyeMenuInputSlot;
    @Getter
    private static int dyeMenuOutputSlot;
    @Getter
    private static int configVersion;
    @Getter
    private static boolean debugMode;
    @Getter
    private static boolean unapplyOnDeath;
    @Getter
    private static boolean forcePermissionJoin;
    @Getter
    private static boolean forceShowOnJoin;
    @Getter
    private static boolean itemsAdderChangeReload;
    @Getter
    private static boolean worldGuardMoveCheck;
    @Getter
    private static boolean cosmeticEmoteBlockCheck;
    private static final HashMap<EquipmentSlot, SlotOptionConfig> slotOptions = new HashMap<>();
    @Getter
    private static boolean emoteAirCheck;
    @Getter
    private static boolean emoteDamageLeave;
    @Getter
    private static boolean emoteInvincible;
    @Getter
    private static boolean destroyLooseCosmetics;
    @Getter
    private static boolean backpackForceRidingEnabled;
    @Getter
    private static boolean backpackLightEmination;
    @Getter
    private static boolean backpackBlockDetection;
    @Getter
    private static boolean emotesEnabled;
    @Getter
    private static boolean disabledGamemodesEnabled;
    @Getter
    private static boolean balloonHeadForward;
    @Getter
    private static List<String> disabledGamemodes;
    @Getter
    private static List<String> disabledWorlds;
    @Getter
    private static int viewDistance;
    @Getter
    private static int tickPeriod;
    @Getter
    private static int packetEntityTeleportCooldown;
    @Getter
    private static Long defaultMenuCooldown;
    @Getter
    private static boolean menuClickCooldown;
    @Getter
    private static double emoteDistance;
    @Getter
    private static Vector balloonOffset;
    @Getter
    private static String cosmeticEquipClickType;
    @Getter
    private static String cosmeticUnEquipClickType;
    @Getter
    private static boolean defaultShading;
    @Getter
    private static String firstRowShift;
    @Getter
    private static String sequentRowShift;
    @Getter
    private static String individualColumnShift;
    @Getter
    private static String background;
    @Getter
    private static String clearBackground;
    @Getter
    private static String equippedCosmeticColor;
    @Getter
    private static String equipableCosmeticColor;
    @Getter
    private static String lockedCosmeticColor;
    @Getter
    private static boolean emoteCameraEnabled;
    @Getter
    private static boolean emoteMoveCheck;
    @Getter @Setter
    private static boolean allPlayersHidden;


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

        ConfigurationNode disabledGamemodeSettings = cosmeticSettings.node(DISABLED_GAMEMODE_PATH);
        disabledGamemodesEnabled = disabledGamemodeSettings.node(ENABLED_PATH).getBoolean(true);
        try {
            disabledGamemodes = disabledGamemodeSettings.node(DISABLED_GAMEMODE_GAMEMODES_PATH).getList(String.class);
            disabledWorlds = cosmeticSettings.node(COSMETIC_DISABLED_WORLDS_PATH).getList(String.class);
        } catch (Exception e) {
            disabledGamemodes = new ArrayList<>();
            disabledWorlds = new ArrayList<>();
        }

        unapplyOnDeath = cosmeticSettings.node(UNAPPLY_DEATH_PATH).getBoolean(false);
        forcePermissionJoin = cosmeticSettings.node(FORCE_PERMISSION_JOIN_PATH).getBoolean(false);
        forceShowOnJoin = cosmeticSettings.node(FORCE_SHOW_COSMETICS_PATH).getBoolean(false);
        emotesEnabled = cosmeticSettings.node(COSMETIC_EMOTE_ENABLE).getBoolean(true);
        emoteDistance = cosmeticSettings.node(EMOTE_DISTANCE_PATH).getDouble(-3);
        cosmeticEmoteBlockCheck = cosmeticSettings.node(COSMETIC_EMOTE_CHECK_PATH).getBoolean(true);
        emoteAirCheck = cosmeticSettings.node(COSMETIC_EMOTE_AIR_CHECK_PATH).getBoolean(true);
        emoteDamageLeave = cosmeticSettings.node(COSMETIC_EMOTE_DAMAGE_PATH).getBoolean(false);
        emoteInvincible = cosmeticSettings.node(COSMETIC_EMOTE_INVINCIBLE_PATH).getBoolean(false);
        destroyLooseCosmetics = cosmeticSettings.node(COSMETIC_DESTROY_LOOSE_COSMETIC_PATH).getBoolean(false);
        backpackForceRidingEnabled = cosmeticSettings.node(COSMETIC_BACKPACK_FORCE_RIDING_PACKET_PATH).getBoolean(false);
        backpackLightEmination = cosmeticSettings.node(COSMETIC_BACKPACK_LIGHT_EMINATION_PATH).getBoolean(true);
        backpackBlockDetection = cosmeticSettings.node(COSMETIC_BACKPACK_LIGHT_BLOCK_DETECTION).getBoolean(true);

        cosmeticSettings.node(SLOT_OPTIONS_PATH).childrenMap().forEach((key, value) -> {
            EquipmentSlot slot = convertConfigToEquipment(key.toString().toLowerCase());
            if (slot == null) {
                MessagesUtil.sendDebugMessages("Invalid slot option: " + key, Level.WARNING);
                return;
            }
            boolean addEnchantments = value.node("add-enchantments").getBoolean(false);
            boolean requireEmpty = value.node("require-empty").getBoolean(false);
            slotOptions.put(slot, new SlotOptionConfig(slot, addEnchantments, requireEmpty));
        });

        tickPeriod = cosmeticSettings.node(TICK_PERIOD_PATH).getInt(-1);
        viewDistance = cosmeticSettings.node(VIEW_DISTANCE_PATH).getInt(-3);
        emoteCameraEnabled = cosmeticSettings.node(COSMETIC_EMOTE_CAMERA_PATH).getBoolean(true);
        emoteMoveCheck = cosmeticSettings.node(COSMETIC_EMOTE_MOVE_CHECK_PATH).getBoolean(false);
        packetEntityTeleportCooldown = cosmeticSettings.node(COSMETIC_PACKET_ENTITY_TELEPORT_COOLDOWN_PATH).getInt(-1);
        balloonHeadForward = cosmeticSettings.node(COSMETIC_BALLOON_HEAD_FORWARD_PATH).getBoolean(false);

        ConfigurationNode menuSettings = source.node(MENU_SETTINGS_PATH);

        ConfigurationNode clickCooldownSettings = menuSettings.node(MENU_CLICK_COOLDOWN_PATH);
        menuClickCooldown = clickCooldownSettings.node(ENABLED_PATH).getBoolean(true);
        defaultMenuCooldown = clickCooldownSettings.node(MENU_CLICK_COOLDOWN_TIME_PATH).getLong(1000L);

        ConfigurationNode shadingSettings = menuSettings.node(SHADING_PATH);
        defaultShading = shadingSettings.node(ENABLED_PATH).getBoolean();
        firstRowShift = shadingSettings.node(FIRST_ROW_SHIFT_PATH).getString();
        sequentRowShift = shadingSettings.node(SEQUENT_ROW_SHIFT_PATH).getString();
        individualColumnShift = shadingSettings.node(INDIVIDUAL_COLUMN_SHIFT_PATH).getString();
        background = shadingSettings.node(BACKGROUND_PATH).getString();
        clearBackground = shadingSettings.node(CLEAR_BACKGROUND_PATH).getString();
        equippedCosmeticColor = shadingSettings.node(EQUIPPED_COSMETIC_COLOR_PATH).getString();
        equipableCosmeticColor = shadingSettings.node(EQUIPABLE_COSMETIC_COLOR_PATH).getString();
        lockedCosmeticColor = shadingSettings.node(LOCKED_COSMETIC_COLOR_PATH).getString();

        ConfigurationNode cosmeticTypeSettings = menuSettings.node(COSMETIC_TYPE_SETTINGS_PATH);
        cosmeticEquipClickType = cosmeticTypeSettings.node(EQUIP_CLICK_TYPE).getString("ALL");
        cosmeticUnEquipClickType = cosmeticTypeSettings.node(UNEQUIP_CLICK_TYPE).getString("ALL");

        final var balloonSection = cosmeticSettings.node(BALLOON_OFFSET);
        balloonOffset = loadVector(balloonSection);

        ConfigurationNode dyeMenuSettings = source.node(DYE_MENU_PATH);

        dyeMenuName = dyeMenuSettings.node(DYE_MENU_NAME).getString("Dye Menu");
        dyeMenuInputSlot = dyeMenuSettings.node(DYE_MENU_INPUT_SLOT).getInt(19);
        dyeMenuOutputSlot = dyeMenuSettings.node(DYE_MENU_OUTPUT_SLOT).getInt(25);

        ConfigurationNode hookSettings = source.node(HOOK_SETTING_PATH);
        ConfigurationNode itemsAdderSettings = hookSettings.node(HOOK_ITEMADDER_PATH);
        itemsAdderChangeReload = itemsAdderSettings.node(HOOK_RELOAD_CHANGE_PATH).getBoolean(false);

        ConfigurationNode worldGuardSettings = hookSettings.node(HOOK_WORLDGUARD_PATH);
        worldGuardMoveCheck = worldGuardSettings.node(HOOK_WG_MOVE_CHECK_PATH).getBoolean(true);
        // I messed up in release 2.2.6 and forgot to change player_move_check to player-move-check.
        if (!worldGuardSettings.node(HOOK_WG_MOVE_CHECK_PATH_LEGACY).virtual()) {
            MessagesUtil.sendDebugMessages("There is a deprecated way of using WG hook setting. Change player_move_check to player-move-check in your configuration to prevent issues in the future. ", Level.WARNING);
            worldGuardMoveCheck = worldGuardSettings.node(HOOK_WG_MOVE_CHECK_PATH_LEGACY).getBoolean(true);
        }
    }

    public static Vector loadVector(final ConfigurationNode config) {
        return new Vector(config.node("x").getDouble(), config.node("y").getDouble(), config.node("z").getDouble());
    }

    public static SlotOptionConfig getSlotOption(EquipmentSlot slot) {
        if (!slotOptions.containsKey(slot)) slotOptions.put(slot, new SlotOptionConfig(slot, false, false));
        return slotOptions.get(slot);
    }

    public static void setDebugMode(boolean newSetting) {
        debugMode = newSetting;

        HMCCosmeticsPlugin plugin = HMCCosmeticsPlugin.getInstance();

        plugin.getConfig().set("debug-mode", newSetting);

        plugin.saveConfig();
    }

    private static EquipmentSlot convertConfigToEquipment(String slot) {
        return switch (slot) {
            case "helmet" -> EquipmentSlot.HEAD;
            case "chestplate" -> EquipmentSlot.CHEST;
            case "leggings" -> EquipmentSlot.LEGS;
            case "boots" -> EquipmentSlot.FEET;
            case "offhand" -> EquipmentSlot.OFF_HAND;
            case "mainhand" -> EquipmentSlot.HAND;
            default -> null;
        };
    }
}
