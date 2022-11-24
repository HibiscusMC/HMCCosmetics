package com.hibiscusmc.hmccosmetics.gui.actions;

import com.hibiscusmc.hmccosmetics.user.CosmeticUser;

public class Action {

    private String id;

    public Action(String id) {
        this.id = id.toUpperCase();
        Actions.addAction(this);
    }

    public String getId() {
        return this.id;
    }

    public void run(CosmeticUser user, String raw) {
        // Override
    }
}
