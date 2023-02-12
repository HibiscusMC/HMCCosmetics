package com.hibiscusmc.hmccosmetics.cosmetic;

import com.google.common.collect.HashBiMap;
import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.config.Settings;
import com.hibiscusmc.hmccosmetics.cosmetic.types.*;
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;
import org.apache.commons.lang3.EnumUtils;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.util.Set;
import java.util.logging.Level;

public class Cosmetics {

    private static HashBiMap<String, Cosmetic> COSMETICS = HashBiMap.create();

    public static void addCosmetic(Cosmetic cosmetic) {
        COSMETICS.put(cosmetic.getId(), cosmetic);
    }

    public static void removeCosmetic(String id) {
        COSMETICS.remove(id);
    }

    public static void removeCosmetic(Cosmetic cosmetic) {
        COSMETICS.remove(cosmetic);
    }

    public static Cosmetic getCosmetic(String id) {
        return COSMETICS.get(id);
    }

    public static Set<Cosmetic> values() {
        return COSMETICS.values();
    }

    public static Set<String> keys() {
        return COSMETICS.keySet();
    }

    public static boolean hasCosmetic(String id) {
        return COSMETICS.containsKey(id);
    }

    public static boolean hasCosmetic(Cosmetic cosmetic) {
        return COSMETICS.containsValue(cosmetic);
    }

    public static void setup() {
        COSMETICS.clear();

        File cosmeticFolder = new File(HMCCosmeticsPlugin.getInstance().getDataFolder() + "/cosmetics");
        if (!cosmeticFolder.exists()) cosmeticFolder.mkdir();

        File[] directoryListing = cosmeticFolder.listFiles();
        if (directoryListing == null) return;

        for (File child : directoryListing) {
            if (child.toString().contains(".yml") || child.toString().contains(".yaml")) {
                MessagesUtil.sendDebugMessages("Scanning " + child);
                // Loads file
                YamlConfigurationLoader loader = YamlConfigurationLoader.builder().path(child.toPath()).build();
                CommentedConfigurationNode root;
                try {
                    root = loader.load();
                } catch (ConfigurateException e) {
                    throw new RuntimeException(e);
                }
                setupCosmetics(root);
            }
        }
    }

    private static void setupCosmetics(CommentedConfigurationNode config) {
        for (ConfigurationNode cosmeticConfig : config.childrenMap().values()) {
            try {
                String id = cosmeticConfig.key().toString();
                MessagesUtil.sendDebugMessages("Attempting to add " + id);
                ConfigurationNode slotNode = cosmeticConfig.node("slot");
                if (slotNode.virtual()) {
                    MessagesUtil.sendDebugMessages("Unable to create " + id + " because there is no slot defined!", Level.WARNING);
                    continue;
                }
                if (!EnumUtils.isValidEnum(CosmeticSlot.class, slotNode.getString())) {
                    MessagesUtil.sendDebugMessages("Unable to create " + id + " because " + slotNode.getString() + " is not a valid slot!", Level.WARNING);
                    continue;
                }
                switch (CosmeticSlot.valueOf(cosmeticConfig.node("slot").getString())) {
                    case BALLOON -> new CosmeticBalloonType(id, cosmeticConfig);
                    case BACKPACK -> new CosmeticBackpackType(id, cosmeticConfig);
                    case MAINHAND -> new CosmeticMainhandType(id, cosmeticConfig);
                    case EMOTE -> new CosmeticEmoteType(id, cosmeticConfig);
                    default -> new CosmeticArmorType(id, cosmeticConfig);
                }
            } catch (Exception e) {
                if (Settings.isDebugEnabled()) e.printStackTrace();
            }
        }
    }
}