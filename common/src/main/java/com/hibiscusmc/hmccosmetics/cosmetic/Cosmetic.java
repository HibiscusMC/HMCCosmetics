package com.hibiscusmc.hmccosmetics.cosmetic;

import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;
import com.hibiscusmc.hmccosmetics.util.misc.ItemDisplayMetadata;
import lombok.Getter;
import lombok.Setter;
import me.lojosho.hibiscuscommons.config.serializer.ItemSerializer;
import me.lojosho.shaded.configurate.ConfigurationNode;
import me.lojosho.shaded.configurate.serialize.SerializationException;
import org.bukkit.Material;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Arrays;
import java.util.List;
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

    protected ItemDisplayMetadata generateItemDisplayMetadata(ConfigurationNode config) {
        ItemDisplayMetadata metadata = new ItemDisplayMetadata();
        if (!config.virtual()) {
            ConfigurationNode translationNode = config.node("translation");
            ConfigurationNode scaleNode = config.node("scale");
            ConfigurationNode rotationLeftNode = config.node("rotation-left");
            ConfigurationNode rotationRightNode = config.node("rotation-right");
            ConfigurationNode billboardNode = config.node("billboard");
            ConfigurationNode blockLightNode = config.node("block-light");
            ConfigurationNode skyLightNode = config.node("sky-light");
            ConfigurationNode viewRangeNode = config.node("viewrange");
            ConfigurationNode widthNode = config.node("width");
            ConfigurationNode heightNode = config.node("height");
            ConfigurationNode displayTransformNode = config.node("display-transform");
            ConfigurationNode itemstackNode = config.node("item");

            if (!translationNode.virtual()) metadata.translation = stringToVector(translationNode.getString("0,0,0"));
            if (!scaleNode.virtual()) metadata.scale = stringToVector(scaleNode.getString("1,1,1"));
            if (!rotationLeftNode.virtual()) metadata.rotationLeft = stringToQuaternion(rotationLeftNode.getString("0,0,0,1"));
            if (!rotationRightNode.virtual()) metadata.rotationRight = stringToQuaternion(rotationRightNode.getString("0,0,0,1"));
            if (!billboardNode.virtual()) try {
                metadata.billboard = Display.Billboard.valueOf(billboardNode.getString("VERTICAL"));
            } catch (Exception ignored) {}
            if (!blockLightNode.virtual()) metadata.blockLight = blockLightNode.getInt(0);
            if (!skyLightNode.virtual()) metadata.skyLight = skyLightNode.getInt(15);
            if (!viewRangeNode.virtual()) metadata.viewRange = viewRangeNode.getFloat(1);
            if (!widthNode.virtual()) metadata.width = widthNode.getFloat(0);
            if (!heightNode.virtual()) metadata.height = heightNode.getFloat(0);
            if (!displayTransformNode.virtual()) try {
                metadata.displayTransform = ItemDisplay.ItemDisplayTransform.valueOf(displayTransformNode.getString());
            } catch (Exception ignored) {}
            if (!itemstackNode.virtual()) try {
                metadata.itemStack = ItemSerializer.INSTANCE.deserialize(ItemStack.class, itemstackNode);
            } catch (Exception ignored) {}
        }

        return metadata;
    }

    protected Vector3f stringToVector(String string) {
        List<String> vector = Arrays.stream(string.replace(" ", "").split(",", 3)).toList();
        while (vector.size() < 3) vector.add("0");
        float x = Float.parseFloat(vector.get(0)), y = Float.parseFloat(vector.get(1)), z = Float.parseFloat(vector.get(2));
        return new Vector3f(x, y, z);
    }

    protected Quaternionf stringToQuaternion(String string) {
        List<String> vector = Arrays.stream(string.replace(" ", "").split(",", 4)).toList();
        while (vector.size() < 3) vector.add("0");
        if (vector.size() < 4) vector.add("1");
        float x = Float.parseFloat(vector.get(0)), y = Float.parseFloat(vector.get(1)), z = Float.parseFloat(vector.get(2)), w = Float.parseFloat(vector.get(3));
        return new Quaternionf(x, y, z, w);
    }
}
