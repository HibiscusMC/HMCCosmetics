package com.hibiscusmc.hmccosmetics.nms;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;

public class NMSHandlers {

    private static final String[] SUPPORTED_VERSION = new String[]{"v1_18_R2", "v1_19_R1", "v1_19_R2", "v1_19_R3", "v1_20_R1", "v1_20_R2", "v1_20_R3"};
    private static NMSHandler handler;
    @Getter
    private static String version;

    @Nullable
    public static NMSHandler getHandler() {
        if (handler != null) {
            return handler;
        } else {
            setup();
        }
        return handler;
    }

    public static boolean isVersionSupported() {
        if (getHandler() == null) return false;
        return getHandler().getSupported();
    }

    public static void setup() {
        if (handler != null) return;
        final String packageName = HMCCosmeticsPlugin.getInstance().getServer().getClass().getPackage().getName();
        String packageVersion = packageName.substring(packageName.lastIndexOf('.') + 1);

        for (String selectedVersion : SUPPORTED_VERSION) {
            if (!selectedVersion.contains(packageVersion)) {
                continue;
            }
            MessagesUtil.sendDebugMessages(packageVersion + " has been detected.", Level.INFO);
            version = packageVersion;
            try {
                handler = (NMSHandler) Class.forName("com.hibiscusmc.hmccosmetics.nms." + packageVersion + ".NMSHandler").getConstructor().newInstance();
                return;
            } catch (ClassNotFoundException | InvocationTargetException | InstantiationException |
                     IllegalAccessException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
