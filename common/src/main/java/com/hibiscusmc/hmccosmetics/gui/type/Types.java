package com.hibiscusmc.hmccosmetics.gui.type;

import com.hibiscusmc.hmccosmetics.gui.type.types.TypeCosmetic;
import com.hibiscusmc.hmccosmetics.gui.type.types.TypeEmpty;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class Types {

    private static final HashMap<String, Type> types = new HashMap<>();

    private static final TypeCosmetic TYPE_COSMETIC = new TypeCosmetic();
    private static final TypeEmpty TYPE_EMPTY = new TypeEmpty();

    /**
     * Get's a Menu Item type. Add custom menu item types with {@link #addType(Type)}
     * @param id A non-case sensitive ID
     * @return The type of Menu Item
     */
    public static Type getType(@NotNull String id) {
        return types.get(id.toUpperCase());
    }

    /**
     * Checks if a type is valid. Add custom menu item types with {@link #addType(Type)}
     * @param id A non-case sensitive ID
     * @return True if exists, False if not.
     */
    public static boolean isType(@NotNull String id) {
        return types.containsKey(id.toUpperCase());
    }

    /**
     * Adds a Menu Item Type to the types HashMap for reference. Menu Types will automatically be added using this method.
     * @param type A non-null {@link Type} that'll be added. ID should be unique; can't be duplicated
     */
    public static boolean addType(@NotNull Type type) {
        String id = type.getId().toUpperCase();
        if (types.containsKey(id)) return false;
        types.put(id, type);
        return true;
    }

    /**
     * Gets the default menu item; {@link TypeEmpty}
     * @return The empty menu type.
     */
    public static TypeEmpty getDefaultType() {
        return TYPE_EMPTY;
    }
}
