package com.hibiscusmc.hmccosmetics.nms;

import java.lang.reflect.InvocationTargetException;

public class NMSHandlers {

    private static final String[] SUPPORTED_VERSION = new String[]{"v1_19_R1"};
    private static NMSHandler handler;

    public static NMSHandler getHandler() {
        if (handler != null) {
            return handler;
        } else {
            setup();
        }
        return handler;
    }

    public static void setup() {
        if (handler != null) return;
        for (String version : SUPPORTED_VERSION) {
            try {
                //Class.forName("org.bukkit.craftbukkit." + version + ".block.CraftBlock").getName();
                handler = (NMSHandler) Class.forName("com.hibiscusmc.hmccosmetics.nms." + version + ".NMSHandler").getConstructor().newInstance();
            } catch (ClassNotFoundException | InvocationTargetException | InstantiationException |
                     IllegalAccessException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
