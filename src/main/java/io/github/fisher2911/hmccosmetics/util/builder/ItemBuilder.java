package io.github.fisher2911.hmccosmetics.util.builder;

import io.github.fisher2911.hmccosmetics.message.Placeholder;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ItemBuilder {

    protected Material material;
    protected int amount;
    protected ItemMeta itemMeta;

    /**
     * @param material builder material
     */

    ItemBuilder(final Material material) {
        this.material = material;
        this.itemMeta = Bukkit.getItemFactory().getItemMeta(material);
    }

    /**
     * @param itemStack builder ItemStack
     */

    ItemBuilder(final ItemStack itemStack) {
        this.material = itemStack.getType();
        this.itemMeta = itemStack.hasItemMeta() ? itemStack.getItemMeta() : Bukkit.getItemFactory().getItemMeta(this.material);
    }

    /**
     * @param material builder material
     * @return
     */

    public static ItemBuilder from(final Material material) {
        return new ItemBuilder(material);
    }

    /**
     * @param itemStack builder ItemStack
     * @return
     */

    public static ItemBuilder from(final ItemStack itemStack) {
        return new ItemBuilder(itemStack);
    }

    /**
     * @param amount ItemStack amount
     * @return this
     */

    public ItemBuilder amount(final int amount) {
        this.amount = Math.min(Math.max(1, amount), 64);
        return this;
    }

    /**
     * @param name ItemStack name
     * @return this
     */

    public ItemBuilder name(final String name) {
        if (this.itemMeta == null) {
            return this;
        }
        this.itemMeta.setDisplayName(name);
        return this;
    }

    /**
     * Sets placeholders to the item's name
     *
     * @param placeholders placeholders
     */

    public ItemBuilder namePlaceholders(final Map<String, String> placeholders) {
        if (this.itemMeta == null) {
            return this;
        }

        final String name = Placeholder.
                applyPlaceholders(this.itemMeta.getDisplayName(), placeholders);
        this.itemMeta.setDisplayName(name);
        return this;
    }

    /**
     * @param lore ItemStack lore
     * @return this
     */

    public ItemBuilder lore(final List<String> lore) {
        if (this.itemMeta == null) {
            return this;
        }
        this.itemMeta.setLore(lore);
        return this;
    }

    /**
     * Sets placeholders to the item's lore
     *
     * @param placeholders placeholders
     */


    public ItemBuilder lorePlaceholders(final Map<String, String> placeholders) {
        if (this.itemMeta == null) {
            return this;
        }
        final List<String> lore = new ArrayList<>();

        final List<String> previousLore = this.itemMeta.getLore();

        if (previousLore == null) {
            return this;
        }

        for (final String line : previousLore) {
            lore.add(Placeholder.applyPlaceholders(
                    line, placeholders
            ));
        }

        this.itemMeta.setLore(lore);
        return this;
    }

    public ItemBuilder papiPlaceholders(final Player player) {
        this.lorePapiPlaceholders(player);
        this.namePapiPlaceholders(player);
        return this;
    }

    private void lorePapiPlaceholders(final Player player) {
        if (this.itemMeta == null) return;
        final List<String> newLore = new ArrayList<>();

        final List<String> lore = this.itemMeta.getLore();

        if (lore == null) return;

        for (final String line : this.itemMeta.getLore()) {
            newLore.add(Placeholder.applyPapiPlaceholders(player, line));
        }

        this.itemMeta.setLore(newLore);
    }

    private void namePapiPlaceholders(final Player player) {
        if (this.itemMeta == null) return;

        this.itemMeta.setDisplayName(
                Placeholder.applyPapiPlaceholders(
                        player,
                        this.itemMeta.getDisplayName()
                )
        );
    }

    /**
     * @param unbreakable whether the ItemStack is unbreakable
     * @return this
     */

    public ItemBuilder unbreakable(final boolean unbreakable) {
        if (this.itemMeta == null) {
            return this;
        }
        this.itemMeta.setUnbreakable(unbreakable);
        return this;
    }

    public ItemBuilder glow(final boolean glow) {
        if (this.itemMeta == null) {
            return this;
        }
        if (glow) {
            this.itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            this.itemMeta.addEnchant(Enchantment.LUCK, 1, true);
        }
        return this;
    }

    /**
     * @param enchantments            enchants to be added to the ItemStack
     * @param ignoreLeveLRestrictions whether to ignore enchantment level restrictions
     * @return this
     */

    public ItemBuilder enchants(final Map<Enchantment, Integer> enchantments, boolean ignoreLeveLRestrictions) {
        if (this.itemMeta == null) {
            return this;
        }
        enchantments.forEach((enchantment, level) -> this.itemMeta.addEnchant(enchantment, level, ignoreLeveLRestrictions));
        return this;
    }

    /**
     * @param itemFlags ItemStack ItemFlags
     * @return this
     */

    public ItemBuilder itemFlags(final Set<ItemFlag> itemFlags) {
        if (this.itemMeta == null) {
            return this;
        }
        this.itemMeta.addItemFlags(itemFlags.toArray(new ItemFlag[0]));
        return this;
    }

    /**
     * @param modelData ItemStack modelData
     * @return this
     */

    public ItemBuilder modelData(final int modelData) {
        if (this.itemMeta == null) {
            return this;
        }
        this.itemMeta.setCustomModelData(modelData);
        return this;
    }

    /**
     * @return built ItemStack
     */

    public ItemStack build() {
        final ItemStack itemStack = new ItemStack(this.material, Math.max(this.amount, 1));
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

}
