package io.github.fisher2911.hmccosmetics.util;

import io.github.fisher2911.hmccosmetics.HMCCosmetics;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class Keys {

    static HMCCosmetics plugin;

    static {
        plugin = HMCCosmetics.getPlugin(HMCCosmetics.class);
    }

    public static final NamespacedKey ITEM_KEY = new NamespacedKey(plugin, "cosmetic");
    public static final NamespacedKey ARMOR_STAND_KEY = new NamespacedKey(plugin, "armor-stand");

    public static void setKey(final ItemStack itemStack) {
        final ItemMeta itemMeta = itemStack.getItemMeta();

        if (itemMeta == null) {
            return;
        }

        itemMeta.getPersistentDataContainer().set(ITEM_KEY, PersistentDataType.BYTE, (byte) 1);

        itemStack.setItemMeta(itemMeta);
    }

    public static boolean hasKey(final ItemStack itemStack) {
        final ItemMeta itemMeta = itemStack.getItemMeta();

        if (itemMeta == null) {
            return false;
        }

        return itemMeta.getPersistentDataContainer().has(ITEM_KEY, PersistentDataType.BYTE);
    }

}
