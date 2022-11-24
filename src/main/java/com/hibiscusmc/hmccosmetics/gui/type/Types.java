package com.hibiscusmc.hmccosmetics.gui.type;

import com.hibiscusmc.hmccosmetics.gui.type.types.CosmeticType;

import java.util.HashMap;

public class Types {

    private static HashMap<String, Type> types = new HashMap<>();

    private static CosmeticType COSMETIC_TYPE = new CosmeticType();

    public static Type getType(String id) {
        return types.get(id);
    }

    public static boolean isType(String id) {
        return types.containsKey(id);
    }

    public static void addType(Type type) {
        types.put(type.getId(), type);
    }
}
