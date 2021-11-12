package io.github.fisher2911.hmccosmetics.gui;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import io.github.fisher2911.hmccosmetics.HMCCosmetics;
import io.github.fisher2911.hmccosmetics.inventory.PlayerArmor;
import io.github.fisher2911.hmccosmetics.user.User;
import io.github.fisher2911.hmccosmetics.util.builder.ColorBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.Map;
import java.util.Optional;

public class DyeSelectorGui extends CosmeticGui{

    public DyeSelectorGui(
            final HMCCosmetics plugin,
            final String title,
            final int rows,
            final Map<Integer, GuiItem> guiItemMap) {
        super(plugin, title, rows, guiItemMap);
    }

    public Gui getGui(final User user, final ArmorItem armorItem) {
        final Gui gui = Gui.gui().
                title(Component.text(this.title)).
                rows(rows).
                create();

        for (final var entry : this.guiItemMap.entrySet()) {
            gui.setItem(entry.getKey(), entry.getValue());
        }

        gui.setDefaultClickAction(event -> {
            event.setCancelled(true);

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
                event.getWhoClicked().sendMessage("ItemStack null");
                return;
            }

            if (!armorItem.isDyeable()) {
                event.getWhoClicked().sendMessage("Not dyeable");
                return;
            }

            final ColorBuilder colorBuilder =
                    ColorBuilder.from(itemStack);

            final GuiItem guiItem = this.guiItemMap.get(event.getSlot());

            if (!(guiItem instanceof final ColorItem colorItem)) {
                event.getWhoClicked().sendMessage("Not color item");
                return;
            }

            final Color color = colorItem.getColor();

            colorBuilder.color(color);

            final ArmorItem newArmorItem = new ArmorItem(
                    colorBuilder.build(),
                    armorItem.getAction(),
                    armorItem.getId(),
                    armorItem.getLockedLore(),
                    armorItem.getPermission(),
                    armorItem.getType(),
                    armorItem.isDyeable()
            );

            switch (type) {
                case HAT -> user.setHat(newArmorItem);
                case BACKPACK -> user.setBackpack(newArmorItem);
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
