package io.github.fisher2911.hmccosmetics.gui;

import com.google.common.collect.HashBiMap;
import dev.triumphteam.gui.guis.GuiItem;
import io.github.fisher2911.hmccosmetics.HMCCosmetics;
import io.github.fisher2911.hmccosmetics.cosmetic.CosmeticManager;
import io.github.fisher2911.hmccosmetics.message.Messages;
import io.github.fisher2911.hmccosmetics.message.Placeholder;
import io.github.fisher2911.hmccosmetics.user.User;
import org.bukkit.entity.Player;
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
        player.sendMessage("Opened GUI");
        super.open(user, player);
        this.gui.setDefaultClickAction(event -> {
            player.sendMessage("Test");
            final int slot = event.getSlot();
            final Inventory inventory = event.getClickedInventory();
            if (inventory == null) {
                event.setCancelled(true);
                return;
            }
            if (event.getClickedInventory().equals(event.getView().getBottomInventory())) {
                player.sendMessage("Same inventory");
                return;
            }
            player.sendMessage("Not same");
            if (slot != tokenSlot && slot != this.cosmeticSlot) {
                event.setCancelled(true);
                return;
            }
            final ItemStack inHand = event.getCursor();
            if (slot == this.tokenSlot) {
                final Token token = this.cosmeticManager.getToken(inHand);
                if (token == null) {
                    event.setCancelled(true);
                    return;
                }
                final ArmorItem item = token.getArmorItem();
                inventory.setItem(this.cosmeticSlot, item.getItemStack(ArmorItem.Status.ALLOWED));
                return;
            }
            final ItemStack tokenItem = event.getInventory().getItem(this.tokenSlot);
            if (tokenItem == null) {
                event.setCancelled(true);
                return;
            }
            final Token token = this.cosmeticManager.getToken(tokenItem);
            if (token == null) {
                event.setCancelled(true);
                return;
            }
            tokenItem.setAmount(tokenItem.getAmount() - 1);
            inventory.setItem(this.tokenSlot, tokenItem);
            final ItemStack clicked = event.getCurrentItem();
            final ArmorItem armorItem = token.getArmorItem();
            if (clicked == null) return;
            if (user.hasPermissionToUse(armorItem)) {
                this.messageHandler.sendMessage(
                        player,
                        Messages.ALREADY_UNLOCKED,
                        Map.of(Placeholder.ID, armorItem.getItemName())
                );
                return;
            }
            clicked.setAmount(0);
            player.addAttachment(this.plugin, armorItem.getPermission(), true);
            this.messageHandler.sendMessage(
                    player,
                    Messages.TRADED_TOKEN,
                    Map.of(Placeholder.ID, armorItem.getItemName())
            );
        });

        this.gui.setCloseGuiAction(event -> {
            final Inventory inventory = event.getInventory();
            final ItemStack tokens = inventory.getItem(this.tokenSlot);
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
