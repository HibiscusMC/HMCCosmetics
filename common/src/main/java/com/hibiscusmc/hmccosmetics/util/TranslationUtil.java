package com.hibiscusmc.hmccosmetics.util;

import com.google.common.collect.HashBiMap;
import kotlin.Pair;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TranslationUtil {

    private static HashMap<String, String> keys = new HashMap<>();

    public static void setup(ConfigurationNode config) {
        // TODO: Finish this
        /*
        for (ConfigurationNode node : config.childrenMap().values()) {
            HashMap<Pair> translableMessages = new HashMap<>();
            for (ConfigurationNode translatableMessage : node.childrenMap().values()) {
                translableMessages.put( new Pair<>(translatableMessage.key().toString(), translatableMessage.getString()))
                MessagesUtil.sendDebugMessages("setupTranslation key:" + node.key().toString() + " | " + node);
            }
            keys.put(node.key().toString().toLowerCase(), HashMap);
        }
         */
    }

    public static String getTranslation(String key, String message) {
        // TODO: Finish this
        return message;
        /*
        key = key.toLowerCase();
        MessagesUtil.sendDebugMessages("getTranslation key:" + key + " | " + message);
        if (!keys.containsKey(key)) return message;
        List<Pair> config = keys.get(key);
        if (config.getFirst() == message) {
            return config.getSecond().toString();
        }
        return message;
         */
    }
}
