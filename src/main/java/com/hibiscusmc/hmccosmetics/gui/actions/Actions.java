package com.hibiscusmc.hmccosmetics.gui.actions;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.List;

public class Actions {

    private static HashMap<String, Action> actions = new HashMap<>();

    // [ID]
    private static ActionMessage ACTION_MESSAGE = new ActionMessage();

    public static Action getAction(String id) {
        return actions.get(id);
    }

    public static boolean isAction(String id) {
        return actions.containsKey(id);
    }

    public static void addAction(Action action) {
        actions.put(action.getId(), action);
    }

    public static void runActions(CosmeticUser user, List<String> raw) {
        for (String a : raw) {
            String id = StringUtils.substringBetween(a, "[", "]").toUpperCase();
            String message = StringUtils.substringAfter(a, "] ");
            HMCCosmeticsPlugin.getInstance().getLogger().info("ID is " + id + " // Message is " + message);
            if (isAction(id)) {
                getAction(id).run(user, message);
            } else {
                HMCCosmeticsPlugin.getInstance().getLogger().info("Possible ids: " + actions.keySet());
            }
        }
    }

}
