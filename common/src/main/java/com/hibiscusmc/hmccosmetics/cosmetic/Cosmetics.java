package com.hibiscusmc.hmccosmetics.cosmetic;

import com.google.common.collect.HashBiMap;
import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.api.events.CosmeticTypeRegisterEvent;
import com.hibiscusmc.hmccosmetics.config.Settings;
import com.hibiscusmc.hmccosmetics.cosmetic.types.*;
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;
import me.lojosho.shaded.configurate.CommentedConfigurationNode;
import me.lojosho.shaded.configurate.ConfigurateException;
import me.lojosho.shaded.configurate.ConfigurationNode;
import me.lojosho.shaded.configurate.yaml.YamlConfigurationLoader;
import org.apache.commons.lang3.EnumUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Stream;

public class Cosmetics {

    private static final HashBiMap<String, Cosmetic> COSMETICS = HashBiMap.create();

    public static void addCosmetic(Cosmetic cosmetic) {
        COSMETICS.put(cosmetic.getId(), cosmetic);
    }

    public static void removeCosmetic(String id) {
        COSMETICS.remove(id);
    }

    public static void removeCosmetic(Cosmetic cosmetic) {
        COSMETICS.remove(cosmetic);
    }

    @Nullable
    public static Cosmetic getCosmetic(String id) {
        return COSMETICS.get(id);
    }

    @Contract(pure = true)
    @NotNull
    public static Set<Cosmetic> values() {
        return COSMETICS.values();
    }

    @Contract(pure = true)
    @NotNull
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

        try (Stream<Path> walkStream = Files.walk(cosmeticFolder.toPath())) {
            walkStream.filter(p -> p.toFile().isFile()).forEach(child -> {
                if (child.toString().contains(".yml") || child.toString().contains(".yaml")) {
                    MessagesUtil.sendDebugMessages("Scanning " + child);
                    // Loads file
                    YamlConfigurationLoader loader = YamlConfigurationLoader.builder().path(child).build();
                    CommentedConfigurationNode root;
                    try {
                        root = loader.load();
                    } catch (ConfigurateException e) {
                        throw new RuntimeException(e);
                    }
                    setupCosmetics(root);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void setupCosmetics(@NotNull CommentedConfigurationNode config) {
        for (ConfigurationNode cosmeticConfig : config.childrenMap().values()) {
            String id = cosmeticConfig.key().toString();
            MessagesUtil.sendDebugMessages("Attempting to add " + id);
            ConfigurationNode slotNode = cosmeticConfig.node("slot");
            if (slotNode.virtual()) {
                MessagesUtil.sendDebugMessages("Unable to create " + id + " because there is no slot defined!", Level.WARNING);
                continue;
            }
            String slot = slotNode.getString("");
            if (!CosmeticSlot.contains(slot)) {
                MessagesUtil.sendDebugMessages("Unable to create " + id + " because " + slotNode.getString() + " is not a valid slot!", Level.WARNING);
                continue;
            }
            switch (slot) {
                case "BALLOON" -> new CosmeticBalloonType(id, cosmeticConfig);
                case "BACKPACK" -> new CosmeticBackpackType(id, cosmeticConfig);
                case "MAINHAND" -> new CosmeticMainhandType(id, cosmeticConfig);
                case "EMOTE" -> new CosmeticEmoteType(id, cosmeticConfig);
                case "HELMET", "CHESTPLATE", "LEGGINGS", "BOOTS", "OFFHAND" -> new CosmeticArmorType(id, cosmeticConfig);
                default -> new CosmeticTypeRegisterEvent(id, cosmeticConfig).callEvent();
            }
        }
    }
}