package com.hibiscusmc.hmccosmetics.gui.action;

import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import org.jetbrains.annotations.NotNull;

public abstract class Action {

    private final String id;

    public Action(@NotNull String id) {
        this.id = id.toUpperCase();
        Actions.addAction(this);
    }

    public String getId() {
        return this.id;
    }

    public abstract void run(CosmeticUser user, String raw);
}
