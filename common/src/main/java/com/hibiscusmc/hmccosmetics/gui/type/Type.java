package com.hibiscusmc.hmccosmetics.gui.type;

import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import org.bukkit.event.inventory.ClickType;
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
        run(user, config, null);
    }
    public void run(CosmeticUser user, ConfigurationNode config, ClickType clickType) {
        // Override
    }

    public ItemMeta setLore(CosmeticUser user, ConfigurationNode config, ItemMeta itemMeta) {
        //TODO: Finish this
        return null; // Override
    }
}
