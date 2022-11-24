package com.hibiscusmc.hmccosmetics.gui.type.types;

import com.hibiscusmc.hmccosmetics.gui.type.Type;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import org.spongepowered.configurate.ConfigurationNode;

public class TypeEmpty extends Type {

    public TypeEmpty() {
        super("empty");
    }

    @Override
    public void run(CosmeticUser user, ConfigurationNode config) {
        // Nothing
    }
}
