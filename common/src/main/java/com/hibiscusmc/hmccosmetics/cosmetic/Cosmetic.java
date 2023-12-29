package com.hibiscusmc.hmccosmetics.cosmetic;

import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;
import lombok.Getter;
import lombok.Setter;
import me.lojosho.hibiscuscommons.config.serializer.ItemSerializer;
import me.lojosho.shaded.configurate.ConfigurationNode;
import me.lojosho.shaded.configurate.serialize.SerializationException;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Level;

public abstract class Cosmetic {

    @Getter @Setter
    private String id;
    @Getter @Setter
    private String permission;
    private ItemStack item;
    @Getter @Setter
    private String material;
    @Getter @Setter
    private CosmeticSlot slot;
    @Getter @Setter
    private boolean dyable;

    protected Cosmetic(String id, @NotNull ConfigurationNode config) {
        this.id = id;

        if (!config.node("permission").virtual()) {
            this.permission = config.node("permission").getString();
        } else {
            this.permission = null;
        }

        if (!config.node("item").virtual()) {
            this.material = config.node("item", "material").getString();
            this.item = generateItemStack(config.node("item"));
        }

        MessagesUtil.sendDebugMessages("Slot: " + config.node("slot").getString());

        setSlot(CosmeticSlot.valueOf(config.node("slot").getString()));
        setDyable(config.node("dyeable").getBoolean(false));

        MessagesUtil.sendDebugMessages("Dyeable " + dyable);
        Cosmetics.addCosmetic(this);
    }

    public boolean requiresPermission() {
        return permission != null;
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
