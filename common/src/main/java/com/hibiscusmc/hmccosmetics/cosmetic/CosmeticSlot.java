package com.hibiscusmc.hmccosmetics.cosmetic;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class CosmeticSlot {
    private static final ConcurrentHashMap<String, CosmeticSlot> REGISTRY = new ConcurrentHashMap<>();

    public static final CosmeticSlot HELMET = register("HELMET");
    public static final CosmeticSlot CHESTPLATE = register("CHESTPLATE");
    public static final CosmeticSlot LEGGINGS = register("LEGGINGS");
    public static final CosmeticSlot BOOTS = register("BOOTS");
    public static final CosmeticSlot MAINHAND = register("MAINHAND");
    public static final CosmeticSlot OFFHAND = register("OFFHAND");
    public static final CosmeticSlot BACKPACK = register("BACKPACK");
    public static final CosmeticSlot BALLOON = register("BALLOON");
    public static final CosmeticSlot EMOTE = register("EMOTE");

    private final String name;

    private CosmeticSlot(@NotNull String name) {
        this.name = name;
    }

    /**
     * Registers a new slot with the given name. If a slot with the given name already exists, it will be returned.
     * @param name The name of the slot (This will automatically be converted into all UPPERCASE)
     * @return The slot that was registered or already exists.
     * @throws IllegalArgumentException if a cosmetic slot by that name has already been registered
     */
    @NotNull
    public static CosmeticSlot register(@NotNull String name) {
        final String upperName = name.toUpperCase();
        if(REGISTRY.containsKey(upperName)) {
            throw new IllegalArgumentException("A cosmetic slot with name '" + name + "' is already registered.");
        }

        final CosmeticSlot slot = new CosmeticSlot(upperName);
        REGISTRY.put(upperName, slot);
        return slot;
    }

    /**
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
        final String upperName = name.toUpperCase();
        return REGISTRY.get(upperName);
    }

    /**
     * Checks if the registry contains a slot with the given name.
     * @param name The name of the slot to check for. This is automatically converted to all UPPERCASE.
     * @return True if the slot exists, false otherwise.
     */
    public static boolean contains(@NotNull String name) {
        final String upperName = name.toUpperCase();
        return REGISTRY.containsKey(upperName);
    }

    @Override
    public String toString() {
        return name;
    }
}
