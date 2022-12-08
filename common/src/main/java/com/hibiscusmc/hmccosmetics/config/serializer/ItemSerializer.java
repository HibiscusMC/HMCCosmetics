package com.hibiscusmc.hmccosmetics.config.serializer;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.hooks.items.ItemHooks;
import com.hibiscusmc.hmccosmetics.util.builder.ColorBuilder;
import com.hibiscusmc.hmccosmetics.util.misc.StringUtils;
import com.hibiscusmc.hmccosmetics.util.misc.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class ItemSerializer implements TypeSerializer<ItemStack> {

    public static final ItemSerializer INSTANCE = new ItemSerializer();
    private static final String MATERIAL = "material";
    private static final String AMOUNT = "amount";
    private static final String NAME = "name";
    private static final String UNBREAKABLE = "unbreakable";
    private static final String GLOWING = "glowing";
    private static final String LORE = "lore";
    private static final String MODEL_DATA = "model-data";
    private static final String ENCHANTS = "enchants";
    private static final String ITEM_FLAGS = "item-flags";
    private static final String TEXTURE = "texture";
    private static final String OWNER = "owner";
    private static final String COLOR = "color";
    private static final String RED = "red";
    private static final String GREEN = "green";
    private static final String BLUE = "blue";

    private ItemSerializer() {
    }

    @Override
    public ItemStack deserialize(final Type type, final ConfigurationNode source)
            throws SerializationException {
        final ConfigurationNode materialNode = source.node(MATERIAL);
        final ConfigurationNode amountNode = source.node(AMOUNT);
        final ConfigurationNode nameNode = source.node(NAME);
        final ConfigurationNode unbreakableNode = source.node(UNBREAKABLE);
        final ConfigurationNode glowingNode = source.node(GLOWING);
        final ConfigurationNode loreNode = source.node(LORE);
        final ConfigurationNode modelDataNode = source.node(MODEL_DATA);
        final ConfigurationNode enchantsNode = source.node(ENCHANTS);
        final ConfigurationNode itemFlagsNode = source.node(ITEM_FLAGS);
        final ConfigurationNode textureNode = source.node(TEXTURE);
        final ConfigurationNode ownerNode = source.node(OWNER);
        final ConfigurationNode colorNode = source.node(COLOR);
        final ConfigurationNode redNode = colorNode.node(RED);
        final ConfigurationNode greenNode = colorNode.node(GREEN);
        final ConfigurationNode blueNode = colorNode.node(BLUE);

        if (materialNode.virtual()) return null;

        String material = materialNode.getString();

        ItemStack item = ItemHooks.getItem(material);
        if (item == null) {
            HMCCosmeticsPlugin.getInstance().getLogger().severe("Invalid Material -> " + material);
            return new ItemStack(Material.AIR);
        }
        item.setAmount(amountNode.getInt(1));

        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta == null) return item;
        if (!nameNode.virtual()) itemMeta.setDisplayName(StringUtils.parseStringToString(Utils.replaceIfNull(nameNode.getString(), "")));
        if (!unbreakableNode.virtual()) itemMeta.setUnbreakable(unbreakableNode.getBoolean());
        if (!glowingNode.virtual()) {
            itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            itemMeta.addEnchant(Enchantment.LUCK, 1, true);
        }
        if (!loreNode.virtual()) itemMeta.setLore(Utils.replaceIfNull(loreNode.getList(String.class),
                            new ArrayList<String>()).
                    stream().map(StringUtils::parseStringToString).collect(Collectors.toList()));
        if (!modelDataNode.virtual()) itemMeta.setCustomModelData(modelDataNode.getInt());

        if (!enchantsNode.virtual()) {
            for (ConfigurationNode enchantNode : enchantsNode.childrenMap().values()) {
                if (Enchantment.getByKey(NamespacedKey.minecraft(enchantNode.key().toString())) == null) continue;
                itemMeta.addEnchant(Enchantment.getByKey(NamespacedKey.minecraft(enchantNode.key().toString())), enchantNode.getInt(1), true);
            }
        }

        if (!itemFlagsNode.virtual()) {
            for (ConfigurationNode flagNode : itemFlagsNode.childrenMap().values()) {
                if (ItemFlag.valueOf(flagNode.key().toString()) == null) continue;
                itemMeta.addItemFlags(ItemFlag.valueOf(flagNode.key().toString()));
            }
        }

        if (item.getType() == Material.PLAYER_HEAD) {
            SkullMeta skullMeta = (SkullMeta) itemMeta;
            if (!ownerNode.virtual()) {
                skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(ownerNode.getString()));
            }

            if (!textureNode.virtual()) {
                Bukkit.getUnsafe().modifyItemStack(item, "{SkullOwner:{Id:[I;0,0,0,0],Properties:{textures:[{Value:\""
                        + textureNode.getString() + "\"}]}}}");
                itemMeta = skullMeta;
            }
        }

        if (!colorNode.virtual()) {
            if (ColorBuilder.canBeColored(item.getType())) {
                itemMeta = ColorBuilder.color(itemMeta, Color.fromRGB(redNode.getInt(0), greenNode.getInt(0), blueNode.getInt(0)));
            }
        }

        NamespacedKey key = new NamespacedKey(HMCCosmeticsPlugin.getInstance(), source.key().toString());
        itemMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, source.key().toString());

        item.setItemMeta(itemMeta);
        return item;
    }

    @Override
    public void serialize(final Type type, @Nullable final ItemStack obj, final ConfigurationNode node) throws SerializationException {

    }

}
