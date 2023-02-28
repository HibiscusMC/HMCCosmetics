package com.hibiscusmc.hmccosmetics.cosmetic;

import com.hibiscusmc.hmccosmetics.config.serializer.ItemSerializer;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.logging.Level;

public abstract class Cosmetic {

    private String id;
    private String permission;
    private ItemStack item;
    private CosmeticSlot slot;
    private boolean dyable;

    protected Cosmetic(String id, @NotNull ConfigurationNode config) {
        this.id = id;

        if (!config.node("permission").virtual()) {
            this.permission = config.node("permission").getString();
        } else {
            this.permission = null;
        }

        if (!config.node("item").virtual()) this.item = generateItemStack(config.node("item"));

        MessagesUtil.sendDebugMessages("Slot: " + config.node("slot").getString());

        setSlot(CosmeticSlot.valueOf(config.node("slot").getString()));
        setDyable(config.node("dyeable").getBoolean(false));

        MessagesUtil.sendDebugMessages("Dyeable " + dyable);
        Cosmetics.addCosmetic(this);
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public CosmeticSlot getSlot() {
        return this.slot;
    }

    public void setSlot(CosmeticSlot slot) {
        this.slot = slot;
    }

    public String getPermission() {
        return this.permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public boolean requiresPermission() {
        return permission != null;
    }

    public void setDyable(boolean dyable) {
        this.dyable = dyable;
    }

    public boolean isDyable() {
        return this.dyable;
    }

    public abstract void update(CosmeticUser user);

    @Nullable
    public ItemStack getItem() {
        if (item == null) return null;
        return item.clone();
    }

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
}
