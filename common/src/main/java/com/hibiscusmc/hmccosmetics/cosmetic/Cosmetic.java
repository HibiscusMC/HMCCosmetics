package com.hibiscusmc.hmccosmetics.cosmetic;

import com.hibiscusmc.hmccosmetics.cosmetic.behavior.CosmeticUpdateBehavior;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import me.lojosho.hibiscuscommons.config.serializer.ItemSerializer;
import me.lojosho.shaded.configurate.ConfigurationNode;
import me.lojosho.shaded.configurate.serialize.SerializationException;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.logging.Level;

@Getter
@Setter
public abstract class Cosmetic {
    protected static ItemStack UNDEFINED_DISPLAY_ITEM_STACK;

    static {
        UNDEFINED_DISPLAY_ITEM_STACK = new ItemStack(Material.BARRIER);

        ItemMeta meta = UNDEFINED_DISPLAY_ITEM_STACK.getItemMeta();
        if (meta != null) {
            // Legacy methods for Spigot >:(
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&cUndefined Item Display"));
            meta.setLore(List.of(
                    ChatColor.translateAlternateColorCodes('&', "&cPlease check your configurations & console to"),
                    ChatColor.translateAlternateColorCodes('&', "&censure there are no errors.")));
        }
        UNDEFINED_DISPLAY_ITEM_STACK.setItemMeta(meta);
    }

    /** Identifier of the cosmetic. */
    private String id;

    /** Permission to use the cosmetic. */
    private String permission;

    /** The display {@link ItemStack} of the cosmetic. */
    @Getter(AccessLevel.NONE) @Setter(AccessLevel.NONE)
    private ItemStack item;

    /** The material string of the cosmetic. */
    private String material;

    /** The {@link CosmeticSlot} this cosmetic occupies. */
    private CosmeticSlot slot;

    /** Whether the cosmetic is dyeable or not. */
    private boolean dyeable;

    /** The config for the cosmetic */
    private ConfigurationNode config;

    protected Cosmetic(@NotNull String id, @NotNull ConfigurationNode config) {
        this.id = id;
        this.config = config;

        if (!config.node("permission").virtual()) {
            this.permission = config.node("permission").getString();
        } else {
            this.permission = null;
        }

        if (!config.node("item").virtual()) {
            this.material = config.node("item", "material").getString();
            try {
                this.item = generateItemStack(config.node("item"));
            } catch(Exception ex) {
                MessagesUtil.sendDebugMessages("Forcing %s to use undefined display".formatted(getId()));
                this.item = UNDEFINED_DISPLAY_ITEM_STACK;
            }
        }

        MessagesUtil.sendDebugMessages("Slot: " + config.node("slot").getString());
        this.slot = CosmeticSlot.valueOf(config.node("slot").getString());

        this.dyeable = config.node("dyeable").getBoolean(false);
        MessagesUtil.sendDebugMessages("Dyeable " + dyeable);
    }

    protected Cosmetic(String id, String permission, ItemStack item, String material, CosmeticSlot slot, boolean dyeable) {
        this.id = id;
        this.permission = permission;
        this.item = item;
        this.material = material;
        this.slot = slot;
        this.dyeable = dyeable;
    }

    public boolean requiresPermission() {
        return permission != null && !permission.isBlank();
    }

    /**
     * Dispatched when an update is requested upon the cosmetic. Instead, you should use {@link CosmeticUser#updateCosmetic(CosmeticSlot)})}
     * @param user the user to preform the update against
     */
    @Deprecated(since = "2.8.2")
    public void update(CosmeticUser user) {
        if(this instanceof CosmeticUpdateBehavior behavior) {
            behavior.dispatchUpdate(user);
        }
    }

    /**
     * Action preformed on the update. Instead, you should use {@link CosmeticUser#updateCosmetic(CosmeticSlot)})}
     * @param user the user to preform the update against
     */
    @Deprecated(since = "2.8.2")
    protected void doUpdate(final CosmeticUser user) {
        // #update should be the preferred way of interacting with this api now.
        this.update(user);
    }

    @Nullable
    public ItemStack getItem() {
        if (item == null) return null;
        return item.clone();
    }

    /**
     * Generate an {@link ItemStack} from a {@link ConfigurationNode}.
     * @param config the configuration node
     * @return the {@link ItemStack}
     */
    protected ItemStack generateItemStack(ConfigurationNode config) {
        try {
            ItemStack item = ItemSerializer.INSTANCE.deserialize(ItemStack.class, config);
            if (item == null) {
                MessagesUtil.sendDebugMessages("Unable to create item for " + getId(), Level.SEVERE);
                return new ItemStack(Material.AIR);
            }
            return item;
        } catch (SerializationException e) {
            MessagesUtil.sendDebugMessages("Fatal error encountered for " + getId() + " regarding Serialization of item", Level.SEVERE);
            throw new RuntimeException(e);
        }
    }

    /**
     * While cosmetics registered in HMCC are made through a configuration, cosmetics registered from other plugins
     * may not and instead opt for {@link Cosmetic#Cosmetic(String, String, ItemStack, String, CosmeticSlot, boolean)}, which doesn't use a config.
     * This should be used only for reference.
     */
    @ApiStatus.Experimental
    public @Nullable ConfigurationNode getConfig() {
        return config;
    }
}
