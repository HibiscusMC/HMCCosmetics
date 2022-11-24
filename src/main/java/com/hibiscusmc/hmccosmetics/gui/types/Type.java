package com.hibiscusmc.hmccosmetics.gui.types;

import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import org.spongepowered.configurate.ConfigurationNode;

public class Type {

    private String id;

    public Type(String id) {
        this.id = id;
        Types.addType(this);
    }

    public String getId() {
        return this.id;
    }

    public void run(CosmeticUser user, ConfigurationNode config) {
        // Override
    }
}
