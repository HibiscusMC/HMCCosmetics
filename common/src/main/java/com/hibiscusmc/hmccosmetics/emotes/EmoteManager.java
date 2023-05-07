package com.hibiscusmc.hmccosmetics.emotes;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.ticxo.playeranimator.api.PlayerAnimator;
import com.ticxo.playeranimator.api.animation.pack.AnimationPack;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Manages Emotes
 */
@SuppressWarnings("SpellCheckingInspection")
public class EmoteManager {
    private static final @NotNull Map<@NotNull String, @NotNull String> emotes = new HashMap<>();

    /**
     * Loads all BlockBench animations from the emotes folder and puts it into the animation manager registry and local registry
     */
    public static void loadEmotes() {
        // Clear the PlayerAnimator and local registries
        PlayerAnimator.api.getAnimationManager().clearRegistry();
        emotes.clear();

        // Get the emote directory and check if it exists
        File emoteDir = new File(HMCCosmeticsPlugin.get().getDataFolder().getPath() + "/emotes/");
        if (!emoteDir.exists()) return;

        // Get all the files inside the directory and check if it isn't 0
        File[] emoteFiles = emoteDir.listFiles();
        if (emoteFiles == null || emoteFiles.length == 0) return;

        // Remove any files that don't have the file extension ".bbmodel" and check if there are still resulting files
        emoteFiles = Arrays.stream(emoteFiles).filter(file -> file.getPath().endsWith(".bbmodel")).distinct().toArray(File[]::new);
        if (emoteFiles.length == 0) return;

        // Loop through all files, importing all block bench animations into the registry
        for (File animationFile : emoteFiles) {
            String animationKey = FilenameUtils.removeExtension(animationFile.getName());
            PlayerAnimator.api.getAnimationManager().importAnimations(animationKey, animationFile);
        }

        // Loops through all the entries in the registries and unpacks any animation packs to ensure if there were multiple animations
        // inside a singular file, that they are added to the local registry individually for tab completion
        for (Map.Entry<String, AnimationPack> packEntry : PlayerAnimator.api.getAnimationManager().getRegistry().entrySet()) {
            packEntry.getValue().getAnimations().keySet().forEach(animationName -> {
                // API key is the format "animationKey.animationFileName.animationName"
                String apiKey = packEntry.getKey().replace(":", ".") + "." + animationName;
                emotes.put(animationName, apiKey);
            });
        }
    }

    /**
     * Returns true if there is an animation with the specified name
     * @param animationName Name whose presence is to be tested
     * @return True if this registry contains a mapping for the specified name
     */
    public static boolean has(@NotNull String animationName) {
        return emotes.containsKey(animationName);
    }

    /**
     * Returns the {@code API key} to which the specified name is mapped, or {@code null} if this map contains no mapping for the name.
     * @param animationName Name whose {@code API key} is to be fetched
     * @return The {@code API key} of the specified name or {@code null} if there was no animation name found
     */
    public static @Nullable String get(@NotNull String animationName) {
        return emotes.get(animationName);
    }

    /**
     * Gets a set of all the laoded animation names
     * @return A set of all loaded animation names
     */
    public static @NotNull Set<String> getAllNames() {
        return emotes.keySet();
    }
}
