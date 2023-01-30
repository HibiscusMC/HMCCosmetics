package com.hibiscusmc.hmccosmetics.gui.type;

import com.hibiscusmc.hmccosmetics.gui.type.types.TypeCosmetic;
import com.hibiscusmc.hmccosmetics.gui.type.types.TypeEmpty;

import java.util.HashMap;

public class Types {

    private static HashMap<String, Type> types = new HashMap<>();

    private static TypeCosmetic TYPE_COSMETIC = new TypeCosmetic();
    private static TypeEmpty TYPE_EMPTY = new TypeEmpty();

    public static Type getType(String id) {
        return types.get(id.toUpperCase());
    }

    public static boolean isType(String id) {
        return types.containsKey(id.toUpperCase());
    }

    public static void addType(Type type) {
        types.put(type.getId().toUpperCase(), type);
    }
}
