package com.hibiscusmc.hmccosmetics.gui.type;

import javax.annotation.Nullable;

public enum ShadingType {
    MODERN, TEXT, NONE;

    public static ShadingType fromString(String string, @Nullable ShadingType defaultType) {
        try {
            return ShadingType.valueOf(string);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return defaultType != null ? defaultType : NONE;
        }
    }
}
