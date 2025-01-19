package com.hibiscusmc.hmccosmetics.cosmetic;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CosmeticSlot {
    private static final ConcurrentHashMap<String, CosmeticSlot> REGISTRY = new ConcurrentHashMap<>();

    public static final CosmeticSlot HELMET = new CosmeticSlot("HELMET");
    public static final CosmeticSlot CHESTPLATE = new CosmeticSlot("CHESTPLATE");
    public static final CosmeticSlot LEGGINGS = new CosmeticSlot("LEGGINGS");
    public static final CosmeticSlot BOOTS = new CosmeticSlot("BOOTS");
    public static final CosmeticSlot MAINHAND = new CosmeticSlot("MAINHAND");
    public static final CosmeticSlot OFFHAND = new CosmeticSlot("OFFHAND");
    public static final CosmeticSlot BACKPACK = new CosmeticSlot("BACKPACK");
    public static final CosmeticSlot BALLOON = new CosmeticSlot("BALLOON");
    public static final CosmeticSlot EMOTE = new CosmeticSlot("EMOTE");
    public static final CosmeticSlot CUSTOM = new CosmeticSlot("CUSTOM");

    private final String name;

    private CosmeticSlot(@NotNull String name) {
        this.name = name;
        REGISTRY.put(name, this);
    }

    /**
     * Registers a new slot with the given name. If a slot with the given name already exists, it will be returned.
     * @param name The name of the slot (This will automatically be converted into all UPPERCASE)
     * @return The slot that was registered or already exists.
     */
    @NotNull
    public static CosmeticSlot register(@NotNull String name) {
        name = name.toUpperCase();
        return REGISTRY.computeIfAbsent(name, key -> new CosmeticSlot(key));
    }

    /**
     * Returns an unmodifiable map of all the slots that have been registered.
     * @return An unmodifiable map of all the slots that have been registered.
     */
    @NotNull
    public static Map<String, CosmeticSlot> values() {
        return Collections.unmodifiableMap(REGISTRY);
    }

    /**
     * Gets the slot with the given name.
     * @param name The name of the slot to get. This is automatically converted to all UPPERCASE.
     * @return The slot with the given name, or null if it does not exist.
     */
    @Nullable
    public static CosmeticSlot valueOf(@NotNull String name) {
        name = name.toUpperCase();
        return REGISTRY.get(name);
    }

    /**
     * Checks if the registry contains a slot with the given name.
     * @param name The name of the slot to check for. This is automatically converted to all UPPERCASE.
     * @return True if the slot exists, false otherwise.
     */
    public static boolean contains(@NotNull String name) {
        name = name.toUpperCase();
        return REGISTRY.containsKey(name);
    }

    @Override
    public String toString() {
        return name;
    }
}
