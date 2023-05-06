package com.hibiscusmc.hmccosmetics.gui.type;

import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.spongepowered.configurate.ConfigurationNode;

public abstract class Type {

    private final String id;

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

    public abstract void run(CosmeticUser user, ConfigurationNode config, ClickType clickType);

    public abstract ItemStack setItem(CosmeticUser user, ConfigurationNode config, ItemStack itemStack, int slot);
}
