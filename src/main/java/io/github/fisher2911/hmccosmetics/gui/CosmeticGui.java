package io.github.fisher2911.hmccosmetics.gui;

import dev.triumphteam.gui.components.GuiAction;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import io.github.fisher2911.hmccosmetics.HMCCosmetics;
import io.github.fisher2911.hmccosmetics.inventory.PlayerArmor;
import io.github.fisher2911.hmccosmetics.message.Adventure;
import io.github.fisher2911.hmccosmetics.message.MessageHandler;
import io.github.fisher2911.hmccosmetics.message.Messages;
import io.github.fisher2911.hmccosmetics.message.Placeholder;
import io.github.fisher2911.hmccosmetics.user.User;
import io.github.fisher2911.hmccosmetics.util.StringUtils;
import io.github.fisher2911.hmccosmetics.util.builder.ItemBuilder;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class CosmeticGui {

    protected final HMCCosmetics plugin;
    protected final MessageHandler messageHandler;
    protected final String title;
    protected final int rows;
    protected final Map<Integer, ItemStack> itemStackMap;
    protected final Map<Integer, GuiItem> guiItemMap;
    protected Gui gui;

    private long lastClicked;

    private static final float COOL_DOWN = 0.5f;

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
            if (guiItem == null) continue;

            this.gui.setItem(slot, guiItem);
        }
    }

    private void setUserArmor(
            final HumanEntity human,
            final User user,
            final ArmorItem armorItem,
            final InventoryClickEvent event,
            final GuiAction<InventoryClickEvent> actionIfSet) {

        final long current = System.currentTimeMillis();
        if ((current - this.lastClicked) / 1000. < COOL_DOWN) return;
        this.lastClicked = current;

        if (!(human instanceof final Player player)) {
            return;
        }

        final ArmorItem.Type type = armorItem.getType();

        final ArmorItem setTo = this.plugin.getUserManager().setOrUnset(
                user,
                armorItem,
                Messages.getRemovedMessage(type),
                Messages.getSetMessage(type)
        );

        if (!setTo.isEmpty()) {
            actionIfSet.execute(event);
        }

        final int slot = event.getSlot();

        final GuiItem guiItem = this.getGuiItem(user, player, slot);

        if (guiItem == null) return;

        this.gui.updateItem(slot, guiItem);
    }

    public void open(final HumanEntity humanEntity) {
        final Optional<User> optionalUser = this.plugin.getUserManager().get(humanEntity.getUniqueId());

        if (optionalUser.isEmpty()) {
            return;
        }

        final User user = optionalUser.get();

        this.gui = Gui.gui().
                title(Adventure.MINI_MESSAGE.parse(StringUtils.applyPapiPlaceholders(user.getPlayer(), this.title))).
                rows(this.rows).
                create();

        this.gui.setDefaultClickAction(event -> event.setCancelled(true));

        this.setItems(user);

        this.gui.open(humanEntity);
    }

    @Nullable
    private GuiItem getGuiItem(final User user, final Player player, final int slot) {
        final GuiItem guiItem = this.guiItemMap.get(slot);

        if (guiItem == null) return null;

        final ItemStack itemStack = this.itemStackMap.get(slot);

        if (itemStack == null) return null;

        if (guiItem instanceof final ArmorItem armorItem) {

            final String permission = armorItem.getPermission() == null ? "" : armorItem.getPermission();

            final boolean hasPermission = permission.isBlank() || player.hasPermission(permission);

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

                        final ArmorItem cosmeticItem = this.plugin.getCosmeticManager().getArmorItem(armorItem.getId());

                        if (cosmeticItem == null) return;

                        this.setUserArmor(player, user, cosmeticItem, event, armorItem.getAction());
                    }
            );
        }

        guiItem.setItemStack(
                ItemBuilder.from(itemStack.clone()).papiPlaceholders(player).build()
        );

        return guiItem;
    }

    protected ItemStack applyPlaceholders(final User user, final Player player, final ArmorItem armorItem, final boolean hasPermission) {
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

    public CosmeticGui copy() {
        return new CosmeticGui(
                this.plugin,
                this.title,
                this.rows,
                new HashMap<>(this.guiItemMap)
        );
    }
}