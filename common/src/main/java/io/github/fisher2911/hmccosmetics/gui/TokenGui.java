package io.github.fisher2911.hmccosmetics.gui;

import dev.triumphteam.gui.guis.GuiItem;
import io.github.fisher2911.hmccosmetics.HMCCosmetics;
import io.github.fisher2911.hmccosmetics.cosmetic.CosmeticManager;
import io.github.fisher2911.hmccosmetics.message.Messages;
import io.github.fisher2911.hmccosmetics.message.Placeholder;
import io.github.fisher2911.hmccosmetics.user.User;
import io.github.fisher2911.hmccosmetics.util.builder.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class TokenGui extends CosmeticGui {

    private final int tokenSlot;
    private final int cosmeticSlot;
    private final CosmeticManager cosmeticManager;

    public TokenGui(final CosmeticGui gui, final int tokenSlot, final int cosmeticSlot) {
        this(gui.plugin, gui.title, gui.rows, gui.guiItemMap, tokenSlot, cosmeticSlot);
    }

    public TokenGui(final HMCCosmetics plugin, final String title, final int rows, final Map<Integer, GuiItem> guiItemMap, final int tokenSlot, final int cosmeticSlot) {
        super(plugin, title, rows, guiItemMap);
        this.tokenSlot = tokenSlot;
        this.cosmeticSlot = cosmeticSlot;
        this.cosmeticManager = this.plugin.getCosmeticManager();
    }

    @Override
    public void open(final User user, final Player player) {
        super.open(user, player);
        this.gui.setDragAction(event -> event.setCancelled(true));
        this.gui.setDefaultClickAction(event -> {
            final int slot = event.getSlot();
            final Inventory inventory = event.getClickedInventory();
            if (inventory == null) {
                event.setCancelled(true);
                return;
            }
            final ClickType clickType = event.getClick();
            if (clickType == ClickType.SHIFT_RIGHT || clickType == ClickType.SHIFT_LEFT) {
                event.setCancelled(true);
                return;
            }
            if (event.getClickedInventory().equals(event.getView().getBottomInventory())) return;
            ItemStack tokenItem = event.getInventory().getItem(this.tokenSlot);
            if (slot != tokenSlot && slot != this.cosmeticSlot) {
                event.setCancelled(true);
                if (tokenItem == null) {
                    inventory.setItem(this.cosmeticSlot, new ItemStack(Material.AIR));
                }
                return;
            }
            final ItemStack inHand = event.getCursor();
            Token token;
            if (slot == this.tokenSlot) {
                if (inHand == null || inHand.getType() == Material.AIR) {
                    if (tokenItem != null && tokenItem.getAmount() > 1 && clickType == ClickType.RIGHT) return;
                    inventory.setItem(this.cosmeticSlot, new ItemStack(Material.AIR));
                    return;
                }
                token = this.cosmeticManager.getToken(inHand);
                if (token == null) {
                    event.setCancelled(true);
                    return;
                }
                final ArmorItem item = token.getArmorItem();
                inventory.setItem(this.cosmeticSlot,
                        this.applyPlaceholders(
                                user,
                                player,
                                item,
                                true
                        )
                );
                return;
            }
            if (inHand != null && inHand.getType() != Material.AIR) {
                event.setCancelled(true);
                return;
            }
            tokenItem = inventory.getItem(this.tokenSlot);
            token = this.cosmeticManager.getToken(tokenItem);
            if (tokenItem == null || token == null) {
                event.setCancelled(true);
                return;
            }
            final ItemStack clicked = event.getCurrentItem();
            final ArmorItem armorItem = token.getArmorItem();
            if (clicked == null) return;
            if (user.hasPermissionToUse(armorItem)) {
                this.messageHandler.sendMessage(
                        player,
                        Messages.ALREADY_UNLOCKED,
                        Map.of(Placeholder.ID, armorItem.getName())
                );
                event.setCancelled(true);
                return;
            }
            tokenItem.setAmount(tokenItem.getAmount() - 1);
            inventory.setItem(this.tokenSlot, tokenItem);
            clicked.setAmount(0);
            for (final String command : token.getCommands()) {
                Bukkit.dispatchCommand(
                        Bukkit.getConsoleSender(),
                        command.replace(Placeholder.PLAYER, player.getName())
                );
            }
            this.messageHandler.sendMessage(
                    player,
                    Messages.TRADED_TOKEN,
                    Map.of(Placeholder.ID, armorItem.getName())
            );
        });

        this.gui.setCloseGuiAction(event -> {
            final Inventory inventory = event.getInventory();
            final ItemStack tokens = inventory.getItem(this.tokenSlot);
            if (tokens == null) return;
            event.getPlayer().getInventory().addItem(tokens);
            user.setOpenGui(null);
        });
    }


    @Override
    public TokenGui copy() {
        return new TokenGui(
                this.plugin,
                super.title,
                super.rows,
                new HashMap<>(super.guiItemMap),
                this.tokenSlot,
                this.cosmeticSlot
        );
    }
}
