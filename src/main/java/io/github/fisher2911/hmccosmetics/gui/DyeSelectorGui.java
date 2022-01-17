package io.github.fisher2911.hmccosmetics.gui;

import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import io.github.fisher2911.hmccosmetics.HMCCosmetics;
import io.github.fisher2911.hmccosmetics.inventory.PlayerArmor;
import io.github.fisher2911.hmccosmetics.user.User;
import io.github.fisher2911.hmccosmetics.util.StringUtils;
import io.github.fisher2911.hmccosmetics.util.builder.ColorBuilder;
import io.github.fisher2911.hmccosmetics.util.builder.ItemBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class DyeSelectorGui extends CosmeticGui {

    public DyeSelectorGui(
            final HMCCosmetics plugin,
            final String title,
            final int rows,
            final Map<Integer, GuiItem> guiItemMap) {
        super(plugin, title, rows, guiItemMap);
    }

    public Gui getGui(final User user, final ArmorItem armorItem) {
        final Gui gui = Gui.gui().
                title(Component.text(StringUtils.applyPapiPlaceholders(user.getPlayer(), this.title))).
                rows(rows).
                create();

        final Player player = user.getPlayer();

        for (final var entry : this.guiItemMap.entrySet()) {

            final GuiItem guiItem = entry.getValue();

            final ItemStack itemStack = this.itemStackMap.get(entry.getKey());

            if (itemStack == null) continue;

            guiItem.setItemStack(
                    ItemBuilder.from(itemStack.clone()).papiPlaceholders(player).build()
            );

            gui.setItem(entry.getKey(), guiItem);
        }

        gui.setDefaultClickAction(event -> {
            event.setCancelled(true);

            if (armorItem == null) {
                return;
            }

            final ArmorItem.Type type = armorItem.getType();

            final PlayerArmor playerArmor = user.getPlayerArmor();

            if (playerArmor == null) {
                return;
            }

            final ItemStack itemStack = switch (type) {
                case HAT -> {
                    final ArmorItem hatItem = playerArmor.getHat();

                    if (hatItem == null) {
                        yield null;
                    }
                    yield hatItem.getItemStack();
                }
                case BACKPACK -> {
                    final ArmorItem backpackItem = playerArmor.getBackpack();

                    if (backpackItem == null) {
                        yield null;
                    }
                    yield backpackItem.getItemStack();
                }
            };

            if (itemStack == null) {
                return;
            }

            if (!armorItem.isDyeable()) {
                return;
            }

            final GuiItem guiItem = this.guiItemMap.get(event.getSlot());

            if (!(guiItem instanceof final ColorItem colorItem)) {
                return;
            }

            final Color color = colorItem.getColor();
            user.setDye(color.asRGB());

            switch (type) {
                case HAT -> user.setHat(armorItem, this.plugin);
                case BACKPACK -> user.setBackpack(armorItem, this.plugin);
            }
        });

        return gui;
    }

    @Override
    public void open(final HumanEntity player) {
        final Optional<User> optionalUser = this.plugin.getUserManager().get(player.getUniqueId());
        optionalUser.ifPresent(user -> this.getGui(user, user.getLastSetItem()).open(player));
    }
}
