package io.github.fisher2911.hmccosmetics.gui;

import dev.triumphteam.gui.components.GuiAction;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import io.github.fisher2911.hmccosmetics.HMCCosmetics;
import io.github.fisher2911.hmccosmetics.config.CosmeticGuiAction;
import io.github.fisher2911.hmccosmetics.inventory.PlayerArmor;
import io.github.fisher2911.hmccosmetics.message.Adventure;
import io.github.fisher2911.hmccosmetics.message.MessageHandler;
import io.github.fisher2911.hmccosmetics.message.Messages;
import io.github.fisher2911.hmccosmetics.message.Placeholder;
import io.github.fisher2911.hmccosmetics.user.User;
import io.github.fisher2911.hmccosmetics.util.builder.ItemBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class CosmeticGui {

    private static final float COOL_DOWN = 0.5f;
    protected final HMCCosmetics plugin;
    protected final MessageHandler messageHandler;
    protected final String title;
    protected final int rows;
    protected final Map<Integer, ItemStack> itemStackMap;
    protected final Map<Integer, GuiItem> guiItemMap;
    protected Gui gui;
    private long lastClicked;

    public CosmeticGui(
            final HMCCosmetics plugin,
            final String title,
            final int rows,
            final Map<Integer, GuiItem> guiItemMap) {
        this.plugin = plugin;
        this.messageHandler = this.plugin.getMessageHandler();
        this.title = title;
        this.rows = rows;
        this.guiItemMap = guiItemMap;
        this.itemStackMap = new HashMap<>();
        this.guiItemMap.forEach((key, value) -> itemStackMap.put(key, value.getItemStack()));

    }

    private void setItems(final User user) {
        final Player player = user.getPlayer();

        if (player == null) {
            return;
        }

        for (final var entry : guiItemMap.entrySet()) {
            final int slot = entry.getKey();

            final GuiItem guiItem = this.getGuiItem(user, player, slot);
            if (guiItem == null) {
                continue;
            }

            this.gui.setItem(slot, guiItem);
        }
    }

    private void setUserArmor(
            final HumanEntity human,
            final User user,
            final ArmorItem armorItem,
            final InventoryClickEvent event,
            final List<CosmeticGuiAction> actions) {

        final long current = System.currentTimeMillis();
        if ((current - this.lastClicked) / 1000. < COOL_DOWN) {
            return;
        }
        this.lastClicked = current;

        if (!(human instanceof final Player player)) {
            return;
        }

        final ArmorItem.Type type = armorItem.getType();

        final User setUser;
        if (user.isWardrobeActive()) {
            setUser = user.getWardrobe();
        } else {
            setUser = user;
        }
        final ArmorItem setTo = this.plugin.getUserManager().setOrUnset(
                setUser,
                armorItem,
                Messages.getRemovedMessage(type),
                Messages.getSetMessage(type)
        );

        if (!setTo.isEmpty()) {
            executeActions(event, actions, CosmeticGuiAction.When.EQUIP);
        } else {
            executeActions(event, actions, CosmeticGuiAction.When.REMOVE);
        }

        final int slot = event.getSlot();

        final GuiItem guiItem = this.getGuiItem(user, player, slot);

        if (guiItem == null) {
            return;
        }

        this.gui.updateItem(slot, guiItem);
    }

    private void executeActions(
            final InventoryClickEvent event,
            final List<CosmeticGuiAction> actions,
            final CosmeticGuiAction.When when
    ) {
        for (final CosmeticGuiAction action : actions) {
            action.execute(event, when);
        }
    }

    public void open(final User user, final Player player) {
        this.gui = Gui.gui().
                title(Adventure.MINI_MESSAGE.deserialize(
                        Placeholder.applyPapiPlaceholders(player, this.title))).
                rows(this.rows).
                create();

        this.gui.setDefaultClickAction(event -> event.setCancelled(true));
        this.gui.setCloseGuiAction(event -> user.setOpenGui(null));

        this.setItems(user);

        this.gui.open(player);
    }

    @Nullable
    private GuiItem getGuiItem(final User user, final Player player, final int slot) {
        final GuiItem guiItem = this.guiItemMap.get(slot);
        if (guiItem == null) {
            return null;
        }
        final ItemStack itemStack = this.itemStackMap.get(slot);
        if (itemStack == null) return null;
        return this.getGuiItem(user, player, guiItem, itemStack);
    }

    @Nullable
    protected GuiItem getGuiItem(final User user, final Player player, final GuiItem guiItem, final ItemStack itemStack) {
        if (guiItem instanceof final ArmorItem armorItem) {
            final String permission =
                    armorItem.getPermission() == null ? "" : armorItem.getPermission();

            final boolean hasPermission = permission.isBlank() || user.hasPermissionToUse(armorItem);

            return new GuiItem(
                    this.applyPlaceholders(user, player, armorItem, hasPermission),
                    event -> {
                        if (!hasPermission) {
                            this.messageHandler.sendMessage(
                                    player,
                                    Messages.NO_COSMETIC_PERMISSION
                            );
                            return;
                        }

                        final ArmorItem cosmeticItem = this.plugin.getCosmeticManager()
                                .getArmorItem(armorItem.getId());

                        if (cosmeticItem == null) {
                            return;
                        }

                        this.setUserArmor(player, user, cosmeticItem, event, armorItem.getActions());
                    }
            );
        }

        guiItem.setItemStack(
                ItemBuilder.from(itemStack.clone()).papiPlaceholders(player).build()
        );

        return guiItem;
    }

    protected ItemStack applyPlaceholders(
            final User user,
            final Player player,
            final ArmorItem armorItem,
            final boolean hasPermission
    ) {
        final Map<String, String> placeholders = new HashMap<>();

        final PlayerArmor playerArmor = user.getPlayerArmor();

        final ArmorItem.Type type = armorItem.getType();

        final String id = playerArmor.getItem(type).getId();

        placeholders.put(
                Placeholder.ENABLED,
                String.valueOf(id.equals(armorItem.getId())).
                        toLowerCase(Locale.ROOT));

        placeholders.put(
                Placeholder.ALLOWED,
                String.valueOf(hasPermission).
                        toLowerCase(Locale.ROOT));

        final ItemStack itemStack;

        if (!hasPermission) {
            itemStack = armorItem.getItemStack(false);
        } else {
            itemStack = armorItem.getColored();
        }

        return ItemBuilder.from(
                        itemStack
                ).namePlaceholders(placeholders).
                lorePlaceholders(placeholders).
                papiPlaceholders(player).
                build();
    }

    public void updateItem(final int slot, final GuiItem guiItem, final User user, final Player player) {
        final ItemStack itemStack = guiItem.getItemStack().clone();
        this.guiItemMap.put(slot, guiItem);
        this.itemStackMap.put(slot, itemStack);
        final GuiItem setItem = this.getGuiItem(user, player, guiItem, itemStack);
        if (setItem == null) return;
        this.gui.updateItem(slot, setItem);
    }

    public CosmeticGui copy() {
        return new CosmeticGui(
                this.plugin,
                this.title,
                this.rows,
                new HashMap<>(this.guiItemMap)
        );
    }

}