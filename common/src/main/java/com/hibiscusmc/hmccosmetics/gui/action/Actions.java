package com.hibiscusmc.hmccosmetics.gui.action;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.gui.action.actions.ActionConsoleCommand;
import com.hibiscusmc.hmccosmetics.gui.action.actions.ActionMenu;
import com.hibiscusmc.hmccosmetics.gui.action.actions.ActionMessage;
import com.hibiscusmc.hmccosmetics.gui.action.actions.ActionPlayerCommand;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.List;

public class Actions {

    private static HashMap<String, Action> actions = new HashMap<>();

    // [ID]
    private static ActionMessage ACTION_MESSAGE = new ActionMessage();
    private static ActionMenu ACTION_MENU = new ActionMenu();
    private static ActionPlayerCommand ACTION_CONSOLE_COMMAND = new ActionPlayerCommand();
    private static ActionConsoleCommand ACTION_PLAYER_COMMAND = new ActionConsoleCommand();


    public static Action getAction(String id) {
        return actions.get(id.toUpperCase());
    }

    public static boolean isAction(String id) {
        return actions.containsKey(id.toUpperCase());
    }

    public static void addAction(Action action) {
        actions.put(action.getId().toUpperCase(), action);
    }

    public static void runActions(CosmeticUser user, List<String> raw) {
        for (String a : raw) {
            String id = StringUtils.substringBetween(a, "[", "]").toUpperCase();
            String message = StringUtils.substringAfter(a, "] ");
            MessagesUtil.sendDebugMessages("ID is " + id + " // Message is " + message);
            if (isAction(id)) {
                getAction(id).run(user, message);
            } else {
                MessagesUtil.sendDebugMessages("Possible ids: " + actions.keySet());
            }
        }
    }
}
