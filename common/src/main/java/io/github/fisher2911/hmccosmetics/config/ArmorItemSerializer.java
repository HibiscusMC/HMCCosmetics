package io.github.fisher2911.hmccosmetics.config;

import dev.triumphteam.gui.components.GuiAction;
import dev.triumphteam.gui.guis.GuiItem;
import io.github.fisher2911.hmccosmetics.gui.ArmorItem;
import io.github.fisher2911.hmccosmetics.gui.BalloonItem;
import io.github.fisher2911.hmccosmetics.gui.WrappedGuiItem;
import io.github.fisher2911.hmccosmetics.util.StringUtils;
import io.github.fisher2911.hmccosmetics.util.Utils;
import io.github.fisher2911.hmccosmetics.util.builder.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class ArmorItemSerializer implements TypeSerializer<WrappedGuiItem> {

    public static final ArmorItemSerializer INSTANCE = new ArmorItemSerializer();

    private ArmorItemSerializer() {
    }

    private static final String ITEM = "item";
    private static final String LOCKED_LORE = "locked-lore";
    private static final String LOCKED_ITEM = "locked-item";
    private static final String APPLIED_ITEM = "applied-item";
    private static final String PERMISSION = "permission";
    private static final String TYPE = "type";
    private static final String ACTION = "action";
    private static final String ID = "id";
    private static final String DYEABLE = "dyeable";
    private static final String BALLOON_MODEL_ID = "balloon";

    private ConfigurationNode nonVirtualNode(final ConfigurationNode source, final Object... path)
            throws SerializationException {
        if (!source.hasChild(path)) {
            throw new SerializationException(
                    "Required field " + Arrays.toString(path) + " was not present in node");
        }

        return source.node(path);
    }

    @Override
    public WrappedGuiItem deserialize(final Type type, final ConfigurationNode source)
            throws SerializationException {
        final ConfigurationNode lockedLoreNode = source.node(LOCKED_LORE);
        final ConfigurationNode lockedItemNode = source.node(LOCKED_ITEM);
        final ConfigurationNode appliedItemNode = source.node(APPLIED_ITEM);
        final ConfigurationNode permissionNode = source.node(PERMISSION);
        final ConfigurationNode typeNode = source.node(TYPE);
        final ConfigurationNode actionNode = source.node(ACTION);
        final ConfigurationNode idNode = source.node(ID);
        final ConfigurationNode dyeableNode = source.node(DYEABLE);
        final ConfigurationNode balloonModelIdNode = source.node(BALLOON_MODEL_ID);

        final ItemStack itemStack = Utils.replaceIfNull(
                ItemSerializer.INSTANCE.deserialize(ItemStack.class, source),
                new ItemStack(Material.AIR)
        );
        ItemStack lockedItem = ItemSerializer.INSTANCE.deserialize(ItemStack.class, lockedItemNode);
        if (lockedItem == null) {
            final List<String> lockedLore = Utils.replaceIfNull(lockedLoreNode.getList(String.class),
                            new ArrayList<String>()).
                    stream().map(StringUtils::parseStringToString).collect(Collectors.toList());
            lockedItem = ItemBuilder.from(itemStack.clone()).lore(lockedLore).build();
        }
        ItemStack appliedItem = ItemSerializer.INSTANCE.deserialize(ItemStack.class, appliedItemNode);
        if (appliedItem == null) appliedItem = itemStack.clone();

        final boolean dyeable = dyeableNode.getBoolean();

        final List<CosmeticGuiAction> actions = ActionSerializer.INSTANCE.deserialize(GuiAction.class, actionNode);

        try {
            final ArmorItem.Type cosmeticType = ArmorItem.Type.valueOf(
                    Utils.replaceIfNull(
                            typeNode.getString(), ""
                    ).toUpperCase(Locale.ROOT)
            );

            final String permission = permissionNode.getString();

            if (cosmeticType == ArmorItem.Type.BALLOON) {
                return new BalloonItem(
                        itemStack,
                        actions,
                        Utils.replaceIfNull(idNode.getString(), ""),
                        lockedItem,
                        appliedItem,
                        permission,
                        cosmeticType,
                        dyeable,
                        -1,
                        balloonModelIdNode.getString()
                );
            }

            return new ArmorItem(
                    itemStack,
                    actions,
                    Utils.replaceIfNull(idNode.getString(), ""),
                    lockedItem,
                    appliedItem,
                    permission,
                    cosmeticType,
                    dyeable,
                    -1
            );


        } catch (final IllegalArgumentException exception) {
            final GuiItem guiItem = dev.triumphteam.gui.builder.item.ItemBuilder.from(itemStack).asGuiItem();
            final GuiAction<InventoryClickEvent> guiAction = event -> {
                for (final CosmeticGuiAction action : actions) {
                    action.execute(event, CosmeticGuiAction.When.ALL);
                }
            };
            guiItem.setAction(guiAction);
            return new WrappedGuiItem(guiItem, guiAction);
        }
    }

    @Override
    public void serialize(final Type type, @Nullable final WrappedGuiItem obj, final ConfigurationNode node) throws SerializationException {

    }


}
