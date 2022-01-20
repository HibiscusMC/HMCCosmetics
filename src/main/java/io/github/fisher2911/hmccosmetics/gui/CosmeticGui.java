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
import io.github.fisher2911.hmccosmetics.user.UserManager;
import io.github.fisher2911.hmccosmetics.util.StringUtils;
import io.github.fisher2911.hmccosmetics.util.builder.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class CosmeticGui {

    protected final HMCCosmetics plugin;
    protected final UserManager userManager;
    protected final MessageHandler messageHandler;
    protected final String title;
    protected final int rows;
    protected final Map<Integer, ItemStack> itemStackMap;
    protected final Map<Integer, GuiItem> guiItemMap;
    protected Gui gui;

    public CosmeticGui(
            final HMCCosmetics plugin,
            final String title,
            final int rows,
            final Map<Integer, GuiItem> guiItemMap) {
        this.plugin = plugin;
        this.userManager = this.plugin.getUserManager();
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

            final GuiItem guiItem = entry.getValue();

            final ItemStack itemStack = this.itemStackMap.get(slot);

            if (itemStack == null) continue;

            guiItem.setItemStack(
                    ItemBuilder.from(itemStack.clone()).papiPlaceholders(player).build()
            );

            if (guiItem instanceof final ArmorItem armorItem) {

                final Map<String, String> placeholders = new HashMap<>();

                final PlayerArmor playerArmor = user.getPlayerArmor();

                final ArmorItem hat = playerArmor.getHat();
                final ArmorItem backpack = playerArmor.getBackpack();
                final ArmorItem offHand = playerArmor.getOffHand();

                final ArmorItem.Type type = armorItem.getType();

                final String id = switch (type) {
                    case HAT -> hat.getId();
                    case BACKPACK -> backpack.getId();
                    case OFF_HAND -> offHand.getId();
                };

                placeholders.put(
                        Placeholder.ENABLED,
                        String.valueOf(id.equals(armorItem.getId())).
                                toLowerCase(Locale.ROOT));

                final String permission = armorItem.getPermission() == null ? "" : armorItem.getPermission();

                final boolean hasPermission = permission.isBlank() || player.hasPermission(permission);

                placeholders.put(
                        Placeholder.ALLOWED,
                        String.valueOf(hasPermission).
                                toLowerCase(Locale.ROOT));

                this.gui.setItem(slot,
                        new GuiItem(
                                ItemBuilder.from(
                                                armorItem.getItemStack(hasPermission)
                                        ).namePlaceholders(placeholders).
                                        lorePlaceholders(placeholders).
                                        papiPlaceholders(player).
                                        build(),
                                event -> {
                                    if (!hasPermission) {
                                        this.messageHandler.sendMessage(
                                                player,
                                                Messages.NO_COSMETIC_PERMISSION
                                        );
                                        return;
                                    }

                                    this.setUserArmor(player, user, armorItem, event, armorItem.getAction());
                                }
                        )
                );

                continue;
            }

            this.gui.setItem(slot, guiItem);
        }
    }

    private void setUserArmor(
            final HumanEntity player,
            final User user,
            final ArmorItem armorItem,
            final InventoryClickEvent event,
            final GuiAction<InventoryClickEvent> actionIfSet) {

        if (player == null) {
            return;
        }

        final ArmorItem.Type type = armorItem.getType();

        final ArmorItem setTo = this.userManager.setOrUnset(
                user,
                armorItem,
                Messages.getSetMessage(type),
                Messages.getRemovedMessage(type));
        if (setTo.isEmpty()) return;
        actionIfSet.execute(event);
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

        this.gui.setDefaultClickAction(event -> {
            event.setCancelled(true);
            Bukkit.getScheduler().runTaskLater(
                    this.plugin,
                    () -> {
                        this.setItems(user);
                        this.gui.update();
                    },
                    1);
        });

        this.setItems(user);

        this.gui.open(humanEntity);
    }
}
