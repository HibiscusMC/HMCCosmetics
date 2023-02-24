package com.hibiscusmc.hmccosmetics.gui.action;

import com.hibiscusmc.hmccosmetics.gui.action.actions.*;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;

public class Actions {

    private static final HashMap<String, Action> actions = new HashMap<>();

    // [ID]
    private static ActionMessage ACTION_MESSAGE = new ActionMessage();
    private static ActionMenu ACTION_MENU = new ActionMenu();
    private static ActionPlayerCommand ACTION_CONSOLE_COMMAND = new ActionPlayerCommand();
    private static ActionConsoleCommand ACTION_PLAYER_COMMAND = new ActionConsoleCommand();
    private static ActionCloseMenu ACTION_EXIT_MENU = new ActionCloseMenu();
    private static ActionSound ACTION_SOUND = new ActionSound();
    private static ActionEquip ACTION_EQUIP = new ActionEquip();
    private static ActionUnequip ACTION_UNEQUIP = new ActionUnequip();
    private static ActionParticle ACTION_PARTICLE = new ActionParticle();
    private static ActionCosmeticShow ACTION_SHOW = new ActionCosmeticShow();
    private static ActionCosmeticHide ACTION_HIDE = new ActionCosmeticHide();
    private static ActionCosmeticToggle ACTION_TOGGLE = new ActionCosmeticToggle();


    public static Action getAction(@NotNull String id) {
        return actions.get(id.toUpperCase());
    }

    public static boolean isAction(@NotNull String id) {
        return actions.containsKey(id.toUpperCase());
    }

    public static void addAction(Action action) {
        actions.put(action.getId().toUpperCase(), action);
    }

    public static void runActions(CosmeticUser user, @NotNull List<String> raw) {
        for (String a : raw) {
            String id = StringUtils.substringBetween(a, "[", "]").toUpperCase();
            String message = StringUtils.substringAfter(a, "] ");
            MessagesUtil.sendDebugMessages("ID is " + id + " // Raw Data is " + message);
            if (isAction(id)) {
                getAction(id).run(user, message);
            } else {
                MessagesUtil.sendDebugMessages("Possible ids: " + actions.keySet());
            }
        }
    }
}
