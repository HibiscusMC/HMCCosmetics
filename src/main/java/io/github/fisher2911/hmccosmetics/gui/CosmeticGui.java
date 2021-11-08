package io.github.fisher2911.hmccosmetics.gui;

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
import io.github.fisher2911.hmccosmetics.util.builder.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class CosmeticGui {

    private final HMCCosmetics plugin;
    private final MessageHandler messageHandler;
    private final String title;
    private final int rows;
    private final Map<Integer, GuiItem> guiItemMap;
    private Gui gui;

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
    }

    private void setItems(final User user) {

        final Player player = user.getPlayer();

        if (player == null) {
            return;
        }

        for (final var entry : guiItemMap.entrySet()) {
            final int slot = entry.getKey();

            final GuiItem guiItem = entry.getValue();

            if (guiItem instanceof final ArmorItem armorItem) {

                final Map<String, String> placeholders = new HashMap<>();

                final PlayerArmor playerArmor = user.getPlayerArmor();

                final ArmorItem hat = playerArmor.getHat();
                final ArmorItem backpack = playerArmor.getBackpack();

                final ArmorItem.Type type = armorItem.getType();

                final String id = switch (type) {
                    case HAT -> hat.getId();
                    case BACKPACK -> backpack.getId();
                };

                placeholders.put(
                        Placeholder.ENABLED,
                        String.valueOf(id.equals(armorItem.getId())).
                                toLowerCase(Locale.ROOT));

                placeholders.put(
                        Placeholder.ALLOWED,
                        String.valueOf(
                                        player.hasPermission(armorItem.getPermission())).
                                toUpperCase(Locale.ROOT)
                );

                this.gui.setItem(slot,
                        new GuiItem(
                                ItemBuilder.from(
                                                armorItem.getItemStack()
                                        ).namePlaceholders(placeholders).
                                        lorePlaceholders(placeholders).
                                        build(),
                                event -> {
                                    final String permission = armorItem.getPermission();

                                    if (permission != null &&
                                            !permission.isBlank() &&
                                            !player.hasPermission(armorItem.getPermission())) {
                                        this.messageHandler.sendMessage(
                                                player,
                                                Messages.NO_PERMISSION
                                        );
                                        return;
                                    }

                                    this.setUserArmor(player, user, armorItem);
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
            final ArmorItem armorItem) {

        if (player == null) {
            return;
        }

        final ArmorItem.Type type = armorItem.getType();

        switch (type) {
            case HAT -> user.setOrUnsetHat(armorItem);
            case BACKPACK -> user.setOrUnsetBackpack(armorItem);
        }
    }

    public void open(final HumanEntity humanEntity) {
        final Optional<User> optionalUser = this.plugin.getUserManager().get(humanEntity.getUniqueId());

        if (optionalUser.isEmpty()) {
            return;
        }

        final User user = optionalUser.get();

        this.gui = Gui.gui().
                title(Adventure.MINI_MESSAGE.parse(this.title)).
                rows(this.rows).
                create();

        this.gui.setDefaultClickAction(event -> {
            event.setCancelled(true);
            this.setItems(user);
            this.gui.update();
        });
        this.setItems(user);

        this.gui.open(humanEntity);
    }
}
