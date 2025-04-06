package com.hibiscusmc.hmccosmetics.gui.type;

import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticHolder;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import me.lojosho.shaded.configurate.ConfigurationNode;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public abstract class Type {

    private final String id;

    public Type(String id) {
        this.id = id;
        Types.addType(this);
    }

    public String getId() {
        return this.id;
    }

    public void run(Player viewer, CosmeticHolder cosmeticHolder, ConfigurationNode config) {
        run(viewer, cosmeticHolder, config, null);
    }

    public void run(Player viewer, CosmeticHolder cosmeticHolder, ConfigurationNode config, ClickType clickType) {
        run(CosmeticHolder.ensureSingleCosmeticUser(viewer, cosmeticHolder), config, clickType);
    }

    public void run(CosmeticUser user, ConfigurationNode config) {
        final var player = user.getPlayer();
        if (player == null) return;
        run(player, user, config, null);
    }

    /**
     * @deprecated Override {@link #run(Player, CosmeticHolder, ConfigurationNode, ClickType)} instead.
     */
    @Deprecated
    public abstract void run(CosmeticUser user, ConfigurationNode config, ClickType clickType);

    public ItemStack setItem(Player viewer, CosmeticHolder cosmeticHolder, ConfigurationNode config, ItemStack itemStack, int slot) {
        return setItem(CosmeticHolder.ensureSingleCosmeticUser(viewer, cosmeticHolder), config, itemStack, slot);
    }

    /**
     * @deprecated Override {@link #setItem(Player, CosmeticHolder, ConfigurationNode, ItemStack, int)} instead.
     */
    public abstract ItemStack setItem(CosmeticUser user, ConfigurationNode config, ItemStack itemStack, int slot);
}
