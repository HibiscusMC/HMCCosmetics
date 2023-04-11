package com.hibiscusmc.hmccosmetics.util.misc;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;

public class Keys {
    private static final HMCCosmeticsPlugin plugin = HMCCosmeticsPlugin.getInstance();
    public static final NamespacedKey ITEM_KEY = new NamespacedKey(plugin, "cosmetic");
    public static final NamespacedKey TOKEN_KEY = new NamespacedKey(plugin, "token-key");

    public static void setKey(final @NotNull ItemStack itemStack) {
        final ItemMeta itemMeta = itemStack.getItemMeta();

        if (itemMeta == null) {
            return;
        }

        itemMeta.getPersistentDataContainer().set(ITEM_KEY, PersistentDataType.BYTE, (byte) 1);

        itemStack.setItemMeta(itemMeta);
    }

    public static <T, Z> void setKey(
            final @NotNull ItemStack itemStack,
            final NamespacedKey key,
            final PersistentDataType<T, Z> type,
            final Z value) {
        final ItemMeta itemMeta = itemStack.getItemMeta();

        if (itemMeta == null) {
            return;
        }

        itemMeta.getPersistentDataContainer().set(key, type, value);

        itemStack.setItemMeta(itemMeta);
    }

    public static boolean hasKey(final @NotNull ItemStack itemStack) {
        final ItemMeta itemMeta = itemStack.getItemMeta();

        if (itemMeta == null) {
            return false;
        }

        return itemMeta.getPersistentDataContainer().has(ITEM_KEY, PersistentDataType.BYTE);
    }

    public static <T, Z> boolean hasKey(final @NotNull ItemStack itemStack, final NamespacedKey key, final PersistentDataType<T, Z> type) {
        final ItemMeta itemMeta = itemStack.getItemMeta();

        if (itemMeta == null) {
            return false;
        }

        return itemMeta.getPersistentDataContainer().has(key, type);
    }

    @Nullable
    public static <T, Z> Z getValue(final @NotNull ItemStack itemStack, final NamespacedKey key, final PersistentDataType<T, Z> type) {
        final ItemMeta itemMeta = itemStack.getItemMeta();

        if (itemMeta == null) {
            return null;
        }

        return itemMeta.getPersistentDataContainer().get(key, type);
    }
}
