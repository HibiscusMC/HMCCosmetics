package io.github.fisher2911.hmccosmetics.util.builder;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.UUID;

/**
 * Some parts taken from https://github.com/TriumphTeam/triumph-gui/blob/master/core/src/main/java/dev/triumphteam/gui/builder/item/SkullBuilder.java
 */

public class SkullBuilder extends ItemBuilder {

    private static final Field PROFILE_FIELD;

    static {
        Field field;

        try {
            final SkullMeta skullMeta = (SkullMeta) new ItemStack(Material.PLAYER_HEAD).getItemMeta();
            field = skullMeta.getClass().getDeclaredField("profile");
            field.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            field = null;
        }

        PROFILE_FIELD = field;
    }


    /**
     *
     * @param material The material
     */

    SkullBuilder(final Material material) {
        super(material);
    }

    /**
     * Creates a new SkullBuilder instance
     * @return this
     */

    public static SkullBuilder create() {
        return new SkullBuilder(Material.PLAYER_HEAD);
    }


    /**
     *
     * @param player skull owner
     * @return this
     */
    public SkullBuilder owner(final OfflinePlayer player) {
        if (this.itemMeta instanceof final SkullMeta skullMeta) {
            skullMeta.setOwningPlayer(player);
            this.itemMeta = skullMeta;
        }
        return this;
    }

    /**
     *
     * @param texture skull texture
     * @return this
     */

    public SkullBuilder texture(@NotNull final String texture) {
        if (PROFILE_FIELD == null) {
            return this;
        }

        final SkullMeta skullMeta = (SkullMeta) this.itemMeta;
        final GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        profile.getProperties().put("textures", new Property("textures", texture));

        try {
            PROFILE_FIELD.set(skullMeta, profile);
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            ex.printStackTrace();
        }

        this.itemMeta = skullMeta;
        return this;
    }

}
