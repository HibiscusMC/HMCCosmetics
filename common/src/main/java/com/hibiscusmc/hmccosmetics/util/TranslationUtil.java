package com.hibiscusmc.hmccosmetics.util;

import me.lojosho.hibiscuscommons.util.StringUtils;
import me.lojosho.shaded.configurate.ConfigurationNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TranslationUtil {

    // unlocked-cosmetic -> true -> True
    private static final HashMap<String, List<TranslationPair>> keys = new HashMap<>();

    public static void setup(ConfigurationNode config) {
        keys.clear();
        for (ConfigurationNode node : config.childrenMap().values()) {
            ArrayList<TranslationPair> pairs = new ArrayList<>();
            for (ConfigurationNode translatableMessage : node.childrenMap().values()) {
                String key = translatableMessage.key().toString();
                key = key.replaceAll("'", ""); // Autoupdater adds ' to it? Removes it from the key
                TranslationPair pair = new TranslationPair(key, translatableMessage.getString());
                pairs.add(pair);
                MessagesUtil.sendDebugMessages("setupTranslation key:" + node.key().toString() + " | " + node);
                MessagesUtil.sendDebugMessages("Overall Key " + node.key().toString());
                MessagesUtil.sendDebugMessages("Key '" + pair.key() + "' Value '" + pair.value() + "'");
            }
            keys.put(node.key().toString().toLowerCase(), pairs);
        }
    }

    public static String getTranslation(String key, String message) {
        List<TranslationPair> pairs = keys.get(key);
        for (TranslationPair pair : pairs) {
            if (pair.key().equals(message)) return StringUtils.parseStringToString(pair.value());
        }

        return message;
    }
}
