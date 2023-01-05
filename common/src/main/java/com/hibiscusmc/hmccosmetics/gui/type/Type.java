package com.hibiscusmc.hmccosmetics.gui.type;

import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import org.bukkit.inventory.meta.ItemMeta;
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

    public ItemMeta setLore(CosmeticUser user, ConfigurationNode config, ItemMeta itemMeta) {
        //TODO: Finish this
        return null; // Override
    }
}
