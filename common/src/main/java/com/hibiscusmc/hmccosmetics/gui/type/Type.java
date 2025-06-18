package com.hibiscusmc.hmccosmetics.gui.type;

import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticHolder;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import me.lojosho.hibiscuscommons.HibiscusCommonsPlugin;
import me.lojosho.hibiscuscommons.hooks.Hooks;
import me.lojosho.shaded.configurate.ConfigurationNode;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

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

    @Contract("_, _ -> param2")
    @NotNull
    @SuppressWarnings("Duplicates")
    protected ItemMeta processItemMeta(Player viewer, @NotNull ItemMeta itemMeta) {
        // New implementation - Paper and forks
        // I know this is a kinda batshit way to do it (with serializing and deserializing MiniMessage)
        // But currently I can't think of another good way of doing it without a big refactor. I'll come back to this
        // At a later date.
        if (HibiscusCommonsPlugin.isOnPaper()) {
            List<Component> processedLore = new ArrayList<>();
            if (itemMeta.hasDisplayName()) {
                String displayName = MiniMessage.miniMessage().serialize(itemMeta.displayName());
                displayName = Hooks.processPlaceholders(viewer, displayName);
                itemMeta.displayName(MiniMessage.miniMessage().deserialize(displayName).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            }

            if (itemMeta.hasLore()) {
                for (Component loreLine : itemMeta.lore()) {
                    String loreStringLine = MiniMessage.miniMessage().serialize(loreLine);
                    loreStringLine = Hooks.processPlaceholders(viewer, loreStringLine);
                    processedLore.add(MiniMessage.miniMessage().deserialize(loreStringLine).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                }
            }

            if (itemMeta instanceof SkullMeta skullMeta) {
                if (skullMeta.hasOwner() && skullMeta.getOwner() != null) {
                    skullMeta.setOwner(Hooks.processPlaceholders(viewer, skullMeta.getOwner()));
                }
            }

            itemMeta.lore(processedLore);
        } else {
            // Legacy implementation - Spigot
            List<String> processedLore = new ArrayList<>();
            if (itemMeta.hasDisplayName()) {
                itemMeta.setDisplayName(Hooks.processPlaceholders(viewer, itemMeta.getDisplayName()));
            }

            if (itemMeta.hasLore()) {
                for (String loreLine : itemMeta.getLore()) {
                    processedLore.add(Hooks.processPlaceholders(viewer, loreLine));
                }
            }

            if (itemMeta instanceof SkullMeta skullMeta) {
                if (skullMeta.hasOwner() && skullMeta.getOwner() != null) {
                    skullMeta.setOwner(Hooks.processPlaceholders(viewer, skullMeta.getOwner()));
                }
            }
            itemMeta.setLore(processedLore);
        }

        return itemMeta;
    }
}
