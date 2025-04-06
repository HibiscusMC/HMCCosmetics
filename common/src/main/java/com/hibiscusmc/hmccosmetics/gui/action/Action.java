package com.hibiscusmc.hmccosmetics.gui.action;

import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticHolder;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import org.bukkit.entity.Player;
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

    public void run(Player viewer, CosmeticHolder cosmeticHolder, String raw) {
        run(CosmeticHolder.ensureSingleCosmeticUser(viewer, cosmeticHolder), raw);
    }

    /**
     * @deprecated Override {@link #run(Player, CosmeticHolder, String)} instead.
     */
    @Deprecated
    public abstract void run(CosmeticUser user, String raw);
}
